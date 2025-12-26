package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArrowItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class TetherArrows extends RunicArrow {
	public static final String ID = "tether_arrows";
   
   public TetherArrows(){
      id = ID;
      name = "Tether Arrows";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new TetherArrowsItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW,ResearchTasks.RIPTIDE_TRIDENT,ResearchTasks.FISH_MOB,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Component.literal("Tether Arrows:").withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY));
      lore.add(Component.literal("")
            .append(Component.literal("These ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Runic Arrows ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("pull").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" you to a block like a ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("grappling hook").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("They will also ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("pull").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" a hit ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("entity ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("towards you.").withStyle(ChatFormatting.AQUA)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      if(arrow.getData().getBooleanOr("severed", false)) return;
      if(arrow.getOwner() instanceof ServerPlayer player && entityHitResult.getEntity() instanceof LivingEntity entity){
         Vec3 hitPos = entityHitResult.getLocation();
         
         BorisLib.addTickTimerCallback(player.level(), new GenericTimer(1, () -> {
            Vec3 motion = player.position().subtract(hitPos);
            Vec3 horizBoost = motion.multiply(1,0,1).normalize().scale(1.5);
            motion = motion.add(horizBoost);
            double verticalMotion = motion.y < -3 ? (player.getY() - entity.getY())*.3 : velFromHeight(motion.y)/20;
            Vec3 velocity = new Vec3(velFromLength(motion.x)*2.0/9.0,verticalMotion,velFromLength(motion.z)*2.0/9.0);
            entity.setDeltaMovement(velocity);
            if(entity instanceof ServerPlayer targetPlayer) targetPlayer.connection.send(new ClientboundSetEntityMotionPacket(targetPlayer));
            
            ArcanaEffectUtils.tetherArrowEntity(player.level(),entity,player);
            SoundUtils.playSound(arrow.level(),player.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_1, SoundSource.PLAYERS,.8f,.6f);
         }));
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      if(arrow.getData().getBooleanOr("severed", false)) return;
      if(arrow.getOwner() instanceof ServerPlayer player){
         Vec3 hitPos = blockHitResult.getLocation();
         Vec3 motion = hitPos.subtract(player.position());
         Vec3 horizBoost = motion.multiply(1,0,1).normalize().scale(1.5);
         //motion = motion.add(horizBoost);
         Vec3 velocity = new Vec3(velFromLength(motion.x)*2.0/9.0,velFromHeight(motion.y)/20,velFromLength(motion.z)*2.0/9.0);
         player.setDeltaMovement(velocity);
         player.connection.send(new ClientboundSetEntityMotionPacket(player));
         ArcanaEffectUtils.tetherArrowGrapple(player.level(),player,blockHitResult.getLocation());
         SoundUtils.playSound(arrow.level(),player.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_2, SoundSource.PLAYERS,.8f,.6f);
         
         if(motion.y >= 12) ArcanaAchievements.progress(player,ArcanaAchievements.SPIDERMAN.id,1);
      }
      
   }
   
   
   // TODO Redo the funny physics
   private double velFromLength(double d){
      double a = .96; // Drag
      return -20*d*Math.log(a);
   }
   
   private double velFromHeight(double h){
      double a = .98; // Drag
      double b = .08; // Gravity
      if(h < 0) h = 0;
      if(h < 1){
         h += 0.5;
      }else if(h > 1){
         h += 1.5;
      }
      
      double exp = -Math.pow(a,(h/(a*b) - h/b));
      double n = 20*a*b*(lambertNeg(exp/Math.E)+1);
      return n/(a-1);
   }
   
   
   private double appx1(double x, double y){
      return x - (x*Math.exp(x)-y)/((x+1)*Math.exp(x));
   }
   
   private double appx2(double x, double y){
      return appx1(appx1(appx1(appx1(appx1(appx1(appx1(appx1(x,y),y),y),y),y),y),y),y);
   }
   
   private double lambertNeg(double y){
      return y > -1/Math.E ? (y > 0 ? 0 : appx2(appx2(appx2(appx2(appx2(appx2(appx2(appx2(-2,y),y),y),y),y),y),y),y)) : 0;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = ArcanaIngredient.EMPTY;
      ArcanaIngredient c = new ArcanaIngredient(Items.STRING,32);
      ArcanaIngredient g = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentInstance(MinecraftUtils.getEnchantment(Enchantments.RIPTIDE),3));
      ArcanaIngredient h = new ArcanaIngredient(Items.SPECTRAL_ARROW,16);
      ArcanaIngredient i = new ArcanaIngredient(Items.POTION,1).withPotions(Potions.STRONG_LEAPING);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,i,a},
            {c,h,m,h,c},
            {a,i,h,g,a},
            {a,a,c,a,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withFletchery().withEnchanter());
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("   Tether Arrows").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nThrough precise kinematic equations formed on the Matrix, these Arrows should form the perfect tether to pull me to the location I shoot. It can also pull creatures towards me.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class TetherArrowsItem extends ArcanaPolymerArrowItem {
      public TetherArrowsItem(){
         super(getThis(),getArcanaArrowItemComponents(10724259));
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
}

