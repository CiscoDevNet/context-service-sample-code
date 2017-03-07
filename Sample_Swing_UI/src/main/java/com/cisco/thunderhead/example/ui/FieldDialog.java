package com.cisco.thunderhead.example.ui;

import com.cisco.thunderhead.datatypes.ElementClassification;
import com.cisco.thunderhead.datatypes.ElementDataType;
import com.cisco.thunderhead.dictionary.Field;
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

/**
 * This allows the user to edit the properties of a field.
 */
public class FieldDialog extends JDialog {
    private static final Logger LOGGER = LoggerFactory.getLogger(FieldDialog.class);
    private JPanel contentPane;
    private JButton buttonCancel;
    private JTextField textFieldName;
    private JComboBox comboDataTypes;
    private JComboBox comboClassification;
    private JButton buttonOK;
    private JCheckBox checkBoxSearchable;
    private JTable tableTranslations;
    private JButton addTranslationButton;
    private JButton removeTranslationButton;
    private JTextArea textLocales;
    private JCheckBox checkBoxFieldPublic;
    private Field field;
    private boolean okClicked = false;

    private static final Vector COLUMN_HEADERS = new Vector() {{
        add("Language");
        add("Translation");
    }};

    public FieldDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonCancel);

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        buttonOK.addActionListener(e -> handleOK());

        addTranslationButton.addActionListener(e -> handleAddTranslation());
        removeTranslationButton.addActionListener(e -> handleRemoveTranslation());
    }

    private void handleAddTranslation() {
        DefaultTableModel tableModel = (DefaultTableModel) tableTranslations.getModel();
        Vector dataVector = tableModel.getDataVector();
        Vector<String> vector = new Vector<>();
        vector.add("ISO code");
        vector.add(textFieldName.getText());
        dataVector.add(vector);
        tableModel.setDataVector(dataVector, COLUMN_HEADERS);
        tableTranslations.setModel(tableModel);
    }

    private void handleRemoveTranslation() {
        int selectedRow = tableTranslations.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        DefaultTableModel tableModel = (DefaultTableModel) tableTranslations.getModel();
        Vector dataVector = tableModel.getDataVector();
        dataVector.remove(selectedRow);
        tableModel.fireTableDataChanged();
    }

    private void handleOK() {
        okClicked = true;
        dispose();
    }

    private void setFieldValuesFromUi(Field field) {
        String dataType = (String) comboDataTypes.getModel().getSelectedItem();
        String classification = (String) comboClassification.getModel().getSelectedItem();
        List<String> locales = Arrays.asList(textLocales.getText().split("\\r?\\n"));
        DefaultTableModel transModel = (DefaultTableModel) tableTranslations.getModel();
        Map<String, Object> transMap = new HashMap<>();
        transModel.getDataVector().forEach(v -> {
            Vector data = (Vector) v;
            transMap.put(data.get(0).toString(), data.get(1));
        });

        field.setKey(textFieldName.getText());
        field.setDataType(dataType);
        field.setClassification(classification);
        field.setLocales(locales);
        field.setTranslations(transMap);
        field.setSearchable(String.valueOf(checkBoxSearchable.isSelected()));
        field.setPubliclyAccessible(checkBoxFieldPublic.isSelected());
    }

    private void onCancel() {
        dispose();
    }

    private void showError(String message) {
        message = message.replaceAll("(.{100})", "$1\n");
        JOptionPane.showMessageDialog(this, message);
    }

    @SuppressWarnings("unchecked")
    void initLists() {
        new Thread(() -> {
            try {
                addFieldTypes(comboDataTypes);
                addClassifications(comboClassification);

                if (this.field == null) {
                    textLocales.setText("en_US\nen_GB");
                    checkBoxSearchable.setSelected(true);
                    checkBoxFieldPublic.setSelected(false);
                } else {
                    textFieldName.setText(field.getKey());
                    textFieldName.setEnabled(false);
                    comboDataTypes.setSelectedItem(field.getDataType());
                    comboClassification.setSelectedItem(field.getClassification());

                    // searchable is true, false, or null (considered to be true)
                    checkBoxSearchable.setSelected(!StringUtils.equals(field.getSearchable(), "false"));
                    checkBoxFieldPublic.setSelected(field.getPubliclyAccessible());

                    DefaultTableModel translationsTable = new DefaultTableModel(null, COLUMN_HEADERS);
                    if (field.getTranslations() != null) {
                        Vector dataVector = new Vector();

                        for (Map.Entry<String, Object> e : field.getTranslations().entrySet()) {
                            Vector row = new Vector();
                            row.add(e.getKey());
                            row.add(e.getValue());
                            dataVector.add(row);
                        }
                        translationsTable.setDataVector(dataVector, COLUMN_HEADERS);
                    }
                    tableTranslations.setModel(translationsTable);

                    StringBuilder localeStrings = new StringBuilder();
                    for (String locale : field.getLocales()) {
                        localeStrings.append(locale).append("\n");
                    }
                    textLocales.setText(localeStrings.toString());
                }

                if (this.field != null) {
                    setFieldValuesFromUi(this.field);
                }
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        }).start();
    }

    @SuppressWarnings("unchecked")
    private void addFieldTypes(JComboBox comboFieldTypes) {
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) comboFieldTypes.getModel();
        model.addElement(ElementDataType.STRING);
        model.addElement(ElementDataType.INTEGER);
        model.addElement(ElementDataType.DOUBLE);
        model.addElement(ElementDataType.BOOLEAN);
    }

    @SuppressWarnings("unchecked")
    private void addClassifications(JComboBox comboClassification) {
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) comboClassification.getModel();
        model.addElement(ElementClassification.UNENCRYPTED);
        model.addElement(ElementClassification.ENCRYPTED);
        model.addElement(ElementClassification.PII);
    }

    public void setField(Field field) {
        this.field = field;
    }

    /**
     * Gets values from the UI and sets it onto the field.
     */
    public Field getField() {
        Field f;
        if (field != null) {
            f = field;
        } else {
            f = new Field();
        }
        setFieldValuesFromUi(f);
        return f;
    }

    public boolean isOkClicked() {
        return okClicked;
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
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(7, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Field Name");
        panel4.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Data Type");
        panel4.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Classification");
        panel4.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Searchable");
        panel4.add(label4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboDataTypes = new JComboBox();
        panel4.add(comboDataTypes, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboClassification = new JComboBox();
        panel4.add(comboClassification, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkBoxSearchable = new JCheckBox();
        checkBoxSearchable.setText("");
        panel4.add(checkBoxSearchable, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel5.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(250, 100), null, 0, false));
        tableTranslations = new JTable();
        scrollPane1.setViewportView(tableTranslations);
        final JScrollPane scrollPane2 = new JScrollPane();
        panel4.add(scrollPane2, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textLocales = new JTextArea();
        textLocales.setPreferredSize(new Dimension(100, 100));
        scrollPane2.setViewportView(textLocales);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel6, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Translations");
        panel6.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel6.add(spacer2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        addTranslationButton = new JButton();
        addTranslationButton.setText("Add");
        panel6.add(addTranslationButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        removeTranslationButton = new JButton();
        removeTranslationButton.setText("Remove");
        panel6.add(removeTranslationButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel7, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Locales");
        panel7.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel7.add(spacer3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Public");
        panel4.add(label7, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkBoxFieldPublic = new JCheckBox();
        checkBoxFieldPublic.setText("");
        panel4.add(checkBoxFieldPublic, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textFieldName = new JTextField();
        panel4.add(textFieldName, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
