package model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all circuit components (gates, switches, LEDs).
 * Each component has a unique ID, position (x, y), input pins, and an output pin.
 * Subclasses implement evaluate() for logic and draw() for rendering.
 */
public abstract class Component {
    protected String id;
    protected int x, y;
    protected List<Pin> inputPins;
    protected Pin outputPin;

    public static final int PIN_RADIUS = 5;
    public static final int PIN_LEAD = 20;

    public Component(String id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.inputPins = new ArrayList<>();
        this.outputPin = null;
    }

    /** Evaluate the component's logic: compute output from inputs. */
    public abstract void evaluate();

    /** Draw the component on the canvas. */
    public abstract void draw(Graphics2D g2d);

    /** Return the display width of this component. */
    public int getWidth() {
        return 80;
    }

    /** Return the display height of this component. */
    public int getHeight() {
        return 60;
    }

    /**
     * Get the canvas position of the specified input pin.
     * @param index index of the input pin in the inputPins list
     * @return Point at the pin dot location
     */
    public Point getInputPinPosition(int index) {
        int count = inputPins.size();
        if (count == 0) return null;
        int yOffset = getHeight() * (index + 1) / (count + 1);
        return new Point(x - PIN_LEAD, y + yOffset);
    }

    /**
     * Get the canvas position of the output pin.
     * @return Point at the output pin dot location, or null if no output pin
     */
    public Point getOutputPinPosition() {
        if (outputPin == null) return null;
        return new Point(x + getWidth() + PIN_LEAD, y + getHeight() / 2);
    }

    /**
     * Draw input/output pin dots, lead lines, and pin number labels.
     * Called by subclass draw() methods after drawing the component body.
     */
    protected void drawPins(Graphics2D g2d) {
        Graphics2D g = (Graphics2D) g2d.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Font pinFont = new Font("Consolas", Font.BOLD, 11);
        g.setFont(pinFont);
        FontMetrics fm = g.getFontMetrics();

        // --- Draw input pins ---
        for (int i = 0; i < inputPins.size(); i++) {
            Point p = getInputPinPosition(i);
            if (p == null) continue;

            // Lead line from pin dot to component body edge
            g.setColor(new Color(0x555555));
            g.setStroke(new BasicStroke(2f));
            g.drawLine(p.x, p.y, x, p.y);

            // Pin dot (red for input)
            g.setColor(new Color(0xE74C3C));
            g.fillOval(p.x - PIN_RADIUS, p.y - PIN_RADIUS, PIN_RADIUS * 2, PIN_RADIUS * 2);
            g.setColor(new Color(0xC0392B));
            g.setStroke(new BasicStroke(1.2f));
            g.drawOval(p.x - PIN_RADIUS, p.y - PIN_RADIUS, PIN_RADIUS * 2, PIN_RADIUS * 2);

            // Pin number label (above pin dot)
            String label = inputPins.get(i).getId();
            g.setColor(new Color(0x2C3E50));
            g.drawString(label, p.x - fm.stringWidth(label) / 2, p.y - PIN_RADIUS - 3);
        }

        // --- Draw output pin ---
        if (outputPin != null) {
            Point p = getOutputPinPosition();
            if (p != null) {
                int bodyEndX = x + getWidth();

                // Lead line
                g.setColor(new Color(0x555555));
                g.setStroke(new BasicStroke(2f));
                g.drawLine(bodyEndX, y + getHeight() / 2, p.x, p.y);

                // Pin dot (blue for output)
                g.setColor(new Color(0x3498DB));
                g.fillOval(p.x - PIN_RADIUS, p.y - PIN_RADIUS, PIN_RADIUS * 2, PIN_RADIUS * 2);
                g.setColor(new Color(0x2980B9));
                g.setStroke(new BasicStroke(1.2f));
                g.drawOval(p.x - PIN_RADIUS, p.y - PIN_RADIUS, PIN_RADIUS * 2, PIN_RADIUS * 2);

                // Pin number label
                String label = outputPin.getId();
                g.setColor(new Color(0x2C3E50));
                g.drawString(label, p.x - fm.stringWidth(label) / 2, p.y - PIN_RADIUS - 3);
            }
        }

        g.dispose();
    }

    /** Draw the component ID label above the component body. */
    protected void drawLabel(Graphics2D g2d) {
        Graphics2D g = (Graphics2D) g2d.create();
        g.setColor(new Color(0x7F8C8D));
        g.setFont(new Font("SansSerif", Font.ITALIC, 9));
        g.drawString(id, x, y - 8);
        g.dispose();
    }

    /** Check if a screen point falls within this component's bounding box (with margin). */
    public boolean containsPoint(int px, int py) {
        int margin = PIN_LEAD;
        return px >= x - margin && px <= x + getWidth() + margin
                && py >= y - 10 && py <= y + getHeight() + 10;
    }

    /** Return the type identifier string for serialization (e.g. "AND", "OR"). */
    public String getComponentType() {
        return "UNKNOWN";
    }

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public List<Pin> getInputPins() {
        return inputPins;
    }

    public Pin getOutputPin() {
        return outputPin;
    }

    public void setOutputPin(Pin p) {
        this.outputPin = p;
    }
}
