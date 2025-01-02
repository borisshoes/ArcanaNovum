package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.callbacks.OverhealTimerCallback;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArrowItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class SiphoningArrows extends RunicArrow {
	public static final String ID = "siphoning_arrows";
   public static final Identifier EFFECT_ID = Identifier.of(ArcanaNovum.MOD_ID,ID+".overheal");
   
   private static final int[] overhealCap = {0,2,4,10};
   
   public SiphoningArrows(){
      id = ID;
      name = "Siphoning Arrows";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new SiphoningArrowsItem(addArcanaItemComponents(new Item.Settings().maxCount(64)
            .component(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(),Optional.of(15866018),new ArrayList<>(),Optional.empty()))
      ));
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.DARK_RED);
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW, ResearchTasks.ADVANCEMENT_BREW_POTION,ResearchTasks.OBTAIN_GLISTERING_MELON};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Text.literal("Siphoning Arrows:").formatted(Formatting.BOLD,Formatting.DARK_RED));
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.RED))
            .append(Text.literal("Runic Arrows").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" siphon health ").formatted(Formatting.DARK_RED))
            .append(Text.literal("from hit ").formatted(Formatting.RED))
            .append(Text.literal("entities").formatted(Formatting.GOLD))
            .append(Text.literal(".").formatted(Formatting.RED)));
      lore.add(Text.literal("")
            .append(Text.literal("The amount ").formatted(Formatting.RED))
            .append(Text.literal("siphoned ").formatted(Formatting.DARK_RED))
            .append(Text.literal("is proportional to the ").formatted(Formatting.RED))
            .append(Text.literal("damage ").formatted(Formatting.GOLD))
            .append(Text.literal("dealt.").formatted(Formatting.RED)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      if(arrow.getOwner() instanceof ServerPlayerEntity player){
         double damage = MathHelper.ceil(MathHelper.clamp(arrow.getVelocity().length() * arrow.getDamage(), 0.0, 2.147483647E9)) / 5.5;
         damage += arrow.isCritical() ? damage/4 : 0;
         
         if(player.getHealth() < 1.5f){
            ArcanaNovum.addTickTimerCallback(player.getServerWorld(), new GenericTimer(2, () -> {
               if(entityHitResult.getEntity() instanceof MobEntity mob && mob.isDead()) ArcanaAchievements.grant(player,ArcanaAchievements.CIRCLE_OF_LIFE.id);
            }));
         }
         
         int overhealLvl = arrow.getAugment(ArcanaAugments.OVERHEAL.id);
         float overheal = (float) MathHelper.clamp((damage+player.getHealth()) - player.getMaxHealth(),0,overhealCap[overhealLvl]);
         if(overheal > 0){
            float curAbs = player.getAbsorptionAmount();
            ArcanaNovum.addTickTimerCallback(new OverhealTimerCallback(100,player,overheal));
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1.8f);
            MiscUtils.addMaxAbsorption(player, SiphoningArrows.EFFECT_ID,overheal);
            player.setAbsorptionAmount((curAbs + overheal));
         }
         
         player.heal((float)damage);
         player.getServerWorld().spawnParticles(ParticleTypes.HEART,player.getX(),player.getY()+player.getHeight()/2,player.getZ(),(int)Math.ceil(damage), .5,.5,.5,1);
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){}
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = ArcanaIngredient.EMPTY;
      ArcanaIngredient c = new ArcanaIngredient(Items.POTION,1).withPotions(Potions.STRONG_HARMING);
      ArcanaIngredient g = new ArcanaIngredient(Items.FERMENTED_SPIDER_EYE,16);
      ArcanaIngredient h = new ArcanaIngredient(Items.SPECTRAL_ARROW,16);
      ArcanaIngredient i = new ArcanaIngredient(Items.GLISTERING_MELON_SLICE,16);
      ArcanaIngredient k = new ArcanaIngredient(Items.POTION,1).withPotions(Potions.STRONG_HEALING);
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
      list.add(List.of(Text.literal(" Siphoning Arrows").formatted(Formatting.DARK_RED,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nHealth manipulation is something that I have rarely explored. I’ve invoked some simple life runes to draw upon the health lost from my arrows and draw it back to me.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class SiphoningArrowsItem extends ArcanaPolymerArrowItem {
      public SiphoningArrowsItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}

