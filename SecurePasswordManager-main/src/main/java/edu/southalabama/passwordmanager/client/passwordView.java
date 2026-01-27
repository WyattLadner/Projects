package edu.southalabama.passwordmanager.client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * Frame shown after a successful connection.
 * Shows server info, allows Add/View/Remove passwords, and Change Password.
 */
public class passwordView extends JFrame {
    private final client client;
    private final DefaultTableModel tableModel;
    private final JTable passwordTable;
    private final JLabel statusLabel;
    private final Icon greenDot = new DotIcon(Color.GREEN, 10);
    private final Icon redDot = new DotIcon(Color.RED, 10);
    private int revealedRow = -1;
    private final String username;

    private CardLayout cardLayout;
    private JPanel centerPanel;
    private JPasswordField oldPassField;
    private JPasswordField newPassField;
    private JPasswordField confirmPassField;

    // pending remove target so we can remove the correct row when server confirms
    private volatile String pendingRemoveService = null;
    private volatile String pendingRemoveUser = null;

    public passwordView(client client, String host, int port, String username) {
        super("SSPM - Connected");
        this.client = client;
        this.username = username;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        try {
            // load from classpath resource stream (throws if missing/invalid)
            java.awt.Image icon = javax.imageio.ImageIO.read(
                    getClass().getResourceAsStream("/edu/southalabama/passwordmanager/client/images/lock.png")
            );
            if (icon != null) setIconImage(icon);
            else System.err.println("Icon resource not found: /edu/southalabama/passwordmanager/client/images/lock.png");
        } catch (IOException ex) {
            System.err.println("Failed to load icon: " + ex.getMessage());
        }

        // server info
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        statusLabel = new JLabel(host + ":" + port + " (" + username + ")", greenDot, JLabel.LEFT);
        top.add(statusLabel);
        add(top, BorderLayout.NORTH);

        // center panel with cards
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);

        // MAIN CARD
        JPanel mainCard = new JPanel(new BorderLayout(8, 8));
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));

        JButton addBtn = new JButton("Add Password");
        JButton viewBtn = new JButton("View Passwords");
        JButton removeBtn = new JButton("Remove Password");
        JButton changePassBtn = new JButton("Change Auth Password");

        buttonRow.add(addBtn);
        buttonRow.add(viewBtn);
        buttonRow.add(removeBtn);
        buttonRow.add(changePassBtn);

        mainCard.add(buttonRow, BorderLayout.NORTH);

        String[] columnNames = {"Website/URL", "Username", "Password"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        passwordTable = new JTable(tableModel);
        passwordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        passwordTable.setRowHeight(25);

        // Masked password renderer
        passwordTable.getColumnModel().getColumn(2).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                          boolean isSelected, boolean hasFocus,
                                                          int row, int col) {
                JLabel label = new JLabel();
                label.setOpaque(true);
                label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                label.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());

                if (row == revealedRow && value != null) {
                    label.setText((String) value);
                } else if (value != null) {
                    label.setText("••••••••••••••");
                }

                label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                return label;
            }
        });

        passwordTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = passwordTable.rowAtPoint(e.getPoint());
                int col = passwordTable.columnAtPoint(e.getPoint());

                if (col == 2 && row >= 0) {
                    revealedRow = (revealedRow == row) ? -1 : row;
                    passwordTable.repaint();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(passwordTable);
        mainCard.add(scroll, BorderLayout.CENTER);

        centerPanel.add(mainCard, "MAIN");

        // ADD PASSWORD CARD
        JPanel addCard = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField serviceField = new JTextField(30);
        JTextField usernameField = new JTextField(30);
        JPasswordField passField = new JPasswordField(30);

        JButton submitPass = new JButton("Submit");
        JButton backBtn = new JButton("Back");

        gbc.gridx = 0; gbc.gridy = 0; addCard.add(new JLabel("URL:"), gbc);
        gbc.gridx = 1; addCard.add(serviceField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; addCard.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; addCard.add(usernameField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; addCard.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; addCard.add(passField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; addCard.add(backBtn, gbc);
        gbc.gridx = 1; addCard.add(submitPass, gbc);

        centerPanel.add(addCard, "ADD");

        // CHANGE PASSWORD CARD
        JPanel changePassCard = new JPanel(new GridBagLayout());
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(6,6,6,6);
        gbc2.fill = GridBagConstraints.HORIZONTAL;

        oldPassField = new JPasswordField(30);
        newPassField = new JPasswordField(30);
        confirmPassField = new JPasswordField(30);

        JButton submitChangePass = new JButton("Change Password");
        JButton backBtn2 = new JButton("Back");

        gbc2.gridx = 0; gbc2.gridy = 0; changePassCard.add(new JLabel("Old Password:"), gbc2);
        gbc2.gridx = 1; changePassCard.add(oldPassField, gbc2);
        gbc2.gridx = 0; gbc2.gridy = 1; changePassCard.add(new JLabel("New Password:"), gbc2);
        gbc2.gridx = 1; changePassCard.add(newPassField, gbc2);
        gbc2.gridx = 0; gbc2.gridy = 2; changePassCard.add(new JLabel("Confirm Password:"), gbc2);
        gbc2.gridx = 1; changePassCard.add(confirmPassField, gbc2);
        gbc2.gridx = 0; gbc2.gridy = 3; changePassCard.add(backBtn2, gbc2);
        gbc2.gridx = 1; changePassCard.add(submitChangePass, gbc2);

        centerPanel.add(changePassCard, "CHANGE_PASS");

        add(centerPanel, BorderLayout.CENTER);

        // ADD Password button action
        addBtn.addActionListener((ActionEvent e) -> cardLayout.show(centerPanel, "ADD"));
        backBtn.addActionListener((ActionEvent e) -> cardLayout.show(centerPanel, "MAIN"));

        submitPass.addActionListener((ActionEvent e) -> {
            String service = serviceField.getText().trim();
            String uname = usernameField.getText().trim();
            String pwd = new String(passField.getPassword());

            if (service.isEmpty() || uname.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(passwordView.this, "All fields required.", "Input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            submitPass.setEnabled(false);
            new Thread(() -> {
                try {
                    // Encrypt the password before sending
                    javax.crypto.SecretKey key = KeyManager.getKey();
                    String encryptedPwd = encrypt.encrypt(pwd, key);

                    client.send("ADD:" + service + ":" + uname + ":" + encryptedPwd);

                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(passwordView.this, "Password added for " + service, "Success", JOptionPane.INFORMATION_MESSAGE);
                        serviceField.setText("");
                        usernameField.setText("");
                        passField.setText("");
                        cardLayout.show(centerPanel, "MAIN");
                    });

                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(passwordView.this, "Failed to send: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE)
                    );
                } finally {
                    SwingUtilities.invokeLater(() -> submitPass.setEnabled(true));
                }
            }).start();
        });

        // VIEW
        viewBtn.addActionListener((ActionEvent e) -> {
            tableModel.setRowCount(0);
            revealedRow = -1;

            // send GET and rely on messageListener to add rows until END_OF_PASSWORDS
            new Thread(() -> {
                try {
                    client.send("GET");
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(passwordView.this, "Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE)
                    );
                }
            }).start();
        });

        // DELETE
        removeBtn.addActionListener((ActionEvent e) -> {
            int row = passwordTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(passwordView.this,
                        "Select an entry to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String service = (String) tableModel.getValueAt(row, 0);
            String uname = (String) tableModel.getValueAt(row, 1);

            int confirm = JOptionPane.showConfirmDialog(passwordView.this,
                    "Remove password for " + service + " (" + uname + ")?",
                    "Confirm Removal", JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION)
                return;

            // store pending removal so we can remove the correct row on server ACK
            pendingRemoveService = service;
            pendingRemoveUser = uname;

            new Thread(() -> {
                try {
                    // send REMOVE (server expects REMOVE:<service>:<username>)
                    client.send("REMOVE:" + service + ":" + uname);
                    // do not remove locally yet; wait for server confirmation
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(passwordView.this,
                                "Failed to remove: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE)
                    );
                    // clear pending on failure to send
                    pendingRemoveService = null;
                    pendingRemoveUser = null;
                }
            }).start();
        });

        // CHANGE PASSWORD
        changePassBtn.addActionListener((ActionEvent e) -> {
            oldPassField.setText("");
            newPassField.setText("");
            confirmPassField.setText("");
            cardLayout.show(centerPanel, "CHANGE_PASS");
        });

        backBtn2.addActionListener((ActionEvent e) -> cardLayout.show(centerPanel, "MAIN"));

        submitChangePass.addActionListener((ActionEvent e) -> {
            String oldPass = new String(oldPassField.getPassword());
            String newPass = new String(newPassField.getPassword());
            String confirmPass = new String(confirmPassField.getPassword());

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                JOptionPane.showMessageDialog(passwordView.this, "All fields required.", "Input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(passwordView.this, "New passwords do not match.", "Input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (newPass.length() < 4) {
                JOptionPane.showMessageDialog(passwordView.this, "Password must be at least 4 characters.", "Input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            submitChangePass.setEnabled(false);

            new Thread(() -> {
                try {
                    String oldHash = hash(oldPass);
                    String newHash = hash(newPass);
                    client.send("CHANGE_PASS:" + this.username + ":" + oldHash + ":" + newHash);
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(passwordView.this,
                                "Failed to change password: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE)
                    );
                } finally {
                    SwingUtilities.invokeLater(() -> submitChangePass.setEnabled(true));
                }
            }).start();
        });

        // incoming messages from server
        client.setMessageListener((String message) -> {
            // handle special control messages
            if (message.equals("CHANGE_PASS_OK")) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(passwordView.this,
                            "Password changed successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    oldPassField.setText("");
                    newPassField.setText("");
                    confirmPassField.setText("");
                    cardLayout.show(centerPanel, "MAIN");
                });
                return;
            }

            if (message.equals("CHANGE_PASS_FAIL")) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(passwordView.this,
                            "Password change failed. Check your old password.",
                            "Error", JOptionPane.ERROR_MESSAGE)
                );
                return;
            }

            if (message.equals("NO_PASSWORDS")) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(passwordView.this, "No passwords found.", "Info", JOptionPane.INFORMATION_MESSAGE)
                );
                return;
            }

            // server sends END_PASSWORDS (not END_OF_PASSWORDS)
            if (message.equals("END_PASSWORDS")) {
                // finished receiving password list - nothing else to do
                return;
            }

            // handle remove confirmations from server
            if (message.equals("REMOVE_OK")) {
                // remove the matching row (if still present)
                SwingUtilities.invokeLater(() -> {
                    if (pendingRemoveService != null && pendingRemoveUser != null) {
                        for (int i = 0; i < tableModel.getRowCount(); i++) {
                            String s = (String) tableModel.getValueAt(i, 0);
                            String u = (String) tableModel.getValueAt(i, 1);
                            if (pendingRemoveService.equals(s) && pendingRemoveUser.equals(u)) {
                                tableModel.removeRow(i);
                                break;
                            }
                        }
                    }
                    pendingRemoveService = null;
                    pendingRemoveUser = null;
                    JOptionPane.showMessageDialog(passwordView.this, "Password removed.", "Success", JOptionPane.INFORMATION_MESSAGE);
                });
                return;
            }

            if (message.equals("REMOVE_FAIL")) {
                SwingUtilities.invokeLater(() -> {
                    pendingRemoveService = null;
                    pendingRemoveUser = null;
                    JOptionPane.showMessageDialog(passwordView.this, "Failed to remove password on server.", "Error", JOptionPane.ERROR_MESSAGE);
                });
                return;
            }

            if (message.equals("GET_FAILED")) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(passwordView.this, "Failed to retrieve passwords.", "Error", JOptionPane.ERROR_MESSAGE)
                );
                return;
            }

            if (message.equals("UNKNOWN_COMMAND")) {
                // optional: log or show a small notification; ignore to avoid spam
                System.out.println("Server responded: UNKNOWN_COMMAND");
                return;
            }

            // expect "service username encryptedPassword" (space separated)
            String[] parts = message.split(" ", 3);
            if (parts.length == 3) {
                String service = parts[0];
                String uname = parts[1];
                String encryptedPwd = parts[2];

                try {
                    javax.crypto.SecretKey key = KeyManager.getKey();
                    String decryptedPwd = decrypt.decrypt(encryptedPwd, key);
                    SwingUtilities.invokeLater(() -> tableModel.addRow(new Object[]{ service, uname, decryptedPwd }));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> tableModel.addRow(new Object[]{ service, uname, "**DECRYPT ERROR**" }));
                }
            } else {
                // unexpected messages can be logged or ignored
                System.out.println("Unhandled server message: " + message);
            }
        });

        setSize(700, 450);
        setLocationRelativeTo(null);
        setVisible(true);

        if (!client.isConnected()) {
            statusLabel.setIcon(redDot);
            statusLabel.setText(statusLabel.getText() + " (disconnected)");
        }
    }

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class DotIcon implements Icon {
        private final Color color;
        private final int size;

        DotIcon(Color color, int size) { this.color = color; this.size = size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillOval(x, y, size, size);
            g.setColor(Color.DARK_GRAY);
            g.drawOval(x, y, size, size);
        }
        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }
}