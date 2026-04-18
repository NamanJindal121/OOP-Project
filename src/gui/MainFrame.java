package gui;

import model.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;

/**
 * Main application window (JFrame).
 * Contains a ToolBar (NORTH), CircuitCanvas (CENTER), status bar (SOUTH),
 * and a menu bar with File and Circuit menus.
 * Wires all user actions to the CircuitManager.
 */
public class MainFrame extends JFrame {
    private CircuitManager circuitManager;
    private CircuitCanvas canvas;
    private ToolBar toolBar;
    private JLabel statusLabel;

    public MainFrame() {
        super("Digital Circuit Simulator");
        circuitManager = new CircuitManager();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        // Create components
        canvas = new CircuitCanvas(circuitManager);
        toolBar = new ToolBar();

        // Layout
        setLayout(new BorderLayout());
        add(toolBar, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(canvas);
        scrollPane.getViewport().setBackground(new Color(0xF0F3F4));
        add(scrollPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(new Color(0x1A252F));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        statusLabel = new JLabel("Ready — Add components and connect them to build your circuit.");
        statusLabel.setForeground(new Color(0xBDC3C7));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusBar.add(statusLabel);
        add(statusBar, BorderLayout.SOUTH);

        setupMenuBar();
        setupToolBarActions();

        setVisible(true);
    }

    // ========================================================================
    // MENU BAR
    // ========================================================================

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(0x34495E));
        menuBar.setBorderPainted(false);

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setForeground(Color.BLACK);

        JMenuItem save = new JMenuItem("Save Circuit");
        save.setAccelerator(KeyStroke.getKeyStroke("control S"));
        save.addActionListener(e -> saveCircuit());

        JMenuItem load = new JMenuItem("Load Circuit");
        load.setAccelerator(KeyStroke.getKeyStroke("control O"));
        load.addActionListener(e -> loadCircuit());

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));

        fileMenu.add(save);
        fileMenu.add(load);
        fileMenu.addSeparator();
        fileMenu.add(exit);

        // Circuit menu
        JMenu circuitMenu = new JMenu("Circuit");
        circuitMenu.setForeground(Color.BLACK);

        JMenuItem sim = new JMenuItem("Simulate");
        sim.addActionListener(e -> {
            circuitManager.simulate();
            canvas.repaint();
            statusLabel.setText("Simulation complete.");
        });

        JMenuItem tt = new JMenuItem("Truth Table");
        tt.addActionListener(e -> showTruthTable());

        JMenuItem sc = new JMenuItem("Check Circuit");
        sc.addActionListener(e -> showShortCircuitCheck());

        circuitMenu.add(sim);
        circuitMenu.add(tt);
        circuitMenu.add(sc);

        menuBar.add(fileMenu);
        menuBar.add(circuitMenu);
        setJMenuBar(menuBar);
    }

    // ========================================================================
    // TOOLBAR ACTIONS
    // ========================================================================

    private void setupToolBarActions() {
        // --- Add component buttons ---
        toolBar.getAddAndButton().addActionListener(e -> addComponentAction("AND"));
        toolBar.getAddOrButton().addActionListener(e -> addComponentAction("OR"));
        toolBar.getAddNotButton().addActionListener(e -> addComponentAction("NOT"));
        toolBar.getAddXorButton().addActionListener(e -> addComponentAction("XOR"));
        toolBar.getAddSwitchButton().addActionListener(e -> addComponentAction("SWITCH"));
        toolBar.getAddLedButton().addActionListener(e -> addComponentAction("LED"));

        // --- Connect ---
        toolBar.getConnectButton().addActionListener(e -> connectPinsAction());

        // --- Simulate ---
        toolBar.getSimulateButton().addActionListener(e -> {
            circuitManager.simulate();
            canvas.repaint();
            statusLabel.setText("Simulation complete.");
        });

        // --- Truth Table ---
        toolBar.getTruthTableButton().addActionListener(e -> showTruthTable());

        // --- Short Circuit Check ---
        toolBar.getShortCircuitButton().addActionListener(e -> showShortCircuitCheck());

        // --- Clear All ---
        toolBar.getClearAllButton().addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Clear all components and wires?", "Confirm Clear",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                circuitManager.clear();
                canvas.repaint();
                statusLabel.setText("Circuit cleared.");
            }
        });
    }

    // ========================================================================
    // ACTION IMPLEMENTATIONS
    // ========================================================================

    private void addComponentAction(String type) {
        model.Component c = circuitManager.addComponent(type);
        if (c == null) return;

        StringBuilder msg = new StringBuilder("Added ").append(c.getId());

        if (!c.getInputPins().isEmpty()) {
            msg.append("  |  Input pins: ").append(formatPinList(c.getInputPins()));
        }
        if (c.getOutputPin() != null) {
            msg.append("  |  Output pin: ").append(c.getOutputPin().getId());
        }
        if (c instanceof LogicSwitch) {
            msg.append("  (click to toggle)");
        }

        statusLabel.setText(msg.toString());
        circuitManager.simulate();
        canvas.repaint();
    }

    private void connectPinsAction() {
        JTextField sourceField = new JTextField(8);
        JTextField targetField = new JTextField(8);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(new JLabel("Source pin # (output):"));
        panel.add(sourceField);
        panel.add(new JLabel("Target pin # (input):"));
        panel.add(targetField);
        panel.add(new JLabel(""));
        JLabel hint = new JLabel("(Red dots = input, Blue dots = output)");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 10));
        hint.setForeground(Color.GRAY);
        panel.add(hint);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Connect Pins", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String sourceId = sourceField.getText().trim();
            String targetId = targetField.getText().trim();

            if (sourceId.isEmpty() || targetId.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter both pin numbers.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String error = circuitManager.connectPins(sourceId, targetId);
            if (error != null) {
                JOptionPane.showMessageDialog(this, error,
                        "Connection Error", JOptionPane.ERROR_MESSAGE);
            } else {
                statusLabel.setText("Connected: pin " + sourceId + " --> pin " + targetId);
                circuitManager.simulate();
                canvas.repaint();
            }
        }
    }

    private void showTruthTable() {
        Object[] result = circuitManager.generateTruthTable();
        if (result == null) {
            JOptionPane.showMessageDialog(this,
                    "Need at least one Switch (input) and one LED (output) to generate a truth table.",
                    "Truth Table", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] headers = (String[]) result[0];
        String[][] data = (String[][]) result[1];

        // Create styled table
        JTable table = new JTable(data, headers);
        table.setFont(new Font("Consolas", Font.PLAIN, 14));
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(new Color(0xBDC3C7));
        table.setSelectionBackground(new Color(0xD5F5E3));

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Consolas", Font.BOLD, 14));
        header.setBackground(new Color(0x2C3E50));
        header.setForeground(Color.BLACK);
        header.setReorderingAllowed(false);

        // Center-align all cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(i).setPreferredWidth(80);
        }

        JScrollPane sp = new JScrollPane(table);
        int prefW = Math.min(headers.length * 100 + 30, 700);
        int prefH = Math.min(data.length * 28 + 50, 500);
        sp.setPreferredSize(new Dimension(prefW, prefH));

        JDialog dialog = new JDialog(this, "Truth Table — All Input Combinations", true);
        dialog.setContentPane(sp);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showShortCircuitCheck() {
        String report = circuitManager.getShortCircuitReport();
        boolean hasIssues = !report.startsWith("VALID");

        JTextArea textArea = new JTextArea(report);
        textArea.setEditable(false);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(hasIssues ? new Color(0xFDEDEC) : new Color(0xEAFAF1));
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane sp = new JScrollPane(textArea);
        sp.setPreferredSize(new Dimension(550, 220));

        String title = hasIssues ? "Circuit Issues Found" : "Circuit Valid";
        int msgType = hasIssues ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
        JOptionPane.showMessageDialog(this, sp, title, msgType);
    }

    // ========================================================================
    // SAVE / LOAD
    // ========================================================================

    private void saveCircuit() {
        JFileChooser fc = new JFileChooser(".");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Circuit Files (*.circuit)", "circuit"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (!file.getName().endsWith(".circuit")) {
                file = new File(file.getAbsolutePath() + ".circuit");
            }
            try {
                circuitManager.saveToFile(file);
                statusLabel.setText("Circuit saved to " + file.getName());
                JOptionPane.showMessageDialog(this,
                        "Circuit saved successfully!", "Save", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error saving circuit: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadCircuit() {
        JFileChooser fc = new JFileChooser(".");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Circuit Files (*.circuit)", "circuit"));

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                circuitManager.loadFromFile(fc.getSelectedFile());
                circuitManager.simulate();
                canvas.repaint();
                statusLabel.setText("Circuit loaded from " + fc.getSelectedFile().getName());
                JOptionPane.showMessageDialog(this,
                        "Circuit loaded successfully!", "Load", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading circuit: " + ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ========================================================================
    // HELPERS
    // ========================================================================

    private String formatPinList(java.util.List<Pin> pins) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pins.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(pins.get(i).getId());
        }
        return sb.toString();
    }
}
