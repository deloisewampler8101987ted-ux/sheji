package airline.ui;

import airline.service.AirlineService;
import airline.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class OverviewPanel extends JPanel {
    private DefaultTableModel model;

    public OverviewPanel(AirlineService service) {
        setLayout(new BorderLayout(10, 10));
        UIUtils.padPanel(this);

        model = new DefaultTableModel(new String[]{"起始站", "终点站", "航班号", "飞机号", "飞行日",
                "头等舱(余/总)", "商务舱(余/总)", "经济舱(余/总)"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = UIUtils.createTable(model, 25);

        JButton refreshBtn = UIUtils.createButton("刷新");
        JPanel btnPanel = UIUtils.createInputPanel();
        btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.NORTH);
        add(UIUtils.createTableScroll(table, null), BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> refresh(service));
        refresh(service);
    }

    private void refresh(AirlineService service) {
        model.setRowCount(0);
        FlightList list = service.getFlightList();
        for (int i = 0; i < list.getCount(); i++) {
            FlightRoute r = list.getRoute(i);
            model.addRow(new Object[]{
                    r.originStation, r.terminalStation, r.flightNumber,
                    r.aircraftNumber, FlightRoute.dayOfWeek(r.flightDay),
                    r.firstClassRemaining + "/" + r.firstClassCapacity,
                    r.businessRemaining + "/" + r.businessCapacity,
                    r.economyRemaining + "/" + r.economyCapacity
            });
        }
    }
}