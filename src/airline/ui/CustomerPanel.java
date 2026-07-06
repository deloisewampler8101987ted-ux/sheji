package airline.ui;

import airline.service.AirlineService;
import airline.model.*;
import airline.datastructure.LinkNode;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * 客户与队列面板：输入航班号查询该航班的已订票客户和候补队列，
 * 以上下分割的两个表格分别展示
 */
public class CustomerPanel extends JPanel {
    /** 核心业务服务对象 */
    private AirlineService service;
    /** 父窗口，用于弹出对话框 */
    private JFrame parentFrame;
    /** 已订票客户表格模型 */
    private DefaultTableModel custModel;
    /** 候补队列表格模型 */
    private DefaultTableModel waitModel;

    /**
     * 构造方法：初始化客户与队列面板，包含顶部航班号输入区和上下分割的两个表格
     * @param service     核心业务服务对象
     * @param parentFrame 父窗口，用于弹出对话框
     */
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

        // 查询按钮事件监听：根据航班号刷新已订票客户和候补队列
        queryBtn.addActionListener(e -> {
            String fn = flightField.getText().trim();
            if (fn.isEmpty()) {
                JOptionPane.showMessageDialog(parentFrame, "请输入航班号！");
                return;
            }
            refresh(fn);
        });
    }

    /**
     * 刷新表格数据：查询指定航班的已订票客户链表和候补队列，
     * 分别填充到上方已订票表格和下方等候队列表格
     * @param flightNum 航班号
     */
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