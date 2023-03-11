package net.borisshoes.arcananovum;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import eu.pb4.sgui.api.elements.*;
import net.borisshoes.arcananovum.bosses.BossFight;
import net.borisshoes.arcananovum.bosses.BossFights;
import net.borisshoes.arcananovum.bosses.dragon.DragonBossFight;
import net.borisshoes.arcananovum.bosses.dragon.guis.PuzzleGui;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.gui.arcanetome.LoreGui;
import net.borisshoes.arcananovum.gui.cache.CacheGui;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.recipes.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.borisshoes.arcananovum.Arcananovum.*;
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
   
   public static int skillpointsCommand(CommandContext<ServerCommandSource> ctx, Collection<ServerPlayerEntity> targets, int amount, boolean set){
      try{
         ServerCommandSource source = ctx.getSource();
      
         for (ServerPlayerEntity player : targets) {
            IArcanaProfileComponent profile = PLAYER_DATA.get(player);
            
            NbtInt pointsEle = (NbtInt) profile.getMiscData("adminSkillPoints");
            int oldPoints = pointsEle == null ? 0 : pointsEle.intValue();
            int newPoints = set ? amount : amount + oldPoints;
            profile.addMiscData("adminSkillPoints", NbtInt.of(newPoints));
         }
      
         if(targets.size() == 1 && set){
            source.sendFeedback(Text.translatable("Set Bonus Skill Points to "+amount+" for ").formatted(Formatting.LIGHT_PURPLE).append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() == 1 && !set){
            source.sendFeedback(Text.translatable("Gave "+amount+" Bonus Skill Points to ").formatted(Formatting.LIGHT_PURPLE).append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() != 1 && set){
            source.sendFeedback(Text.translatable("Set Bonus Skill Points to "+amount+" for " + targets.size() + " players").formatted(Formatting.LIGHT_PURPLE), true);
         }else if(targets.size() != 1 && !set){
            source.sendFeedback(Text.translatable("Gave "+amount+" Bonus Skill Points to " + targets.size() + " players").formatted(Formatting.LIGHT_PURPLE), true);
         }
      
         return targets.size();
      }catch(Exception e){
         e.printStackTrace();
         return 0;
      }
   }
   
   public static int skillpointsCommandQuery(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity target){
      try{
         ServerCommandSource source = ctx.getSource();
         IArcanaProfileComponent profile = PLAYER_DATA.get(target);
         NbtInt pointsEle = (NbtInt) profile.getMiscData("adminSkillPoints");
         int adminPoints = pointsEle == null ? 0 : pointsEle.intValue();
         MutableText feedback = Text.translatable("")
               .append(target.getDisplayName())
               .append(Text.translatable(" has ").formatted(Formatting.LIGHT_PURPLE))
               .append(Text.translatable(Integer.toString(adminPoints)).formatted(Formatting.AQUA,Formatting.BOLD))
               .append(Text.translatable(" Bonus Skill Points").formatted(Formatting.LIGHT_PURPLE));
         source.sendFeedback(feedback, false);
         return 1;
      }catch(Exception e){
         e.printStackTrace();
         return 0;
      }
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
   
   //TODO: Quiver Support + Better Scalable Nesting for future item holding items
   public static int uuidCommand(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player){
      ServerCommandSource source = ctx.getSource();
      ArrayList<MutableText> response = new ArrayList<>();
      ArrayList<MutableText> response2 = new ArrayList<>();
      Set<String> uuids = new HashSet<>();
      int count = 0;
      
      List<MagicItemUtils.MagicInvItem> magicInv = MagicItemUtils.getMagicInventory(player);
      for(MagicItemUtils.MagicInvItem invItem : magicInv){
         MagicItem magicItem = invItem.item;
         for(String uuid : invItem.getUuids()){
            count++;
   
            String storage = invItem.eChest && invItem.shulker ? "EC+SB" : invItem.eChest ? "EC" : invItem.shulker ? "SB" : "Inv";
            
            MutableText feedback = Text.translatable("")
                  .append(Text.translatable("(").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.translatable(storage).formatted(Formatting.BLUE))
                  .append(Text.translatable(") ").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.translatable("[").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.translatable(magicItem.getName()).formatted(Formatting.AQUA))
                  .append(Text.translatable("] ID: ").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.translatable(uuid).formatted(Formatting.DARK_PURPLE));
            response.add(feedback.styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid))));
            if(!uuids.add(uuid) || invItem.getUuids().size() < (invItem.getCount()/magicItem.getPrefItem().getCount())){
               MutableText duplicateWarning = Text.translatable("")
                     .append(Text.translatable("Duplicate: ").formatted(Formatting.RED))
                     .append(Text.translatable(magicItem.getName()).formatted(Formatting.AQUA))
                     .append(Text.translatable(" ID: ").formatted(Formatting.LIGHT_PURPLE))
                     .append(Text.translatable(uuid).formatted(Formatting.DARK_PURPLE));
               response2.add(duplicateWarning);
            }
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
   
   public static int getItemData(CommandContext<ServerCommandSource> objectCommandContext, String name) {
      if (!devMode)
         return 0;
      try {
         ServerPlayerEntity player = objectCommandContext.getSource().getPlayerOrThrow();
         ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
         if(!stack.isEmpty()){
            NbtCompound tag = stack.getNbt();
            NbtCompound display = tag.getCompound("display");
            
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
            
            lines.add("public static final MagicItem "+idName.toUpperCase()+" = MagicItems.register(\""+idName+"\", new "+name+"());");
            
            lines.add("public class "+name+" extends MagicItem {");
            lines.add("");
            lines.add("public "+name+"(){");
            
            lines.add("id = \""+idName+"\";");
            lines.add("name = \""+fullName+"\";");
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
            if(stack.isOf(Items.TIPPED_ARROW)){
               lines.add("tag.putInt(\"CustomPotionColor\","+tag.getInt("CustomPotionColor")+");");
               lines.add("tag.putInt(\"HideFlags\",127);");
               lines.add("item.setCount(64);");
            }
            lines.add("");
            lines.add("setBookLore(makeLore());");
            lines.add("//setRecipe(makeRecipe());");
            lines.add("prefNBT = addMagicNbt(tag);");
            lines.add("");
            lines.add("item.setNbt(prefNBT);");
            lines.add("prefItem = item;");
            lines.add("}");
            lines.add("");
   
            lines.add("");
            lines.add("");
            lines.add("//TODO: Make Recipe");
            lines.add("private MagicItemRecipe makeRecipe(){");
            lines.add("return null;");
            lines.add("}");
            lines.add("");
            lines.add("//TODO: Make Lore");
            lines.add("private List<String> makeLore(){");
            lines.add("ArrayList<String> list = new ArrayList<>();");
            lines.add("list.add(\"{\\\"text\\\":\\\"TODO\\\"}\");");
            lines.add("return list;");
            lines.add("}");
            lines.add("}");
            
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
         ServerWorld world = player.getWorld();
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
            
            Pair<MagicItemIngredient,Character>[][] ingredients = new Pair[5][5];
            HashMap<Character,ArrayList<String>> lineSet = new HashMap<>();
            
            for(int i = 0; i < 5; i++){
               for(int j = 0; j < 5; j++){
                  ItemStack stack = chestInventory.getStack(i*9+j);
                  
                  MagicItem magicItem = MagicItemUtils.identifyItem(stack);
                  MagicItemIngredient ingred;
                  ArrayList<String> lines = new ArrayList<>();
                  char letter = (char) ('a' + (i * 5 + j));
                  
                  if(magicItem != null){
                     ingred = new GenericMagicIngredient(magicItem,stack.getCount());
                     String idName = magicItem.getId().toUpperCase();
                     lines.add("GenericMagicIngredient "+letter+" = new GenericMagicIngredient(MagicItems."+idName+","+stack.getCount()+");");
                  }else if(stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION)){
                     String idName = Registries.ITEM.getId(stack.getItem()).getPath().toUpperCase();
                     Potion potion = PotionUtil.getPotion(stack);
                     ItemStack potionStack = new ItemStack(stack.getItem());
                     ingred = new MagicItemIngredient(stack.getItem(),stack.getCount(), PotionUtil.setPotion(potionStack, potion).getNbt());
                     
                     lines.add("ItemStack potion"+(i * 5 + j)+" = new ItemStack(Items."+idName+");");
                     lines.add("MagicItemIngredient "+letter+" = new MagicItemIngredient(Items."+idName+","+stack.getCount()+", PotionUtil.setPotion(potion"+(i * 5 + j)+", Potions."+Registries.POTION.getId(potion).getPath().toUpperCase()+").getNbt());");
                  }else if(stack.isOf(Items.ENCHANTED_BOOK)){
                     ItemStack enchantedItem = new ItemStack(Items.ENCHANTED_BOOK);
                     lines.add("ItemStack enchantedBook"+(i * 5 + j)+" = new ItemStack(Items.ENCHANTED_BOOK);");
   
                     Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
                     for(Map.Entry<Enchantment, Integer> entry : enchants.entrySet()){
                        EnchantedBookItem.addEnchantment(enchantedItem,new EnchantmentLevelEntry(entry.getKey(),entry.getValue()));
                        lines.add("EnchantedBookItem.addEnchantment(enchantedBook"+(i * 5 + j)+",new EnchantmentLevelEntry(Enchantments."+ Registries.ENCHANTMENT.getId(entry.getKey()).getPath().toUpperCase()+","+entry.getValue()+"));");
                     }
                     ingred = new MagicItemIngredient(Items.ENCHANTED_BOOK,stack.getCount(),enchantedItem.getNbt());
   
                     lines.add("MagicItemIngredient "+letter+" = new MagicItemIngredient(Items.ENCHANTED_BOOK,"+stack.getCount()+",enchantedBook"+(i * 5 + j)+".getNbt());");
                  }else if(stack.hasEnchantments()){
                     ItemStack enchantedItem = new ItemStack(stack.getItem());
                     String idName = Registries.ITEM.getId(stack.getItem()).getPath().toUpperCase();
                     String line = "MagicItemIngredient "+letter+" = new MagicItemIngredient(Items."+idName+","+stack.getCount()+", MagicItemIngredient.getEnchantNbt(";
   
                     Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
                     for(Map.Entry<Enchantment, Integer> entry : enchants.entrySet()){
                        enchantedItem.addEnchantment(entry.getKey(),entry.getValue());
                        line += "new Pair(Enchantments."+Registries.ENCHANTMENT.getId(entry.getKey()).getPath().toUpperCase()+","+entry.getValue()+"),";
                     }
                     ingred = new MagicItemIngredient(Items.ENCHANTED_BOOK,stack.getCount(),enchantedItem.getNbt());
                     line = line.substring(0,line.length()-1) + "));";
                     lines.add(line);
                  }else if(stack.isEmpty()){
                     ingred = MagicItemIngredient.EMPTY;
                     lines.add("MagicItemIngredient "+letter+" = MagicItemIngredient.EMPTY;");
                  }else{
                     ingred = new MagicItemIngredient(stack.getItem(),stack.getCount(),null);
                     String idName = Registries.ITEM.getId(stack.getItem()).getPath().toUpperCase();
                     lines.add("MagicItemIngredient "+letter+" = new MagicItemIngredient(Items."+idName+","+stack.getCount()+",null);");
                  }
                  
                  boolean match = false;
                  for(int m = 0; m <= i; m++){
                     if(match) break;
                     for(int n = 0; n < (m == i ? j : 5); n++){
                        Pair<MagicItemIngredient,Character> prev = ingredients[m][n];
                        MagicItemIngredient prevIng = prev.getLeft();
                        
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
   
            String path = "C:\\Users\\Boris\\Desktop\\itemrecipe.txt";
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, false)));
            ArrayList<String> lines = new ArrayList<>();
   
            for(Map.Entry<Character, ArrayList<String>> entry : lineSet.entrySet()){
               lines.addAll(entry.getValue());
            }
            lines.add("");
            lines.add("MagicItemIngredient[][] ingredients = {");
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
            lines.add("return new MagicItemRecipe(ingredients);");
            
            /*
            MagicItemIngredient[][] ingredients = {
               {c,e,x,e,c},
               {e,m,x,m,e},
               {x,x,h,x,x},
               {e,m,x,m,e},
               {c,e,x,e,c}};
            return new MagicItemRecipe(ingredients);
            */
            
            for(String line : lines){
               out.println(line);
            }
            out.close();
         }
         
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
         //player.getWorld().syncWorldEvent(3007, pos, 0);
         //player.getWorld().emitGameEvent(GameEvent.SHRIEK, pos, GameEvent.Emitter.of(player));
         //player.getWorld().spawnParticles(ParticleTypes.SHRIEK,player.getX(),player.getY(),player.getZ(),1,0,0,0,1);
         //SoundUtils.playSongToPlayer(player, SoundEvents.PARTICLE_SOUL_ESCAPE);
      } catch (Exception e) {
         e.printStackTrace();
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
         e.printStackTrace();
         return -1;
      }
   }
   
   public static CompletableFuture<Suggestions> getItemSuggestions(CommandContext<ServerCommandSource> serverCommandSourceCommandContext, SuggestionsBuilder builder){
      String start = builder.getRemaining().toLowerCase();
      Set<String> items = MagicItems.registry.keySet();
      items.stream().filter(s -> s.startsWith(start)).forEach(builder::suggest);
      return builder.buildFuture();
   }
   
   public static int createItems(ServerCommandSource source, String id, Collection<ServerPlayerEntity> targets){
      try{
         MagicItem magicItem = MagicItemUtils.getItemFromId(id);
         if(magicItem == null){
            source.getPlayerOrThrow().sendMessage(Text.translatable("Invalid Magic Item ID: "+id).formatted(Formatting.RED, Formatting.ITALIC), false);
            return 0;
         }
   
         for(ServerPlayerEntity target : targets){
            ItemStack item = magicItem.addCrafter(magicItem.getNewItem(),target.getUuidAsString(),true,source.getServer());
   
            if(item == null){
               source.getPlayerOrThrow().sendMessage(Text.translatable("No Preferred Item Found For: "+magicItem.getName()).formatted(Formatting.RED, Formatting.ITALIC), false);
               return 0;
            }else{
               NbtCompound magicTag = item.getNbt().getCompound("arcananovum");
               String uuid = magicTag.getString("UUID");
               source.getPlayerOrThrow().sendMessage(Text.translatable("Generated New: "+magicItem.getName()+" with UUID "+uuid).formatted(Formatting.GREEN), false);
               target.giveItemStack(item);
            }
         }
         return 1;
      }catch(Exception e){
         e.printStackTrace();
         return -1;
      }
   }
   
   public static int createItem(ServerCommandSource source, String id) throws CommandSyntaxException{
      try{
         MagicItem magicItem = MagicItemUtils.getItemFromId(id);
         if(magicItem == null){
            source.getPlayerOrThrow().sendMessage(Text.translatable("Invalid Magic Item ID: "+id).formatted(Formatting.RED, Formatting.ITALIC), false);
            return 0;
         }
         ItemStack item = magicItem.addCrafter(magicItem.getNewItem(),source.getPlayerOrThrow().getUuidAsString(),true,source.getServer());
         
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
   
   public static int bossResetAbilities(CommandContext<ServerCommandSource> context, boolean doAbility){
      ServerCommandSource source = context.getSource();
      Pair<BossFights, NbtCompound> bossFight = BOSS_FIGHT.get(source.getServer().getWorld(World.END)).getBossFight();
      if(bossFight == null){
         source.sendFeedback(Text.translatable("No Boss Fight Active"),false);
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
         source.sendFeedback(Text.translatable("No Boss Fight Active"),false);
         return -1;
      }
      if(bossFight.getLeft() == BossFights.DRAGON){
         return DragonBossFight.forceLairAction(source.getServer(),context.getSource());
      }
      return -1;
   }
   
   public static int testBoss(CommandContext<ServerCommandSource> context){
      ServerCommandSource source = context.getSource();
      devPrint("Test Boss");
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
