package airline.ui;

import airline.service.AirlineService;
import airline.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * 航线总览面板：以表格形式展示所有航线的完整信息，
 * 包含起始站、终点站、航班号、飞机号、飞行日及各舱位余票/定额
 */
public class OverviewPanel extends JPanel {
    /** 航线总览表格的数据模型 */
    private DefaultTableModel model;

    /**
     * 构造方法：初始化航线总览面板，加载所有航线数据并展示在表格中
     * @param service 核心业务服务对象
     */
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

        // 刷新按钮事件监听：重新加载所有航线数据
        refreshBtn.addActionListener(e -> refresh(service));
        refresh(service);
    }

    /**
     * 刷新表格数据：清空当前表格，重新从服务层加载所有航线信息
     * @param service 核心业务服务对象
     */
    private void refresh(AirlineService service) {
        model.setRowCount(0);
        FlightList list = service.getFlightList();
        for (int i = 0; i < list.getCount(); i++) {
            FlightRoute r = list.getRoute(i);
            model.addRow(new Object[]{
                    r.origin, r.dest, r.flightNo,
                    r.planeNo, FlightRoute.dayOfWeek(r.day),
                    r.firstRem + "/" + r.firstCap,
                    r.bizRem + "/" + r.bizCap,
                    r.ecoRem + "/" + r.ecoCap
            });
        }
    }
}