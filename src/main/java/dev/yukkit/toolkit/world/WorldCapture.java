package dev.yukkit.toolkit.world;

import dev.yukkit.toolkit.YukkitToolkitClient;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.DeflaterOutputStream;

/**
 * Experimental vanilla Anvil capture from chunks already loaded by the client.
 * Does not fetch hidden data, inventories, entities, structures, or unloaded chunks.
 */
public final class WorldCapture {
    private final Set<Long> saved = new HashSet<>();
    private boolean capturing;
    private Path saveFolder;
    private int ticks;

    public boolean isCapturing() { return capturing; }
    public int getSavedChunks() { return saved.size(); }

    public void start(Minecraft client) {
        if (client.level == null || client.player == null) return;
        if (client.level.dimension() != Level.OVERWORLD) {
            YukkitToolkitClient.notifyPlayer("World Downloader alpha currently supports only the Overworld.");
            return;
        }
        try {
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            saveFolder = client.gameDirectory.toPath().resolve("saves").resolve("YukkitCapture-" + stamp);
            Files.createDirectories(saveFolder.resolve("region"));
            writeLevelDat(client, saveFolder);
            saved.clear();
            ticks = 0;
            capturing = true;
            YukkitToolkitClient.notifyPlayer("Capturing loaded chunks into saves/" + saveFolder.getFileName());
        } catch (Exception e) {
            capturing = false;
            YukkitToolkitClient.LOGGER.error("World capture initialization failed", e);
            YukkitToolkitClient.notifyPlayer("Could not start world capture. Check latest.log.");
        }
    }

    public void stop() {
        capturing = false;
    }

    public void tick(Minecraft client) {
        if (!capturing || client.level == null || client.player == null) return;
        if (client.level.dimension() != Level.OVERWORLD) {
            stop();
            YukkitToolkitClient.notifyPlayer("World capture stopped after a dimension change.");
            return;
        }
        if (++ticks % 8 != 0) return; // Limit synchronous disk writes.
        int cx = client.player.chunkPosition().x();
        int cz = client.player.chunkPosition().z();
        final int radius = 6;
        for (int d = 0; d <= radius; d++) {
            for (int dz = -d; dz <= d; dz++) {
                for (int dx = -d; dx <= d; dx++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != d) continue;
                    int x = cx + dx;
                    int z = cz + dz;
                    long key = ((long) x << 32) ^ (z & 0xFFFFFFFFL);
                    if (saved.contains(key)) continue;
                    var candidate = client.level.getChunkSource().getChunk(x, z, ChunkStatus.FULL, false);
                    if (!(candidate instanceof LevelChunk chunk)) continue;
                    if (chunk == null) continue;
                    try {
                        saveChunk(chunk, saveFolder.resolve("region"));
                        saved.add(key);
                    } catch (Exception e) {
                        stop();
                        YukkitToolkitClient.LOGGER.error("Failed to save chunk {}, {}", x, z, e);
                        YukkitToolkitClient.notifyPlayer("Capture stopped due to an I/O error.");
                    }
                    return;
                }
            }
        }
    }

    private static void writeLevelDat(Minecraft client, Path root) throws IOException {
        CompoundTag data = new CompoundTag();
        data.putInt("DataVersion", 5023);
        data.putString("LevelName", root.getFileName().toString());
        data.putInt("GameType", 1);
        data.putBoolean("allowCommands", true);
        data.putBoolean("hardcore", false);
        data.putInt("Difficulty", 2);
        data.putLong("LastPlayed", System.currentTimeMillis());
        data.putLong("Time", 6000L);
        data.putLong("DayTime", 6000L);
        data.putInt("SpawnX", client.player.blockPosition().getX());
        data.putInt("SpawnY", client.player.blockPosition().getY());
        data.putInt("SpawnZ", client.player.blockPosition().getZ());
        data.putBoolean("WasModded", true);
        data.putString("generatorName", "default");
        CompoundTag version = new CompoundTag();
        version.putInt("Id", 5023);
        version.putString("Name", "26.3");
        version.putBoolean("Snapshot", false);
        version.putInt("Series", 0);
        data.put("Version", version);

        ListTag enabledFeatures = new ListTag();
        enabledFeatures.add(StringTag.valueOf("minecraft:vanilla"));
        data.put("enabled_features", enabledFeatures);
        CompoundTag settings = new CompoundTag();
        settings.putLong("seed", 0L); // Unknown server seed; avoid claiming original world generation.
        settings.putBoolean("generate_features", true);
        settings.putBoolean("bonus_chest", false);
        CompoundTag dimensions = new CompoundTag();
        dimensions.put("minecraft:overworld", dimension("minecraft:overworld", "minecraft:overworld", "minecraft:overworld"));
        dimensions.put("minecraft:the_nether", dimension("minecraft:the_nether", "minecraft:nether", "minecraft:nether"));
        dimensions.put("minecraft:the_end", dimension("minecraft:the_end", "minecraft:end", "minecraft:the_end"));
        settings.put("dimensions", dimensions);
        data.put("WorldGenSettings", settings);

        CompoundTag rootTag = new CompoundTag();
        rootTag.put("Data", data);
        NbtIo.writeCompressed(rootTag, root.resolve("level.dat"));
    }

    private static CompoundTag dimension(String type, String noise, String biomePreset) {
        CompoundTag entry = new CompoundTag();
        entry.putString("type", type);
        CompoundTag generator = new CompoundTag();
        generator.putString("type", "minecraft:noise");
        generator.putString("settings", noise);
        CompoundTag biomes = new CompoundTag();
        biomes.putString("type", "minecraft:multi_noise");
        biomes.putString("preset", biomePreset);
        generator.put("biome_source", biomes);
        entry.put("generator", generator);
        return entry;
    }

    private static void saveChunk(LevelChunk chunk, Path regionDir) throws IOException {
        int cx = chunk.getPos().x();
        int cz = chunk.getPos().z();
        int baseY = chunk.getMinY();
        int height = chunk.getHeight();

        CompoundTag tag = new CompoundTag();
        tag.putInt("DataVersion", 5023);
        tag.putInt("xPos", cx);
        tag.putInt("yPos", Math.floorDiv(baseY, 16));
        tag.putInt("zPos", cz);
        tag.putString("Status", "minecraft:full");
        tag.putLong("LastUpdate", 0L);
        tag.putLong("InhabitedTime", 0L);
        tag.putBoolean("isLightOn", false);

        ListTag sections = new ListTag();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y0 = baseY; y0 < baseY + height; y0 += 16) {
            Map<BlockState, Integer> indices = new HashMap<>();
            ListTag palette = new ListTag();
            int[] data = new int[4096];
            int idx = 0;
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        BlockState state = chunk.getBlockState(pos.set(cx * 16 + x, y0 + y, cz * 16 + z));
                        Integer paletteId = indices.get(state);
                        if (paletteId == null) {
                            paletteId = palette.size();
                            indices.put(state, paletteId);
                            palette.add(serializeState(state));
                        }
                        data[idx++] = paletteId;
                    }
                }
            }
            CompoundTag section = new CompoundTag();
            section.putByte("Y", (byte) Math.floorDiv(y0, 16));
            CompoundTag states = new CompoundTag();
            states.put("palette", palette);
            if (palette.size() > 1) {
                int bits = Math.max(4, 32 - Integer.numberOfLeadingZeros(palette.size() - 1));
                states.putLongArray("data", pack(data, bits));
            }
            section.put("block_states", states);
            CompoundTag biome = new CompoundTag();
            ListTag biomePalette = new ListTag();
            biomePalette.add(StringTag.valueOf("minecraft:plains"));
            biome.put("palette", biomePalette); // Biome data not yet captured in alpha.
            section.put("biomes", biome);
            sections.add(section);
        }
        tag.put("sections", sections);
        tag.put("block_entities", new ListTag());
        tag.put("block_ticks", new ListTag());
        tag.put("fluid_ticks", new ListTag());
        CompoundTag structures = new CompoundTag();
        structures.put("starts", new CompoundTag());
        structures.put("References", new CompoundTag());
        tag.put("structures", structures);

        CompoundTag heightmaps = new CompoundTag();
        int[] heights = new int[256];
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                int top = 0;
                for (int y = baseY + height - 1; y >= baseY; y--) {
                    if (!chunk.getBlockState(pos.set(cx * 16 + x, y, cz * 16 + z)).isAir()) {
                        top = y - baseY + 1;
                        break;
                    }
                }
                heights[z * 16 + x] = top;
            }
        }
        long[] packedHeights = pack(heights, 9);
        for (String type : List.of("WORLD_SURFACE", "MOTION_BLOCKING", "OCEAN_FLOOR", "MOTION_BLOCKING_NO_LEAVES")) {
            heightmaps.putLongArray(type, packedHeights);
        }
        tag.put("Heightmaps", heightmaps);
        writeRegion(regionDir, cx, cz, tag);
    }

    private static CompoundTag serializeState(BlockState state) {
        CompoundTag compound = new CompoundTag();
        compound.putString("Name", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
        if (state.getValues().findAny().isPresent()) {
            CompoundTag properties = new CompoundTag();
            state.getValues().forEach(v -> properties.putString(v.property().getName(), v.valueName()));
            compound.put("Properties", properties);
        }
        return compound;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String propertyValue(Property property, Comparable value) {
        return property.getName(value);
    }

    /** Vanilla padded bit storage (no values crossing 64-bit boundaries). */
    private static long[] pack(int[] values, int bits) {
        int perLong = 64 / bits;
        long[] result = new long[(values.length + perLong - 1) / perLong];
        for (int i = 0; i < values.length; i++) {
            result[i / perLong] |= (long) values[i] << ((i % perLong) * bits);
        }
        return result;
    }

    /**
     * Writes standard 26.3 Anvil region sectors. Appends new chunks;
     * never modifies unrelated Minecraft saves or overwrites existing regions.
     */
    private static void writeRegion(Path dir, int cx, int cz, CompoundTag chunk) throws IOException {
        Files.createDirectories(dir);
        Path file = dir.resolve("r." + Math.floorDiv(cx, 32) + "." + Math.floorDiv(cz, 32) + ".mca");
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(raw)) {
            NbtIo.write(chunk, out);
        }
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (DeflaterOutputStream zip = new DeflaterOutputStream(compressed)) {
            zip.write(raw.toByteArray());
        }
        byte[] payload = compressed.toByteArray();
        int size = payload.length + 5;
        int sectors = (size + 4095) / 4096;
        if (sectors > 255) throw new IOException("Chunk exceeds supported region sector size");

        try (RandomAccessFile region = new RandomAccessFile(file.toFile(), "rw")) {
            if (region.length() < 8192) region.setLength(8192);
            int slot = (cx & 31) + ((cz & 31) << 5);
            region.seek(slot * 4L);
            if (region.readInt() != 0) return; // Existing capture remains untouched.
            long offset = (region.length() + 4095L) / 4096L;
            region.seek(offset * 4096L);
            region.writeInt(payload.length + 1);
            region.writeByte(2); // ZLIB
            region.write(payload);
            region.setLength((offset + sectors) * 4096L);

            region.seek(slot * 4L);
            region.writeInt(((int) offset << 8) | sectors);
            region.seek(4096 + slot * 4L);
            region.writeInt((int) (System.currentTimeMillis() / 1000L));
        }
    }
}
