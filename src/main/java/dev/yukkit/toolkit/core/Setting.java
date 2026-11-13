package dev.yukkit.toolkit.core;

/**
 * A single persisted module option. Values are always stored as a JSON
 * primitive (boolean / number / string) so the config file stays simple.
 */
public abstract class Setting<T> {
    public final String name;
    protected T value;
    protected final T defaultValue;

    protected Setting(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public T get() { return value; }
    public void reset() { value = defaultValue; }

    /** Serialises to a gson-friendly primitive. */
    public abstract Object toJson();
    /** Loads from a gson-parsed primitive; ignores invalid input. */
    public abstract void fromJson(Object raw);
    /** Cycles / steps the value from the GUI. {@code forward=false} steps backwards. */
    public abstract void step(boolean forward);
    public abstract String display();

    // ---------------------------------------------------------------- Bool
    public static final class Bool extends Setting<Boolean> {
        public Bool(String name, boolean def) { super(name, def); }
        public void set(boolean v) { value = v; }
        @Override public Object toJson() { return value; }
        @Override public void fromJson(Object raw) { if (raw instanceof Boolean b) value = b; }
        @Override public void step(boolean forward) { value = !value; }
        @Override public String display() { return value ? "On" : "Off"; }
    }

    // ---------------------------------------------------------------- Number
    public static final class Num extends Setting<Double> {
        public final double min, max, increment;

        public Num(String name, double def, double min, double max, double increment) {
            super(name, def);
            this.min = min;
            this.max = max;
            this.increment = increment;
        }

        public void set(double v) { value = clamp(v); }
        public float asFloat() { return value.floatValue(); }
        public int asInt() { return (int) Math.round(value); }
        private double clamp(double v) { return Math.max(min, Math.min(max, v)); }

        @Override public Object toJson() { return value; }
        @Override public void fromJson(Object raw) { if (raw instanceof Number n) value = clamp(n.doubleValue()); }
        @Override public void step(boolean forward) {
            double next = value + (forward ? increment : -increment);
            if (next > max + 1e-9) next = min;
            if (next < min - 1e-9) next = max;
            value = clamp(Math.round(next / increment) * increment);
        }
        @Override public String display() {
            return increment >= 1.0 ? Integer.toString((int) Math.round(value)) : String.format(java.util.Locale.ROOT, "%.2f", value);
        }
    }

    // ---------------------------------------------------------------- Choice
    public static final class Choice extends Setting<String> {
        public final String[] options;

        public Choice(String name, String def, String... options) {
            super(name, def);
            this.options = options;
        }

        public boolean is(String option) { return value.equalsIgnoreCase(option); }
        private int index() {
            for (int i = 0; i < options.length; i++) if (options[i].equalsIgnoreCase(value)) return i;
            return 0;
        }

        @Override public Object toJson() { return value; }
        @Override public void fromJson(Object raw) {
            if (raw instanceof String s) for (String o : options) if (o.equalsIgnoreCase(s)) { value = o; return; }
        }
        @Override public void step(boolean forward) {
            int i = index() + (forward ? 1 : options.length - 1);
            value = options[i % options.length];
        }
        @Override public String display() { return value; }
    }
}
