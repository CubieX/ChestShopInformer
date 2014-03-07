package com.github.CubieX.ChestShopInformer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class CSIentityListener implements Listener
{
   private final ChestShopInformer plugin;
   private WorldEditPlugin weInst;
   private long lastScanTimestamp = 0;
   private final static int LAST_SCAN_TIMESTAMP = 5000; // this defines the maximum scan interval in milliseconds (only use full thousands!)

   //Constructor
   public CSIentityListener(ChestShopInformer plugin, WorldEditPlugin weInst)
   {
      this.plugin = plugin;
      this.weInst = weInst;

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
            int firstZcoord = 0;   
            int secondXcoord = 0;            
            int secondZcoord = 0;

            boolean parsingXok = false;            
            boolean parsingZok = false;

            Selection weSel = null;

            try //correct format and valid item/block?
            {
               if(ChestShopInformer.debug){ChestShopInformer.log.info("Ist das Sign korrekt geschrieben?");}

               if(null != weInst)
               {
                  if(null != weInst.getSelection(player))
                  {
                     weSel = weInst.getSelection(player);
                  }
               }

               if((null != weSel) &&
                     (event.getLine(1).equalsIgnoreCase("we")))
               {
                  // Get coords from active WE selection, if player wrote "we" in line 1 and write them on the sign in line 1 and 2.
                  firstXcoord = (int)weSel.getMinimumPoint().getX();                 
                  firstZcoord = (int)weSel.getMinimumPoint().getZ();

                  secondXcoord = (int)weSel.getMaximumPoint().getX();                  
                  secondZcoord = (int)weSel.getMaximumPoint().getZ();

                  event.setLine(1, String.valueOf(firstXcoord) + ":" + String.valueOf(firstZcoord));
                  event.setLine(2, String.valueOf(secondXcoord) + ":" + String.valueOf(secondZcoord));

                  // END Get coords from active WE selection.
               }
               else
               {
                  // Get coords directly from sign text (no active WE selection or player does not want to use it) ==================
                  lineArray = (event.getLine(1).split(":"));   //parse firstXcoord:firstYcoord:firstZcoord
                  if(ChestShopInformer.debug){ChestShopInformer.log.info("ArrayLength: " + String.valueOf(lineArray.length));}

                  if(lineArray.length == 2)
                  {
                     firstXcoord = Integer.parseInt(lineArray[0]);                     
                     firstZcoord = Integer.parseInt(lineArray[1]);

                     if(ChestShopInformer.debug){ChestShopInformer.log.info("firstXcoord: " + String.valueOf(firstXcoord) + ", firstZcoord: " + String.valueOf(firstZcoord));}                 
                  }
                  else
                  {
                     throw new Exception(ChatColor.YELLOW + "Ungueltige Eingaben!");
                  }

                  lineArray = (event.getLine(2).split(":"));   //parse secondXcoord:secondZcoord
                  if(ChestShopInformer.debug){ChestShopInformer.log.info("ArrayLength: " + String.valueOf(lineArray.length));}

                  if(lineArray.length == 2)
                  {
                     secondXcoord = Integer.parseInt(lineArray[0]);                     
                     secondZcoord = Integer.parseInt(lineArray[1]);

                     if(ChestShopInformer.debug){ChestShopInformer.log.info("secondXcoord: " + String.valueOf(secondXcoord) + ", secondZcoord: " + String.valueOf(secondZcoord));}                 
                  }
                  else
                  {
                     throw new Exception(ChatColor.YELLOW + "Ungueltige Eingaben!");
                  }

                  // END Get coords from sign text ===========================================
               }

               // check which X coord value is bigger and calculate difference accordingly
               if(firstXcoord >= secondXcoord)
               {
                  if((firstXcoord - secondXcoord) <= ChestShopInformer.maxScanDistanceX)
                  {
                     parsingXok = true;
                  }
                  else
                  {
                     throw new Exception(ChatColor.YELLOW + "X-Distanz zu Gross! Maximal erlaubt: " + ChestShopInformer.maxScanDistanceX);
                  }
               }
               else
               {
                  if((secondXcoord - firstXcoord) <= ChestShopInformer.maxScanDistanceX)
                  {
                     parsingXok = true;
                  }
                  else
                  {
                     throw new Exception(ChatColor.YELLOW + "X-Distanz zu Gross! Maximal erlaubt: " + ChestShopInformer.maxScanDistanceX);
                  }
               }

               // check which Z coord value is bigger and calculate difference accordingly
               if(firstZcoord >= secondZcoord)
               {
                  if((firstZcoord - secondZcoord) <= ChestShopInformer.maxScanDistanceZ)
                  {
                     parsingZok = true;
                  }
                  else
                  {
                     throw new Exception(ChatColor.YELLOW + "Z-Distanz zu Gross! Maximal erlaubt: " + ChestShopInformer.maxScanDistanceZ);
                  }
               }
               else
               {
                  if((secondZcoord - firstZcoord) <= ChestShopInformer.maxScanDistanceZ)
                  {
                     parsingZok = true;
                  }
                  else
                  {
                     throw new Exception(ChatColor.YELLOW + "Z-Distanz zu Gross! Maximal erlaubt: " + ChestShopInformer.maxScanDistanceZ);
                  }
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
               player.sendMessage(ChatColor.YELLOW + "Hilfe: 1.Zeile: <CS-Informer> 2.Zeile: x1:z1 3.Zeile: x2:z2\n");             

               return; //leave method
            }

            if(parsingXok && parsingZok)
            {
               event.setLine(0,  "<CS-Informer>");
               event.setLine(3, "Shops abfragen");
               player.sendMessage(ChatColor.GREEN + "ChestShopInformer-Schild erstellt!");
            }
            else
            {
               //not a correctly formatted sign. Abort.                
               event.getBlock().breakNaturally();
               player.sendMessage(ChatColor.YELLOW + "Hilfe: 1.Zeile: <CS-Informer> 2.Zeile: x1:z1 3.Zeile: x2:z2\n");
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
      int firstZcoord = 0;   
      int secondXcoord = 0;      
      int secondZcoord = 0;
      int firstChunkXcoord = 0;
      int firstChunkZcoord = 0;
      int secondChunkXcoord = 0;
      int secondChunkZcoord = 0;

      if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
      {
         if(event.getClickedBlock().getType() == Material.WALL_SIGN ||
               event.getClickedBlock().getType() == Material.SIGN_POST)
         {
            sign = (Sign) event.getClickedBlock().getState();
            final Sign signToUpdate = (Sign) event.getClickedBlock().getState();

            if(sign.getLine(0).equalsIgnoreCase("<CS-Informer>")) // is CSI sign?
            {
               if(event.getPlayer().hasPermission("chestshopinformer.use"))
               {
                  // this blocks exploiting the sign by a player by setting a minimum wait time until scan is ready again
                  if((plugin.getCurrTimeInMillis() - lastScanTimestamp) > CSIentityListener.LAST_SCAN_TIMESTAMP)
                  {
                     lastScanTimestamp = plugin.getCurrTimeInMillis();                     
                     sign.setLine(3, "bitte warten..");
                     sign.update();                     

                     try
                     {
                        lineArray = (sign.getLine(1).split(":"));   //parse firstXcoord:firstYcoord:firstZcoord

                        firstXcoord = Integer.parseInt(lineArray[0]);                        
                        firstZcoord = Integer.parseInt(lineArray[1]);
                        firstChunkXcoord = event.getPlayer().getWorld().getBlockAt(firstXcoord, 0, firstZcoord).getChunk().getX();
                        firstChunkZcoord = event.getPlayer().getWorld().getBlockAt(firstXcoord, 0, firstZcoord).getChunk().getZ();

                        if(ChestShopInformer.debug){ChestShopInformer.log.info("firstXcoord: " + String.valueOf(firstXcoord) + ", firstZcoord: " + String.valueOf(firstZcoord));}

                        lineArray = (sign.getLine(2).split(":"));   //parse secondXcoord:secondYcoord:secondZcoord

                        secondXcoord = Integer.parseInt(lineArray[0]);                     
                        secondZcoord = Integer.parseInt(lineArray[1]);
                        secondChunkXcoord = event.getPlayer().getWorld().getBlockAt(secondXcoord, 0, secondZcoord).getChunk().getX();
                        secondChunkZcoord = event.getPlayer().getWorld().getBlockAt(secondXcoord, 0, secondZcoord).getChunk().getZ();
                        
                        if(ChestShopInformer.debug){ChestShopInformer.log.info("secondXcoord: " + String.valueOf(secondXcoord) + ", secondZcoord: " + String.valueOf(secondZcoord));}

                        // TODO scan shops here within given area and send message to askingPlayer!
                        // Scan all signs for ChestShop-Signs of the player, then scan chests directly below them
                        // TODO DO THIS ASYNCHRONOUSLY IF POSSIBLE....

                              if(firstChunkXcoord <= secondChunkXcoord)
                              {
                                 if(firstChunkZcoord <= secondChunkZcoord)
                                 {
                                    plugin.scanForShops(askingPlayer, askingPlayer.getWorld().getName(), firstChunkXcoord, secondChunkXcoord, firstChunkZcoord, secondChunkZcoord);
                                 }
                                 else
                                 {
                                    plugin.scanForShops(askingPlayer, askingPlayer.getWorld().getName(), firstChunkXcoord, secondChunkXcoord, secondChunkZcoord, firstChunkZcoord);
                                 }                        
                              }
                              else
                              {
                                 if(firstZcoord <= secondZcoord)
                                 {
                                    plugin.scanForShops(askingPlayer, askingPlayer.getWorld().getName(), secondChunkXcoord, firstChunkXcoord, firstChunkZcoord, secondChunkZcoord);
                                 }
                                 else
                                 {
                                    plugin.scanForShops(askingPlayer, askingPlayer.getWorld().getName(), secondChunkXcoord, firstChunkXcoord, secondChunkZcoord, firstChunkZcoord);
                                 }
                              }

                        // askingPlayer.sendMessage(ChatColor.GREEN + "Shop-Statstik fuer dich:\n" +
                        // "1. Eisenbloecke: 134 uebrig\n" +
                        // "2. Eisenbloecke: 54 uebrig");

                        // reset sign text after waiting time has passed =====================
                        Bukkit.getServer().getScheduler().runTaskLater(plugin, new Runnable()
                        {
                           public void run()
                           {
                              if(null != signToUpdate)
                              {
                                 signToUpdate.setLine(3, "Shops abfragen");
                                 signToUpdate.update();
                              }
                           }

                        }, 20*(CSIentityListener.LAST_SCAN_TIMESTAMP / 1000));
                        // ===================================================================
                     }
                     catch(Exception e)
                     {
                        if(ChestShopInformer.debug){ChestShopInformer.log.info("This is no valid ChestShopInformer sign! " + e.getMessage());}
                        event.getPlayer().sendMessage(ChatColor.RED + "Dies ist kein gueltiges ChestShopInformer-Schild!");
                     }
                  }
                  else
                  {                     
                     event.getPlayer().sendMessage(ChatColor.YELLOW + "Abfrage ist nur alle 5 Sekunden moeglich!");                                          
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
