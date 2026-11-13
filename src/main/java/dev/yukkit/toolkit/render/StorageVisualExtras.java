package dev.yukkit.toolkit.render;

import dev.yukkit.toolkit.ToolkitConfig;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Optional StorageESP tracers and item/storages markers, via the 26.3
 * BEFORE_GIZMOS hook. Never opens nested render passes (Iris/Sodium safe).
 * Chests themselves are rendered by the texture-only ChestModel Mixin,
 * with NO colored box around a chest.
 */
public final class StorageVisualExtras {
    private static final GizmoStyle SHULKER_STYLE =
            GizmoStyle.strokeAndFill(0xFFB757FF,1.5f,0x22B757FF);
    private static final GizmoStyle HOPPER_STYLE =
            GizmoStyle.strokeAndFill(0xFFE0BA6D,1.5f,0x22E0BA6D);
    private static final GizmoStyle ITEM_STYLE =
            GizmoStyle.strokeAndFill(0xFF62DE74,1.5f,0x1162DE74);

    private StorageVisualExtras() {}

    public static void register() {
        LevelRenderEvents.BEFORE_GIZMOS.register(context -> {
            Minecraft mc=Minecraft.getInstance();
            if(mc.level==null || mc.player==null) return;
            ToolkitConfig c=ToolkitConfig.INSTANCE;

            if(c.chestXray && c.storageTracers) {
                Vec3 start=mc.gameRenderer.mainCamera().position();
                int count=0;
                int color=0xFF000000 | (c.storageTracerColor & 0xFFFFFF);
                for(var pos:ChestCandidates.positions()) {
                    if(count++>=96) break;
                    Gizmos.line(start,Vec3.atCenterOf(pos),color,1.0f).setAlwaysOnTop();
                }
            }
            if(c.storageShulkers || c.storageHoppers) {
                int count=0;
                for(var item:ChestCandidates.extras()) {
                    if(count++>=128) break;
                    Gizmos.cuboid(new AABB(item.pos()),item.shulker()?SHULKER_STYLE:HOPPER_STYLE)
                            .setAlwaysOnTop();
                }
            }
            if(c.itemEsp) {
                int count=0;
                for(Entity entity:mc.level.entitiesForRendering()) {
                    if(!(entity instanceof ItemEntity)) continue;
                    if(entity.distanceToSqr(mc.player)>64.0*64.0) continue;
                    Gizmos.cuboid(entity.getBoundingBox().inflate(0.08),ITEM_STYLE)
                            .setAlwaysOnTop();
                    if(++count>=96) break;
                }
            }
        });
    }
}
