package com.github.CubieX.ChestShopInformer;

import java.util.Calendar;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class ChestShopInformer extends JavaPlugin
{
   private CSIconfigHandler cHandler = null;
   private CSIentityListener eListener = null;   
   private CSIcommandHandler comHandler = null;
   private WorldEditPlugin weInst = null;

   public static final Logger log = Logger.getLogger("Minecraft");
   static final String logPrefix = "[ChestShopInformer] "; // Prefix to go in front of all log entries
   static boolean debug = false;
   public static final int MAX_SCAN_DISTANCE_X = 316; // Hard limit for performance reasons. Config values must be within these values.
   public static final int MAX_SCAN_DISTANCE_Z = 316;

   //************************************************
   static String usedConfigVersion = "1"; // Update this every time the config file version changes, so the plugin knows, if there is a suiting config present
   //************************************************

   static int maxScanDistanceX = 32; // these values are checked on sign creation and can be set in the config file. 
   static int maxScanDistanceZ = 32;

   @Override
   public void onEnable()
   {
      cHandler = new CSIconfigHandler(this);

      if(!checkConfigFileVersion())
      {
         log.severe(logPrefix + "Outdated or corrupted config file. Please delete your current config file, so Assignment can create a new one!");
         log.severe(logPrefix + "will be disabled now. Config file is outdated or corrupted.");
         disablePlugin();
         return;
      }

      if (!hookToPermissionSystem())
      {
         log.info(String.format("[%s] - Disabled due to no superperms compatible permission system found!", getDescription().getName()));
         getServer().getPluginManager().disablePlugin(this);
         return;
      }

      if(null != getServer().getPluginManager().getPlugin("WorldEdit"))
      {
         weInst = (WorldEditPlugin)getServer().getPluginManager().getPlugin("WorldEdit");         
      }

      comHandler = new CSIcommandHandler(this, cHandler);
      getCommand("csi").setExecutor(comHandler);      
      eListener = new CSIentityListener(this, weInst);  

      readConfigValues();

      log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is enabled!");
   }

   private boolean hookToPermissionSystem()
   {
      if ((getServer().getPluginManager().getPlugin("PermissionsEx") == null) &&
            (getServer().getPluginManager().getPlugin("bPermissions") == null) &&
            (getServer().getPluginManager().getPlugin("zPermissions") == null) &&
            (getServer().getPluginManager().getPlugin("PermissionsBukkit") == null))
      {
         return false;
      }
      else
      {
         return true;
      }
   }

   private boolean checkConfigFileVersion()
   {
      boolean res = false;

      if(this.getConfig().isSet("config_version"))
      {
         String configVersion = this.getConfig().getString("config_version");

         if(configVersion.equals(usedConfigVersion))
         {
            res = true;
         }
      }

      return (res);
   }

   public void readConfigValues()
   {
      boolean exceed = false;

      debug = this.getConfig().getBoolean("debug");

      maxScanDistanceX = this.getConfig().getInt("maxScanDistanceX");
      if(ChestShopInformer.maxScanDistanceX > ChestShopInformer.MAX_SCAN_DISTANCE_X) { maxScanDistanceX = ChestShopInformer.MAX_SCAN_DISTANCE_X; exceed = true; }
      if(ChestShopInformer.maxScanDistanceX < 1) { maxScanDistanceX = 1; exceed = true; }

      maxScanDistanceZ = this.getConfig().getInt("maxScanDistanceZ");
      if(ChestShopInformer.maxScanDistanceZ > ChestShopInformer.MAX_SCAN_DISTANCE_Z) { maxScanDistanceZ = ChestShopInformer.MAX_SCAN_DISTANCE_Z; exceed = true; }
      if(ChestShopInformer.maxScanDistanceZ < 1) { maxScanDistanceZ = 1; exceed = true; }

      if(exceed)
      {
         log.warning(logPrefix + "A config value is out of it's allowed range! Please check config file.");
      }
   }

   void disablePlugin()
   {
      getServer().getPluginManager().disablePlugin(this);        
   }

   @Override
   public void onDisable()
   {
      getServer().getScheduler().cancelTasks(this);
      eListener = null;
      cHandler = null;     
      comHandler = null;     
      log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is disabled!");
   }

   // ==============================================================================

   void scanForShops (Player player, String world, int startXcoord, int endXcoord, int yCoord, int startZcoord, int endZCoord)
   {
      Sign sign = null;
      String statisticEmptyShops = ChatColor.YELLOW + "------------------------------------------------\n" +
            "Folgende Shops von dir auf dieser Ebene sind leer:\n" + 
            "------------------------------------------------" + ChatColor.WHITE + "\n";
      String statisticInvalidSigns = ChatColor.RED + "------------------------------------------------\n" +
            "Folgende Shops von dir auf dieser Ebene sind \n" + 
            "falsch angelegt (Schild) und damit ungeschuetzt:\n" + 
            "------------------------------------------------" + ChatColor.WHITE + "\n";
      int emptyChests = 0;
      int invalidShopSigns = 0;

      // iterate through all blocks in Y-height layer and search for ChestShop-Signs of the player
      for(int xC = startXcoord; xC <= endXcoord; xC++)
      {
         for(int zC = startZcoord; zC <= endZCoord; zC++)
         {
            if(this.getServer().getWorld(world).getBlockTypeIdAt(xC, yCoord, zC) == 63 ||
                  this.getServer().getWorld(world).getBlockTypeIdAt(xC, yCoord, zC) == 68) // is block a sign on a block (68) or a signpost (63)?
            {
               sign = (Sign) this.getServer().getWorld(world).getBlockAt(xC, yCoord, zC).getState();

               // check if sign is a ChestShop sign by reading the first line and match it with players name
               if(sign.getLine(0).equalsIgnoreCase(player.getName()))
               {
                  // this sign seems to be a shop of the asking player, so scan for chest below it
                  Block chestBlock = this.getServer().getWorld(world).getBlockAt(sign.getX(), sign.getY()-1, sign.getZ());

                  if(chestBlock.getState() instanceof Chest || chestBlock.getState() instanceof DoubleChest)
                  {
                     // a chest was found below the sign. Now check if the sign is correctly formatted and thus a valid ChestShop sign                     
                     if(shopSignIsValid(sign))
                     {
                        Chest chest = ((Chest) chestBlock.getState());
                        boolean empty = true;

                        // look through chest inventory to see if its empty or not
                        for (ItemStack stack : chest.getBlockInventory().getContents())
                        {
                           if (null != stack)
                           {
                              empty = false;
                              // TODO count items here to show it to the player (in later version...)

                              break; // delete this, if item count should be made!
                           }
                        }

                        if(empty)
                        {
                           emptyChests++;
                           // chest is empty, so add it to report
                           statisticEmptyShops = statisticEmptyShops + emptyChests + ". " + chestBlock.getX() + ":" + chestBlock.getY() + ":" + chestBlock.getZ() + " " + sign.getLine(3) + "\n";
                        }
                     }
                     else
                     {
                        invalidShopSigns++;
                        // this shop sign is not correctly formatted, thus the shop is not working and unprotected
                        statisticInvalidSigns = statisticInvalidSigns + invalidShopSigns + ". " + chestBlock.getX() + ":" + chestBlock.getY() + ":" + chestBlock.getZ() + " " + sign.getLine(3) + "\n";
                     }
                  }
               }
            } // end if is sign
         } // end for xZ
      } // end for xC

      if(0 == emptyChests)
      {
         statisticEmptyShops = ChatColor.YELLOW + "Es wurden keine leeren Shops von dir \n" +
         		"im Suchbereich gefunden!";
      }

      player.sendMessage(statisticEmptyShops);

      if(0 < invalidShopSigns)
      {
         player.sendMessage(statisticInvalidSigns);
      }
   } // end method

   boolean shopSignIsValid (Sign sign)
   {
      boolean valid = false;
      String line = "";
      String[] lineArrayA;

      if(null != sign)
      {
         try
         {
            // parse second line to check if the value is a positive value            
            if(isPosNumber(sign.getLine(1)))
            {
               // parse third line to check if it has valid format "B PRICE" or ":S PRICE" or "B PRICE:PRICE S"
               line = sign.getLine(2);
               line = line.replace(":", " "); // this makes further parsing easier. Correct line may now be "B XX" or " YY S" or "B PRICE PRICE S"

               lineArrayA = line.split(" "); // splits at first space found. If text is " YY S", the leading space will cause a empty string to be written to lineArray[0].

               if(2 == lineArrayA.length) // may be [0]=B and [1]=XX
               {
                  if((lineArrayA[0].equals("B")) && (isPosNumber(lineArrayA[1]))) // on Sign: "B XX"
                  {
                     if(0 < sign.getLine(3).length()) // ItemID is present
                     {
                        valid = true; // seems to be a valid ChestShop sign
                     }
                  }
               }

               if(3 == lineArrayA.length) // may be [0]=" " and [1]=YY and [2]=S
               {
                  if((lineArrayA[0].equals("")) && (isPosNumber(lineArrayA[1])) && (lineArrayA[2].equals("S"))) // on Sign: ":YY S"
                  {
                     if(0 < sign.getLine(3).length()) // ItemID is present
                     {
                        valid = true; // seems to be a valid ChestShop sign
                     }
                  }
               }

               if (4 == lineArrayA.length) // may be format "B XX:YY S" (in line string it's then "B XX YY S")
               {
                  if((lineArrayA[0].equals("B")) &&
                        (isPosNumber(lineArrayA[1])) &&
                        (isPosNumber(lineArrayA[2])) &&
                        (lineArrayA[3].equals("S")))
                  {
                     if(0 < sign.getLine(3).length()) // ItemID is present
                     {
                        valid = true;
                     }
                  }
               }
            }
         }
         catch (Exception ex)
         {
            // something went wrong while parsing the sign. So it seems not to be a valid ChestShop sign
         }
      }

      return (valid);
   }

   public boolean isPosNumber(String input)  
   {
      try  
      {
         if(0 < Float.parseFloat(input))
         {
            return true;
         }
         else
         {
            return false;
         }
      }  
      catch( Exception ex)  
      {  
         return false;  
      }
   }  

   public long getCurrTimeInMillis()
   {
      return (((Calendar)Calendar.getInstance()).getTimeInMillis());
   }

}
