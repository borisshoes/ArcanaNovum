package net.borisshoes.arcananovum.items;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.gui.brainjar.BrainJarGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class BrainJar extends EnergyItem {
	public static final String ID = "brain_jar";
   public static final int[] capacities = {1000000,2000000,4000000,6000000,8000000,10000000};
   private static final Item textureItem = Items.TINTED_GLASS;
   
   public BrainJar(){
      id = ID;
      name = "Brain in a Jar";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.ZOMBIE_HEAD;
      item = new BrainJarItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN);
      researchTasks = new ResourceKey[]{ResearchTasks.USE_ENDER_CHEST,ResearchTasks.BREAK_SCULK,ResearchTasks.LEVEL_100,ResearchTasks.ACTIVATE_MENDING,ResearchTasks.OBTAIN_BOTTLES_OF_ENCHANTING,ResearchTasks.OBTAIN_ZOMBIE_HEAD,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER,ResearchTasks.UNLOCK_TWILIGHT_ANVIL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,ACTIVE_TAG,false);
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("A ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("zombie").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(" has more aptitude for storing ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("knowledge").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" than most mobs.").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Containing its ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("brain").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(" in a jar could serve as ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("XP storage").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("It should also be capable of activating the ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("mending").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" enchantment.").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Right click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to deposit or withdraw ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("XP").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to toggle ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Mending").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" interaction.").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal(""));
      
      boolean mending = itemStack != null && getBooleanProperty(itemStack, ACTIVE_TAG);
      int xp = itemStack != null ? getEnergy(itemStack) : 0;
      
      Component mendText = mending ? Component.literal("ON").withStyle(ChatFormatting.DARK_GREEN) : Component.literal("OFF").withStyle(ChatFormatting.RED);
      
      lore.add(Component.literal("")
            .append(Component.literal(LevelUtils.readableInt(xp)+" XP Stored - Mending ").withStyle(ChatFormatting.GREEN))
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
   
   public void openGui(Player playerEntity, ItemStack stack){
      if(!(playerEntity instanceof ServerPlayer player))
         return;
      BrainJarGui gui = new BrainJarGui(MenuType.HOPPER,player,this, stack);
      gui.makeGui();
      gui.open();
   }
   
   public void toggleMending(BrainJarGui gui, ServerPlayer player, ItemStack stack){
      boolean active = !getBooleanProperty(stack,ACTIVE_TAG);
      putProperty(stack,ACTIVE_TAG,active);
      buildItemLore(stack,player.level().getServer());
      gui.makeGui();
   }
   
   public void withdrawXP(ServerPlayer player, ItemStack stack, boolean single, BrainJarGui gui){
      if(single){
         int xpToTake = Math.min(LevelUtils.vanillaLevelToTotalXp(player.experienceLevel+1) - player.totalExperience,getEnergy(stack));
         addEnergy(stack,-xpToTake);
         player.giveExperiencePoints(xpToTake);
      }else{
         player.giveExperiencePoints(getEnergy(stack));
         setEnergy(stack,0);
      }
      
      gui.makeGui();
      buildItemLore(stack,player.level().getServer());
   }
   
   public void depositXP(ServerPlayer player, ItemStack stack, boolean single, BrainJarGui gui){
      int xpToStore;
      if(single){
         int xpDiff = player.totalExperience - LevelUtils.vanillaLevelToTotalXp(player.experienceLevel);
         xpToStore = xpDiff == 0 ? player.totalExperience - LevelUtils.vanillaLevelToTotalXp(player.experienceLevel - 1): xpDiff;
         xpToStore = Math.min(xpToStore, getMaxEnergy(stack) - getEnergy(stack));
      }else{
         xpToStore = Math.min(player.totalExperience, getMaxEnergy(stack) - getEnergy(stack));
      }
      addEnergy(stack,xpToStore);
      player.giveExperiencePoints(-xpToStore);
      if(xpToStore > 0 && getEnergy(stack) >= getMaxEnergy(stack)) ArcanaAchievements.grant(player,ArcanaAchievements.BREAK_BANK.id);
      
      gui.makeGui();
      buildItemLore(stack,player.level().getServer());
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("   Brain in a Jar").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nZombies seem to have a higher level of intelligence compared to other mobs. Their brains also seem capable of storing knowledge over time, similar to you and me.\nIf I can expand their capacity for ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Brain in a Jar").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), Component.literal("\nknowledge using the storage capabilities of Ender Chests, it should hold enough XP for practical use.\n\nThere should also be a way to incorporate the use of the Mending enchantment to have direct access to the storage.\n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Brain in a Jar").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), Component.literal("\nUse the Brain in a Jar to open its internal storage, where you can deposit or withdraw XP.\n \nSneak Use to toggle the Jar’s Mending interaction.\n\nThe Jar can store 1 million XP Points.\n").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class BrainJarItem extends ArcanaPolymerItem {
      public BrainJarItem(){
         super(getThis());
      }
      
      @Override
      public Item getPolymerItem(ItemStack itemStack, PacketContext context){
         if(PolymerResourcePackUtils.hasMainPack(context.getPlayer())){
            return textureItem;
         }
         return super.getPolymerItem(itemStack, context);
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         boolean active = getBooleanProperty(itemStack,ACTIVE_TAG);
         
         List<String> stringList = new ArrayList<>();
         if(active){
            stringList.add("on");
         }else{
            stringList.add("off");
         }
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerLevel && entity instanceof ServerPlayer player)) return;
         boolean active = getBooleanProperty(stack,ACTIVE_TAG);
         if(active && getEnergy(stack) != 0){
            // Check each player's inventory for gear that needs repairing
            Inventory inv = player.getInventory();
            for(int i = 0; i < inv.getContainerSize() && getEnergy(stack) != 0; i++){
               ItemStack tool = inv.getItem(i);
               if(tool.isEmpty())
                  continue;
               if(!tool.isEnchanted())
                  continue;
               
               boolean hasMending = EnchantmentHelper.has(tool, EnchantmentEffectComponents.REPAIR_WITH_XP);
               
               if(hasMending){
                  int durability = tool.getDamageValue();
                  int repairAmount = (int) Math.ceil((EnchantmentHelper.modifyDurabilityToRepairFromXp(player.level(), tool, 1) * (1 + 0.5 * Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.TRADE_SCHOOL.id)))));
                  if(durability <= 0 || !tool.isDamageableItem())
                     continue;
                  int newDura = Mth.clamp(durability - repairAmount, 0, Integer.MAX_VALUE);
                  ArcanaAchievements.progress(player,ArcanaAchievements.CERTIFIED_REPAIR.id,durability-newDura);
                  addEnergy(stack,-1);
                  ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_BRAIN_JAR_MEND_PER_XP));
                  buildItemLore(stack,player.level().getServer());
                  tool.setDamageValue(newDura);
               }
            }
         }
         
         if(world.getServer().getTickCount() % 1200 == 0 && getEnergy(stack) < getMaxEnergy(stack)){
            double interestRate = .002 * Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.KNOWLEDGE_BANK.id));
            int beforeEnergy = getEnergy(stack);
            addEnergy(stack, (int) (interestRate*beforeEnergy));
            if(beforeEnergy < getMaxEnergy(stack) && getEnergy(stack) >= getMaxEnergy(stack)){
               ArcanaAchievements.grant(player,ArcanaAchievements.BREAK_BANK.id);
            }
            buildItemLore(stack,player.level().getServer());
         }
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
         if(playerEntity.isShiftKeyDown()){
            boolean active = !getBooleanProperty(stack,ACTIVE_TAG);
            putProperty(stack,ACTIVE_TAG,active);
            if(active){
               playerEntity.displayClientMessage(Component.literal("The Jar's Experience Mends").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.EXPERIENCE_ORB_PICKUP, .5f,1.3f);
            }else{
               playerEntity.displayClientMessage(Component.literal("The Jar's Experience Withdraws").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.EXPERIENCE_ORB_PICKUP, .5f,0.7f);
            }
         }else{
            openGui(playerEntity, stack);
         }
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Override
      public InteractionResult useOn(UseOnContext context){
         Player playerEntity = context.getPlayer();
         ItemStack stack = context.getItemInHand();
         openGui(playerEntity, stack);
         return InteractionResult.SUCCESS_SERVER;
      }
   }
}

