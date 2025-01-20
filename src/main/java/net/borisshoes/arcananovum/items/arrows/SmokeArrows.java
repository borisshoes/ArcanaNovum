package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.areaeffects.SmokeArrowAreaEffectTracker;
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
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class SmokeArrows extends RunicArrow {
	public static final String ID = "smoke_arrows";
   
   public SmokeArrows(){
      id = ID;
      name = "Smoke Arrows";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new SmokeArrowsItem(addArcanaItemComponents(new Item.Settings().maxCount(64)
            .component(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(),Optional.of(6908265),new ArrayList<>(),Optional.empty()))
      ));
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.DARK_GRAY);
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW,ResearchTasks.KILL_SQUID,ResearchTasks.USE_CAMPFIRE,ResearchTasks.ADVANCEMENT_DRAGON_BREATH,ResearchTasks.EFFECT_BLINDNESS,ResearchTasks.EFFECT_WEAKNESS};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Text.literal("Smoke Arrows:").formatted(Formatting.BOLD,Formatting.DARK_GRAY));
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.GRAY))
            .append(Text.literal("Runic Arrows").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" emit ").formatted(Formatting.GRAY))
            .append(Text.literal("smoke").formatted(Formatting.DARK_GRAY))
            .append(Text.literal(" particles near where they land.").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Smoke").formatted(Formatting.DARK_GRAY))
            .append(Text.literal(" gives ").formatted(Formatting.GRAY))
            .append(Text.literal("blindness").formatted(Formatting.DARK_GRAY))
            .append(Text.literal(" and ").formatted(Formatting.GRAY))
            .append(Text.literal("weakness").formatted(Formatting.DARK_GRAY))
            .append(Text.literal(" to those inside it.").formatted(Formatting.GRAY)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         float range = (float) MathHelper.clamp(arrow.getVelocity().length()*.8,.3,2.5);
         int gasLvl = arrow.getAugment(ArcanaAugments.TEAR_GAS.id);
         ArcanaRegistry.AREA_EFFECTS.get(ArcanaRegistry.SMOKE_ARROW_AREA_EFFECT_TRACKER.getId()).addSource(SmokeArrowAreaEffectTracker.source(arrow.getOwner(),entityHitResult.getEntity(),null,null,range,gasLvl));
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         float range = (float) MathHelper.clamp(arrow.getVelocity().length()*.8,.3,2.5);
         int gasLvl = arrow.getAugment(ArcanaAugments.TEAR_GAS.id);
         ArcanaRegistry.AREA_EFFECTS.get(ArcanaRegistry.SMOKE_ARROW_AREA_EFFECT_TRACKER.getId()).addSource(SmokeArrowAreaEffectTracker.source(arrow.getOwner(),null,blockHitResult.getBlockPos(),serverWorld,range,gasLvl));
      }
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = ArcanaIngredient.EMPTY;
      ArcanaIngredient c = new ArcanaIngredient(Items.CAMPFIRE,12);
      ArcanaIngredient g = new ArcanaIngredient(Items.GLOW_INK_SAC,12);
      ArcanaIngredient h = new ArcanaIngredient(Items.SPECTRAL_ARROW,16);
      ArcanaIngredient i = new ArcanaIngredient(Items.INK_SAC,12);
      ArcanaIngredient k = new ArcanaIngredient(Items.LINGERING_POTION,1).withPotions(Potions.LONG_WEAKNESS);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,i,a},
            {k,h,m,h,k},
            {a,i,h,g,a},
            {a,a,c,a,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withFletchery());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("   Smoke Arrows").formatted(Formatting.DARK_GRAY,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nThis Runic Matrix has been configured to summon copious amounts of smoke. Those inside will have trouble seeing and even breathing, making it harder to land a solid blow.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class SmokeArrowsItem extends ArcanaPolymerArrowItem {
      public SmokeArrowsItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}

