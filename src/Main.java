import gui.MainFrame;
import javax.swing.*;

/**
 * Entry point for the Digital Circuit Simulator application.
 * Launches the Swing GUI on the Event Dispatch Thread.
 */
public class Main {
    public static void main(String[] args) {
        // Use the system's native look and feel for a polished appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fall back to default Swing look and feel
        }

        // Launch GUI on the EDT (Event Dispatch Thread) as required by Swing
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
