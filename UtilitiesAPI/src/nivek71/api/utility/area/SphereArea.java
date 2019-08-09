package nivek71.api.utility.area;

import org.bukkit.Location;

public class SphereArea implements Area {
    private Location center;
    private double radius;

    public SphereArea(Location center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    public Location getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public boolean isInside(Location location) {
        return radius * radius > center.distanceSquared(location);
    }
}
