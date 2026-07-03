package airline.ui;

import airline.service.AirlineService;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class RefundPanel extends JPanel {
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