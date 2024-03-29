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
import net.borisshoes.arcananovum.bosses.dragon.guis.PuzzleGui;
import net.borisshoes.arcananovum.callbacks.ServerStartedCallback;
import net.borisshoes.arcananovum.callbacks.TickCallback;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.gui.arcanetome.LoreGui;
import net.borisshoes.arcananovum.gui.cache.CacheGui;
import net.borisshoes.arcananovum.items.AequalisScientia;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.NulMemento;
import net.borisshoes.arcananovum.recipes.arcana.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
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

import static net.borisshoes.arcananovum.ArcanaNovum.DEBUG_VALUE;
import static net.borisshoes.arcananovum.ArcanaNovum.devMode;
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
         MutableText feedback = Text.literal("")
               .append(target.getDisplayName())
               .append(Text.literal(" has ").formatted(Formatting.LIGHT_PURPLE))
               .append(Text.translatable(Integer.toString(adminPoints)).formatted(Formatting.AQUA,Formatting.BOLD))
               .append(Text.literal(" Bonus Skill Points").formatted(Formatting.LIGHT_PURPLE));
         source.sendFeedback(()->feedback, false);
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
      
      List<MagicItemUtils.MagicInvItem> magicInv = MagicItemUtils.getMagicInventory(player);
      for(MagicItemUtils.MagicInvItem invItem : magicInv){
         MagicItem magicItem = invItem.item;
         for(Pair<String,ItemStack> pair : invItem.getStacks()){
            String uuid = pair.getLeft();
            ItemStack stack = pair.getRight();
            count++;
   
            String storage = invItem.getShortContainerString();
            
            MutableText feedback = Text.literal("")
                  .append(Text.literal("(").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.translatable(storage).formatted(Formatting.BLUE))
                  .append(Text.literal(") ").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.literal("[").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.translatable(magicItem.getNameString()).formatted(Formatting.AQUA))
                  .append(Text.literal("] ID: ").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.translatable(uuid).formatted(Formatting.DARK_PURPLE));
            response.add(feedback.styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(stack))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid))));
            
            if(!uuids.add(uuid) || invItem.getStacks().size() < (invItem.getCount()/magicItem.getPrefItem().getCount())){
               MutableText duplicateWarning = Text.literal("")
                     .append(Text.literal("Duplicate: ").formatted(Formatting.RED))
                     .append(Text.translatable(magicItem.getNameString()).formatted(Formatting.AQUA))
                     .append(Text.literal(" ID: ").formatted(Formatting.LIGHT_PURPLE))
                     .append(Text.translatable(uuid).formatted(Formatting.DARK_PURPLE));
               response2.add(duplicateWarning);
            }
         }
      }
      
      MutableText feedback = Text.literal("")
            .append(player.getDisplayName())
            .append(Text.literal(" has ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.translatable(Integer.toString(count)).formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
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
            player.sendMessage(Text.literal("Hold a book to get data"),true);
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
         MutableText feedback = Text.literal("")
               .append(target.getDisplayName())
               .append(Text.literal(" has ").formatted(Formatting.LIGHT_PURPLE))
               .append(Text.translatable(Integer.toString(profile.getLevel())).formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
               .append(Text.literal(" levels (").formatted(Formatting.LIGHT_PURPLE))
               .append(Text.translatable(LevelUtils.getCurLevelXp(profile.getXP())+"/"+LevelUtils.nextLevelNewXp(profile.getLevel())).formatted(Formatting.AQUA))
               .append(Text.literal("). ").formatted(Formatting.LIGHT_PURPLE))
               .append(Text.translatable(Integer.toString(profile.getXP())).formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
               .append(Text.literal(" Total XP").formatted(Formatting.LIGHT_PURPLE));
         source.sendFeedback(()->feedback, false);
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
            
            lines.add("public static final MagicItem "+idName.toUpperCase()+" = ArcanaRegistry.register(new "+name+"());");
            
            lines.add("public class "+name+" extends MagicItem {");
            lines.add("");
            lines.add("private static final String TXT = \"item/"+idName+"\";");
            lines.add("");
            lines.add("public "+name+"(){");
            
            lines.add("id = \""+idName+"\";");
            lines.add("name = \""+fullName+"\";");
            lines.add("rarity = MagicRarity.;");
            lines.add("categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.};");
            lines.add("vanillaItem = Items."+Registries.ITEM.getId(stack.getItem()).getPath().toUpperCase()+";");
            lines.add("item = new "+name+"Item(new FabricItemSettings().maxCount(1).fireproof());");
            lines.add("models = new ArrayList<>();");
            lines.add("models.add(new Pair<>(vanillaItem,TXT));");
            lines.add("");
            lines.add("ItemStack stack = new ItemStack(item);");
            lines.add("NbtCompound tag = stack.getOrCreateNbt();");
            lines.add("NbtCompound display = new NbtCompound();");
            lines.add("NbtList enchants = new NbtList();");
            lines.add("enchants.add(new NbtCompound()); // Gives enchant glow with no enchants");
            if(display != null){
               lines.add("display.putString(\"Name\",\""+display.getString("Name").replaceAll("\"","\\\\\"")+"\");");
            }
            lines.add("tag.put(\"display\",display);");
            lines.add("tag.put(\"Enchantments\",enchants);");
            if(stack.isOf(Items.TIPPED_ARROW)){
               lines.add("tag.putInt(\"CustomPotionColor\","+tag.getInt("CustomPotionColor")+");");
               lines.add("tag.putInt(\"HideFlags\",255);");
               lines.add("stack.setCount(64);");
            }
            lines.add("buildItemLore(stack, ArcanaNovum.SERVER);");
            lines.add("");
            lines.add("setBookLore(makeLore());");
            lines.add("//setRecipe(makeRecipe());");
            lines.add("prefNBT = addMagicNbt(tag);");
            lines.add("");
            lines.add("stack.setNbt(prefNBT);");
            lines.add("prefItem = stack;");
            lines.add("}");
            lines.add("");
            
            lines.add("@Override");
            lines.add("public NbtList getItemLore(@Nullable ItemStack itemStack){");
            lines.add("NbtList loreList = new NbtList();");
            if(display != null){
               NbtList lore = display.getList("Lore",NbtElement.STRING_TYPE);
               for(int i = 0; i < lore.size(); i++){
                  lines.add("loreList.add(NbtString.of(\""+lore.getString(i).replaceAll("\"","\\\\\"")+"\"));");
               }
            }
            lines.add("return loreList;");
            lines.add("}");
   
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
            
            lines.add("");
            lines.add("public class "+name+"Item extends MagicPolymerItem {");
            lines.add("public "+name+"Item(Settings settings){");
            lines.add("super(getThis(),settings);");
            lines.add("}");
            lines.add("");
            lines.add("@Override");
            lines.add("public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){");
            lines.add("return ArcanaRegistry.MODELS.get(TXT).value();");
            lines.add("}");
            lines.add("");
            lines.add("@Override");
            lines.add("public ItemStack getDefaultStack(){");
            lines.add("return prefItem;");
            lines.add("}");
            lines.add("");
            lines.add("@Override");
            lines.add("public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){");
            lines.add("if(!MagicItemUtils.isMagic(stack)) return;");
            lines.add("if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;");
            lines.add("");
            lines.add("}");
            lines.add("");
            lines.add("@Override");
            lines.add("public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {");
            lines.add("");
            lines.add("return TypedActionResult.success(playerEntity.getStackInHand(hand));");
            lines.add("}");
            lines.add("}");
            
            lines.add("}");
            
            for(String line : lines){
               out.println(line);
            }
            out.close();
         }else{
            player.sendMessage(Text.literal("Hold an item to get data"),true);
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
                     lines.add("GenericMagicIngredient "+letter+" = new GenericMagicIngredient(ArcanaRegistry."+idName+","+stack.getCount()+");");
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
            lines.add("return new MagicItemRecipe(ingredients,new ForgeRequirement());");
            
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
         
         PuzzleGui gui = new PuzzleGui(ScreenHandlerType.GENERIC_9X6,player,null);
         gui.buildPuzzle();
         gui.open();
         
      } catch (Exception e) {
         e.printStackTrace();
      }
      return 0;
   }
   
   public static int testCommand(CommandContext<ServerCommandSource> objectCommandContext, int num) {
      if (!devMode)
         return 0;
      try {
         ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
         
         DEBUG_VALUE = num;
         
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
   
   public static CompletableFuture<Suggestions> getItemSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder){
      String start = builder.getRemaining().toLowerCase();
      Set<String> items = ArcanaRegistry.MAGIC_ITEMS.keySet();
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
      MagicItem magicItem;
      if(src.isExecutedByPlayer() && src.getPlayer() != null){
         ItemStack handItem = src.getPlayer().getMainHandStack();
         magicItem = MagicItemUtils.identifyItem(handItem);
      }else{
         magicItem = null;
      }
      Set<String> augments = ArcanaAugments.registry.keySet();
      if(magicItem != null){
         augments = augments.stream().filter(s -> ArcanaAugments.registry.get(s).getMagicItem().getId().equals(magicItem.getId())).collect(Collectors.toSet());
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
            PLAYER_DATA.get(player).removeAugment(id);
            src.sendMessage(Text.literal("Successfully removed "+augment.name+" from ").append(player.getDisplayName()));
         }else{
            PLAYER_DATA.get(player).setAugmentLevel(id,level);
            src.sendMessage(Text.literal("Successfully set "+augment.name+" to level "+level+" for ").append(player.getDisplayName()));
         }
         return 1;
      }catch(Exception e){
         e.printStackTrace();
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
         MagicItem magicItem = MagicItemUtils.identifyItem(handItem);
         
         if(augment == null){
            src.sendError(Text.literal("That is not a valid Augment"));
            return -1;
         }
         if(level < 1 || level > augment.getTiers().length){
            src.sendError(Text.literal("Level out of bounds (1-"+augment.getTiers().length+")"));
            return -1;
         }
         if(magicItem == null || !magicItem.getId().equals(augment.getMagicItem().getId())){
            src.sendError(Text.literal("Player is not holding a valid Magic Item"));
            return -1;
         }
         if(ArcanaAugments.isIncompatible(handItem,id)){
            src.sendError(Text.literal("This augment is incompatible with existing augments"));
            return -1;
         }
         if(ArcanaAugments.applyAugment(handItem,id,level,false)){
            src.sendMessage(Text.literal("Successfully applied "+augment.name+" at level "+level+" for ").append(player.getDisplayName()));
            return 1;
         }else{
            src.sendError(Text.literal("Couldn't apply augment (Cannot downgrade existing augments)"));
            return -1;
         }
      }catch(Exception e){
         e.printStackTrace();
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
            feedback.append(Text.literal(achievement.name).formatted(Formatting.AQUA));
            feedback.append(Text.literal("] to ").formatted(Formatting.LIGHT_PURPLE));
         }else{
            feedback.append(Text.literal("Revoked Achievement [").formatted(Formatting.LIGHT_PURPLE));
            feedback.append(Text.literal(achievement.name).formatted(Formatting.AQUA));
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
         e.printStackTrace();
         return 0;
      }
   }
   
   public static int getAchievement(CommandContext<ServerCommandSource> ctx, String id, ServerPlayerEntity target){
      try{
         ServerCommandSource source = ctx.getSource();
         IArcanaProfileComponent profile = PLAYER_DATA.get(target);
         ArcanaAchievement baseAch = ArcanaAchievements.registry.get(id);
         if(baseAch == null){
            source.sendError(Text.literal("That is not a valid Achievement"));
            return -1;
         }
         ArcanaAchievement profAchieve = profile.getAchievement(baseAch.getMagicItem().getId(),id);
         ArcanaAchievement achieve = profAchieve == null ? baseAch : profAchieve;
         MutableText[] response = achieve.getStatusDisplay(target);
         
   
         MutableText header = Text.literal("")
               .append(target.getDisplayName().copy().append("'s"))
               .append(Text.literal(" progress towards [").formatted(Formatting.LIGHT_PURPLE))
               .append(Text.literal(achieve.name).formatted(Formatting.AQUA))
               .append(Text.literal("]: ").formatted(Formatting.LIGHT_PURPLE));
         
         source.sendFeedback(()->header,false);
         if(response == null) return 0;
         for(MutableText mutableText : response){
            source.sendFeedback(()->mutableText, false);
         }
         return 1;
      }catch(Exception e){
         e.printStackTrace();
         return 0;
      }
   }
   
   public static int createItems(ServerCommandSource source, String id, Collection<ServerPlayerEntity> targets){
      try{
         MagicItem magicItem = MagicItemUtils.getItemFromId(id);
         if(magicItem == null){
            source.sendMessage(Text.literal("Invalid Magic Item ID: "+id).formatted(Formatting.RED, Formatting.ITALIC));
            return 0;
         }
   
         for(ServerPlayerEntity target : targets){
            ItemStack item = magicItem.addCrafter(magicItem.getNewItem(),target.getUuidAsString(),true,source.getServer());
   
            if(item == null){
               source.sendMessage(Text.literal("No Preferred Item Found For: "+magicItem.getNameString()).formatted(Formatting.RED, Formatting.ITALIC));
               return 0;
            }else{
               NbtCompound magicTag = item.getNbt().getCompound("arcananovum");
               String uuid = magicTag.getString("UUID");
               source.sendFeedback(() -> Text.literal("Generated New: "+magicItem.getNameString()+" with UUID "+uuid).formatted(Formatting.GREEN), false);
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
            source.sendMessage(Text.literal("Invalid Magic Item ID: "+id).formatted(Formatting.RED, Formatting.ITALIC));
            return 0;
         }
         ItemStack item = magicItem.addCrafter(magicItem.getNewItem(),source.getPlayerOrThrow().getUuidAsString(),true,source.getServer());
         
         if(item == null){
            source.sendMessage(Text.literal("No Preferred Item Found For: "+magicItem.getNameString()).formatted(Formatting.RED, Formatting.ITALIC));
            return 0;
         }else{
            NbtCompound magicTag = item.getNbt().getCompound("arcananovum");
            String uuid = magicTag.getString("UUID");
            source.sendMessage(Text.literal("Generated New: "+magicItem.getNameString()+" with UUID "+uuid).formatted(Formatting.GREEN));
            source.getPlayerOrThrow().giveItemStack(item);
            return 1;
         }
      }catch(Exception e){
         e.printStackTrace();
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
      if(!MagicItemUtils.isMagic(player.getMainHandStack())){
         source.sendFeedback(()->Text.literal("You can only show off Magic Items"), false);
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
   
   
}
