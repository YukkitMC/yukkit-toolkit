package dev.yukkit.toolkit.core;

/** Module categories shown as columns in the module screen. */
public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    WORLD("World"),
    RENDER("Render"),
    MISC("Misc"),
    FUN("Fun"),
    EXPLOIT("Exploit");

    public final String label;

    Category(String label) { this.label = label; }
}
