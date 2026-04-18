package gui;

import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CircuitCanvas extends JPanel implements MouseListener, MouseMotionListener {
    private CircuitManager circuitManager;

    // --- Component dragging state ---
    private model.Component draggedComponent = null;
    private int dragOffsetX, dragOffsetY;
    private boolean hasDragged = false; // distinguishes click from drag

    // --- Wire drawing state ---
    private Pin wireStartPin = null; // pin where wire drag began
    private Point wireEndPoint = null; // current mouse pos during wire drag
    private Pin hoveredPin = null; // pin under cursor during wire drag (for highlighting)

    // --- Constants ---
    private static final int PIN_HIT_RADIUS = 12; // px radius for detecting pin clicks
    private static final float[] DASH_PATTERN = { 6f, 4f };
    private static final BasicStroke PREVIEW_STROKE = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            10f, DASH_PATTERN, 0f);

    public CircuitCanvas(CircuitManager cm) {
        this.circuitManager = cm;
        setBackground(new Color(0xF0F3F4));
        setPreferredSize(new Dimension(1200, 800));
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    // ========================================================================
    // PAINTING
    // ========================================================================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        drawGrid(g2d);
        drawWires(g2d);

        // Draw compatible-pin highlights during wire drag
        if (wireStartPin != null) {
            drawPinHighlights(g2d);
        }

        // Draw all components
        for (model.Component c : circuitManager.getComponents()) {
            // Slight visual feedback for dragged component
            if (c == draggedComponent && hasDragged) {
                Graphics2D gc = (Graphics2D) g2d.create();
                gc.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
                c.draw(gc);
                gc.dispose();
            } else {
                c.draw(g2d);
            }
        }

        // Draw preview wire during pin-to-pin drag
        if (wireStartPin != null && wireEndPoint != null) {
            drawWirePreview(g2d);
        }
    }

    /** Draw a subtle background grid for visual alignment. */
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(0xE8EAED));
        g2d.setStroke(new BasicStroke(0.5f));
        int w = Math.max(getWidth(), 1200);
        int h = Math.max(getHeight(), 800);
        for (int gx = 0; gx < w; gx += 30) {
            g2d.drawLine(gx, 0, gx, h);
        }
        for (int gy = 0; gy < h; gy += 30) {
            g2d.drawLine(0, gy, w, gy);
        }
    }

    /** Draw all committed wires using Manhattan routing (H-V-H). */
    private void drawWires(Graphics2D g2d) {
        for (Wire wire : circuitManager.getWires()) {
            Pin source = wire.getSource();
            Pin target = wire.getTarget();

            Point sp = source.getParent().getOutputPinPosition();
            int targetIdx = target.getParent().getInputPins().indexOf(target);
            Point tp = target.getParent().getInputPinPosition(targetIdx);

            if (sp == null || tp == null)
                continue;

            // Color wire based on signal: green = HIGH, dark = LOW
            Color wireColor = source.getValue() ? new Color(0x27AE60) : new Color(0x2C3E50);
            g2d.setColor(wireColor);
            g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Manhattan routing
            int midX = (sp.x + tp.x) / 2;
            g2d.drawLine(sp.x, sp.y, midX, sp.y);
            g2d.drawLine(midX, sp.y, midX, tp.y);
            g2d.drawLine(midX, tp.y, tp.x, tp.y);

            // Connection dots
            int dotR = 4;
            g2d.fillOval(sp.x - dotR, sp.y - dotR, dotR * 2, dotR * 2);
            g2d.fillOval(tp.x - dotR, tp.y - dotR, dotR * 2, dotR * 2);
        }
    }

    /**
     * While dragging a wire, highlight all compatible target pins
     * with a pulsing glow so the user knows where they can drop.
     * If dragging from an OUTPUT → highlight INPUT pins (and vice versa).
     */
    private void drawPinHighlights(Graphics2D g2d) {
        PinType compatibleType = (wireStartPin.getType() == PinType.OUTPUT) ? PinType.INPUT : PinType.OUTPUT;

        for (model.Component c : circuitManager.getComponents()) {
            if (c == wireStartPin.getParent())
                continue; // skip self

            // Check input pins
            for (int i = 0; i < c.getInputPins().size(); i++) {
                Pin p = c.getInputPins().get(i);
                if (p.getType() == compatibleType) {
                    Point pos = c.getInputPinPosition(i);
                    if (pos != null)
                        drawPinGlow(g2d, pos, p == hoveredPin);
                }
            }

            // Check output pin
            if (c.getOutputPin() != null && c.getOutputPin().getType() == compatibleType) {
                Point pos = c.getOutputPinPosition();
                if (pos != null)
                    drawPinGlow(g2d, pos, c.getOutputPin() == hoveredPin);
            }
        }
    }

    /** Draw a glow ring around a compatible pin. Brighter if hovered. */
    private void drawPinGlow(Graphics2D g2d, Point pos, boolean isHovered) {
        Graphics2D g = (Graphics2D) g2d.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isHovered) {
            // Strong glow when hovering directly over pin
            for (int i = 3; i >= 1; i--) {
                float alpha = 0.2f * (4 - i);
                g.setColor(new Color(0.2f, 0.6f, 1.0f, alpha));
                int r = model.Component.PIN_RADIUS + i * 5;
                g.fillOval(pos.x - r, pos.y - r, r * 2, r * 2);
            }
        } else {
            // Subtle ring for all compatible pins
            g.setColor(new Color(0x3498DB, true));
            g.setStroke(new BasicStroke(2f));
            int r = model.Component.PIN_RADIUS + 6;
            g.drawOval(pos.x - r, pos.y - r, r * 2, r * 2);
        }

        g.dispose();
    }

    /**
     * Draw a dashed preview wire from the start pin to the current cursor position.
     */
    private void drawWirePreview(Graphics2D g2d) {
        // Determine start position from the pin
        Point startPos = getPinScreenPosition(wireStartPin);
        if (startPos == null)
            return;

        Graphics2D g = (Graphics2D) g2d.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Snap end to hovered pin if near one
        Point endPos = wireEndPoint;
        if (hoveredPin != null) {
            Point snap = getPinScreenPosition(hoveredPin);
            if (snap != null)
                endPos = snap;
        }

        // Dashed line style
        g.setColor(new Color(0x3498DB));
        g.setStroke(PREVIEW_STROKE);

        // Manhattan routing for preview too
        int midX = (startPos.x + endPos.x) / 2;
        g.drawLine(startPos.x, startPos.y, midX, startPos.y);
        g.drawLine(midX, startPos.y, midX, endPos.y);
        g.drawLine(midX, endPos.y, endPos.x, endPos.y);

        // Start dot
        int dotR = 5;
        g.setColor(new Color(0x3498DB));
        g.fillOval(startPos.x - dotR, startPos.y - dotR, dotR * 2, dotR * 2);

        g.dispose();
    }

    // ========================================================================
    // MOUSE HANDLING
    // ========================================================================

    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX(), my = e.getY();
        hasDragged = false;

        // Priority 1: check if clicking on a pin → start wire drag
        Pin pin = findPinAt(mx, my);
        if (pin != null) {
            wireStartPin = pin;
            wireEndPoint = new Point(mx, my);
            repaint();
            return;
        }

        // Priority 2: check if clicking on a component → start component drag
        model.Component comp = findCircuitComponentAt(mx, my);
        if (comp != null) {
            draggedComponent = comp;
            dragOffsetX = mx - comp.getX();
            dragOffsetY = my - comp.getY();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int mx = e.getX(), my = e.getY();

        if (wireStartPin != null) {
            // Wire drag: update endpoint and check for hovered target pin
            wireEndPoint = new Point(mx, my);
            hoveredPin = findPinAt(mx, my);
            // Don't allow hovering on the start pin itself
            if (hoveredPin == wireStartPin)
                hoveredPin = null;
            repaint();
        } else if (draggedComponent != null) {
            // Component drag: reposition the component
            hasDragged = true;
            int newX = Math.max(10, mx - dragOffsetX);
            int newY = Math.max(10, my - dragOffsetY);
            draggedComponent.setX(newX);
            draggedComponent.setY(newY);
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int mx = e.getX(), my = e.getY();

        if (wireStartPin != null) {
            // Complete wire connection if released on a compatible pin
            Pin endPin = findPinAt(mx, my);
            if (endPin != null && endPin != wireStartPin) {
                connectPinsByDrag(wireStartPin, endPin);
            }

            // Clear wire drag state
            wireStartPin = null;
            wireEndPoint = null;
            hoveredPin = null;
            repaint();

        } else if (draggedComponent != null) {
            if (!hasDragged && draggedComponent instanceof LogicSwitch) {
                // Click without drag on a switch → toggle it
                ((LogicSwitch) draggedComponent).toggle();
                circuitManager.simulate();
            }
            draggedComponent = null;
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Update cursor style based on what's under the mouse
        Pin pin = findPinAt(e.getX(), e.getY());
        if (pin != null) {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            setToolTipText("Pin " + pin.getId() + " (" + pin.getType()
                    + " of " + pin.getParent().getId() + ") — drag to connect");
        } else {
            model.Component comp = findCircuitComponentAt(e.getX(), e.getY());
            if (comp != null) {
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                if (comp instanceof LogicSwitch) {
                    setToolTipText(comp.getId() + " — click to toggle, drag to move");
                } else {
                    setToolTipText(comp.getId() + " — drag to move");
                }
            } else {
                setCursor(Cursor.getDefaultCursor());
                setToolTipText(null);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Toggle is handled in mouseReleased (only if !hasDragged)
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        setCursor(Cursor.getDefaultCursor());
        setToolTipText(null);
    }

    // ========================================================================
    // HIT DETECTION
    // ========================================================================

    /**
     * Find the pin closest to (mx, my) within PIN_HIT_RADIUS.
     * Checks all output and input pins across all components.
     */
    private Pin findPinAt(int mx, int my) {
        Pin closest = null;
        double closestDist = PIN_HIT_RADIUS;

        for (model.Component c : circuitManager.getComponents()) {
            // Check output pin
            if (c.getOutputPin() != null) {
                Point op = c.getOutputPinPosition();
                if (op != null) {
                    double d = distance(mx, my, op.x, op.y);
                    if (d < closestDist) {
                        closestDist = d;
                        closest = c.getOutputPin();
                    }
                }
            }

            // Check input pins
            for (int i = 0; i < c.getInputPins().size(); i++) {
                Point ip = c.getInputPinPosition(i);
                if (ip != null) {
                    double d = distance(mx, my, ip.x, ip.y);
                    if (d < closestDist) {
                        closestDist = d;
                        closest = c.getInputPins().get(i);
                    }
                }
            }
        }

        return closest;
    }

    /**
     * Find the component whose bounding box contains (mx, my).
     * Returns the last (topmost) matching component.
     */
    private model.Component findCircuitComponentAt(int mx, int my) {
        // Iterate in reverse so topmost (last-drawn) component is found first
        java.util.List<model.Component> comps = circuitManager.getComponents();
        for (int i = comps.size() - 1; i >= 0; i--) {
            if (comps.get(i).containsPoint(mx, my)) {
                return comps.get(i);
            }
        }
        return null;
    }

    /** Get the screen position of a pin (delegates to its parent component). */
    private Point getPinScreenPosition(Pin pin) {
        model.Component parent = pin.getParent();
        if (pin.getType() == PinType.OUTPUT) {
            return parent.getOutputPinPosition();
        } else {
            int idx = parent.getInputPins().indexOf(pin);
            if (idx >= 0)
                return parent.getInputPinPosition(idx);
        }
        return null;
    }

    private double distance(int x1, int y1, int x2, int y2) {
        double dx = x1 - x2, dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // ========================================================================
    // CONNECTION LOGIC
    // ========================================================================

    /**
     * Attempt to connect two pins that were linked by a drag gesture.
     * Automatically determines which is source (output) and target (input).
     */
    private void connectPinsByDrag(Pin pinA, Pin pinB) {
        String sourceId, targetId;

        if (pinA.getType() == PinType.OUTPUT && pinB.getType() == PinType.INPUT) {
            sourceId = pinA.getId();
            targetId = pinB.getId();
        } else if (pinA.getType() == PinType.INPUT && pinB.getType() == PinType.OUTPUT) {
            sourceId = pinB.getId();
            targetId = pinA.getId();
        } else {
            // Both same type
            String typeName = pinA.getType().toString().toLowerCase();
            JOptionPane.showMessageDialog(this,
                    "Cannot connect two " + typeName + " pins.\n"
                            + "Connect an output pin (blue) to an input pin (red).",
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String error = circuitManager.connectPins(sourceId, targetId);
        if (error != null) {
            JOptionPane.showMessageDialog(this, error,
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        } else {
            circuitManager.simulate();
        }
    }

}
