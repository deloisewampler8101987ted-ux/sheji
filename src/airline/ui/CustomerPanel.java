package airline.ui;

import airline.service.AirlineService;
import airline.model.*;
import airline.datastructure.LinkNode;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CustomerPanel extends JPanel {
    private AirlineService service;
    private JFrame parentFrame;
    private DefaultTableModel custModel, waitModel;

    public CustomerPanel(AirlineService service, JFrame parentFrame) {
        this.service = service;
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(10, 10));
        UIUtils.padPanel(this);

        JPanel inputPanel = UIUtils.createInputPanel();
        inputPanel.add(new JLabel("航班号:"));
        JTextField flightField = UIUtils.createTextField(10);
        inputPanel.add(flightField);
        JButton queryBtn = UIUtils.createButton("查询");
        inputPanel.add(queryBtn);

        custModel = new DefaultTableModel(new String[]{"姓名", "订票数", "舱位"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        waitModel = new DefaultTableModel(new String[]{"姓名", "需要票数", "舱位"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JScrollPane cs = new JScrollPane(UIUtils.createTable(custModel, 22));
        cs.setBorder(new TitledBorder("已订票客户"));
        JScrollPane ws = new JScrollPane(UIUtils.createTable(waitModel, 22));
        ws.setBorder(new TitledBorder("等候队列"));
        split.setTopComponent(cs);
        split.setBottomComponent(ws);
        split.setResizeWeight(0.5);
        add(inputPanel, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);

        queryBtn.addActionListener(e -> {
            String fn = flightField.getText().trim();
            if (fn.isEmpty()) {
                JOptionPane.showMessageDialog(parentFrame, "请输入航班号！");
                return;
            }
            refresh(fn);
        });
    }

    private void refresh(String flightNum) {
        FlightRoute r = service.getFlightList().searchByFlight(flightNum);
        custModel.setRowCount(0);
        waitModel.setRowCount(0);
        if (r == null) {
            JOptionPane.showMessageDialog(parentFrame, "航班不存在！");
            return;
        }
        LinkNode<Customer> cc = r.booked.getHead().next;
        while (cc != null) {
            Customer c = cc.data;
            custModel.addRow(new Object[]{c.name, c.ticketCount, c.cabinName()});
            cc = cc.next;
        }
        LinkNode<Waiter> wc = r.queue.getFront();
        while (wc != null) {
            Waiter w = wc.data;
            waitModel.addRow(new Object[]{w.name, w.ticketCount,
                    Customer.cabinName(w.cabinClass)});
            wc = wc.next;
        }
    }
}