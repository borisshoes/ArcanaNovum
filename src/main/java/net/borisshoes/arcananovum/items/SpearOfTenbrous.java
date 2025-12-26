package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.entities.SpearOfTenbrousEntity;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class SpearOfTenbrous extends ArcanaItem {
   public static final String ID = "spear_of_tenbrous";
   
   public static final String SPEAR_ID_TAG = "spearID";
   public static final String HAND_TAG = "hand";
   
   public SpearOfTenbrous(){
      id = ID;
      name = "Spear Of Tenbrous";
      rarity = ArcanaRarity.DIVINE;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.EQUIPMENT};
      itemVersion = 0;
      vanillaItem = Items.NETHERITE_SWORD;
      item = new SpearOfTenbrousItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_SPEAR_OF_TENBROUS};
      attributions = new Tuple[]{new Tuple<>(Component.translatable("credits_and_attribution.arcananovum.texture_by"), Component.literal("Magirush"))};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,SPEAR_ID_TAG,"");
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("last remnant").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(" of the long lost ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("progenitor ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("of the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("end").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("spear ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("pulses with ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("lightning ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("filled by ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("hate ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("and ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("vitriol").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Wielding ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("spear ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("gives additional ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("attack range").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Throwing ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("spear").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(" ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("immediately ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("returns it to your ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("hand").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Throwing ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("spear ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("renders it ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("unthrowable ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("for a ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("short period").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Creatures ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("impaled ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("by the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("spear ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("are ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("stunned").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" briefly.").withStyle(ChatFormatting.DARK_GRAY)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack onAugment(ItemStack stack, ArcanaAugment augment, int level){
      if(ArcanaItemUtils.identifyItem(stack) instanceof SpearOfTenbrous){
         if(augment == ArcanaAugments.STARLESS_DOMAIN && level >= 1){
            EnhancedStatUtils.enhanceItem(stack,1);
         }
      }
      return stack;
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("Spear Of Tenbrous").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nWhat little I know of Tenbrous is of their unending vitriol and hatred. It seems as though they were involved in the creation of the End, as well as its current state of destruction.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Spear Of Tenbrous").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), Component.literal("\nFrom what I gather, Tenbrous was superseded by Enderia in some sort of ritual. Talk about cruelty begetting more cruelty… \nIt interests me as to why Enderia chose to keep this last relic of her predecessor.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Spear Of Tenbrous").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), Component.literal("\nWielding the Spear fills me with a nauseating feeling of disgust, as well as a surge of intoxicating power. Its unusually long reach lets me outrange most foes in melee combat.\n\nI can throw the Spear and it will immediately ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Spear Of Tenbrous").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), Component.literal("\nturn into a black smoke that returns to my hand and reforms.\n\nHowever, it takes me a moment to gather myself enough to throw the Spear again. \nA creature impaled by ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Spear Of Tenbrous").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), Component.literal("\nthe Spear is stunned for a moment, overcome with agony.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class SpearOfTenbrousItem extends ArcanaPolymerItem implements ProjectileItem {
      public SpearOfTenbrousItem(){
         super(getThis(),getEquipmentArcanaItemComponents()
               .component(DataComponents.TOOL, new Tool(List.of(), 1.0F, 2, false))
               .component(DataComponents.ATTACK_RANGE,new AttackRange(1.0f,4.75f,1.0f,6.75f,0.1375f,0.5f))
               .component(DataComponents.DAMAGE_TYPE, new EitherHolder<>(ArcanaDamageTypes.ARCANE_LIGHTNING))
               .component(DataComponents.MINIMUM_ATTACK_CHARGE, 1.0f)
               .component(DataComponents.PIERCING_WEAPON, new PiercingWeapon(true, false, Optional.of(SoundEvents.SPEAR_ATTACK), Optional.of(SoundEvents.SPEAR_HIT)))
               .component(DataComponents.SWING_ANIMATION, new SwingAnimation(SwingAnimationType.STAB, 20))
               .component(DataComponents.USE_EFFECTS, new UseEffects(true,true,1.0f))
               .attributes(ItemAttributeModifiers.builder()
                     .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 6.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                     .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.0F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                     .build())
               .enchantable(15)
         );
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerLevel && entity instanceof ServerPlayer player)) return;
         if(slot == null){
            stack.remove(DataComponents.KINETIC_WEAPON);
            stack.remove(DataComponents.CONSUMABLE);
            ArcanaItem.removeProperty(stack,HAND_TAG);
         }else if(slot == EquipmentSlot.MAINHAND){
            stack.remove(DataComponents.KINETIC_WEAPON);
            stack.set(DataComponents.CONSUMABLE, Consumable.builder().consumeSeconds(72000).animation(ItemUseAnimation.TRIDENT).sound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.AMETHYST_BLOCK_CHIME)).build());
         }else if(slot == EquipmentSlot.OFFHAND){
            stack.remove(DataComponents.CONSUMABLE);
            stack.set(DataComponents.KINETIC_WEAPON, new KineticWeapon(
                        10,
                        10,
                        KineticWeapon.Condition.ofAttackerSpeed(175, 7.0f),
                        KineticWeapon.Condition.ofAttackerSpeed(350, 5.1f),
                        KineticWeapon.Condition.ofRelativeSpeed(525, 4.6f),
                        0.38f,
                        1.25f,
                        Optional.of(SoundEvents.SPEAR_USE),
                        Optional.of(SoundEvents.SPEAR_HIT)));
         }
      }
      
      @Override
      public boolean releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
         if(stack.has(DataComponents.KINETIC_WEAPON)) return super.releaseUsing(stack,world,user,remainingUseTicks);
         if(!(user instanceof Player playerEntity)) return false;
         
         int i = this.getUseDuration(stack, user) - remainingUseTicks;
         if(i < 10) return false;
         if (stack.nextDamageWillBreak()) return false;
         
         Holder<SoundEvent> registryEntry = EnchantmentHelper.pickHighestLevel(stack, EnchantmentEffectComponents.TRIDENT_SOUND).orElse(SoundEvents.TRIDENT_THROW);
         playerEntity.awardStat(Stats.ITEM_USED.get(this));
         if (world instanceof ServerLevel serverWorld) {
            stack.hurtWithoutBreaking(1, playerEntity);
            int slot = playerEntity.getInventory().findSlotMatchingItem(stack);
            SpearOfTenbrousEntity spearEntity = Projectile.spawnProjectileFromRotation(SpearOfTenbrousEntity::new, serverWorld, stack, playerEntity, 0.0F, 3.5F, 0.1F);
            spearEntity.setSlot(slot);
            if(!playerEntity.hasInfiniteMaterials()){
               playerEntity.getInventory().removeItem(stack);
            }
            
            world.playSound(null, spearEntity, registryEntry.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            return true;
         }
         
         return false;
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand) {
         ItemStack stack = playerEntity.getItemInHand(hand);
         if(stack.has(DataComponents.KINETIC_WEAPON)){
            InteractionResult sup = super.use(world,playerEntity,hand);
            if(sup == InteractionResult.CONSUME || sup == InteractionResult.SUCCESS){
               if(ArcanaItem.getStringProperty(stack,HAND_TAG).equals(InteractionHand.MAIN_HAND.name())) playerEntity.releaseUsingItem();
               ArcanaItem.putProperty(stack,HAND_TAG,hand.name());
            }
            return sup;
         }
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
         if (stack.nextDamageWillBreak()) {
            return InteractionResult.FAIL;
         } else {
            playerEntity.startUsingItem(hand);
            if(ArcanaItem.getStringProperty(stack,HAND_TAG).equals(InteractionHand.OFF_HAND.name())) playerEntity.releaseUsingItem();
            ArcanaItem.putProperty(stack,HAND_TAG,hand.name());
            return InteractionResult.CONSUME;
         }
      }
      
      @Override
      public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks){
         super.onUseTick(world, user, stack, remainingUseTicks);
      }
      
      @Override
      public float getAttackDamageBonus(Entity target, float baseAttackDamage, DamageSource damageSource){
         if(target.getType().is(ArcanaRegistry.TENBROUS_BONUS_DAMAGE)){
            return baseAttackDamage * 0.25f;
         }else{
            return 0;
         }
      }
      
      @Override
      public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
         stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
      }
      
      @Override
      public ItemUseAnimation getUseAnimation(ItemStack stack) {
         if(stack.has(DataComponents.KINETIC_WEAPON)) return ItemUseAnimation.SPEAR;
         return ItemUseAnimation.TRIDENT;
      }
      
      @Override
      public int getUseDuration(ItemStack stack, LivingEntity user) {
         if(stack.has(DataComponents.KINETIC_WEAPON)) return super.getUseDuration(stack,user);
         return 72000;
      }
      
      @Override
      public Projectile asProjectile(Level world, Position pos, ItemStack stack, Direction direction){
         return null;
      }
      
      @Override
      public ProjectileItem.DispenseConfig createDispenseConfig(){
         return ProjectileItem.super.createDispenseConfig();
      }
      
      @Override
      public void shoot(Projectile entity, double x, double y, double z, float power, float uncertainty){
         ProjectileItem.super.shoot(entity, x, y, z, power, uncertainty);
      }
   }
}