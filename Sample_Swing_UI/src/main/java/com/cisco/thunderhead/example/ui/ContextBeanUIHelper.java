package com.cisco.thunderhead.example.ui;

import com.cisco.thunderhead.BaseDbBean;
import com.cisco.thunderhead.ContextBean;
import com.cisco.thunderhead.ContextObject;
import com.cisco.thunderhead.Contributor;
import com.cisco.thunderhead.DataElement;
import com.cisco.thunderhead.client.Operation;
import com.cisco.thunderhead.client.SearchParameters;
import com.cisco.thunderhead.dictionary.Field;
import com.cisco.thunderhead.dictionary.FieldSet;
import com.cisco.thunderhead.errors.ApiException;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This contains some helper methods related to the UI.
 */
public class ContextBeanUIHelper {
    public static void initDialog(List<FieldSet> fieldSets, Map<String, Pair<JTextField, Field>> fieldToTextField,
                                  JPanel panelDataElements, JList<FieldSet> listFieldSets, JTextField textCreatedDate,
                                  JTextField textLastUpdated, ContextBean contextBean, JList<String> listContributors, JList listWorkgroups, JTextField textFieldId) {
        SearchParameters sp = new SearchParameters();
        for (FieldSet fieldSet : fieldSets) {
            for (String fieldName : fieldSet.getFields()) {
                sp.add("id",fieldName);
            }
        }

        // add all the field entries based on the selected field sets
        List<Field> fields = ConnectionData.getContextServiceClient().search(Field.class, sp, Operation.OR);
        for (Field field : fields) {
            JPanel fieldPanel = new JPanel();
            fieldPanel.setLayout(new BorderLayout());
            fieldPanel.add(new JLabel(field.getId()), BorderLayout.WEST);

            JTextField textField = new JTextField();
            fieldPanel.add(textField, BorderLayout.CENTER);

            if (CustomerPodRequestDialog.SEARCHABLE_FIELD_NAME.equals(field.getId())) {
                textField.setText(CustomerPodRequestDialog.SEARCHABLE_FIELD_VALUE);
            }

            Pair<JTextField,Field> pair = new Pair<>(textField, field);
            fieldToTextField.put(field.getId(),pair);
            panelDataElements.add(fieldPanel);
        }

        DefaultListModel<FieldSet> fieldSetsModel = new DefaultListModel<>();
        fieldSets.forEach(fieldSetsModel::addElement);
        listFieldSets.setModel(fieldSetsModel);

        setIdTextField(textFieldId, contextBean);

        if (contextBean!=null) { // this is an edit
            textCreatedDate.setText(contextBean.getCreated().toString());
            textLastUpdated.setText(contextBean.getLastUpdated().toString());

            DefaultListModel<String> contributorsModel = new DefaultListModel<>();
            contextBean.getContributors().forEach((c) -> contributorsModel.addElement(c.getUsername() + "[" + c.getContributorType() + "]"));
            listContributors.setModel(contributorsModel);

            DefaultListModel<String> workgroupsModel = new DefaultListModel<>();
            contextBean.getWorkgroups().forEach((k,v) -> workgroupsModel.addElement(k));
            listWorkgroups.setModel(workgroupsModel);

            for (Map.Entry<String, Pair<JTextField,Field>> e : fieldToTextField.entrySet()) {
                for (DataElement dataElement : contextBean.getDataElements()) {
                    if (dataElement.getDataKey().equals(e.getValue().b.getKey())) {
                        e.getValue().a.setText(dataElement.getDataValue().toString());
                    }
                }
            }
        }
    }

    public static void setIdTextField(JTextField textFieldId, BaseDbBean bean) {
        textFieldId.setEditable(false);
        if (bean != null) { // this is an edit
            int pos = bean.getRefURL().lastIndexOf('/');
            if (pos != -1) {
                String id = bean.getRefURL().substring(pos + 1);
                textFieldId.setText(id);
            }
        }
    }

    public static boolean saveContextBean(Map<String, Pair<JTextField, Field>> fieldToTextField,
                                          JComboBox<String> comboContributorType, List<FieldSet> fieldSets,
                                          JTextField textUsername, Component parent,
                                          ContextObject contextBean, Function<Set<DataElement>, ContextObject> createContextBean) {
        Set<DataElement> dataElements = new HashSet<>();
        for (Map.Entry<String, ContextBeanUIHelper.Pair<JTextField,Field>> e : fieldToTextField.entrySet()) {
            String value = e.getValue().a.getText();
            if (StringUtils.isNotEmpty(value)) {
                DataElement dataElement = new DataElement(e.getKey(), value, e.getValue().b.getDataType());
                dataElements.add(dataElement);
            }
        }
        ContextObject ctxBean;  // operate on 'ctxBean'
        if (contextBean==null) {
            ctxBean = createContextBean.apply(dataElements);
        } else {
            ctxBean = contextBean;
            ctxBean.setDataElements(dataElements);
        }
        String contributorType = (String) comboContributorType.getSelectedItem();
        if (Arrays.asList("MACHINE","USER").contains(contributorType)) {
            // only set contributor if user has specified proper value
            Contributor contributor = new Contributor();
            contributor.setContributorType(contributorType);
            contributor.setUsername(textUsername.getText());
            ctxBean.setNewContributor(contributor);
        }

        ArrayList<String> fieldSetNames = fieldSets.stream().map(FieldSet::getId).collect(Collectors.toCollection(ArrayList::new));
        ctxBean.setFieldsets(fieldSetNames);
        try {
            if (contextBean==null) {
                ConnectionData.getContextServiceClient().create(ctxBean);
                Utils.waitForSearchable(ConnectionData.getContextServiceClient(), Collections.singletonList(ctxBean.getId().toString()), ctxBean.getType());
            } else {
                ConnectionData.getContextServiceClient().update(ctxBean);
            }
            return true;
        } catch(ApiException e) {
            showError(parent, "could not create customer: " + e.getMessage());
            return false;
        }
    }

    public static void showError(Component parent, String message) {
        message = message.replaceAll("(.{100})", "$1\n");
        JOptionPane.showMessageDialog(parent, message);
    }

    public static class Pair<A,B> {
        A a;
        B b;
        Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }
    }
}
