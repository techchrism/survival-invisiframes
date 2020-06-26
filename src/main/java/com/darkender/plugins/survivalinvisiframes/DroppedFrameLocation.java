package com.darkender.plugins.survivalinvisiframes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.util.BoundingBox;

import java.util.Collection;

public class DroppedFrameLocation
{
    private BoundingBox box;
    private Location location;
    private long tick;
    
    public DroppedFrameLocation(Location location, long tick)
    {
        this.location = location;
        this.tick = tick;
        this.box = BoundingBox.of(location, 1.0, 1.0, 1.0);
    }
    
    public Item getFrame()
    {
        Collection<Entity> frames = location.getWorld().getNearbyEntities(box, entity ->
                entity.getType() == EntityType.DROPPED_ITEM && ((Item) entity).getItemStack().getType() == Material.ITEM_FRAME);
        
        if(frames.size() == 0)
        {
            return null;
        }
        else if(frames.size() == 1)
        {
            return (Item) frames.iterator().next();
        }
        else
        {
            Item closest = null;
            double closestDistance = Double.MAX_VALUE;
            for(Entity check : frames)
            {
                double distance = check.getLocation().distance(location);
                if(distance < closestDistance)
                {
                    closestDistance = distance;
                    closest = (Item) check;
                }
            }
            return closest;
        }
    }
    
    public Location getLocation()
    {
        return location;
    }
    
    public BoundingBox getBox()
    {
        return box;
    }
    
    public long getTick()
    {
        return tick;
    }
}
