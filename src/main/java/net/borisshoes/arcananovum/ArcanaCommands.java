package net.borisshoes.arcananovum;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.bosses.BossFight;
import net.borisshoes.arcananovum.bosses.BossFights;
import net.borisshoes.arcananovum.bosses.dragon.DragonBossFight;
import net.borisshoes.arcananovum.cardinalcomponents.ArcanaProfileComponent;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.gui.arcanetome.LoreGui;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.gui.cache.CacheGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.*;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.BOSS_FIGHT;
import static net.borisshoes.arcananovum.gui.arcanetome.TomeGui.getGuideBook;

public class ArcanaCommands {
   
   
   public static int openGuideBook(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException{
      ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
      BookElementBuilder bookBuilder = getGuideBook();
      LoreGui loreGui = new LoreGui(player,bookBuilder,null, TomeGui.TomeMode.NONE,null);
      loreGui.open();
      return 1;
   }
   
   public static int skillpointsCommand(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> targets, int amount, boolean set){
      try{
         ServerCommandSource source = ctx.getSource();
      
         for (ServerPlayerEntity player : targets) {
            IArcanaProfileComponent profile = ArcanaNovum.data(player);
            
            NbtInt pointsEle = (NbtInt) profile.getMiscData(ArcanaProfileComponent.ADMIN_SKILL_POINTS_TAG);
            int oldPoints = pointsEle == null ? 0 : pointsEle.intValue();
            int newPoints = set ? amount : amount + oldPoints;
            profile.addMiscData(ArcanaProfileComponent.ADMIN_SKILL_POINTS_TAG, NbtInt.of(newPoints));
         }
      
         if(targets.size() == 1 && set){
            source.sendFeedback(()->Text.literal("Set Bonus Skill Points to "+amount+" for ").formatted(Formatting.LIGHT_PURPLE).append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() == 1 && !set){
            source.sendFeedback(()->Text.literal("Gave "+amount+" Bonus Skill Points to ").formatted(Formatting.LIGHT_PURPLE).append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() != 1 && set){
            source.sendFeedback(()->Text.literal("Set Bonus Skill Points to "+amount+" for " + targets.size() + " players").formatted(Formatting.LIGHT_PURPLE), true);
         }else if(targets.size() != 1 && !set){
            source.sendFeedback(()->Text.literal("Gave "+amount+" Bonus Skill Points to " + targets.size() + " players").formatted(Formatting.LIGHT_PURPLE), true);
         }
      
         return targets.size();
      }catch(Exception e){
         log(2,e.toString());
         return 0;
      }
   }
   
   public static int skillpointsCommandQuery(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity target){
      try{
         ServerCommandSource source = ctx.getSource();
         IArcanaProfileComponent profile = ArcanaNovum.data(target);
         NbtInt pointsEle = (NbtInt) profile.getMiscData(ArcanaProfileComponent.ADMIN_SKILL_POINTS_TAG);
         int adminPoints = pointsEle == null ? 0 : pointsEle.intValue();
         MutableText feedback = Text.literal("")
               .append(target.getDisplayName())
               .append(Text.literal(" has ").formatted(Formatting.LIGHT_PURPLE))
               .append(Text.literal(Integer.toString(adminPoints)).formatted(Formatting.AQUA,Formatting.BOLD))
               .append(Text.literal(" Bonus Skill Points").formatted(Formatting.LIGHT_PURPLE));
         source.sendFeedback(()->feedback, false);
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return 0;
      }
   }
   
   public static int xpCommand(CommandContext<ServerCommandSource> ctx, Collection<? extends ServerPlayerEntity> targets, int amount, boolean set, boolean points){
      try{
         ServerCommandSource source = ctx.getSource();
         
         for (ServerPlayerEntity player : targets) {
            IArcanaProfileComponent profile = ArcanaNovum.data(player);
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
            source.sendFeedback(()->Text.literal("Set Arcana XP to "+amount+" for ").formatted(Formatting.LIGHT_PURPLE).append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() == 1 && set && !points){
            source.sendFeedback(()->Text.literal("Set Arcana Level to "+amount+" for ").formatted(Formatting.LIGHT_PURPLE).append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() == 1 && !set && points){
            source.sendFeedback(()->Text.literal("Gave "+amount+" Arcana XP to ").formatted(Formatting.LIGHT_PURPLE).append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() == 1 && !set && !points){
            source.sendFeedback(()->Text.literal("Gave "+amount+" Arcana Levels to ").formatted(Formatting.LIGHT_PURPLE).append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() != 1 && set && points){
            source.sendFeedback(()->Text.literal("Set Arcana XP to "+amount+" for " + targets.size() + " players").formatted(Formatting.LIGHT_PURPLE), true);
         }else if(targets.size() != 1 && set && !points){
            source.sendFeedback(()->Text.literal("Set Arcana Level to "+amount+" for " + targets.size() + " players").formatted(Formatting.LIGHT_PURPLE), true);
         }else if(targets.size() != 1 && !set && points){
            source.sendFeedback(()->Text.literal("Gave "+amount+" Arcana XP to " + targets.size() + " players").formatted(Formatting.LIGHT_PURPLE), true);
         }else if(targets.size() != 1 && !set && !points){
            source.sendFeedback(()->Text.literal("Gave "+amount+" Arcana Levels to " + targets.size() + " players").formatted(Formatting.LIGHT_PURPLE), true);
         }
         
         return targets.size();
      }catch(Exception e){
         log(2,e.toString());
         return 0;
      }
   }
   
   public static int uuidCommand(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player){
      ServerCommandSource source = ctx.getSource();
      ArrayList<MutableText> response = new ArrayList<>();
      ArrayList<MutableText> response2 = new ArrayList<>();
      Set<String> uuids = new HashSet<>();
      int count = 0;
      
      List<ArcanaItemUtils.ArcanaInvItem> arcanaInv = ArcanaItemUtils.getArcanaInventory(player);
      for(ArcanaItemUtils.ArcanaInvItem invItem : arcanaInv){
         ArcanaItem arcanaItem = invItem.item;
         for(Pair<String,ItemStack> pair : invItem.getStacks()){
            String uuid = pair.getLeft();
            ItemStack stack = pair.getRight();
            count++;
   
            String storage = invItem.getShortContainerString();
            
            MutableText feedback = Text.literal("")
                  .append(Text.literal("(").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.literal(storage).formatted(Formatting.BLUE))
                  .append(Text.literal(") ").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.literal("[").formatted(Formatting.LIGHT_PURPLE))
                  .append(arcanaItem.getTranslatedName().formatted(Formatting.AQUA))
                  .append(Text.literal("] ID: ").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.literal(uuid).formatted(Formatting.DARK_PURPLE));
            response.add(feedback.styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(stack))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid))));
            
            if(!uuids.add(uuid) || invItem.getStacks().size() < (invItem.getCount()/ arcanaItem.getPrefItem().getCount())){
               MutableText duplicateWarning = Text.literal("")
                     .append(Text.literal("Duplicate: ").formatted(Formatting.RED))
                     .append(arcanaItem.getTranslatedName().formatted(Formatting.AQUA))
                     .append(Text.literal(" ID: ").formatted(Formatting.LIGHT_PURPLE))
                     .append(Text.literal(uuid).formatted(Formatting.DARK_PURPLE));
               response2.add(duplicateWarning);
            }
         }
      }
      
      MutableText feedback = Text.literal("")
            .append(player.getDisplayName())
            .append(Text.literal(" has ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(Integer.toString(count)).formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
            .append(Text.literal(" items.").formatted(Formatting.LIGHT_PURPLE));
      source.sendFeedback(()->Text.literal(""),false);
      source.sendFeedback(()->feedback,false);
      source.sendFeedback(()->Text.literal("================================").formatted(Formatting.LIGHT_PURPLE),false);
      for(MutableText r : response){
         source.sendFeedback(()->r,false);
      }
      source.sendFeedback(()->Text.literal("================================").formatted(Formatting.LIGHT_PURPLE),false);
      for(MutableText r : response2){
         source.sendFeedback(()->r,false);
      }
      return count;
   }
   
   public static int getBookData(CommandContext<ServerCommandSource> objectCommandContext) {
      if (!DEV_MODE)
         return 0;
      try {
         ServerPlayerEntity player = objectCommandContext.getSource().getPlayerOrThrow();
         ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
         ArrayList<String> lines = new ArrayList<>();
         if(stack.isOf(Items.WRITTEN_BOOK)){
            WrittenBookContentComponent comp = stack.getOrDefault(DataComponentTypes.WRITTEN_BOOK_CONTENT,WrittenBookContentComponent.DEFAULT);
            for(Text page : comp.getPages(false)){
               lines.add(page.getString());
            }
         }else if(stack.isOf(Items.WRITABLE_BOOK)){
            WritableBookContentComponent comp = stack.getOrDefault(DataComponentTypes.WRITABLE_BOOK_CONTENT,WritableBookContentComponent.DEFAULT);
            for(RawFilteredPair<String> page : comp.pages()){
               lines.add(page.get(false));
            }
         }
         
         if(lines.isEmpty()){
            player.sendMessage(Text.literal("Hold a written book to get data"),true);
         }else{
            String path = "C:\\Users\\Boris\\Desktop\\bookdata.txt";
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, false)));
            
            for(String line : lines){
               out.println("pages.add(Text.literal(\""+line.replace("\n","\\n")+"\").formatted(Formatting.BLACK));");
            }
            
            out.close();
         }
      } catch (Exception e) {
         log(2,e.toString());
      }
      return 0;
   }
   
   public static int xpCommandQuery(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity target){
      try{
         ServerCommandSource source = ctx.getSource();
         IArcanaProfileComponent profile = ArcanaNovum.data(target);
         MutableText feedback = Text.literal("")
               .append(target.getDisplayName())
               .append(Text.literal(" has ").formatted(Formatting.LIGHT_PURPLE))
               .append(Text.literal(Integer.toString(profile.getLevel())).formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
               .append(Text.literal(" levels (").formatted(Formatting.LIGHT_PURPLE))
               .append(Text.literal(LevelUtils.getCurLevelXp(profile.getXP())+"/"+LevelUtils.nextLevelNewXp(profile.getLevel())).formatted(Formatting.AQUA))
               .append(Text.literal("). ").formatted(Formatting.LIGHT_PURPLE))
               .append(Text.literal(Integer.toString(profile.getXP())).formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
               .append(Text.literal(" Total XP").formatted(Formatting.LIGHT_PURPLE));
         source.sendFeedback(()->feedback, false);
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return 0;
      }
   }
   
   public static int getItemData(CommandContext<ServerCommandSource> objectCommandContext, String name) {
      if (!DEV_MODE)
         return 0;
      try {
         ServerPlayerEntity player = objectCommandContext.getSource().getPlayerOrThrow();
         ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
         if(!stack.isEmpty()){
            
            StringBuilder itemName = new StringBuilder();
            boolean foundFirstCap = false;
            for(int i = 0; i < name.length(); i++){
               char c = name.charAt(i);
               if(Character.isUpperCase(c)){
                  if(foundFirstCap){
                     itemName.append(" ").append(c);
                     continue;
                  }else{
                     foundFirstCap = true;
                  }
               }
               itemName.append(c);
            }
            String fullName = itemName.toString();
            String idName = fullName.replace(" ","_").toLowerCase();
            
            String path = "C:\\Users\\Boris\\Desktop\\itemdata.txt";
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, false)));
            ArrayList<String> lines = new ArrayList<>();
            
            
            // TODO New item gen
            
            
            for(String line : lines){
               out.println(line);
            }
            out.close();
         }else{
            player.sendMessage(Text.literal("Hold an item to get data"),true);
         }
      } catch (Exception e) {
         log(2,e.toString());
      }
      return 0;
   }
   
   public static int makeCraftingRecipe(CommandContext<ServerCommandSource> objectCommandContext) {
      if (!DEV_MODE)
         return 0;
      try {
         ServerPlayerEntity player = objectCommandContext.getSource().getPlayerOrThrow();
         ServerWorld world = player.getServerWorld();
         Vec3d vec3d = player.getCameraPosVec(0);
         Vec3d vec3d2 = player.getRotationVec(0);
         double maxDistance = 5;
         Vec3d vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
         BlockHitResult result = world.raycast(new RaycastContext(vec3d, vec3d3, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
         if(result.getType() == BlockHitResult.Type.MISS){
            return 0;
         }
         BlockPos blockPos = result.getBlockPos();
         BlockEntity blockEntity = world.getBlockEntity(blockPos);
         BlockState blockState = world.getBlockState(blockPos);
         Block block = blockState.getBlock();
         if(blockEntity instanceof ChestBlockEntity chest && block instanceof ChestBlock chestBlock){
            Inventory chestInventory = ChestBlock.getInventory(chestBlock, blockState, world, blockPos, true);
            if(chestInventory == null || chestInventory.size() != 54){
               return 0;
            }
            
            Pair<ArcanaIngredient,Character>[][] ingredients = new Pair[5][5];
            HashMap<Character,ArrayList<String>> lineSet = new HashMap<>();
            for(int i = 0; i < 5; i++){
               for(int j = 0; j < 5; j++){
                  ItemStack stack = chestInventory.getStack(i*9+j);

                  ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
                  ArcanaIngredient ingred;
                  ArrayList<String> lines = new ArrayList<>();
                  char letter = (char) ('a' + (i * 5 + j));

                  if(arcanaItem != null){
                     ingred = new GenericArcanaIngredient(arcanaItem,stack.getCount());
                     String idName = arcanaItem.getId().toUpperCase();
                     lines.add("GenericArcanaIngredient "+letter+" = new GenericArcanaIngredient(ArcanaRegistry."+idName+","+stack.getCount()+");");
                  }else if(stack.contains(DataComponentTypes.POTION_CONTENTS)){
                     String idName = Registries.ITEM.getId(stack.getItem()).getPath().toUpperCase();
                     PotionContentsComponent potionsComp = stack.get(DataComponentTypes.POTION_CONTENTS);
                     String ingredStr = "ArcanaIngredient "+letter+" = new ArcanaIngredient(Items."+idName+","+stack.getCount()+")";
                     ingred = new ArcanaIngredient(stack.getItem(),stack.getCount());
                     if(potionsComp.potion().isPresent()){
                        ingredStr += ".withPotions(Potions."+Registries.POTION.getId(potionsComp.potion().get().value()).getPath().toUpperCase()+");";
                        ingred = ingred.withPotions(potionsComp.potion().get());
                     }
                     lines.add(ingredStr);
                  }else if(EnchantmentHelper.hasEnchantments(stack)){
                     String idName = Registries.ITEM.getId(stack.getItem()).getPath().toUpperCase();
                     ItemEnchantmentsComponent enchantComp = EnchantmentHelper.getEnchantments(stack);
                     String ingredStr = "ArcanaIngredient "+letter+" = new ArcanaIngredient(Items."+idName+","+stack.getCount()+")";
                     ingred = new ArcanaIngredient(stack.getItem(),stack.getCount());
                     
                     ArrayList<String> enchStrs = new ArrayList<>();
                     for(RegistryEntry<Enchantment> entry : enchantComp.getEnchantments()){
                        enchStrs.add("new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments."+entry.getKey().get().getValue().getPath().toUpperCase()+"),"+enchantComp.getLevel(entry)+")");
                        ingred = ingred.withEnchantments(new EnchantmentLevelEntry(entry, enchantComp.getLevel(entry)));
                     }
                     
                     if(!enchStrs.isEmpty()){
                        ingredStr += ".withEnchantments(";
                        for(int k = 0; k < enchStrs.size(); k++){
                           ingredStr += enchStrs.get(k);
                           if(k != enchStrs.size() - 1){
                              ingredStr += ", ";
                           }
                        }
                        ingredStr += ")";
                     }
                     ingredStr += ";";
                     lines.add(ingredStr);
                  }else if(stack.isEmpty()){
                     ingred = ArcanaIngredient.EMPTY;
                     lines.add("ArcanaIngredient "+letter+" = ArcanaIngredient.EMPTY;");
                  }else{
                     ingred = new ArcanaIngredient(stack.getItem(),stack.getCount());
                     String idName = Registries.ITEM.getId(stack.getItem()).getPath().toUpperCase();
                     lines.add("ArcanaIngredient "+letter+" = new ArcanaIngredient(Items."+idName+","+stack.getCount()+");");
                  }

                  boolean match = false;
                  for(int m = 0; m <= i; m++){
                     if(match) break;
                     for(int n = 0; n < (m == i ? j : 5); n++){
                        Pair<ArcanaIngredient,Character> prev = ingredients[m][n];
                        ArcanaIngredient prevIng = prev.getLeft();

                        if(prevIng.equals(ingred)){
                           ingredients[i][j] = prev;
                           match = true;
                           break;
                        }
                     }
                  }
                  if(!match) {
                     ingredients[i][j] = new Pair<>(ingred, letter);
                     lineSet.put(letter,lines);
                  }
                  //System.out.print(chestInventory.getStack(i*9+j).getItem().getName().getString()+" ");
               }
               //System.out.println();
            }
            
            String forgeReqStr = "new ForgeRequirement()";
            int[] forgeSlots = new int[]{6,7,8,15,16,17};
            for(int forgeSlot : forgeSlots){
               ItemStack stack = chestInventory.getStack(forgeSlot);
               if(stack.isOf(Items.ANVIL)){
                  forgeReqStr += ".withAnvil()";
               }else if(stack.isOf(Items.BLAST_FURNACE)){
                  forgeReqStr += ".withCore()";
               }else if(stack.isOf(Items.ENCHANTING_TABLE)){
                  forgeReqStr += ".withEnchanter()";
               }else if(stack.isOf(Items.FLETCHING_TABLE)){
                  forgeReqStr += ".withFletchery()";
               }else if(stack.isOf(Items.LECTERN)){
                  forgeReqStr += ".withSingularity()";
               }
            }
   
            String path = "C:\\Users\\Boris\\Desktop\\itemrecipe.txt";
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, false)));
            ArrayList<String> lines = new ArrayList<>();
   
            for(Map.Entry<Character, ArrayList<String>> entry : lineSet.entrySet()){
               lines.addAll(entry.getValue());
            }
            lines.add("");
            lines.add("ArcanaIngredient[][] ingredients = {");
            for(int i = 0; i < 5; i++){
               String line = "   {";
               for(int j = 0; j < 5; j++){
                  line += ingredients[i][j].getRight()+",";
               }
               if(i == 4){
                  line = line.substring(0,line.length()-1) + "}};";
               }else{
                  line = line.substring(0,line.length()-1) + "},";
               }
               lines.add(line);
            }
            lines.add("return new ArcanaRecipe(ingredients,"+forgeReqStr+");");
            
            for(String line : lines){
               out.println(line);
            }
            out.close();
            return 1;
         }
         
      } catch (Exception e) {
         log(2,e.toString());
      }
      return 0;
   }
   
   public static int testCommand(CommandContext<ServerCommandSource> ctx) {
      if (!DEV_MODE)
         return 0;
      try {
         ServerPlayerEntity player = ctx.getSource().getPlayer();
         
//         PuzzleGui gui = new PuzzleGui(ScreenHandlerType.GENERIC_9X6,player,null);
//         gui.buildPuzzle();
//         gui.open();
         
         //ArcanaItem.putProperty(player.getMainHandStack(), GraphicalItem.GRAPHICS_TAG,"confirm");
         
//         ParticleEffectUtils.animatedLightningBolt(
//               player.getServerWorld(),
//               player.getPos().add(0,4,0),
//               player.getPos().add(4,8,4),
//               13,
//               0.8,
//               ParticleTypes.END_ROD,
//               12,
//               1,
//               0,
//               0,
//               false,
//               0,
//               5
//               );
         
         ParticleEffectUtils.aequalisTransmuteAnim(ctx.getSource().getWorld(),player.getPos().add(0,2,5),0,player.getRotationClient(),1,new ItemStack(Items.ACACIA_LOG),new ItemStack(Items.SPRUCE_LOG),new ItemStack(Items.COPPER_INGOT),new ItemStack(Items.EMERALD),ArcanaRegistry.AEQUALIS_SCIENTIA.getPrefItemNoLore());
         
      } catch (Exception e) {
         log(2,e.toString());
      }
      return 0;
   }
   
   public static int testCommand(CommandContext<ServerCommandSource> objectCommandContext, int num) {
      if (!DEV_MODE)
         return 0;
      try {
         ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
         
         DEBUG_VALUE = num;
         
      } catch (Exception e) {
         log(2,e.toString());
      }
      return 0;
   }
   
   public static int cacheCommand(CommandContext<ServerCommandSource> objectCommandContext){
      try{
         ServerCommandSource source = objectCommandContext.getSource();
         ServerPlayerEntity player = source.getPlayerOrThrow();
   
         CacheGui gui = new CacheGui(player);
         gui.buildCompendiumGui();
         gui.open();
         return 0;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static CompletableFuture<Suggestions> getItemSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder){
      String start = builder.getRemaining().toLowerCase();
      Set<String> items = new HashSet<>();
      ArcanaRegistry.ARCANA_ITEMS.getKeys().forEach(key -> items.add(key.getValue().getPath()));
      items.stream().filter(s -> s.startsWith(start)).forEach(builder::suggest);
      return builder.buildFuture();
   }
   
   public static CompletableFuture<Suggestions> getResearchSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder){
      String start = builder.getRemaining().toLowerCase();
      Set<String> items = new HashSet<>();
      ArcanaRegistry.ARCANA_ITEMS.getKeys().forEach(key -> items.add(key.getValue().getPath()));
      items.add("all");
      items.stream().filter(s -> s.startsWith(start)).forEach(builder::suggest);
      return builder.buildFuture();
   }
   
   public static CompletableFuture<Suggestions> getAchievementSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder){
      String start = builder.getRemaining().toLowerCase();
      Set<String> items = ArcanaAchievements.registry.keySet();
      items.stream().filter(s -> s.startsWith(start)).forEach(builder::suggest);
      return builder.buildFuture();
   }
   
   public static CompletableFuture<Suggestions> getAugmentSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder){
      ServerCommandSource src = context.getSource();
      String start = builder.getRemaining().toLowerCase();
      ArcanaItem arcanaItem;
      if(src.isExecutedByPlayer() && src.getPlayer() != null){
         ItemStack handItem = src.getPlayer().getMainHandStack();
         arcanaItem = ArcanaItemUtils.identifyItem(handItem);
      }else{
         arcanaItem = null;
      }
      Set<String> augments = ArcanaAugments.registry.keySet();
      if(arcanaItem != null){
         augments = augments.stream().filter(s -> ArcanaAugments.registry.get(s).getArcanaItem().getId().equals(arcanaItem.getId())).collect(Collectors.toSet());
      }
      augments.stream().filter(s -> s.startsWith(start)).forEach(builder::suggest);
      return builder.buildFuture();
   }
   
   public static int setAugment(CommandContext<ServerCommandSource> ctx, String id, int level, ServerPlayerEntity player){
      try{
         ServerCommandSource src = ctx.getSource();
         if(!src.isExecutedByPlayer() && player == null){
            src.sendError(Text.literal("Must specify player, or run command as a player"));
            return -1;
         }else if(player == null && src.getPlayer() != null){
            player = src.getPlayer();
         }
         ArcanaAugment augment = ArcanaAugments.registry.get(id);
         if(augment == null){
            src.sendError(Text.literal("That is not a valid Augment"));
            return -1;
         }
         if(level < 0 || level > augment.getTiers().length){
            src.sendError(Text.literal("Level out of bounds (0-"+augment.getTiers().length+")"));
            return -1;
         }
         if(level == 0){
            ArcanaNovum.data(player).removeAugment(id);
            src.sendMessage(Text.literal("Successfully removed ").append(augment.getTranslatedName()).append(" from ").append(player.getDisplayName()));
         }else{
            ArcanaNovum.data(player).setAugmentLevel(id,level);
            src.sendMessage(Text.literal("Successfully set ").append(augment.getTranslatedName()).append(" to level "+level+" for ").append(player.getDisplayName()));
         }
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int enhanceCommand(CommandContext<ServerCommandSource> ctx, double percentage, ServerPlayerEntity player){
      try{
         ServerCommandSource src = ctx.getSource();
         if(!src.isExecutedByPlayer() && player == null){
            src.sendError(Text.literal("Must specify player, or run command as a player"));
            return -1;
         }else if(player == null && src.getPlayer() != null){
            player = src.getPlayer();
         }
         ItemStack handItem = player.getMainHandStack();
         
         if(percentage > 100 || percentage < 0){
            src.sendError(Text.literal("Percentage out of bounds 0.0 - 100.0"));
            return -1;
         }
         if(!EnhancedStatUtils.isItemEnhanceable(handItem)){
            src.sendError(Text.literal("Player is not holding a valid infusion item"));
            return -1;
         }
         
         if(percentage == 0){
            EnhancedStatUtils.stripEnhancements(handItem,true);
         }else{
            EnhancedStatUtils.enhanceItem(handItem,percentage/100.0);
         }
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int applyAugment(CommandContext<ServerCommandSource> ctx, String id, int level, ServerPlayerEntity player){
      try{
         ServerCommandSource src = ctx.getSource();
         if(!src.isExecutedByPlayer() && player == null){
            src.sendError(Text.literal("Must specify player, or run command as a player"));
            return -1;
         }else if(player == null && src.getPlayer() != null){
            player = src.getPlayer();
         }
         ArcanaAugment augment = ArcanaAugments.registry.get(id);
         ItemStack handItem = player.getMainHandStack();
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(handItem);
         
         if(augment == null){
            src.sendError(Text.literal("That is not a valid Augment"));
            return -1;
         }
         if(level < 1 || level > augment.getTiers().length){
            src.sendError(Text.literal("Level out of bounds (1-"+augment.getTiers().length+")"));
            return -1;
         }
         if(arcanaItem == null || !arcanaItem.getId().equals(augment.getArcanaItem().getId())){
            src.sendError(Text.literal("Player is not holding a valid Arcana Item"));
            return -1;
         }
         if(ArcanaAugments.isIncompatible(handItem,id)){
            src.sendError(Text.literal("This augment is incompatible with existing augments"));
            return -1;
         }
         if(ArcanaAugments.applyAugment(handItem,id,level,false)){
            src.sendMessage(Text.literal("Successfully applied ").append(augment.getTranslatedName()).append(" at level "+level+" for ").append(player.getDisplayName()));
            return 1;
         }else{
            src.sendError(Text.literal("Couldn't apply augment (Cannot downgrade existing augments)"));
            return -1;
         }
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int setResearch(CommandContext<ServerCommandSource> ctx, String id, boolean grant, Collection<ServerPlayerEntity> targets){
      try{
         ServerCommandSource source = ctx.getSource();
         ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(id);
         boolean isAll = id.equals("all");
         
         if(arcanaItem == null && !isAll){
            source.sendError(Text.literal("That is not a valid item id"));
            return -1;
         }
         
         List<ArcanaItem> items = new ArrayList<>();
         if(isAll){
            ArcanaRegistry.ARCANA_ITEMS.getEntrySet().forEach(entry -> items.add(entry.getValue()));
         }else{
            items.add(arcanaItem);
         }
         
         for (ServerPlayerEntity player : targets) {
            if(grant){
               for(ArcanaItem item : items){
                  ArcanaNovum.data(player).addResearchedItem(item.getId());
               }
            }else{
               for(ArcanaItem item : items){
                  ArcanaNovum.data(player).removeResearchedItem(item.getId());
               }
            }
         }
         
         MutableText feedback = Text.literal("");
         MutableText itemName = isAll ? Text.literal("All Arcana Items") : arcanaItem.getTranslatedName();
         if(grant){
            feedback.append(Text.literal("Added Research for [").formatted(Formatting.LIGHT_PURPLE));
            feedback.append(itemName.formatted(Formatting.AQUA));
            feedback.append(Text.literal("] to ").formatted(Formatting.LIGHT_PURPLE));
         }else{
            feedback.append(Text.literal("Removed Research for [").formatted(Formatting.LIGHT_PURPLE));
            feedback.append(itemName.formatted(Formatting.AQUA));
            feedback.append(Text.literal("] from ").formatted(Formatting.LIGHT_PURPLE));
         }
         if(targets.size() == 1){
            feedback.append(targets.iterator().next().getDisplayName());
         }else{
            feedback.append(Text.literal(targets.size() + " players").formatted(Formatting.LIGHT_PURPLE));
         }
         source.sendFeedback(()->feedback,true);
         
         return targets.size();
      }catch(Exception e){
         log(2,e.toString());
         return 0;
      }
   }
   
   public static int getResearch(CommandContext<ServerCommandSource> ctx, String id, ServerPlayerEntity target){
      try{
         ServerCommandSource source = ctx.getSource();
         IArcanaProfileComponent profile = ArcanaNovum.data(target);
         ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(id);
         
         if(arcanaItem == null){
            source.sendError(Text.literal("That is not a valid item id"));
            return -1;
         }
         
         boolean researched = profile.hasResearched(arcanaItem);
         
         MutableText feedback = Text.literal("")
               .append(target.getDisplayName().copy())
               .append(Text.literal(researched ? " has researched [" : " has NOT researched [").formatted(Formatting.LIGHT_PURPLE))
               .append(arcanaItem.getTranslatedName().formatted(Formatting.AQUA))
               .append(Text.literal("]").formatted(Formatting.LIGHT_PURPLE));
         
         source.sendFeedback(()->feedback,false);
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   
   public static int setAchievement(CommandContext<ServerCommandSource> ctx, String id, boolean grant, Collection<ServerPlayerEntity> targets){
      try{
         ServerCommandSource source = ctx.getSource();
         ArcanaAchievement achievement = ArcanaAchievements.registry.get(id);
         if(achievement == null){
            source.sendError(Text.literal("That is not a valid Achievement"));
            return -1;
         }
      
         for (ServerPlayerEntity player : targets) {
            if(grant){
               ArcanaAchievements.grant(player,id);
            }else{
               ArcanaAchievements.revoke(player,id);
            }
         }
      
         MutableText feedback = Text.literal("");
         if(grant){
            feedback.append(Text.literal("Granted Achievement [").formatted(Formatting.LIGHT_PURPLE));
            feedback.append(achievement.getTranslatedName().formatted(Formatting.AQUA));
            feedback.append(Text.literal("] to ").formatted(Formatting.LIGHT_PURPLE));
         }else{
            feedback.append(Text.literal("Revoked Achievement [").formatted(Formatting.LIGHT_PURPLE));
            feedback.append(achievement.getTranslatedName().formatted(Formatting.AQUA));
            feedback.append(Text.literal("] from ").formatted(Formatting.LIGHT_PURPLE));
         }
         if(targets.size() == 1){
            feedback.append(targets.iterator().next().getDisplayName());
         }else{
            feedback.append(Text.literal(targets.size() + " players").formatted(Formatting.LIGHT_PURPLE));
         }
         source.sendFeedback(()->feedback,true);
      
         return targets.size();
      }catch(Exception e){
         log(2,e.toString());
         return 0;
      }
   }
   
   public static int getAchievement(CommandContext<ServerCommandSource> ctx, String id, ServerPlayerEntity target){
      try{
         ServerCommandSource source = ctx.getSource();
         IArcanaProfileComponent profile = ArcanaNovum.data(target);
         ArcanaAchievement baseAch = ArcanaAchievements.registry.get(id);
         if(baseAch == null){
            source.sendError(Text.literal("That is not a valid Achievement"));
            return -1;
         }
         ArcanaAchievement profAchieve = profile.getAchievement(baseAch.getArcanaItem().getId(),id);
         ArcanaAchievement achieve = profAchieve == null ? baseAch : profAchieve;
         MutableText[] response = achieve.getStatusDisplay(target);
         
   
         MutableText header = Text.literal("")
               .append(target.getDisplayName().copy().append("'s"))
               .append(Text.literal(" progress towards [").formatted(Formatting.LIGHT_PURPLE))
               .append(achieve.getTranslatedName().formatted(Formatting.AQUA))
               .append(Text.literal("]: ").formatted(Formatting.LIGHT_PURPLE));
         
         source.sendFeedback(()->header,false);
         if(response == null) return 0;
         for(MutableText mutableText : response){
            source.sendFeedback(()->mutableText, false);
         }
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return 0;
      }
   }
   
   public static int createItems(ServerCommandSource source, String id, Collection<ServerPlayerEntity> targets){
      try{
         ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(id);
         if(arcanaItem == null){
            source.sendMessage(Text.literal("Invalid Arcana Item ID: "+id).formatted(Formatting.RED, Formatting.ITALIC));
            return 0;
         }
   
         for(ServerPlayerEntity target : targets){
            ItemStack item = arcanaItem.addCrafter(arcanaItem.getNewItem(),target.getUuidAsString(),true,source.getServer());
            
            if(item == null){
               source.sendMessage((Text.literal("No Preferred Item Found For: ").append(arcanaItem.getTranslatedName())).formatted(Formatting.RED, Formatting.ITALIC));
               return 0;
            }else{
               String uuid = ArcanaItem.getUUID(item);
               source.sendMessage((Text.literal("Generated New: ").append(arcanaItem.getTranslatedName()).append(Text.literal(" with UUID "+uuid))).formatted(Formatting.GREEN));
               target.giveItemStack(item);
            }
         }
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int createItem(ServerCommandSource source, String id) throws CommandSyntaxException{
      try{
         ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(id);
         if(arcanaItem == null){
            source.sendMessage(Text.literal("Invalid Arcana Item ID: "+id).formatted(Formatting.RED, Formatting.ITALIC));
            return 0;
         }
         ItemStack item = arcanaItem.addCrafter(arcanaItem.getNewItem(),source.getPlayerOrThrow().getUuidAsString(),true,source.getServer());
         
         if(item == null){
            source.sendMessage((Text.literal("No Preferred Item Found For: ").append(arcanaItem.getTranslatedName())).formatted(Formatting.RED, Formatting.ITALIC));
            return 0;
         }else{
            String uuid = ArcanaItem.getUUID(item);
            source.sendMessage((Text.literal("Generated New: ").append(arcanaItem.getTranslatedName()).append(Text.literal(" with UUID "+uuid))).formatted(Formatting.GREEN));
            source.getPlayerOrThrow().giveItemStack(item);
            return 1;
         }
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int showItem(CommandContext<ServerCommandSource> context){
      ServerCommandSource source = context.getSource();
      if(!source.isExecutedByPlayer() || source.getPlayer() == null){
         source.sendFeedback(()->Text.literal("Command must be executed by a player"), false);
         return -1;
      }
      ServerPlayerEntity player = source.getPlayer();
      if(!ArcanaItemUtils.isArcane(player.getMainHandStack())){
         source.sendFeedback(()->Text.literal("You can only show off Arcana Items"), false);
         return -1;
      }
      
      MutableText message = Text.literal("")
            .append(player.getStyledDisplayName())
            .append(Text.literal(" is showing off their ").formatted(Formatting.LIGHT_PURPLE))
            .append(player.getMainHandStack().toHoverableText());
      
      for(ServerPlayerEntity other : context.getSource().getServer().getPlayerManager().getPlayerList()){
         other.sendMessage(message);
      }
      
      return 0;
   }
   
   public static int startDragonBoss(CommandContext<ServerCommandSource> context){
      ServerCommandSource source = context.getSource();
      if(!source.isExecutedByPlayer()){
         source.sendFeedback(()->Text.literal("Command must be executed by a player"), false);
         return -1;
      }
      for(ServerWorld world : source.getServer().getWorlds()){
         if(BOSS_FIGHT.get(world).getBossFight() != null){
            source.sendFeedback(()->Text.literal("A Boss Fight is Currently Active"), false);
            return -1;
         }
      }
      ServerPlayerEntity player = source.getPlayer();
      return DragonBossFight.prepBoss(player);
   }
   
   public static int abortBoss(CommandContext<ServerCommandSource> context){
      MinecraftServer server = context.getSource().getServer();
      Pair<BossFights, NbtCompound> bossFight = BOSS_FIGHT.get(server.getWorld(World.END)).getBossFight();
      context.getSource().sendFeedback(()->Text.literal("Aborting Boss Fight"),true);
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
      source.sendFeedback(()->Text.literal("Cleaned Boss Data"),true);
      return BossFight.cleanBoss(source.getServer());
   }
   
   public static int bossStatus(CommandContext<ServerCommandSource> context){
      ServerCommandSource source = context.getSource();
      Pair<BossFights, NbtCompound> bossFight = BOSS_FIGHT.get(source.getServer().getWorld(World.END)).getBossFight();
      if(bossFight == null){
         source.sendFeedback(()->Text.literal("No Boss Fight Active"),false);
         return -1;
      }
      if(bossFight.getLeft() == BossFights.DRAGON){
         return DragonBossFight.bossStatus(source.getServer(),context.getSource());
      }
      return -1;
   }
   
   public static int bossResetAbilities(CommandContext<ServerCommandSource> context, boolean doAbility){
      ServerCommandSource source = context.getSource();
      Pair<BossFights, NbtCompound> bossFight = BOSS_FIGHT.get(source.getServer().getWorld(World.END)).getBossFight();
      if(bossFight == null){
         source.sendFeedback(()->Text.literal("No Boss Fight Active"),false);
         return -1;
      }
      if(bossFight.getLeft() == BossFights.DRAGON){
         return DragonBossFight.resetDragonAbilities(source.getServer(),context.getSource(),doAbility);
      }
      return -1;
   }
   
   public static int bossForceLairAction(CommandContext<ServerCommandSource> context){
      ServerCommandSource source = context.getSource();
      Pair<BossFights, NbtCompound> bossFight = BOSS_FIGHT.get(source.getServer().getWorld(World.END)).getBossFight();
      if(bossFight == null){
         source.sendFeedback(()->Text.literal("No Boss Fight Active"),false);
         return -1;
      }
      if(bossFight.getLeft() == BossFights.DRAGON){
         return DragonBossFight.forceLairAction(source.getServer(),context.getSource());
      }
      return -1;
   }
   
   public static int bossForcePlayerCount(CommandContext<ServerCommandSource> context, int playerCount){
      ServerCommandSource source = context.getSource();
      Pair<BossFights, NbtCompound> bossFight = BOSS_FIGHT.get(source.getServer().getWorld(World.END)).getBossFight();
      if(bossFight == null){
         source.sendFeedback(()->Text.literal("No Boss Fight Active"),false);
         return -1;
      }
      if(bossFight.getLeft() == BossFights.DRAGON){
         DragonBossFight.setForcedPlayerCount(context.getSource().getServer(),playerCount);
         return 1;
      }
      return -1;
   }
   
   public static int testBoss(CommandContext<ServerCommandSource> context){
      ServerCommandSource source = context.getSource();
      
      return 0;
   }
   
   public static int bossTeleport(CommandContext<ServerCommandSource> context, ServerPlayerEntity player, boolean all){
      ServerCommandSource source = context.getSource();
      Pair<BossFights, NbtCompound> bossFight = BOSS_FIGHT.get(source.getServer().getWorld(World.END)).getBossFight();
      if(bossFight == null){
         source.sendFeedback(()->Text.literal("No Boss Fight Active"),false);
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
         source.sendFeedback(()->Text.literal("No Boss Fight Active"),false);
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
         context.getSource().sendFeedback(()->Text.literal("No Boss Fight Active"),false);
         return -1;
      }
      if(bossFight.getLeft() == BossFights.DRAGON){
         return DragonBossFight.beginBoss(context.getSource().getServer(),bossFight.getRight());
      }
      return -1;
   }
   
   
   public static int setItemName(CommandContext<ServerCommandSource> ctx, String name){
      try{
         ServerCommandSource src = ctx.getSource();
         if(!src.isExecutedByPlayer()){
            src.sendError(Text.literal("Must run command as a player"));
            return -1;
         }
         ServerPlayerEntity player = src.getPlayer();
         ItemStack handItem = player.getMainHandStack();
         handItem.set(DataComponentTypes.ITEM_NAME, TextUtils.parseString(name));
         
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int getItemName(CommandContext<ServerCommandSource> ctx){
      try{
         ServerCommandSource src = ctx.getSource();
         if(!src.isExecutedByPlayer()){
            src.sendError(Text.literal("Must run command as a player"));
            return -1;
         }
         ServerPlayerEntity player = src.getPlayer();
         ItemStack handItem = player.getMainHandStack();
         
         String feedback = TextUtils.textToString(handItem.getName());
         String copyText = feedback;
         src.sendFeedback(() -> Text.literal(feedback).styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to Copy Data"))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copyText))), false);
         
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int setItemLore(CommandContext<ServerCommandSource> ctx, int index, String lore){
      try{
         ServerCommandSource src = ctx.getSource();
         if(!src.isExecutedByPlayer()){
            src.sendError(Text.literal("Must run command as a player"));
            return -1;
         }
         ServerPlayerEntity player = src.getPlayer();
         ItemStack handItem = player.getMainHandStack();
         LoreComponent loreComp = handItem.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT);
         LoreComponent newLore;
         List<Text> lines = loreComp.styledLines();
         
         if(index < 0){
            newLore = loreComp.with(TextUtils.removeItalics(TextUtils.parseString(lore)));
         }else if(index < lines.size()){
            lines.set(index, TextUtils.removeItalics(TextUtils.parseString(lore)));
            newLore = new LoreComponent(lines);
         }else{
            int blankLines = index - lines.size();
            for(int i = 0; i < blankLines; i++){
               lines.add(Text.literal(""));
            }
            lines.add(TextUtils.removeItalics(TextUtils.parseString(lore)));
            newLore = new LoreComponent(lines);
         }
         
         handItem.set(DataComponentTypes.LORE,newLore);
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int getItemLore(CommandContext<ServerCommandSource> ctx, int index){
      try{
         ServerCommandSource src = ctx.getSource();
         if(!src.isExecutedByPlayer()){
            src.sendError(Text.literal("Must run command as a player"));
            return -1;
         }
         ServerPlayerEntity player = src.getPlayer();
         ItemStack handItem = player.getMainHandStack();
         LoreComponent loreComp = handItem.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT);
         List<Text> lines = loreComp.styledLines();
         
         if(index >= 0 && index < lines.size()){
            String feedback = TextUtils.textToString(lines.get(index));
            String copyText = feedback;
            src.sendFeedback(() -> Text.literal(feedback).styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to Copy Data"))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copyText))), false);
         }else{
            return 0;
         }
         
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int removeItemLore(CommandContext<ServerCommandSource> ctx, int index){
      try{
         ServerCommandSource src = ctx.getSource();
         if(!src.isExecutedByPlayer()){
            src.sendError(Text.literal("Must run command as a player"));
            return -1;
         }
         ServerPlayerEntity player = src.getPlayer();
         ItemStack handItem = player.getMainHandStack();
         LoreComponent loreComp = handItem.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT);
         List<Text> lines = loreComp.styledLines();
         
         if(index >= 0 && index < lines.size()){
            lines.remove(index);
         }else{
            return 0;
         }
         
         handItem.set(DataComponentTypes.LORE,new LoreComponent(lines));
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
}
