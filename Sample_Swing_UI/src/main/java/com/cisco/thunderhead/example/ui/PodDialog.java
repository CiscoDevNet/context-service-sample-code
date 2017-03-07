package com.cisco.thunderhead.example.ui;

import com.cisco.thunderhead.client.Operation;
import com.cisco.thunderhead.client.SearchParameters;
import com.cisco.thunderhead.customer.Customer;
import com.cisco.thunderhead.datatypes.PodMediaType;
import com.cisco.thunderhead.datatypes.PodState;
import com.cisco.thunderhead.dictionary.Field;
import com.cisco.thunderhead.dictionary.FieldSet;
import com.cisco.thunderhead.pod.Pod;
import com.cisco.thunderhead.request.Request;
import com.cisco.thunderhead.tag.Tag;
import com.cisco.thunderhead.util.RFC3339Date;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.List;

/**
 * This allows a user to edit a Pod's properties.
 */
public class PodDialog extends JDialog {
    private Pod pod = null; // null on create, non-null on update

    private JPanel contentPane;
    private JButton buttonSave;
    private JButton buttonClose;
    private JPanel panelDataElements;
    private JList<Tag> listTags;
    private JButton addTagButton;
    private JButton deleteTagButton;
    private JComboBox comboContributorType;
    private JTextField textUsername;
    private JTextField textCreatedDate;
    private JList<FieldSet> listFieldSets;
    private JList listContributors;
    private JList listWorkgroups;
    private JTextField textLastUpdated;
    private JComboBox<String> comboMediaType;
    private JComboBox<String> comboState;
    private JTextField textFieldId;
    private List<FieldSet> fieldSets;
    private Map<String, ContextBeanUIHelper.Pair<JTextField, Field>> fieldToTextField = new HashMap<>();
    private Customer customer;
    private Request request;
    private static final String SELECT = "select";

    public PodDialog() {
        $$$setupUI$$$();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonSave);

        buttonSave.addActionListener(e -> onSave());

        buttonClose.addActionListener(e -> onClose());

        // call onClose() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });

        // call onClose() on ESCAPE
        contentPane.registerKeyboardAction(e -> onClose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        listFieldSets.setCellRenderer(new MyCellRenderer() {
            @Override
            void handleSetText(Object value, JLabel label) {
                FieldSet fieldSet = (FieldSet) value;
                label.setText(fieldSet.getId() + " [" + (fieldSet.getPubliclyAccessible() ? "public" : "private") + "]");
            }
        });

        listTags.setCellRenderer(new MyCellRenderer() {
            @Override
            void handleSetText(Object value, JLabel label) {
                Tag tag = (Tag) value;
                label.setText(tag.getName());
            }
        });

        comboMediaType.setModel(new DefaultComboBoxModel<>(getPrivateStaticFinalStrings(PodMediaType.class)));
        comboState.setModel(new DefaultComboBoxModel<>(getPrivateStaticFinalStrings(PodState.class)));
        addTagButton.addActionListener(e -> handleAddTag());
        deleteTagButton.addActionListener(e -> handleRemoveTag());
    }

    private void handleAddTag() {
        String tagName = JOptionPane.showInputDialog(this, "Tag name", "Enter text", JOptionPane.OK_CANCEL_OPTION);
        if (tagName != null) {
            Tag tag = new Tag(new RFC3339Date(), tagName);
            DefaultListModel<Tag> model = (DefaultListModel<Tag>) listTags.getModel();
            model.addElement(tag);
        }
    }

    private void handleRemoveTag() {
        Tag tag = listTags.getSelectedValue();
        ((DefaultListModel<Tag>) listTags.getModel()).removeElement(tag);
    }

    private static String[] getPrivateStaticFinalStrings(Class clazz) {
        List<String> strings = new ArrayList<>();
        strings.add(SELECT);
        for (java.lang.reflect.Field field : clazz.getFields()) {
            if (field.getType() == String.class
                    && (field.getModifiers() & (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)) > 0) {
                // it's public static final
                try {
                    String fieldValue = (String) field.get(null);
                    strings.add(fieldValue);
                } catch (IllegalAccessException ignore) {
                }
            }
        }
        return strings.toArray(new String[strings.size()]);
    }

    private void onSave() {
        if (!comboState.getSelectedItem().equals(SELECT)) {
            pod.setState((String) comboState.getSelectedItem());
        } else {
            pod.setState(null);
        }

        if (!comboMediaType.getSelectedItem().equals(SELECT)) {
            pod.setMediaType((String) comboMediaType.getSelectedItem());
        } else {
            pod.setMediaType(null);
        }

        Set<Tag> tags = new HashSet<>();
        DefaultListModel<Tag> listModel = (DefaultListModel<Tag>) listTags.getModel();
        for (Object o : listModel.toArray()) {
            tags.add((Tag) o);
        }
        pod.setTags(tags);

        boolean success = ContextBeanUIHelper.saveContextBean(fieldToTextField, comboContributorType, fieldSets, textUsername, this, pod, Pod.class, ((dataElements) -> {
            Pod pod = new Pod(dataElements);

            if (customer != null) {
                pod.setCustomerId(customer.getCustomerId());
            }
            if (request != null) {
                pod.setRequestId(request.getRequestId());
            }
            return pod;
        }));
        if (success) {
            dispose();
        }
    }

    private void onClose() {
        // add your code here if necessary
        dispose();
    }

    public void initLists() {
        ContextBeanUIHelper.initDialog(fieldSets, fieldToTextField, panelDataElements, listFieldSets,
                textCreatedDate, textLastUpdated, pod, listContributors, listWorkgroups, textFieldId);

        if (pod != null) {
            comboState.setSelectedItem(pod.getState() == null ? SELECT : pod.getState());
            comboMediaType.setSelectedItem(pod.getMediaType() == null ? SELECT : pod.getMediaType());
            DefaultListModel<Tag> listModel = new DefaultListModel<>();
            listTags.setModel(listModel);
            pod.getTags().forEach(listModel::addElement);
        } else {
            listTags.setModel(new DefaultListModel<>());
        }
    }

    public void setPod(Pod pod) {
        this.pod = pod;
        SearchParameters sp = new SearchParameters();
        pod.getFieldsets().forEach((fieldSetName) -> {
            sp.add("id", fieldSetName);
        });
        this.fieldSets = ConnectionData.getContextServiceClient().search(FieldSet.class, sp, Operation.OR);
    }

    private void createUIComponents() {
        panelDataElements = new JPanel();
        panelDataElements.setLayout(new BoxLayout(panelDataElements, BoxLayout.Y_AXIS));
    }

    public void setFieldSets(List<FieldSet> fieldSets) {
        this.fieldSets = fieldSets;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 2, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(11, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Tags");
        panel1.add(label1, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        listTags = new JList();
        panel1.add(listTags, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        addTagButton = new JButton();
        addTagButton.setText("Add Tag");
        panel2.add(addTagButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel2.add(spacer2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        deleteTagButton = new JButton();
        deleteTagButton.setText("Delete Tag");
        panel2.add(deleteTagButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("State");
        panel1.add(label2, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Media Type");
        panel1.add(label3, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("New Contributor");
        panel1.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Type");
        panel3.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboContributorType = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("select");
        defaultComboBoxModel1.addElement("USER");
        defaultComboBoxModel1.addElement("MACHINE");
        comboContributorType.setModel(defaultComboBoxModel1);
        panel3.add(comboContributorType, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Username");
        panel3.add(label6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textUsername = new JTextField();
        panel3.add(textUsername, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Created");
        panel1.add(label7, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textCreatedDate = new JTextField();
        panel1.add(textCreatedDate, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Field Sets");
        panel1.add(label8, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        listFieldSets = new JList();
        panel1.add(listFieldSets, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Contributors");
        panel1.add(label9, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        listContributors = new JList();
        panel1.add(listContributors, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Workgroups");
        panel1.add(label10, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        listWorkgroups = new JList();
        panel1.add(listWorkgroups, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Last Updated");
        panel1.add(label11, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textLastUpdated = new JTextField();
        panel1.add(textLastUpdated, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        comboMediaType = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        defaultComboBoxModel2.addElement("select");
        defaultComboBoxModel2.addElement("CHAT");
        defaultComboBoxModel2.addElement("EMAIL");
        defaultComboBoxModel2.addElement("EVENT");
        defaultComboBoxModel2.addElement("MOBILE");
        defaultComboBoxModel2.addElement("SOCIAL");
        defaultComboBoxModel2.addElement("VIDEO");
        defaultComboBoxModel2.addElement("VOICE");
        defaultComboBoxModel2.addElement("WEB");
        comboMediaType.setModel(defaultComboBoxModel2);
        panel1.add(comboMediaType, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboState = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel3 = new DefaultComboBoxModel();
        defaultComboBoxModel3.addElement("select");
        defaultComboBoxModel3.addElement("ACTIVE");
        defaultComboBoxModel3.addElement("CLOSED");
        comboState.setModel(defaultComboBoxModel3);
        panel1.add(comboState, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Pod ID");
        panel1.add(label12, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textFieldId = new JTextField();
        panel1.add(textFieldId, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel4, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel4.add(spacer3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel4.add(panel5, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonSave = new JButton();
        buttonSave.setText("Save");
        panel5.add(buttonSave, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        buttonClose = new JButton();
        buttonClose.setText("Close");
        panel5.add(buttonClose, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(300, -1), null, 0, false));
        panel6.add(panelDataElements, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 1, false));
        final Spacer spacer4 = new Spacer();
        panel6.add(spacer4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
