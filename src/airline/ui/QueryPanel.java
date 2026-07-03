package airline.ui;

import airline.service.AirlineService;
import airline.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class QueryPanel extends JPanel {
    private AirlineService service;
    private JFrame parentFrame;

    public QueryPanel(AirlineService service, JFrame parentFrame) {
        this.service = service;
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(10, 10));
        UIUtils.padPanel(this);

        JPanel inputPanel = UIUtils.createInputPanel();
        inputPanel.add(new JLabel("终点站名:"));
        JTextField stationField = UIUtils.createTextField(12);
        inputPanel.add(stationField);
        JButton queryBtn = UIUtils.createButton("查询");
        inputPanel.add(queryBtn);

        String[] cols = {"起始站", "终点站", "航班号", "飞机号", "飞行日",
                "头等舱(余/总)", "商务舱(余/总)", "经济舱(余/总)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = UIUtils.createTable(model, 25);
        add(inputPanel, BorderLayout.NORTH);
        add(UIUtils.createTableScroll(table, "查询结果"), BorderLayout.CENTER);

        queryBtn.addActionListener(e -> {
            String station = stationField.getText().trim();
            if (station.isEmpty()) {
                JOptionPane.showMessageDialog(parentFrame, "请输入终点站名！");
                return;
            }
            FlightRoute[] results = service.queryRouteByStation(station);
            model.setRowCount(0);
            if (results.length == 0) {
                JOptionPane.showMessageDialog(parentFrame, "无此航线。");
                return;
            }
            for (FlightRoute r : results) {
                model.addRow(new Object[]{
                        r.originStation, r.terminalStation, r.flightNumber,
                        r.aircraftNumber, FlightRoute.dayOfWeek(r.flightDay),
                        r.firstClassRemaining + "/" + r.firstClassCapacity,
                        r.businessRemaining + "/" + r.businessCapacity,
                        r.economyRemaining + "/" + r.economyCapacity
                });
            }
        });
    }
}