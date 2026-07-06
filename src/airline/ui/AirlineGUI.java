package airline.ui;

import airline.service.AirlineService;
import javax.swing.*;

/**
 * 航空客运订票系统主窗口
 * 使用选项卡面板组织五个功能模块：客票预订、航线查询、办理退票、航线总览、客户与队列
 */
public class AirlineGUI extends JFrame {

    /** 核心业务服务对象，统一管理所有航班数据和业务逻辑 */
    private AirlineService service = new AirlineService();

    /**
     * 构造方法：初始化主窗口界面
     * 设置窗口标题、大小、关闭行为，并创建五个功能选项卡
     */
    public AirlineGUI() {
        // 窗口基本配置
        setTitle("航空客运订票系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 680);
        setLocationRelativeTo(null);

        // 创建选项卡面板，将五个功能模块组织在一起
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("客票预订", new BookPanel(service, this));
        tabs.addTab("航线查询", new QueryPanel(service, this));
        tabs.addTab("办理退票", new RefundPanel(service, this));
        tabs.addTab("航线总览", new OverviewPanel(service));
        tabs.addTab("客户与队列", new CustomerPanel(service, this));
        add(tabs);
    }

    /**
     * 程序入口
     * 设置系统原生外观风格，在事件调度线程上启动窗口
     */
    public static void main(String[] args) {
        // 尝试使用系统原生外观风格，使界面与操作系统融合
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        // 在EDT（事件调度线程）上创建并显示窗口，保证线程安全
        SwingUtilities.invokeLater(() -> new AirlineGUI().setVisible(true));
    }
}