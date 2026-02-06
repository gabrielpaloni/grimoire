package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RenderizadorModerno extends DefaultListCellRenderer {

    private static final String ICONE_ARQUIVO = "•  ";

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        label.setText(ICONE_ARQUIVO + value.toString());

        label.setBorder(new EmptyBorder(8, 15, 8, 15));

        if (isSelected) {
            label.setBackground(new Color(44, 49, 58));
            label.setForeground(new Color(97, 175, 239));

            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(97, 175, 239)),
                    new EmptyBorder(8, 12, 8, 15)
            ));
        } else {
            label.setBackground(new Color(33, 37, 43));
            label.setForeground(new Color(157, 165, 180));
        }
        return label;
    }
}