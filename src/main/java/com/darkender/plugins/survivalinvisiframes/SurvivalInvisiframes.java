package com.darkender.plugins.survivalinvisiframes;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SurvivalInvisiframes extends JavaPlugin implements Listener
{
    private NamespacedKey invisibleRecipe;
    private static NamespacedKey invisibleKey;
    private static NamespacedKey indicatorSlimeKey;
    private Set<DroppedFrameLocation> droppedFrames;
    
    private boolean slimesEnabled;
    private boolean framesGlow;
    private boolean firstLoad = true;
    
    @Override
    public void onEnable()
    {
        invisibleRecipe = new NamespacedKey(this, "invisible-recipe");
        invisibleKey = new NamespacedKey(this, "invisible");
        indicatorSlimeKey = new NamespacedKey(this, "indicator-slime");
        
        droppedFrames = new HashSet<>();
        
        reload();
        
        getServer().getPluginManager().registerEvents(this, this);
        InvisiFramesCommand invisiFramesCommand = new InvisiFramesCommand(this);
        getCommand("iframe").setExecutor(invisiFramesCommand);
        getCommand("iframe").setTabCompleter(invisiFramesCommand);
    }
    
    @Override
    public void onDisable()
    {
        // Remove added recipes on plugin disable
        removeRecipe();
    
        for(World world : Bukkit.getWorlds())
        {
            for(Chunk chunk : world.getLoadedChunks())
            {
                removeSlimes(chunk);
            }
        }
    }
    
    private void removeRecipe()
    {
        Iterator<Recipe> iter = getServer().recipeIterator();
        while(iter.hasNext())
        {
            Recipe check = iter.next();
            if(isInvisibleRecipe(check))
            {
                iter.remove();
                break;
            }
        }
    }
    
    public void setRecipeItem(ItemStack item)
    {
        getConfig().set("recipe", item);
        saveConfig();
        reload();
    }
    
    public void reload()
    {
        saveDefaultConfig();
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        removeRecipe();
        
        if(firstLoad)
        {
            firstLoad = false;
            slimesEnabled = !getConfig().getBoolean("slimes-enabled");
        }
        if(getConfig().getBoolean("slimes-enabled") != slimesEnabled)
        {
            for(World world : Bukkit.getWorlds())
            {
                for(Chunk chunk : world.getLoadedChunks())
                {
                    if(slimesEnabled)
                    {
                        removeSlimes(chunk);
                    }
                    else
                    {
                        addSlimes(chunk);
                    }
                }
            }
        }
        slimesEnabled = getConfig().getBoolean("slimes-enabled");
        framesGlow = getConfig().getBoolean("item-frames-glow");
    
        ItemStack invisibleItem = generateInvisibleItemFrame();
        invisibleItem.setAmount(8);
        
        ItemStack invisibilityPotion = getConfig().getItemStack("recipe");
        ShapedRecipe invisRecipe = new ShapedRecipe(invisibleRecipe, invisibleItem);
        invisRecipe.shape("FFF", "FPF", "FFF");
        invisRecipe.setIngredient('F', Material.ITEM_FRAME);
        invisRecipe.setIngredient('P', new RecipeChoice.ExactChoice(invisibilityPotion));
        Bukkit.addRecipe(invisRecipe);
    }
    
    public void forceRecheck()
    {
        for(World world : Bukkit.getWorlds())
        {
            for(ItemFrame frame : world.getEntitiesByClass(ItemFrame.class))
            {
                if(frame.getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE))
                {
                    Slime slimeFor = getSlimeFor(frame);
                    if(frame.getItem().getType() == Material.AIR && slimeFor == null && slimesEnabled)
                    {
                        addSlimeFor(frame);
                    }
                    else if(frame.getItem().getType() != Material.AIR && slimeFor != null)
                    {
                        slimeFor.remove();
                    }
                    
                    if(frame.getItem().getType() == Material.AIR && framesGlow)
                    {
                        frame.setGlowing(true);
                        frame.setVisible(true);
                    }
                    else if(frame.getItem().getType() != Material.AIR)
                    {
                        frame.setGlowing(false);
                        frame.setVisible(false);
                    }
                }
            }
        }
    }
    
    private boolean isInvisibleRecipe(Recipe recipe)
    {
        return (recipe instanceof ShapedRecipe && ((ShapedRecipe) recipe).getKey().equals(invisibleRecipe));
    }
    
    public static ItemStack generateInvisibleItemFrame()
    {
        ItemStack item = new ItemStack(Material.ITEM_FRAME, 1);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addEnchant(Enchantment.DURABILITY, 1 ,true);
        meta.setDisplayName(ChatColor.WHITE + "Invisible Item Frame");
        meta.getPersistentDataContainer().set(invisibleKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }
    
    private Location getSlimePos(ItemFrame itemFrame)
    {
        return itemFrame.getLocation().getBlock().getRelative(itemFrame.getAttachedFace()).getLocation()
                .add(0.5, 0.2, 0.5).add(itemFrame.getAttachedFace().getDirection().multiply(-0.30));
    }
    
    private void addSlimeFor(ItemFrame itemFrame)
    {
        itemFrame.getWorld().spawn(getSlimePos(itemFrame),
                Slime.class, slime ->
                {
                    slime.setSize(1);
                    slime.setGlowing(true);
                    slime.setInvulnerable(true);
                    slime.setAI(false);
                    slime.setAware(false);
                    slime.setRemoveWhenFarAway(false);
                    slime.setSilent(true);
                    slime.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
                    slime.getPersistentDataContainer().set(indicatorSlimeKey, PersistentDataType.BYTE, (byte) 1);
                });
    }
    
    private Slime getSlimeFor(ItemFrame itemFrame)
    {
        Location pos = getSlimePos(itemFrame);
        BoundingBox check = BoundingBox.of(pos, 0.05, 0.05, 0.05);
        for(Entity e : itemFrame.getWorld().getNearbyEntities(check))
        {
            if(e.getType() == EntityType.SLIME &&
                    e.getPersistentDataContainer().has(indicatorSlimeKey, PersistentDataType.BYTE) &&
                    e.getLocation().distance(pos) < 0.05)
            {
                return (Slime) e;
            }
        }
        return null;
    }
    
    private void removeSlimeFor(ItemFrame itemFrame)
    {
        Slime slime = getSlimeFor(itemFrame);
        if(slime != null)
        {
            slime.remove();
        }
    }
    
    private void addSlimes(Chunk chunk)
    {
        for(Entity entity : chunk.getEntities())
        {
            if(entity.getType() == EntityType.ITEM_FRAME &&
                    entity.getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE))
            {
                ItemFrame frame = (ItemFrame) entity;
                if(frame.getItem().getType() == Material.AIR)
                {
                    addSlimeFor(frame);
                }
            }
        }
    }
    
    private void removeSlimes(Chunk chunk)
    {
        for(Entity entity : chunk.getEntities())
        {
            if(entity.getType() == EntityType.SLIME &&
                    entity.getPersistentDataContainer().has(indicatorSlimeKey, PersistentDataType.BYTE))
            {
                entity.remove();
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    private void onCraft(PrepareItemCraftEvent event)
    {
        if(isInvisibleRecipe(event.getRecipe()) && !event.getView().getPlayer().hasPermission("survivalinvisiframes.craft"))
        {
            event.getInventory().setResult(null);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onHangingPlace(HangingPlaceEvent event)
    {
        if(event.getEntity().getType() != EntityType.ITEM_FRAME || event.getPlayer() == null)
        {
            return;
        }
        
        // Get the frame item that the player placed
        ItemStack frame;
        Player p = event.getPlayer();
        if(p.getInventory().getItemInMainHand().getType() == Material.ITEM_FRAME)
        {
            frame = p.getInventory().getItemInMainHand();
        }
        else if(p.getInventory().getItemInOffHand().getType() == Material.ITEM_FRAME)
        {
            frame = p.getInventory().getItemInOffHand();
        }
        else
        {
            return;
        }
        
        // If the frame item has the invisible tag, make the placed item frame invisible
        if(frame.getItemMeta().getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE))
        {
            if(!p.hasPermission("survivalinvisiframes.place"))
            {
                event.setCancelled(true);
                return;
            }
            ItemFrame itemFrame = (ItemFrame) event.getEntity();
            if(framesGlow)
            {
                itemFrame.setVisible(true);
                itemFrame.setGlowing(true);
            }
            else
            {
                itemFrame.setVisible(false);
            }
            event.getEntity().getPersistentDataContainer().set(invisibleKey, PersistentDataType.BYTE, (byte) 1);
            if(slimesEnabled)
            {
                addSlimeFor(itemFrame);
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onHangingBreak(HangingBreakEvent event)
    {
        if(event.getEntity().getType() != EntityType.ITEM_FRAME ||
                !event.getEntity().getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE))
        {
            return;
        }
        
        if(slimesEnabled)
        {
            removeSlimeFor((ItemFrame) event.getEntity());
        }
        // This is the dumbest possible way to change the drops of an item frame
        // Apparently, there's no api to change the dropped item
        // So this sets up a bounding box that checks for items near the frame and converts them
        DroppedFrameLocation droppedFrameLocation = new DroppedFrameLocation(event.getEntity().getLocation());
        droppedFrames.add(droppedFrameLocation);
        droppedFrameLocation.setRemoval((new BukkitRunnable()
        {
            @Override
            public void run()
            {
                droppedFrames.remove(droppedFrameLocation);
            }
        }).runTaskLater(this, 20L));
    }
    
    @EventHandler
    private void onItemSpawn(ItemSpawnEvent event)
    {
        Item item = event.getEntity();
        if(item.getItemStack().getType() != Material.ITEM_FRAME)
        {
            return;
        }
        
        Iterator<DroppedFrameLocation> iter = droppedFrames.iterator();
        while(iter.hasNext())
        {
            DroppedFrameLocation droppedFrameLocation = iter.next();
            if(droppedFrameLocation.isFrame(item))
            {
                event.getEntity().setItemStack(generateInvisibleItemFrame());
                
                droppedFrameLocation.getRemoval().cancel();
                iter.remove();
                break;
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    private void onChunkLoad(ChunkLoadEvent event)
    {
        if(slimesEnabled)
        {
            addSlimes(event.getChunk());
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    private void onChunkUnload(ChunkUnloadEvent event)
    {
        if(slimesEnabled)
        {
            removeSlimes(event.getChunk());
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    private void onEntityDamage(EntityDamageEvent event)
    {
        if(event.getEntityType() == EntityType.SLIME &&
                event.getEntity().getPersistentDataContainer().has(indicatorSlimeKey, PersistentDataType.BYTE))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    private void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        if(!slimesEnabled && !framesGlow)
        {
            return;
        }
        
        if(event.getRightClicked().getType() == EntityType.ITEM_FRAME &&
                event.getRightClicked().getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE))
        {
            ItemFrame frame = (ItemFrame) event.getRightClicked();
            Bukkit.getScheduler().runTaskLater(this, () ->
            {
                if(frame.getItem().getType() != Material.AIR)
                {
                    removeSlimeFor(frame);
                    frame.setGlowing(false);
                    frame.setVisible(false);
                }
            }, 1L);
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    private void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        if(!slimesEnabled && !framesGlow)
        {
            return;
        }
        
        if(event.getEntityType() == EntityType.ITEM_FRAME &&
                event.getEntity().getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE))
        {
            ItemFrame frame = (ItemFrame) event.getEntity();
            Bukkit.getScheduler().runTaskLater(this, () ->
            {
                if(frame.getItem().getType() == Material.AIR)
                {
                    if(slimesEnabled)
                    {
                        addSlimeFor(frame);
                    }
                    if(framesGlow)
                    {
                        frame.setGlowing(true);
                        frame.setVisible(true);
                    }
                }
            }, 1L);
        }
    }
}
