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
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class TrackingArrows extends RunicArrow {
	public static final String ID = "tracking_arrows";
   
   public TrackingArrows(){
      id = ID;
      name = "Tracking Arrows";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity),TomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new TrackingArrowsItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.YELLOW);
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW,ResearchTasks.USE_ENDER_EYE,ResearchTasks.ADVANCEMENT_USE_LODESTONE,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Text.literal("Tracking Arrows:").formatted(Formatting.BOLD,Formatting.YELLOW));
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.AQUA))
            .append(Text.literal("Runic Arrows").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" lock on").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(" to a ").formatted(Formatting.AQUA))
            .append(Text.literal("creature ").formatted(Formatting.YELLOW))
            .append(Text.literal("in front of it.").formatted(Formatting.AQUA)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      int broadheads = arrow.getAugment(ArcanaAugments.BROADHEADS.id);
      if(entityHitResult.getEntity() instanceof LivingEntity living && broadheads > 0){
         living.addStatusEffect(new StatusEffectInstance(ArcanaRegistry.DAMAGE_AMP_EFFECT,broadheads*100,1),arrow.getOwner());
      }
      NbtCompound arrowData = arrow.getData();
      if(arrow.getOwner() instanceof ServerPlayerEntity player && arrowData.contains("initPos")){
         NbtList posList = arrowData.getListOrEmpty("initPos");
         Vec3d initPos = new Vec3d(posList.getDouble(0,0.0),posList.getDouble(1,0.0),posList.getDouble(2,0.0));
         double dist = initPos.multiply(1,0,1).distanceTo(arrow.getPos().multiply(1,0,1));
         if(dist >= 250){
            ArcanaAchievements.grant(player,ArcanaAchievements.ACTUAL_AIMBOT.id);
         }
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){}
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = ArcanaIngredient.EMPTY;
      ArcanaIngredient c = new ArcanaIngredient(Items.ENDER_EYE,16);
      ArcanaIngredient g = new ArcanaIngredient(Items.COMPASS,16);
      ArcanaIngredient h = new ArcanaIngredient(Items.SPECTRAL_ARROW,16);
      ArcanaIngredient k = new ArcanaIngredient(Items.NETHER_STAR,2);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,g,a},
            {k,h,m,h,k},
            {a,g,h,g,a},
            {a,a,c,a,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withFletchery().withEnchanter());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal(" Tracking Arrows").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nThese arrows take advantage of Ender Eyes’ ability to home in on a location. \nThe arrow will look ahead a short distance and correct its angle to head for a creature in sight.\n").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class TrackingArrowsItem extends ArcanaPolymerArrowItem {
      public TrackingArrowsItem(){
         super(getThis(),getArcanaArrowItemComponents(16777063));
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}

