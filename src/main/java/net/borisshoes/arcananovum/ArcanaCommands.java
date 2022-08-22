package net.borisshoes.arcananovum;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.gui.arcanetome.LoreGui;
import net.borisshoes.arcananovum.items.MagicItem;
import net.borisshoes.arcananovum.items.MagicItems;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static net.borisshoes.arcananovum.Arcananovum.devMode;
import static net.borisshoes.arcananovum.Arcananovum.log;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.borisshoes.arcananovum.gui.arcanetome.TomeGui.getGuideBook;

public class ArcanaCommands {
   
   
   public static int openGuideBook(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException{
      ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
      ItemStack writablebook = new ItemStack(Items.WRITABLE_BOOK);
      writablebook.setNbt(getGuideBook());
      BookElementBuilder bookBuilder = BookElementBuilder.from(writablebook);
      LoreGui loreGui = new LoreGui(player,bookBuilder,null,-1);
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
         ServerPlayerEntity player = objectCommandContext.getSource().getPlayerOrThrow();
         ItemStack item1 = player.getStackInHand(Hand.MAIN_HAND);
         ItemStack item2 = player.getStackInHand(Hand.OFF_HAND);
         if(item1.hasNbt() && item2.hasNbt()){
            log("Testing My Thing: "+ MagicItemIngredient.validNbt(item1.getNbt(),item2.getNbt())+"\n");
         }else if(!item1.hasNbt() && item2.hasNbt()){
            log("false");
         }else if(item1.hasNbt() && !item2.hasNbt()){
            log("true");
         }else{
            log("true");
         }
         
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
}
