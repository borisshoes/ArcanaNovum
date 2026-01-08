package net.borisshoes.arcananovum;

import eu.pb4.sgui.api.elements.BookElementBuilder;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.borisshoes.arcananovum.callbacks.*;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.datastorage.AnchorData;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.gui.VirtualInventoryGui;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.config.ConfigManager;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.borislib.datastorage.DefaultPlayerData;
import net.borisshoes.borislib.utils.ItemModDataHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.ACTIVE_ANCHORS;

public class ArcanaNovum implements ModInitializer, ClientModInitializer {
   
   private static final Logger LOGGER = LogManager.getLogger("Arcana Novum");
   public static final HashMap<ServerLevel, Long2IntOpenHashMap> ANCHOR_CHUNKS = new HashMap<>();
   public static final HashMap<Tuple<BlockEntity, ArcanaBlockEntity>,Integer> ACTIVE_ARCANA_BLOCKS = new HashMap<>();
   public static final List<UUID> TOTEM_KILL_LIST = new ArrayList<>();
   public static final HashMap<VirtualInventoryGui<?>, ServerPlayer> VIRTUAL_INVENTORY_GUIS = new HashMap<>();
   public static MinecraftServer SERVER = null;
   public static final boolean DEV_MODE = true;
   private static final String CONFIG_NAME = "ArcanaNovum.properties";
   public static final String MOD_ID = "arcananovum";
   public static final String BLANK_UUID = "00000000-0000-4000-8000-000000000000";
   public static final ItemModDataHandler ITEM_DATA = new ItemModDataHandler(MOD_ID);
   public static ConfigManager CONFIG;
   public static int DEBUG_VALUE = 0;
   
   @Override
   public void onInitialize(){
      CONFIG = new ConfigManager(MOD_ID,"Arcana Novum",CONFIG_NAME,ArcanaRegistry.CONFIG_SETTINGS);
      ArcanaRegistry.initialize();
      
      ServerTickEvents.END_WORLD_TICK.register(WorldTickCallback::onWorldTick);
      ServerTickEvents.END_SERVER_TICK.register(TickCallback::onTick);
      UseEntityCallback.EVENT.register(EntityUseCallback::useEntity);
      AttackBlockCallback.EVENT.register(BlockAttackCallback::attackBlock);
      PlayerBlockBreakEvents.BEFORE.register(BlockBreakCallback::breakBlock);
      AttackEntityCallback.EVENT.register(EntityAttackCallback::attackEntity);
      ServerPlayConnectionEvents.JOIN.register(PlayerConnectionCallback::onPlayerJoin);
      ServerPlayConnectionEvents.DISCONNECT.register(PlayerConnectionCallback::onPlayerLeave);
      CommandRegistrationCallback.EVENT.register(CommandRegisterCallback::registerCommands);
      ServerEntityEvents.ENTITY_LOAD.register(EntityLoadCallbacks::loadEntity);
      ServerEntityEvents.ENTITY_UNLOAD.register(EntityLoadCallbacks::unloadEntity);
      ServerPlayerEvents.AFTER_RESPAWN.register(PlayerDeathCallback::afterRespawn);
      ServerPlayerEvents.COPY_FROM.register(PlayerDeathCallback::onPlayerCopy);
      ServerLifecycleEvents.SERVER_STARTING.register(ServerStartingCallback::serverStarting);
      ServerLifecycleEvents.SERVER_STARTED.register(ServerStartedCallback::serverStarted);
      
      LOGGER.info("Arcana Surges Through The World!");
   }
   
   @Override
   public void onInitializeClient(){
      LOGGER.info("Arcana Surges Through Your Client!");
   }
   
   public static boolean addActiveAnchor(ServerLevel world, BlockPos pos){
      return DataAccess.getWorld(world.dimension(), AnchorData.KEY).addAnchor(pos);
   }
   
   public static boolean removeActiveAnchor(ServerLevel world, BlockPos pos){
      return DataAccess.getWorld(world.dimension(), AnchorData.KEY).removeAnchor(pos);
   }
   
   public static boolean addActiveBlock(Tuple<BlockEntity,ArcanaBlockEntity> pair){
      List<Tuple<BlockEntity,ArcanaBlockEntity>> existing = ACTIVE_ARCANA_BLOCKS.keySet().stream().filter(p -> p.getB().getUuid().equals(pair.getB().getUuid())).toList();
      existing.forEach(ACTIVE_ARCANA_BLOCKS::remove);
      ACTIVE_ARCANA_BLOCKS.put(pair,30);
      return existing.isEmpty();
   }
   
   public static ArcanaPlayerData data(UUID player){
      if(player == null){
         return null;
      }
      try{
         return DataAccess.getPlayer(player,ArcanaPlayerData.KEY);
      }catch(Exception e){
         DefaultPlayerData defaultPlayerData = DataAccess.getPlayer(player, BorisLib.PLAYER_DATA_KEY);
         String username = defaultPlayerData != null ? defaultPlayerData.getUsername() : "<???>";
         log(3,"Failed to get Arcane Profile for "+username + " ("+player+")");
         log(3,e.toString());
      }
      return null;
   }
   
   public static ArcanaPlayerData data(Player player){
      if(player == null){
         return null;
      }
      return data(player.getUUID());
   }
   
   public static void devPrint(String msg){
      if(DEV_MODE){
         System.out.println(msg);
      }
   }
   
   /**
    * Uses built in logger to log a message
    * @param level 0 - Info | 1 - Warn | 2 - Error | 3 - Fatal | Else - Debug
    * @param msg  The {@code String} to be printed.
    */
   public static void log(int level, String msg){
      switch(level){
         case 0 -> LOGGER.info(msg);
         case 1 -> LOGGER.warn(msg);
         case 2 -> LOGGER.error(msg);
         case 3 -> LOGGER.fatal(msg);
         default -> LOGGER.debug(msg);
      }
   }
   
   public static BookElementBuilder getGuideBook(){
      BookElementBuilder book = new BookElementBuilder();
      List<Component> pages = new ArrayList<>();
      
      pages.add(Component.literal("       Welcome to\n     Arcana Novum!\n\nArcana Novum is a server-sided fabric Magic mod that adds various power Arcana Items to the game. It also includes new game mechanics and multiblocks!").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("      Introduction\n\nYou are probably reading this in your Tome, which will be your guidebook for the entirety of the mod.\n\nThe first page of the tome is your profile.\nThe profile has 3 main sections to it.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("      Arcane Level\n\nYour level decides how many Arcana Items you can carry through Concentration\n\nYou gain XP by using and crafting items.\nCrafting an item for the first time gives additional XP.\nArcana Achievements also grant XP.   ").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("     Concentration\n\nArcana Items each take a certain amount of focus to channel Arcana into. Each rarity tier of item takes a different amount of concentration to use.\nIf you go over your concentration limit, your mind will collapse and you will die.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("     Concentration\n\nItems take full concentration while in your inventory, but half concentration in your Ender Chest or Shulker Boxes.\n\nBlocks that are placed down take a quarter of the concentration when loaded in the world.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("       Skill Points\n\nYou get 3 skill points per Arcana level.\nYou also earn skill points by completing Arcana Achievements.\n\nYou can use these points to unlock Augments for items, which can be applied to enhance or change their abilities.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("      Item Rarities\n\nThere are 5 rarities:\nMundane, Empowered, Exotic, Sovereign, and Divine.\n\nAll Arcana Items above Mundane are immensely powerful, but some are more demanding to wield, which is reflected by their rarity.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("     Mundane Items\n\nConcentration: 0\n\nMundane Items only faintly emit Arcana and are mostly used in conjunction with other Arcana Items, such as being an ingredient in more powerful items, or as a fuel source.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("   Empowered Items\n\nConcentration: 1\n\nEmpowered Items are mostly utility items that offer conveniences in common situations.\n\nThey take a minimal toll to keep in your inventory so feel free to stock up on them!").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("      Exotic Items\n\nConcentration: 5\n\nExotic Items are more powerful items that can offer unique abilities not gained elsewhere, or provide a significant advantage in troubling situations. They are much more demanding to use.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("    Sovereign Items\n\nConcentration: 20\n\nSovereign Items are Arcanists' best attempts at recreating the power found in Divine Artifacts. However, unlike Divine Items, they lack the presence of Divine Arcana that makes wielding them trivial.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("    Sovereign Items\n\nAs a result, these items take an extraordinary amount of focus to wield.\n\nFortunately, they successfully replicate the incredible abilities of Divine Items in a form craftable by mere mortals.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("       Divine Items\n\nConcentration: 0\n\nDivine Items are made by godlike entities, such as the Aspect of Death. As previously mentioned, they use Divine Arcana, which uses the raw energy of the world itself to power them with no effort by the user.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("       Divine Items\n\nThere is no way to craft them in your forge, which means the only way of getting them is by interacting with these powerful entities.\nThis can be a very dangerous door to be knocking on, but the reward could be worth the risk...").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("    Item Compendium\n\nNow that you are caught up on the types of Arcana Items, you can use your Tome to look through all of the available items and how to use and craft them.\nThe Compendium is accessed by clicking the book in the Profile").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("  Researching Items\n\nBefore crafting an item you need to research it. This is done by completing various tasks, which can be in the form of obtaining an item, or  an Advancement.\n\nCompleting research requires Arcane Paper.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("     Forging Items\n\nIn order to craft Arcana Items you need a Starlight Forge. The recipe for which is viewable in the Compendium after researching it.\n\nThe Starlight Forge will require a structure beneath it which is shown by").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("     Forging Items\n\nright clicking the Forge.\n\nOnce completed, the Forge will allow you to craft better gear, and Arcana Items, by researching and following the recipes in your Tome.\n\nSome recipes may\n").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("     Forging Items\n\nrequire your Forge to be upgraded by crafting Forge Additions and placing them around your Forge.\n\nEach Forge Addon will unlock new recipes along with providing their own unique functionalities. ").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("      Augmentation\n\nAugments give your items enhanced capabilities or provide their own unique twist on their original purpose.\n\nEvery item has its own Augments you can unlock with Skill Points.\nHowever, there are not enough Skill Points").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("      Augmentation\n\nto unlock every augment so you much choose carefully.\n\nAugments follow the same rarity as items.\nRarity defines how many skill points they take to unlock and the type of catalyst needed to apply them.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("      Augmentation\n\nUnlocking an Augment does NOT immediately provide their benefits.\n\nAugments must be applied to an individual item by using an Augmentation Catalyst in the Twilight Anvil Forge Addition.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("      Augmentation\n\nItems that possess augments that you do not have unlocked will cost additional concentration to wield, so borrowing items from other players may cause you to use more concentration than expected.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("       Conclusion\n\nThose are the basics of the Arcana Novum mod!\n\nIf you have any questions, ideas, or find any bugs with the mod, please make an issue on the GitHub!\n\nEnjoy unlocking the secrets of Arcana!").withStyle(ChatFormatting.BLACK));
      
      pages.forEach(book::addPage);
      book.setAuthor("Arcana Novum");
      book.setTitle("Arcana Guide");
      
      return book;
   }
}
