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
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BlinkArrows extends RunicArrow {
	public static final String ID = "blink_arrows";
   
   private static final int[] phaseDur = {0,20,60,100};
   private static final String TXT = "item/runic_arrow";
   
   public BlinkArrows(){
      id = ID;
      name = "Blink Arrows";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EXOTIC, TomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new BlinkArrowsItem(new Item.Settings().maxCount(64).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.literal("Runic Arrows - Blink").formatted(Formatting.BOLD,Formatting.DARK_AQUA))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .component(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(),Optional.of(1404502),new ArrayList<>()))
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW,ResearchTasks.USE_ENDER_PEARL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Text.literal("Blink Arrows:").formatted(Formatting.BOLD,Formatting.DARK_AQUA));
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("Runic Arrows").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" take after ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("Ender Pearls").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.DARK_GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("Upon ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("impact ").formatted(Formatting.GREEN))
            .append(Text.literal("or ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("hitting a target").formatted(Formatting.GREEN))
            .append(Text.literal(" you get ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("teleported").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" to the ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("arrow").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_GREEN)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      if(arrow.getOwner() instanceof ServerPlayerEntity player){
         Vec3d tpPos = entityHitResult.getPos();
         if(tpPos.distanceTo(player.getPos()) >= 100) ArcanaAchievements.grant(player,ArcanaAchievements.NOW_YOU_SEE_ME.id);
         player.teleport(player.getServerWorld(),tpPos.x,tpPos.y+0.25,tpPos.z,player.getYaw(),player.getPitch());
         ParticleEffectUtils.blinkArrowTp(player.getServerWorld(),player.getPos());
         SoundUtils.playSound(arrow.getWorld(),player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS,.8f,.9f);
         
         int phaseLvl = arrow.getAugment(ArcanaAugments.PHASE_IN.id);
         StatusEffectInstance invuln = new StatusEffectInstance(StatusEffects.RESISTANCE,phaseDur[phaseLvl], 3, false, false, true);
         player.addStatusEffect(invuln);
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      if(arrow.getOwner() instanceof ServerPlayerEntity player){
         Vec3d offset = new Vec3d(blockHitResult.getSide().getUnitVector());
         Vec3d tpPos = blockHitResult.getPos().add(offset);
         if(tpPos.distanceTo(player.getPos()) >= 100) ArcanaAchievements.grant(player,ArcanaAchievements.NOW_YOU_SEE_ME.id);
         player.teleport(player.getServerWorld(),tpPos.x,tpPos.y+0.25,tpPos.z,player.getYaw(),player.getPitch());
         ParticleEffectUtils.blinkArrowTp(player.getServerWorld(),player.getPos());
         SoundUtils.playSound(arrow.getWorld(),player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS,.8f,.9f);
         
         int phaseLvl = arrow.getAugment(ArcanaAugments.PHASE_IN.id);
         StatusEffectInstance invuln = new StatusEffectInstance(StatusEffects.RESISTANCE,phaseDur[phaseLvl], 3, false, false, true);
         player.addStatusEffect(invuln);
      }
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = ArcanaIngredient.EMPTY;
      ArcanaIngredient c = new ArcanaIngredient(Items.ENDER_PEARL,8);
      ArcanaIngredient h = new ArcanaIngredient(Items.SPECTRAL_ARROW,16);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,c,h,c,a},
            {c,h,m,h,c},
            {a,c,h,c,a},
            {a,a,c,a,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withFletchery());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("      Blink Arrows\n\nRarity: Exotic\n\nThe Runic Matrix has been configured for invoking teleportation spells, and now the arrows act like a thrown Ender Pearl when activated by a Runic Bow.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class BlinkArrowsItem extends ArcanaPolymerArrowItem {
      public BlinkArrowsItem(Item.Settings settings){
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

