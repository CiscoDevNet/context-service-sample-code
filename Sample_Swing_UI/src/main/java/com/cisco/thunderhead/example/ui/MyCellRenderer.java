package com.cisco.thunderhead.example.ui;

import javax.swing.*;
import java.awt.*;

/**
 * This is used to display the text in the list.
 */
abstract class MyCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (comp instanceof JLabel) {
            handleSetText(value, (JLabel) comp);
        }
        return comp;
    }

    abstract void handleSetText(Object value, JLabel label);
}
