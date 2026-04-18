package model;

import java.awt.*;

/**
 * An LED indicator component that shows ON (green glow) or OFF (dark gray).
 * Has 1 input pin and no output pin.
 * Open (unconnected) input defaults to ground (OFF) per requirements.
 */
public class Led extends Component {
    private boolean isOn;

    public Led(String id, int x, int y) {
        super(id, x, y);
        this.isOn = false; // Default OFF = GND per requirements
        inputPins.add(new Pin("", PinType.INPUT, this));
    }

    @Override
    public int getWidth() {
        return 40;
    }

    @Override
    public int getHeight() {
        return 40;
    }

    @Override
    public void evaluate() {
        isOn = inputPins.get(0).getValue();
    }

    public boolean isOn() {
        return isOn;
    }

    @Override
    public String getComponentType() {
        return "LED";
    }

    @Override
    public Point getOutputPinPosition() {
        return null; // LED has no output pin
    }

    @Override
    public void draw(Graphics2D g2d) {
        int w = getWidth(), h = getHeight();
        Graphics2D g = (Graphics2D) g2d.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Glow effect when ON — concentric semi-transparent rings
        if (isOn) {
            for (int i = 3; i >= 1; i--) {
                float alpha = 0.12f * (4 - i);
                g.setColor(new Color(0.18f, 0.80f, 0.44f, alpha));
                int expand = i * 6;
                g.fillOval(x - expand, y - expand, w + expand * 2, h + expand * 2);
            }
        }

        // Shadow (only when OFF, glow replaces shadow when ON)
        if (!isOn) {
            g.setColor(new Color(0, 0, 0, 25));
            g.fillOval(x + 3, y + 3, w, h);
        }

        // LED circle with radial gradient
        Color fillColor = isOn ? new Color(0x2ECC71) : new Color(0x636E72);
        Color borderColor = isOn ? new Color(0x27AE60) : new Color(0x4A4A4A);
        RadialGradientPaint rgp = new RadialGradientPaint(
                x + w / 2f, y + h / 2f, w / 2f,
                new float[]{0f, 1f},
                new Color[]{isOn ? new Color(0x82E0AA) : new Color(0x808B8D), fillColor}
        );
        g.setPaint(rgp);
        g.fillOval(x, y, w, h);

        // Border
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(2.5f));
        g.drawOval(x, y, w, h);

        // LED text
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        FontMetrics fm = g.getFontMetrics();
        String text = "LED";
        g.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + h / 2 + fm.getAscent() / 2 - 1);

        g.dispose();
        drawLabel(g2d);
        drawPins(g2d);
    }
}
