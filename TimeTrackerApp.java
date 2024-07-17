import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class TimeTrackerApp extends JFrame {
    private JTextField taskName;
    private JLabel timerLabel;
    private Timer timer;
    private long startTime;
    private long pausedTime;
    private boolean isRunning;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private PrintWriter logWriter;
    private String logFileName = "session_logs.txt";

    public TimeTrackerApp() {
        setTitle("Time Tracker App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null); // Center the frame

        // Initialize log file writer
        try {
            logWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFileName, true)));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while initializing log file.");
        }

        // Create card layout
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Create login panel
        JPanel loginPanel = new JPanel(new GridLayout(3, 2));
        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton signUpButton = new JButton("Sign Up");
        JButton loginButton = new JButton("Login");

        // Add components to login panel
        loginPanel.add(usernameLabel);
        loginPanel.add(usernameField);
        loginPanel.add(passwordLabel);
        loginPanel.add(passwordField);
        loginPanel.add(signUpButton);
        loginPanel.add(loginButton);

        // Add login panel to card panel
        cardPanel.add(loginPanel, "LOGIN");

        // Create main application panel
        JPanel timeTrackerPanel = new JPanel(new BorderLayout());

        // Time input panel
        JPanel inputPanel = new JPanel();
        JLabel taskNameLabel = new JLabel("Task Name:");
        taskName = new JTextField("", 8);
        inputPanel.add(taskNameLabel);
        inputPanel.add(taskName);

        // Timer display
        timerLabel = new JLabel("00:00:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 40));
        timeTrackerPanel.add(timerLabel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        JButton startButton = new JButton("Start");
        JButton pauseButton = new JButton("Pause");
        JButton stopButton = new JButton("Stop");
        buttonPanel.add(startButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(stopButton);

        // Add input and buttons panel to the main panel
        timeTrackerPanel.add(inputPanel, BorderLayout.NORTH);
        timeTrackerPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add time tracker panel to card panel
        cardPanel.add(timeTrackerPanel, "TRACKER");

        // Add card panel to the frame
        add(cardPanel);

        // Action listeners for buttons
        signUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                char[] password = passwordField.getPassword();
                // Handle sign up action
                signUp(username, password);
                // JOptionPane.showMessageDialog(TimeTrackerApp.this, "Sign up successful for
                // username: " + username);
            }
        });

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                char[] password = passwordField.getPassword();
                // Handle login action
                if (login(username, password)) {
                    // Show time tracker panel after successful login
                    cardLayout.show(cardPanel, "TRACKER");
                } else {
                    JOptionPane.showMessageDialog(TimeTrackerApp.this, "Invalid username or password.");
                }
            }
        });

        // Action listeners for buttons
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startStopwatch(taskName.getText());
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                togglePauseResume(taskName.getText());
            }
        });

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopStopwatch(taskName.getText());
            }
        });

        setVisible(true);
    }

    private void signUp(String username, char[] password) {
        try (BufferedReader br = new BufferedReader(new FileReader("user_details.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 1 && parts[0].equals(username)) {
                    JOptionPane.showMessageDialog(this, "Username already exists. Please choose a different username.");
                    return; // Username already exists, so return without signing up
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred during sign up.");
            return; // Error occurred, so return without signing up
        }

        // If username does not already exist, proceed with sign up
        try (FileWriter writer = new FileWriter("user_details.txt", true);
                BufferedWriter bw = new BufferedWriter(writer);
                PrintWriter out = new PrintWriter(bw)) {
            out.println(username + ":" + String.valueOf(password));
            JOptionPane.showMessageDialog(this, "Sign up successful for username: " + username);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred during sign up.");
        }
    }

    private boolean login(String username, char[] password) {
        try (BufferedReader br = new BufferedReader(new FileReader("user_details.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(String.valueOf(password))) {
                    return true; // Credentials matched
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false; // Credentials not found or incorrect
    }

    private void startStopwatch(String taskTitle) {
        if (!isRunning) {
            startTime = System.currentTimeMillis();
            timer = new Timer(1000, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    long elapsedTime = System.currentTimeMillis() - startTime + pausedTime;
                    int hours = (int) (elapsedTime / 3600000);
                    int minutes = (int) ((elapsedTime / 60000) % 60);
                    int seconds = (int) ((elapsedTime / 1000) % 60);
                    timerLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                }
            });
            timer.start();
            isRunning = true;

            // Log session start time with task title
            logWriter.println("Task: " + taskTitle);
            logWriter.println("Session Start Time: " + new java.util.Date());
            logWriter.flush();
        } else {
            // Log session resume time with task title
            logWriter.println("Task: " + taskTitle);
            logWriter.println("Session Resumed Time: " + new java.util.Date());
            logWriter.flush();
        }
    }

    private void togglePauseResume(String taskTitle) {
        if (timer != null) {
            if (isRunning) {
                timer.stop();
                isRunning = false;
                pausedTime += System.currentTimeMillis() - startTime;

                // Log session pause time with task title
                logWriter.println("Task: " + taskTitle);
                logWriter.println("Session Paused Time: " + new java.util.Date());
                logWriter.flush();
            } else {
                startTime = System.currentTimeMillis();
                timer.start();
                isRunning = true;

                // Log session resume time with task title
                logWriter.println("Task: " + taskTitle);
                logWriter.println("Session Resumed Time: " + new java.util.Date());
                logWriter.flush();
            }
        }
    }

    private void stopStopwatch(String taskTitle) {
        if (timer != null) {
            timer.stop();
            isRunning = false;
            timerLabel.setText("00:00:00");
            pausedTime = 0;

            // Log session stop time with task title
            logWriter.println("Task: " + taskTitle);
            logWriter.println("Session Stop Time: " + new java.util.Date());
            logWriter.println();
            logWriter.flush();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new TimeTrackerApp();
            }
        });
    }
}
