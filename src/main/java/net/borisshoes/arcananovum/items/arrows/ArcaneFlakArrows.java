package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArrowItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArcaneFlakArrows extends RunicArrow {
   public static final String ID = "arcane_flak_arrows";
   
   public static final int armTime = 5;
   private static final String TXT = "item/runic_arrow";
   
   
   public ArcaneFlakArrows(){
      id = ID;
      name = "Arcane Flak Arrows";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EXOTIC, TomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new ArcaneFlakArrowsItem(new Item.Settings().maxCount(64).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.literal("Runic Arrows - Arcane Flak").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .component(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(),Optional.of(7802273),new ArrayList<>()))
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW, ResearchTasks.USE_FIREWORK, ResearchTasks.ADVANCEMENT_DRAGON_BREATH};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Text.literal("Arcane Flak Arrows:").formatted(Formatting.BOLD,Formatting.LIGHT_PURPLE));
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Runic Arrows").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" explode").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" when passing by a ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("flying creature").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Deals ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("increased damage").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" to ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("airborne entities").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      double radius = 4 + 1.25*arrow.getAugment(ArcanaAugments.AIRBURST.id);
      detonate(arrow,radius);
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      double radius = 4 + 1.25*arrow.getAugment(ArcanaAugments.AIRBURST.id);
      detonate(arrow,radius);
   }
   
   public static void detonate(PersistentProjectileEntity arrow, double damageRange){
      int deadPhantomCount = 0;
      List<Entity> triggerTargets = arrow.getEntityWorld().getOtherEntities(arrow,arrow.getBoundingBox().expand(damageRange*2),
            e -> !e.isSpectator() && e.distanceTo(arrow) <= damageRange && e instanceof LivingEntity);
      for(Entity entity : triggerTargets){
         if(entity instanceof LivingEntity e){
            float damage = (float) MathHelper.clamp(arrow.getVelocity().length()*4,1,10);
            damage *= (e.isOnGround() ? 0.5f : 3.5f);
            damage *= e.distanceTo(arrow) > damageRange*.75 ? 0.5f : 1;
            DamageSource source = arrow.getDamageSources().explosion(arrow,arrow.getOwner());
            e.damage(source,damage);
            if(e instanceof PhantomEntity && e.isDead()) deadPhantomCount++;
         }
      }
      if(arrow.getOwner() instanceof ServerPlayerEntity player && deadPhantomCount >= 5) ArcanaAchievements.grant(player,ArcanaAchievements.AA_ARTILLERY.id);
   
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         ParticleEffectUtils.arcaneFlakArrowDetonate(serverWorld,arrow.getPos(),damageRange,0);
         SoundUtils.playSound(serverWorld,arrow.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.PLAYERS,1f,1f);
         SoundUtils.playSound(serverWorld,arrow.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE, SoundCategory.PLAYERS,1f,1f);
      }
      arrow.discard();
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = ArcanaIngredient.EMPTY;
      ArcanaIngredient c = new ArcanaIngredient(Items.DRAGON_BREATH,16);
      ArcanaIngredient g = new ArcanaIngredient(Items.FIREWORK_STAR,12);
      ArcanaIngredient h = new ArcanaIngredient(Items.SPECTRAL_ARROW,16);
      ArcanaIngredient k = new ArcanaIngredient(Items.GLOWSTONE_DUST,32);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,g,a},
            {k,h,m,h,k},
            {a,g,h,g,a},
            {a,a,c,a,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withFletchery());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal(" Arcane Flak Arrows\\n\\nRarity: Exotic\\n\\nPhantoms... Scourges of the night sky. I shall create a weapon that strikes fear into their undead hearts.\\nThese arrows detonate when near flying creatures doing massive bonus damage in a brilliant display.")));
      return list;
   }
   
   public class ArcaneFlakArrowsItem extends ArcanaPolymerArrowItem {
      public ArcaneFlakArrowsItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.getModelData(TXT).value();
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType context, RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player){
         return super.getPolymerItemStack(itemStack, context, lookup, player);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}
