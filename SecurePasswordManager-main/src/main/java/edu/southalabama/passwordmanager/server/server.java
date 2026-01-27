package edu.southalabama.passwordmanager.server;

import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.security.KeyStore;
import javax.net.ssl.*;
import java.sql.*;

public class server {
    private SSLServerSocket serverSocket;
    private databaseController dbController;
    private authController authController;

    public server(int port) throws Exception {
        // Database init
        this.dbController = new databaseController();
        this.authController = new authController();

        dbController.createTable(dbController.connect());

        // Determine folder of running JAR/exe
        String jarFolder = new File(server.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getParent();
        File keystoreFile = new File(jarFolder, "server.keystore.p12");

        if (!keystoreFile.exists()) {
            throw new FileNotFoundException("Server keystore not found: " + keystoreFile.getAbsolutePath());
        }

        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (InputStream ksIs = new FileInputStream(keystoreFile)) {
            ks.load(ksIs, "changeit".toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, "changeit".toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(kmf.getKeyManagers(), null, new java.security.SecureRandom());

        SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
        serverSocket = (SSLServerSocket) ssf.createServerSocket(port);

        // Force TLSv1.2 only to avoid protocol negotiation issues.
        try {
            serverSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
        } catch (Exception ex) {
            System.out.println("Warning: Failed to set TLSv1.2 explicitly on server socket. Using default protocols.");
        }

        System.out.println("Server started on port " + port + " using TLS");

        acceptClients();
    }

    private void acceptClients() {
        while (true) {
            try {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                // set protocol explicitly on the accepted socket as well
                try {
                    clientSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
                } catch (Exception ex) {
                    // non-fatal; continue
                }
                new Thread(() -> handleClient(clientSocket)).start();
            } catch (IOException e) {
                System.out.println("Server accept error:");
                e.printStackTrace();
            }
        }
    }

    // utility: split into at most 'limit' parts, leaving remainder in last element
    private static String[] splitLimit(String s, char sep, int limit) {
        String[] out = new String[limit];
        int start = 0;
        for (int i = 0; i < limit - 1; i++) {
            int pos = s.indexOf(sep, start);
            if (pos == -1) {
                out[i] = s.substring(start);
                for (int j = i + 1; j < limit; j++) out[j] = "";
                return out;
            }
            out[i] = s.substring(start, pos);
            start = pos + 1;
        }
        out[limit - 1] = start <= s.length() ? s.substring(start) : "";
        return out;
    }

    private void handleClient(SSLSocket s) {
        // Make sure the socket gets closed on exit
        try (SSLSocket socket = s;
             DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {

            boolean authenticated = false;
            String currentUser = null;
            String m;

            while (true) {
                try {
                    m = in.readUTF(); // throws EOFException when client closes
                } catch (EOFException e) {
                    System.out.println("Client disconnected (EOF).");
                    break;
                } catch (IOException e) {
                    System.out.println("Server read error:");
                    e.printStackTrace();
                    break;
                }

                if (m == null) continue;
                m = m.trim();
                if ("Over".equalsIgnoreCase(m)) break;

                // DEBUG log
                System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] RECV: \"" + m + "\"");

                // AUTHENTICATION
                if (!authenticated) {
                    if (m.startsWith("AUTH:")) {
                        String[] parts = splitLimit(m, ':', 3); // command:user:hash (hash may contain colons but will be preserved)
                        if (parts.length == 3) {
                            String user = parts[1];
                            String passHash = parts[2];

                            boolean ok;
                            try {
                                ok = authController.authenticate(user, passHash);
                            } catch (Throwable t) {
                                System.out.println("Auth controller threw an exception:");
                                t.printStackTrace();
                                ok = false;
                            }

                            try {
                                if (ok) {
                                    out.writeUTF("AUTH_OK");
                                    out.flush();
                                    authenticated = true;
                                    currentUser = user;
                                    System.out.println("Client authenticated successfully as: " + user);
                                } else {
                                    out.writeUTF("AUTH_FAIL");
                                    out.flush();
                                    System.out.println("Authentication failed for user: " + user);
                                }
                            } catch (IOException e) {
                                System.out.println("Failed to write auth response:");
                                e.printStackTrace();
                                break;
                            }
                        } else {
                            try { out.writeUTF("AUTH_FAIL"); out.flush(); } catch (IOException ignored) {}
                        }
                    } else {
                        try { out.writeUTF("NOT_AUTHENTICATED"); out.flush(); } catch (IOException ignored) {}
                    }
                    continue;
                }

                // parse command name and rest safely (preserve colons in payload)
                String cmd;
                String rest = "";
                int colon = m.indexOf(':');
                if (colon == -1) {
                    cmd = m;
                } else {
                    cmd = m.substring(0, colon);
                    rest = m.substring(colon + 1);
                }

                try {
                    switch (cmd) {
                        case "CHANGE_PASS": {
                            // CHANGE_PASS:user:oldHash:newHash
                            String[] p = splitLimit(m, ':', 4);
                            if (p.length == 4 && p[1].equals(currentUser) && authController.changePassword(p[1], p[2], p[3])) {
                                out.writeUTF("CHANGE_PASS_OK");
                            } else {
                                out.writeUTF("CHANGE_PASS_FAIL");
                            }
                            out.flush();
                            break;
                        }

                        case "ADD": {
                            // ADD:service:username:encryptedPassword  (encryptedPassword may contain ':', preserved by splitLimit)
                            String[] p = splitLimit(m, ':', 4);
                            if (p.length == 4) {
                                Connection c = dbController.connect();
                                if (c != null) {
                                    try {
                                        dbController.insertData(c, p[1], p[2], p[3]);
                                        out.writeUTF("ADD_OK");
                                    } catch (Throwable t) {
                                        System.out.println("DB insert error:");
                                        t.printStackTrace();
                                        out.writeUTF("ADD_FAILED");
                                    } finally {
                                        try { c.close(); } catch (Exception ignored) {}
                                    }
                                } else {
                                    out.writeUTF("ADD_FAILED");
                                }
                            } else {
                                out.writeUTF("ADD_FAILED");
                            }
                            out.flush();
                            break;
                        }

                        case "GET": {
                            Connection c = dbController.connect();
                            if (c == null) {
                                out.writeUTF("GET_FAILED");
                                out.flush();
                                break;
                            }

                            try (Statement st = c.createStatement();
                                 ResultSet rs = st.executeQuery("SELECT Service, Username, Password FROM Passwords")) {

                                boolean sentAny = false;
                                while (rs.next()) {
                                    String srv = rs.getString("Service");
                                    String usr = rs.getString("Username");
                                    String pwd = rs.getString("Password");
                                    if (srv == null) continue;
                                    String line = srv + " " + usr + " " + pwd;
                                    out.writeUTF(line);
                                    out.flush();
                                    sentAny = true;
                                }

                                if (!sentAny) {
                                    out.writeUTF("NO_PASSWORDS");
                                    out.flush();
                                }

                                out.writeUTF("END_PASSWORDS");
                                out.flush();
                            } catch (SQLException | IOException ex) {
                                System.out.println("Failed while handling GET:");
                                ex.printStackTrace();
                                try { out.writeUTF("GET_FAILED"); out.flush(); } catch (IOException ignored) {}
                            } finally {
                                try { c.close(); } catch (Exception ignored) {}
                            }
                            break;
                        }

                        case "REMOVE": {
                            // REMOVE:service:username
                            String[] p = splitLimit(m, ':', 3);
                            if (p.length == 3) {
                                String service = p[1];
                                String user = p[2];
                                Connection c = dbController.connect();
                                boolean deleted = false;
                                if (c != null) {
                                    try {
                                        deleted = dbController.deleteEntry(c, service, user);
                                    } catch (Throwable t) {
                                        System.out.println("DB delete error:");
                                        t.printStackTrace();
                                    } finally {
                                        try { c.close(); } catch (Exception ignored) {}
                                    }
                                }
                                out.writeUTF(deleted ? "REMOVE_OK" : "REMOVE_FAIL");
                            } else {
                                out.writeUTF("REMOVE_FAIL");
                            }
                            out.flush();
                            break;
                        }

                        default:
                            out.writeUTF("UNKNOWN_COMMAND");
                            out.flush();
                            break;
                    }
                } catch (IOException e) {
                    System.out.println("I/O error handling command '" + cmd + "':");
                    e.printStackTrace();
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("IOException in handleClient:");
            e.printStackTrace();
        } catch (Throwable t) {
            System.out.println("Unexpected error in handleClient:");
            t.printStackTrace();
        } finally {
            try {
                s.close();
            } catch (IOException ignored) {}
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 5000;
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter port number for server (default 5000): ");
        String input = scanner.nextLine().trim();
        if (!input.isEmpty()) {
            try {
                int userPort = Integer.parseInt(input);
                if (userPort >= 1 && userPort <= 65535) port = userPort;
            } catch (NumberFormatException ignored) {}
        }

        new server(port);
    }
}
