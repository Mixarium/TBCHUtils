package org.tbch.tbchutils.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TeleportUtil {
    private static final Random random = new Random();

    private static final List<String> surpriseMessages = new ArrayList<>();
    static {
        surpriseMessages.add("§e[WARN] EntityMoveHandler: velocity data exceeds threshold (got 847.3, max 0.5), clamping pos");
        surpriseMessages.add("§c[ERROR] PhysicsEngine: player desync detected, resetting to last known safe position");
        surpriseMessages.add("§c[ERROR] ChunkLoader: entity position out of valid range, forcing relocation");
        surpriseMessages.add("§c[ERROR] RegionFile: chunk unloaded due to read fault at sector 0x3F2A, repositioning entity");
        surpriseMessages.add("§e[WARN] NetServerHandler: position packet overflow, last valid pos restored (seq=0x4F3A)");
        surpriseMessages.add("§c[ERROR] VoidHandler: entity fell outside world boundary, enabling safety net");
        surpriseMessages.add("§e[WARN] BoundaryCheck: entity coords exceed region limit, transferring to registered fallback zone");
        surpriseMessages.add("§e[WARN] EntityRouter: path resolution failed after 50 attempts, defaulting to contingency destination");
    }

    public static String getRandomMessage() {
        return surpriseMessages.get(random.nextInt(surpriseMessages.size()));
    }
    private static long getRandomCoordinate(long minL, long maxL) {
        long range = maxL - minL + 1;
        return (long) (random.nextDouble() * range) + minL;
    }

    private static boolean isPassable(Block block) {
        switch (block.getType()) {
            case AIR:
            case SAPLING:
            case YELLOW_FLOWER:
            case RED_ROSE:
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case TORCH:
            case REDSTONE_WIRE:
            case SEEDS:
            case SUGAR_CANE_BLOCK:
            case DEAD_BUSH:
            case LONG_GRASS:
            case SNOW:
                return true;
            default:
                return false;
        }
    }

    private static boolean isSolid(Material material) {
        switch (material) {
            case STONE:
            case GRASS:
            case DIRT:
            case COBBLESTONE:
            case WOOD:
            case BEDROCK:
            case SAND:
            case GRAVEL:
            case GOLD_ORE:
            case IRON_ORE:
            case COAL_ORE:
            case LOG:
            case LEAVES:
            case SPONGE:
            case GLASS:
            case LAPIS_ORE:
            case LAPIS_BLOCK:
            case DISPENSER:
            case SANDSTONE:
            case NOTE_BLOCK:
            case WOOL:
            case GOLD_BLOCK:
            case IRON_BLOCK:
            case DOUBLE_STEP:
            case STEP:
            case BRICK:
            case TNT:
            case BOOKSHELF:
            case MOSSY_COBBLESTONE:
            case OBSIDIAN:
            case DIAMOND_ORE:
            case DIAMOND_BLOCK:
            case WORKBENCH:
            case SOIL:
            case FURNACE:
            case BURNING_FURNACE:
            case CHEST:
            case REDSTONE_ORE:
            case GLOWING_REDSTONE_ORE:
            case ICE:
            case SNOW_BLOCK:
            case CLAY:
            case JUKEBOX:
            case FENCE:
            case PUMPKIN:
            case NETHERRACK:
            case SOUL_SAND:
            case GLOWSTONE:
            case JACK_O_LANTERN:
            case LOCKED_CHEST:
                return true;
            default:
                return false;
        }
    }

    private static boolean isSafeGround(Block block) {
        switch (block.getType()) {
            // not solid at all
            case AIR:
            case WATER:
            case STATIONARY_WATER:
                return false;
            // dangerous solids
            case LAVA:
            case STATIONARY_LAVA:
            case FIRE:
            case CACTUS:
                return false;
            default:
                return isSolid(block.getType());
        }
    }

    private static boolean isSafeLocation(Location loc) {
        Block feet = loc.getBlock();
        Block head = feet.getRelative(BlockFace.UP);
        Block ground = feet.getRelative(BlockFace.DOWN);

        if (!isPassable(feet)) return false;
        if (!isPassable(head)) return false;

        if (!isSafeGround(ground)) return false;

        return !(loc.getY() < 0);
    }

    public static List<Double> searchSafeLocation(String worldName, List<Long> coordinateBounds) {
        long x1 = coordinateBounds.get(0), y1 = coordinateBounds.get(1), z1 = coordinateBounds.get(2);
        long x2 = coordinateBounds.get(3), y2 = coordinateBounds.get(4), z2 = coordinateBounds.get(5);

        long minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        long minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        long minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);

        long configNoclipSearchTries = 50;
        for (int attempt = 0; attempt < configNoclipSearchTries; attempt++) {
            double x = Double.parseDouble(getRandomCoordinate(minX, maxX) + ".5");
            double y = getRandomCoordinate(minY, maxY);
            double z = Double.parseDouble(getRandomCoordinate(minZ, maxZ) + ".5");

            Location loc = new Location(Bukkit.getServer().getWorld(worldName), x, y, z);
            if (isSafeLocation(loc)) {
                List<Double> finalCoordinates = new ArrayList<>();
                Collections.addAll(finalCoordinates, x, y, z);
                return finalCoordinates;
            }
        }

        return null;
    }
}
