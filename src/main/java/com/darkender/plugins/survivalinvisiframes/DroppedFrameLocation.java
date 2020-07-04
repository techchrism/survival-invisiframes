package com.darkender.plugins.survivalinvisiframes;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;

public class DroppedFrameLocation
{
    private final BoundingBox box;
    private BukkitTask removal;
    
    public DroppedFrameLocation(Location location)
    {
        this.box = BoundingBox.of(location, 1.0, 1.0, 1.0);
    }
    
    public boolean isFrame(Item item)
    {
        return box.contains(item.getBoundingBox());
    }
    
    public BukkitTask getRemoval()
    {
        return removal;
    }
    
    public void setRemoval(BukkitTask removal)
    {
        this.removal = removal;
    }
}
