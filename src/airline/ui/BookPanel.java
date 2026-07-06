package airline.ui;

import airline.service.AirlineService;
import airline.model.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// 预订面板主类 
// 该类负责航班预订的完整界面，包括航班查询、舱位选择、客票预订、候补队列和推荐航线
public class BookPanel extends JPanel {
    // 成员变量 
    private AirlineService service;
    private JFrame parent;

    /**
     * 构造方法：初始化预订面板界面，包含顶部查询区、中间航班结果表格和查询按钮事件
     * @param service 核心业务服务对象
     * @param parent  父窗口，用于弹出对话框
     */
    public BookPanel(AirlineService service, JFrame parent) {
        this.service = service;
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));
        UIUtils.padPanel(this);

        // 查询面板：起始地、目的地输入和查询按钮 
        JPanel searchPanel = UIUtils.createInputPanel();
        searchPanel.add(new JLabel("起始地:"));
        JTextField originField = UIUtils.createTextField(8);
        searchPanel.add(originField);
        searchPanel.add(new JLabel("目的地:"));
        JTextField destField = UIUtils.createTextField(8);
        searchPanel.add(destField);
        JButton searchBtn = UIUtils.createButton("查询航班");
        searchPanel.add(searchBtn);

        // 航班查询结果表格：展示航班信息，最后一列为预订按钮 
        String[] cols = {"航班号", "飞机号", "飞行日",
                "头等舱(余/总)", "商务舱(余/总)", "经济舱(余/总)", "操作"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return c == 6;
            }
        };
        JTable table = UIUtils.createTable(model, 28);
        table.getColumnModel().getColumn(6).setCellRenderer(new UIUtils.ButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new BookEditor(new JCheckBox(), model));

        JScrollPane tableScroll = UIUtils.createTableScroll(table, "查询结果（点击「预订」按钮操作）");

        // 界面布局组装：将查询面板和结果表格组合 
        add(searchPanel, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);

        // 查询按钮事件监听：根据起始地和目的地查询航班并展示结果 
        searchBtn.addActionListener(e -> {
            String o = originField.getText().trim();
            String d = destField.getText().trim();
            if (o.isEmpty() || d.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "请输入起始地和目的地！");
                return;
            }
            FlightRoute[] results = service.searchByRoute(o, d);
            model.setRowCount(0);
            if (results.length == 0) {
                JOptionPane.showMessageDialog(parent, "未找到从「" + o + "」到「" + d + "」的航班。");
                return;
            }
            for (FlightRoute r : results) {
                model.addRow(new Object[]{
                        r.flightNo, r.planeNo,
                        FlightRoute.dayOfWeek(r.day),
                        r.firstRem + "/" + r.firstCap,
                        r.bizRem + "/" + r.bizCap,
                        r.ecoRem + "/" + r.ecoCap,
                        "预订"
                });
            }
        });
    }

    /**
     * 预订按钮编辑器：将表格最后一列的预订按钮点击事件
     * 转换为订票交互流程（舱位选择-表单填写-调用服务层）
     */
    private class BookEditor extends DefaultCellEditor {
        private JButton btn = new JButton();
        private String flightNum;
        private DefaultTableModel model;
        private boolean pushed;

        /**
         * 构造方法：初始化按钮编辑器，设置按钮外观和点击事件
         * @param cb    JCheckBox 参数，满足 DefaultCellEditor 父类约束
         * @param model 表格数据模型，用于读取当前行的航班号
         */
        public BookEditor(JCheckBox cb, DefaultTableModel model) {
            super(cb);
            this.model = model;
            btn.setOpaque(true);
            btn.addActionListener(e -> fireEditingStopped());
        }

        /**
         * 当用户点击表格单元格时调用，读取当前行航班号并用按钮替换单元格
         * @param t 表格对象
         * @param v 单元格当前值
         * @param s 是否选中
         * @param r 行号
         * @param c 列号
         * @return 返回 JButton 作为单元格编辑器
         */
        public Component getTableCellEditorComponent(JTable t, Object v,
                boolean s, int r, int c) {
            flightNum = (String) model.getValueAt(r, 0);
            btn.setText(v == null ? "" : v.toString());
            pushed = true;
            return btn;
        }

        /**
         * 编辑结束后由表格调用，触发订票流程
         * @return 固定返回 "预订" 字符串，恢复单元格显示
         */
        public Object getCellEditorValue() {
            if (pushed) {
                pushed = false;
                handleBooking(flightNum);
            }
            return "预订";
        }

        /**
         * 处理预订逻辑：弹出舱位选择对话框，用户选择舱位后进入订票流程，
         * 若所选舱位售罄则引导用户选择其他舱位、加入候补队列或查看推荐航线
         * @param fn 航班号
         */
        private void handleBooking(String fn) {
            FlightRoute route = service.getFlightList().searchByFlight(fn);
            if (route == null) {
                return;
            }

            String[] cabinNames = {"头等舱", "商务舱", "经济舱"};

            while (true) {
                JPanel p = new JPanel(new GridLayout(4, 1, 5, 5));
                p.setBorder(new EmptyBorder(5, 5, 5, 5));
                JLabel titleLabel = new JLabel("航班 " + fn + "  ("
                        + route.origin + " → " + route.dest + ")");
                titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
                p.add(titleLabel);

                ButtonGroup bg = new ButtonGroup();
                JRadioButton[] rbs = new JRadioButton[3];
                for (int i = 0; i < 3; i++) {
                    int rem = route.cabinRem(i + 1);
                    int cap = route.cabinCap(i + 1);
                    rbs[i] = new JRadioButton(cabinNames[i]
                            + "  —  余票: " + rem + " / " + cap
                            + (rem == 0 ? "  (已售罄)" : ""));
                    rbs[i].setEnabled(true);
                    bg.add(rbs[i]);
                    p.add(rbs[i]);
                }

                if (route.firstRem > 0) {
                    rbs[0].setSelected(true);
                } else if (route.bizRem > 0) {
                    rbs[1].setSelected(true);
                } else if (route.ecoRem > 0) {
                    rbs[2].setSelected(true);
                } else {
                    rbs[2].setSelected(true);
                }

                int choice = JOptionPane.showConfirmDialog(parent, p, "选择舱位",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (choice != JOptionPane.OK_OPTION) {
                    return;
                }

                int cc = rbs[0].isSelected() ? 1 : rbs[1].isSelected() ? 2 : 3;
                int rem = route.cabinRem(cc);
                if (rem > 0) {
                    showBook(fn, route, cc, rem);
                    continue;
                }

                // 统计其他舱位余票
                int otherCount = 0;
                for (int i = 0; i < 3; i++) {
                    if (i + 1 != cc && route.cabinRem(i + 1) > 0) {
                        otherCount++;
                    }
                }

                if (otherCount > 0) {
                    Object[] cabinOptions = new Object[otherCount + 2];
                    int[] cabinMap = new int[otherCount];
                    int idx = 0;
                    for (int i = 0; i < 3; i++) {
                        if (i + 1 != cc && route.cabinRem(i + 1) > 0) {
                            cabinMap[idx] = i + 1;
                            cabinOptions[idx] = cabinNames[i]
                                    + "（余票: " + route.cabinRem(i + 1) + "）";
                            idx++;
                        }
                    }
                    cabinOptions[otherCount] = "加入候补队列";
                    cabinOptions[otherCount + 1] = "查看其他航线";

                    int pick = JOptionPane.showOptionDialog(parent,
                            "「" + cabinNames[cc - 1] + "」已售罄，可选其他舱位：",
                            "舱位已售罄",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null, cabinOptions, cabinOptions[0]);

                    if (pick >= 0 && pick < otherCount) {
                        showBook(fn, route, cabinMap[pick],
                                route.cabinRem(cabinMap[pick]));
                    } else if (pick == otherCount) {
                        showWait(fn, route, cc);
                    } else if (pick == otherCount + 1) {
                        showRecommend(fn, route.dest, cc, 1,
                                "「" + cabinNames[cc - 1] + "」暂无余票");
                    }
                } else {
                    Object[] options = {"加入候补队列", "查看其他航线", "取消"};
                    int waitChoice = JOptionPane.showOptionDialog(parent,
                            "所有舱位均已售罄，请选择：",
                            "舱位已售罄",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null, options, options[0]);
                    if (waitChoice == 0) {
                        showWait(fn, route, cc);
                    } else if (waitChoice == 1) {
                        showRecommend(fn, route.dest, cc, 1, "所有舱位均已售罄");
                    }
                }
                // 关闭子对话框 → 回到舱位选择
            }
        }

        /**
         * 弹出预订或候补表单对话框，收集客户姓名和票数信息
         * @param title       对话框标题
         * @param fn          航班号
         * @param routeInfo   航线信息（起终点）
         * @param cabinInfo   舱位信息（名称和余票）
         * @param ticketLabel 票数输入框标签
         * @return 包含姓名和票数的数组，用户取消返回 null
         */
        private String[] showForm(String title, String fn, String routeInfo,
                String cabinInfo, String ticketLabel) {
            JPanel p = new JPanel(new GridBagLayout());
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(5, 5, 5, 5);
            g.anchor = GridBagConstraints.WEST;

            addLabelValue(p, g, 0, "航班号:", fn + "  (" + routeInfo + ")");
            addLabelValue(p, g, 1, "舱位:", cabinInfo);
            JTextField nameField = UIUtils.createTextField(12);
            addLabelValue(p, g, 2, "客户姓名:", nameField);
            JTextField countField = UIUtils.createTextField(12);
            addLabelValue(p, g, 3, ticketLabel, countField);

            int result = JOptionPane.showConfirmDialog(parent, p, title,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return null;
            }

            String name = nameField.getText().trim();
            String cntStr = countField.getText().trim();
            if (name.isEmpty() || cntStr.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "请填写所有字段！");
                return null;
            }
            try {
                Integer.parseInt(cntStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(parent, "请输入有效的票数！");
                return null;
            }
            return new String[]{name, cntStr};
        }

        /**
         * 执行客票预订：调用服务层 bookTicket 方法，订票失败时自动推荐其他航线
         * @param fn    航班号
         * @param route 航线对象
         * @param cc    舱位等级（1-头等舱，2-商务舱，3-经济舱）
         * @param rem   当前舱位余票数
         */
        private void showBook(String fn, FlightRoute route, int cc, int rem) {
            String[] result = showForm("预订客票", fn,
                    route.origin + " → " + route.dest,
                    Customer.cabinName(cc) + "  (余票: " + rem + ")",
                    "订票数量:");
            if (result == null) {
                return;
            }
            int cnt = Integer.parseInt(result[1]);
            String msg = service.bookTicket(fn, cnt, result[0], cc);
            if (msg.contains("余票不足")) {
                showRecommend(fn, route.dest, cc, cnt, msg);
            } else {
                JOptionPane.showMessageDialog(parent, msg);
            }
        }

        /**
         * 辅助布局方法：向面板的指定行添加「标签-值」组合，
         * 值可以是 JLabel（显示文本）或 JComponent（输入框等交互组件）
         * @param p     目标面板
         * @param g     GridBagConstraints 布局约束
         * @param y     行号
         * @param label 标签文字
         * @param value 值，可以是 String 或 JComponent
         */
        private void addLabelValue(JPanel p, GridBagConstraints g, int y,
                String label, Object value) {
            g.gridx = 0;
            g.gridy = y;
            if (value instanceof JComponent) {
                p.add(new JLabel(label), g);
                g.gridx = 1;
                p.add((JComponent) value, g);
            } else {
                p.add(new JLabel(label), g);
                g.gridx = 1;
                p.add(new JLabel(value.toString()), g);
            }
        }

        /**
         * 显示推荐航线弹窗：当前航班余票不足时，查找到达同一目的地的其他有余票航班，
         * 以表格形式展示，用户可点击「预订」按钮直接订票
         * @param exclude  需要排除的航班号（当前已失败的航班）
         * @param dest     目的地
         * @param cc       舱位等级
         * @param need     需要的票数
         * @param failMsg  订票失败原因信息
         */
        private void showRecommend(String exclude, String dest, int cc,
                int need, String failMsg) {
            FlightRoute[] recs = service.recommendSameDestination(
                    exclude, dest, cc, need);

            JPanel p = new JPanel(new BorderLayout(10, 10));
            JLabel failLabel = new JLabel(
                    "<html>" + failMsg.replace("\n", "<br>") + "</html>");
            failLabel.setForeground(Color.RED);
            p.add(failLabel, BorderLayout.NORTH);

            if (recs.length == 0) {
                p.add(new JLabel("暂无可到达「" + dest + "」的其他航线。"),
                        BorderLayout.CENTER);
            } else {
                DefaultTableModel rm = new DefaultTableModel(new String[]{
                        "航班号", "起始站", "飞机号", "飞行日",
                        Customer.cabinName(cc) + "余票", "操作"}, 0) {
                    public boolean isCellEditable(int r, int c) {
                        return c == 5;
                    }
                };
                for (FlightRoute r : recs) {
                    rm.addRow(new Object[]{
                            r.flightNo, r.origin, r.planeNo,
                            FlightRoute.dayOfWeek(r.day),
                            r.cabinRem(cc), "预订"
                    });
                }
                JTable rt = UIUtils.createTable(rm, 25);
                rt.getColumnModel().getColumn(5)
                        .setCellRenderer(new UIUtils.ButtonRenderer());
                rt.getColumnModel().getColumn(5)
                        .setCellEditor(new RecEditor(new JCheckBox(), rm, cc));
                JScrollPane sp = UIUtils.createTableScroll(rt,
                        "推荐以下到达「" + dest + "」的航线");
                sp.setPreferredSize(new Dimension(600, 150));
                p.add(sp, BorderLayout.CENTER);
            }
            JOptionPane.showMessageDialog(parent, p,
                    "订票失败 - 为您推荐其他航线",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        /**
         * 推荐航线表格中的预订按钮编辑器：处理推荐航线表格中「预订」按钮的点击，
         * 点击后直接进入该航班的订票流程
         */
        private class RecEditor extends DefaultCellEditor {
            private JButton btn = new JButton();
            private String fn;
            private int rem, cc;
            private DefaultTableModel rm;
            private boolean pushed;

            /**
             * 构造方法：初始化推荐航线表格的按钮编辑器
             * @param cb JCheckBox 参数，满足 DefaultCellEditor 父类约束
             * @param rm 推荐航线表格的数据模型
             * @param cc 当前推荐的舱位等级
             */
            public RecEditor(JCheckBox cb, DefaultTableModel rm, int cc) {
                super(cb);
                this.rm = rm;
                this.cc = cc;
                btn.setOpaque(true);
                btn.addActionListener(e -> fireEditingStopped());
            }

            /**
             * 当用户点击推荐航线表格单元格时调用，读取航班号和余票数
             * @param t 表格对象
             * @param v 单元格当前值
             * @param s 是否选中
             * @param r 行号
             * @param c 列号
             * @return 返回 JButton 作为单元格编辑器
             */
            public Component getTableCellEditorComponent(JTable t, Object v,
                    boolean s, int r, int c) {
                fn = (String) rm.getValueAt(r, 0);
                rem = (Integer) rm.getValueAt(r, 4);
                btn.setText(v == null ? "" : v.toString());
                pushed = true;
                return btn;
            }

            /**
             * 编辑结束后由表格调用，查找航线后进入订票流程
             * @return 固定返回 "预订" 字符串
             */
            public Object getCellEditorValue() {
                if (pushed) {
                    pushed = false;
                    FlightRoute rr = service.getFlightList().searchByFlight(fn);
                    if (rr != null) {
                        showBook(fn, rr, cc, rem);
                    }
                }
                return "预订";
            }
        }

        /**
         * 加入候补队列：当舱位售罄时，弹出表单收集客户信息后加入候补等待退票，
         * 候补客户按 FIFO 顺序在有退票时自动替补订票
         * @param fn    航班号
         * @param route 航线对象
         * @param cc    舱位等级
         */
        private void showWait(String fn, FlightRoute route, int cc) {
            String[] result = showForm("加入候补队列", fn,
                    route.origin + " → " + route.dest,
                    Customer.cabinName(cc) + "  (当前候补: "
                            + route.queue.size() + " 人)",
                    "候补票数:");
            if (result == null) {
                return;
            }
            JOptionPane.showMessageDialog(parent,
                    service.joinWaitQueue(fn, Integer.parseInt(result[1]), result[0], cc));
        }
    }
}