package nivek71.api.utility.area;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public interface Area {
    boolean isInside(Location location);

    default boolean isInside(Entity entity) {
        return isInside(entity.getLocation());
    }

    default boolean isInside(World world, double x, double y, double z) {
        return isInside(new Location(world, z, y, z));
    }
}
