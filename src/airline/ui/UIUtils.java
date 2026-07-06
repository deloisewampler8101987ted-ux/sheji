package airline.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * UI 工具类：提供统一的字体、组件创建和样式设置方法，
 * 所有面板和表格的公共样式均在此集中管理
 */
public class UIUtils {

    /** 表格表头字体，加粗12号 SansSerif */
    public static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 12);

    /** 表格内容字体，常规13号 SansSerif */
    public static final Font TABLE_FONT = new Font("SansSerif", Font.PLAIN, 13);

    /** 等宽字体，用于对齐显示，常规14号 Monospaced */
    public static final Font MONO_FONT = new Font("Monospaced", Font.PLAIN, 14);

    /**
     * 按钮渲染器：用于在 JTable 单元格中显示按钮样式，
     * 仅负责渲染外观，不处理点击事件
     */
    public static class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setOpaque(true);
        }

        /**
         * 返回渲染用的按钮组件，设置按钮文本为单元格的值
         * @param table      表格对象
         * @param value      单元格当前值
         * @param isSelected 是否选中
         * @param hasFocus   是否聚焦
         * @param row        行号
         * @param column     列号
         * @return 渲染后的按钮组件
         */
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    /**
     * 创建指定列数的文本输入框
     * @param columns 列数，控制文本框宽度
     * @return JTextField 文本输入框
     */
    public static JTextField createTextField(int columns) {
        return new JTextField(columns);
    }

    /**
     * 创建指定文本的按钮
     * @param text 按钮文字
     * @return JButton 按钮
     */
    public static JButton createButton(String text) {
        return new JButton(text);
    }

    /**
     * 创建统一风格的 JTable：设置行高、禁止表头拖拽排序，并应用默认字体
     * @param model     表格数据模型
     * @param rowHeight 行高（像素）
     * @return 配置好的 JTable
     */
    public static JTable createTable(DefaultTableModel model, int rowHeight) {
        JTable table = new JTable(model);
        table.setRowHeight(rowHeight);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(HEADER_FONT);
        table.setFont(TABLE_FONT);
        return table;
    }

    /**
     * 创建带标题边框的滚动面板，包裹指定的表格
     * @param table 表格对象
     * @param title 标题文字，为 null 时不添加标题边框
     * @return JScrollPane 滚动面板
     */
    public static JScrollPane createTableScroll(JTable table, String title) {
        JScrollPane sp = new JScrollPane(table);
        if (title != null) {
            sp.setBorder(BorderFactory.createTitledBorder(title));
        }
        return sp;
    }

    /**
     * 创建水平流式布局的输入面板，左对齐，水平间距10像素，垂直间距5像素
     * @return JPanel 输入面板
     */
    public static JPanel createInputPanel() {
        return new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    }

    /**
     * 为面板添加统一的 15 像素内边距
     * @param panel 目标面板
     */
    public static void padPanel(JPanel panel) {
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    }
}