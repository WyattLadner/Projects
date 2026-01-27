package edu.southalabama.passwordmanager.client;

import javax.net.ssl.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import java.security.KeyStore;

public class client {

    private SSLSocket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private Thread reader;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final BlockingQueue<String> inbox = new LinkedBlockingQueue<>();

    public interface MessageListener {
        void onMessage(String message);
    }

    private volatile MessageListener messageListener;

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    /**
     * Connects to a TLS server using the truststore in the same folder as the JAR/exe.
     */
    public client(String host, int port) throws Exception {
        String jarFolder = new File(client.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getParent();
        File truststoreFile = new File(jarFolder, "client.truststore.p12");

        if (!truststoreFile.exists()) {
            throw new FileNotFoundException("Truststore not found: " + truststoreFile.getAbsolutePath());
        }

        KeyStore ts = KeyStore.getInstance("PKCS12");
        try (InputStream tsIs = new FileInputStream(truststoreFile)) {
            ts.load(tsIs, "changeit".toCharArray());
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());

        SSLSocketFactory factory = sslContext.getSocketFactory();
        socket = (SSLSocket) factory.createSocket(host, port);

        // Force TLSv1.2 only to avoid protocol negotiation issues.
        try {
            socket.setEnabledProtocols(new String[]{"TLSv1.2"});
        } catch (Exception ex) {
            System.out.println("Warning: Failed to set TLSv1.2 explicitly. Using default protocols.");
        }

        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        running.set(true);
        startReader();
        System.out.println("Connected to server " + host + ":" + port + " using TLS");
    }

    private void startReader() {
        reader = new Thread(() -> {
            try {
                while (running.get()) {
                    String msg;
                    try {
                        msg = in.readUTF(); // will throw EOFException on remote close or IOException on error
                    } catch (EOFException e) {
                        System.out.println("Server closed the connection (EOF).");
                        break;
                    } catch (IOException e) {
                        if (running.get()) {
                            System.out.println("Client read error:");
                            e.printStackTrace();
                        }
                        break;
                    }

                    // readUTF never returns null; but be defensive
                    if (msg == null) continue;

                    inbox.offer(msg);

                    final String delivered = msg;
                    if (messageListener != null) {
                        SwingUtilities.invokeLater(() -> {
                            try {
                                messageListener.onMessage(delivered);
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        });
                    } else {
                        System.out.println("Server: " + delivered);
                    }
                }
            } finally {
                close();
            }
        }, "Client-Reader");
        reader.setDaemon(true);
        reader.start();
    }

    public synchronized void send(String message) throws IOException {
        if (out == null) throw new IOException("Not connected to server");
        if (message == null || message.trim().isEmpty()) {
            throw new IOException("Cannot send null/empty message");
        }
        try {
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            // If send fails, close connection to ensure consistent state
            close();
            throw e;
        }
    }

    public String sendAndWait(String message, long timeoutMs) throws IOException, InterruptedException {
        inbox.clear();
        send(message);
        if (timeoutMs <= 0) timeoutMs = 5000;
        return inbox.poll(timeoutMs, TimeUnit.MILLISECONDS);
    }

    public boolean isConnected() {
        return running.get() && socket != null && !socket.isClosed() && socket.isConnected();
    }

    public synchronized void close() {
        if (!running.getAndSet(false)) return;
        try { if (in != null) in.close(); } catch (IOException ignored) {}
        try { if (out != null) { out.flush(); out.close(); } } catch (IOException ignored) {}
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
        if (reader != null) reader.interrupt();
        System.out.println("Disconnected");
    }
}
