package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBowItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.items.arrows.PhotonicArrows;
import net.borisshoes.arcananovum.items.arrows.RunicArrow;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class RunicBow extends ArcanaItem {
	public static final String ID = "runic_bow";

   public RunicBow(){
      id = ID;
      name = "Runic Bow";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.EQUIPMENT};
      vanillaItem = Items.BOW;
      item = new RunicBowItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE);
      researchTasks = new ResourceKey[]{ResearchTasks.ADVANCEMENT_SHOOT_ARROW,ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER,ResearchTasks.UNLOCK_STELLAR_CORE,ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.ADVANCEMENT_SNIPER_DUEL,ResearchTasks.ADVANCEMENT_BULLSEYE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public void finalizePrefItem(MinecraftServer server){
      super.finalizePrefItem(server);
      ItemStack curPrefItem = this.getPrefItem();
      curPrefItem.set(DataComponents.ENCHANTMENTS, MinecraftUtils.makeEnchantComponent(
            new EnchantmentInstance(MinecraftUtils.getEnchantment(server.registryAccess(), Enchantments.POWER),7)
      ));
      this.prefItem = buildItemLore(curPrefItem, server);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Runic Bow").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" makes use of the ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Runic Matrix").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" to create ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("unique effects").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Runic Bow").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" can fire and ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("activate ").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_AQUA))
            .append(Component.literal("the effects of ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Runic Arrows").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("bow").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" also acts as a ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("normal bow").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" with ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Power VII").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" and is ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("unbreakable").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      ItemStack newStack = super.updateItem(stack,server);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public ItemStack forgeItem(Container inv, List<Integer> centerpieces, StarlightForgeBlockEntity starlightForge){
      ItemStack newArcanaItem = getNewItem();
      if(centerpieces.isEmpty()) return newArcanaItem;
      ItemStack bowStack = inv.getItem(centerpieces.getFirst()); // Should be the Bow
      
      if(bowStack.isEnchanted()){
         EnchantmentHelper.setEnchantments(newArcanaItem,bowStack.getEnchantments());
      }
      newArcanaItem.enchant(MinecraftUtils.getEnchantment(Enchantments.POWER),7);
      
      if(hasProperty(bowStack, EnhancedStatUtils.ENHANCED_STAT_TAG)){
         EnhancedStatUtils.enhanceItem(newArcanaItem,getDoubleProperty(bowStack,EnhancedStatUtils.ENHANCED_STAT_TAG));
      }
      
      return newArcanaItem;
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("      Runic Bow").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nThe Runic Bow is truly a masterpiece of adaptive Arcana. The integrated Runic Matrices reconfigure the Bow’s ethereal structure based on the projectile being fired to unlock its Arcane effects.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("      Runic Bow").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), Component.literal("\nThe Runic Bow is capable of utilizing Runic Arrows and activating their special abilities. \n\nThe Bow also enhances normal arrows to do more damage than a traditional enchanted bow.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class RunicBowItem extends ArcanaPolymerBowItem {
      public static final Predicate<ItemStack> RUNIC_BOW_PROJECTILES = stack -> (stack.is(ItemTags.ARROWS) || ArcanaItemUtils.identifyItem(stack) instanceof RunicArrow);
      public static final float[] STABILITY = new float[]{1f, .75f, .5f, 0f};
      
      public RunicBowItem(){
         super(getThis(),getEquipmentArcanaItemComponents());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         List<String> stringList = new ArrayList<>();
         int tier = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.BOW_ACCELERATION.id);
         if(tier > 0) stringList.add("accel_"+tier);
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public Predicate<ItemStack> getAllSupportedProjectiles(){
         return RUNIC_BOW_PROJECTILES;
      }
      
      @Override
      public boolean releaseUsing(ItemStack bow, Level world, LivingEntity user, int remainingUseTicks){
         if(!(user instanceof Player playerEntity)){
            return false;
         }
         ItemStack arrowStack = playerEntity.getProjectile(bow);
         boolean arrowsRunic = ArcanaItemUtils.isRunicArrow(arrowStack);
         if(!arrowStack.isEmpty()){
            float pullPercent = getPullProgress(this.getUseDuration(bow, user) - remainingUseTicks, bow);
            if((double) pullPercent < 0.1){
               return false;
            }
            List<ItemStack> list = draw(bow, arrowStack, playerEntity);
            if(world instanceof ServerLevel serverWorld && !list.isEmpty()){
               float divergence = STABILITY[Math.max(0, ArcanaAugments.getAugmentOnItem(bow,ArcanaAugments.BOW_STABILIZATION.id))];
               this.shoot(serverWorld, playerEntity, playerEntity.getUsedItemHand(), bow, list, pullPercent * 3.0F, divergence, pullPercent == 1.0F, null);
            }
            
            SoundEvent sound = SoundEvents.ARROW_SHOOT;
            float volume = 1.0f;
            
            if(arrowsRunic){
               sound = SoundEvents.TRIDENT_THROW.value();
               volume = 0.8f;
               
               RunicArrow runicArrow = ArcanaItemUtils.identifyRunicArrow(arrowStack);
               if(runicArrow instanceof PhotonicArrows photonArrows){
                  sound = SoundEvents.AMETHYST_BLOCK_HIT;
                  volume = 1.2f;
               }
               
               ArcanaNovum.data(playerEntity).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_RUNIC_ARROW_SHOOT) * list.size());
               if(playerEntity instanceof ServerPlayer player) ArcanaAchievements.progress(player,ArcanaAchievements.JUST_LIKE_ARCHER.id, list.size());
            }
            
            world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), sound, SoundSource.PLAYERS,volume,1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + pullPercent * 0.5F);
            playerEntity.awardStat(Stats.ITEM_USED.get(this));
            return true;
         }
         return false;
      }
      
      protected void shoot(ServerLevel world, LivingEntity shooter, InteractionHand hand, ItemStack bow, List<ItemStack> projectiles, float speed, float divergence, boolean critical, @Nullable LivingEntity target){
         float f = EnchantmentHelper.processProjectileSpread(world, bow, shooter, 0.0F);
         float g = projectiles.size() == 1 ? 0.0F : 2.0F * f / (float)(projectiles.size() - 1);
         float h = (float)((projectiles.size() - 1) % 2) * g / 2.0F;
         float i = 1.0F;
         
         for (int j = 0; j < projectiles.size(); j++){
            ItemStack arrowStack = (ItemStack)projectiles.get(j);
            if(!arrowStack.isEmpty()){
               float k = h + i * (float)((j + 1) / 2) * g;
               i = -i;
               Projectile projectileEntity = this.createProjectile(world, shooter, bow, arrowStack, critical);
               this.shootProjectile(shooter, projectileEntity, j, speed, divergence, k, target);
               world.addFreshEntity(projectileEntity);
               
               if(ArcanaItemUtils.identifyRunicArrow(arrowStack) instanceof PhotonicArrows photonArrows){
                  int alignmentLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(arrowStack, ArcanaAugments.PRISMATIC_ALIGNMENT.id));
                  photonArrows.shoot(world, shooter, projectileEntity, alignmentLvl);
                  projectileEntity.kill(world);
               }
               bow.hurtAndBreak(this.getDurabilityUse(arrowStack), shooter, hand.asEquipmentSlot());
               if(bow.isEmpty()){
                  break;
               }
            }
         }
      }
      
      @Override
      public void onUseTick(Level world, LivingEntity user, ItemStack bow, int remainingUseTicks){
         int accelLvl = ArcanaAugments.getAugmentOnItem(bow,ArcanaAugments.BOW_ACCELERATION.id);
         float prog = getPullProgress(getUseDuration(bow,user)-remainingUseTicks,bow);
         if(accelLvl > 0 && user instanceof ServerPlayer player && prog >= 0.1){
            String t =  "▁▂▃▅▆▇۞";
            char c = t.charAt((int) (Math.max(0,prog*t.length()-1)));
            player.displayClientMessage(Component.literal("")
                  .append(Component.literal("\uD83C\uDFF9 (").withStyle(ChatFormatting.LIGHT_PURPLE))
                  .append(Component.literal(String.valueOf(c)).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(") \uD83C\uDFF9").withStyle(ChatFormatting.LIGHT_PURPLE)),true);
         }
         
         super.onUseTick(world, user, bow, remainingUseTicks);
      }
      
      private float getPullProgress(int useTicks, ItemStack bow){
         float maxPullTicks = 20f;
         ArcanaItem arcanaBow = ArcanaItemUtils.identifyItem(bow);
         if(arcanaBow instanceof RunicBow){
            int accelLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(bow,ArcanaAugments.BOW_ACCELERATION.id));
            final float[] accel = {20,18,17,16,15,10};
            maxPullTicks = accel[accelLvl];
         }
         
         float f = (float)useTicks / maxPullTicks;
         f = (f * f + f * 2.0F) / 3.0F;
         if(f > 1.0F){
            f = 1.0F;
         }
         
         return f;
      }
      
      @Override
      public int getUseDuration(ItemStack stack, LivingEntity user){
         return super.getUseDuration(stack,user);
      }
      
      @Override
      public InteractionResult use(Level world, Player user, InteractionHand hand){
         return super.use(world, user, hand);
      }
   }
}

