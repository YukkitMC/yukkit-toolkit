package dev.yukkit.toolkit.modules.misc;

import dev.yukkit.toolkit.core.ModuleManager;

/** Registers every module of the Misc category. */
public final class MiscModules {
    private MiscModules() {}

    public static void register() {
        ModuleManager.register(
                new AntiBot(),
                new AntiStaff(),
                new AutoPearl(),
                new BookBot(),
                new EasyPearl(),
                new ElytraSwap(),
                new FlagCheck(),
                new GUICloser(),
                new ItemScroller(),
                new MiddleClickAction(),
                new NameProtect(),
                new Notifier(),
                new PacketLogger(),
                new Spammer(),
                new Teams()
        );
    }
}
