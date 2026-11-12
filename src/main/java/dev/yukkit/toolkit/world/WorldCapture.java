package dev.yukkit.toolkit.world;

import dev.yukkit.toolkit.YukkitToolkitClient;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

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

    public void start(MinecraftClient client) {
        if (client.world == null || client.player == null) return;
        if (client.world.getRegistryKey() != World.OVERWORLD) {
            YukkitToolkitClient.notifyPlayer("World Downloader alpha currently supports only the Overworld.");
            return;
        }
        try {
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            saveFolder = client.runDirectory.toPath().resolve("saves").resolve("YukkitCapture-" + stamp);
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

    public void tick(MinecraftClient client) {
        if (!capturing || client.world == null || client.player == null) return;
        if (client.world.getRegistryKey() != World.OVERWORLD) {
            stop();
            YukkitToolkitClient.notifyPlayer("World capture stopped after a dimension change.");
            return;
        }
        if (++ticks % 8 != 0) return; // Limit synchronous disk writes.
        int cx = client.player.getChunkPos().x;
        int cz = client.player.getChunkPos().z;
        final int radius = 6;
        for (int d = 0; d <= radius; d++) {
            for (int dz = -d; dz <= d; dz++) {
                for (int dx = -d; dx <= d; dx++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != d) continue;
                    int x = cx + dx;
                    int z = cz + dz;
                    long key = ((long) x << 32) ^ (z & 0xFFFFFFFFL);
                    if (saved.contains(key)) continue;
                    WorldChunk chunk = client.world.getChunkManager().getChunk(x, z, ChunkStatus.FULL, false);
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

    private static void writeLevelDat(MinecraftClient client, Path root) throws IOException {
        NbtCompound data = new NbtCompound();
        data.putInt("DataVersion", 3955);
        data.putString("LevelName", root.getFileName().toString());
        data.putInt("GameType", 1);
        data.putBoolean("allowCommands", true);
        data.putBoolean("hardcore", false);
        data.putInt("Difficulty", 2);
        data.putLong("LastPlayed", System.currentTimeMillis());
        data.putLong("Time", 6000L);
        data.putLong("DayTime", 6000L);
        data.putInt("SpawnX", client.player.getBlockX());
        data.putInt("SpawnY", client.player.getBlockY());
        data.putInt("SpawnZ", client.player.getBlockZ());
        data.putBoolean("WasModded", true);
        data.putString("generatorName", "default");
        NbtCompound version = new NbtCompound();
        version.putInt("Id", 3955);
        version.putString("Name", "1.21.1");
        version.putBoolean("Snapshot", false);
        version.putInt("Series", 0);
        data.put("Version", version);

        NbtList enabledFeatures = new NbtList();
        enabledFeatures.add(NbtString.of("minecraft:vanilla"));
        data.put("enabled_features", enabledFeatures);
        NbtCompound settings = new NbtCompound();
        settings.putLong("seed", 0L); // Unknown server seed; avoid claiming original world generation.
        settings.putBoolean("generate_features", true);
        settings.putBoolean("bonus_chest", false);
        NbtCompound dimensions = new NbtCompound();
        dimensions.put("minecraft:overworld", dimension("minecraft:overworld", "minecraft:overworld", "minecraft:overworld"));
        dimensions.put("minecraft:the_nether", dimension("minecraft:the_nether", "minecraft:nether", "minecraft:nether"));
        dimensions.put("minecraft:the_end", dimension("minecraft:the_end", "minecraft:end", "minecraft:the_end"));
        settings.put("dimensions", dimensions);
        data.put("WorldGenSettings", settings);

        NbtCompound rootTag = new NbtCompound();
        rootTag.put("Data", data);
        NbtIo.writeCompressed(rootTag, root.resolve("level.dat"));
    }

    private static NbtCompound dimension(String type, String noise, String biomePreset) {
        NbtCompound entry = new NbtCompound();
        entry.putString("type", type);
        NbtCompound generator = new NbtCompound();
        generator.putString("type", "minecraft:noise");
        generator.putString("settings", noise);
        NbtCompound biomes = new NbtCompound();
        biomes.putString("type", "minecraft:multi_noise");
        biomes.putString("preset", biomePreset);
        generator.put("biome_source", biomes);
        entry.put("generator", generator);
        return entry;
    }

    private static void saveChunk(WorldChunk chunk, Path regionDir) throws IOException {
        int cx = chunk.getPos().x;
        int cz = chunk.getPos().z;
        int baseY = chunk.getBottomY();
        int height = chunk.getHeight();

        NbtCompound tag = new NbtCompound();
        tag.putInt("DataVersion", 3955);
        tag.putInt("xPos", cx);
        tag.putInt("yPos", Math.floorDiv(baseY, 16));
        tag.putInt("zPos", cz);
        tag.putString("Status", "minecraft:full");
        tag.putLong("LastUpdate", 0L);
        tag.putLong("InhabitedTime", 0L);
        tag.putBoolean("isLightOn", false);

        NbtList sections = new NbtList();
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int y0 = baseY; y0 < baseY + height; y0 += 16) {
            Map<BlockState, Integer> indices = new HashMap<>();
            NbtList palette = new NbtList();
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
            NbtCompound section = new NbtCompound();
            section.putByte("Y", (byte) Math.floorDiv(y0, 16));
            NbtCompound states = new NbtCompound();
            states.put("palette", palette);
            if (palette.size() > 1) {
                int bits = Math.max(4, 32 - Integer.numberOfLeadingZeros(palette.size() - 1));
                states.putLongArray("data", pack(data, bits));
            }
            section.put("block_states", states);
            NbtCompound biome = new NbtCompound();
            NbtList biomePalette = new NbtList();
            biomePalette.add(NbtString.of("minecraft:plains"));
            biome.put("palette", biomePalette); // Biome data not yet captured in alpha.
            section.put("biomes", biome);
            sections.add(section);
        }
        tag.put("sections", sections);
        tag.put("block_entities", new NbtList());
        tag.put("block_ticks", new NbtList());
        tag.put("fluid_ticks", new NbtList());
        NbtCompound structures = new NbtCompound();
        structures.put("starts", new NbtCompound());
        structures.put("References", new NbtCompound());
        tag.put("structures", structures);

        NbtCompound heightmaps = new NbtCompound();
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

    private static NbtCompound serializeState(BlockState state) {
        NbtCompound compound = new NbtCompound();
        compound.putString("Name", Registries.BLOCK.getId(state.getBlock()).toString());
        if (!state.getEntries().isEmpty()) {
            NbtCompound properties = new NbtCompound();
            for (Map.Entry<Property<?>, Comparable<?>> e : state.getEntries().entrySet()) {
                properties.putString(e.getKey().getName(), propertyValue(e.getKey(), e.getValue()));
            }
            compound.put("Properties", properties);
        }
        return compound;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String propertyValue(Property property, Comparable value) {
        return property.name(value);
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
     * Writes standard 1.21.1 Anvil region sectors. Appends new chunks;
     * never modifies unrelated Minecraft saves or overwrites existing regions.
     */
    private static void writeRegion(Path dir, int cx, int cz, NbtCompound chunk) throws IOException {
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
