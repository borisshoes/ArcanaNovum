package net.borisshoes.arcananovum.items;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.gui.brainjar.BrainJarGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class BrainJar extends EnergyItem {
	public static final String ID = "brain_jar";
   public static final int[] capacities = {1000000,2000000,4000000,6000000,8000000,10000000};
   private static final String TXT_ON = "item/brain_jar_on";
   private static final String TXT_OFF = "item/brain_jar_off";
   private static final Item textureItem = Items.TINTED_GLASS;
   
   public BrainJar(){
      id = ID;
      name = "Brain in a Jar";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EXOTIC, TomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.ZOMBIE_HEAD;
      item = new BrainJarItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.literal("Brain in a Jar").formatted(Formatting.BOLD,Formatting.GREEN))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      models = new ArrayList<>();
      models.add(new Pair<>(textureItem,TXT_OFF));
      models.add(new Pair<>(textureItem,TXT_ON));
      researchTasks = new RegistryKey[]{ResearchTasks.USE_ENDER_CHEST,ResearchTasks.BREAK_SCULK,ResearchTasks.LEVEL_100,ResearchTasks.ACTIVATE_MENDING,ResearchTasks.OBTAIN_BOTTLES_OF_ENCHANTING,ResearchTasks.OBTAIN_ZOMBIE_HEAD,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER,ResearchTasks.UNLOCK_TWILIGHT_ANVIL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,ACTIVE_TAG,false);
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("A ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("zombie").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(" has more aptitude for storing ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("knowledge").formatted(Formatting.GREEN))
            .append(Text.literal(" than most mobs.").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Containing its ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("brain").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(" in a jar could serve as ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("XP storage").formatted(Formatting.GREEN))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("It should also be capable of activating the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("mending").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" enchantment.").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Right click").formatted(Formatting.AQUA))
            .append(Text.literal(" to configure.").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal(""));
      
      boolean mending = itemStack != null && getBooleanProperty(itemStack, ACTIVE_TAG);
      int xp = itemStack != null ? getEnergy(itemStack) : 0;
      
      Text mendText = mending ? Text.literal("ON").formatted(Formatting.DARK_GREEN) : Text.literal("OFF").formatted(Formatting.RED);
      
      lore.add(Text.literal("")
            .append(Text.literal(xp+" XP Stored - Mending ").formatted(Formatting.GREEN))
            .append(mendText));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      int capLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.UNENDING_WISDOM.id));
      return capacities[capLvl];
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      boolean active = getBooleanProperty(stack,ACTIVE_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,ACTIVE_TAG,active);
      return buildItemLore(newStack,server);
   }
   
   public void openGui(PlayerEntity playerEntity, ItemStack stack){
      if(!(playerEntity instanceof ServerPlayerEntity player))
         return;
      BrainJarGui gui = new BrainJarGui(ScreenHandlerType.HOPPER,player,this, stack);
      gui.makeGui();
      gui.open();
   }
   
   public void toggleMending(BrainJarGui gui, ServerPlayerEntity player, ItemStack stack){
      boolean active = !getBooleanProperty(stack,ACTIVE_TAG);
      putProperty(stack,ACTIVE_TAG,active);
      buildItemLore(stack,player.getServer());
      gui.makeGui();
   }
   
   public void withdrawXP(ServerPlayerEntity player, ItemStack stack, boolean single, BrainJarGui gui){
      if(single){
         int xpToTake = Math.min(LevelUtils.vanillaLevelToTotalXp(player.experienceLevel+1) - player.totalExperience,getEnergy(stack));
         addEnergy(stack,-xpToTake);
         player.addExperience(xpToTake);
      }else{
         player.addExperience(getEnergy(stack));
         setEnergy(stack,0);
      }
      
      gui.makeGui();
      buildItemLore(stack,player.getServer());
   }
   
   public void depositXP(ServerPlayerEntity player, ItemStack stack, boolean single, BrainJarGui gui){
      int xpToStore;
      if(single){
         int xpDiff = player.totalExperience - LevelUtils.vanillaLevelToTotalXp(player.experienceLevel);
         xpToStore = xpDiff == 0 ? player.totalExperience - LevelUtils.vanillaLevelToTotalXp(player.experienceLevel - 1): xpDiff;
         xpToStore = Math.min(xpToStore, getMaxEnergy(stack) - getEnergy(stack));
      }else{
         xpToStore = Math.min(player.totalExperience, getMaxEnergy(stack) - getEnergy(stack));
      }
      addEnergy(stack,xpToStore);
      player.addExperience(-xpToStore);
      if(xpToStore > 0 && getEnergy(stack) >= getMaxEnergy(stack)) ArcanaAchievements.grant(player,ArcanaAchievements.BREAK_BANK.id);
      
      gui.makeGui();
      buildItemLore(stack,player.getServer());
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("     Brain in a Jar\n\nRarity: Exotic\n\nZombies seem to have a higher level of intelligence compared to other mobs. Their brains also seem capable of storing knowledge over time similar to you or me.\n\nIf I can expand their").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("     Brain in a Jar\n\ncapacity for knowledge using the extra-dimensional capabilities of Ender Chests it should hold enough XP for practical use.\n\nThere should also be a way to incorporate the use of Mending enchantments to have").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("     Brain in a Jar\n\ndirect access to the storage.\n\nRight Click the Brain in a Jar to open its internal storage where you can set its Mending interaction or deposit or withdraw XP. It has a base-line internal storage of 1 million XP").formatted(Formatting.BLACK)));
      return list;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.ENDER_CHEST,4);
      ArcanaIngredient b = new ArcanaIngredient(Items.EXPERIENCE_BOTTLE,8);
      ArcanaIngredient c = new ArcanaIngredient(Items.SCULK,16);
      ArcanaIngredient g = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.MENDING),1));
      ArcanaIngredient h = new ArcanaIngredient(Items.SCULK_CATALYST,8);
      ArcanaIngredient m = new ArcanaIngredient(Items.ZOMBIE_HEAD,1, true);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withAnvil().withEnchanter());
   }
   
   public class BrainJarItem extends ArcanaPolymerItem {
      public BrainJarItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(PolymerResourcePackUtils.hasMainPack(player)){
            return textureItem;
         }
         return super.getPolymerItem(itemStack, player);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!ArcanaItemUtils.isArcane(itemStack)) return ArcanaRegistry.getModelData(TXT_OFF).value();
         boolean active = getBooleanProperty(itemStack,ACTIVE_TAG);
         return active ? ArcanaRegistry.getModelData(TXT_ON).value() : ArcanaRegistry.getModelData(TXT_OFF).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         boolean active = getBooleanProperty(stack,ACTIVE_TAG);
         if(active && getEnergy(stack) != 0){
            // Check each player's inventory for gear that needs repairing
            PlayerInventory inv = player.getInventory();
            for(int i = 0; i < inv.size() && getEnergy(stack) != 0; i++){
               ItemStack tool = inv.getStack(i);
               if(tool.isEmpty())
                  continue;
               if(!tool.hasEnchantments())
                  continue;
               
               boolean hasMending = EnchantmentHelper.hasAnyEnchantmentsWith(tool, EnchantmentEffectComponentTypes.REPAIR_WITH_XP);
               
               if(hasMending){
                  int durability = tool.getDamage();
                  int repairAmount = (int) Math.ceil((EnchantmentHelper.getRepairWithXp(player.getServerWorld(), tool, 1) * (1 + 0.5 * Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.TRADE_SCHOOL.id)))));
                  if(durability <= 0 || !tool.isDamageable())
                     continue;
                  int newDura = MathHelper.clamp(durability - repairAmount, 0, Integer.MAX_VALUE);
                  ArcanaAchievements.progress(player,ArcanaAchievements.CERTIFIED_REPAIR.id,durability-newDura);
                  addEnergy(stack,-1);
                  PLAYER_DATA.get(player).addXP(5);
                  buildItemLore(stack,player.getServer());
                  tool.setDamage(newDura);
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

