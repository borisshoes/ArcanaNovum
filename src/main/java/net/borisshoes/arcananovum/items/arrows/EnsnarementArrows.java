package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArrowItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.RepeatTimer;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class EnsnarementArrows extends RunicArrow {
	public static final String ID = "ensnarement_arrows";
   
   public EnsnarementArrows(){
      id = ID;
      name = "Ensnarement Arrows";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity),TomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new EnsnarementArrowsItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW,ResearchTasks.EFFECT_SLOWNESS};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Component.literal("Ensnarement Arrows:").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE));
      lore.add(Component.literal("")
            .append(Component.literal("These ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Runic Arrows").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" restrain ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("a ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("target ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("from ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("moving ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("of their own will.").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Arrows ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("have a ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("reduced effect").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(" on ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("players").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      int entrapment = arrow.getAugment(ArcanaAugments.ENTRAPMENT.id);
      boolean anchor = arrow.getAugment(ArcanaAugments.ETHEREAL_ANCHOR.id) > 0;
      if(entityHitResult.getEntity() instanceof LivingEntity living){
         int duration = living instanceof ServerPlayer ? (entrapment+1) : (entrapment+1)*5;
         living.addEffect(new MobEffectInstance(ArcanaRegistry.ENSNAREMENT_EFFECT, (int) (duration*20),anchor ? 1 : 0),arrow.getOwner());
         
         if(arrow.getOwner() instanceof ServerPlayer player){
            if(living.getAirSupply() <= 0){
               ArcanaAchievements.grant(player, ArcanaAchievements.WATERBOARDING.id);
            }
            
            if(!ArcanaAchievements.isTimerActive(player, ArcanaAchievements.SHACKLED.id)){
               ArcanaAchievements.progress(player, ArcanaAchievements.SHACKLED.id,10);
               BorisLib.addTickTimerCallback(new RepeatTimer(10,121, ()->{
                  if(living.isAlive() && living.getEffect(ArcanaRegistry.ENSNAREMENT_EFFECT) != null){
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
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("   Ensnarement\n       Arrows").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nThese arrows unleash Arcane chains around the target creature. These chains fully stop the creature from moving of their own will. They are still affected by the ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Ensnarement\n       Arrows").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), Component.literal("\nenvironment. Players are affected less.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class EnsnarementArrowsItem extends ArcanaPolymerArrowItem {
      public EnsnarementArrowsItem(){
         super(getThis(),getArcanaArrowItemComponents(5046527));
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

