package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.borisshoes.borislib.utils.AlgoUtils;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ShadowStalkersGlaive extends EnergyItem {
	public static final String ID = "shadow_stalkers_glaive";
   
   public static final String TETHER_TARGET_TAG = "tetherTarget";
   public static final String TETHER_TIME_TAG = "tetherTime";
   
   private final int teleportLength = 10;
   
   public ShadowStalkersGlaive(){
      id = ID;
      name = "Shadow Stalkers Glaive";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.EQUIPMENT};
      vanillaItem = Items.NETHERITE_SWORD;
      item = new ShadowStalkersGlaiveItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.NUL_COLOR);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_NETHERITE_SWORD,ResearchTasks.OBTAIN_NETHER_STAR,ResearchTasks.USE_ENDER_PEARL,ResearchTasks.ADVANCEMENT_KILL_A_MOB,ResearchTasks.UNLOCK_STELLAR_CORE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,TETHER_TIME_TAG,-1);
      putProperty(stack,TETHER_TARGET_TAG,"");
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("This ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("blade ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("lets you move through your opponents ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("shadow").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("blade ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("stores the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("blood ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("from every strike and uses it as ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("energy").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Stride ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("through the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("darkness ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("behind your opponent or ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("blink forward").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" to ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("teleport ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("behind ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("your most recently attacked foe.").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right Click").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" to ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("teleport ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("a ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("short distance").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      return 100; // 100 damage stored
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      String target = getStringProperty(stack,TETHER_TARGET_TAG);
      int time = getIntProperty(stack,TETHER_TIME_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,TETHER_TARGET_TAG,target);
      putProperty(newStack,TETHER_TIME_TAG,time);
      return buildItemLore(newStack,server);
   }
   
   public void entityAttacked(Player player, ItemStack stack, Entity entity){
      if(entity instanceof Mob || entity instanceof Player){
         putProperty(stack,TETHER_TARGET_TAG,entity.getStringUUID());
         putProperty(stack,TETHER_TIME_TAG,60);
      }
   }
   
   @Override
   public ItemStack forgeItem(Container inv, List<Integer> centerpieces, StarlightForgeBlockEntity starlightForge){
      ItemStack newArcanaItem = getNewItem();
      if(centerpieces.isEmpty()) return newArcanaItem;
      ItemStack toolStack = inv.getItem(centerpieces.getFirst()); // Should be the Sword
      
      if(toolStack.isEnchanted()){
         EnchantmentHelper.setEnchantments(newArcanaItem,toolStack.getEnchantments());
      }
      
      if(hasProperty(toolStack,EnhancedStatUtils.ENHANCED_STAT_TAG)){
         EnhancedStatUtils.enhanceItem(newArcanaItem,getDoubleProperty(toolStack,EnhancedStatUtils.ENHANCED_STAT_TAG));
      }
      return newArcanaItem;
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("  Shadow Stalkers\n       Glaive").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.NUL_COLOR), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nThis Blade was forged to mimic the power of Endermen to teleport and relentlessly pursue foes. However, instead of using Ender particles to warp through dimensions, this Glaive  ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Shadow Stalkers\n       Glaive").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.NUL_COLOR), Component.literal("\nrelies on a mechanism I came up with after my studies in the Nether. Using the Glaive lets me fall through the shadows and emerge elsewhere. The feeling is far different from Ender-based teleportation, such as ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Shadow Stalkers\n       Glaive").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.NUL_COLOR), Component.literal("\nan Ender Pearl. \n\nBlood that is spilled on the Glaive gets soaked up by its shadowy surface.\nStriking and killing foes grants Glaive Charges.\n\n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Shadow Stalkers\n       Glaive").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.NUL_COLOR), Component.literal("\nSneak Use consumes one Charge to blink forward and emerge from the shadows 10 blocks in the direction of my gaze. \n\nThe Glaive remembers the last target it struck, and Using the ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Shadow Stalkers\n       Glaive").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.NUL_COLOR), Component.literal("\nGlaive consumes four Charges to emerge behind the target.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class ShadowStalkersGlaiveItem extends ArcanaPolymerItem {
      public ShadowStalkersGlaiveItem(){
         super(getThis(), getEquipmentArcanaItemComponents()
               .component(DataComponents.ATTACK_RANGE, new AttackRange(0.5f,3.75f,0.5f,5.75f,0.075f,0.8f))
               .sword(ToolMaterial.NETHERITE, 3.0F, -2.4F)
               .component(DataComponents.WEAPON, new Weapon(1,0.75f))
         );
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(entity instanceof ServerPlayer player)) return;
         if(world.getServer().getTickCount() % (20) == 0){
            
            String targetID = getStringProperty(stack,TETHER_TARGET_TAG);
            if(targetID != null && !targetID.isEmpty()){
               Entity target = player.level().getEntity(AlgoUtils.getUUID(targetID));
               if(target == null || !target.isAlive() || player.level().dimension() != target.level().dimension()){
                  putProperty(stack,TETHER_TIME_TAG,-1);
                  putProperty(stack,TETHER_TARGET_TAG,"");
               }
            }
            
            int tetherTime = getIntProperty(stack,TETHER_TIME_TAG);
            if(tetherTime > 0){
               putProperty(stack,TETHER_TIME_TAG,tetherTime-1);
            }else if(tetherTime == 0){
               putProperty(stack,TETHER_TIME_TAG,-1);
               putProperty(stack,TETHER_TARGET_TAG,"");
            }
            
            if(world.getServer().getTickCount() % (100) == 0){
               int energy = getEnergy(stack);
               boolean recharge = false;
               if(energy < 20){
                  recharge = true;
               }else if(energy < getMaxEnergy(stack) && ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.BLOODLETTER.id) >= 1 && player.getHealth() > 2){
                  recharge = true;
                  if(!player.isCreative() && !player.isSpectator()) player.setHealth(player.getHealth() - 2);
               }
               if(recharge){
                  addEnergy(stack, 20);
                  StringBuilder message = new StringBuilder("Glaive Charges: ");
                  for(int i = 1; i <= 5; i++){
                     message.append(getEnergy(stack) >= i * 20 ? "✦ " : "✧ ");
                  }
                  player.displayClientMessage(Component.literal(message.toString()).withColor(ArcanaColors.NUL_COLOR), true);
               }
            }
         }
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         if(!(playerEntity instanceof ServerPlayer player))
            return InteractionResult.PASS;
         
         int energy = getEnergy(stack);
         String tetherTarget = getStringProperty(stack,TETHER_TARGET_TAG);
         
         if(tetherTarget != null && !tetherTarget.isEmpty() && !player.isShiftKeyDown()){
            if(energy >= 80){
               Entity target = player.level().getEntity(AlgoUtils.getUUID(tetherTarget));
               if(target == null || !target.isAlive() || player.level().dimension() != target.level().dimension()){
                  player.displayClientMessage(Component.literal("The Glaive Has No Target").withColor(ArcanaColors.NUL_COLOR),true);
               }else{
                  Vec3 targetPos = target.position();
                  Vec3 targetView = target.getForward();
                  Vec3 tpPos = targetPos.add(targetView.multiply(-1.5,0,-1.5));
                  
                  ArcanaEffectUtils.shadowGlaiveTp(player.level(),player);
                  player.teleport(new TeleportTransition(player.level(),tpPos.add(0,0.25,0), Vec3.ZERO, target.getYRot(),target.getXRot(), TeleportTransition.DO_NOTHING));
                  ArcanaEffectUtils.shadowGlaiveTp(player.level(),player);
                  SoundUtils.playSound(world,player.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS,.8f,.8f);
                  addEnergy(stack,-80);
                  String message = "Glaive Charges: ";
                  for(int i=1; i<=5; i++){
                     message += getEnergy(stack) >= i*20 ? "✦ " : "✧ ";
                  }
                  player.displayClientMessage(Component.literal(message).withColor(ArcanaColors.NUL_COLOR),true);
                  ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_SHADOW_STALKERS_GLAIVE_STALK)); // Add xp
                  
                  if(target instanceof ServerPlayer || target instanceof Warden) ArcanaAchievements.progress(player,ArcanaAchievements.OMAE_WA.id,0);
                  if(target instanceof Mob){
                     if(ArcanaAchievements.isTimerActive(player,ArcanaAchievements.SHADOW_FURY.id)){
                        if(ArcanaAchievements.getProgress(player,ArcanaAchievements.SHADOW_FURY.id) % 2 == 1){
                           ArcanaAchievements.progress(player,ArcanaAchievements.SHADOW_FURY.id,1);
                        }
                     }else{
                        ArcanaAchievements.progress(player,ArcanaAchievements.SHADOW_FURY.id,0);
                     }
                  }
                  
                  int blindDur = new int[]{0,20,40,100}[Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.PARANOIA.id))];
                  int invisDur = new int[]{0,20,40,100}[Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SHADOW_STRIDE.id))];
                  MobEffectInstance invis = new MobEffectInstance(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT, invisDur, 0, false, false, true);
                  player.addEffect(invis);
                  if(target instanceof LivingEntity living){
                     MobEffectInstance blind = new MobEffectInstance(ArcanaRegistry.GREATER_BLINDNESS_EFFECT, blindDur, 5, false, true, true);
                     living.addEffect(blind);
                  }
                  
                  return InteractionResult.SUCCESS_SERVER;
               }
            }else{
               player.displayClientMessage(Component.literal("The Glaive Needs At Least 4 Charges").withColor(ArcanaColors.NUL_COLOR),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1,0.8f);
            }
         }else if(player.isShiftKeyDown()){
            if(energy >= 20){
               Vec3 playerPos = player.position();
               Vec3 view = player.getForward();
               Vec3 tpPos = playerPos.add(view.scale(teleportLength));
               
               ArcanaEffectUtils.shadowGlaiveTp(player.level(),player);
               player.teleport(new TeleportTransition(player.level(),tpPos.add(0,0.25,0), Vec3.ZERO, player.getYRot(),player.getXRot(), TeleportTransition.DO_NOTHING));
               ArcanaEffectUtils.shadowGlaiveTp(player.level(),player);
               SoundUtils.playSound(world,player.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS,.8f,.8f);
               addEnergy(stack,-20);
               String message = "Glaive Charges: ";
               for(int i=1; i<=5; i++){
                  message += getEnergy(stack) >= i*20 ? "✦ " : "✧ ";
               }
               player.displayClientMessage(Component.literal(message).withColor(ArcanaColors.NUL_COLOR),true);
               ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_SHADOW_STALKERS_GLAIVE_BLINK)); // Add xp
               
               int invisDur = new int[]{0,20,40,100}[Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SHADOW_STRIDE.id))];
               MobEffectInstance invis = new MobEffectInstance(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT, invisDur, 0, false, false, true);
               player.addEffect(invis);
               
               return InteractionResult.SUCCESS_SERVER;
            }else{
               player.displayClientMessage(Component.literal("The Glaive Needs At Least 1 Charge").withColor(ArcanaColors.NUL_COLOR),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1,0.8f);
            }
         }
         return InteractionResult.PASS;
      }
   }
}

