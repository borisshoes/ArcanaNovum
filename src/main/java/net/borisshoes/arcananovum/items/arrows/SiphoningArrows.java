package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.callbacks.OverhealTimerCallback;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArrowItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.mixins.AbstractArrowAccessor;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
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

public class SiphoningArrows extends RunicArrow {
	public static final String ID = "siphoning_arrows";
   public static final Identifier EFFECT_ID = Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,ID+".overheal");
   
   private static final int[] overhealCap = {0,2,4,10};
   
   public SiphoningArrows(){
      id = ID;
      name = "Siphoning Arrows";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new SiphoningArrowsItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW, ResearchTasks.ADVANCEMENT_BREW_POTION,ResearchTasks.OBTAIN_GLISTERING_MELON};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Component.literal("Siphoning Arrows:").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED));
      lore.add(Component.literal("")
            .append(Component.literal("These ").withStyle(ChatFormatting.RED))
            .append(Component.literal("Runic Arrows").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" siphon health ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("from hit ").withStyle(ChatFormatting.RED))
            .append(Component.literal("entities").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(".").withStyle(ChatFormatting.RED)));
      lore.add(Component.literal("")
            .append(Component.literal("The amount ").withStyle(ChatFormatting.RED))
            .append(Component.literal("siphoned ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("is proportional to the ").withStyle(ChatFormatting.RED))
            .append(Component.literal("damage ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("dealt.").withStyle(ChatFormatting.RED)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      if(arrow.getOwner() instanceof ServerPlayer player){
         double damage = Mth.ceil(Mth.clamp(arrow.getDeltaMovement().length() * ((AbstractArrowAccessor)arrow).getBaseDamage(), 0.0, 2.147483647E9)) / 5.5;
         damage += arrow.isCritArrow() ? damage/4 : 0;
         
         if(player.getHealth() < 1.5f){
            BorisLib.addTickTimerCallback(player.level(), new GenericTimer(2, () -> {
               if(entityHitResult.getEntity() instanceof Mob mob && mob.isDeadOrDying()) ArcanaAchievements.grant(player,ArcanaAchievements.CIRCLE_OF_LIFE.id);
            }));
         }
         
         int overhealLvl = arrow.getAugment(ArcanaAugments.OVERHEAL.id);
         float overheal = (float) Mth.clamp((damage+player.getHealth()) - player.getMaxHealth(),0,overhealCap[overhealLvl]);
         if(overheal > 0){
            float curAbs = player.getAbsorptionAmount();
            BorisLib.addTickTimerCallback(new OverhealTimerCallback(100,player,overheal));
            SoundUtils.playSongToPlayer(player, SoundEvents.ENCHANTMENT_TABLE_USE, 1, 1.8f);
            MinecraftUtils.addMaxAbsorption(player, SiphoningArrows.EFFECT_ID,overheal);
            player.setAbsorptionAmount((curAbs + overheal));
         }
         
         player.heal((float)damage);
         player.level().sendParticles(ParticleTypes.HEART,player.getX(),player.getY()+player.getBbHeight()/2,player.getZ(),(int)Math.ceil(damage), .5,.5,.5,1);
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){}
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal(" Siphoning Arrows").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nHealth manipulation is something that I have rarely explored. I’ve invoked some simple life runes to draw upon the health lost from my arrows and draw it back to me.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class SiphoningArrowsItem extends ArcanaPolymerArrowItem {
      public SiphoningArrowsItem(){
         super(getThis(),getArcanaArrowItemComponents(15866018));
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

