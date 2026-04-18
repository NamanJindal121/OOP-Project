package model;

import java.awt.*;
import java.awt.geom.*;

/**
 * NOT (inverter) logic gate: outputs the complement of its single input.
 * Has 1 input pin and 1 output pin.
 * Drawn as a triangle with an inversion bubble at the output.
 */
public class NotGate extends Component {

    public NotGate(String id, int x, int y) {
        super(id, x, y);
        inputPins.add(new Pin("", PinType.INPUT, this));
        outputPin = new Pin("", PinType.OUTPUT, this);
    }

    @Override
    public void evaluate() {
        outputPin.setValue(!inputPins.get(0).getValue());
    }

    @Override
    public String getComponentType() {
        return "NOT";
    }

    @Override
    public void draw(Graphics2D g2d) {
        int w = getWidth(), h = getHeight();
        int bubbleR = 6;
        Graphics2D g = (Graphics2D) g2d.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Triangle body
        GeneralPath path = new GeneralPath();
        path.moveTo(x, y);
        path.lineTo(x + w - bubbleR * 2 - 2, y + h / 2);
        path.lineTo(x, y + h);
        path.closePath();

        // Shadow
        AffineTransform shadowTransform = AffineTransform.getTranslateInstance(3, 3);
        g.setColor(new Color(0, 0, 0, 30));
        g.fill(shadowTransform.createTransformedShape(path));

        // Gradient fill
        GradientPaint gp = new GradientPaint(x, y, new Color(0xEC7063), x, y + h, new Color(0xE74C3C));
        g.setPaint(gp);
        g.fill(path);

        // Border
        g.setColor(new Color(0xC0392B));
        g.setStroke(new BasicStroke(2f));
        g.draw(path);

        // Inversion bubble
        int bubbleX = x + w - bubbleR * 2 - 2;
        int bubbleY = y + h / 2 - bubbleR;
        g.setColor(Color.WHITE);
        g.fillOval(bubbleX, bubbleY, bubbleR * 2, bubbleR * 2);
        g.setColor(new Color(0xC0392B));
        g.setStroke(new BasicStroke(2f));
        g.drawOval(bubbleX, bubbleY, bubbleR * 2, bubbleR * 2);

        // Gate label
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        g.drawString("NOT", x + 10, y + h / 2 + fm.getAscent() / 2 - 2);

        g.dispose();
        drawLabel(g2d);
        drawPins(g2d);
    }
}
