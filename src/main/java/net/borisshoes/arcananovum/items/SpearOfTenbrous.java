package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.entities.SpearOfTenbrousEntity;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ProjectileItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class SpearOfTenbrous extends ArcanaItem {
   public static final String ID = "spear_of_tenbrous";
   
   public static final String SPEAR_ID_TAG = "spearID";
   
   public SpearOfTenbrous(){
      id = ID;
      name = "Spear Of Tenbrous";
      rarity = ArcanaRarity.DIVINE;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.EQUIPMENT};
      itemVersion = 0;
      vanillaItem = Items.NETHERITE_SWORD;
      item = new SpearOfTenbrousItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.DARK_GREEN,Formatting.BOLD);
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_SPEAR_OF_TENBROUS};
      attributions = new Pair[]{new Pair<>(Text.translatable("credits_and_attribution.arcananovum.texture_by"),Text.literal("Magirush"))};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,SPEAR_ID_TAG,"");
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("last remnant").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(" of the long lost ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("progenitor ").formatted(Formatting.GREEN))
            .append(Text.literal("of the ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("end").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("spear ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("pulses with ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("lightning ").formatted(Formatting.GREEN))
            .append(Text.literal("filled by ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("hate ").formatted(Formatting.DARK_RED))
            .append(Text.literal("and ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("vitriol").formatted(Formatting.DARK_RED))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Wielding ").formatted(Formatting.GREEN))
            .append(Text.literal("the ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("spear ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("gives additional ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("attack range").formatted(Formatting.DARK_RED))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Throwing ").formatted(Formatting.GREEN))
            .append(Text.literal("the ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("spear").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(" ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("immediately ").formatted(Formatting.DARK_RED))
            .append(Text.literal("returns it to your ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("hand").formatted(Formatting.GREEN))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Throwing ").formatted(Formatting.GREEN))
            .append(Text.literal("the ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("spear ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("renders it ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("unthrowable ").formatted(Formatting.DARK_RED))
            .append(Text.literal("for a ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("short period").formatted(Formatting.GREEN))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Creatures ").formatted(Formatting.GREEN))
            .append(Text.literal("impaled ").formatted(Formatting.DARK_RED))
            .append(Text.literal("by the ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("spear ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("are ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("stunned").formatted(Formatting.GREEN))
            .append(Text.literal(" briefly.").formatted(Formatting.DARK_GRAY)));
      
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
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("Spear Of Tenbrous").formatted(Formatting.DARK_GREEN,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nWhat little I know of Tenbrous is of their unending vitriol and hatred. It seems as though they were involved in the creation of the End, as well as its current state of destruction.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("Spear Of Tenbrous").formatted(Formatting.DARK_GREEN,Formatting.BOLD),Text.literal("\nFrom what I gather, Tenbrous was superseded by Enderia in some sort of ritual. Talk about cruelty begetting more cruelty… \nIt interests me as to why Enderia chose to keep this last relic of her predecessor.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("Spear Of Tenbrous").formatted(Formatting.DARK_GREEN,Formatting.BOLD),Text.literal("\nWielding the Spear fills me with a nauseating feeling of disgust, as well as a surge of intoxicating power. Its unusually long reach lets me outrange most foes in melee combat.\n\nI can throw the Spear and it will immediately ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("Spear Of Tenbrous").formatted(Formatting.DARK_GREEN,Formatting.BOLD),Text.literal("\nturn into a black smoke that returns to my hand and reforms.\n\nHowever, it takes me a moment to gather myself enough to throw the Spear again. \nA creature impaled by ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("Spear Of Tenbrous").formatted(Formatting.DARK_GREEN,Formatting.BOLD),Text.literal("\nthe Spear is stunned for a moment, overcome with agony.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class SpearOfTenbrousItem extends ArcanaPolymerItem implements ProjectileItem {
      public SpearOfTenbrousItem(){
         super(getThis(),getEquipmentArcanaItemComponents()
               .component(DataComponentTypes.TOOL, new ToolComponent(List.of(), 1.0F, 2, false))
               .component(DataComponentTypes.CONSUMABLE, ConsumableComponent.builder().consumeSeconds(72000).useAction(UseAction.SPEAR).sound(Registries.SOUND_EVENT.getEntry(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME)).build())
               .attributeModifiers(AttributeModifiersComponent.builder()
                     .add(EntityAttributes.ATTACK_DAMAGE, new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 10.0, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                     .add(EntityAttributes.ATTACK_SPEED, new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -3.0F, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                     .add(EntityAttributes.ENTITY_INTERACTION_RANGE, new EntityAttributeModifier(Identifier.of(MOD_ID,id), 1.0F, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
                     .build())
               .enchantable(15)
         );
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
      }
      
      @Override
      public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
         if(!(user instanceof PlayerEntity playerEntity)) return false;
         
         int i = this.getMaxUseTime(stack, user) - remainingUseTicks;
         if(i < 10) return false;
         if (stack.willBreakNextUse()) return false;
         
         RegistryEntry<SoundEvent> registryEntry = EnchantmentHelper.getEffect(stack, EnchantmentEffectComponentTypes.TRIDENT_SOUND).orElse(SoundEvents.ITEM_TRIDENT_THROW);
         playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
         if (world instanceof ServerWorld serverWorld) {
            stack.damage(1, playerEntity);
            SpearOfTenbrousEntity spearEntity = ProjectileEntity.spawnWithVelocity(SpearOfTenbrousEntity::new, serverWorld, stack, playerEntity, 0.0F, 3.5F, 0.1F);
            if(!playerEntity.isInCreativeMode()){
               playerEntity.getInventory().removeOne(stack);
            }
            
            world.playSoundFromEntity(null, spearEntity, registryEntry.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
            return true;
         }
         
         return false;
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand) {
         ItemStack stack = playerEntity.getStackInHand(hand);
         if(!(playerEntity instanceof ServerPlayerEntity player)) return ActionResult.PASS;
         if (stack.willBreakNextUse()) {
            return ActionResult.FAIL;
         } else {
            playerEntity.setCurrentHand(hand);
            return ActionResult.CONSUME;
         }
      }
      
      @Override
      public float getBonusAttackDamage(Entity target, float baseAttackDamage, DamageSource damageSource){
         if(target.getType().isIn(ArcanaRegistry.TENBROUS_BONUS_DAMAGE)){
            return baseAttackDamage * 0.25f;
         }else{
            return 0;
         }
      }
      
      @Override
      public void postDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
         stack.damage(1, attacker, EquipmentSlot.MAINHAND);
      }
      
      @Override
      public UseAction getUseAction(ItemStack stack) {
         return UseAction.SPEAR;
      }
      
      @Override
      public int getMaxUseTime(ItemStack stack, LivingEntity user) {
         return 72000;
      }
      
      @Override
      public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction){
         return null;
      }
      
      @Override
      public ProjectileItem.Settings getProjectileSettings(){
         return ProjectileItem.super.getProjectileSettings();
      }
      
      @Override
      public void initializeProjectile(ProjectileEntity entity, double x, double y, double z, float power, float uncertainty){
         ProjectileItem.super.initializeProjectile(entity, x, y, z, power, uncertainty);
      }
   }
}