package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerArrowItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TetherArrows extends RunicArrow {
   
   private static final String TXT = "item/runic_arrow";
   
   public TetherArrows(){
      id = "tether_arrows";
      name = "Tether Arrows";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new TetherArrowsItem(new FabricItemSettings().maxCount(64).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name", "[{\"text\":\"Runic Arrows - Tether\",\"italic\":false,\"bold\":true,\"color\":\"gray\"}]");
      tag.put("display", display);
      tag.put("Enchantments", enchants);
      tag.putInt("CustomPotionColor", 10724259);
      tag.putInt("HideFlags", 255);
      stack.setCount(64);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      stack.setNbt(addMagicNbt(tag));
      setPrefStack(stack);
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      addRunicArrowLore(loreList);
      loreList.add(NbtString.of("[{\"text\":\"Tether Arrows:\",\"italic\":false,\"bold\":true,\"color\":\"gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"Runic Arrows \",\"color\":\"light_purple\"},{\"text\":\"pull\",\"color\":\"aqua\"},{\"text\":\" you to a block like a \"},{\"text\":\"grappling hook\",\"color\":\"gray\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"They will also \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"pull\",\"color\":\"aqua\"},{\"text\":\" a hit \"},{\"text\":\"entity \",\"color\":\"gray\"},{\"text\":\"towards you.\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      
      return loreList;
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
         motion = motion.add(horizBoost);
         Vec3d velocity = new Vec3d(velFromLength(motion.x)*2.0/9.0,velFromHeight(motion.y)/20,velFromLength(motion.z)*2.0/9.0);
         player.setVelocity(velocity);
         player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
         ParticleEffectUtils.tetherArrowGrapple(player.getServerWorld(),player,blockHitResult.getPos());
         SoundUtils.playSound(arrow.getWorld(),player.getBlockPos(), SoundEvents.ITEM_TRIDENT_RIPTIDE_2, SoundCategory.PLAYERS,.8f,.6f);
         
         if(motion.y >= 12) ArcanaAchievements.progress(player,ArcanaAchievements.SPIDERMAN.id,1);
      }
      
   }
   
   private double velFromLength(double d){
      double a = .98; // Drag
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
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = MagicItemIngredient.EMPTY;
      MagicItemIngredient c = new MagicItemIngredient(Items.STRING,64,null);
      ItemStack potion6 = new ItemStack(Items.POTION);
      MagicItemIngredient g = new MagicItemIngredient(Items.POTION,1, PotionUtil.setPotion(potion6, Potions.STRONG_LEAPING).getNbt());
      MagicItemIngredient h = new MagicItemIngredient(Items.SPECTRAL_ARROW,64,null);
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,g,a},
            {c,h,m,h,c},
            {a,g,h,g,a},
            {a,a,c,a,a}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withFletchery());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"    Tether Arrows\\n\\nRarity: Empowered\\n\\nThrough precise math equations inscribed within the Matrix, these Arrows should create the perfect magical tether to pull me to the location I shot. It can also pull creatures to me. It sucks to miss though.\\n\"}");
      return list;
   }
   
   public class TetherArrowsItem extends MagicPolymerArrowItem {
      public TetherArrowsItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.MODELS.get(TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}
