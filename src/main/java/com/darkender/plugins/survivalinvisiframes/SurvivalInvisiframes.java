package com.darkender.plugins.survivalinvisiframes;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.Iterator;

public class SurvivalInvisiframes extends JavaPlugin implements Listener
{
    private NamespacedKey invisibleRecipe;
    private static NamespacedKey invisibleKey;
    
    @Override
    public void onEnable()
    {
        invisibleRecipe = new NamespacedKey(this, "invisible-recipe");
        invisibleKey = new NamespacedKey(this, "invisible");
        
        ItemStack invisibleItem = generateInvisibleItemFrame();
        invisibleItem.setAmount(8);
        
        ItemStack invisibilityPotion = new ItemStack(Material.LINGERING_POTION);
        PotionMeta meta = (PotionMeta) invisibilityPotion.getItemMeta();
        meta.setBasePotionData(new PotionData(PotionType.INVISIBILITY));
        invisibilityPotion.setItemMeta(meta);
        
        ShapedRecipe invisRecipe = new ShapedRecipe(invisibleRecipe, invisibleItem);
        invisRecipe.shape("FFF", "FPF", "FFF");
        invisRecipe.setIngredient('F', Material.ITEM_FRAME);
        invisRecipe.setIngredient('P', new RecipeChoice.ExactChoice(invisibilityPotion));
        Bukkit.addRecipe(invisRecipe);
        
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    @Override
    public void onDisable()
    {
        // Remove added recipes on plugin disable
        Iterator<Recipe> iter = getServer().recipeIterator();
        while(iter.hasNext())
        {
            Recipe check = iter.next();
            if(check instanceof ShapedRecipe)
            {
                if(invisibleRecipe.equals(((ShapedRecipe) check).getKey()))
                {
                    getLogger().info("Removed recipe");
                    iter.remove();
                }
            }
        }
    }
    
    public static ItemStack generateInvisibleItemFrame()
    {
        ItemStack item = new ItemStack(Material.ITEM_FRAME, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + "Invisible Item Frame");
        meta.getPersistentDataContainer().set(invisibleKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }
    
    @EventHandler(ignoreCancelled = true)
    private void onHangingPlace(HangingPlaceEvent event)
    {
        if(event.getEntity().getType() != EntityType.ITEM_FRAME || event.getPlayer() == null)
        {
            return;
        }
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
        
        if(frame.getItemMeta().getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE))
        {
            ItemFrame itemFrame = (ItemFrame) event.getEntity();
            NBTEditor.set(itemFrame, (byte) 1, "Invisible");
        }
    }
}
