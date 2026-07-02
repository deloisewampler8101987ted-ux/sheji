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

    // 航线总览表格
    private JTable routeTable;
    private DefaultTableModel routeTableModel;

    // 客户/队列表格
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
        setSize(850, 620);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("航线查询", createQueryPanel());
        tabbedPane.addTab("客票预订", createBookPanel());
        tabbedPane.addTab("办理退票", createRefundPanel());
        tabbedPane.addTab("航线总览", createOverviewPanel());
        tabbedPane.addTab("客户与队列", createCustomerPanel());

        add(tabbedPane);
    }

    // ==================== Tab 1: 航线查询 ====================
    private JPanel createQueryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 输入区
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        inputPanel.add(new JLabel("终点站名:"));
        JTextField stationField = new JTextField(12);
        inputPanel.add(stationField);
        JButton queryBtn = new JButton("查询");
        inputPanel.add(queryBtn);

        // 结果区
        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(new TitledBorder("查询结果"));

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        queryBtn.addActionListener(e -> {
            String station = stationField.getText().trim();
            if (station.isEmpty()) {
                resultArea.setText("请输入终点站名！");
                return;
            }
            String result = service.queryRoute(station);
            resultArea.setText(result);
        });

        return panel;
    }

    // ==================== Tab 2: 客票预订 ====================
    private JPanel createBookPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 输入区
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("航班号:"), gbc);
        gbc.gridx = 1;
        JTextField flightField = new JTextField(12);
        inputPanel.add(flightField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("订票数量:"), gbc);
        gbc.gridx = 1;
        JTextField countField = new JTextField(12);
        inputPanel.add(countField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("客户姓名:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(12);
        inputPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("舱位等级:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> cabinBox = new JComboBox<>(
                new String[]{"头等舱", "商务舱", "经济舱"});
        inputPanel.add(cabinBox, gbc);

        // 按钮区
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton bookBtn = new JButton("预订");
        JButton waitBtn = new JButton("加入等候队列");
        btnPanel.add(bookBtn);
        btnPanel.add(waitBtn);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(btnPanel, BorderLayout.SOUTH);

        // 结果区
        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(new TitledBorder("预订结果"));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 预订按钮
        bookBtn.addActionListener(e -> {
            String flightNum = flightField.getText().trim();
            String countStr = countField.getText().trim();
            String name = nameField.getText().trim();
            int cabinClass = cabinBox.getSelectedIndex() + 1;

            if (flightNum.isEmpty() || countStr.isEmpty() || name.isEmpty()) {
                resultArea.setText("请填写所有字段！");
                return;
            }
            int count;
            try {
                count = Integer.parseInt(countStr);
            } catch (NumberFormatException ex) {
                resultArea.setText("请输入有效的订票数量！");
                return;
            }

            String result = service.bookTicket(flightNum, count, name, cabinClass);
            resultArea.setText(result);
        });

        // 等候队列按钮
        waitBtn.addActionListener(e -> {
            String flightNum = flightField.getText().trim();
            String countStr = countField.getText().trim();
            String name = nameField.getText().trim();

            if (flightNum.isEmpty() || countStr.isEmpty() || name.isEmpty()) {
                resultArea.setText("请填写所有字段！");
                return;
            }
            int count;
            try {
                count = Integer.parseInt(countStr);
            } catch (NumberFormatException ex) {
                resultArea.setText("请输入有效的订票数量！");
                return;
            }

            String result = service.joinWaitQueue(flightNum, count, name);
            resultArea.setText(result);
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

        // 表格
        String[] columns = {"终点站", "航班号", "飞机号", "飞行日", "乘员定额", "余票量"};
        routeTableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        routeTable = new JTable(routeTableModel);
        routeTable.setRowHeight(25);
        routeTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        routeTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(routeTable);

        JButton refreshBtn = new JButton("刷新");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(refreshBtn);

        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> refreshRouteTable());
        // 初始加载
        refreshRouteTable();

        return panel;
    }

    private void refreshRouteTable() {
        routeTableModel.setRowCount(0);
        FlightList list = service.getFlightList();
        for (int i = 0; i < list.getCount(); i++) {
            FlightRoute r = list.getRoute(i);
            routeTableModel.addRow(new Object[]{
                    r.terminalStation,
                    r.flightNumber,
                    r.aircraftNumber,
                    FlightRoute.dayOfWeek(r.flightDay),
                    r.capacity,
                    r.remainingTickets
            });
        }
    }

    // ==================== Tab 5: 客户与队列 ====================
    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 输入区
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        inputPanel.add(new JLabel("航班号:"));
        JTextField flightField = new JTextField(10);
        inputPanel.add(flightField);
        JButton queryBtn = new JButton("查询");
        inputPanel.add(queryBtn);

        // 表格区
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        // 已订票客户表
        String[] custCols = {"姓名", "订票数", "舱位"};
        customerTableModel = new DefaultTableModel(custCols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        customerTable = new JTable(customerTableModel);
        customerTable.setRowHeight(22);
        JScrollPane custScroll = new JScrollPane(customerTable);
        custScroll.setBorder(new TitledBorder("已订票客户"));

        // 等候队列表
        String[] waiterCols = {"姓名", "需要票数"};
        waiterTableModel = new DefaultTableModel(waiterCols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        waiterTable = new JTable(waiterTableModel);
        waiterTable.setRowHeight(22);
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

        // 已订票客户
        customerTableModel.setRowCount(0);
        LinkNode<Customer> custCurr = route.bookedList.getHead();
        while (custCurr != null) {
            Customer c = custCurr.data;
            customerTableModel.addRow(new Object[]{
                    c.name, c.ticketCount, c.getCabinName()
            });
            custCurr = custCurr.next;
        }

        // 等候队列
        waiterTableModel.setRowCount(0);
        LinkNode<Waiter> waitCurr = route.waitQueue.getFront();
        while (waitCurr != null) {
            Waiter w = waitCurr.data;
            waiterTableModel.addRow(new Object[]{w.name, w.ticketCount});
            waitCurr = waitCurr.next;
        }
    }

    // ==================== 启动 ====================
    public static void main(String[] args) {
        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new AirlineGUI().setVisible(true);
        });
    }
}