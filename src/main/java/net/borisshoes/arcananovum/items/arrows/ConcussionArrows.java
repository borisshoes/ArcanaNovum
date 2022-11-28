package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.RunicArrow;
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
import net.minecraft.server.network.ServerPlayerEntity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import static net.borisshoes.arcananovum.Arcananovum.log;

public class ConcussionArrows extends MagicItem implements RunicArrow {
   
   public ConcussionArrows(){
      id = "concussion_arrows";
      name = "Concussion Arrows";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.ARROWS};
      
      ItemStack item = new ItemStack(Items.TIPPED_ARROW);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Arrows - Concussion\",\"italic\":false,\"color\":\"gold\",\"bold\":true}]");
      addRunicArrowLore(loreList);
      loreList.add(NbtString.of("[{\"text\":\"Concussion Arrows:\",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\" \"},{\"text\":\"concuss \",\"color\":\"gold\"},{\"text\":\"entities \",\"color\":\"yellow\"},{\"text\":\"near where the arrow \"},{\"text\":\"impacts\",\"color\":\"gold\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Empowered \",\"italic\":false,\"color\":\"green\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("CustomPotionColor",14391821);
      tag.putInt("HideFlags",127);
      item.setCount(64);
      
      setBookLore(makeLore());
      //setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   @Override
   public void entityHit(PersistentProjectileEntity arrow, EntityHitResult entityHitResult){
      concuss(arrow, arrow.getEntityWorld(),entityHitResult.getPos());
   }
   
   @Override
   public void blockHit(PersistentProjectileEntity arrow, BlockHitResult blockHitResult){
      concuss(arrow, arrow.getEntityWorld(),blockHitResult.getPos());
   }
   
   private void concuss(PersistentProjectileEntity arrow, World world, Vec3d pos){
      Box rangeBox = new Box(pos.x+10,pos.y+10,pos.z+10,pos.x-10,pos.y-10,pos.z-10);
      float range = (float) MathHelper.clamp(arrow.getVelocity().length()*2.5,1,6);
      List<Entity> entities = world.getOtherEntities(null,rangeBox,e -> !e.isSpectator() && e.squaredDistanceTo(pos) < range*range && e instanceof LivingEntity);
      float percent = range/6;
      for(Entity entity : entities){
         if(entity instanceof LivingEntity e){
            
            StatusEffectInstance blind = new StatusEffectInstance(StatusEffects.BLINDNESS, (int)(25*percent), 0, false, false, true);
            StatusEffectInstance nausea = new StatusEffectInstance(StatusEffects.NAUSEA, (int)(120*percent), 0, false, false, true);
            StatusEffectInstance slow = new StatusEffectInstance(StatusEffects.SLOWNESS, (int)(40*percent), 4, false, false, true);
            StatusEffectInstance slow2 = new StatusEffectInstance(StatusEffects.SLOWNESS, (int)(120*percent), 2, false, false, true);
            StatusEffectInstance fatigue = new StatusEffectInstance(StatusEffects.MINING_FATIGUE, (int)(80*percent), 2, false, false, true);
            StatusEffectInstance weakness = new StatusEffectInstance(StatusEffects.WEAKNESS, (int)(120*percent), 1, false, false, true);
            e.addStatusEffect(blind);
            e.addStatusEffect(nausea);
            e.addStatusEffect(slow);
            e.addStatusEffect(slow2);
            e.addStatusEffect(fatigue);
            e.addStatusEffect(weakness);
            
            if(world instanceof ServerWorld serverWorld){
               if(e instanceof MobEntity mob){
                  mob.setAiDisabled(true);
                  Arcananovum.addTickTimerCallback(serverWorld, new GenericTimer(100, new TimerTask() {
                     @Override
                     public void run(){
                        if(mob.isAlive()){
                           mob.setAiDisabled(false);
                        }
                     }
                  }));
               }
            }
         }
      }
      if(world instanceof ServerWorld serverWorld){
         SoundUtils.playSound(world, new BlockPos(pos), SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.PLAYERS, 1, .8f);
         ParticleEffectUtils.concussionArrowShot(serverWorld, pos, range, 0);
      }
   }
   
   //TODO: Make Recipe
   private MagicItemRecipe makeRecipe(){
      return null;
   }
   
   //TODO: Make Lore
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"TODO\"}");
      return list;
   }
}
