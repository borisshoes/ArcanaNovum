package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.MagicEntity;
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
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class ExpulsionArrows extends MagicItem implements RunicArrow {
   
   public ExpulsionArrows(){
      id = "expulsion_arrows";
      name = "Expulsion Arrows";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ARROWS};
      
      ItemStack item = new ItemStack(Items.TIPPED_ARROW);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Arrows - Expulsion\",\"italic\":false,\"color\":\"blue\",\"bold\":true}]");
      addRunicArrowLore(loreList);
      loreList.add(NbtString.of("[{\"text\":\"Expulsion Arrows:\",\"italic\":false,\"color\":\"blue\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\" \"},{\"text\":\"repulse \",\"color\":\"blue\"},{\"text\":\"entities\",\"color\":\"aqua\"},{\"text\":\" near the area of \"},{\"text\":\"impact\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"dark_aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"A hit \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"entity \",\"color\":\"aqua\"},{\"text\":\"is \"},{\"text\":\"not affected\",\"color\":\"blue\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Exotic \",\"italic\":false,\"color\":\"aqua\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("CustomPotionColor",889599);
      tag.putInt("HideFlags",127);
      item.setCount(64);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   @Override
   public void entityHit(PersistentProjectileEntity arrow, EntityHitResult entityHitResult, MagicEntity magicEntity){
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         int duration = (int) MathHelper.clamp(arrow.getVelocity().length()*7,2,20); // Measured in quarter seconds
         double range = 4 + 1.5*Math.max(0, ArcanaAugments.getAugmentFromCompound(magicEntity.getData(),"repulsion"));;
         expulsionPulse(arrow, serverWorld,null,entityHitResult.getEntity(),duration,range,0);
      }
   }
   
   @Override
   public void blockHit(PersistentProjectileEntity arrow, BlockHitResult blockHitResult, MagicEntity magicEntity){
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         int duration = (int) MathHelper.clamp(arrow.getVelocity().length()*7,2,20); // Measured in quarter seconds
         double range = 4 + 1.5*Math.max(0, ArcanaAugments.getAugmentFromCompound(magicEntity.getData(),"repulsion"));;
         expulsionPulse(arrow, serverWorld,blockHitResult.getPos(),null,duration,range,0);
      }
   }
   
   private void expulsionPulse(PersistentProjectileEntity arrow, ServerWorld world, @Nullable Vec3d start, @Nullable Entity entity, int duration, double range, int calls){
      if(start == null && entity == null) return;
      Vec3d pos = entity == null ? start : entity.getPos();
      
      Box rangeBox = new Box(pos.x+8,pos.y+8,pos.z+8,pos.x-8,pos.y-8,pos.z-8);
      List<Entity> entities = world.getOtherEntities(entity,rangeBox, e -> !e.isSpectator() && e.squaredDistanceTo(pos) < 1.5*range*range && !(e instanceof PersistentProjectileEntity) && !(e instanceof EnderDragonEntity));
      for(Entity entity1 : entities){
         Vec3d diff = entity1.getPos().subtract(pos);
         double multiplier = MathHelper.clamp(range*.75-diff.length()*.5,.1,5);
         Vec3d motion = diff.add(0,0,0).normalize().multiply(multiplier);
         entity1.setVelocity(motion.x,motion.y,motion.z);
         if(entity1 instanceof ServerPlayerEntity player){
            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
            
            if(arrow.getOwner() != null && arrow.getOwner().getUuid().equals(player.getUuid()) && motion.y > 2) ArcanaAchievements.grant(player,"jump_pad");
         }
      }
      
      if(calls % 5 == 0){
         ParticleEffectUtils.expulsionArrowEmit(world,pos,range,0);
         SoundUtils.playSound(world,new BlockPos(pos), SoundEvents.ENTITY_ALLAY_ITEM_TAKEN, SoundCategory.PLAYERS,.5f,.5f);
      }
      if(calls % 10 == 1){
         SoundUtils.playSound(world,new BlockPos(pos), SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, SoundCategory.PLAYERS,.5f,.9f);
      }
      
      if(calls < duration){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(5, new TimerTask() {
            @Override
            public void run(){
               expulsionPulse(arrow, world, pos, entity,duration,range,calls + 1);
            }
         }));
      }
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = MagicItemIngredient.EMPTY;
      MagicItemIngredient c = new MagicItemIngredient(Items.AMETHYST_SHARD,32,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.SLIME_BLOCK,64,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.SPECTRAL_ARROW,64,null);
      MagicItemIngredient k = new MagicItemIngredient(Items.ENDER_PEARL,16,null);
      GenericMagicIngredient m = new GenericMagicIngredient(MagicItems.RUNIC_MATRIX,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,a,c,a,a},
            {a,g,h,g,a},
            {k,h,m,h,k},
            {a,g,h,g,a},
            {a,a,c,a,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"   Expulsion Arrows\\n\\nRarity: Exotic\\n\\nThis Runic Matrix is configured to repulse anything nearby like bouncing on a slime block. Great for jump pads, zoning off monsters, or sending foes off a steep cliff.\"}");
      return list;
   }
}
