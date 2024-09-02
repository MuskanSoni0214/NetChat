import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class Server extends JFrame {

    private ServerSocket server;
    private Socket socket;
    private BufferedReader br;
    private PrintWriter out;
    private volatile boolean running = true; // To control the loop

    // Components declaration
    private JLabel heading = new JLabel("Server Area");
    private JTextArea messageArea = new JTextArea();
    private JTextField messageInput = new JTextField();
    private Font font = new Font("Roboto", Font.PLAIN, 20);

    // Constructor
    public Server() {
        SwingUtilities.invokeLater(() -> {
            createGUI();
            try {
                server = new ServerSocket(7777);
                System.out.println("Server is waiting to accept connection...");
                socket = server.accept();
                System.out.println("Connection established with client!");

                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true); // Autoflush enabled

                handleEvents();
                startReading();
                // startWriting(); // Uncomment if you want server to send messages from console input
            } catch (IOException e) {
                e.printStackTrace();
                closeResources();
            }
        });
    }

    private void handleEvents() {
        messageInput.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                // This method is called when a key is pressed
                // You can add your custom logic here
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // This method is called when a key is released
                if (e.getKeyCode() == KeyEvent.VK_ENTER) { // Enter key
                    String contentToSend = messageInput.getText();
                    messageArea.append("Me: " + contentToSend + "\n");
                    out.println(contentToSend);
                    out.flush();
                    messageInput.setText("");
                    messageInput.requestFocus();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                // This method is called when a key is typed (pressed and released)
                // You can add your custom logic here
            }
        });
    }

    private void createGUI() {
        // GUI code
        this.setTitle("Server Messenger [End]");
        this.setSize(600, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Coding for components
        heading.setFont(font);
        messageArea.setFont(font);
        messageInput.setFont(font);
        heading.setIcon(new ImageIcon("CLogo.png"));
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        heading.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        messageArea.setEditable(false);
        messageInput.setHorizontalAlignment(SwingConstants.CENTER);

        // Frame Layout
        this.setLayout(new BorderLayout());

        // Adding components to frame
        this.add(heading, BorderLayout.NORTH);
        JScrollPane jScrollPane = new JScrollPane(messageArea);
        this.add(jScrollPane, BorderLayout.CENTER);
        this.add(messageInput, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    public void startReading() {
        // Read thread
        Runnable r1 = () -> {
            System.out.println("Reader started...");
            try {
                while (running) {
                    String msg = br.readLine();
                    if (msg == null || msg.equalsIgnoreCase("exit")) {
                        System.out.println("Client terminated the chat...");
                        JOptionPane.showMessageDialog(this, "Client terminated the chat.");
                        messageInput.setEnabled(false);
                        running = false;
                        closeResources();
                        break;
                    }
                    System.out.println("Client: " + msg);
                    messageArea.append("Client: " + msg + "\n");
                }
            } catch (IOException e) {
                if (running) {
                    System.out.println("Connection closed unexpectedly.");
                    e.printStackTrace();
                }
            }
        };
        new Thread(r1).start();
    }

    private void closeResources() {
        try {
            if (br != null) br.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            if (server != null && !server.isClosed()) server.close();
            System.out.println("Resources closed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("This is the server... going to start the server...");
        new Server();
    }
}
