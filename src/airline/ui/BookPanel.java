// 包声明和导入 ====================
package airline.ui;

import airline.service.AirlineService;
import airline.model.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// 预订面板主类 ====================
// 该类负责航班预订的完整界面，包括航班查询、舱位选择、客票预订、候补队列和推荐航线
public class BookPanel extends JPanel {
    // 成员变量 ====================
    private AirlineService service;
    private JFrame parent;

    // 构造函数：初始化预订面板界面 ====================
    public BookPanel(AirlineService service, JFrame parent) {
        this.service = service;
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));
        UIUtils.padPanel(this);

        // 查询面板：起始地、目的地输入和查询按钮 =====
        JPanel searchPanel = UIUtils.createInputPanel();
        searchPanel.add(new JLabel("起始地:"));
        JTextField originField = UIUtils.createTextField(8);
        searchPanel.add(originField);
        searchPanel.add(new JLabel("目的地:"));
        JTextField destField = UIUtils.createTextField(8);
        searchPanel.add(destField);
        JButton searchBtn = UIUtils.createButton("查询航班");
        searchPanel.add(searchBtn);

        // 航班查询结果表格：展示航班信息，最后一列为预订按钮 =====
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

        // 操作提示区域：显示帮助信息和查询结果提示 =====
        JTextArea tip = new JTextArea();
        tip.setEditable(false);
        tip.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tip.setForeground(new Color(100, 100, 100));
        tip.setText("  提示：输入起始地和目的地后点击「查询航班」，在结果中点击对应航班的「预订」按钮。");
        JScrollPane tipScroll = new JScrollPane(tip);
        tipScroll.setPreferredSize(new Dimension(0, 50));
        tipScroll.setBorder(new TitledBorder("操作提示"));

        // 界面布局组装：将查询面板、结果表格和提示区域组合 =====
        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.add(tableScroll, BorderLayout.CENTER);
        center.add(tipScroll, BorderLayout.SOUTH);
        add(searchPanel, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        // 查询按钮事件监听：根据起始地和目的地查询航班并展示结果 =====
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
                tip.setText("  未找到从「" + o + "」到「" + d + "」的航班。");
                return;
            }
            tip.setText("  共找到 " + results.length + " 个航班。点击右侧「预订」按钮进行操作。");
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

    // 预订按钮编辑器：处理航班结果表格中「预订」按钮的点击事件 ====================
    private class BookEditor extends DefaultCellEditor {
        private JButton btn = new JButton();
        private String flightNum;
        private DefaultTableModel model;
        private boolean pushed;

        public BookEditor(JCheckBox cb, DefaultTableModel model) {
            super(cb);
            this.model = model;
            btn.setOpaque(true);
            btn.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable t, Object v,
                boolean s, int r, int c) {
            flightNum = (String) model.getValueAt(r, 0);
            btn.setText(v == null ? "" : v.toString());
            pushed = true;
            return btn;
        }

        public Object getCellEditorValue() {
            if (pushed) {
                pushed = false;
                handleBooking(flightNum);
            }
            return "预订";
        }

        // 处理预订逻辑：选择舱位、处理售罄情况、提供候补和推荐选项 =====
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

        // 显示预订/候补表单：收集客户姓名和票数信息 =====
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

        // 执行预订操作：调用服务层进行客票预订，余票不足时推荐其他航线 =====
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

        // 辅助方法：向面板添加「标签-值」行，支持 JLabel 和 JComponent 两种值类型 =====
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

        // 显示推荐航线：当前航班余票不足时，推荐到达同一目的地的其他航班 =====
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

        // 推荐航线表格中的预订按钮编辑器：处理推荐航线表格中「预订」按钮的点击 =====
        private class RecEditor extends DefaultCellEditor {
            private JButton btn = new JButton();
            private String fn;
            private int rem, cc;
            private DefaultTableModel rm;
            private boolean pushed;

            public RecEditor(JCheckBox cb, DefaultTableModel rm, int cc) {
                super(cb);
                this.rm = rm;
                this.cc = cc;
                btn.setOpaque(true);
                btn.addActionListener(e -> fireEditingStopped());
            }

            public Component getTableCellEditorComponent(JTable t, Object v,
                    boolean s, int r, int c) {
                fn = (String) rm.getValueAt(r, 0);
                rem = (Integer) rm.getValueAt(r, 4);
                btn.setText(v == null ? "" : v.toString());
                pushed = true;
                return btn;
            }

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

        // 加入候补队列：当舱位售罄时，将客户加入候补等待退票 =====
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