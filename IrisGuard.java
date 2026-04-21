import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class IrisGuard extends JFrame {

    private int focusSecondsLeft = 20 * 60;
    private int breakSecondsLeft = 20;      
    private Timer focusTimer;
    private Timer breakCountdown;
    
    private JLabel statusLabel;
    private JProgressBar healthBar;
    private final Color ACCENT_COLOR = new Color(0, 255, 200);
    private final Color WARNING_COLOR = new Color(255, 80, 80);

    public IrisGuard() {
        setTitle("IrisGuard.exe");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBackground(new Color(30, 30, 35));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        statusLabel = new JLabel("Focusing: 20:00", SwingConstants.CENTER);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 22));

        healthBar = new JProgressBar(0, 20 * 60);
        healthBar.setValue(20 * 60);
        healthBar.setForeground(ACCENT_COLOR);
        healthBar.setBackground(new Color(60, 60, 70));
        healthBar.setBorderPainted(false);

        JButton snoozeBtn = new JButton("Snooze (2 min)");
        snoozeBtn.setFocusPainted(false);
        snoozeBtn.addActionListener(e -> snooze());

        panel.add(statusLabel);
        panel.add(healthBar);
        panel.add(snoozeBtn);
        add(panel);

        focusTimer = new Timer(1000, e -> updateFocusTimer());
        focusTimer.start();
    }

    private void updateFocusTimer() {
        if (focusSecondsLeft > 0) {
            focusSecondsLeft--;
            refreshDisplay();
        } else {
            startStrictBreak();
        }
    }

    private void refreshDisplay() {
        int mins = focusSecondsLeft / 60;
        int secs = focusSecondsLeft % 60;
        statusLabel.setText(String.format("Focusing: %02d:%02d", mins, secs));
        healthBar.setValue(focusSecondsLeft);
    }

    private void startStrictBreak() {
        focusTimer.stop();
        breakSecondsLeft = 20;

        JDialog breakOverlay = new JDialog(this, "EYE BREAK", true);
        breakOverlay.setUndecorated(true);
        breakOverlay.setAlwaysOnTop(true);
        breakOverlay.setBackground(new Color(0, 0, 0, 230)); 
        breakOverlay.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        
        JLabel msg = new JLabel("LOOK AWAY! (20s remaining)", SwingConstants.CENTER);
        msg.setFont(new Font("SansSerif", Font.BOLD, 50));
        msg.setForeground(ACCENT_COLOR);
        breakOverlay.setLayout(new BorderLayout());
        breakOverlay.add(msg, BorderLayout.CENTER);

        final Point startPoint = MouseInfo.getPointerInfo().getLocation();

        breakOverlay.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point currentPoint = e.getLocationOnScreen();
                if (currentPoint.distance(startPoint) > 5) {
                    if (breakSecondsLeft < 19) {
                        breakSecondsLeft = 20; 
                        msg.setText("STAY STILL! Resetting: 20s");
                        msg.setForeground(WARNING_COLOR);
                        startPoint.setLocation(currentPoint); 
                    }
                }
            }
        });

        breakCountdown = new Timer(1000, e -> {
            breakSecondsLeft--;
            if (breakSecondsLeft > 0) {
                msg.setText("LOOK AWAY! (" + breakSecondsLeft + "s remaining)");
                if (breakSecondsLeft < 18) msg.setForeground(ACCENT_COLOR);
            } else {
                ((Timer)e.getSource()).stop();
                breakOverlay.dispose();
                resetFocusTimer();
            }
        });

        breakCountdown.start();
        breakOverlay.setVisible(true);
    }

    private void resetFocusTimer() {
        focusSecondsLeft = 20 * 60;
        refreshDisplay();
        focusTimer.start();
    }

    private void snooze() {

        focusSecondsLeft = 120; 
        
        if (focusSecondsLeft > healthBar.getMaximum()) {
            healthBar.setMaximum(focusSecondsLeft);
        }
        
        refreshDisplay();
        
        if (!focusTimer.isRunning()) {
            focusTimer.start();
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } 
        catch (Exception e) {}

        SwingUtilities.invokeLater(() -> new IrisGuard().setVisible(true));
    }
}