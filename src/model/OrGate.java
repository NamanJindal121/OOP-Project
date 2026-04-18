package model;

import java.awt.*;
import java.awt.geom.*;

/**
 * OR logic gate: outputs HIGH when ANY input is HIGH.
 * Has 2 input pins and 1 output pin.
 * Drawn as a curved shield shape with concave left and convex right.
 */
public class OrGate extends Component {

    public OrGate(String id, int x, int y) {
        super(id, x, y);
        inputPins.add(new Pin("", PinType.INPUT, this));
        inputPins.add(new Pin("", PinType.INPUT, this));
        outputPin = new Pin("", PinType.OUTPUT, this);
    }

    @Override
    public void evaluate() {
        boolean result = false;
        for (Pin p : inputPins) {
            result = result || p.getValue();
        }
        outputPin.setValue(result);
    }

    @Override
    public String getComponentType() {
        return "OR";
    }

    @Override
    public void draw(Graphics2D g2d) {
        int w = getWidth(), h = getHeight();
        Graphics2D g = (Graphics2D) g2d.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Build OR gate shape: curved input side, pointed output
        GeneralPath path = new GeneralPath();
        path.moveTo(x + 10, y);
        path.curveTo(x + 35, y, x + 60, y + 10, x + w, y + h / 2);
        path.curveTo(x + 60, y + h - 10, x + 35, y + h, x + 10, y + h);
        path.curveTo(x + 25, y + h * 2 / 3, x + 25, y + h / 3, x + 10, y);
        path.closePath();

        // Shadow
        AffineTransform shadowTransform = AffineTransform.getTranslateInstance(3, 3);
        g.setColor(new Color(0, 0, 0, 30));
        g.fill(shadowTransform.createTransformedShape(path));

        // Gradient fill
        GradientPaint gp = new GradientPaint(x, y, new Color(0x58D68D), x, y + h, new Color(0x27AE60));
        g.setPaint(gp);
        g.fill(path);

        // Border
        g.setColor(new Color(0x1E8449));
        g.setStroke(new BasicStroke(2f));
        g.draw(path);

        // Gate label
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        String text = "OR";
        g.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + h / 2 + fm.getAscent() / 2 - 2);

        g.dispose();
        drawLabel(g2d);
        drawPins(g2d);
    }
}
