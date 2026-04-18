package model;

/**
 * Represents a wire connecting two pins in the circuit.
 * A wire always goes from an OUTPUT pin (source) to an INPUT pin (target).
 */
public class Wire {
    private Pin source;
    private Pin target;

    public Wire(Pin source, Pin target) {
        this.source = source;
        this.target = target;
    }

    public Pin getSource() {
        return source;
    }

    public Pin getTarget() {
        return target;
    }
}
