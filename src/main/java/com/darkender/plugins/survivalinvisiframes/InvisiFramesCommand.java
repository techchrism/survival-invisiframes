package com.darkender.plugins.survivalinvisiframes;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class InvisiFramesCommand implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(!(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "Sorry, you must be a player to use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        player.getInventory().addItem(SurvivalInvisiframes.generateInvisibleItemFrame());
        player.sendMessage(ChatColor.GREEN + "Added an invisible item frame to your inventory");
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        return Collections.emptyList();
    }
}
