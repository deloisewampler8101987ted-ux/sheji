package airline.ui;

import airline.service.AirlineService;
import airline.model.*;
import airline.datastructure.LinkNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AirlineGUI extends JFrame {
    private AirlineService service;
    private JTabbedPane tabbedPane;

    private JTable routeTable;
    private DefaultTableModel routeTableModel;

    private JTable customerTable;
    private DefaultTableModel customerTableModel;
    private JTable waiterTable;
    private DefaultTableModel waiterTableModel;

    public AirlineGUI() {
        this.service = new AirlineService();
        initUI();
    }

    private void initUI() {
        setTitle("航空客运订票系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 680);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("客票预订", createBookPanel());
        tabbedPane.addTab("航线查询", createQueryPanel());
        tabbedPane.addTab("办理退票", createRefundPanel());
        tabbedPane.addTab("航线总览", createOverviewPanel());
        tabbedPane.addTab("客户与队列", createCustomerPanel());

        add(tabbedPane);
    }

    // ==================== Tab 1: 客票预订 ====================
    private JPanel createBookPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.add(new JLabel("起始地:"));
        JTextField originField = new JTextField(8);
        searchPanel.add(originField);
        searchPanel.add(new JLabel("目的地:"));
        JTextField destField = new JTextField(8);
        searchPanel.add(destField);
        JButton searchBtn = new JButton("查询航班");
        searchPanel.add(searchBtn);

        String[] cols = {"航班号", "飞机号", "飞行日",
                "头等舱(余/总)", "商务舱(余/总)", "经济舱(余/总)", "操作"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return col == 6; }
        };
        JTable flightTable = new JTable(tableModel);
        flightTable.setRowHeight(28);
        flightTable.getTableHeader().setReorderingAllowed(false);
        flightTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        flightTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        flightTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        flightTable.getColumnModel().getColumn(6).setCellEditor(
                new ButtonEditor(new JCheckBox(), flightTable, tableModel));

        JScrollPane tableScroll = new JScrollPane(flightTable);
        tableScroll.setBorder(new TitledBorder("查询结果（点击「预订」按钮操作）"));

        JTextArea tipArea = new JTextArea();
        tipArea.setEditable(false);
        tipArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tipArea.setForeground(new Color(100, 100, 100));
        tipArea.setText("  提示：输入起始地和目的地后点击「查询航班」，在结果中点击对应航班的「预订」按钮。");
        JScrollPane tipScroll = new JScrollPane(tipArea);
        tipScroll.setPreferredSize(new Dimension(0, 50));
        tipScroll.setBorder(new TitledBorder("操作提示"));

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        centerPanel.add(tipScroll, BorderLayout.SOUTH);

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        searchBtn.addActionListener(e -> {
            String origin = originField.getText().trim();
            String dest = destField.getText().trim();
            if (origin.isEmpty() || dest.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入起始地和目的地！");
                return;
            }
            FlightRoute[] results = service.searchByRoute(origin, dest);
            tableModel.setRowCount(0);
            if (results.length == 0) {
                tipArea.setText("  未找到从「" + origin + "」到「" + dest + "」的航班。");
            } else {
                tipArea.setText("  共找到 " + results.length + " 个航班。点击右侧「预订」按钮进行操作。");
                for (FlightRoute r : results) {
                    tableModel.addRow(new Object[]{
                            r.flightNumber,
                            r.aircraftNumber,
                            FlightRoute.dayOfWeek(r.flightDay),
                            r.firstClassRemaining + "/" + r.firstClassCapacity,
                            r.businessRemaining + "/" + r.businessCapacity,
                            r.economyRemaining + "/" + r.economyCapacity,
                            "预订"
                    });
                }
            }
        });

        return panel;
    }

    // ==================== 按钮渲染器 ====================
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // ==================== 按钮编辑器 ====================
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String flightNum;
        private DefaultTableModel tableModel;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox, JTable table, DefaultTableModel tableModel) {
            super(checkBox);
            this.tableModel = tableModel;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            flightNum = (String) tableModel.getValueAt(row, 0);
            button.setText((value == null) ? "" : value.toString());
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                handleBooking(flightNum);
            }
            isPushed = false;
            return "预订";
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        private void handleBooking(String flightNum) {
            FlightRoute route = service.getFlightList().searchByFlight(flightNum);
            if (route == null) return;

            // 构建舱位选择面板，显示各舱位余票
            JPanel cabinPanel = new JPanel(new GridLayout(4, 1, 5, 5));
            cabinPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            JLabel titleLabel = new JLabel("航班 " + flightNum + "  ("
                    + route.originStation + " → " + route.terminalStation + ")");
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            cabinPanel.add(titleLabel);

            ButtonGroup group = new ButtonGroup();
            JRadioButton firstBtn = new JRadioButton("头等舱  —  余票: " + route.firstClassRemaining
                    + " / " + route.firstClassCapacity
                    + (route.firstClassRemaining == 0 ? "  (已售罄)" : ""));
            firstBtn.setActionCommand("1");
            firstBtn.setEnabled(route.firstClassRemaining > 0);
            group.add(firstBtn);
            cabinPanel.add(firstBtn);

            JRadioButton businessBtn = new JRadioButton("商务舱  —  余票: " + route.businessRemaining
                    + " / " + route.businessCapacity
                    + (route.businessRemaining == 0 ? "  (已售罄)" : ""));
            businessBtn.setActionCommand("2");
            businessBtn.setEnabled(route.businessRemaining > 0);
            group.add(businessBtn);
            cabinPanel.add(businessBtn);

            JRadioButton economyBtn = new JRadioButton("经济舱  —  余票: " + route.economyRemaining
                    + " / " + route.economyCapacity
                    + (route.economyRemaining == 0 ? "  (已售罄)" : ""));
            economyBtn.setActionCommand("3");
            economyBtn.setEnabled(route.economyRemaining > 0);
            group.add(economyBtn);
            cabinPanel.add(economyBtn);

            // 默认选中第一个有余票的舱位
            if (route.firstClassRemaining > 0) firstBtn.setSelected(true);
            else if (route.businessRemaining > 0) businessBtn.setSelected(true);
            else if (route.economyRemaining > 0) economyBtn.setSelected(true);
            else economyBtn.setSelected(true);

            int cabinChoice = JOptionPane.showConfirmDialog(
                    AirlineGUI.this, cabinPanel, "选择舱位",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (cabinChoice != JOptionPane.OK_OPTION) return;

            int cabinClass;
            if (firstBtn.isSelected()) cabinClass = 1;
            else if (businessBtn.isSelected()) cabinClass = 2;
            else cabinClass = 3;

            int selectedRemaining = route.getRemainingByCabin(cabinClass);

            if (selectedRemaining > 0) {
                showBookDialog(flightNum, route, cabinClass, selectedRemaining);
            } else {
                // 该舱位无余票 → 询问是否候补
                int choice = JOptionPane.showConfirmDialog(
                        AirlineGUI.this,
                        "该舱位暂无余票。\n\n是否加入候补队列？",
                        "候补确认",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    showWaitDialog(flightNum, route, cabinClass);
                }
            }
        }

        private void showBookDialog(String flightNum, FlightRoute route,
                                     int cabinClass, int remaining) {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(new JLabel("航班号:"), gbc);
            gbc.gridx = 1;
            panel.add(new JLabel(flightNum + "  (" + route.originStation
                    + " → " + route.terminalStation + ")"), gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(new JLabel("舱位:"), gbc);
            gbc.gridx = 1;
            panel.add(new JLabel(Customer.getCabinNameStatic(cabinClass)
                    + "  (余票: " + remaining + ")"), gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            panel.add(new JLabel("客户姓名:"), gbc);
            gbc.gridx = 1;
            JTextField nameField = new JTextField(12);
            panel.add(nameField, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            panel.add(new JLabel("订票数量:"), gbc);
            gbc.gridx = 1;
            JTextField countField = new JTextField(12);
            panel.add(countField, gbc);

            int result = JOptionPane.showConfirmDialog(
                    AirlineGUI.this, panel, "预订客票",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String name = nameField.getText().trim();
                String countStr = countField.getText().trim();
                if (name.isEmpty() || countStr.isEmpty()) {
                    JOptionPane.showMessageDialog(AirlineGUI.this, "请填写所有字段！");
                    return;
                }
                int count;
                try {
                    count = Integer.parseInt(countStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(AirlineGUI.this, "请输入有效的订票数量！");
                    return;
                }
                String msg = service.bookTicket(flightNum, count, name, cabinClass);
                JOptionPane.showMessageDialog(AirlineGUI.this, msg);
            }
        }

        private void showWaitDialog(String flightNum, FlightRoute route, int cabinClass) {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(new JLabel("航班号:"), gbc);
            gbc.gridx = 1;
            panel.add(new JLabel(flightNum + "  (" + route.originStation
                    + " → " + route.terminalStation + ")"), gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(new JLabel("舱位:"), gbc);
            gbc.gridx = 1;
            panel.add(new JLabel(Customer.getCabinNameStatic(cabinClass)
                    + "  (当前候补: " + route.waitQueue.size() + " 人)"), gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            panel.add(new JLabel("客户姓名:"), gbc);
            gbc.gridx = 1;
            JTextField nameField = new JTextField(12);
            panel.add(nameField, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            panel.add(new JLabel("候补票数:"), gbc);
            gbc.gridx = 1;
            JTextField countField = new JTextField(12);
            panel.add(countField, gbc);

            int result = JOptionPane.showConfirmDialog(
                    AirlineGUI.this, panel, "加入候补队列",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String name = nameField.getText().trim();
                String countStr = countField.getText().trim();
                if (name.isEmpty() || countStr.isEmpty()) {
                    JOptionPane.showMessageDialog(AirlineGUI.this, "请填写所有字段！");
                    return;
                }
                int count;
                try {
                    count = Integer.parseInt(countStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(AirlineGUI.this, "请输入有效的票数！");
                    return;
                }
                String msg = service.joinWaitQueue(flightNum, count, name, cabinClass);
                JOptionPane.showMessageDialog(AirlineGUI.this, msg);
            }
        }
    }

    // ==================== Tab 2: 航线查询 ====================
    private JPanel createQueryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        inputPanel.add(new JLabel("终点站名:"));
        JTextField stationField = new JTextField(12);
        inputPanel.add(stationField);
        JButton queryBtn = new JButton("查询");
        inputPanel.add(queryBtn);

        String[] cols = {"起始站", "终点站", "航班号", "飞机号", "飞行日",
                "头等舱(余/总)", "商务舱(余/总)", "经济舱(余/总)"};
        DefaultTableModel queryTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable queryTable = new JTable(queryTableModel);
        queryTable.setRowHeight(25);
        queryTable.getTableHeader().setReorderingAllowed(false);
        queryTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        queryTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(queryTable);
        scrollPane.setBorder(new TitledBorder("查询结果"));

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        queryBtn.addActionListener(e -> {
            String station = stationField.getText().trim();
            if (station.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入终点站名！");
                return;
            }
            FlightRoute[] results = service.queryRouteByStation(station);
            queryTableModel.setRowCount(0);
            if (results.length == 0) {
                JOptionPane.showMessageDialog(this, "无此航线。");
            } else {
                for (FlightRoute r : results) {
                    queryTableModel.addRow(new Object[]{
                            r.originStation, r.terminalStation,
                            r.flightNumber, r.aircraftNumber,
                            FlightRoute.dayOfWeek(r.flightDay),
                            r.firstClassRemaining + "/" + r.firstClassCapacity,
                            r.businessRemaining + "/" + r.businessCapacity,
                            r.economyRemaining + "/" + r.economyCapacity
                    });
                }
            }
        });

        return panel;
    }

    // ==================== Tab 3: 办理退票 ====================
    private JPanel createRefundPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        inputPanel.add(new JLabel("航班号:"));
        JTextField flightField = new JTextField(10);
        inputPanel.add(flightField);
        inputPanel.add(new JLabel("客户姓名:"));
        JTextField nameField = new JTextField(10);
        inputPanel.add(nameField);
        JButton refundBtn = new JButton("退票");
        inputPanel.add(refundBtn);

        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(new TitledBorder("退票结果"));

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refundBtn.addActionListener(e -> {
            String flightNum = flightField.getText().trim();
            String name = nameField.getText().trim();
            if (flightNum.isEmpty() || name.isEmpty()) {
                resultArea.setText("请填写航班号和客户姓名！");
                return;
            }
            String result = service.refundTicket(flightNum, name);
            resultArea.setText(result);
        });

        return panel;
    }

    // ==================== Tab 4: 航线总览 ====================
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] columns = {"起始站", "终点站", "航班号", "飞机号", "飞行日",
                "头等舱(余/总)", "商务舱(余/总)", "经济舱(余/总)"};
        routeTableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        routeTable = new JTable(routeTableModel);
        routeTable.setRowHeight(25);
        routeTable.getTableHeader().setReorderingAllowed(false);
        routeTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        routeTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(routeTable);

        JButton refreshBtn = new JButton("刷新");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(refreshBtn);

        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> refreshRouteTable());
        refreshRouteTable();

        return panel;
    }

    private void refreshRouteTable() {
        routeTableModel.setRowCount(0);
        FlightList list = service.getFlightList();
        for (int i = 0; i < list.getCount(); i++) {
            FlightRoute r = list.getRoute(i);
            routeTableModel.addRow(new Object[]{
                    r.originStation, r.terminalStation,
                    r.flightNumber, r.aircraftNumber,
                    FlightRoute.dayOfWeek(r.flightDay),
                    r.firstClassRemaining + "/" + r.firstClassCapacity,
                    r.businessRemaining + "/" + r.businessCapacity,
                    r.economyRemaining + "/" + r.economyCapacity
            });
        }
    }

    // ==================== Tab 5: 客户与队列 ====================
    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        inputPanel.add(new JLabel("航班号:"));
        JTextField flightField = new JTextField(10);
        inputPanel.add(flightField);
        JButton queryBtn = new JButton("查询");
        inputPanel.add(queryBtn);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        String[] custCols = {"姓名", "订票数", "舱位"};
        customerTableModel = new DefaultTableModel(custCols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        customerTable = new JTable(customerTableModel);
        customerTable.setRowHeight(22);
        customerTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane custScroll = new JScrollPane(customerTable);
        custScroll.setBorder(new TitledBorder("已订票客户"));

        String[] waiterCols = {"姓名", "需要票数", "舱位"};
        waiterTableModel = new DefaultTableModel(waiterCols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        waiterTable = new JTable(waiterTableModel);
        waiterTable.setRowHeight(22);
        waiterTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane waiterScroll = new JScrollPane(waiterTable);
        waiterScroll.setBorder(new TitledBorder("等候队列"));

        splitPane.setTopComponent(custScroll);
        splitPane.setBottomComponent(waiterScroll);
        splitPane.setResizeWeight(0.5);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);

        queryBtn.addActionListener(e -> {
            String flightNum = flightField.getText().trim();
            if (flightNum.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入航班号！");
                return;
            }
            refreshCustomerTables(flightNum);
        });

        return panel;
    }

    private void refreshCustomerTables(String flightNum) {
        FlightRoute route = service.getFlightList().searchByFlight(flightNum);
        if (route == null) {
            customerTableModel.setRowCount(0);
            waiterTableModel.setRowCount(0);
            JOptionPane.showMessageDialog(this, "航班不存在！");
            return;
        }

        customerTableModel.setRowCount(0);
        LinkNode<Customer> custCurr = route.bookedList.getHead().next;
        while (custCurr != null) {
            Customer c = custCurr.data;
            customerTableModel.addRow(new Object[]{c.name, c.ticketCount, c.getCabinName()});
            custCurr = custCurr.next;
        }

        waiterTableModel.setRowCount(0);
        LinkNode<Waiter> waitCurr = route.waitQueue.getFront();
        while (waitCurr != null) {
            Waiter w = waitCurr.data;
            waiterTableModel.addRow(new Object[]{
                    w.name, w.ticketCount,
                    Customer.getCabinNameStatic(w.cabinClass)});
            waitCurr = waitCurr.next;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new AirlineGUI().setVisible(true);
        });
    }
}