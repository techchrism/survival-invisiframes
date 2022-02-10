package com.darkender.plugins.survivalinvisiframes;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SurvivalInvisiframes extends JavaPlugin implements Listener
{
    private NamespacedKey invisibleRecipe;
    private static NamespacedKey invisibleKey;
    private Set<DroppedFrameLocation> droppedFrames;
    
    private boolean framesGlow;
    private boolean firstLoad = true;
    
    // Stays null if not in 1.17
    private Material glowInkSac = null;
    private Material glowFrame = null;
    private EntityType glowFrameEntity = null;

    private String display_name;
    private String display_name_glow;

    @Override
    public void onEnable()
    {
        invisibleRecipe = new NamespacedKey(this, "invisible-recipe");
        invisibleKey = new NamespacedKey(this, "invisible");
        
        droppedFrames = new HashSet<>();
    
        try
        {
            glowInkSac = Material.valueOf("GLOW_INK_SAC");
            glowFrame = Material.valueOf("GLOW_ITEM_FRAME");
            glowFrameEntity = EntityType.valueOf("GLOW_ITEM_FRAME");
        }
        catch(IllegalArgumentException ignored) {}
        
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
            framesGlow = !getConfig().getBoolean("item-frames-glow");
        }
        if(getConfig().getBoolean("item-frames-glow") != framesGlow)
        {
            framesGlow = getConfig().getBoolean("item-frames-glow");
            forceRecheck();
        }
        display_name = getConfig().getString("display_name");
        display_name_glow = getConfig().getString("display_name_glow");
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
    
    private boolean isFrameEntity(Entity entity)
    {
        return (entity != null && (entity.getType() == EntityType.ITEM_FRAME ||
                (glowFrameEntity != null && entity.getType() == glowFrameEntity)));
    }
    
    public static ItemStack generateInvisibleItemFrame()
    {
        ItemStack item = new ItemStack(Material.ITEM_FRAME, 1);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addEnchant(Enchantment.DURABILITY, 1 ,true);
        meta.setDisplayName(ChatColor.WHITE + display_name);
        meta.getPersistentDataContainer().set(invisibleKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }
    
    @EventHandler(ignoreCancelled = true)
    private void onCraft(PrepareItemCraftEvent event)
    {
        if(isInvisibleRecipe(event.getRecipe()) && !event.getView().getPlayer().hasPermission("survivalinvisiframes.craft"))
        {
            event.getInventory().setResult(null);
        }
        else if(glowInkSac != null && glowFrame != null)
        {
            boolean foundFrame = false;
            boolean foundInkSac = false;
            for(ItemStack i : event.getInventory().getMatrix())
            {
                if(i == null || i.getType() == Material.AIR) continue;
                
                if(i.getType() == glowInkSac)
                {
                    if(foundInkSac) return;
                    foundInkSac = true;
                    continue;
                }
                
                if(i.getItemMeta().getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE) &&
                        i.getType() != glowFrame)
                {
                    if(foundFrame) return;
                    foundFrame = true;
                    continue;
                }
                
                // Item isn't what we're looking for
                return;
            }
            
            if(foundFrame && foundInkSac && event.getView().getPlayer().hasPermission("survivalinvisiframes.craft"))
            {
                ItemStack invisibleGlowingItem = generateInvisibleItemFrame();
                ItemMeta meta = invisibleGlowingItem.getItemMeta();
                meta.setDisplayName(ChatColor.WHITE + display_name_glow);
                invisibleGlowingItem.setItemMeta(meta);
                invisibleGlowingItem.setType(glowFrame);
                
                event.getInventory().setResult(invisibleGlowingItem);
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onHangingPlace(HangingPlaceEvent event)
    {
        if(!isFrameEntity(event.getEntity()) || event.getPlayer() == null)
        {
            return;
        }
        
        // Get the frame item that the player placed
        ItemStack frame;
        Player p = event.getPlayer();
        if(p.getInventory().getItemInMainHand().getType() == Material.ITEM_FRAME ||
                (glowFrame != null && p.getInventory().getItemInMainHand().getType() == glowFrame))
        {
            frame = p.getInventory().getItemInMainHand();
        }
        else if(p.getInventory().getItemInOffHand().getType() == Material.ITEM_FRAME ||
                (glowFrame != null && p.getInventory().getItemInOffHand().getType() == glowFrame))
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
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onHangingBreak(HangingBreakEvent event)
    {
        if(!isFrameEntity(event.getEntity()) || !event.getEntity().getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE))
        {
            return;
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
        if(item.getItemStack().getType() != Material.ITEM_FRAME && (glowFrame == null || item.getItemStack().getType() != glowFrame))
        {
            return;
        }
        
        Iterator<DroppedFrameLocation> iter = droppedFrames.iterator();
        while(iter.hasNext())
        {
            DroppedFrameLocation droppedFrameLocation = iter.next();
            if(droppedFrameLocation.isFrame(item))
            {
                ItemStack frame = generateInvisibleItemFrame();
                if(glowFrame != null && item.getItemStack().getType() == glowFrame)
                {
                    ItemMeta meta = frame.getItemMeta();
                    meta.setDisplayName(ChatColor.WHITE + display_name_glow);
                    frame.setItemMeta(meta);
                    frame.setType(glowFrame);
                }
                event.getEntity().setItemStack(frame);
                
                droppedFrameLocation.getRemoval().cancel();
                iter.remove();
                break;
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    private void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        if(!framesGlow)
        {
            return;
        }
        
        if(isFrameEntity(event.getRightClicked()) &&
                event.getRightClicked().getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE))
        {
            ItemFrame frame = (ItemFrame) event.getRightClicked();
            Bukkit.getScheduler().runTaskLater(this, () ->
            {
                if(frame.getItem().getType() != Material.AIR)
                {
                    frame.setGlowing(false);
                    frame.setVisible(false);
                }
            }, 1L);
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    private void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        if(!framesGlow)
        {
            return;
        }
        
        if(isFrameEntity(event.getEntity()) &&
                event.getEntity().getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE))
        {
            ItemFrame frame = (ItemFrame) event.getEntity();
            Bukkit.getScheduler().runTaskLater(this, () ->
            {
                if(frame.getItem().getType() == Material.AIR)
                {
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
