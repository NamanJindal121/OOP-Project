package model;

/**
 * Represents a pin (connection point) on a circuit component.
 * Each pin has a unique numeric ID (the "number" in the connection scheme),
 * a type (INPUT or OUTPUT), a reference to its parent component,
 * and a boolean logic value.
 */
public class Pin {
    private String id;
    private PinType type;
    private Component parent;
    private boolean value;

    public Pin(String id, PinType type, Component parent) {
        this.id = id;
        this.type = type;
        this.parent = parent;
        this.value = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PinType getType() {
        return type;
    }

    public Component getParent() {
        return parent;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }
}
