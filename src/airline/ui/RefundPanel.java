package airline.ui;

import airline.service.AirlineService;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * 办理退票面板：输入航班号和客户姓名执行退票操作，
 * 退票后自动处理候补队列，将释放的余票按顺序分配给等候客户
 */
public class RefundPanel extends JPanel {

    /**
     * 构造方法：初始化退票面板界面，包含顶部的航班号/姓名输入区和下方的退票结果显示区
     * @param service     核心业务服务对象
     * @param parentFrame 父窗口，用于弹出对话框
     */
    public RefundPanel(AirlineService service, JFrame parentFrame) {
        setLayout(new BorderLayout(10, 10));
        UIUtils.padPanel(this);

        JPanel inputPanel = UIUtils.createInputPanel();
        inputPanel.add(new JLabel("航班号:"));
        JTextField flightField = UIUtils.createTextField(10);
        inputPanel.add(flightField);
        inputPanel.add(new JLabel("客户姓名:"));
        JTextField nameField = UIUtils.createTextField(10);
        inputPanel.add(nameField);
        JButton refundBtn = UIUtils.createButton("退票");
        inputPanel.add(refundBtn);

        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(UIUtils.MONO_FONT);
        JScrollPane sp = new JScrollPane(resultArea);
        sp.setBorder(new TitledBorder("退票结果"));
        add(inputPanel, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);

        // 退票按钮事件监听：调用服务层退票，结果显示在下方文本区域
        refundBtn.addActionListener(e -> {
            String fn = flightField.getText().trim();
            String nm = nameField.getText().trim();
            if (fn.isEmpty() || nm.isEmpty()) {
                resultArea.setText("请填写航班号和客户姓名！");
            } else {
                resultArea.setText(service.refundTicket(fn, nm));
            }
        });
    }
}