package nivek71.api.utility.area;

import org.apache.commons.lang.Validate;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Iterator;
import java.util.Objects;

public class BoxedArea implements Area {
    private Location corner1, corner2;

    public BoxedArea(Location corner1, Location corner2) {
        Validate.isTrue(Objects.equals(corner1.getWorld(), corner2.getWorld()), "corners must be from the same world");
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    public Location getCorner1() {
        return corner1;
    }

    public Location getCorner2() {
        return corner2;
    }

    public Location getLowestCorner() {
        return new Location(corner1.getWorld(), Math.min(corner1.getX(), corner2.getX()), Math.min(corner1.getY(),
                corner2.getY()), Math.min(corner1.getZ(), corner2.getZ()));
    }

    public Location getHighestCorner() {
        return new Location(corner1.getWorld(), Math.max(corner1.getX(), corner2.getX()), Math.max(corner1.getY(),
                corner2.getY()), Math.max(corner1.getZ(), corner2.getZ()));
    }

    private static boolean isBetween(double a, double b, double c) {
        return a <= b && b <= c;
    }

    @Override
    public boolean isInside(Location check) {
        Location lowest = getLowestCorner();
        Location highest = getHighestCorner();
        return (lowest.getWorld() == null || lowest.getWorld().equals(check.getWorld())) &&
                isBetween(lowest.getX(), check.getX(), highest.getX()) &&
                isBetween(lowest.getY(), check.getY(), highest.getY()) &&
                isBetween(lowest.getZ(), check.getZ(), highest.getZ());
    }

    public Iterator<Chunk> getChunksInArea() {
        return new Iterator<Chunk>() {
            private final World world = getCorner1().getWorld();
            private final int beginX;
            private int chunkX, chunkZ;
            private final int endX, endZ;

            {
                Location lowestCorner = getLowestCorner(), highestCorner = getHighestCorner();
                beginX = chunkX = lowestCorner.getChunk().getX() - 1; // exclusive begin
                chunkZ = lowestCorner.getChunk().getZ() + 1; // exclusive end
                endX = highestCorner.getChunk().getX();
                endZ = highestCorner.getChunk().getZ();
            }

            @Override
            public boolean hasNext() {
                return endZ > chunkZ;
            }

            @Override
            public Chunk next() {
                if (++chunkX == endX) {
                    chunkX = beginX;
                    chunkZ++;
                }
                return world.getChunkAt(chunkX, chunkZ);
            }
        };
    }
}
