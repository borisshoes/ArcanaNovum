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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class SmokeArrows extends MagicItem implements RunicArrow {
   
   public SmokeArrows(){
      id = "smoke_arrows";
      name = "Smoke Arrows";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.ARROWS};
      
      ItemStack item = new ItemStack(Items.TIPPED_ARROW);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Arrows - Smoke\",\"italic\":false,\"color\":\"dark_gray\",\"bold\":true}]");
      addRunicArrowLore(loreList);
      loreList.add(NbtString.of("[{\"text\":\"Smoke Arrows:\",\"italic\":false,\"color\":\"dark_gray\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\" emit \"},{\"text\":\"smoke\",\"color\":\"dark_gray\"},{\"text\":\" particles near where they land.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Smoke\",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\" gives \",\"color\":\"gray\"},{\"text\":\"blindness\"},{\"text\":\" and \",\"color\":\"gray\"},{\"text\":\"weakness\"},{\"text\":\" to those inside it.\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Empowered \",\"italic\":false,\"color\":\"green\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("CustomPotionColor",6908265);
      tag.putInt("HideFlags",127);
      item.setCount(64);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   @Override
   public void entityHit(PersistentProjectileEntity arrow, EntityHitResult entityHitResult){
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         float range = (float) MathHelper.clamp(arrow.getVelocity().length()*.8,.3,2.5);
         ParticleEffectUtils.smokeArrowEmit(serverWorld,null,entityHitResult.getEntity(),range,0);
         smokeEffects(serverWorld,null,entityHitResult.getEntity(),range,0);
      }
   }
   
   @Override
   public void blockHit(PersistentProjectileEntity arrow, BlockHitResult blockHitResult){
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         float range = (float) MathHelper.clamp(arrow.getVelocity().length()*.8,.3,2.5);
         ParticleEffectUtils.smokeArrowEmit(serverWorld,blockHitResult.getPos(),null,range,0);
         smokeEffects(serverWorld,blockHitResult.getPos(),null,range,0);
      }
   }
   
   private void smokeEffects(ServerWorld world, @Nullable Vec3d start, @Nullable Entity entity, double range, int calls){
      if(start == null && entity == null) return;
      Vec3d pos = entity == null ? start : entity.getPos();
      
      Box rangeBox = new Box(pos.x+8,pos.y+8,pos.z+8,pos.x-8,pos.y-8,pos.z-8);
      List<Entity> entities = world.getOtherEntities(null,rangeBox,e -> !e.isSpectator() && e.squaredDistanceTo(pos) < 4*range*range && e instanceof LivingEntity);
      for(Entity entity1 : entities){
         if(entity1 instanceof LivingEntity e){
            int amp = e instanceof MobEntity ? 5 : 0;
            StatusEffectInstance blind = new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0, false, false, true);
            StatusEffectInstance weakness = new StatusEffectInstance(StatusEffects.WEAKNESS, 60, amp, false, false, true);
            e.addStatusEffect(blind);
            e.addStatusEffect(weakness);
         }
      }
   
      SoundUtils.playSound(world,new BlockPos(pos), SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.PLAYERS,.5f,1);
   
      if(calls < 20){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(5, new TimerTask() {
            @Override
            public void run(){
               smokeEffects(world, pos, entity,range,calls + 1);
            }
         }));
      }
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = MagicItemIngredient.EMPTY;
      MagicItemIngredient c = new MagicItemIngredient(Items.CAMPFIRE,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.GLOW_INK_SAC,64,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.SPECTRAL_ARROW,64,null);
      MagicItemIngredient i = new MagicItemIngredient(Items.INK_SAC,64,null);
      ItemStack potion10 = new ItemStack(Items.LINGERING_POTION);
      MagicItemIngredient k = new MagicItemIngredient(Items.LINGERING_POTION,1, PotionUtil.setPotion(potion10, Potions.LONG_WEAKNESS).getNbt());
      GenericMagicIngredient m = new GenericMagicIngredient(MagicItems.RUNIC_MATRIX,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,i,a},
            {k,h,m,h,k},
            {a,i,h,g,a},
            {a,a,c,a,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"     Smoke Arrows\\n\\nRarity: Empowered\\n\\nThis Runic Matrix has been configured to summon copious amounts of campfire smoke. Those inside will have trouble seeing, and even breathing, making it harder to land a solid blow.\"}");
      return list;
   }
}
