package model;

import java.awt.*;
import java.awt.geom.*;

/**
 * XOR (exclusive OR) logic gate: outputs HIGH when inputs differ.
 * Has 2 input pins and 1 output pin.
 * Drawn like an OR gate with an extra curved line on the input side.
 */
public class XorGate extends Component {

    public XorGate(String id, int x, int y) {
        super(id, x, y);
        inputPins.add(new Pin("", PinType.INPUT, this));
        inputPins.add(new Pin("", PinType.INPUT, this));
        outputPin = new Pin("", PinType.OUTPUT, this);
    }

    @Override
    public void evaluate() {
        outputPin.setValue(inputPins.get(0).getValue() ^ inputPins.get(1).getValue());
    }

    @Override
    public String getComponentType() {
        return "XOR";
    }

    @Override
    public void draw(Graphics2D g2d) {
        int w = getWidth(), h = getHeight();
        Graphics2D g = (Graphics2D) g2d.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Build XOR gate shape (same as OR body, shifted slightly right)
        GeneralPath path = new GeneralPath();
        path.moveTo(x + 15, y);
        path.curveTo(x + 38, y, x + 62, y + 10, x + w, y + h / 2);
        path.curveTo(x + 62, y + h - 10, x + 38, y + h, x + 15, y + h);
        path.curveTo(x + 30, y + h * 2 / 3, x + 30, y + h / 3, x + 15, y);
        path.closePath();

        // Shadow
        AffineTransform shadowTransform = AffineTransform.getTranslateInstance(3, 3);
        g.setColor(new Color(0, 0, 0, 30));
        g.fill(shadowTransform.createTransformedShape(path));

        // Gradient fill
        GradientPaint gp = new GradientPaint(x, y, new Color(0xBB8FCE), x, y + h, new Color(0x8E44AD));
        g.setPaint(gp);
        g.fill(path);

        // Border
        g.setColor(new Color(0x6C3483));
        g.setStroke(new BasicStroke(2f));
        g.draw(path);

        // Extra left curve (distinguishes XOR from OR)
        QuadCurve2D extra = new QuadCurve2D.Double(x + 7, y, x + 22, y + h / 2, x + 7, y + h);
        g.draw(extra);

        // Gate label
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fm = g.getFontMetrics();
        String text = "XOR";
        g.drawString(text, x + (w - fm.stringWidth(text)) / 2 + 3, y + h / 2 + fm.getAscent() / 2 - 2);

        g.dispose();
        drawLabel(g2d);
        drawPins(g2d);
    }
}
