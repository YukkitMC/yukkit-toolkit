package dev.yukkit.toolkit.modules.player;

import dev.yukkit.toolkit.core.ModuleManager;

public final class PlayerModules {
    private PlayerModules() {}

    public static void register() {
        ModuleManager.register(
                new NoFall(), new ChestStealer(), new InvCleaner(), new AutoBuff(), new Blink(),
                new AntiVoid(), new Reach(), new Eagle(), new AutoFish(), new Replenish(),
                new Offhand(), new AntiAFK(), new AutoWalk(), new SmartEat(), new AutoBreak(),
                new FastExp(), new NoSlotSet(), new NoRotateSet(), new PotionSpoof(),
                new AutoWindCharge(), new AutoDeposit(), new ChestCleaner());
    }
}
