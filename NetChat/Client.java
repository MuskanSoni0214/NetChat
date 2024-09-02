import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.*;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class Client extends JFrame {
    private Socket socket;
    private BufferedReader br;
    private PrintWriter out;
    private volatile boolean running = true; // To control the loop

    // Components
    private JLabel heading = new JLabel("Client Area");
    private JTextArea messageArea = new JTextArea();
    private JTextField messageInput = new JTextField();
    private Font font = new Font("Roboto", Font.PLAIN, 20);

    // Client constructor
    public Client() {
        try {
            System.out.println("Sending request to server...");
            socket = new Socket("192.168.1.6", 7777); // Ensure this IP and port match your server
            System.out.println("Connection established with server!");
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true); // Autoflush enabled

            createGUI();
            handleEvents();

            startReading();
            // startWriting(); // Uncomment if you want to manually input messages in console
        } catch (IOException e) {
            e.printStackTrace();
            closeResources();
        }
    }

    private void handleEvents() {
        messageInput.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String contentToSend = messageInput.getText();
                    messageArea.append("Me: " + contentToSend + "\n");
                    out.println(contentToSend);
                    messageInput.setText("");
                    messageInput.requestFocus();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {}
        });
    }

    private void createGUI() {
        this.setTitle("Client Messenger [End]");
        this.setSize(600, 600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Component setup
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

    // Reading
    public void startReading() {
        Runnable r1 = () -> {
            System.out.println("Reader started...");
            try {
                while (running) {
                    String msg = br.readLine();
                    if (msg == null || msg.equalsIgnoreCase("exit")) {
                        System.out.println("Server terminated the chat...");
                        JOptionPane.showMessageDialog(this, "Server terminated the chat.");
                        messageInput.setEnabled(false);
                        running = false;
                        closeResources();
                        break;
                    }
                    System.out.println("Server: " + msg);
                    messageArea.append("Server: " + msg + "\n");
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

    // Writing
    public void startWriting() {
        Runnable r2 = () -> {
            System.out.println("Writer started...");
            try (BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in))) {
                while (!socket.isClosed()) {
                    String content = br1.readLine();
                    out.println(content);
                    if (content.equalsIgnoreCase("exit")) {
                        running = false;
                        socket.close(); // This will also break the reading thread
                        break;
                    }
                }
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(r2).start();
    }

    private void closeResources() {
        try {
            running = false;
            if (br != null) br.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("Resources closed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("This is the client...");
        new Client();
    }
}
