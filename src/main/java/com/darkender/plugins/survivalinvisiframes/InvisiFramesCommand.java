package com.darkender.plugins.survivalinvisiframes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

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
        else if(args[0].equalsIgnoreCase("setitem"))
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
            survivalInvisiframes.setRecipeItem(item);
            sender.sendMessage(ChatColor.GREEN + "Recipe item updated!");
            return true;
        }
        else if (args[0].equalsIgnoreCase("give"))
        {
            if(!sender.hasPermission("survivalinvisiframes.give"))
            {
                sendNoPermissionMessage(sender);
                return true;
            }
            if(args.length < 2)
            {
                sender.sendMessage(ChatColor.RED + "Usage: /invisiframes give <player> <amount>");
                return true;
            }
            Player player = survivalInvisiframes.getServer().getPlayer(args[1]);
            if(player == null)
            {
                sender.sendMessage(ChatColor.RED + "Sorry, that player is not online!");
                return true;
            }
            int amount = 1;
            if(args.length >= 3)
            {
                try
                {
                    amount = Integer.parseInt(args[2]);
                }
                catch(NumberFormatException e)
                {
                    sender.sendMessage(ChatColor.RED + "Sorry, that is not a valid number!");
                    return true;
                }
            }
            player.getInventory().addItem(SurvivalInvisiframes.generateInvisibleItemFrame(amount));
            // player.sendMessage(ChatColor.GREEN + "Added an invisible item frame to your inventory");
            // sender.sendMessage(ChatColor.GREEN + "Added an invisible item frame to " + player.getName() + "'s inventory");
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
            options.add("setitem");
        }
        if(sender.hasPermission("survivalinvisiframes.give"))
        {
            options.add("give");
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
