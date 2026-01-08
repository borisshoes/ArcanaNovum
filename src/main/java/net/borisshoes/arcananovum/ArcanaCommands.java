package net.borisshoes.arcananovum;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.bosses.BossFight;
import net.borisshoes.arcananovum.bosses.BossFights;
import net.borisshoes.arcananovum.bosses.dragon.DragonBossFight;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.datastorage.BossFightData;
import net.borisshoes.arcananovum.gui.arcanetome.*;
import net.borisshoes.arcananovum.recipes.RecipeManager;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTask;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.callbacks.ItemReturnTimerCallback;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.borislib.utils.TextUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.server.players.ProfileResolver;
import net.minecraft.util.Tuple;
import net.minecraft.util.Util;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.function.TriConsumer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.borisshoes.arcananovum.ArcanaNovum.*;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.BOSS_FIGHT;
import static net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui.getIngredStr;

public class ArcanaCommands {
   
   
   public static int openGuideBook(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException{
      CommandSourceStack source = ctx.getSource();
      if(!source.isPlayer() || source.getPlayer() == null){
         source.sendSuccess(()-> Component.literal("Command must be executed by a player"), false);
         return -1;
      }
      ServerPlayer player = ctx.getSource().getPlayerOrException();
      BookElementBuilder bookBuilder = getGuideBook();
      LoreGui loreGui = new LoreGui(player,bookBuilder,null);
      loreGui.open();
      return 1;
   }
   
   public static int skillpointsCommand(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets, int amount, boolean set){
      try{
         CommandSourceStack source = ctx.getSource();
      
         for (ServerPlayer player : targets){
            ArcanaPlayerData profile = ArcanaNovum.data(player);
            
            IntTag pointsEle = (IntTag) profile.getMiscData(ArcanaPlayerData.ADMIN_SKILL_POINTS_TAG);
            int oldPoints = pointsEle == null ? 0 : pointsEle.intValue();
            int newPoints = set ? amount : amount + oldPoints;
            profile.addMiscData(ArcanaPlayerData.ADMIN_SKILL_POINTS_TAG, IntTag.valueOf(newPoints));
         }
      
         if(targets.size() == 1 && set){
            source.sendSuccess(()-> Component.literal("Set Bonus Skill Points to "+amount+" for ").withStyle(ChatFormatting.LIGHT_PURPLE).append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() == 1 && !set){
            source.sendSuccess(()-> Component.literal("Gave "+amount+" Bonus Skill Points to ").withStyle(ChatFormatting.LIGHT_PURPLE).append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() != 1 && set){
            source.sendSuccess(()-> Component.literal("Set Bonus Skill Points to "+amount+" for " + targets.size() + " players").withStyle(ChatFormatting.LIGHT_PURPLE), true);
         }else if(targets.size() != 1 && !set){
            source.sendSuccess(()-> Component.literal("Gave "+amount+" Bonus Skill Points to " + targets.size() + " players").withStyle(ChatFormatting.LIGHT_PURPLE), true);
         }
      
         return targets.size();
      }catch(Exception e){
         log(2,e.toString());
         return 0;
      }
   }
   
   public static int skillpointsCommandQuery(CommandContext<CommandSourceStack> ctx, ServerPlayer target){
      try{
         CommandSourceStack source = ctx.getSource();
         ArcanaPlayerData profile = ArcanaNovum.data(target);
         IntTag pointsEle = (IntTag) profile.getMiscData(ArcanaPlayerData.ADMIN_SKILL_POINTS_TAG);
         int adminPoints = pointsEle == null ? 0 : pointsEle.intValue();
         MutableComponent feedback = Component.literal("")
               .append(target.getDisplayName())
               .append(Component.literal(" has ").withStyle(ChatFormatting.LIGHT_PURPLE))
               .append(Component.literal(Integer.toString(adminPoints)).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
               .append(Component.literal(" Bonus Skill Points").withStyle(ChatFormatting.LIGHT_PURPLE));
         source.sendSuccess(()->feedback, false);
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return 0;
      }
   }
   
   public static int xpCommand(CommandContext<CommandSourceStack> ctx, Collection<? extends ServerPlayer> targets, int amount, boolean set, boolean points){
      try{
         CommandSourceStack source = ctx.getSource();
         
         for (ServerPlayer player : targets){
            ArcanaPlayerData profile = ArcanaNovum.data(player);
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
            source.sendSuccess(()-> Component.literal("Set Arcana XP to "+amount+" for ").withStyle(ChatFormatting.LIGHT_PURPLE).append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() == 1 && set && !points){
            source.sendSuccess(()-> Component.literal("Set Arcana Level to "+amount+" for ").withStyle(ChatFormatting.LIGHT_PURPLE).append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() == 1 && !set && points){
            source.sendSuccess(()-> Component.literal("Gave "+amount+" Arcana XP to ").withStyle(ChatFormatting.LIGHT_PURPLE).append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() == 1 && !set && !points){
            source.sendSuccess(()-> Component.literal("Gave "+amount+" Arcana Levels to ").withStyle(ChatFormatting.LIGHT_PURPLE).append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() != 1 && set && points){
            source.sendSuccess(()-> Component.literal("Set Arcana XP to "+amount+" for " + targets.size() + " players").withStyle(ChatFormatting.LIGHT_PURPLE), true);
         }else if(targets.size() != 1 && set && !points){
            source.sendSuccess(()-> Component.literal("Set Arcana Level to "+amount+" for " + targets.size() + " players").withStyle(ChatFormatting.LIGHT_PURPLE), true);
         }else if(targets.size() != 1 && !set && points){
            source.sendSuccess(()-> Component.literal("Gave "+amount+" Arcana XP to " + targets.size() + " players").withStyle(ChatFormatting.LIGHT_PURPLE), true);
         }else if(targets.size() != 1 && !set && !points){
            source.sendSuccess(()-> Component.literal("Gave "+amount+" Arcana Levels to " + targets.size() + " players").withStyle(ChatFormatting.LIGHT_PURPLE), true);
         }
         
         return targets.size();
      }catch(Exception e){
         log(2,e.toString());
         return 0;
      }
   }
   
   public static int uuidCommand(CommandContext<CommandSourceStack> ctx, ServerPlayer player){
      CommandSourceStack source = ctx.getSource();
      ArrayList<MutableComponent> response = new ArrayList<>();
      ArrayList<MutableComponent> response2 = new ArrayList<>();
      Set<String> uuids = new HashSet<>();
      int count = 0;
      
      List<ArcanaItemUtils.ArcanaInvItem> arcanaInv = ArcanaItemUtils.getArcanaInventory(player);
      for(ArcanaItemUtils.ArcanaInvItem invItem : arcanaInv){
         ArcanaItem arcanaItem = invItem.item;
         for(Tuple<String, ItemStack> pair : invItem.getStacks()){
            String uuid = pair.getA();
            ItemStack stack = pair.getB();
            count++;
   
            String storage = invItem.getShortContainerString();
            
            MutableComponent feedback = Component.literal("")
                  .append(Component.literal("(").withStyle(ChatFormatting.LIGHT_PURPLE))
                  .append(Component.literal(storage).withStyle(ChatFormatting.BLUE))
                  .append(Component.literal(") ").withStyle(ChatFormatting.LIGHT_PURPLE))
                  .append(Component.literal("[").withStyle(ChatFormatting.LIGHT_PURPLE))
                  .append(arcanaItem.getTranslatedName().withStyle(ChatFormatting.AQUA))
                  .append(Component.literal("] ID: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                  .append(Component.literal(uuid).withStyle(ChatFormatting.DARK_PURPLE));
            response.add(feedback.withStyle(s -> s.withHoverEvent(new HoverEvent.ShowItem(stack)).withClickEvent(new ClickEvent.CopyToClipboard(uuid))));
            
            if(!uuids.add(uuid) || invItem.getStacks().size() < (invItem.getCount()/ arcanaItem.getPrefItem().getCount())){
               MutableComponent duplicateWarning = Component.literal("")
                     .append(Component.literal("Duplicate: ").withStyle(ChatFormatting.RED))
                     .append(arcanaItem.getTranslatedName().withStyle(ChatFormatting.AQUA))
                     .append(Component.literal(" ID: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                     .append(Component.literal(uuid).withStyle(ChatFormatting.DARK_PURPLE));
               response2.add(duplicateWarning);
            }
         }
      }
      
      MutableComponent feedback = Component.literal("")
            .append(player.getDisplayName())
            .append(Component.literal(" has ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(Integer.toString(count)).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
            .append(Component.literal(" items.").withStyle(ChatFormatting.LIGHT_PURPLE));
      source.sendSuccess(()-> Component.literal(""),false);
      source.sendSuccess(()->feedback,false);
      source.sendSuccess(()-> Component.literal("================================").withStyle(ChatFormatting.LIGHT_PURPLE),false);
      for(MutableComponent r : response){
         source.sendSuccess(()->r,false);
      }
      source.sendSuccess(()-> Component.literal("================================").withStyle(ChatFormatting.LIGHT_PURPLE),false);
      for(MutableComponent r : response2){
         source.sendSuccess(()->r,false);
      }
      return count;
   }
   
   public static int getBookData(CommandContext<CommandSourceStack> objectCommandContext){
      if(!DEV_MODE)
         return 0;
      try {
         ServerPlayer player = objectCommandContext.getSource().getPlayerOrException();
         ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
         ArrayList<String> lines = new ArrayList<>();
         if(stack.is(Items.WRITTEN_BOOK)){
            WrittenBookContent comp = stack.getOrDefault(DataComponents.WRITTEN_BOOK_CONTENT, WrittenBookContent.EMPTY);
            for(Component page : comp.getPages(false)){
               lines.add(page.getString());
            }
         }else if(stack.is(Items.WRITABLE_BOOK)){
            WritableBookContent comp = stack.getOrDefault(DataComponents.WRITABLE_BOOK_CONTENT, WritableBookContent.EMPTY);
            for(Filterable<String> page : comp.pages()){
               lines.add(page.get(false));
            }
         }
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(player.getOffhandItem());
         
         if(lines.isEmpty()){
            player.displayClientMessage(Component.literal("Hold a written book to get data"),true);
         }else{
            Optional<Optional<Path>> outPathOpt = FabricLoader.getInstance().getModContainer(MOD_ID).map(container -> container.findPath("data/"+MOD_ID+"/datagen/"));
            if(outPathOpt.isEmpty() || outPathOpt.get().isEmpty()){
               return -1;
            }
            
            String path = outPathOpt.get().get() + "\\" + "bookdata.txt";
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, false)));
            List<List<Component>> loreData = new ArrayList<>();
            
            boolean first = true;
            for(String line : lines){
               if(arcanaItem != null){
                  String displayName = TextUtils.textToCode(Component.literal(arcanaItem.getNameString()).toFlatList(arcanaItem.getDisplayName().getStyle()).getFirst()).replace(";","");
                  if(first){
                     loreData.add(List.of(arcanaItem.getDisplayName(), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(arcanaItem.getRarity(),false)), Component.literal("\n"+line).withStyle(ChatFormatting.BLACK)));
                     out.println("list.add(List.of("+displayName+",Text.literal(\"\\nRarity: \").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal(\"\\n"+line.replace("\n","\\n")+"\").formatted(Formatting.BLACK)));");
                     first = false;
                  }else{
                     loreData.add(List.of(arcanaItem.getDisplayName(), Component.literal("\n"+line).withStyle(ChatFormatting.BLACK)));
                     out.println("list.add(List.of("+displayName+",Text.literal(\"\\n"+line.replace("\n","\\n")+"\").formatted(Formatting.BLACK)));");
                  }
               }else{
                  loreData.add(List.of(Component.literal(line).withStyle(ChatFormatting.BLACK)));
                  out.println("list.add(List.of(Text.literal(\""+line.replace("\n","\\n")+"\").formatted(Formatting.BLACK)));");
               }
            }
            BookElementBuilder bookBuilder = new BookElementBuilder();
            loreData.forEach(list -> bookBuilder.addPage(list.toArray(new Component[0])));
            BookGui loreGui = new BookGui(player,bookBuilder);
            loreGui.open();
            player.sendSystemMessage(Component.literal("Click to get item data location").withStyle(s -> s.withClickEvent(new ClickEvent.CopyToClipboard(path))));
            
            out.close();
         }
      } catch (Exception e){
         log(2,e.toString());
      }
      return 0;
   }
   
   public static int xpCommandQuery(CommandContext<CommandSourceStack> ctx, ServerPlayer target){
      try{
         CommandSourceStack source = ctx.getSource();
         ArcanaPlayerData profile = ArcanaNovum.data(target);
         MutableComponent feedback = Component.literal("")
               .append(target.getDisplayName())
               .append(Component.literal(" has ").withStyle(ChatFormatting.LIGHT_PURPLE))
               .append(Component.literal(Integer.toString(profile.getLevel())).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
               .append(Component.literal(" levels (").withStyle(ChatFormatting.LIGHT_PURPLE))
               .append(Component.literal(LevelUtils.getCurLevelXp(profile.getXP())+"/"+LevelUtils.nextLevelNewXp(profile.getLevel())).withStyle(ChatFormatting.AQUA))
               .append(Component.literal("). ").withStyle(ChatFormatting.LIGHT_PURPLE))
               .append(Component.literal(Integer.toString(profile.getXP())).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
               .append(Component.literal(" Total XP").withStyle(ChatFormatting.LIGHT_PURPLE));
         source.sendSuccess(()->feedback, false);
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return 0;
      }
   }
   
   public static int reloadCommand(CommandContext<CommandSourceStack> ctx){
      try {
         ctx.getSource().sendSuccess(() -> Component.literal("Reloading Arcana Data..."),true);
         CONFIG.read();
         RecipeManager.refreshRecipes(ctx.getSource().getServer());
         ctx.getSource().sendSuccess(() -> Component.literal("Arcana Data Reloaded!"),true);
         return 1;
      } catch (Exception e){
         log(2,e.toString());
      }
      return 0;
   }
   
   public static int getItemData(CommandContext<CommandSourceStack> objectCommandContext, String name){
      if(!DEV_MODE)
         return 0;
      try {
         ServerPlayer player = objectCommandContext.getSource().getPlayerOrException();
         ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
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
            String idName = fullName.replace(" ","_").toLowerCase(Locale.ROOT);
            
            Optional<Optional<Path>> inPathOpt = FabricLoader.getInstance().getModContainer(MOD_ID).map(container -> container.findPath("data/"+MOD_ID+"/datagen/new_item_template.txt"));
            Optional<Optional<Path>> outPathOpt = FabricLoader.getInstance().getModContainer(MOD_ID).map(container -> container.findPath("data/"+MOD_ID+"/datagen/"));
            if(inPathOpt.isEmpty() || inPathOpt.get().isEmpty() || outPathOpt.isEmpty() || outPathOpt.get().isEmpty()){
               return -1;
            }
            InputStream in = Files.newInputStream(inPathOpt.get().get());
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in))){
               String line;
               while ((line = bufferedReader.readLine()) != null){
                  stringBuilder.append(line).append("\n");
               }
            }
            String template = stringBuilder.toString();
            
            String path = outPathOpt.get().get() + "\\" + idName + ".txt";
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, false)));
            ArrayList<String> lines = new ArrayList<>();
            
            template = template.replace("$FName",fullName);
            template = template.replace("$SCName",idName);
            template = template.replace("$CCName",name);
            template = template.replace("$CName",idName.toUpperCase(Locale.ROOT));
            
            String nameCode = TextUtils.textToCode(stack.getHoverName());
            String parameters = Pattern.compile("\\.formatted\\((.*?)\\)") .matcher(nameCode).find() ? nameCode.replaceAll(".*\\.formatted\\((.*?)\\).*", "$1") : "";
            template = template.replace("$NameFormat",parameters);
            
            ItemLore loreComp = stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
            List<Component> loreLines = loreComp.styledLines();
            
            String lore = "";
            for(Component loreLine : loreLines){
               String loreCode = TextUtils.textToCode(loreLine).replace(";","");
               lore += "lore.add("+loreCode.replace(",Formatting.ITALICS","")+");\n";
            }
            template = template.replace("$LoreText",lore);
            
            lines.add(template);
            
            for(String line : lines){
               out.println(line);
            }
            player.sendSystemMessage(Component.literal("Click to get item data location").withStyle(s -> s.withClickEvent(new ClickEvent.CopyToClipboard(path))));
            
            out.close();
         }else{
            player.displayClientMessage(Component.literal("Hold an item to get data"),true);
         }
      } catch (Exception e){
         log(2,e.toString());
      }
      return 0;
   }
   
   
   public static int loadItemData(CommandContext<CommandSourceStack> ctx, String id){
      if(!DEV_MODE)
         return 0;
      try{
         CommandSourceStack source = ctx.getSource();
         ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(id);
         if(arcanaItem == null){
            source.sendSystemMessage(Component.literal("Invalid Arcana Item ID: "+id).withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
            return 0;
         }
         
         ServerPlayer player = source.getPlayerOrException();
         ServerLevel world = player.level();
         Vec3 vec3d = player.getEyePosition(0);
         Vec3 vec3d2 = player.getViewVector(0);
         double maxDistance = 5;
         Vec3 vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
         BlockHitResult result = world.clip(new ClipContext(vec3d, vec3d3, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
         if(result.getType() == BlockHitResult.Type.MISS){
            return 0;
         }
         BlockPos blockPos = result.getBlockPos();
         BlockEntity blockEntity = world.getBlockEntity(blockPos);
         BlockState blockState = world.getBlockState(blockPos);
         Block block = blockState.getBlock();
         if(blockEntity instanceof ChestBlockEntity chest && block instanceof ChestBlock chestBlock){
            Container chestInventory = ChestBlock.getContainer(chestBlock, blockState, world, blockPos, true);
            if(chestInventory == null || chestInventory.getContainerSize() != 54){
               return 0;
            }
            
            List<ArcanaRecipe> recipes = RecipeManager.getRecipesFor(arcanaItem.getItem());
            if(!recipes.isEmpty()){
               ArcanaRecipe recipe = recipes.getFirst(); // TODO add index param
               ArcanaIngredient[][] ingreds = recipe.getIngredients();
               for(int x = 0; x < 5; x++){
                  for(int y = 0; y < 5; y++){
                     chestInventory.setItem(9*y+x+1,ingreds[y][x].ingredientAsStack());
                  }
               }
               ForgeRequirement reqs = recipe.getForgeRequirement();
               if(reqs.needsAnvil()){
                  chestInventory.setItem(0,ArcanaRegistry.TWILIGHT_ANVIL.getNewItem());
               }
               if(reqs.needsCore()){
                  chestInventory.setItem(9,ArcanaRegistry.STELLAR_CORE.getNewItem());
               }
               if(reqs.needsFletchery()){
                  chestInventory.setItem(18,ArcanaRegistry.RADIANT_FLETCHERY.getNewItem());
               }
               if(reqs.needsEnchanter()){
                  chestInventory.setItem(27,ArcanaRegistry.MIDNIGHT_ENCHANTER.getNewItem());
               }
               if(reqs.needsSingularity()){
                  chestInventory.setItem(36,ArcanaRegistry.ARCANE_SINGULARITY.getNewItem());
               }
               
               GuiElementBuilder recipeItem = new GuiElementBuilder(Items.PAPER).hideDefaultTooltip();
               HashMap<String, Tuple<Integer, ItemStack>> ingredList = recipe.getIngredientList();
               recipeItem.setName(Component.literal("Total Ingredients").withStyle(ChatFormatting.DARK_PURPLE));
               recipeItem.addLoreLine(TextUtils.removeItalics(Component.literal("-----------------------").withStyle(ChatFormatting.LIGHT_PURPLE)));
               for(Map.Entry<String, Tuple<Integer, ItemStack>> ingred : ingredList.entrySet()){
                  recipeItem.addLoreLine(TextUtils.removeItalics(getIngredStr(ingred)));
               }
               recipeItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
               for(ArcanaItem req : recipe.getForgeRequirementList()){
                  MutableComponent requiresText = Component.literal("")
                        .append(Component.literal("Requires").withStyle(ChatFormatting.GREEN))
                        .append(Component.literal(" a ").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(req.getTranslatedName().withStyle(ChatFormatting.AQUA));
                  recipeItem.addLoreLine(TextUtils.removeItalics(requiresText));
               }
               if(!recipe.getForgeRequirementList().isEmpty()) recipeItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
               recipeItem.addLoreLine(TextUtils.removeItalics(Component.literal("Does not include item data").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)));
               chestInventory.setItem(34,recipeItem.asStack());
            }
            
            ItemStack item = arcanaItem.addCrafter(arcanaItem.getNewItem(),source.getPlayerOrException().getStringUUID(),1,source.getServer());
            if(item == null){
               source.sendSystemMessage((Component.literal("No Preferred Item Found For: ").append(arcanaItem.getTranslatedName())).withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
               return 0;
            }
            chestInventory.setItem(25,item);
            
            List<List<Component>> rawBookLore = arcanaItem.getBookLore();
            List<Filterable<String>> filteredLore = new ArrayList<>();
            for(List<Component> components : rawBookLore){
               StringBuilder builder = new StringBuilder();
               for(Component component : components){
                  builder.append(component.getString());
               }
               filteredLore.add(new Filterable<>(builder.toString(),Optional.empty()));
            }
            ItemStack loreBook = new ItemStack(Items.WRITABLE_BOOK);
            loreBook.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(filteredLore));
            loreBook.set(DataComponents.CUSTOM_NAME,arcanaItem.getTranslatedName().append(Component.literal(" Lore")));
            chestInventory.setItem(16,loreBook);
            
            GuiElementBuilder categoriesItem = new GuiElementBuilder(Items.PAPER).hideDefaultTooltip();
            categoriesItem.setName(Component.literal("Item Categories").withStyle(ChatFormatting.LIGHT_PURPLE));
            for(ArcaneTomeGui.TomeFilter category : arcanaItem.getCategories()){
               categoriesItem.addLoreLine(TextUtils.removeItalics(category.getColoredLabel()));
            }
            chestInventory.setItem(7,categoriesItem.asStack());
            
            
            List<ResearchTask> allTasks = ResearchTasks.getUniqueTasks(arcanaItem.getResearchTasks()).stream().toList();
            GuiElementBuilder researchItem = new GuiElementBuilder(Items.MAP).hideDefaultTooltip();
            researchItem.setName(Component.literal("Item Research").withStyle(ChatFormatting.YELLOW));
            boolean colorSwitch = false;
            for(ResearchTask researchTask : allTasks){
               researchItem.addLoreLine(TextUtils.removeItalics(researchTask.getName()).withColor(colorSwitch ? 0xe6d9bc : 0xb5a684));
               colorSwitch = !colorSwitch;
            }
            chestInventory.setItem(43,researchItem.asStack());
            
            return 1;
         }
         return 0;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int makeCraftingRecipe(CommandContext<CommandSourceStack> objectCommandContext){
      if(!DEV_MODE)
         return 0;
      try {
         ServerPlayer player = objectCommandContext.getSource().getPlayerOrException();
         ServerLevel world = player.level();
         Vec3 vec3d = player.getEyePosition(0);
         Vec3 vec3d2 = player.getViewVector(0);
         double maxDistance = 5;
         Vec3 vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
         BlockHitResult result = world.clip(new ClipContext(vec3d, vec3d3, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
         if(result.getType() == BlockHitResult.Type.MISS){
            return 0;
         }
         BlockPos blockPos = result.getBlockPos();
         BlockEntity blockEntity = world.getBlockEntity(blockPos);
         BlockState blockState = world.getBlockState(blockPos);
         Block block = blockState.getBlock();
         if(blockEntity instanceof ChestBlockEntity chest && block instanceof ChestBlock chestBlock){
            Container chestInventory = ChestBlock.getContainer(chestBlock, blockState, world, blockPos, true);
            if(chestInventory == null || chestInventory.getContainerSize() != 54){
               return 0;
            }
            
            ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(chestInventory.getItem(25));
            if(arcanaItem == null) return 0;
            
            ArcanaIngredient[][] ingredients = new ArcanaIngredient[5][5];
            for(int i = 0; i < 5; i++){
               for(int j = 0; j < 5; j++){
                  ItemStack stack = chestInventory.getItem(i*9+j+1);

                  ArcanaItem arcanaItemIngred = ArcanaItemUtils.identifyItem(stack);
                  ArcanaIngredient ingred;

                  if(arcanaItemIngred != null){
                     ingred = new GenericArcanaIngredient(arcanaItemIngred,stack.getCount());
                  }else if(stack.has(DataComponents.POTION_CONTENTS)){
                     PotionContents potionsComp = stack.get(DataComponents.POTION_CONTENTS);
                     ingred = new ArcanaIngredient(stack.getItem(),stack.getCount());
                     if(potionsComp.potion().isPresent()){
                        ingred = ingred.withPotion(potionsComp.potion().get());
                     }
                  }else if(EnchantmentHelper.hasAnyEnchantments(stack)){
                     ItemEnchantments enchantComp = EnchantmentHelper.getEnchantmentsForCrafting(stack);
                     ingred = new ArcanaIngredient(stack.getItem(),stack.getCount());
                     for(Holder<Enchantment> entry : enchantComp.keySet()){
                        ingred = ingred.withEnchantments(new ArcanaIngredient.EnchantmentEntry(entry.unwrapKey().get(), enchantComp.getLevel(entry)));
                     }
                  }else if(stack.isEmpty()){
                     ingred = ArcanaIngredient.EMPTY;
                  }else{
                     ingred = new ArcanaIngredient(stack.getItem(),stack.getCount());
                  }
                  ingredients[i][j] = ingred;
               }
            }
            
            ForgeRequirement forgeReq = new ForgeRequirement();
            int[] forgeSlots = new int[]{0,9,18,27,36};
            for(int forgeSlot : forgeSlots){
               ItemStack stack = chestInventory.getItem(forgeSlot);
               if(stack.is(ArcanaRegistry.MIDNIGHT_ENCHANTER.getItem())){
                  forgeReq = forgeReq.withEnchanter();
               }else if(stack.is(ArcanaRegistry.TWILIGHT_ANVIL.getItem())){
                  forgeReq = forgeReq.withAnvil();
               }else if(stack.is(ArcanaRegistry.STELLAR_CORE.getItem())){
                  forgeReq = forgeReq.withCore();
               }else if(stack.is(ArcanaRegistry.ARCANE_SINGULARITY.getItem())){
                  forgeReq = forgeReq.withSingularity();
               }else if(stack.is(ArcanaRegistry.RADIANT_FLETCHERY.getItem())){
                  forgeReq = forgeReq.withFletchery();
               }
            }
            
            ArcanaRecipe recipe = new ArcanaRecipe(arcanaItem,ingredients,forgeReq);
            
            Path dirPath = FabricLoader.getInstance().getConfigDir().resolve("arcananovum").resolve("recipes").resolve("recipe_gen");
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            
            File newFile = dirPath.resolve(recipe.getOutputId().getPath()+"_forging.json").toFile();
            newFile.getParentFile().mkdirs();
            
            try(BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile)))){
               JsonObject json = recipe.toJson();
               gson.toJson(json, output);
               ArcanaNovum.log(0,"Saved Forging Recipe for "+ recipe.getOutputId() +" to "+newFile.getAbsolutePath());
            }catch(IOException err){
               ArcanaNovum.log(2,"Failed to save "+ recipe.getOutputId() +" forging recipe file!");
               ArcanaNovum.log(2,err.toString());
            }
            player.sendSystemMessage(Component.literal("Click to get recipe data location").withStyle(s -> s.withClickEvent(new ClickEvent.CopyToClipboard(dirPath.toAbsolutePath().toString()))));
            
            
            return 1;
         }
         
      } catch (Exception e){
         log(2,e.toString());
      }
      return 0;
   }
   
   public static int testCommand(CommandContext<CommandSourceStack> ctx){
      if(!DEV_MODE)
         return 0;
      try {
         ServerPlayer player = ctx.getSource().getPlayer();
         
         // Get the recipe_gen directory
         Path dirPath = FabricLoader.getInstance().getConfigDir().resolve("arcananovum").resolve("recipes").resolve("recipe_gen");
         
         if(!Files.exists(dirPath)){
            player.sendSystemMessage(Component.literal("Recipe directory does not exist: " + dirPath));
            return 0;
         }
         
         // Find all JSON files with "_forging" in their name
         List<Path> forgingRecipes = new ArrayList<>();
         try(Stream<Path> paths = Files.walk(dirPath)){
            paths.filter(Files::isRegularFile)
                  .filter(path -> path.getFileName().toString().contains("_forging"))
                  .filter(path -> path.toString().endsWith(".json"))
                  .forEach(forgingRecipes::add);
         }
         
         if(forgingRecipes.isEmpty()){
            player.sendSystemMessage(Component.literal("No forging recipes found in directory"));
            return 0;
         }
         
         // Generate code file
         Path outputFile = dirPath.resolve("generated_recipes.txt");
         try(BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile.toFile()))){
            writer.write("// Generated Recipe Definitions\n");
            writer.write("// Total recipes: " + forgingRecipes.size() + "\n\n");
            
            for(Path recipePath : forgingRecipes){
               String content = Files.readString(recipePath);
               JsonObject json = JsonParser.parseString(content).getAsJsonObject();
               ArcanaRecipe recipe = ArcanaRecipe.fromJson(json);
               
               if(recipe == null){
                  writer.write("// ERROR: Failed to parse " + recipePath.getFileName() + "\n\n");
                  continue;
               }
               
               String itemName = recipe.getOutputId().getPath().toUpperCase().replace("_", " ");
               
               // Write comment header
               writer.write("      \n");
               writer.write("      // ===================================\n");
               writer.write("      //          " + itemName + "\n");
               writer.write("      // ===================================\n");
               
               // Generate ingredient assignments
               Map<String, ArcanaIngredient> ingredientMap = new HashMap<>();
               char currentVar = 'a';
               
               ArcanaIngredient[][] ingredients = recipe.getIngredients();
               for(int i = 0; i < 5; i++){
                  for(int j = 0; j < 5; j++){
                     ArcanaIngredient ingred = ingredients[i][j];
                     if(ingred == null || ingred.equals(ArcanaIngredient.EMPTY)) continue;
                     
                     String key = String.valueOf(currentVar);
                     boolean found = false;
                     for(Map.Entry<String, ArcanaIngredient> entry : ingredientMap.entrySet()){
                        if(entry.getValue().equals(ingred)){
                           found = true;
                           break;
                        }
                     }
                     
                     if(!found){
                        ingredientMap.put(key, ingred);
                        writer.write("      " + key + " = " + generateIngredientCode(ingred) + ";\n");
                        currentVar++;
                     }
                  }
               }
               
               writer.write("      \n");
               
               // Generate ingredients array
               writer.write("      ingredients = new ArcanaIngredient[][]{\n");
               for(int i = 0; i < 5; i++){
                  writer.write("            {");
                  for(int j = 0; j < 5; j++){
                     ArcanaIngredient ingred = ingredients[i][j];
                     String varName = findIngredientVar(ingred, ingredientMap);
                     writer.write(varName);
                     if(j < 4) writer.write(", ");
                  }
                  writer.write("}");
                  if(i < 4) writer.write(",");
                  writer.write("\n");
               }
               writer.write("            };\n");
               
               // Generate recipe add statement
               String forgeReqCode = generateForgeRequirementCode(recipe.getForgeRequirement());
               writer.write("      arcanaRecipes.add(new ArcanaRecipe(ArcanaRegistry." +
                     recipe.getOutputId().getPath().toUpperCase() + ", ingredients," + forgeReqCode + ")");
               
               // Add centerpieces if any
               List<Integer> centerpieces = recipe.getCenterpieces();
               for(int centerpiece : centerpieces){
                  writer.write(".addCenterpiece(" + centerpiece + ")");
               }
               writer.write(");\n");
               writer.write("      \n");
            }
            
            writer.write("\n// End of generated recipes\n");
         }
         
         player.sendSystemMessage(Component.literal("Generated recipe code file at: " + outputFile.toAbsolutePath()));
         player.sendSystemMessage(Component.literal("Click to copy path").withStyle(s ->
               s.withClickEvent(new ClickEvent.CopyToClipboard(outputFile.toAbsolutePath().toString()))));
         
      } catch (Exception e){
         log(2,e.toString());
         e.printStackTrace();
      }
      return 0;
   }
   
   private static String generateIngredientCode(ArcanaIngredient ingred){
      if(ingred == null || ingred.equals(ArcanaIngredient.EMPTY)){
         return "ArcanaIngredient.EMPTY";
      }
      
      ItemStack stack = ingred.ingredientAsStack();
      StringBuilder code = new StringBuilder();
      
      // Check if it's a GenericArcanaIngredient (Arcana item)
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
      if(arcanaItem != null){
         code.append("new GenericArcanaIngredient(ArcanaRegistry.")
               .append(arcanaItem.getId().toUpperCase())
               .append(",")
               .append(stack.getCount())
               .append(")");
         return code.toString();
      }
      
      // Regular item
      String itemName = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toUpperCase();
      boolean isIgnoresResourceful = ingred.getIgnoresResourceful();
      
      code.append("new ArcanaIngredient(Items.")
            .append(itemName)
            .append(",")
            .append(stack.getCount());
      
      if(isIgnoresResourceful){
         code.append(", true");
      }
      
      code.append(")");
      
      // Add enchantments if any
      List<Tuple<ResourceKey<Enchantment>, Integer>> enchantments = ingred.getEnchantments();
      if(!enchantments.isEmpty()){
         code.append(".withEnchantments(");
         for(int i = 0; i < enchantments.size(); i++){
            Tuple<ResourceKey<Enchantment>, Integer> ench = enchantments.get(i);
            String enchName = ench.getA().identifier().getPath().toUpperCase();
            code.append("new ArcanaIngredient.EnchantmentEntry(Enchantments.")
                  .append(enchName)
                  .append(",")
                  .append(ench.getB())
                  .append(")");
            if(i < enchantments.size() - 1) code.append(", ");
         }
         code.append(")");
      }
      
      // Add potion if any
      Holder<Potion> potion = ingred.getPotion();
      if(potion != null){
         String potionName = BuiltInRegistries.POTION.getKey(potion.value()).getPath().toUpperCase();
         code.append(".withPotions(Potions.").append(potionName).append(")");
      }
      
      return code.toString();
   }
   
   private static String findIngredientVar(ArcanaIngredient ingred, Map<String, ArcanaIngredient> ingredientMap){
      if(ingred == null || ingred.equals(ArcanaIngredient.EMPTY)){
         return "ArcanaIngredient.EMPTY";
      }
      
      for(Map.Entry<String, ArcanaIngredient> entry : ingredientMap.entrySet()){
         if(entry.getValue().equals(ingred)){
            return entry.getKey();
         }
      }
      
      return "ArcanaIngredient.EMPTY";
   }
   
   private static String generateForgeRequirementCode(ForgeRequirement req){
      StringBuilder code = new StringBuilder("new ForgeRequirement()");
      
      if(req.needsEnchanter()){
         code.append(".withEnchanter()");
      }
      if(req.needsAnvil()){
         code.append(".withAnvil()");
      }
      if(req.needsCore()){
         code.append(".withCore()");
      }
      if(req.needsFletchery()){
         code.append(".withFletchery()");
      }
      if(req.needsSingularity()){
         code.append(".withSingularity()");
      }
      
      return code.toString();
   }
   
   public static int testCommand(CommandContext<CommandSourceStack> objectCommandContext, int num){
      if(!DEV_MODE)
         return 0;
      try {
         ServerPlayer player = objectCommandContext.getSource().getPlayer();
         DEBUG_VALUE = num;
         
         
         
      } catch (Exception e){
         log(2,e.toString());
      }
      return 0;
   }
   
   public static int cacheCommand(CommandContext<CommandSourceStack> objectCommandContext){
      try{
         CommandSourceStack source = objectCommandContext.getSource();
         ServerPlayer player = source.getPlayerOrException();
   
         ArcaneTomeGui gui = new ArcaneTomeGui(player, ArcaneTomeGui.TomeMode.COMPENDIUM,null);
         gui.setGuiFlags(false,false,true,false);
         TriConsumer<CompendiumEntry, Integer, ClickType> consumer = (entry, index, clickType) -> {
            if(clickType.isRight){
               player.containerMenu.setCarried(ItemStack.EMPTY.copy());
               return;
            }
            if(entry instanceof ArcanaItemCompendiumEntry arcanaEntry){
               player.containerMenu.setCarried(arcanaEntry.getArcanaItem().addCrafter(arcanaEntry.getArcanaItem().getNewItem(),player.getStringUUID(),1,source.getServer()));
            }else if(entry instanceof IngredientCompendiumEntry ing){
               player.containerMenu.setCarried(ing.getDisplayStack().copyWithCount(ing.getDisplayStack().getMaxStackSize()));
            }
         };
         gui.addModes(consumer,(a,b,c)->{},(a,b,c)->{},(a,b,c)->{});
         gui.buildAndOpen();
         return 0;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static CompletableFuture<Suggestions> getItemSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder){
      String start = builder.getRemaining().toLowerCase(Locale.ROOT);
      Set<String> items = new HashSet<>();
      ArcanaRegistry.ARCANA_ITEMS.registryKeySet().forEach(key -> items.add(key.identifier().getPath()));
      items.stream().filter(s -> s.startsWith(start)).forEach(builder::suggest);
      return builder.buildFuture();
   }
   
   public static CompletableFuture<Suggestions> getResearchSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder){
      String start = builder.getRemaining().toLowerCase(Locale.ROOT);
      Set<String> items = new HashSet<>();
      ArcanaRegistry.ARCANA_ITEMS.registryKeySet().forEach(key -> items.add(key.identifier().getPath()));
      items.add("all");
      items.stream().filter(s -> s.startsWith(start)).forEach(builder::suggest);
      return builder.buildFuture();
   }
   
   public static CompletableFuture<Suggestions> getAchievementSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder){
      String start = builder.getRemaining().toLowerCase(Locale.ROOT);
      Set<String> items = ArcanaAchievements.registry.keySet();
      items.stream().filter(s -> s.startsWith(start)).forEach(builder::suggest);
      return builder.buildFuture();
   }
   
   public static CompletableFuture<Suggestions> getAugmentSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder){
      CommandSourceStack src = context.getSource();
      String start = builder.getRemaining().toLowerCase(Locale.ROOT);
      ArcanaItem arcanaItem;
      if(src.isPlayer() && src.getPlayer() != null){
         ItemStack handItem = src.getPlayer().getMainHandItem();
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
   
   public static int setAugment(CommandContext<CommandSourceStack> ctx, String id, int level, ServerPlayer player){
      try{
         CommandSourceStack src = ctx.getSource();
         if(!src.isPlayer() && player == null){
            src.sendFailure(Component.literal("Must specify player, or run command as a player"));
            return -1;
         }else if(player == null && src.getPlayer() != null){
            player = src.getPlayer();
         }
         ArcanaAugment augment = ArcanaAugments.registry.get(id);
         if(augment == null){
            src.sendFailure(Component.literal("That is not a valid Augment"));
            return -1;
         }
         if(level < 0 || level > augment.getTiers().length){
            src.sendFailure(Component.literal("Level out of bounds (0-"+augment.getTiers().length+")"));
            return -1;
         }
         if(level == 0){
            ArcanaNovum.data(player).removeAugment(id);
            src.sendSystemMessage(Component.literal("Successfully removed ").append(augment.getTranslatedName()).append(" from ").append(player.getDisplayName()));
         }else{
            ArcanaNovum.data(player).setAugmentLevel(id,level);
            src.sendSystemMessage(Component.literal("Successfully set ").append(augment.getTranslatedName()).append(" to level "+level+" for ").append(player.getDisplayName()));
         }
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int enhanceCommand(CommandContext<CommandSourceStack> ctx, double percentage, ServerPlayer player){
      try{
         CommandSourceStack src = ctx.getSource();
         if(!src.isPlayer() && player == null){
            src.sendFailure(Component.literal("Must specify player, or run command as a player"));
            return -1;
         }else if(player == null && src.getPlayer() != null){
            player = src.getPlayer();
         }
         ItemStack handItem = player.getMainHandItem();
         
         if(percentage > 100 || percentage < 0){
            src.sendFailure(Component.literal("Percentage out of bounds 0.0 - 100.0"));
            return -1;
         }
         if(!EnhancedStatUtils.isItemEnhanceable(handItem)){
            src.sendFailure(Component.literal("Player is not holding a valid infusion item"));
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
   
   public static int changeCrafter(CommandContext<CommandSourceStack> ctx, String username, int type){
      try{
         CommandSourceStack src = ctx.getSource();
         if(!src.isPlayer()){
            src.sendFailure(Component.literal("Must run command as a player"));
            return -1;
         }
         
         ServerPlayer player = src.getPlayer();
         ItemStack handItem = player.getMainHandItem();
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(handItem);
         
         if(arcanaItem == null){
            src.sendFailure(Component.literal("Player is not holding a valid Arcana Item"));
            return -1;
         }
         
         ProfileResolver profileResolver = src.getServer().services().profileResolver();
         Optional<GameProfile> optional = profileResolver.fetchByName(username);
         if(optional.isEmpty()){
            src.sendFailure(Component.translatable("commands.fetchprofile.name.failure", Component.literal(username)));
            return -1;
         }
         arcanaItem.addCrafter(handItem,optional.get().id().toString(),type,src.getServer());
         src.sendSuccess(() -> Component.translatable("command.arcananovum.change_crafter_success", optional.get().name()),false);
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int applyAugment(CommandContext<CommandSourceStack> ctx, String id, int level, ServerPlayer player){
      try{
         CommandSourceStack src = ctx.getSource();
         if(!src.isPlayer() && player == null){
            src.sendFailure(Component.literal("Must specify player, or run command as a player"));
            return -1;
         }else if(player == null && src.getPlayer() != null){
            player = src.getPlayer();
         }
         ArcanaAugment augment = ArcanaAugments.registry.get(id);
         ItemStack handItem = player.getMainHandItem();
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(handItem);
         
         if(augment == null){
            src.sendFailure(Component.literal("That is not a valid Augment"));
            return -1;
         }
         if(level < 1 || level > augment.getTiers().length){
            src.sendFailure(Component.literal("Level out of bounds (1-"+augment.getTiers().length+")"));
            return -1;
         }
         if(arcanaItem == null || !arcanaItem.getId().equals(augment.getArcanaItem().getId())){
            src.sendFailure(Component.literal("Player is not holding a valid Arcana Item"));
            return -1;
         }
         if(ArcanaAugments.isIncompatible(handItem,id)){
            src.sendFailure(Component.literal("This augment is incompatible with existing augments"));
            return -1;
         }
         if(ArcanaAugments.applyAugment(handItem,id,level,false)){
            src.sendSystemMessage(Component.literal("Successfully applied ").append(augment.getTranslatedName()).append(" at level "+level+" for ").append(player.getDisplayName()));
            return 1;
         }else{
            src.sendFailure(Component.literal("Couldn't apply augment (Cannot downgrade existing augments)"));
            return -1;
         }
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int setResearch(CommandContext<CommandSourceStack> ctx, String id, boolean grant, Collection<ServerPlayer> targets){
      try{
         CommandSourceStack source = ctx.getSource();
         ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(id);
         boolean isAll = id.equals("all");
         
         if(arcanaItem == null && !isAll){
            source.sendFailure(Component.literal("That is not a valid item id"));
            return -1;
         }
         
         List<ArcanaItem> items = new ArrayList<>();
         if(isAll){
            ArcanaRegistry.ARCANA_ITEMS.entrySet().forEach(entry -> items.add(entry.getValue()));
         }else{
            items.add(arcanaItem);
         }
         
         for (ServerPlayer player : targets){
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
         
         MutableComponent feedback = Component.literal("");
         MutableComponent itemName = isAll ? Component.literal("All Arcana Items") : arcanaItem.getTranslatedName();
         if(grant){
            feedback.append(Component.literal("Added Research for [").withStyle(ChatFormatting.LIGHT_PURPLE));
            feedback.append(itemName.withStyle(ChatFormatting.AQUA));
            feedback.append(Component.literal("] to ").withStyle(ChatFormatting.LIGHT_PURPLE));
         }else{
            feedback.append(Component.literal("Removed Research for [").withStyle(ChatFormatting.LIGHT_PURPLE));
            feedback.append(itemName.withStyle(ChatFormatting.AQUA));
            feedback.append(Component.literal("] from ").withStyle(ChatFormatting.LIGHT_PURPLE));
         }
         if(targets.size() == 1){
            feedback.append(targets.iterator().next().getDisplayName());
         }else{
            feedback.append(Component.literal(targets.size() + " players").withStyle(ChatFormatting.LIGHT_PURPLE));
         }
         source.sendSuccess(()->feedback,true);
         
         return targets.size();
      }catch(Exception e){
         log(2,e.toString());
         return 0;
      }
   }
   
   public static int getResearch(CommandContext<CommandSourceStack> ctx, String id, ServerPlayer target){
      try{
         CommandSourceStack source = ctx.getSource();
         ArcanaPlayerData profile = ArcanaNovum.data(target);
         ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(id);
         
         if(arcanaItem == null){
            source.sendFailure(Component.literal("That is not a valid item id"));
            return -1;
         }
         
         boolean researched = profile.hasResearched(arcanaItem);
         
         MutableComponent feedback = Component.literal("")
               .append(target.getDisplayName().copy())
               .append(Component.literal(researched ? " has researched [" : " has NOT researched [").withStyle(ChatFormatting.LIGHT_PURPLE))
               .append(arcanaItem.getTranslatedName().withStyle(ChatFormatting.AQUA))
               .append(Component.literal("]").withStyle(ChatFormatting.LIGHT_PURPLE));
         
         source.sendSuccess(()->feedback,false);
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   
   public static int setAchievement(CommandContext<CommandSourceStack> ctx, String id, boolean grant, Collection<ServerPlayer> targets){
      try{
         CommandSourceStack source = ctx.getSource();
         ArcanaAchievement achievement = ArcanaAchievements.registry.get(id);
         if(achievement == null){
            source.sendFailure(Component.literal("That is not a valid Achievement"));
            return -1;
         }
      
         for (ServerPlayer player : targets){
            if(grant){
               ArcanaAchievements.grant(player,id);
            }else{
               ArcanaAchievements.revoke(player,id);
            }
         }
      
         MutableComponent feedback = Component.literal("");
         if(grant){
            feedback.append(Component.literal("Granted Achievement [").withStyle(ChatFormatting.LIGHT_PURPLE));
            feedback.append(achievement.getTranslatedName().withStyle(ChatFormatting.AQUA));
            feedback.append(Component.literal("] to ").withStyle(ChatFormatting.LIGHT_PURPLE));
         }else{
            feedback.append(Component.literal("Revoked Achievement [").withStyle(ChatFormatting.LIGHT_PURPLE));
            feedback.append(achievement.getTranslatedName().withStyle(ChatFormatting.AQUA));
            feedback.append(Component.literal("] from ").withStyle(ChatFormatting.LIGHT_PURPLE));
         }
         if(targets.size() == 1){
            feedback.append(targets.iterator().next().getDisplayName());
         }else{
            feedback.append(Component.literal(targets.size() + " players").withStyle(ChatFormatting.LIGHT_PURPLE));
         }
         source.sendSuccess(()->feedback,true);
      
         return targets.size();
      }catch(Exception e){
         log(2,e.toString());
         return 0;
      }
   }
   
   public static int getAchievement(CommandContext<CommandSourceStack> ctx, String id, ServerPlayer target){
      try{
         CommandSourceStack source = ctx.getSource();
         ArcanaPlayerData profile = ArcanaNovum.data(target);
         ArcanaAchievement baseAch = ArcanaAchievements.registry.get(id);
         if(baseAch == null){
            source.sendFailure(Component.literal("That is not a valid Achievement"));
            return -1;
         }
         ArcanaAchievement profAchieve = profile.getAchievement(baseAch.getArcanaItem().getId(),id);
         ArcanaAchievement achieve = profAchieve == null ? baseAch : profAchieve;
         MutableComponent[] response = achieve.getStatusDisplay(target);
         
   
         MutableComponent header = Component.literal("")
               .append(target.getDisplayName().copy().append("'s"))
               .append(Component.literal(" progress towards [").withStyle(ChatFormatting.LIGHT_PURPLE))
               .append(achieve.getTranslatedName().withStyle(ChatFormatting.AQUA))
               .append(Component.literal("]: ").withStyle(ChatFormatting.LIGHT_PURPLE));
         
         source.sendSuccess(()->header,false);
         if(response == null) return 0;
         for(MutableComponent mutableText : response){
            source.sendSuccess(()->mutableText, false);
         }
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return 0;
      }
   }
   
   public static int createItems(CommandSourceStack source, String id, Collection<ServerPlayer> targets){
      try{
         ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(id);
         if(arcanaItem == null){
            source.sendSystemMessage(Component.literal("Invalid Arcana Item ID: "+id).withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
            return 0;
         }
   
         for(ServerPlayer target : targets){
            ItemStack item = arcanaItem.addCrafter(arcanaItem.getNewItem(),target.getStringUUID(),1,source.getServer());
            
            if(item == null){
               source.sendSystemMessage((Component.literal("No Preferred Item Found For: ").append(arcanaItem.getTranslatedName())).withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
               return 0;
            }else{
               String uuid = ArcanaItem.getUUID(item);
               source.sendSystemMessage((Component.literal("Generated New: ").append(arcanaItem.getTranslatedName()).append(Component.literal(" with UUID "+uuid))).withStyle(ChatFormatting.GREEN));
               BorisLib.addTickTimerCallback(new ItemReturnTimerCallback(item,target,0));
            }
         }
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int createItem(CommandSourceStack source, String id){
      try{
         ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(id);
         if(arcanaItem == null){
            source.sendSystemMessage(Component.literal("Invalid Arcana Item ID: "+id).withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
            return 0;
         }
         ItemStack item = arcanaItem.addCrafter(arcanaItem.getNewItem(),source.getPlayerOrException().getStringUUID(),1,source.getServer());
         
         if(item == null){
            source.sendSystemMessage((Component.literal("No Preferred Item Found For: ").append(arcanaItem.getTranslatedName())).withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
            return 0;
         }else{
            String uuid = ArcanaItem.getUUID(item);
            source.sendSystemMessage((Component.literal("Generated New: ").append(arcanaItem.getTranslatedName()).append(Component.literal(" with UUID "+uuid))).withStyle(ChatFormatting.GREEN));
            BorisLib.addTickTimerCallback(new ItemReturnTimerCallback(item,source.getPlayerOrException(),0));
            return 1;
         }
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int showItem(CommandContext<CommandSourceStack> context){
      CommandSourceStack source = context.getSource();
      if(!source.isPlayer() || source.getPlayer() == null){
         source.sendSuccess(()-> Component.literal("Command must be executed by a player"), false);
         return -1;
      }
      ServerPlayer player = source.getPlayer();
      if(!ArcanaItemUtils.isArcane(player.getMainHandItem())){
         source.sendSuccess(()-> Component.literal("You can only show off Arcana Items"), false);
         return -1;
      }
      
      MutableComponent message = Component.literal("")
            .append(player.getFeedbackDisplayName())
            .append(Component.literal(" is showing off their ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(player.getMainHandItem().getDisplayName());
      
      for(ServerPlayer other : context.getSource().getServer().getPlayerList().getPlayers()){
         other.sendSystemMessage(message);
      }
      
      return 0;
   }
   
   public static int startDragonBoss(CommandContext<CommandSourceStack> context){
      CommandSourceStack source = context.getSource();
      if(!source.isPlayer()){
         source.sendSuccess(()-> Component.literal("Command must be executed by a player"), false);
         return -1;
      }
      for(ServerLevel world : source.getServer().getAllLevels()){
         if(DataAccess.getWorld(world.dimension(), BossFightData.KEY).getBossFight() != null){
            source.sendSuccess(()-> Component.literal("A Boss Fight is Currently Active"), false);
            return -1;
         }
      }
      ServerPlayer player = source.getPlayer();
      return DragonBossFight.prepBoss(player);
   }
   
   public static int abortBoss(CommandContext<CommandSourceStack> context){
      MinecraftServer server = context.getSource().getServer();
      Tuple<BossFights, CompoundTag> bossFight = DataAccess.getWorld(Level.END, BossFightData.KEY).getBossFight();
      context.getSource().sendSuccess(()-> Component.literal("Aborting Boss Fight"),true);
      if(bossFight == null){
         return BossFight.cleanBoss(server);
      }
      if(bossFight.getA() == BossFights.DRAGON){
         return DragonBossFight.abortBoss(server);
      }
      return 0;
   }
   
   public static int cleanBoss(CommandContext<CommandSourceStack> context){
      CommandSourceStack source = context.getSource();
      source.sendSuccess(()-> Component.literal("Cleaned Boss Data"),true);
      return BossFight.cleanBoss(source.getServer());
   }
   
   public static int bossStatus(CommandContext<CommandSourceStack> context){
      CommandSourceStack source = context.getSource();
      Tuple<BossFights, CompoundTag> bossFight = DataAccess.getWorld(Level.END, BossFightData.KEY).getBossFight();
      if(bossFight == null){
         source.sendSuccess(()-> Component.literal("No Boss Fight Active"),false);
         return -1;
      }
      if(bossFight.getA() == BossFights.DRAGON){
         return DragonBossFight.bossStatus(source.getServer(),context.getSource());
      }
      return -1;
   }
   
   public static int bossResetAbilities(CommandContext<CommandSourceStack> context, boolean doAbility){
      CommandSourceStack source = context.getSource();
      Tuple<BossFights, CompoundTag> bossFight = DataAccess.getWorld(Level.END, BossFightData.KEY).getBossFight();
      if(bossFight == null){
         source.sendSuccess(()-> Component.literal("No Boss Fight Active"),false);
         return -1;
      }
      if(bossFight.getA() == BossFights.DRAGON){
         return DragonBossFight.resetDragonAbilities(source.getServer(),context.getSource(),doAbility);
      }
      return -1;
   }
   
   public static int bossForceLairAction(CommandContext<CommandSourceStack> context){
      CommandSourceStack source = context.getSource();
      Tuple<BossFights, CompoundTag> bossFight = DataAccess.getWorld(Level.END, BossFightData.KEY).getBossFight();
      if(bossFight == null){
         source.sendSuccess(()-> Component.literal("No Boss Fight Active"),false);
         return -1;
      }
      if(bossFight.getA() == BossFights.DRAGON){
         return DragonBossFight.forceLairAction(source.getServer(),context.getSource());
      }
      return -1;
   }
   
   public static int bossForcePlayerCount(CommandContext<CommandSourceStack> context, int playerCount){
      CommandSourceStack source = context.getSource();
      Tuple<BossFights, CompoundTag> bossFight = DataAccess.getWorld(Level.END, BossFightData.KEY).getBossFight();
      if(bossFight == null){
         source.sendSuccess(()-> Component.literal("No Boss Fight Active"),false);
         return -1;
      }
      if(bossFight.getA() == BossFights.DRAGON){
         DragonBossFight.setForcedPlayerCount(context.getSource().getServer(),playerCount);
         return 1;
      }
      return -1;
   }
   
   public static int testBoss(CommandContext<CommandSourceStack> context){
      CommandSourceStack source = context.getSource();
      
      return 0;
   }
   
   public static int bossTeleport(CommandContext<CommandSourceStack> context, ServerPlayer player, boolean all){
      CommandSourceStack source = context.getSource();
      Tuple<BossFights, CompoundTag> bossFight = DataAccess.getWorld(Level.END, BossFightData.KEY).getBossFight();
      if(bossFight == null){
         source.sendSuccess(()-> Component.literal("No Boss Fight Active"),false);
         return -1;
      }
      if(bossFight.getA() == BossFights.DRAGON){
         if(all){
            List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();
            for(ServerPlayer p : players){
               DragonBossFight.teleportPlayer(p,true);
            }
         }else{

            DragonBossFight.teleportPlayer(player, Commands.LEVEL_GAMEMASTERS.check(player.permissions()));
         }
         return 0;
      }
      return -1;
   }
   
   public static int announceBoss(CommandSourceStack source, String time){
      Tuple<BossFights, CompoundTag> bossFight = DataAccess.getWorld(Level.END, BossFightData.KEY).getBossFight();
      if(bossFight == null){
         source.sendSuccess(()-> Component.literal("No Boss Fight Active"),false);
         return -1;
      }
      if(bossFight.getA() == BossFights.DRAGON){
         return DragonBossFight.announceBoss(source.getServer(),bossFight.getB(),time);
      }
      return -1;
   }
   
   public static int beginBoss(CommandContext<CommandSourceStack> context){
      Tuple<BossFights, CompoundTag> bossFight = DataAccess.getWorld(Level.END, BossFightData.KEY).getBossFight();
      if(bossFight == null){
         context.getSource().sendSuccess(()-> Component.literal("No Boss Fight Active"),false);
         return -1;
      }
      if(bossFight.getA() == BossFights.DRAGON){
         return DragonBossFight.beginBoss(context.getSource().getServer(),bossFight.getB());
      }
      return -1;
   }
   
   
   public static int setItemName(CommandContext<CommandSourceStack> ctx, String name){
      try{
         CommandSourceStack src = ctx.getSource();
         if(!src.isPlayer()){
            src.sendFailure(Component.literal("Must run command as a player"));
            return -1;
         }
         ServerPlayer player = src.getPlayer();
         ItemStack handItem = player.getMainHandItem();
         handItem.set(DataComponents.ITEM_NAME, TextUtils.parseString(name));
         
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int getItemName(CommandContext<CommandSourceStack> ctx){
      try{
         CommandSourceStack src = ctx.getSource();
         if(!src.isPlayer()){
            src.sendFailure(Component.literal("Must run command as a player"));
            return -1;
         }
         ServerPlayer player = src.getPlayer();
         ItemStack handItem = player.getMainHandItem();
         
         String feedback = TextUtils.textToString(handItem.getHoverName());
         String copyText = feedback;
         src.sendSuccess(() -> Component.literal(feedback).withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to Copy Data"))).withClickEvent(new ClickEvent.CopyToClipboard(copyText))), false);
         
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int setItemLore(CommandContext<CommandSourceStack> ctx, int index, String lore){
      try{
         CommandSourceStack src = ctx.getSource();
         if(!src.isPlayer()){
            src.sendFailure(Component.literal("Must run command as a player"));
            return -1;
         }
         ServerPlayer player = src.getPlayer();
         ItemStack handItem = player.getMainHandItem();
         ItemLore loreComp = handItem.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
         ItemLore newLore;
         List<Component> lines = loreComp.styledLines();
         
         if(index < 0){
            newLore = loreComp.withLineAdded(TextUtils.removeItalics(TextUtils.parseString(lore)));
         }else if(index < lines.size()){
            lines.set(index, TextUtils.removeItalics(TextUtils.parseString(lore)));
            newLore = new ItemLore(lines);
         }else{
            int blankLines = index - lines.size();
            for(int i = 0; i < blankLines; i++){
               lines.add(Component.literal(""));
            }
            lines.add(TextUtils.removeItalics(TextUtils.parseString(lore)));
            newLore = new ItemLore(lines);
         }
         
         handItem.set(DataComponents.LORE,newLore);
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int getItemLore(CommandContext<CommandSourceStack> ctx, int index){
      try{
         CommandSourceStack src = ctx.getSource();
         if(!src.isPlayer()){
            src.sendFailure(Component.literal("Must run command as a player"));
            return -1;
         }
         ServerPlayer player = src.getPlayer();
         ItemStack handItem = player.getMainHandItem();
         ItemLore loreComp = handItem.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
         List<Component> lines = loreComp.styledLines();
         
         if(index >= 0 && index < lines.size()){
            String feedback = TextUtils.textToString(lines.get(index));
            String copyText = feedback;
            src.sendSuccess(() -> Component.literal(feedback).withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to Copy Data"))).withClickEvent(new ClickEvent.CopyToClipboard(copyText))), false);
         }else{
            return 0;
         }
         
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int removeItemLore(CommandContext<CommandSourceStack> ctx, int index){
      try{
         CommandSourceStack src = ctx.getSource();
         if(!src.isPlayer()){
            src.sendFailure(Component.literal("Must run command as a player"));
            return -1;
         }
         ServerPlayer player = src.getPlayer();
         ItemStack handItem = player.getMainHandItem();
         ItemLore loreComp = handItem.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);
         List<Component> lines = loreComp.styledLines();
         
         if(index >= 0 && index < lines.size()){
            lines.remove(index);
         }else{
            return 0;
         }
         
         handItem.set(DataComponents.LORE,new ItemLore(lines));
         return 1;
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int placedBlocks(CommandContext<CommandSourceStack> ctx, ServerPlayer player){
      try{
         CommandSourceStack src = ctx.getSource();
         
         ArrayList<MutableComponent> blocks = new ArrayList<>();
         for(Tuple<BlockEntity, ArcanaBlockEntity> pair : ACTIVE_ARCANA_BLOCKS.keySet().stream().filter(pair -> player.getStringUUID().equals(pair.getB().getCrafterId()) && pair.getA().hasLevel() && pair.getA().getLevel().getBlockEntity(pair.getA().getBlockPos()) == pair.getA()).toList()){
            BlockEntity blockEntity = pair.getA();
            ArcanaBlockEntity arcanaBlockEntity = pair.getB();
            
            String dim = blockEntity.getLevel().dimension().identifier().toString();
            MutableComponent dimensionName = Component.literal("Unknown").withStyle(ChatFormatting.GRAY);
            if(dim.equals(ServerLevel.OVERWORLD.identifier().toString())){
               dimensionName = Component.literal("The Overworld").withStyle(ChatFormatting.GREEN);
            }else if(dim.equals(ServerLevel.NETHER.identifier().toString())){
               dimensionName = Component.literal("The Nether").withStyle(ChatFormatting.RED);
            }else if(dim.equals(ServerLevel.END.identifier().toString())){
               dimensionName = Component.literal("The End").withStyle(ChatFormatting.DARK_PURPLE);
            }
            BlockPos pos = blockEntity.getBlockPos();
            String posStr = pos.getX()+","+pos.getY()+","+pos.getZ();
            
            MutableComponent blockText = Component.literal("")
                  .append(arcanaBlockEntity.getArcanaItem().getDisplayName())
                  .append(Component.literal(" in ").withStyle(ChatFormatting.LIGHT_PURPLE))
                  .append(dimensionName)
                  .append(Component.literal(" at (").withStyle(ChatFormatting.LIGHT_PURPLE))
                  .append(Component.literal(posStr).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(")").withStyle(ChatFormatting.LIGHT_PURPLE));
            blocks.add(blockText);
         }
         
         MutableComponent feedback = Component.literal("")
               .append(player.getDisplayName())
               .append(Component.literal(" has ").withStyle(ChatFormatting.LIGHT_PURPLE))
               .append(Component.literal(Integer.toString(blocks.size())).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
               .append(Component.literal(" placed Arcana Blocks.").withStyle(ChatFormatting.LIGHT_PURPLE));
         src.sendSuccess(()-> Component.literal(""),false);
         src.sendSuccess(()->feedback,false);
         src.sendSuccess(()-> Component.literal("================================").withStyle(ChatFormatting.LIGHT_PURPLE),false);
         for(MutableComponent r : blocks){
            src.sendSuccess(()->r,false);
         }
         return blocks.size();
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
   
   public static int placedBlocks(CommandContext<CommandSourceStack> ctx){
      try{
         CommandSourceStack src = ctx.getSource();
         if(!src.isPlayer()){
            src.sendFailure(Component.literal("Must run command as a player"));
            return -1;
         }
         return placedBlocks(ctx, src.getPlayer());
      }catch(Exception e){
         log(2,e.toString());
         return -1;
      }
   }
}
