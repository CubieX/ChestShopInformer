package com.github.CubieX.ChestShopInformer;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class CSIentityListener implements Listener
{
   private final ChestShopInformer plugin;

   //Constructor
   public CSIentityListener(ChestShopInformer plugin)
   {
      this.plugin = plugin;

      plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }
   /*
    Event Priorities

    There are six priorities in Bukkit

    EventPriority.HIGHEST
    EventPriority.HIGH
    EventPriority.NORMAL
    EventPriority.LOW
    EventPriority.LOWEST
    EventPriority.MONITOR 

    They are called in the following order

    EventPriority.LOWEST 
    EventPriority.LOW
    EventPriority.NORMAL
    EventPriority.HIGH
    EventPriority.HIGHEST
    EventPriority.MONITOR 

    All Events can be cancelled. Plugins with a high prio for the event can cancel or uncancel earlier issued lower prio plugin actions.
    MONITOR level should only be used, if the outcome of an event is NOT altered from this plugin and if you want to have the final state of the event.
    If the outcome gets changed (i.e. event gets cancelled, uncancelled or actions taken that can lead to it), a prio from LOWEST to HIGHEST must be used!

    The option "ignoreCancelled" if set to "true" says, that the plugin will not get this event if it has been cancelled beforehand from another plugin.
    */

   //================================================================================================
   @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
   public void onSignChange(SignChangeEvent event) //For the Creator. Fires AFTER Sign Creation
   {      
      if(ChestShopInformer.debug)
      {
         ChestShopInformer.log.info("im onSignChangeEvent");
         event.getPlayer().sendMessage("Linie 0:" + event.getLine(0)); //debug
      }   

      if(event.getLine(0).equalsIgnoreCase("<CS-Informer>")) // CSI sign?
      {   
         if(ChestShopInformer.debug){ChestShopInformer.log.info("CSI-Sign erkannt");}

         if(event.getPlayer().hasPermission("chestshopinformer.create") || event.getPlayer().hasPermission("chestshopinformer.admin"))
         {
            Player player = event.getPlayer();            
            String[] lineArray;
            int firstXcoord = 0;
            int firstYcoord = 0;
            int firstZcoord = 0;   
            int secondXcoord = 0;
            int secondYcoord = 0; // this must be the same value than firstYcoord!
            int secondZcoord = 0;

            boolean parsingXok = false;
            boolean parsingYok = false;
            boolean parsingZok = false;

            try //correct format and valid item/block?
            {
               if(ChestShopInformer.debug){ChestShopInformer.log.info("Ist das Sign korrekt geschrieben?");}

               lineArray = (event.getLine(1).split(":"));   //parse firstXcoord:firstYcoord:firstZcoord
               if(ChestShopInformer.debug){ChestShopInformer.log.info("ArrayLength: " + String.valueOf(lineArray.length));}

               if(lineArray.length == 3)
               {
                  firstXcoord = Integer.parseInt(lineArray[0]);
                  firstYcoord = Integer.parseInt(lineArray[1]);
                  firstZcoord = Integer.parseInt(lineArray[2]);

                  if(ChestShopInformer.debug){ChestShopInformer.log.info("firstXcoord: " + String.valueOf(firstXcoord) + ", firstYcoord: " + String.valueOf(firstYcoord) + ", firstZcoord: " + String.valueOf(firstZcoord));}                 
               }
               else
               {
                  throw new Exception(ChatColor.YELLOW + "Ungueltige Eingaben!");
               }

               lineArray = (event.getLine(2).split(":"));   //parse secondXcoord:secondYcoord:secondZcoord
               if(ChestShopInformer.debug){ChestShopInformer.log.info("ArrayLength: " + String.valueOf(lineArray.length));}

               if(lineArray.length == 3)
               {
                  secondXcoord = Integer.parseInt(lineArray[0]);
                  secondYcoord = Integer.parseInt(lineArray[1]);
                  secondZcoord = Integer.parseInt(lineArray[2]);

                  if(ChestShopInformer.debug){ChestShopInformer.log.info("secondXcoord: " + String.valueOf(secondXcoord) + ", secondYcoord: " + String.valueOf(secondYcoord) + ", secondZcoord: " + String.valueOf(secondZcoord));}                 
               }
               else
               {
                  throw new Exception(ChatColor.YELLOW + "Ungueltige Eingaben!");
               }

               // check which X coord value is bigger and calculate difference accordingly
               if(firstXcoord >= secondXcoord)
               {
                  if((firstXcoord - secondXcoord) <= ChestShopInformer.MAX_SCAN_DISTANCE_X)
                  {
                     parsingXok = true;
                  }
               }
               else if (secondXcoord > firstXcoord)
               {
                  if((secondXcoord - firstXcoord) <= ChestShopInformer.MAX_SCAN_DISTANCE_X)
                  {
                     parsingXok = true;
                  }
               }
               else
               {
                  throw new Exception(ChatColor.YELLOW + "X-Distanz zu Gross! Maximal erlaubt: 316");
               }

               // check if Y coord values are identical (only one height level will be scanned for signs!)
               if((firstYcoord == secondYcoord) &&
                     (firstYcoord <= player.getWorld().getMaxHeight()))
               {                  
                  parsingYok = true;
               }
               else
               {
                  throw new Exception(ChatColor.YELLOW + "Y-Werte muessen beide gleich sein und <= 255!");
               }

               // check which Z coord value is bigger and calculate difference accordingly
               if(firstZcoord >= secondZcoord)
               {
                  if((firstZcoord - secondZcoord) <= ChestShopInformer.MAX_SCAN_DISTANCE_Z)
                  {
                     parsingZok = true;
                  }
               }
               else if (secondZcoord > firstZcoord)
               {
                  if((secondZcoord - firstZcoord) <= ChestShopInformer.MAX_SCAN_DISTANCE_Z)
                  {
                     parsingZok = true;
                  }
               }
               else
               {
                  throw new Exception(ChatColor.YELLOW + "Z-Distanz zu Gross! Maximal erlaubt: 316");
               }
            }
            catch (Exception e)
            {
               //not a number. Abort.                
               event.getBlock().breakNaturally();
               if(null != e.getMessage())
               {
                  player.sendMessage(e.getMessage());
               }
               player.sendMessage(ChatColor.YELLOW + "Hilfe: 1.Zeile: <CS-Informer> 2.Zeile: x1:y:z1 3.Zeile: x2:y:z2\n" +
                     "Y-Wert muss die Hoehe sein auf der die Shop-Schilder haengen!");             

               return; //leave method
            }

            if(parsingXok && parsingYok && parsingZok)
            {               
               event.setLine(3, "Shops abfragen");
               player.sendMessage(ChatColor.GREEN + "ChestShopInformer-Schild erstellt!");
            }
            else
            {
               //not a correctly formatted sign. Abort.                
               event.getBlock().breakNaturally();
               player.sendMessage(ChatColor.YELLOW + "Hilfe: 1.Zeile: <CS-Informer> 2.Zeile: x1:y:z1 3.Zeile: x2:y:z2\n" +
               "Y-Wert muss die Hoehe sein auf der die Shop-Schilder haengen!");
            }
         }
         else
         {
            // no permission. Abort.
            event.getBlock().breakNaturally();
            event.getPlayer().sendMessage(ChatColor.RED + "Du hast keine Berechtigung um ChestShopInformer-Schilder aufzustellen!");
         }
      }                
   }

   //================================================================================================
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onPlayerInteract(PlayerInteractEvent event)
   {
      Player askingPlayer = event.getPlayer();
      Sign sign = null; 
      String[] lineArray;
      int firstXcoord = 0;
      int firstYcoord = 0;
      int firstZcoord = 0;   
      int secondXcoord = 0;
      // secondYcoord must be the same value than firstYcoord, so its not needed here
      int secondZcoord = 0;

      if(event.getAction() == Action.LEFT_CLICK_BLOCK)
      {
         if(event.getClickedBlock().getTypeId() == 63 ||
               event.getClickedBlock().getTypeId() == 68) // Left clicked a sign on a block (68) oder a signpost (63)?
         {
            sign = (Sign) event.getClickedBlock().getState();

            if(sign.getLine(0).equalsIgnoreCase("<CS-Informer>")) // is CSI sign?
            {
               if(event.getPlayer().hasPermission("chestshopinformer.use"))
               {                                                      
                  try
                  {
                     lineArray = (sign.getLine(1).split(":"));   //parse firstXcoord:firstYcoord:firstZcoord

                     firstXcoord = Integer.parseInt(lineArray[0]);
                     firstYcoord = Integer.parseInt(lineArray[1]);
                     firstZcoord = Integer.parseInt(lineArray[2]);
                     
                     if(ChestShopInformer.debug){ChestShopInformer.log.info("firstXcoord: " + String.valueOf(firstXcoord) + ", firstYcoord: " + String.valueOf(firstYcoord) + ", firstZcoord: " + String.valueOf(firstZcoord));}

                     lineArray = (sign.getLine(2).split(":"));   //parse secondXcoord:secondYcoord:secondZcoord

                     secondXcoord = Integer.parseInt(lineArray[0]);                     
                     secondZcoord = Integer.parseInt(lineArray[2]);
                     
                     if(ChestShopInformer.debug){ChestShopInformer.log.info("secondXcoord: " + String.valueOf(secondXcoord) + ", secondZcoord: " + String.valueOf(secondZcoord));}

                     // TODO scan shops here within given area and send message to askingPlayer!
                     // Scan all signs for ChestShop-Signs of the player, then scan chests directly below them
                     // TODO DO THIS ASYNCHRONOUSLY IF POSSIBLE....

                     if(firstXcoord <= secondXcoord)
                     {
                        if(firstZcoord <= secondZcoord)
                        {
                           plugin.scanForShops(askingPlayer, askingPlayer.getWorld().getName(), firstXcoord, secondXcoord, firstYcoord, firstZcoord, secondZcoord);
                        }
                        else
                        {
                           plugin.scanForShops(askingPlayer, askingPlayer.getWorld().getName(), firstXcoord, secondXcoord, firstYcoord, secondZcoord, firstZcoord);
                        }                        
                     }
                     else
                     {
                        if(firstZcoord <= secondZcoord)
                        {
                           plugin.scanForShops(askingPlayer, askingPlayer.getWorld().getName(), secondXcoord, firstXcoord, firstYcoord, firstZcoord, secondZcoord);
                        }
                        else
                        {
                           plugin.scanForShops(askingPlayer, askingPlayer.getWorld().getName(), secondXcoord, firstXcoord, firstYcoord, secondZcoord, firstZcoord);
                        }
                     }

                     // askingPlayer.sendMessage(ChatColor.GREEN + "Shop-Statstik fuer dich:\n" +
                     // "1. Eisenbloecke: 134 uebrig\n" +
                     // "2. Eisenbloecke: 54 uebrig");
                  }
                  catch(Exception e)
                  {
                     if(ChestShopInformer.debug){ChestShopInformer.log.info("This is no valid ChestShopInformer sign! " + e.getMessage());}
                     event.getPlayer().sendMessage(ChatColor.RED + "Dies ist kein gueltiges ChestShopInformer-Schild!");
                  }                    
               }
               else
               {
                  event.getPlayer().sendMessage(ChatColor.RED + "Du hast keine Berechtigung um ChestShopInformer-Schilder zu nutzen!");
               }
            }            
         }
      }
   }

   //======================================================================
   /*@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false) // -> priority MUST BE HIGHER to protect the sign
   public void onBlockBreak(BlockBreakEvent event) //For the Assignee
   {
      Player player = event.getPlayer();
      String playerName = player.getName();

      if(null != player)
      {
         if(ChestShopInformer.debug){ChestShopInformer.log.info("im onBlockBreakEvent");}
         if(event.getBlock().getTypeId() == 63 ||
               event.getBlock().getTypeId() == 68) // a sign on a sign on block (68) oder a signpost (63)?
         {
            if(ChestShopInformer.debug){ChestShopInformer.log.info("Sign erkannt");}
            Sign bSign = (Sign)event.getBlock().getState();
            if(ChestShopInformer.debug){ChestShopInformer.log.info("Sign Line 0: " + bSign.getLine(0));}

            if(bSign.getLine(0).equalsIgnoreCase("<CS-Informer>")) // is CSI sign?
            {                          
               if(ChestShopInformer.debug){ChestShopInformer.log.info("ChestShopInformer Sign erkannt");}

               if(!event.getPlayer().hasPermission("chestshopinformer.admin")) //player has permission to break the sign
               {     
                  event.setCancelled(true); //cancel BlockBreak if its a registered Assignment sign and it's not the assigner or an OP who tries to break it
                  event.getPlayer().sendMessage(ChatColor.RED + "Du hast keine Berechtigung um dieses ChestShopInformer-Schild abzureissen!");
               }
            }
         }
      }
   }*/
}
