package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Toolbar panel with color-coded buttons for adding components,
 * connecting pins, and running simulation / analysis.
 */
public class ToolBar extends JPanel {
    private JButton addAnd, addOr, addNot, addXor, addSwitch, addLed;
    private JButton connect, simulate, truthTable, shortCircuit, clearAll;

    public ToolBar() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 8));
        setBackground(new Color(0x2C3E50));
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        addSectionLabel("Components:");
        addAnd    = createButton("AND",    new Color(0x5DADE2));
        addOr     = createButton("OR",     new Color(0x58D68D));
        addNot    = createButton("NOT",    new Color(0xEC7063));
        addXor    = createButton("XOR",    new Color(0xBB8FCE));
        addSwitch = createButton("Switch", new Color(0x2ECC71));
        addLed    = createButton("LED",    new Color(0xF39C12));

        addSeparator();
        addSectionLabel("Actions:");
        connect      = createButton("Connect",       new Color(0x3498DB));
        simulate     = createButton("Simulate",      new Color(0x1ABC9C));
        truthTable   = createButton("Truth Table",   new Color(0xE67E22));
        shortCircuit = createButton("Check Circuit", new Color(0xE74C3C));
        clearAll     = createButton("Clear All",     new Color(0x7F8C8D));
    }

    private void addSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(0xBDC3C7));
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        add(label);
    }

    private void addSeparator() {
        JPanel sep = new JPanel();
        sep.setPreferredSize(new Dimension(2, 30));
        sep.setBackground(new Color(0x34495E));
        add(sep);
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(5, 10, 5, 10));
        btn.setOpaque(true);
        add(btn);
        return btn;
    }

    // --- Button Getters ---
    public JButton getAddAndButton()       { return addAnd; }
    public JButton getAddOrButton()        { return addOr; }
    public JButton getAddNotButton()       { return addNot; }
    public JButton getAddXorButton()       { return addXor; }
    public JButton getAddSwitchButton()    { return addSwitch; }
    public JButton getAddLedButton()       { return addLed; }
    public JButton getConnectButton()      { return connect; }
    public JButton getSimulateButton()     { return simulate; }
    public JButton getTruthTableButton()   { return truthTable; }
    public JButton getShortCircuitButton() { return shortCircuit; }
    public JButton getClearAllButton()     { return clearAll; }
}
