package airline.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class UIUtils {
    public static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 12);
    public static final Font TABLE_FONT = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font MONO_FONT = new Font("Monospaced", Font.PLAIN, 14);

    public static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    public static JTextField createTextField(int columns) {
        return new JTextField(columns);
    }

    public static JButton createButton(String text) {
        return new JButton(text);
    }

    public static JTable createTable(DefaultTableModel model, int rowHeight) {
        JTable table = new JTable(model);
        table.setRowHeight(rowHeight);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(HEADER_FONT);
        table.setFont(TABLE_FONT);
        return table;
    }

    public static JScrollPane createTableScroll(JTable table, String title) {
        JScrollPane sp = new JScrollPane(table);
        if (title != null) {
            sp.setBorder(BorderFactory.createTitledBorder(title));
        }
        return sp;
    }

    public static JPanel createInputPanel() {
        return new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    }

    public static void padPanel(JPanel panel) {
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    }
}