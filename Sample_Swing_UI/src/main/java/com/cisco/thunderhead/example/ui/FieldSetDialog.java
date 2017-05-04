package com.cisco.thunderhead.example.ui;

import com.cisco.thunderhead.BaseDbBean;
import com.cisco.thunderhead.client.Operation;
import com.cisco.thunderhead.client.SearchParameters;
import com.cisco.thunderhead.dictionary.Field;
import com.cisco.thunderhead.dictionary.FieldSet;
import com.cisco.thunderhead.errors.ApiException;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This allows the user to see fields in a field set, and add or delete new fields in that field set.
 */
public class FieldSetDialog extends JDialog {
    private static final Logger LOGGER = LoggerFactory.getLogger(FieldSetDialog.class);
    private JPanel contentPane;
    private JButton buttonClose;
    private JTextField textFieldSetName;
    private JButton addFieldButton;
    private JButton buttonSave;
    private JButton deleteFieldButton;
    private JCheckBox checkBoxFieldSetPublic;
    private JTable fieldsTable;
    private JButton editFieldButton;
    private FieldSet fieldSet;

    public FieldSetDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonClose);

        buttonClose.addActionListener(e -> onClose());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onClose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        addFieldButton.addActionListener(e -> handleAddField());
        editFieldButton.addActionListener(e -> handleEditField());
        deleteFieldButton.addActionListener(e -> handleDeleteField());
        buttonSave.addActionListener(e -> handleSave());
    }

    private void handleAddField() {
        FieldDialog dialog = new FieldDialog();
        dialog.pack();
        dialog.initLists();
        dialog.setVisible(true);
        if (dialog.isOkClicked()) {
            Field field = dialog.getField();
            FieldTableModel tableModel = (FieldTableModel) fieldsTable.getModel();
            tableModel.addField(field);
            tableModel.fireTableDataChanged();
        }
    }

    private void handleEditField() {
        int row = fieldsTable.getSelectedRow();
        if (row == -1) {
            return;
        }
        FieldTableModel tableModel = (FieldTableModel) fieldsTable.getModel();
        Field field = tableModel.getField(row);

        FieldDialog dialog = new FieldDialog();
        dialog.setField(field);
        dialog.pack();
        dialog.initLists();
        dialog.setVisible(true);

        if (dialog.isOkClicked()) {
            field = dialog.getField();
            tableModel.setField(row, field);
            tableModel.fireTableDataChanged();
        }
    }

    private void handleDeleteField() {
        int row = fieldsTable.getSelectedRow();
        if (row == -1) {
            return;
        }

        FieldTableModel tableModel = (FieldTableModel) fieldsTable.getModel();
        tableModel.removeField(row);
        tableModel.fireTableDataChanged();
    }

    private void handleSave() {
        if (StringUtils.isEmpty(textFieldSetName.getText())) {
            showError("must specify field set name");
            return;
        }
        FieldTableModel tableModel = (FieldTableModel) fieldsTable.getModel();
        if (tableModel.fieldList.size() == 0) {
            showError("no fields to add to fieldset.  Must add some fields first");
            return;
        }
        try {
            List<String> fieldNames = tableModel.fieldList.stream().map(Field::getId).collect(Collectors.toList());
            if (areDuplicates(fieldNames, Field.class)
                    || areDuplicates(Collections.singletonList(textFieldSetName.getText()), FieldSet.class)) {
                return;
            }

            Set<String> fields = new HashSet<>();
            for (Field field : tableModel.fieldList) {
                if (field.getRefURL() == null) {
                    ConnectionData.getContextServiceClient().create(field);
                } else {
                    ConnectionData.getContextServiceClient().update(field);
                }
                fields.add(field.getIdentifier());
            }

            Utils.waitForSearchable(ConnectionData.getContextServiceClient(), fields, Field.class);

            FieldSet fs;
            if (this.fieldSet != null) {
                fs = this.fieldSet;
                fs.setKey(textFieldSetName.getText());
                fs.setPubliclyAccessible(checkBoxFieldSetPublic.isSelected());
                ConnectionData.getContextServiceClient().update(fs);
            } else {
                fs = new FieldSet(textFieldSetName.getText(), fields, checkBoxFieldSetPublic.isSelected());
                ConnectionData.getContextServiceClient().create(fs);
            }
            Utils.waitForSearchable(ConnectionData.getContextServiceClient(), Collections.singletonList(textFieldSetName.getText()), FieldSet.class);
            dispose();
        } catch (ApiException e) {
            showError(e.getMessage());
        }
    }

    private boolean areDuplicates(List<String> beanList, Class<? extends BaseDbBean> clazz) {
        for (String name : beanList) {
            SearchParameters sp = new SearchParameters();
            sp.add("id", name);
            List result = ConnectionData.getContextServiceClient().search(clazz, sp, Operation.OR);
            if (result.size() > 0) {
                showError("Duplicate entry found for " + name + "; delete it and try a different name");
                return true;
            }
        }
        return false;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonClose = new JButton();
        buttonClose.setText("Close");
        panel2.add(buttonClose, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonSave = new JButton();
        buttonSave.setText("Save");
        panel2.add(buttonSave, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        textFieldSetName = new JTextField();
        textFieldSetName.setText("");
        panel4.add(textFieldSetName, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 1, false));
        final JLabel label1 = new JLabel();
        label1.setText("Field Set Name");
        panel4.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkBoxFieldSetPublic = new JCheckBox();
        checkBoxFieldSetPublic.setText("");
        panel4.add(checkBoxFieldSetPublic, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Public");
        panel4.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel5.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        fieldsTable = new JTable();
        scrollPane1.setViewportView(fieldsTable);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel6, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        deleteFieldButton = new JButton();
        deleteFieldButton.setText("Delete Field");
        panel6.add(deleteFieldButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel6.add(spacer2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        addFieldButton = new JButton();
        addFieldButton.setText("Add Field");
        panel6.add(addFieldButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editFieldButton = new JButton();
        editFieldButton.setText("Edit Field");
        panel6.add(editFieldButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel7, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Fields");
        panel7.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel7.add(spacer3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    private static class FieldTableModel extends DefaultTableModel {
        List<Field> fieldList = new ArrayList<>();

        FieldTableModel() {
            super(null, new Object[]{"Field Name", "Data Type", "Classification", "Searchable", "Public"});
        }

        void addField(Field field) {
            fieldList.add(field);
        }

        void addRow(Field field) {
            Vector<String> rowData = new Vector<>();
            rowData.add(field.getKey());
            rowData.add(field.getDataType());
            rowData.add(field.getClassification());
            rowData.add(field.getSearchable());
            rowData.add(String.valueOf(field.getPubliclyAccessible()));
            addRow(rowData);
        }

        void removeField(int row) {
            fieldList.remove(row);
        }

        Field getField(int row) {
            return fieldList.get(row);
        }

        private void removeAllRows() {
            for (int i = getRowCount() - 1; i > -1; i--) {
                removeRow(i);
            }
        }

        @Override
        public void fireTableDataChanged() {
            // have to sync up with current field list
            removeAllRows();
            for (Field field : fieldList) {
                addRow(field);
            }
            super.fireTableDataChanged();
        }

        public void setField(int row, Field field) {
            fieldList.set(row, field);
        }
    }

    private void onClose() {
        dispose();
    }

    private void showError(String message) {
        message = message.replaceAll("(.{100})", "$1\n");
        JOptionPane.showMessageDialog(this, message);
    }

    @SuppressWarnings("unchecked")
    void initLists() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new Thread(() -> {
            try {
                FieldTableModel fieldModel = new FieldTableModel();
                if (this.fieldSet != null) {
                    textFieldSetName.setText(fieldSet.getKey());
                    textFieldSetName.setEnabled(false);
                    checkBoxFieldSetPublic.setSelected(fieldSet.getPubliclyAccessible());

                    List<Field> fields = Utils.search(ConnectionData.getContextServiceClient(), fieldSet.getFields(), Field.class);
                    fields.forEach(fieldModel::addField);
                }
                SwingUtilities.invokeLater(() -> {
                    fieldsTable.setModel(fieldModel);
                    fieldModel.fireTableDataChanged();
                });
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        }).start();
    }

    public void setFieldSet(FieldSet fieldSet) {
        this.fieldSet = fieldSet;
    }

}
