package com.github.CubieX.ChestShopInformer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CSIcommandHandler implements CommandExecutor
{
   private final ChestShopInformer plugin;
   private final CSIconfigHandler cHandler;

   public CSIcommandHandler(ChestShopInformer plugin, CSIconfigHandler cHandler)
   {
       this.plugin = plugin;
       this.cHandler = cHandler;
   }

   @Override
   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
   {
       Player player = null;
       if (sender instanceof Player) 
       {
           player = (Player) sender;
       }

       if (cmd.getName().equalsIgnoreCase("csi"))
       {
           if (args.length == 0)
           { //no arguments, so help will be displayed
               return false;
           }
           if (args.length==1)
           {
               if (args[0].equalsIgnoreCase("version")) // show the current version of the plugin
               {            
                   sender.sendMessage(ChatColor.GREEN + "This server is running " + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion());
               }    

               if (args[0].equalsIgnoreCase("reload")) // reload the plugins config and playerfile
               {            
                   if(sender.hasPermission("chestshopinformer.admin"))
                   {
                       cHandler.reloadConfig(sender);
                   }
                   else
                   {
                       sender.sendMessage(ChatColor.RED + "You do not have sufficient permission to reload " + plugin.getDescription().getName() + "!");
                   }
               }               
               return true;
           }             
           else
           {
               sender.sendMessage(ChatColor.YELLOW + ChestShopInformer.logPrefix + "Falsche Anzahl an Parametern.");
           }  
       }         
       return false; // No valid parameter count. If false is returned, the help for the command stated in the plugin.yml will be displayed to the player
   }
}
