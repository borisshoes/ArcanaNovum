package net.borisshoes.arcananovum;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.*;
import eu.pb4.sgui.api.gui.HotbarGui;
import net.borisshoes.arcananovum.bosses.BossFight;
import net.borisshoes.arcananovum.bosses.BossFights;
import net.borisshoes.arcananovum.bosses.dragon.DragonBossFight;
import net.borisshoes.arcananovum.bosses.dragon.guis.PuzzleGui;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.gui.arcanetome.LoreGui;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.MagicItem;
import net.borisshoes.arcananovum.items.MagicItems;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.borisshoes.arcananovum.Arcananovum.devMode;
import static net.borisshoes.arcananovum.Arcananovum.log;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.BOSS_FIGHT;
import static net.borisshoes.arcananovum.gui.arcanetome.TomeGui.getGuideBook;

public class ArcanaCommands {
   
   
   public static int openGuideBook(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException{
      ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
      ItemStack writablebook = new ItemStack(Items.WRITABLE_BOOK);
      writablebook.setNbt(getGuideBook());
      BookElementBuilder bookBuilder = BookElementBuilder.from(writablebook);
      LoreGui loreGui = new LoreGui(player,bookBuilder,null, ArcaneTome.TomeMode.NONE,null);
      loreGui.open();
      return 1;
   }
   
   public static int xpCommand(CommandContext<ServerCommandSource> ctx, Collection<? extends ServerPlayerEntity> targets, int amount, boolean set, boolean points){
      try{
         ServerCommandSource source = ctx.getSource();
         
         for (ServerPlayerEntity player : targets) {
            IArcanaProfileComponent profile = PLAYER_DATA.get(player);
            int oldValue = points ? profile.getXP() : profile.getLevel();
            int newAmount = set ? Math.max(amount, 0) : Math.max(oldValue + amount, 0);
            if(points){
               profile.setXP(newAmount);
            }else{
               newAmount = Math.max(newAmount, 1);
               profile.setXP(LevelUtils.levelToTotalXp(newAmount));
            }
         }
         
         if(targets.size() == 1 && set && points){
            source.sendFeedback(Text.translatable("Set Arcana XP to "+amount+" for ").formatted(Formatting.LIGHT_PURPLE).append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() == 1 && set && !points){
            source.sendFeedback(Text.translatable("Set Arcana Level to "+amount+" for ").formatted(Formatting.LIGHT_PURPLE).append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() == 1 && !set && points){
            source.sendFeedback(Text.translatable("Gave "+amount+" Arcana XP to ").formatted(Formatting.LIGHT_PURPLE).append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() == 1 && !set && !points){
            source.sendFeedback(Text.translatable("Gave "+amount+" Arcana Levels to ").formatted(Formatting.LIGHT_PURPLE).append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() != 1 && set && points){
            source.sendFeedback(Text.translatable("Set Arcana XP to "+amount+" for " + targets.size() + " players").formatted(Formatting.LIGHT_PURPLE), true);
         }else if(targets.size() != 1 && set && !points){
            source.sendFeedback(Text.translatable("Set Arcana Level to "+amount+" for " + targets.size() + " players").formatted(Formatting.LIGHT_PURPLE), true);
         }else if(targets.size() != 1 && !set && points){
            source.sendFeedback(Text.translatable("Gave "+amount+" Arcana XP to " + targets.size() + " players").formatted(Formatting.LIGHT_PURPLE), true);
         }else if(targets.size() != 1 && !set && !points){
            source.sendFeedback(Text.translatable("Gave "+amount+" Arcana Levels to " + targets.size() + " players").formatted(Formatting.LIGHT_PURPLE), true);
         }
         
         return targets.size();
      }catch(Exception e){
         e.printStackTrace();
         return 0;
      }
   }
   
   public static int uuidCommand(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player){
      ServerCommandSource source = ctx.getSource();
      ArrayList<MutableText> response = new ArrayList<>();
      ArrayList<MutableText> response2 = new ArrayList<>();
      Set<String> uuids = new HashSet<>();
      int count = 0;
      
      PlayerInventory inv = player.getInventory();
      for(int i=0; i<inv.size();i++){
         ItemStack item = inv.getStack(i);
         if(item.isEmpty()){
            continue;
         }
         
         boolean isMagic = MagicItemUtils.isMagic(item);
         if(!isMagic)
            continue; // Item not magic, skip
         
         MagicItem magicItem = MagicItemUtils.identifyItem(item);
         NbtCompound magictag = item.getNbt().getCompound("arcananovum");
         count++;
         String uuid = magictag.getString("UUID") ;
         
         MutableText feedback = Text.translatable("")
               .append(Text.translatable("[").formatted(Formatting.LIGHT_PURPLE))
               .append(Text.translatable(magicItem.getName()).formatted(Formatting.AQUA))
               .append(Text.translatable("] ID: ").formatted(Formatting.LIGHT_PURPLE))
               .append(Text.translatable(uuid).formatted(Formatting.DARK_PURPLE));
         response.add(feedback.styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid))));
         if(!uuids.add(uuid) || item.getCount() > 1){
            MutableText duplicateWarning = Text.translatable("")
                  .append(Text.translatable("Duplicate: ").formatted(Formatting.RED))
                  .append(Text.translatable(magicItem.getName()).formatted(Formatting.AQUA))
                  .append(Text.translatable(" ID: ").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.translatable(uuid).formatted(Formatting.DARK_PURPLE));
            response2.add(duplicateWarning);
         }
      }
      
      MutableText feedback = Text.translatable("")
            .append(player.getDisplayName())
            .append(Text.translatable(" has ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.translatable(Integer.toString(count)).formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
            .append(Text.translatable(" items.").formatted(Formatting.LIGHT_PURPLE));
      source.sendFeedback(Text.translatable(""),false);
      source.sendFeedback(feedback,false);
      source.sendFeedback(Text.translatable("================================").formatted(Formatting.LIGHT_PURPLE),false);
      for(MutableText r : response){
         source.sendFeedback(r,false);
      }
      source.sendFeedback(Text.translatable("================================").formatted(Formatting.LIGHT_PURPLE),false);
      for(MutableText r : response2){
         source.sendFeedback(r,false);
      }
      return count;
   }
   
   public static int getBookData(CommandContext<ServerCommandSource> objectCommandContext) {
      if (!devMode)
         return 0;
      try {
         ServerPlayerEntity player = objectCommandContext.getSource().getPlayerOrThrow();
         ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
         if(stack.isOf(Items.WRITTEN_BOOK)){
            NbtCompound tag = stack.getNbt();
            NbtList pages = tag.getList("pages", NbtElement.STRING_TYPE);
            String path = "C:\\Users\\Boris\\Desktop\\bookdata.txt";
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, false)));
            for(int i = 0; i < pages.size(); i++){
               String page = pages.getString(i);
               page = page.replaceAll("\"","\\\\\"").replace("\\n","\\\\n");
               out.println("list.add(\""+page+"\");");
               //log("\n"+page);
            }
            out.close();
         }else{
            player.sendMessage(Text.translatable("Hold a book to get data"),true);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return 0;
   }
   
   public static int xpCommandQuery(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity target){
      try{
         ServerCommandSource source = ctx.getSource();
         IArcanaProfileComponent profile = PLAYER_DATA.get(target);
         MutableText feedback = Text.translatable("")
               .append(target.getDisplayName())
               .append(Text.translatable(" has ").formatted(Formatting.LIGHT_PURPLE))
               .append(Text.translatable(Integer.toString(profile.getLevel())).formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
               .append(Text.translatable(" levels (").formatted(Formatting.LIGHT_PURPLE))
               .append(Text.translatable(LevelUtils.getCurLevelXp(profile.getXP())+"/"+LevelUtils.nextLevelNewXp(profile.getLevel())).formatted(Formatting.AQUA))
               .append(Text.translatable("). ").formatted(Formatting.LIGHT_PURPLE))
               .append(Text.translatable(Integer.toString(profile.getXP())).formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
               .append(Text.translatable(" Total XP").formatted(Formatting.LIGHT_PURPLE));
         source.sendFeedback(feedback, false);
         return 1;
      }catch(Exception e){
         e.printStackTrace();
         return 0;
      }
   }
   
   public static int getItemData(CommandContext<ServerCommandSource> objectCommandContext) {
      if (!devMode)
         return 0;
      try {
         ServerPlayerEntity player = objectCommandContext.getSource().getPlayerOrThrow();
         ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
         if(!stack.isEmpty()){
            NbtCompound tag = stack.getNbt();
            NbtCompound display = tag.getCompound("display");
            
            String path = "C:\\Users\\Boris\\Desktop\\itemdata.txt";
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, false)));
            ArrayList<String> lines = new ArrayList<>();
            
            lines.add("id = \"\";");
            lines.add("name = \"\";");
            lines.add("rarity = MagicRarity.;");
            lines.add("");
            lines.add("ItemStack item = new ItemStack(Items.);");
            lines.add("NbtCompound tag = item.getOrCreateNbt();");
            lines.add("NbtCompound display = new NbtCompound();");
            lines.add("NbtList loreList = new NbtList();");
            lines.add("NbtList enchants = new NbtList();");
            lines.add("enchants.add(new NbtCompound()); // Gives enchant glow with no enchants");
            if(display != null){
               lines.add("display.putString(\"Name\",\""+display.getString("Name").replaceAll("\"","\\\\\"")+"\");");
               NbtList lore = display.getList("Lore",NbtElement.STRING_TYPE);
               for(int i = 0; i < lore.size(); i++){
                  lines.add("loreList.add(NbtString.of(\""+lore.getString(i).replaceAll("\"","\\\\\"")+"\"));");
               }
            }
            lines.add("display.put(\"Lore\",loreList);");
            lines.add("tag.put(\"display\",display);");
            lines.add("tag.put(\"Enchantments\",enchants);");
            lines.add("");
            lines.add("setBookLore(makeLore());");
            lines.add("//setRecipe(makeRecipe());");
            lines.add("prefNBT = addMagicNbt(tag);");
            lines.add("");
            lines.add("item.setNbt(prefNBT);");
            lines.add("prefItem = item;");
            
            for(String line : lines){
               out.println(line);
            }
            out.close();
         }else{
            player.sendMessage(Text.translatable("Hold an item to get data"),true);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return 0;
   }
   
   public static int makeCraftingRecipe(CommandContext<ServerCommandSource> objectCommandContext) {
      if (!devMode)
         return 0;
      try {
         ServerPlayerEntity player = objectCommandContext.getSource().getPlayerOrThrow();
         
      } catch (Exception e) {
         e.printStackTrace();
      }
      return 0;
   }
   
   public static int testCommand(CommandContext<ServerCommandSource> objectCommandContext) {
      if (!devMode)
         return 0;
      try {
         ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
         PuzzleGui gui = new PuzzleGui(ScreenHandlerType.GENERIC_9X6,player,null);
         gui.buildPuzzle();
         gui.open();
         
      } catch (Exception e) {
         e.printStackTrace();
      }
      return 0;
   }
   
   public static CompletableFuture<Suggestions> getItemSuggestions(CommandContext<ServerCommandSource> serverCommandSourceCommandContext, SuggestionsBuilder builder){
      String start = builder.getRemaining().toLowerCase();
      Set<String> items = MagicItems.registry.keySet();
      items.stream().filter(s -> s.startsWith(start)).forEach(builder::suggest);
      return builder.buildFuture();
   }
   
   public static int createItem(ServerCommandSource source, String id) throws CommandSyntaxException{
      try{
         MagicItem magicItem = MagicItemUtils.getItemFromId(id);
         if(magicItem == null){
            source.getPlayerOrThrow().sendMessage(Text.translatable("Invalid Magic Item ID: "+id).formatted(Formatting.RED, Formatting.ITALIC), false);
            return 0;
         }
         ItemStack item = magicItem.getNewItem();
         
         if(item == null){
            source.getPlayerOrThrow().sendMessage(Text.translatable("No Preferred Item Found For: "+magicItem.getName()).formatted(Formatting.RED, Formatting.ITALIC), false);
            return 0;
         }else{
            NbtCompound magicTag = item.getNbt().getCompound("arcananovum");
            String uuid = magicTag.getString("UUID");
            source.getPlayerOrThrow().sendMessage(Text.translatable("Generated New: "+magicItem.getName()+" with UUID "+uuid).formatted(Formatting.GREEN), false);
            source.getPlayerOrThrow().giveItemStack(item);
            return 1;
         }
      }catch(Exception e){
         e.printStackTrace();
         return -1;
      }
   }
   
   public static int startDragonBoss(CommandContext<ServerCommandSource> context){
      ServerCommandSource source = context.getSource();
      if(!source.isExecutedByPlayer()){
         source.sendFeedback(Text.translatable("Command must be executed by a player"), false);
         return -1;
      }
      for(ServerWorld world : source.getServer().getWorlds()){
         if(BOSS_FIGHT.get(world).getBossFight() != null){
            source.sendFeedback(Text.translatable("A Boss Fight is Currently Active"), false);
            return -1;
         }
      }
      ServerPlayerEntity player = source.getPlayer();
      return DragonBossFight.prepBoss(player);
   }
   
   public static int abortBoss(CommandContext<ServerCommandSource> context){
      MinecraftServer server = context.getSource().getServer();
      Pair<BossFights, NbtCompound> bossFight = BOSS_FIGHT.get(server.getWorld(World.END)).getBossFight();
      context.getSource().sendFeedback(Text.translatable("Aborting Boss Fight"),true);
      if(bossFight == null){
         return BossFight.cleanBoss(server);
      }
      if(bossFight.getLeft() == BossFights.DRAGON){
         return DragonBossFight.abortBoss(server);
      }
      return 0;
   }
   
   public static int cleanBoss(CommandContext<ServerCommandSource> context){
      ServerCommandSource source = context.getSource();
      source.sendFeedback(Text.translatable("Cleaned Boss Data"),true);
      return BossFight.cleanBoss(source.getServer());
   }
   
   public static int bossStatus(CommandContext<ServerCommandSource> context){
      ServerCommandSource source = context.getSource();
      Pair<BossFights, NbtCompound> bossFight = BOSS_FIGHT.get(source.getServer().getWorld(World.END)).getBossFight();
      if(bossFight == null){
         source.sendFeedback(Text.translatable("No Boss Fight Active"),false);
         return -1;
      }
      if(bossFight.getLeft() == BossFights.DRAGON){
         return DragonBossFight.bossStatus(source.getServer(),context.getSource());
      }
      return -1;
   }
   
   public static int testBoss(CommandContext<ServerCommandSource> context){
      ServerCommandSource source = context.getSource();
      log("Test Boss");
      DragonBossFight.test();
      return 0;
   }
   
   public static int bossTeleport(CommandContext<ServerCommandSource> context, ServerPlayerEntity player, boolean all){
      ServerCommandSource source = context.getSource();
      Pair<BossFights, NbtCompound> bossFight = BOSS_FIGHT.get(source.getServer().getWorld(World.END)).getBossFight();
      if(bossFight == null){
         source.sendFeedback(Text.translatable("No Boss Fight Active"),false);
         return -1;
      }
      if(bossFight.getLeft() == BossFights.DRAGON){
         if(all){
            List<ServerPlayerEntity> players = source.getServer().getPlayerManager().getPlayerList();
            for(ServerPlayerEntity p : players){
               DragonBossFight.teleportPlayer(p,true);
            }
         }else{
            DragonBossFight.teleportPlayer(player,context.getSource().hasPermissionLevel(2));
         }
         return 0;
      }
      return -1;
   }
   
   public static int announceBoss(ServerCommandSource source, String time){
      Pair<BossFights, NbtCompound> bossFight = BOSS_FIGHT.get(source.getServer().getWorld(World.END)).getBossFight();
      if(bossFight == null){
         source.sendFeedback(Text.translatable("No Boss Fight Active"),false);
         return -1;
      }
      if(bossFight.getLeft() == BossFights.DRAGON){
         return DragonBossFight.announceBoss(source.getServer(),bossFight.getRight(),time);
      }
      return -1;
   }
   
   public static int beginBoss(CommandContext<ServerCommandSource> context){
      Pair<BossFights, NbtCompound> bossFight = BOSS_FIGHT.get(context.getSource().getServer().getWorld(World.END)).getBossFight();
      if(bossFight == null){
         context.getSource().sendFeedback(Text.translatable("No Boss Fight Active"),false);
         return -1;
      }
      if(bossFight.getLeft() == BossFights.DRAGON){
         return DragonBossFight.beginBoss(context.getSource().getServer(),bossFight.getRight());
      }
      return -1;
   }
}
