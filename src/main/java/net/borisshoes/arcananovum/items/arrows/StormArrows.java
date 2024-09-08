package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.areaeffects.AftershockAreaEffectTracker;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArrowItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class StormArrows extends RunicArrow {
	public static final String ID = "storm_arrows";
   
   private static final double[] stormChance = {.1,.2,.4,.6,.8,1};
   private static final String TXT = "item/runic_arrow";
   
   public StormArrows(){
      id = ID;
      name = "Storm Arrows";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EXOTIC, TomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new StormArrowsItem(new Item.Settings().maxCount(64).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.GRAY))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .component(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(),Optional.of(12040354),new ArrayList<>()))
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW, ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER,ResearchTasks.ADVANCEMENT_LIGHTNING_ROD_WITH_VILLAGER_NO_FIRE,ResearchTasks.OBTAIN_LIGHTNING_ROD};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Text.literal("Storm Arrows:").formatted(Formatting.BOLD,Formatting.GRAY));
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.GRAY))
            .append(Text.literal("Runic Arrows").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" channel ").formatted(Formatting.GRAY))
            .append(Text.literal("lightning ").formatted(Formatting.YELLOW))
            .append(Text.literal("from the ").formatted(Formatting.GRAY))
            .append(Text.literal("clouds ").formatted(Formatting.WHITE))
            .append(Text.literal("above.").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Only a ").formatted(Formatting.GRAY))
            .append(Text.literal("small chance").formatted(Formatting.YELLOW))
            .append(Text.literal(" to work when not ").formatted(Formatting.GRAY))
            .append(Text.literal("raining").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      int stableLvl = arrow.getAugment(ArcanaAugments.STORM_STABILIZATION.id);
      int chainLvl = arrow.getAugment(ArcanaAugments.CHAIN_LIGHTNING.id);
      int shockLvl = arrow.getAugment(ArcanaAugments.AFTERSHOCK.id);
      strike(arrow,entityHitResult.getPos(),stableLvl,shockLvl);
      if(chainLvl > 0) chainLightning(arrow,entityHitResult.getEntity(),chainLvl);
      entityHitResult.getEntity().timeUntilRegen = 1;
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      int stableLvl = arrow.getAugment(ArcanaAugments.STORM_STABILIZATION.id);
      int shockLvl = arrow.getAugment(ArcanaAugments.AFTERSHOCK.id);
      strike(arrow,blockHitResult.getPos(),stableLvl,shockLvl);
   }
   
   private void strike(PersistentProjectileEntity arrow, Vec3d pos, int stableLevel, int shockLvl){
      World world = arrow.getEntityWorld();
      if(arrow.isCritical() && (world.isRaining() || world.isThundering() || Math.random() < stormChance[stableLevel])){
         LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, arrow.getEntityWorld());
         lightning.setPosition(pos);
         world.spawnEntity(lightning);
         
         if(arrow.getOwner() instanceof ServerPlayerEntity player){
            ArcanaNovum.addTickTimerCallback(player.getServerWorld(), new GenericTimer(2, () -> {
               if(lightning.getStruckEntities().anyMatch(e -> e instanceof MooshroomEntity)) ArcanaAchievements.grant(player,ArcanaAchievements.SHOCK_THERAPY.id);
            }));
         }
         
         if(shockLvl > 0 && world instanceof ServerWorld serverWorld){
            ArcanaRegistry.AREA_EFFECTS.get(ArcanaRegistry.AFTERSHOCK_AREA_EFFECT_TRACKER.getId()).addSource(AftershockAreaEffectTracker.source(arrow.getOwner(), BlockPos.ofFloored(pos),serverWorld,shockLvl));
            SoundUtils.playSound(world,BlockPos.ofFloored(pos), SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS,.2f,1f);
         }
      }
   }
   
   private void chainLightning(PersistentProjectileEntity arrow, Entity hitEntity, int lvl){
      World world = arrow.getEntityWorld();
      Vec3d pos = hitEntity.getPos();
      double range = 5;
      
      Box rangeBox = new Box(pos.x+8,pos.y+8,pos.z+8,pos.x-8,pos.y-8,pos.z-8);
      List<Entity> entities = world.getOtherEntities(arrow.getOwner(),rangeBox, e -> !e.isSpectator() && e.squaredDistanceTo(pos) < range*range && !(e instanceof PersistentProjectileEntity));
      
      List<LivingEntity> hits = new ArrayList<>();
      if(hitEntity instanceof LivingEntity le) hits.add(le);
      for(Entity entity : entities){
         if(entity instanceof LivingEntity e){
            if(hits.size() < lvl+1){
               if(!hits.isEmpty()){
                  LivingEntity lastHit = hits.get(hits.size() - 1);
                  double dist = lastHit.getPos().distanceTo(e.getPos());
                  
                  if(world instanceof ServerWorld serverWorld)
                     ParticleEffectUtils.line(serverWorld,null,lastHit.getPos().add(0,lastHit.getHeight()/2,0),e.getPos().add(0,e.getHeight()/2,0),ParticleTypes.WAX_OFF,(int)(dist*8),2,0.05,0.05);
               }
               
               DamageSource source = ArcanaDamageTypes.of(world,ArcanaDamageTypes.ARCANE_LIGHTNING,arrow.getOwner(),arrow.getOwner());
               e.timeUntilRegen = 1;
               e.damage(source,6);
               hits.add(e);
            }
         }
      }
      SoundUtils.playSound(world,BlockPos.ofFloored(pos), SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS,.1f,2f);
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = ArcanaIngredient.EMPTY;
      ArcanaIngredient c = new ArcanaIngredient(Items.LIGHTNING_ROD,24);
      ArcanaIngredient g = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.CHANNELING),1));
      ArcanaIngredient h = new ArcanaIngredient(Items.SPECTRAL_ARROW,16);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,g,a},
            {c,h,m,h,c},
            {a,g,h,g,a},
            {a,a,c,a,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withFletchery().withEnchanter());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("     Storm Arrows\n\nRarity: Exotic\n\nThe channeling enchantment requires a storm to use. Throwing a bit of Arcana into it seems to force a storm, abeit briefly. The Matrix doesn't always seem to succeed in activating though.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class StormArrowsItem extends ArcanaPolymerArrowItem {
      public StormArrowsItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.getModelData(TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}

