package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
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
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.potion.Potions;
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

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class TetherArrows extends RunicArrow {
	public static final String ID = "tether_arrows";
   
   private static final String TXT = "item/runic_arrow";
   
   public TetherArrows(){
      id = ID;
      name = "Tether Arrows";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EMPOWERED, TomeGui.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new TetherArrowsItem(new Item.Settings().maxCount(64).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.GRAY))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .component(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(),Optional.of( 10724259),new ArrayList<>()))
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.OBTAIN_SPECTRAL_ARROW,ResearchTasks.RIPTIDE_TRIDENT,ResearchTasks.FISH_MOB,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      addRunicArrowLore(lore);
      lore.add(Text.literal("Tether Arrows:").formatted(Formatting.BOLD,Formatting.GRAY));
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.YELLOW))
            .append(Text.literal("Runic Arrows ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("pull").formatted(Formatting.AQUA))
            .append(Text.literal(" you to a block like a ").formatted(Formatting.YELLOW))
            .append(Text.literal("grappling hook").formatted(Formatting.GRAY))
            .append(Text.literal(".").formatted(Formatting.YELLOW)));
      lore.add(Text.literal("")
            .append(Text.literal("They will also ").formatted(Formatting.YELLOW))
            .append(Text.literal("pull").formatted(Formatting.AQUA))
            .append(Text.literal(" a hit ").formatted(Formatting.YELLOW))
            .append(Text.literal("entity ").formatted(Formatting.GRAY))
            .append(Text.literal("towards you.").formatted(Formatting.AQUA)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      if(arrow.getData().getBoolean("severed")) return;
      if(arrow.getOwner() instanceof ServerPlayerEntity player && entityHitResult.getEntity() instanceof LivingEntity entity){
         Vec3d hitPos = entityHitResult.getPos();
         
         ArcanaNovum.addTickTimerCallback(player.getServerWorld(), new GenericTimer(1, () -> {
            Vec3d motion = player.getPos().subtract(hitPos);
            Vec3d horizBoost = motion.multiply(1,0,1).normalize().multiply(1.5);
            motion = motion.add(horizBoost);
            double verticalMotion = motion.y < -3 ? (player.getY() - entity.getY())*.3 : velFromHeight(motion.y)/20;
            Vec3d velocity = new Vec3d(velFromLength(motion.x)*2.0/9.0,verticalMotion,velFromLength(motion.z)*2.0/9.0);
            entity.setVelocity(velocity);
            if(entity instanceof ServerPlayerEntity targetPlayer) targetPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(targetPlayer));
            
            ParticleEffectUtils.tetherArrowEntity(player.getServerWorld(),entity,player);
            SoundUtils.playSound(arrow.getWorld(),player.getBlockPos(), SoundEvents.ITEM_TRIDENT_RIPTIDE_1, SoundCategory.PLAYERS,.8f,.6f);
         }));
      }
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      if(arrow.getData().getBoolean("severed")) return;
      if(arrow.getOwner() instanceof ServerPlayerEntity player){
         Vec3d hitPos = blockHitResult.getPos();
         Vec3d motion = hitPos.subtract(player.getPos());
         Vec3d horizBoost = motion.multiply(1,0,1).normalize().multiply(1.5);
         //motion = motion.add(horizBoost);
         Vec3d velocity = new Vec3d(velFromLength(motion.x)*2.0/9.0,velFromHeight(motion.y)/20,velFromLength(motion.z)*2.0/9.0);
         player.setVelocity(velocity);
         player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
         ParticleEffectUtils.tetherArrowGrapple(player.getServerWorld(),player,blockHitResult.getPos());
         SoundUtils.playSound(arrow.getWorld(),player.getBlockPos(), SoundEvents.ITEM_TRIDENT_RIPTIDE_2, SoundCategory.PLAYERS,.8f,.6f);
         
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
      ArcanaIngredient g = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.RIPTIDE),3));
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
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("    Tether Arrows\n\nRarity: Empowered\n\nThrough precise math equations inscribed within the Matrix, these Arrows should create the perfect magical tether to pull me to the location I shot. It can also pull creatures to me. It sucks to miss though.\n").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class TetherArrowsItem extends ArcanaPolymerArrowItem {
      public TetherArrowsItem(Item.Settings settings){
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

