package com.cisco.thunderhead.example.ui;

import com.cisco.thunderhead.client.ContextServiceClient;
import com.cisco.thunderhead.connector.ManagementConnector;
import com.cisco.thunderhead.connector.states.ConnectorState;
import com.cisco.thunderhead.connector.states.ConnectorStateListener;
import com.cisco.thunderhead.plugin.ConnectorFactory;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * This is the main class that can be used to start the Context Service SDK GUI application.
 */
public class ContextServiceSdkUI extends JFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextServiceSdkUI.class);
    private static final String NOT_INITIALIZED = "not initialized, must initialize";
    private static final String INITIALIZED = "initialized successfully";
    private static final String INITIALIZING = "initializing, please wait...";

    private JPanel mainPanel;
    private JLabel statusLabel;
    private JTextArea textLog;
    private JLabel versionLabel;
    private ConnectorStateListener connectorStateListener = null;

    public ContextServiceSdkUI(String title) {
        super(title);
    }

    private void init() {
        initMenu();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleDestroy();
            }
        });
        statusLabel.setText(NOT_INITIALIZED);
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("App");
        addMenuItem(menu, "Initialize", (e) -> handleInitialize());
        addMenuItem(menu, "Destroy", (e) -> handleDestroy());
        addMenuItem(menu, "Show Connection Data", (e) -> showConnectionDataDialog());
        addMenuItem(menu, "Get Status", (e) -> showStatusDialog());
        addMenuItem(menu, "Get Metrics", (e) -> showMetricsDialog());
        addMenuItem(menu, "Customer/Pod/Request Management", (e) -> showCustomerPodRequestDialog());
        if (!isMac()) {
            addMenuItem(menu, "Exit", (e) -> handleExit());
        }
        menuBar.add(menu);
        setJMenuBar(menuBar);
        connectorStateListener = (previousState, newState) -> onConnectionStateChange(previousState, newState);
    }

    private void showCustomerPodRequestDialog() {
        CustomerPodRequestDialog dialog = new CustomerPodRequestDialog();
        dialog.initLists();
        dialog.pack();
        dialog.setVisible(true);
    }

    private void showFieldSetDialog() {
        FieldSetDialog dialog = new FieldSetDialog();
        dialog.pack();
        dialog.initLists();
        dialog.setVisible(true);
    }

    private void onConnectionStateChange(ConnectorState previousState, ConnectorState newState) {
        String message = String.format("Connection state change: old state: %s  new state: %s", previousState, newState);
        addLog(message);
    }

    private void addLog(String message) {
        String text = textLog.getText();
        if (text.length() > 0) {
            text += "\n";
        }
        text += message;
        textLog.setText(text);
    }

    private void showMetricsDialog() {
        MetricsDialog dialog = new MetricsDialog();
        dialog.setModal(false);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void addMenuItem(JMenu menu, String text, ActionListener l) {
        JMenuItem menuItem = new JMenuItem(text);
        menuItem.addActionListener(l);
        menu.add(menuItem);
    }

    private void handleExit() {
        handleDestroy();
        setVisible(false);
        System.exit(0);
    }

    private void showStatusDialog() {
        GetStatusDialog dialog = new GetStatusDialog(this);
        dialog.setModal(false);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void handleDestroy() {
        addLog("destroying client");
        ContextServiceClient contextServiceClient = ConnectionData.getContextServiceClient();
        if (contextServiceClient != null) {
            contextServiceClient.destroy();
            contextServiceClient.removeStateListener(connectorStateListener);
        }
        ManagementConnector managementConnector = ConnectorFactory.getConnector(ManagementConnector.class);
        if (managementConnector != null) {
            managementConnector.destroy();
            managementConnector.removeStateListener(connectorStateListener);
        }
        ConnectionData.setContextServiceClient(null);
        statusLabel.setText(NOT_INITIALIZED);
        versionLabel.setText("");
        addLog("client destroyed");
    }

    private void handleInitialize() {
        handleDestroy();

        new Thread(() -> {
            addLog("Start - Initialize context service client");
            SwingUtilities.invokeLater(() -> statusLabel.setText(INITIALIZING));
            try {
                ContextServiceClient client = Utils.getInitializedContextServiceClient(ConnectionData.getConnectionData(), connectorStateListener);
                ConnectionData.setContextServiceClient(client);
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText(INITIALIZED);
                    versionLabel.setText(client.getVersion());
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText(NOT_INITIALIZED);
                    showError("failed to initialize: " + e.getMessage());
                });
            }
            addLog("End - Initialize context service client");
        }).start();

    }

    private void showError(String message) {
        message = message.replaceAll("(.{100})", "$1\n");
        JOptionPane.showMessageDialog(this, message);
    }

    private void showConnectionDataDialog() {
        ConnectionDataDialog dialog = new ConnectionDataDialog(this);
        dialog.setModal(false);
        dialog.pack();
        dialog.setVisible(true);
    }

    private static boolean isMac() {
        return "Mac OS X".equals(System.getProperty("os.name"));
    }

    public static void main(String[] args) {
        if (isMac()) {
            // Trap handling of Apple Quit menu
            System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS");
            // Use the Mac menu bar
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Test");
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ContextServiceSdkUI frame = new ContextServiceSdkUI("Context Service SDK Sample App");
        frame.setContentPane(frame.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.init();
        frame.pack();
        frame.setVisible(true);
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
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setPreferredSize(new Dimension(400, 400));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textLog = new JTextArea();
        textLog.setEnabled(false);
        scrollPane1.setViewportView(textLog);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Version:");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        versionLabel = new JLabel();
        versionLabel.setText("");
        panel2.add(versionLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        statusLabel = new JLabel();
        panel3.add(statusLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 4, false));
        final JLabel label2 = new JLabel();
        label2.setText("State:");
        panel3.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
