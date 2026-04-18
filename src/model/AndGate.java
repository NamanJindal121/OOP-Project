package model;

import java.awt.*;
import java.awt.geom.*;

/**
 * AND logic gate: outputs HIGH only when ALL inputs are HIGH.
 * Has 2 input pins and 1 output pin.
 * Drawn as a D-shape (flat left side, semicircular right side).
 */
public class AndGate extends Component {

    public AndGate(String id, int x, int y) {
        super(id, x, y);
        inputPins.add(new Pin("", PinType.INPUT, this));
        inputPins.add(new Pin("", PinType.INPUT, this));
        outputPin = new Pin("", PinType.OUTPUT, this);
    }

    @Override
    public void evaluate() {
        boolean result = true;
        for (Pin p : inputPins) {
            result = result && p.getValue();
        }
        outputPin.setValue(result);
    }

    @Override
    public String getComponentType() {
        return "AND";
    }

    @Override
    public void draw(Graphics2D g2d) {
        int w = getWidth(), h = getHeight();
        Graphics2D g = (Graphics2D) g2d.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Build AND gate shape: flat left, semicircular right
        GeneralPath path = new GeneralPath();
        path.moveTo(x, y);
        path.lineTo(x + w - h / 2, y);
        path.append(new Arc2D.Double(x + w - h, y, h, h, 90, -180, Arc2D.OPEN), true);
        path.lineTo(x, y + h);
        path.closePath();

        // Shadow
        AffineTransform shadowTransform = AffineTransform.getTranslateInstance(3, 3);
        g.setColor(new Color(0, 0, 0, 30));
        g.fill(shadowTransform.createTransformedShape(path));

        // Gradient fill
        GradientPaint gp = new GradientPaint(x, y, new Color(0x5DADE2), x, y + h, new Color(0x2E86C1));
        g.setPaint(gp);
        g.fill(path);

        // Border
        g.setColor(new Color(0x1A5276));
        g.setStroke(new BasicStroke(2f));
        g.draw(path);

        // Gate label
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        String text = "AND";
        g.drawString(text, x + (w - fm.stringWidth(text)) / 2 - 5, y + h / 2 + fm.getAscent() / 2 - 2);

        g.dispose();
        drawLabel(g2d);
        drawPins(g2d);
    }
}
