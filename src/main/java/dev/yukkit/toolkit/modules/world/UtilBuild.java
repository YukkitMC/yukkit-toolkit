package dev.yukkit.toolkit.modules.world;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CartographyTableBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.StonecutterBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

/** Shared block placing helpers for the world modules. All methods are null-safe. */
public final class UtilBuild {
    private static final Minecraft mc = Minecraft.getInstance();

    /** Search order for support blocks: floor first, then the sides, then the ceiling. */
    public static final Direction[] SUPPORT_ORDER = {
            Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP
    };
    public static final Direction[] HORIZONTAL = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
    };

    /** Slot id meaning "use the off hand". */
    public static final int OFFHAND = -1;
    /** Slot id meaning "nothing suitable found". */
    public static final int NONE = -2;

    private UtilBuild() {}

    // ------------------------------------------------------------ queries
    public static boolean ready() {
        return mc.player != null && mc.level != null && mc.gameMode != null && mc.getConnection() != null
                && !mc.player.isSpectator();
    }

    public static boolean isReplaceable(BlockPos pos) {
        if (mc.level == null) return false;
        BlockState state = mc.level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced();
    }

    /** Blocks that open a GUI or toggle when clicked, so they cannot serve as a placing support. */
    public static boolean isInteractive(BlockState state) {
        Block b = state.getBlock();
        return b instanceof BaseEntityBlock || b instanceof DoorBlock || b instanceof TrapDoorBlock
                || b instanceof ButtonBlock || b instanceof LeverBlock || b instanceof CraftingTableBlock
                || b instanceof FenceGateBlock || b instanceof AnvilBlock || b instanceof BedBlock
                || b instanceof NoteBlock || b instanceof RepeaterBlock || b instanceof ComparatorBlock
                || b instanceof BrewingStandBlock || b instanceof CartographyTableBlock
                || b instanceof GrindstoneBlock || b instanceof StonecutterBlock
                || b instanceof EnchantingTableBlock || b instanceof ComposterBlock
                || b instanceof JukeboxBlock || b instanceof LecternBlock || b instanceof DispenserBlock
                || b instanceof HopperBlock || b instanceof AbstractFurnaceBlock
                || b instanceof DecoratedPotBlock || b instanceof CrafterBlock || b instanceof BellBlock
                || b instanceof RespawnAnchorBlock || b instanceof CakeBlock
                || b == Blocks.LOOM || b == Blocks.SMITHING_TABLE || b == Blocks.FLETCHING_TABLE;
    }

    /** A solid block that can safely be right clicked in order to place something next to it. */
    public static boolean isSupport(BlockState state) {
        return !state.isAir() && !state.canBeReplaced() && state.getFluidState().isEmpty() && !isInteractive(state);
    }

    /** A full-cube, non-gravity, non-container block item that is good for building. */
    public static boolean isBuildBlock(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof BlockItem item)) return false;
        if (mc.level == null) return false;
        Block block = item.getBlock();
        if (block instanceof FallingBlock || block == Blocks.TNT || block == Blocks.SCAFFOLDING) return false;
        BlockState state = block.defaultBlockState();
        if (isInteractive(state)) return false;
        return state.isCollisionShapeFullBlock(mc.level, BlockPos.ZERO);
    }

    /**
     * Finds a hotbar slot (0-8), {@link #OFFHAND} or {@link #NONE}. The held item wins when it matches,
     * otherwise the largest matching stack is used.
     */
    public static int findSlot(Predicate<ItemStack> test) {
        Player player = mc.player;
        if (player == null) return NONE;
        var inv = player.getInventory();
        int selected = inv.getSelectedSlot();
        if (test.test(inv.getItem(selected))) return selected;
        if (test.test(player.getOffhandItem())) return OFFHAND;
        int best = NONE;
        int bestCount = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty() || !test.test(stack)) continue;
            if (stack.getCount() > bestCount) {
                bestCount = stack.getCount();
                best = i;
            }
        }
        return best;
    }

    public static int countItems(Predicate<ItemStack> test) {
        Player player = mc.player;
        if (player == null) return 0;
        int total = 0;
        var inv = player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && test.test(stack)) total += stack.getCount();
        }
        ItemStack off = player.getOffhandItem();
        if (!off.isEmpty() && test.test(off)) total += off.getCount();
        return total;
    }

    public static double reach() {
        return mc.player == null ? 4.5 : Math.max(2.0, mc.player.blockInteractionRange() - 0.3);
    }

    /** Face of {@code pos} that looks towards the player's eyes. */
    public static Direction faceTowardPlayer(BlockPos pos) {
        if (mc.player == null) return Direction.UP;
        Vec3 eye = mc.player.getEyePosition();
        Vec3 c = Vec3.atCenterOf(pos);
        return Direction.getApproximateNearest(eye.x - c.x, eye.y - c.y, eye.z - c.z);
    }

    // ------------------------------------------------------------ placing
    /**
     * Places the item found in {@code slot} at {@code target} by clicking an adjacent support block.
     * Returns true when the server was asked to place and the interaction was accepted.
     */
    public static boolean placeAt(BlockPos target, int slot, boolean airPlaceFallback) {
        if (!ready() || slot == NONE || !isReplaceable(target)) return false;
        Vec3 eye = mc.player.getEyePosition();
        double maxSqr = reach() * reach();
        for (Direction d : SUPPORT_ORDER) {
            BlockPos support = target.relative(d);
            if (!isSupport(mc.level.getBlockState(support))) continue;
            Vec3 hit = Vec3.atCenterOf(support).add(Vec3.atCenterOf(target)).scale(0.5);
            if (eye.distanceToSqr(hit) > maxSqr) continue;
            return click(new BlockHitResult(hit, d.getOpposite(), support.immutable(), false), slot);
        }
        if (airPlaceFallback && eye.distanceToSqr(Vec3.atCenterOf(target)) <= maxSqr) {
            return click(new BlockHitResult(Vec3.atCenterOf(target), Direction.UP, target.immutable(), false), slot);
        }
        return false;
    }

    /** Right clicks {@code hit} with the item in {@code slot}, swapping the hotbar for the duration of the click. */
    public static boolean click(BlockHitResult hit, int slot) {
        if (!ready() || slot == NONE) return false;
        Player player = mc.player;
        var inv = player.getInventory();
        int previous = inv.getSelectedSlot();
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (slot == OFFHAND) hand = InteractionHand.OFF_HAND;
        else if (slot != previous) inv.setSelectedSlot(slot);
        boolean ok = false;
        try {
            InteractionResult result = mc.gameMode.useItemOn(mc.player, hand, hit);
            ok = result.shouldSwingHand();
            if (ok) player.swing(hand, SwingAnimation.DEFAULT, false);
        } finally {
            if (slot >= 0 && slot != previous) inv.setSelectedSlot(previous);
        }
        return ok;
    }
}
