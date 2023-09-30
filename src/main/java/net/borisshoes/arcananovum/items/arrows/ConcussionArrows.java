package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.ArcanaRegistry;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
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

public class ConcussionArrows extends RunicArrow {
   
   public ConcussionArrows(){
      id = "concussion_arrows";
      name = "Concussion Arrows";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.ARROWS};
      vanillaItem = Items.TIPPED_ARROW;
      item = new ConcussionArrowsItem(new FabricItemSettings().maxCount(64).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
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
      tag.putInt("HideFlags", 255);
      stack.setCount(64);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public void entityHit(RunicArrowEntity arrow, EntityHitResult entityHitResult){
      int lvl = arrow.getAugment(ArcanaAugments.SHELLSHOCK.id);
      concuss(arrow, arrow.getEntityWorld(),entityHitResult.getPos(), lvl);
   }
   
   @Override
   public void blockHit(RunicArrowEntity arrow, BlockHitResult blockHitResult){
      int lvl = arrow.getAugment(ArcanaAugments.SHELLSHOCK.id);
      concuss(arrow, arrow.getEntityWorld(),blockHitResult.getPos(), lvl);
   }
   
   private void concuss(PersistentProjectileEntity arrow, World world, Vec3d pos, int levelBoost){
      Box rangeBox = new Box(pos.x+10,pos.y+10,pos.z+10,pos.x-10,pos.y-10,pos.z-10);
      float range = (float) MathHelper.clamp(arrow.getVelocity().length()*2.5,1,6);
      List<Entity> entities = world.getOtherEntities(null,rangeBox,e -> !e.isSpectator() && e.squaredDistanceTo(pos) < range*range && e instanceof LivingEntity);
      float percent = (1+levelBoost*.75f)*range/6;
      int mobsHit = 0;
      for(Entity entity : entities){
         if(entity instanceof LivingEntity e && !(entity instanceof EnderDragonEntity || entity instanceof WitherEntity)){
            if(e instanceof MobEntity) mobsHit++;
            
            StatusEffectInstance blind = new StatusEffectInstance(StatusEffects.BLINDNESS, (int)(25*percent), 0, false, false, true);
            StatusEffectInstance nausea = new StatusEffectInstance(StatusEffects.NAUSEA, (int)(120*percent), 0, false, false, true);
            StatusEffectInstance slow = new StatusEffectInstance(StatusEffects.SLOWNESS, (int)(40*percent), 4, false, false, true);
            StatusEffectInstance slow2 = new StatusEffectInstance(StatusEffects.SLOWNESS, (int)(120*percent), 2, false, false, true);
            StatusEffectInstance fatigue = new StatusEffectInstance(StatusEffects.MINING_FATIGUE, (int)(80*percent), 2+levelBoost, false, false, true);
            StatusEffectInstance weakness = new StatusEffectInstance(StatusEffects.WEAKNESS, (int)(120*percent), 1+levelBoost, false, false, true);
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
      if(arrow.getOwner() instanceof ServerPlayerEntity player && mobsHit >= 10) ArcanaAchievements.grant(player,ArcanaAchievements.SHOCK_AWE.id);
      if(world instanceof ServerWorld serverWorld){
         SoundUtils.playSound(world, BlockPos.ofFloored(pos), SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.PLAYERS, 1, .8f);
         ParticleEffectUtils.concussionArrowShot(serverWorld, pos, range, 0);
      }
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = MagicItemIngredient.EMPTY;
      ItemStack potion2 = new ItemStack(Items.LINGERING_POTION);
      MagicItemIngredient c = new MagicItemIngredient(Items.LINGERING_POTION,1, PotionUtil.setPotion(potion2, Potions.LONG_SLOWNESS).getNbt());
      MagicItemIngredient g = new MagicItemIngredient(Items.GLOW_INK_SAC,64,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.SPECTRAL_ARROW,64,null);
      MagicItemIngredient i = new MagicItemIngredient(Items.INK_SAC,64,null);
      ItemStack potion10 = new ItemStack(Items.LINGERING_POTION);
      MagicItemIngredient k = new MagicItemIngredient(Items.LINGERING_POTION,1, PotionUtil.setPotion(potion10, Potions.LONG_WEAKNESS).getNbt());
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,i,a},
            {k,h,m,h,k},
            {a,i,h,g,a},
            {a,a,c,a,a}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withFletchery());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"  Concussion Arrows\\n\\nRarity: Empowered\\n\\nThis Runic Matrix has been configured to unleash a plethora of unpleasant effects at the area of impact.\\nAnyone caught in its range will have a hard time doing anything for a couple seconds after being hit.\\n\"}");
      return list;
   }
   
   public class ConcussionArrowsItem extends MagicPolymerArrowItem {
      public ConcussionArrowsItem(Settings settings){
         super(getThis(),settings);
      }
      
      
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}
