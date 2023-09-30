package com.darkender.plugins.survivalinvisiframes;

import me.nahu.scheduler.wrapper.task.WrappedTask;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.util.BoundingBox;

public class DroppedFrameLocation
{
    private final BoundingBox box;
    private WrappedTask removal;
    
    public DroppedFrameLocation(Location location)
    {
        this.box = BoundingBox.of(location, 1.0, 1.0, 1.0);
    }
    
    public boolean isFrame(Item item)
    {
        return box.contains(item.getBoundingBox());
    }
    
    public WrappedTask getRemoval()
    {
        return removal;
    }
    
    public void setRemoval(WrappedTask removal)
    {
        this.removal = removal;
    }
}
