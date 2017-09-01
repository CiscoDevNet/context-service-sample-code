package com.cisco.thunderhead.example.ui;

import com.cisco.thunderhead.BaseDbBean;
import com.cisco.thunderhead.ContextBean;
import com.cisco.thunderhead.client.Operation;
import com.cisco.thunderhead.client.SearchParameters;
import com.cisco.thunderhead.customer.Customer;
import com.cisco.thunderhead.dictionary.Field;
import com.cisco.thunderhead.dictionary.FieldSet;
import com.cisco.thunderhead.errors.ApiException;
import com.cisco.thunderhead.pod.Pod;
import com.cisco.thunderhead.request.Request;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * This dialog allows a user to create/edit/delete Field Sets, Customers, Requests, and Pods.
 */
public class CustomerPodRequestDialog extends JDialog {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerPodRequestDialog.class);
    private final String DEFAULT_CUSTOMER_QUERY = "Context_City:Boston";
    private final String DEFAULT_REQUEST_QUERY = "Context_Title:Something";

    private JPanel contentPane;
    private JButton buttonOK;
    private JList<FieldSet> fieldsetsList;
    private JList<Customer> customersList;
    private JList<Pod> podsList;
    private JButton createCustomerButton;
    private JButton createPodButton;
    private JButton buttonRefreshLists;
    private JButton deleteCustomerButton;
    private JButton deletePodButton;
    private JButton editCustomerButton;
    private JButton editPodButton;
    private JList<Request> requestsList;
    private JButton createRequestButton;
    private JButton editRequestButton;
    private JButton deleteRequestButton;
    private JTextField textCustomerQuery;
    private JButton searchCustomersButton;
    private JTextField textRequestQuery;
    private JButton searchRequestsButton;
    private JButton createFieldSetButton;
    private JButton editFieldSetButton;
    private JButton deleteFieldSetButton;
    private JTextField textPodQuery;
    private JButton searchPodsButton;

    private static final String CISCO_BASE_CUSTOMER = "cisco.base.customer";
    private static final String CISCO_BASE_POD = "cisco.base.pod";
    private static final String CISCO_BASE_REQUEST = "cisco.base.request";
    private static final String CUSTOMER_DISPLAY_FIELD_NAME = "Context_First_Name";
    private static final String POD_DISPLAY_FIELD_NAME = "Context_Notes";
    private static final String REQUEST_DISPLAY_FIELD_NAME = "Context_Title";
    public static final String SEARCHABLE_FIELD_NAME = "Context_City";
    public static final String SEARCHABLE_FIELD_VALUE = "Boston";

    public CustomerPodRequestDialog() {
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onOK();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onOK(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        customersList.addListSelectionListener(this::handleCustomerSelectionEvent);
        requestsList.addListSelectionListener(this::handleRequestSelectionEvent);

        createCustomerButton.addActionListener(e -> handleCreateCustomer());
        editCustomerButton.addActionListener(e -> handleEditCustomer());
        deleteCustomerButton.addActionListener(e -> handleDeleteCustomer());

        createPodButton.addActionListener(e -> handleCreatePod());
        editPodButton.addActionListener(e -> handleEditPod());
        deletePodButton.addActionListener(e -> handleDeletePod());

        createRequestButton.addActionListener(e -> handleCreateRequest());
        editRequestButton.addActionListener(e -> handleEditRequest());
        deleteRequestButton.addActionListener(e -> handleDeleteRequest());

        createFieldSetButton.addActionListener(e -> handleCreateFieldSet());
        editFieldSetButton.addActionListener(e -> handleEditFieldSet());
        deleteFieldSetButton.addActionListener(e -> handleDeleteFieldSet());

        customersList.setCellRenderer(new MyCellRenderer() {
            @Override
            void handleSetText(Object value, JLabel label) {
                ((Customer) value).getDataElements().stream()
                        .filter((it) -> it.getDataKey().equals(CUSTOMER_DISPLAY_FIELD_NAME)).findFirst()
                        .ifPresent((it) -> label.setText(it.getDataValue().toString()));
            }
        });

        fieldsetsList.setCellRenderer(new MyCellRenderer() {
            @Override
            void handleSetText(Object value, JLabel label) {
                FieldSet fieldSet = (FieldSet) value;
                label.setText(fieldSet.getId() + " [" + (fieldSet.getPubliclyAccessible() ? "public" : "private") + "]");
            }

        });

        podsList.setCellRenderer(new MyCellRenderer() {
            @Override
            void handleSetText(Object value, JLabel label) {
                ((Pod) value).getDataElements().stream()
                        .filter((it) -> it.getDataKey().equals(POD_DISPLAY_FIELD_NAME)).findFirst()
                        .ifPresent((it) -> label.setText(it.getDataValue().toString()));
            }

        });

        requestsList.setCellRenderer(new MyCellRenderer() {
            @Override
            void handleSetText(Object value, JLabel label) {
                ((Request) value).getDataElements().stream()
                        .filter((it) -> it.getDataKey().equals(REQUEST_DISPLAY_FIELD_NAME)).findFirst()
                        .ifPresent((it) -> label.setText(it.getDataValue().toString()));
            }

        });

        buttonRefreshLists.addActionListener(e -> handleRefreshLists());
        searchCustomersButton.addActionListener(e -> handleSearchCustomers(false));
        searchRequestsButton.addActionListener(e -> handleSearchRequests(false));
        searchPodsButton.addActionListener(e -> handleSearchPods(false));

        textCustomerQuery.setText(DEFAULT_CUSTOMER_QUERY);
        textRequestQuery.setText(DEFAULT_REQUEST_QUERY);
    }

    private void handleCreateFieldSet() {
        FieldSetDialog dialog = new FieldSetDialog();
        dialog.initLists();
        dialog.pack();
        dialog.setVisible(true);
        initLists();
    }

    private void handleEditFieldSet() {
        FieldSet fieldSet = fieldsetsList.getSelectedValue();
        if (fieldSet == null) {
            JOptionPane.showMessageDialog(this, "select a field set");
            return;
        }
        FieldSetDialog dialog = new FieldSetDialog();
        dialog.setFieldSet(fieldSet);
        dialog.initLists();
        dialog.pack();
        dialog.setVisible(true);
        initLists();
    }

    private void handleDeleteFieldSet() {
        handleDelete("field set", fieldsetsList, FieldSet.class);
        FieldSet fieldSet = fieldsetsList.getSelectedValue();
        SearchParameters sp = new SearchParameters();
        fieldSet.getFields().forEach(f -> sp.add("id", f));
        if (!Utils.doRetry("", 10, 200, (Void v) -> {
            List<Field> fields = ConnectionData.getContextServiceClient().search(Field.class, sp, Operation.OR);
            fields.forEach(f -> ConnectionData.getContextServiceClient().delete(f));
            return true;
        })) {
            showError("problem deleting field set");
        } else {
            initLists();
        }
    }

    private void handleSearchRequests(boolean initLists) {
        handleSearch(textRequestQuery, requestsList, Request.class, initLists);
    }

    private void handleSearchCustomers(boolean initLists) {
        handleSearch(textCustomerQuery, customersList, Customer.class, initLists);
    }

    private void handleSearchPods(boolean initLists) {
        handleSearch(textPodQuery, podsList, Pod.class, initLists);
    }

    private void handleSearch(JTextField textQueryField, JList<? extends ContextBean> list, Class<? extends BaseDbBean> clazz, boolean initLists) {
        String query = textQueryField.getText();
        if (initLists && query.length() == 0) {
            return;
        }
        String[] terms = query.split(" ");
        SearchParameters sp = new SearchParameters();
        for (String term : terms) {
            int colonPos = term.indexOf(':');
            if (colonPos == -1) {
                showError("badly formed query.  Terms must be in field:value form");
                return;
            }
            String key = term.substring(0, colonPos);
            String value = term.substring(colonPos + 1);
            sp.add(key, value);
        }
        try {
            List<? extends BaseDbBean> items = ConnectionData.getContextServiceClient().search(clazz, sp, Operation.OR);
            DefaultListModel model = new DefaultListModel();
            items.forEach(model::addElement);
            list.setModel(model);
        } catch (ApiException e) {
            showError("Problem while searching: " + e.getMessage());
        }
    }

    private void handleRefreshLists() {
        initLists();
    }


    private void handleCreateRequest() {
//        Customer customer = customersList.getSelectedValue();

        List<FieldSet> fieldSets = getFieldSetsForDialog(CISCO_BASE_REQUEST);
        RequestDialog dialog = new RequestDialog();
        dialog.setFieldSets(fieldSets);
        dialog.initLists();
        dialog.pack();
        dialog.setVisible(true);

        initLists();
    }

    private void handleEditRequest() {
        Request request = requestsList.getSelectedValue();
        if (request == null) {
            return;
        }
        RequestDialog dialog = new RequestDialog();
        dialog.setRequest(request);
        dialog.initLists();
        dialog.pack();
        dialog.setVisible(true);
    }

    private void handleDeleteRequest() {
        handleDelete("request", requestsList, Request.class);
    }

    private void handleCreatePod() {
        if (fieldsetsList.isSelectionEmpty()) {
            showError("Must select one or more fieldsets");
            return;
        }
        List<FieldSet> fieldSets = getFieldSetsForDialog(CISCO_BASE_POD);
        if (fieldSets == null) {
            return;
        }
        PodDialog dialog = new PodDialog();
        dialog.setFieldSets(fieldSets);
        dialog.setCustomer(customersList.getSelectedValue());
        dialog.setRequest(requestsList.getSelectedValue());
        dialog.initLists();
        dialog.pack();
        dialog.setVisible(true);

        initLists();
    }

    private void handleEditPod() {
        Pod pod = podsList.getSelectedValue();
        if (pod == null) {
            return;
        }
        PodDialog dialog = new PodDialog();
        dialog.setPod(pod);
        dialog.initLists();
        dialog.pack();
        dialog.setVisible(true);
    }

    private void handleDeletePod() {
        handleDelete("pod", podsList, Pod.class);
    }

    private void handleCreateCustomer() {
        List<FieldSet> fieldSets = getFieldSetsForDialog(CISCO_BASE_CUSTOMER);
        if (fieldSets == null) {
            return;
        }
        CustomerDialog dialog = new CustomerDialog();
        dialog.setFieldSets(fieldSets);
        dialog.initLists();
        dialog.pack();
        dialog.setVisible(true);

        initLists();
    }

    private void handleEditCustomer() {
        Customer customer = customersList.getSelectedValue();
        if (customer == null) {
            return;
        }
        CustomerDialog dialog = new CustomerDialog();
        dialog.setCustomer(customer);
        dialog.initLists();
        dialog.pack();
        dialog.setVisible(true);
    }

    private void handleDeleteCustomer() {
        handleDelete("customer", customersList, Customer.class);
    }

    private void handleDelete(String type, JList list, Class<? extends BaseDbBean> clazz) {
        BaseDbBean contextBean = (BaseDbBean) list.getSelectedValue();
        if (contextBean == null) {
            return;
        }
        int response = JOptionPane.showConfirmDialog(this, "Really delete " + type + "?");
        if (response == JOptionPane.OK_OPTION) {
            try {
                ConnectionData.getContextServiceClient().delete(contextBean);
                Utils.waitForNotSearchable(ConnectionData.getContextServiceClient(), contextBean, clazz);
                initLists();
            } catch (ApiException e) {
                showError("couldn't delete customer: " + e.getMessage());
            }
        }
    }

    private List<FieldSet> getFieldSetsForDialog(String base) {
        SearchParameters sp = new SearchParameters();
        sp.add("id", base);

        List<FieldSet> selectedValuesList = fieldsetsList.getSelectedValuesList();
        for (FieldSet fieldSet : selectedValuesList) {
            if (base.equals(fieldSet.getId())) {
                continue;
            }
            sp.add("id", fieldSet.getId());
        }

        return ConnectionData.getContextServiceClient().search(FieldSet.class, sp, Operation.OR);
    }

    private void showError(String message) {
        message = message.replaceAll("(.{100})", "$1\n");
        JOptionPane.showMessageDialog(this, message);
    }

    private void handleRequestSelectionEvent(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() || e.getFirstIndex() < 0 || requestsList.getSelectedValue() == null) {
            return;
        }
        customersList.clearSelection();
        ;
        Request request = requestsList.getSelectedValue();
        SearchParameters params = new SearchParameters();
        params.add("requestId", request.getRequestId().toString());

        List<Pod> items = ConnectionData.getContextServiceClient().search(Pod.class, params, Operation.OR);

        DefaultListModel<Pod> model = new DefaultListModel<>();
        items.forEach(model::addElement);
        podsList.setModel(model);
    }

    private void handleCustomerSelectionEvent(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() || e.getFirstIndex() < 0 || customersList.getSelectedValue() == null) {
            return;
        }
        requestsList.clearSelection();
        Customer customer = customersList.getSelectedValue();
        SearchParameters params = new SearchParameters();
        params.add("customerId", customer.getCustomerId().toString());

        List<Pod> items = ConnectionData.getContextServiceClient().search(Pod.class, params, Operation.OR);

        DefaultListModel<Pod> model = new DefaultListModel<>();
        items.forEach(model::addElement);
        podsList.setModel(model);
    }

    void initLists() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new Thread(() -> {
            DefaultListModel fsModel = populateList(FieldSet.class, "id", "*");
            handleSearchCustomers(true);
            handleSearchRequests(true);
            handleSearchPods(true);
            podsList.setModel(new DefaultListModel<>());
            SwingUtilities.invokeLater(() -> {
                fieldsetsList.setModel(fsModel);
                setCursor(Cursor.getDefaultCursor());
            });
        }).start();
    }

    @SuppressWarnings("unchecked")
    private DefaultListModel populateList(Class clazz, String name, String key) {
        DefaultListModel model = new DefaultListModel();
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.add(name, key);
        List items = ConnectionData.getContextServiceClient().search(clazz, searchParameters, Operation.OR);
        items.forEach(model::addElement);
        return model;
    }

    private void onOK() {
        // add your code here
        dispose();
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
        contentPane.setLayout(new GridLayoutManager(2, 2, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Customers");
        panel2.add(label1, new GridConstraints(1, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel2.add(scrollPane1, new GridConstraints(1, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        customersList = new JList();
        scrollPane1.setViewportView(customersList);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel3.add(spacer1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        deleteCustomerButton = new JButton();
        deleteCustomerButton.setText("Delete Customer");
        panel3.add(deleteCustomerButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createCustomerButton = new JButton();
        createCustomerButton.setText("Create Customer");
        panel3.add(createCustomerButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editCustomerButton = new JButton();
        editCustomerButton.setText("Edit Customer");
        panel3.add(editCustomerButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Query");
        panel2.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textCustomerQuery = new JTextField();
        panel2.add(textCustomerQuery, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        searchCustomersButton = new JButton();
        searchCustomersButton.setText("Search Customers");
        panel2.add(searchCustomersButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Pods");
        panel4.add(label3, new GridConstraints(1, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel4.add(scrollPane2, new GridConstraints(1, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        podsList = new JList();
        scrollPane2.setViewportView(podsList);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        createPodButton = new JButton();
        createPodButton.setText("Create Pod");
        panel5.add(createPodButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel5.add(spacer2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        deletePodButton = new JButton();
        deletePodButton.setText("Delete Pod");
        panel5.add(deletePodButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editPodButton = new JButton();
        editPodButton.setText("Edit Pod");
        panel5.add(editPodButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textPodQuery = new JTextField();
        panel4.add(textPodQuery, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Query");
        panel4.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        searchPodsButton = new JButton();
        searchPodsButton.setText("Search Pods");
        panel4.add(searchPodsButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonRefreshLists = new JButton();
        buttonRefreshLists.setText("Refresh lists");
        panel1.add(buttonRefreshLists, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Requests");
        panel6.add(label5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane3 = new JScrollPane();
        panel6.add(scrollPane3, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        requestsList = new JList();
        scrollPane3.setViewportView(requestsList);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel6.add(panel7, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        createRequestButton = new JButton();
        createRequestButton.setText("Create Request");
        panel7.add(createRequestButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel7.add(spacer3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        editRequestButton = new JButton();
        editRequestButton.setText("Edit Request");
        panel7.add(editRequestButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deleteRequestButton = new JButton();
        deleteRequestButton.setText("Delete Request");
        panel7.add(deleteRequestButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Query");
        panel6.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textRequestQuery = new JTextField();
        panel6.add(textRequestQuery, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        searchRequestsButton = new JButton();
        searchRequestsButton.setText("Search Requests");
        panel6.add(searchRequestsButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel8, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel8.add(buttonOK, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel8.add(spacer4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel9.add(panel10, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane4 = new JScrollPane();
        panel10.add(scrollPane4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        fieldsetsList = new JList();
        scrollPane4.setViewportView(fieldsetsList);
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel10.add(panel11, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        createFieldSetButton = new JButton();
        createFieldSetButton.setText("Create Field Set");
        panel11.add(createFieldSetButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel11.add(spacer5, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        editFieldSetButton = new JButton();
        editFieldSetButton.setText("Edit Field Set");
        panel11.add(editFieldSetButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deleteFieldSetButton = new JButton();
        deleteFieldSetButton.setText("Delete Field Set");
        panel11.add(deleteFieldSetButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Field Sets");
        panel9.add(label7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
