package model;

import java.awt.Point;
import java.io.*;
import java.util.*;

/**
 * Central manager for the circuit simulation.
 * Maintains lists of components and wires, handles:
 * - Adding/connecting components with unique pin IDs
 * - Topological-sort-based simulation
 * - Short circuit detection
 * - Truth table generation
 * - Save/load to .circuit files
 */
public class CircuitManager {
    private List<Component> components;
    private List<Wire> wires;
    private int nextPinId;
    private Map<String, Integer> componentCounters;

    public CircuitManager() {
        components = new ArrayList<>();
        wires = new ArrayList<>();
        nextPinId = 1;
        componentCounters = new HashMap<>();
    }

    // ========================================================================
    // COMPONENT MANAGEMENT
    // ========================================================================

    /**
     * Create and add a new component of the given type.
     * Automatically assigns pin IDs and finds a non-overlapping position.
     * @param type one of "AND", "OR", "NOT", "XOR", "SWITCH", "LED"
     * @return the newly created component
     */
    public Component addComponent(String type) {
        Point pos = findAvailablePosition();
        String id = generateComponentId(type);

        Component comp;
        switch (type) {
            case "AND":    comp = new AndGate(id, pos.x, pos.y);     break;
            case "OR":     comp = new OrGate(id, pos.x, pos.y);      break;
            case "NOT":    comp = new NotGate(id, pos.x, pos.y);     break;
            case "XOR":    comp = new XorGate(id, pos.x, pos.y);     break;
            case "SWITCH": comp = new LogicSwitch(id, pos.x, pos.y); break;
            case "LED":    comp = new Led(id, pos.x, pos.y);         break;
            default:       return null;
        }

        assignPinIds(comp);
        components.add(comp);
        return comp;
    }

    public void addComponent(Component c) {
        components.add(c);
    }

    public void addWire(Wire w) {
        wires.add(w);
    }

    /** Generate a unique component ID like "AND_1", "SWITCH_2", etc. */
    private String generateComponentId(String type) {
        int count = componentCounters.getOrDefault(type, 0) + 1;
        componentCounters.put(type, count);
        return type + "_" + count;
    }

    /** Assign the next available sequential pin IDs to all pins of a component. */
    private void assignPinIds(Component comp) {
        for (Pin p : comp.getInputPins()) {
            p.setId(String.valueOf(nextPinId++));
        }
        if (comp.getOutputPin() != null) {
            comp.getOutputPin().setId(String.valueOf(nextPinId++));
        }
    }

    /** Find the next grid position that doesn't overlap with existing components. */
    private Point findAvailablePosition() {
        int gridW = 160, gridH = 130;
        int startX = 80, startY = 60;
        int cols = 6;

        for (int i = 0; i < 100; i++) {
            int col = i % cols;
            int row = i / cols;
            int px = startX + col * gridW;
            int py = startY + row * gridH;

            boolean occupied = false;
            for (Component c : components) {
                if (Math.abs(c.getX() - px) < 100 && Math.abs(c.getY() - py) < 80) {
                    occupied = true;
                    break;
                }
            }
            if (!occupied) return new Point(px, py);
        }
        return new Point(startX, startY);
    }

    // ========================================================================
    // CONNECTION
    // ========================================================================

    /**
     * Connect two pins by their numeric IDs.
     * Validates: source must be OUTPUT, target must be INPUT,
     * target must not be already connected, no self-connection.
     * @return null on success, or an error message string
     */
    public String connectPins(String sourceId, String targetId) {
        Pin source = findPinById(sourceId);
        Pin target = findPinById(targetId);

        if (source == null) return "Source pin '" + sourceId + "' not found.";
        if (target == null) return "Target pin '" + targetId + "' not found.";
        if (source.getType() != PinType.OUTPUT)
            return "Pin '" + sourceId + "' is not an output pin. Source must be an output pin.";
        if (target.getType() != PinType.INPUT)
            return "Pin '" + targetId + "' is not an input pin. Target must be an input pin.";

        // Prevent short circuit: target already driven
        for (Wire w : wires) {
            if (w.getTarget() == target) {
                return "Pin '" + targetId + "' is already connected. Multiple drivers cause short circuit.";
            }
        }

        // Prevent self-connection
        if (source.getParent() == target.getParent()) {
            return "Cannot connect a component to itself.";
        }

        wires.add(new Wire(source, target));
        return null; // success
    }

    /** Look up a Pin object by its string ID across all components. */
    public Pin findPinById(String id) {
        for (Component c : components) {
            for (Pin p : c.getInputPins()) {
                if (p.getId().equals(id)) return p;
            }
            if (c.getOutputPin() != null && c.getOutputPin().getId().equals(id)) {
                return c.getOutputPin();
            }
        }
        return null;
    }

    // ========================================================================
    // SIMULATION
    // ========================================================================

    /**
     * Simulate the entire circuit using topological sort.
     * 1. Reset all pin values to default (GND).
     * 2. Topologically sort components (switches first, then gates, then LEDs).
     * 3. For each component in order: pull input values from wires, then evaluate().
     * Unconnected input pins default to GND (false).
     */
    public void simulate() {
        if (components.isEmpty()) return;

        // Step 1: Reset all pin values
        for (Component c : components) {
            for (Pin p : c.getInputPins()) {
                p.setValue(false); // default to GND
            }
            if (c.getOutputPin() != null) {
                c.getOutputPin().setValue(false);
            }
        }

        // Step 2: Build dependency graph for topological sort
        Map<Component, Set<Component>> dependsOn = new LinkedHashMap<>();
        Map<Component, Integer> inDegree = new LinkedHashMap<>();
        for (Component c : components) {
            dependsOn.put(c, new LinkedHashSet<>());
            inDegree.put(c, 0);
        }

        for (Wire w : wires) {
            Component src = w.getSource().getParent();
            Component tgt = w.getTarget().getParent();
            if (src != tgt && !dependsOn.get(tgt).contains(src)) {
                dependsOn.get(tgt).add(src);
                inDegree.put(tgt, inDegree.get(tgt) + 1);
            }
        }

        // Step 3: Kahn's algorithm for topological sort
        Queue<Component> queue = new LinkedList<>();
        for (Component c : components) {
            if (inDegree.get(c) == 0) {
                queue.add(c);
            }
        }

        List<Component> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            Component c = queue.poll();
            order.add(c);

            for (Component other : components) {
                if (dependsOn.containsKey(other) && dependsOn.get(other).contains(c)) {
                    inDegree.put(other, inDegree.get(other) - 1);
                    if (inDegree.get(other) == 0) {
                        queue.add(other);
                    }
                }
            }
        }

        // Add any remaining components (handles cycles gracefully)
        for (Component c : components) {
            if (!order.contains(c)) order.add(c);
        }

        // Step 4: Evaluate each component in topological order
        for (Component c : order) {
            // Pull input values from connected wires
            for (Pin inputPin : c.getInputPins()) {
                for (Wire w : wires) {
                    if (w.getTarget() == inputPin) {
                        inputPin.setValue(w.getSource().getValue());
                        break;
                    }
                }
                // If no wire found, value stays false (GND) — open LED connection = ground
            }
            c.evaluate();
        }
    }

    // ========================================================================
    // SHORT CIRCUIT DETECTION
    // ========================================================================

    /**
     * Check if any short circuit exists in the circuit.
     * A short circuit occurs when an input pin is driven by multiple output pins.
     * @return true if short circuit detected
     */
    public boolean checkShortCircuit() {
        Map<Pin, Integer> driverCount = new HashMap<>();
        for (Wire w : wires) {
            driverCount.merge(w.getTarget(), 1, Integer::sum);
        }
        for (int count : driverCount.values()) {
            if (count > 1) return true;
        }
        return false;
    }

    /**
     * Generate a detailed report about circuit validity.
     * Checks for: multiple drivers (short circuit), invalid wires, feedback loops.
     */
    public String getShortCircuitReport() {
        StringBuilder report = new StringBuilder();

        // Check 1: Multiple outputs driving same input = short circuit (0 resistance)
        Map<Pin, List<Wire>> inputConnections = new HashMap<>();
        for (Wire w : wires) {
            inputConnections.computeIfAbsent(w.getTarget(), k -> new ArrayList<>()).add(w);
        }
        for (Map.Entry<Pin, List<Wire>> entry : inputConnections.entrySet()) {
            if (entry.getValue().size() > 1) {
                report.append("SHORT CIRCUIT: Pin ").append(entry.getKey().getId())
                        .append(" on ").append(entry.getKey().getParent().getId())
                        .append(" is driven by multiple outputs: ");
                for (Wire w : entry.getValue()) {
                    report.append("pin ").append(w.getSource().getId())
                            .append(" (").append(w.getSource().getParent().getId()).append(") ");
                }
                report.append("\n");
            }
        }

        // Check 2: Invalid wire directions
        for (Wire w : wires) {
            if (w.getSource().getType() != PinType.OUTPUT) {
                report.append("INVALID WIRE: Source pin ").append(w.getSource().getId())
                        .append(" is not an output pin.\n");
            }
            if (w.getTarget().getType() != PinType.INPUT) {
                report.append("INVALID WIRE: Target pin ").append(w.getTarget().getId())
                        .append(" is not an input pin.\n");
            }
        }

        // Check 3: Cycle detection (feedback loops — not valid in combinational circuits)
        Map<Component, Set<Component>> dependsOn = new LinkedHashMap<>();
        Map<Component, Integer> inDegree = new LinkedHashMap<>();
        for (Component c : components) {
            dependsOn.put(c, new LinkedHashSet<>());
            inDegree.put(c, 0);
        }
        for (Wire w : wires) {
            Component src = w.getSource().getParent();
            Component tgt = w.getTarget().getParent();
            if (src != tgt && !dependsOn.get(tgt).contains(src)) {
                dependsOn.get(tgt).add(src);
                inDegree.put(tgt, inDegree.get(tgt) + 1);
            }
        }
        Queue<Component> q = new LinkedList<>();
        for (Component c : components) {
            if (inDegree.get(c) == 0) q.add(c);
        }
        int sortedCount = 0;
        while (!q.isEmpty()) {
            Component c = q.poll();
            sortedCount++;
            for (Component other : components) {
                if (dependsOn.containsKey(other) && dependsOn.get(other).contains(c)) {
                    inDegree.put(other, inDegree.get(other) - 1);
                    if (inDegree.get(other) == 0) q.add(other);
                }
            }
        }
        if (sortedCount < components.size()) {
            report.append("CYCLE DETECTED: Circuit contains feedback loops ")
                    .append("(not allowed in combinational circuits).\n");
        }

        // Check 4: Unconnected inputs on gates (warning, not error)
        for (Component c : components) {
            if (c instanceof LogicSwitch || c instanceof Led) continue;
            for (Pin p : c.getInputPins()) {
                boolean connected = false;
                for (Wire w : wires) {
                    if (w.getTarget() == p) {
                        connected = true;
                        break;
                    }
                }
                if (!connected) {
                    report.append("WARNING: Input pin ").append(p.getId())
                            .append(" on ").append(c.getId())
                            .append(" is unconnected (defaults to GND).\n");
                }
            }
        }

        if (report.length() == 0) {
            return "VALID: No short circuits or issues detected. Circuit is valid.";
        }
        return report.toString();
    }

    // ========================================================================
    // TRUTH TABLE GENERATION
    // ========================================================================

    /**
     * Generate a truth table for all possible input (switch) combinations.
     * @return Object[]{String[] headers, String[][] data}, or null if no switches/LEDs
     */
    public Object[] generateTruthTable() {
        List<LogicSwitch> switches = new ArrayList<>();
        List<Led> leds = new ArrayList<>();

        for (Component c : components) {
            if (c instanceof LogicSwitch) switches.add((LogicSwitch) c);
            if (c instanceof Led) leds.add((Led) c);
        }

        if (switches.isEmpty() || leds.isEmpty()) return null;

        int n = switches.size();
        int m = leds.size();
        int rows = 1 << n; // 2^n combinations

        String[] headers = new String[n + m];
        for (int i = 0; i < n; i++) headers[i] = switches.get(i).getId();
        for (int i = 0; i < m; i++) headers[n + i] = leds.get(i).getId();

        // Save original switch states to restore after
        boolean[] originalStates = new boolean[n];
        for (int i = 0; i < n; i++) originalStates[i] = switches.get(i).getState();

        String[][] data = new String[rows][n + m];
        for (int row = 0; row < rows; row++) {
            // Set each switch to the corresponding bit of the row counter
            for (int i = 0; i < n; i++) {
                boolean val = ((row >> (n - 1 - i)) & 1) == 1;
                switches.get(i).setState(val);
                data[row][i] = val ? "1" : "0";
            }

            simulate();

            for (int i = 0; i < m; i++) {
                data[row][n + i] = leds.get(i).isOn() ? "1" : "0";
            }
        }

        // Restore original switch states
        for (int i = 0; i < n; i++) switches.get(i).setState(originalStates[i]);
        simulate();

        return new Object[]{headers, data};
    }

    // ========================================================================
    // SAVE / LOAD
    // ========================================================================

    /**
     * Save the circuit to a text file.
     * Format:
     *   NEXT_PIN_ID <n>
     *   COUNTER <type> <count>
     *   <TYPE> <ID> <X> <Y> <inputPinIds> <outputPinId> [<state>]
     *   WIRE <sourcePinId> <targetPinId>
     */
    public void saveToFile(File file) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("# Digital Circuit Simulator - Save File");
            pw.println("NEXT_PIN_ID " + nextPinId);

            // Save component counters
            for (Map.Entry<String, Integer> entry : componentCounters.entrySet()) {
                pw.println("COUNTER " + entry.getKey() + " " + entry.getValue());
            }

            // Save components
            for (Component c : components) {
                StringBuilder sb = new StringBuilder();
                sb.append(c.getComponentType());
                sb.append(" ").append(c.getId());
                sb.append(" ").append(c.getX());
                sb.append(" ").append(c.getY());

                // Input pin IDs (comma-separated, or "-" if none)
                sb.append(" ");
                if (c.getInputPins().isEmpty()) {
                    sb.append("-");
                } else {
                    for (int i = 0; i < c.getInputPins().size(); i++) {
                        if (i > 0) sb.append(",");
                        sb.append(c.getInputPins().get(i).getId());
                    }
                }

                // Output pin ID (or "-" if none)
                sb.append(" ");
                if (c.getOutputPin() != null) {
                    sb.append(c.getOutputPin().getId());
                } else {
                    sb.append("-");
                }

                // Extra state for switches
                if (c instanceof LogicSwitch) {
                    sb.append(" ").append(((LogicSwitch) c).getState() ? "ON" : "OFF");
                }

                pw.println(sb.toString());
            }

            // Save wires
            for (Wire w : wires) {
                pw.println("WIRE " + w.getSource().getId() + " " + w.getTarget().getId());
            }
        }
    }

    /**
     * Load a circuit from a text file, replacing the current circuit.
     */
    public void loadFromFile(File file) throws IOException {
        components.clear();
        wires.clear();
        componentCounters.clear();
        Map<String, Pin> pinMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");

                switch (parts[0]) {
                    case "NEXT_PIN_ID":
                        nextPinId = Integer.parseInt(parts[1]);
                        break;

                    case "COUNTER":
                        componentCounters.put(parts[1], Integer.parseInt(parts[2]));
                        break;

                    case "WIRE":
                        Pin src = pinMap.get(parts[1]);
                        Pin tgt = pinMap.get(parts[2]);
                        if (src != null && tgt != null) {
                            wires.add(new Wire(src, tgt));
                        }
                        break;

                    default:
                        // Component line: TYPE ID X Y INPUT_PINS OUTPUT_PIN [STATE]
                        loadComponent(parts, pinMap);
                        break;
                }
            }
        }
    }

    /** Parse a component line from a save file and add it. */
    private void loadComponent(String[] parts, Map<String, Pin> pinMap) {
        String type = parts[0];
        String id = parts[1];
        int cx = Integer.parseInt(parts[2]);
        int cy = Integer.parseInt(parts[3]);
        String inputIds = parts[4];
        String outputId = parts[5];

        Component comp;
        switch (type) {
            case "AND":    comp = new AndGate(id, cx, cy);     break;
            case "OR":     comp = new OrGate(id, cx, cy);      break;
            case "NOT":    comp = new NotGate(id, cx, cy);     break;
            case "XOR":    comp = new XorGate(id, cx, cy);     break;
            case "SWITCH": comp = new LogicSwitch(id, cx, cy); break;
            case "LED":    comp = new Led(id, cx, cy);         break;
            default:       return;
        }

        // Assign saved pin IDs
        if (!inputIds.equals("-")) {
            String[] inIds = inputIds.split(",");
            for (int i = 0; i < Math.min(inIds.length, comp.getInputPins().size()); i++) {
                comp.getInputPins().get(i).setId(inIds[i]);
                pinMap.put(inIds[i], comp.getInputPins().get(i));
            }
        }
        if (!outputId.equals("-") && comp.getOutputPin() != null) {
            comp.getOutputPin().setId(outputId);
            pinMap.put(outputId, comp.getOutputPin());
        }

        // Restore switch state
        if (comp instanceof LogicSwitch && parts.length > 6) {
            ((LogicSwitch) comp).setState(parts[6].equals("ON"));
        }

        components.add(comp);
    }

    /**
     * Clear all components and wires, resetting the circuit.
     */
    public void clear() {
        components.clear();
        wires.clear();
        nextPinId = 1;
        componentCounters.clear();
    }

    // --- Getters ---

    public List<Component> getComponents() {
        return components;
    }

    public List<Wire> getWires() {
        return wires;
    }
}
