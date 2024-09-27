package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaNovum;
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
import net.borisshoes.arcananovum.utils.RepeatTimer;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class EnsnarementArrows extends RunicArrow {
	public static final String ID = "ensnarement_arrows";
   
   private static final String TXT = "item/runic_arrow";
   
   public EnsnarementArrows(){
      id = ID;
      name = "Ensnarement Arrows";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EXOTIC,TomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new EnsnarementArrowsItem(new Item.Settings().maxCount(64).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.DARK_PURPLE))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .component(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(),Optional.of(5046527),new ArrayList<>(),Optional.empty()))
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW,ResearchTasks.EFFECT_SLOWNESS};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Text.literal("Ensnarement Arrows:").formatted(Formatting.BOLD,Formatting.DARK_PURPLE));
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.GRAY))
            .append(Text.literal("Runic Arrows").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" restrain ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("a ").formatted(Formatting.GRAY))
            .append(Text.literal("target ").formatted(Formatting.YELLOW))
            .append(Text.literal("from ").formatted(Formatting.GRAY))
            .append(Text.literal("moving ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("of their own will.").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.GRAY))
            .append(Text.literal("Arrows ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("have a ").formatted(Formatting.GRAY))
            .append(Text.literal("reduced effect").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(" on ").formatted(Formatting.GRAY))
            .append(Text.literal("players").formatted(Formatting.YELLOW))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      int entrapment = arrow.getAugment(ArcanaAugments.ENTRAPMENT.id);
      boolean anchor = arrow.getAugment(ArcanaAugments.ETHEREAL_ANCHOR.id) > 0;
      if(entityHitResult.getEntity() instanceof LivingEntity living){
         int duration = living instanceof ServerPlayerEntity ? (entrapment+1) : (entrapment+1)*5;
         living.addStatusEffect(new StatusEffectInstance(ArcanaRegistry.ENSNAREMENT_EFFECT, (int) (duration*20),anchor ? 1 : 0),arrow.getOwner());
         
         if(arrow.getOwner() instanceof ServerPlayerEntity player){
            if(living.getAir() <= 0){
               ArcanaAchievements.grant(player, ArcanaAchievements.WATERBOARDING.id);
            }
            
            if(!ArcanaAchievements.isTimerActive(player, ArcanaAchievements.SHACKLED.id)){
               ArcanaAchievements.progress(player, ArcanaAchievements.SHACKLED.id,10);
               ArcanaNovum.addTickTimerCallback(new RepeatTimer(10,120, ()->{
                  if(living.isAlive() && living.getStatusEffect(ArcanaRegistry.ENSNAREMENT_EFFECT) != null){
                     ArcanaAchievements.progress(player, ArcanaAchievements.SHACKLED.id,10);
                  }else{
                     ArcanaAchievements.reset(player,ArcanaAchievements.SHACKLED.id);
                  }
               },null));
            }
         }
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){}
   
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = ArcanaIngredient.EMPTY;
      ArcanaIngredient c = new ArcanaIngredient(Items.CRYING_OBSIDIAN,24);
      ArcanaIngredient g = new ArcanaIngredient(Items.COBWEB,16);
      ArcanaIngredient h = new ArcanaIngredient(Items.SPECTRAL_ARROW,16);
      ArcanaIngredient k = new ArcanaIngredient(Items.POTION,1).withPotions(Potions.STRONG_SLOWNESS);
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
      list.add(List.of(Text.literal(" Ensnarement Arrows\n\nRarity: Exotic\n\nThese arrows unleash Arcane chains around the target creature.\nThese chains fully stop the creature from moving, while still letting them shift due to environmental factors. Players are affected less.")));
      return list;
   }
   
   public class EnsnarementArrowsItem extends ArcanaPolymerArrowItem {
      public EnsnarementArrowsItem(Item.Settings settings){
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

