package net.borisshoes.arcananovum.items;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.gui.brainjar.BrainJarGui;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class BrainJar extends EnergyItem {
   public static final int[] capacities = {1000000,2000000,4000000,6000000,8000000,10000000};
   private static final String TXT_ON = "item/brain_jar_on";
   private static final String TXT_OFF = "item/brain_jar_off";
   private static final Item textureItem = Items.TINTED_GLASS;
   
   public BrainJar(){
      id = "brain_jar";
      name = "Brain in a Jar";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ITEMS};
      vanillaItem = Items.ZOMBIE_HEAD;
      item = new BrainJarItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(textureItem,TXT_OFF));
      models.add(new Pair<>(textureItem,TXT_ON));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Brain in a Jar\",\"italic\":false,\"color\":\"green\",\"bold\":true}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      buildItemLore(stack, ArcanaNovum.SERVER);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      tag = addMagicNbt(tag);
      NbtCompound magicTag = tag.getCompound("arcananovum");
      magicTag.putInt("mode",0);
      prefNBT = tag;
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"A \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"zombie\",\"color\":\"dark_green\"},{\"text\":\" has more aptitude for storing \",\"color\":\"dark_purple\"},{\"text\":\"knowledge\",\"color\":\"green\"},{\"text\":\" than most mobs.\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Containing its \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"brain\",\"color\":\"dark_green\"},{\"text\":\" in a jar could serve as \"},{\"text\":\"XP storage\",\"color\":\"green\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It should also be capable of activating the \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"mending\",\"color\":\"light_purple\"},{\"text\":\" enchantment.\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right click\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" to configure.\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      
      if(itemStack != null){
         int xp = getEnergy(itemStack);
         NbtCompound itemNbt = itemStack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         boolean mending = magicNbt.getInt("mode") == 1;
         if(mending){
            loreList.add(NbtString.of("[{\"text\":\"" + xp + " XP Stored - Mending \",\"italic\":false,\"color\":\"green\"},{\"text\":\"ON\",\"italic\":false,\"color\":\"dark_green\"}]"));
         }else{
            loreList.add(NbtString.of("[{\"text\":\"" + xp + " XP Stored - Mending \",\"italic\":false,\"color\":\"green\"},{\"text\":\"OFF\",\"italic\":false,\"color\":\"red\"}]"));
         }
      }else{
         loreList.add(NbtString.of("[{\"text\":\"0 XP Stored - Mending \",\"italic\":false,\"color\":\"green\"},{\"text\":\"OFF\",\"italic\":false,\"color\":\"red\"}]"));
      }
      
      return loreList;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      int capLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.UNENDING_WISDOM.id));
      return capacities[capLvl];
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      int mode = magicTag.getInt("mode");
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      newTag.getCompound("arcananovum").putInt("mode",mode);
      stack.setNbt(newTag);
      return buildItemLore(stack,server);
   }
   
   public void openGui(PlayerEntity playerEntity, ItemStack item){
      if(!(playerEntity instanceof ServerPlayerEntity player))
         return;
      BrainJarGui gui = new BrainJarGui(ScreenHandlerType.HOPPER,player,this, item);
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      int mode = magicNbt.getInt("mode"); // 0 is off, 1 is on
      
      // Try to fix the wierd xp give shenanigans
      player.totalExperience = (LevelUtils.vanillaLevelToTotalXp(player.experienceLevel) + (int)(player.experienceProgress*player.getNextLevelExperience()));
      player.experienceProgress = (float)(player.totalExperience - LevelUtils.vanillaLevelToTotalXp(player.experienceLevel)) / (float)player.getNextLevelExperience();
   
      ItemStack echest = new ItemStack(Items.ENDER_CHEST);
      NbtCompound tag = echest.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Store Levels\",\"italic\":false,\"color\":\"dark_aqua\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click to store \",\"italic\":false,\"color\":\"green\"},{\"text\":\"1\",\"color\":\"aqua\"},{\"text\":\" level\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right click to store all (\",\"italic\":false,\"color\":\"green\"},{\"text\":\""+player.experienceLevel+"\",\"color\":\"aqua\"},{\"text\":\") levels\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      gui.setSlot(0,GuiElementBuilder.from(echest));
   
      ItemStack bottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
      tag = bottle.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Withdraw Levels\",\"italic\":false,\"color\":\"dark_aqua\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click to gain \",\"italic\":false,\"color\":\"green\"},{\"text\":\"1\",\"color\":\"aqua\"},{\"text\":\" level\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right click to take all (\",\"italic\":false,\"color\":\"green\"},{\"text\":\""+getEnergy(item)+"\",\"color\":\"aqua\"},{\"text\":\") XP\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      gui.setSlot(4,GuiElementBuilder.from(bottle));
   
      gui.setSlot(1,new GuiElementBuilder(Items.GREEN_STAINED_GLASS_PANE).setName(Text.translatable(getEnergy(item)+" XP Stored").formatted(Formatting.GREEN)));
      gui.setSlot(3,new GuiElementBuilder(Items.GREEN_STAINED_GLASS_PANE).setName(Text.translatable(getEnergy(item)+" XP Stored").formatted(Formatting.GREEN)));
   
      if(mode == 0){
         ItemStack notmending = new ItemStack(Items.BARRIER);
         tag = notmending.getOrCreateNbt();
         display = new NbtCompound();
         loreList = new NbtList();
         display.putString("Name","[{\"text\":\"Not Mending Items\",\"italic\":false,\"color\":\"dark_aqua\"}]");
         loreList.add(NbtString.of("[{\"text\":\"Currently Not Mending Items\",\"italic\":false,\"color\":\"red\"}]"));
         loreList.add(NbtString.of("[{\"text\":\"\",\"color\":\"dark_purple\"}]"));
         loreList.add(NbtString.of("[{\"text\":\"Click to toggle ON\",\"italic\":false,\"color\":\"green\"}]"));
         display.put("Lore",loreList);
         tag.put("display",display);
         gui.setSlot(2,GuiElementBuilder.from(notmending));
      }else if(mode == 1){
         ItemStack mending = new ItemStack(Items.STRUCTURE_VOID);
         tag = mending.getOrCreateNbt();
         display = new NbtCompound();
         loreList = new NbtList();
         display.putString("Name","[{\"text\":\"Mending Items\",\"italic\":false,\"color\":\"dark_aqua\"}]");
         loreList.add(NbtString.of("[{\"text\":\"Currently Mending Items\",\"italic\":false,\"color\":\"green\"}]"));
         loreList.add(NbtString.of("[{\"text\":\"\",\"color\":\"dark_purple\"}]"));
         loreList.add(NbtString.of("[{\"text\":\"Click to toggle OFF\",\"italic\":false,\"color\":\"red\"}]"));
         display.put("Lore",loreList);
         tag.put("display",display);
         gui.setSlot(2,GuiElementBuilder.from(mending));
      }
   
   
      gui.setTitle(Text.literal("Brain in a Jar"));
      gui.open();
   }
   
   public void toggleMending(BrainJarGui gui, ServerPlayerEntity player, ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      int mode = magicNbt.getInt("mode");
      
      if(mode == 1){
         ItemStack notmending = new ItemStack(Items.BARRIER);
         NbtCompound tag = notmending.getOrCreateNbt();
         NbtCompound display = new NbtCompound();
         NbtList loreList = new NbtList();
         display.putString("Name","[{\"text\":\"Not Mending Items\",\"italic\":false,\"color\":\"dark_aqua\"}]");
         loreList.add(NbtString.of("[{\"text\":\"Currently Not Mending Items\",\"italic\":false,\"color\":\"red\"}]"));
         loreList.add(NbtString.of("[{\"text\":\"\",\"color\":\"dark_purple\"}]"));
         loreList.add(NbtString.of("[{\"text\":\"Click to toggle ON\",\"italic\":false,\"color\":\"green\"}]"));
         display.put("Lore",loreList);
         tag.put("display",display);
         gui.setSlot(2,GuiElementBuilder.from(notmending));
         
         magicNbt.putInt("mode", 0);
      }else if(mode == 0){
         ItemStack mending = new ItemStack(Items.STRUCTURE_VOID);
         NbtCompound tag = mending.getOrCreateNbt();
         NbtCompound display = new NbtCompound();
         NbtList loreList = new NbtList();
         display.putString("Name","[{\"text\":\"Mending Items\",\"italic\":false,\"color\":\"dark_aqua\"}]");
         loreList.add(NbtString.of("[{\"text\":\"Currently Mending Items\",\"italic\":false,\"color\":\"green\"}]"));
         loreList.add(NbtString.of("[{\"text\":\"\",\"color\":\"dark_purple\"}]"));
         loreList.add(NbtString.of("[{\"text\":\"Click to toggle OFF\",\"italic\":false,\"color\":\"red\"}]"));
         display.put("Lore",loreList);
         tag.put("display",display);
         gui.setSlot(2,GuiElementBuilder.from(mending));
   
         magicNbt.putInt("mode", 1);
      }
      buildItemLore(item,player.getServer());
   }
   
   public void withdrawXP(ServerPlayerEntity player, ItemStack item, boolean single, BrainJarGui gui){
      if(single){
         int xpToTake = Math.min(LevelUtils.vanillaLevelToTotalXp(player.experienceLevel+1) - player.totalExperience,getEnergy(item));
         addEnergy(item,-xpToTake);
         player.addExperience(xpToTake);
      }else{
         player.addExperience(getEnergy(item));
         setEnergy(item,0);
      }
   
      ItemStack echest = new ItemStack(Items.ENDER_CHEST);
      NbtCompound tag = echest.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Store Levels\",\"italic\":false,\"color\":\"dark_aqua\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click to store \",\"italic\":false,\"color\":\"green\"},{\"text\":\"1\",\"color\":\"aqua\"},{\"text\":\" level\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right click to store all (\",\"italic\":false,\"color\":\"green\"},{\"text\":\""+player.experienceLevel+"\",\"color\":\"aqua\"},{\"text\":\") levels\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      gui.setSlot(0,GuiElementBuilder.from(echest));
   
      ItemStack bottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
      tag = bottle.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Withdraw Levels\",\"italic\":false,\"color\":\"dark_aqua\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click to gain \",\"italic\":false,\"color\":\"green\"},{\"text\":\"1\",\"color\":\"aqua\"},{\"text\":\" level\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right click to take all (\",\"italic\":false,\"color\":\"green\"},{\"text\":\""+getEnergy(item)+"\",\"color\":\"aqua\"},{\"text\":\") XP\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      gui.setSlot(4,GuiElementBuilder.from(bottle));
      
      gui.setSlot(1,new GuiElementBuilder(Items.GREEN_STAINED_GLASS_PANE).setName(Text.literal(getEnergy(item)+" XP Stored").formatted(Formatting.GREEN)));
      gui.setSlot(3,new GuiElementBuilder(Items.GREEN_STAINED_GLASS_PANE).setName(Text.literal(getEnergy(item)+" XP Stored").formatted(Formatting.GREEN)));
      buildItemLore(item,player.getServer());
   }
   
   public void depositXP(ServerPlayerEntity player, ItemStack item, boolean single, BrainJarGui gui){
      int xpToStore;
      if(single){
         int xpDiff = player.totalExperience - LevelUtils.vanillaLevelToTotalXp(player.experienceLevel);
         xpToStore = xpDiff == 0 ? player.totalExperience - LevelUtils.vanillaLevelToTotalXp(player.experienceLevel - 1): xpDiff;
         xpToStore = Math.min(xpToStore, getMaxEnergy(item) - getEnergy(item));
      }else{
         xpToStore = Math.min(player.totalExperience, getMaxEnergy(item) - getEnergy(item));
      }
      addEnergy(item,xpToStore);
      player.addExperience(-xpToStore);
      if(xpToStore > 0 && getEnergy(item) == getMaxEnergy(item)) ArcanaAchievements.grant(player,ArcanaAchievements.BREAK_BANK.id);
   
      ItemStack echest = new ItemStack(Items.ENDER_CHEST);
      NbtCompound tag = echest.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Store Levels\",\"italic\":false,\"color\":\"dark_aqua\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click to store \",\"italic\":false,\"color\":\"green\"},{\"text\":\"1\",\"color\":\"aqua\"},{\"text\":\" level\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right click to store all (\",\"italic\":false,\"color\":\"green\"},{\"text\":\""+player.experienceLevel+"\",\"color\":\"aqua\"},{\"text\":\") levels\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      gui.setSlot(0,GuiElementBuilder.from(echest));
   
      ItemStack bottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
      tag = bottle.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Withdraw Levels\",\"italic\":false,\"color\":\"dark_aqua\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click to gain \",\"italic\":false,\"color\":\"green\"},{\"text\":\"1\",\"color\":\"aqua\"},{\"text\":\" level\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right click to take all (\",\"italic\":false,\"color\":\"green\"},{\"text\":\""+getEnergy(item)+"\",\"color\":\"aqua\"},{\"text\":\") XP\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      gui.setSlot(4,GuiElementBuilder.from(bottle));
      
      gui.setSlot(1,new GuiElementBuilder(Items.GREEN_STAINED_GLASS_PANE).setName(Text.translatable(getEnergy(item)+" XP Stored").formatted(Formatting.GREEN)));
      gui.setSlot(3,new GuiElementBuilder(Items.GREEN_STAINED_GLASS_PANE).setName(Text.translatable(getEnergy(item)+" XP Stored").formatted(Formatting.GREEN)));
      buildItemLore(item,player.getServer());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"     Brain in a Jar\\n\\nRarity: Exotic\\n\\nZombies seem to have a higher level of intelligence compared to other mobs. Their brains also seem capable of storing knowledge over time similar to you or me.\\n\\nIf I can expand their\"}");
      list.add("{\"text\":\"     Brain in a Jar\\n\\ncapacity for knowledge using the extra-dimensional capabilities of Ender Chests it should hold enough XP for practical use.\\n\\nThere should also be a way to incorporate the use of Mending enchantments to have\"}");
      list.add("{\"text\":\"     Brain in a Jar\\n\\ndirect access to the storage.\\n\\nRight Click the Brain in a Jar to open its internal storage where you can set its Mending interaction or deposit or withdraw XP. It has a base-line internal storage of 1 million XP\"}");
      return list;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient h = new MagicItemIngredient(Items.ZOMBIE_HEAD,1,null, true);
      MagicItemIngredient c = new MagicItemIngredient(Items.ENDER_CHEST,32,null);
      MagicItemIngredient e = new MagicItemIngredient(Items.ENDER_EYE,64,null);
      MagicItemIngredient x = new MagicItemIngredient(Items.EXPERIENCE_BOTTLE,64,null);
      ItemStack book = EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(Enchantments.MENDING,1));
      MagicItemIngredient m = new MagicItemIngredient(Items.ENCHANTED_BOOK,1,book.getNbt());
      
      MagicItemIngredient[][] ingredients = {
            {c,e,x,e,c},
            {e,m,x,m,e},
            {x,x,h,x,x},
            {e,m,x,m,e},
            {c,e,x,e,c}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withEnchanter());
   }
   
   public class BrainJarItem extends MagicPolymerItem {
      public BrainJarItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(PolymerResourcePackUtils.hasPack(player)){
            return textureItem;
         }
         return super.getPolymerItem(itemStack, player);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!MagicItemUtils.isMagic(itemStack)) return ArcanaRegistry.MODELS.get(TXT_OFF).value();
         NbtCompound magicNbt = itemStack.getNbt().getCompound("arcananovum");
         int mode = magicNbt.getInt("mode");
         return mode == 1 ? ArcanaRegistry.MODELS.get(TXT_ON).value() : ArcanaRegistry.MODELS.get(TXT_OFF).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         int mode = magicNbt.getInt("mode"); // 0 is off, 1 is on
         if(mode == 1 && getEnergy(stack) != 0){
            // Check each player's inventory for gear that needs repairing
            PlayerInventory inv = player.getInventory();
            for(int i = 0; i < inv.size() && getEnergy(stack) != 0; i++){
               ItemStack tool = inv.getStack(i);
               if(tool.isEmpty())
                  continue;
               if(!tool.hasEnchantments())
                  continue;
               NbtList enchants = tool.getEnchantments();
               boolean hasMending = false;
               for(int j = 0; j < enchants.size(); j++){
                  NbtCompound enchant = enchants.getCompound(j);
                  if(enchant.contains("id")){
                     String id = enchant.getString("id");
                     if(id.equals("minecraft:mending")){
                        hasMending = true;
                        break;
                     }
                  }
               }
               if(hasMending){
                  NbtCompound nbt = tool.getNbt();
                  int durability = nbt != null ? nbt.getInt("Damage") : 0;
                  int repairAmount = 2 + Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.TRADE_SCHOOL.id));
                  if(durability <= 0)
                     continue;
                  int newDura = MathHelper.clamp(durability - repairAmount, 0, Integer.MAX_VALUE);
                  ArcanaAchievements.progress(player,ArcanaAchievements.CERTIFIED_REPAIR.id,durability-newDura);
                  addEnergy(stack,-1);
                  PLAYER_DATA.get(player).addXP(5);
                  buildItemLore(stack,player.getServer());
                  nbt.putInt("Damage", newDura);
                  tool.setNbt(nbt);
               }
            }
         }
         
         if(world.getServer().getTicks() % 1200 == 0 && getEnergy(stack) < getMaxEnergy(stack)){
            double interestRate = .002 * Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.KNOWLEDGE_BANK.id));
            int beforeEnergy = getEnergy(stack);
            addEnergy(stack, (int) (interestRate*beforeEnergy));
            if(beforeEnergy < getMaxEnergy(stack) && getEnergy(stack) >= getMaxEnergy(stack)){
               ArcanaAchievements.grant(player,ArcanaAchievements.BREAK_BANK.id);
            }
            buildItemLore(stack,player.getServer());
         }
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
         openGui(playerEntity, playerEntity.getStackInHand(hand));
         return TypedActionResult.success(playerEntity.getStackInHand(hand));
      }
      
      @Override
      public ActionResult useOnBlock(ItemUsageContext context){
         PlayerEntity playerEntity = context.getPlayer();
         ItemStack stack = context.getStack();
         openGui(playerEntity, stack);
         return ActionResult.SUCCESS;
      }
   }
}
