package airline.ui;

import airline.service.AirlineService;
import javax.swing.*;

public class AirlineGUI extends JFrame {
    private AirlineService service = new AirlineService();

    public AirlineGUI() {
        setTitle("航空客运订票系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 680);
        setLocationRelativeTo(null);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("客票预订", new BookPanel(service, this));
        tabs.addTab("航线查询", new QueryPanel(service, this));
        tabs.addTab("办理退票", new RefundPanel(service, this));
        tabs.addTab("航线总览", new OverviewPanel(service));
        tabs.addTab("客户与队列", new CustomerPanel(service, this));
        add(tabs);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new AirlineGUI().setVisible(true));
    }
}