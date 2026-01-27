package edu.southalabama.passwordmanager.client;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Main startup window for entering server IP + port and logging in securely.
 */
public class IPview extends JFrame {

    private final JButton submitButton;
    private final JTextField inputIP;
    private final JTextField inputPort;

    public IPview() {
        super("SSPM - Secure Client");

        submitButton = new JButton("Connect & Login");

        inputIP = new JTextField(15);
        inputIP.setText("127.0.0.1");

        inputPort = new JTextField(6);
        inputPort.setText("5000");

        createLayout();
        addValidation();
        addActions();

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void createLayout() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Server IP:"), gbc);
        gbc.gridx = 1; panel.add(inputIP, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Port:"), gbc);
        gbc.gridx = 1; panel.add(inputPort, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(submitButton, gbc);

        add(panel);
        getRootPane().setDefaultButton(submitButton);
    }

    private void addValidation() {
        DocumentListener validator = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
            private void update() {
                submitButton.setEnabled(
                        isValidIPv4(inputIP.getText().trim()) &&
                        isValidPort(inputPort.getText().trim())
                );
            }
        };
        inputIP.getDocument().addDocumentListener(validator);
        inputPort.getDocument().addDocumentListener(validator);

        submitButton.setEnabled(
                isValidIPv4(inputIP.getText().trim()) &&
                isValidPort(inputPort.getText().trim())
        );
    }

    private void addActions() {
        submitButton.addActionListener((ActionEvent e) -> {
            String[] login = showLoginDialog();
            if (login == null) return;

            final String username = login[0];
            final String password = login[1];

            final String ip = inputIP.getText().trim();
            final int port = Integer.parseInt(inputPort.getText().trim());

            submitButton.setEnabled(false);

            new Thread(() -> {
                try {
                    // TLS client automatically uses embedded truststore
                    client c = new client(ip, port);

                    // Send AUTH request with SHA-256 hashed password
                    String hashed = HashUtil.sha256(password);
                    String resp = c.sendAndWait("AUTH:" + username + ":" + hashed, 3000);

                    if (resp == null || !"AUTH_OK".equals(resp))
                        throw new Exception("Authentication failed: " + resp);

                    SwingUtilities.invokeLater(() -> {
                        new passwordView(c, ip, port, username);
                        dispose();
                    });

                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Failed to connect or authenticate:\n" + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                        submitButton.setEnabled(true);
                    });
                }
            }, "Client-Connect-Thread").start();
        });
    }

    private boolean isValidIPv4(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;
        try {
            for (String p : parts) {
                int v = Integer.parseInt(p);
                if (v < 0 || v > 255) return false;
            }
            return true;
        } catch (NumberFormatException e) { return false; }
    }

    private boolean isValidPort(String portStr) {
        try {
            int p = Integer.parseInt(portStr);
            return p >= 1 && p <= 65535;
        } catch (Exception e) { return false; }
    }

    private String[] showLoginDialog() {
        JTextField userField = new JTextField(12);
        JPasswordField passField = new JPasswordField(12);

        JPanel panel = new JPanel(new GridLayout(2,2,8,8));
        panel.add(new JLabel("Username:")); panel.add(userField);
        panel.add(new JLabel("Password:")); panel.add(passField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Login Required",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        return (result == JOptionPane.OK_OPTION)
                ? new String[]{ userField.getText(), new String(passField.getPassword()) }
                : null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(IPview::new);
    }
}
