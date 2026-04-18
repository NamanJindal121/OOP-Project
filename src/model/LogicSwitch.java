package model;

import java.awt.*;
import java.awt.geom.*;

/**
 * A toggle switch component that provides a logic HIGH or LOW signal.
 * Has no input pins and 1 output pin.
 * Default state is ON (VCC) per requirement: open switch = VCC.
 * Click to toggle between ON (1) and OFF (0).
 */
public class LogicSwitch extends Component {
    private boolean state;

    public LogicSwitch(String id, int x, int y) {
        super(id, x, y);
        this.state = true; // Default ON = VCC per requirements
        outputPin = new Pin("", PinType.OUTPUT, this);
    }

    @Override
    public int getWidth() {
        return 70;
    }

    @Override
    public int getHeight() {
        return 45;
    }

    @Override
    public void evaluate() {
        outputPin.setValue(state);
    }

    public void toggle() {
        state = !state;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    @Override
    public String getComponentType() {
        return "SWITCH";
    }

    @Override
    public Point getInputPinPosition(int index) {
        return null; // Switch has no input pins
    }

    @Override
    public void draw(Graphics2D g2d) {
        int w = getWidth(), h = getHeight();
        Graphics2D g = (Graphics2D) g2d.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Shadow
        g.setColor(new Color(0, 0, 0, 25));
        g.fill(new RoundRectangle2D.Double(x + 3, y + 3, w, h, 12, 12));

        // Switch body — green when ON, gray when OFF
        Color bgColor = state ? new Color(0x2ECC71) : new Color(0x95A5A6);
        Color borderColor = state ? new Color(0x27AE60) : new Color(0x7F8C8D);

        GradientPaint gp = new GradientPaint(x, y, bgColor.brighter(), x, y + h, bgColor);
        g.setPaint(gp);
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 12, 12));
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(2f));
        g.draw(new RoundRectangle2D.Double(x, y, w, h, 12, 12));

        // Toggle knob (slides right when ON, left when OFF)
        int toggleW = 20, toggleH = h - 14;
        int toggleX = state ? (x + w - toggleW - 7) : (x + 7);
        int toggleY = y + 7;
        g.setColor(Color.WHITE);
        g.fillRoundRect(toggleX, toggleY, toggleW, toggleH, 8, 8);
        g.setColor(borderColor);
        g.drawRoundRect(toggleX, toggleY, toggleW, toggleH, 8, 8);

        // ON/OFF label
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        String stateText = state ? "ON" : "OFF";
        int textX = state ? x + 8 : x + w - fm.stringWidth(stateText) - 8;
        g.drawString(stateText, textX, y + h / 2 + fm.getAscent() / 2 - 1);

        g.dispose();
        drawLabel(g2d);
        drawPins(g2d);
    }
}
