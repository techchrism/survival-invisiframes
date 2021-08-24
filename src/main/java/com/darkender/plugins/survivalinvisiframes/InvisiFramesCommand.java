package com.darkender.plugins.survivalinvisiframes;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InvisiFramesCommand implements CommandExecutor, TabCompleter
{
    private SurvivalInvisiframes survivalInvisiframes;
    
    public InvisiFramesCommand(SurvivalInvisiframes survivalInvisiframes)
    {
        this.survivalInvisiframes = survivalInvisiframes;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(args.length == 0 || args[0].equalsIgnoreCase("get"))
        {
            giveItem(sender);
            return true;
        }
        else if(args[0].equalsIgnoreCase("reload"))
        {
            if(!sender.hasPermission("survivalinvisiframes.reload"))
            {
                sendNoPermissionMessage(sender);
                return true;
            }
            survivalInvisiframes.reload();
            sender.sendMessage(ChatColor.GREEN + "Reloaded!");
            return true;
        }
        else if(args[0].equalsIgnoreCase("force-recheck"))
        {
            if(!sender.hasPermission("survivalinvisiframes.forcerecheck"))
            {
                sendNoPermissionMessage(sender);
                return true;
            }
            survivalInvisiframes.forceRecheck();
            sender.sendMessage(ChatColor.GREEN + "Rechecked invisible item frames");
            return true;
        }
        else if(args[0].equalsIgnoreCase("additem") || args[0].equalsIgnoreCase("removeitem"))
        {
            if(!sender.hasPermission("survivalinvisiframes.setitem"))
            {
                sendNoPermissionMessage(sender);
                return true;
            }
            if(!(sender instanceof Player))
            {
                sender.sendMessage(ChatColor.RED + "Sorry, you must be a player to use this command!");
                return true;
            }
            ItemStack item = ((Player) sender).getInventory().getItemInMainHand();
            if(args[0].equalsIgnoreCase("additem"))
            {
                if (survivalInvisiframes.getRecipeCenterItems().contains(item))
                {
                    sender.sendMessage(ChatColor.RED + "That item is already added!");
                    return true;
                }
                survivalInvisiframes.addRecipeItem(item);
            }
            else if(args[0].equalsIgnoreCase("removeitem"))
            {
                if (!survivalInvisiframes.getRecipeCenterItems().contains(item))
                {
                    sender.sendMessage(ChatColor.RED + "That item is not already added!");
                    return true;
                }
                survivalInvisiframes.removeRecipeItem(item);
            }
            sender.sendMessage(ChatColor.GREEN + "Recipe items updated!");
            return true;
        }
        return false;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        if(args.length != 1)
        {
            return Collections.emptyList();
        }
        List<String> options = new ArrayList<>();
        if(sender.hasPermission("survivalinvisiframes.get"))
        {
            options.add("get");
        }
        if(sender.hasPermission("survivalinvisiframes.reload"))
        {
            options.add("reload");
        }
        if(sender.hasPermission("survivalinvisiframes.forcerecheck"))
        {
            options.add("force-recheck");
        }
        if(sender.hasPermission("survivalinvisiframes.setitem"))
        {
            options.add("additem");
            options.add("removeitem");
        }
        List<String> completions = new ArrayList<>();
        StringUtil.copyPartialMatches(args[0], options, completions);
        Collections.sort(completions);
        return completions;
    }
    
    private void sendNoPermissionMessage(CommandSender sender)
    {
        sender.sendMessage(ChatColor.RED + "Sorry, you don't have permission to run this command");
    }
    
    private void giveItem(CommandSender sender)
    {
        if(!sender.hasPermission("survivalinvisiframes.get"))
        {
            sendNoPermissionMessage(sender);
            return;
        }
        if(!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "Sorry, you must be a player to use this command!");
            return;
        }
        
        Player player = (Player) sender;
        player.getInventory().addItem(SurvivalInvisiframes.generateInvisibleItemFrame());
        player.sendMessage(ChatColor.GREEN + "Added an invisible item frame to your inventory");
    }
}
