package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.items.core.RunicArrow;
import net.borisshoes.arcananovum.recipes.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.FishingRodItem;
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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class TetherArrows extends MagicItem implements RunicArrow {
   
   public TetherArrows(){
      id = "tether_arrows";
      name = "Tether Arrows";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.ARROWS};
      
      ItemStack item = new ItemStack(Items.TIPPED_ARROW);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name", "[{\"text\":\"Runic Arrows - Tether\",\"italic\":false,\"bold\":true,\"color\":\"gray\"}]");
      addRunicArrowLore(loreList);
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"arrows\",\"color\":\"light_purple\"},{\"text\":\" can be refilled inside a \"},{\"text\":\"Runic Quiver\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"Runic Arrows \",\"color\":\"light_purple\"},{\"text\":\"pull\",\"color\":\"aqua\"},{\"text\":\" you to a block like a \"},{\"text\":\"grappling hook\",\"color\":\"gray\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"They will also \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"pull\",\"color\":\"aqua\"},{\"text\":\" a hit \"},{\"text\":\"entity \",\"color\":\"gray\"},{\"text\":\"towards you.\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Empowered \",\"italic\":false,\"color\":\"green\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore", loreList);
      tag.put("display", display);
      tag.put("Enchantments", enchants);
      tag.putInt("CustomPotionColor", 10724259);
      tag.putInt("HideFlags", 127);
      item.setCount(64);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   @Override
   public void entityHit(PersistentProjectileEntity arrow, EntityHitResult entityHitResult){
      if(arrow.getOwner() instanceof ServerPlayerEntity player && entityHitResult.getEntity() instanceof LivingEntity entity){
         Vec3d hitPos = entityHitResult.getPos();
   
         Arcananovum.addTickTimerCallback(player.getWorld(), new GenericTimer(1, new TimerTask() {
            @Override
            public void run(){
               Vec3d motion = player.getPos().subtract(hitPos);
               Vec3d horizBoost = motion.multiply(1,0,1).normalize().multiply(1.5);
               motion = motion.add(horizBoost);
               double verticalMotion = motion.y < -3 ? (player.getY() - entity.getY())*.3 : velFromHeight(motion.y)/20;
               Vec3d velocity = new Vec3d(velFromLength(motion.x)*2.0/9.0,verticalMotion,velFromLength(motion.z)*2.0/9.0);
               entity.setVelocity(velocity);
   
               ParticleEffectUtils.tetherArrowEntity(player.getWorld(),entity,player);
               SoundUtils.playSound(arrow.getWorld(),player.getBlockPos(), SoundEvents.ITEM_TRIDENT_RIPTIDE_1, SoundCategory.PLAYERS,.8f,.6f);
            }
         }));
      }
   }
   
   @Override
   public void blockHit(PersistentProjectileEntity arrow, BlockHitResult blockHitResult){
      if(arrow.getOwner() instanceof ServerPlayerEntity player){
         Vec3d hitPos = blockHitResult.getPos();
         Vec3d motion = hitPos.subtract(player.getPos());
         Vec3d horizBoost = motion.multiply(1,0,1).normalize().multiply(1.5);
         motion = motion.add(horizBoost);
         Vec3d velocity = new Vec3d(velFromLength(motion.x)*2.0/9.0,velFromHeight(motion.y)/20,velFromLength(motion.z)*2.0/9.0);
         player.setVelocity(velocity);
         player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
         ParticleEffectUtils.tetherArrowGrapple(player.getWorld(),player,blockHitResult.getPos());
         SoundUtils.playSound(arrow.getWorld(),player.getBlockPos(), SoundEvents.ITEM_TRIDENT_RIPTIDE_2, SoundCategory.PLAYERS,.8f,.6f);
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
      GenericMagicIngredient m = new GenericMagicIngredient(MagicItems.RUNIC_MATRIX,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,g,a},
            {c,h,m,h,c},
            {a,g,h,g,a},
            {a,a,c,a,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"    Tether Arrows\\n\\nRarity: Empowered\\n\\nThrough precise math equations inscribed within the Matrix, these Arrows should create the perfect magical tether to pull me to the location I shot. It can also pull creatures to me. It sucks to miss though.\\n\"}");
      return list;
   }
}
