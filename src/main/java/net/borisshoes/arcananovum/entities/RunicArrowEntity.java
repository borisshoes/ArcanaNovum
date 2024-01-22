package net.borisshoes.arcananovum.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.items.arrows.ArcaneFlakArrows;
import net.borisshoes.arcananovum.items.arrows.RunicArrow;
import net.borisshoes.arcananovum.items.arrows.TetherArrows;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class RunicArrowEntity extends ArrowEntity implements PolymerEntity {
   
   private RunicArrow arrowType;
   private TreeMap<ArcanaAugment,Integer> augments;
   private NbtCompound data;
   
   public RunicArrowEntity(EntityType<? extends RunicArrowEntity> entityType, World world) {
      super(entityType, world);
   }
   
   public RunicArrowEntity(World world, LivingEntity owner, ItemStack stack) {
      this(ArcanaRegistry.RUNIC_ARROW_ENTITY, world);
      setOwner(owner);
      this.setPosition(owner.getX(), owner.getEyeY() - (double)0.1f, owner.getZ());
      this.pickupType = PickupPermission.CREATIVE_ONLY;
      
      augments = ArcanaAugments.getAugmentsOnItem(stack);
      arrowType = MagicItemUtils.identifyRunicArrow(stack);
      data = new NbtCompound();
   }
   
   @Override
   public void initFromStack(ItemStack stack){
      super.initFromStack(stack);
      
      if(arrowType instanceof ArcaneFlakArrows){
         data.putInt("armTime", 5);
      }
   }
   
   @Override
   public EntityType<?> getPolymerEntityType(ServerPlayerEntity player){
      return EntityType.ARROW;
   }
   
   @Override
   public void tick(){
      super.tick();
      
      if(arrowType instanceof TetherArrows){
         if(ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.QUICK_RELEASE.id) > 0){
            if(getOwner() != null && getOwner().isSneaking()){
               data.putBoolean("severed",true);
               if(getOwner() instanceof ServerPlayerEntity player){
                  player.sendMessage(Text.literal("Arcane Tethers Severed").formatted(Formatting.GRAY, Formatting.ITALIC), true);
               }
            }
            
         }
      }else if(arrowType instanceof ArcaneFlakArrows){
         int armTime = data.getInt("armTime");
         if(armTime > 0){
            armTime--;
            data.putInt("armTime",armTime);
         }
         
         if(armTime == 0){
            double senseRange = 4;
            List<Entity> triggerTargets = getWorld().getOtherEntities(this,this.getBoundingBox().expand(senseRange*2),
                  e -> !e.isSpectator() && e.distanceTo(this) <= senseRange && e instanceof LivingEntity && !e.isOnGround());
            if(!triggerTargets.isEmpty()){
               double radius = 4 + 1.25*ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.AIRBURST.id);
               ArcaneFlakArrows.detonate(this,radius);
            }
         }
      }
   }
   
   
   @Override
   protected void onEntityHit(EntityHitResult entityHitResult){
      if(arrowType != null){
         arrowType.entityHit(this,entityHitResult);
         
         if(this.getOwner() instanceof ServerPlayerEntity player){
            if(player.getPos().distanceTo(this.getPos()) >= 100) ArcanaAchievements.grant(player, ArcanaAchievements.AIMBOT.id);
            incArrowForEveryFoe(player);
         }
      }
      super.onEntityHit(entityHitResult);
   }
   
   public void incArrowForEveryFoe(ServerPlayerEntity player){
      // Do this bit manually so extra data can be saved
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      if(ArcanaAchievements.ARROW_FOR_EVERY_FOE instanceof TimedAchievement baseAch){
         String itemId = baseAch.getMagicItem().getId();
         TimedAchievement achievement = (TimedAchievement) profile.getAchievement(itemId, baseAch.id);
         if(achievement == null){
            TimedAchievement newAch = baseAch.makeNew();
            NbtCompound comp = new NbtCompound();
            comp.putBoolean(arrowType.getId(),true);
            newAch.setData(comp);
            profile.setAchievement(itemId, newAch);
            ArcanaAchievements.progress(player, ArcanaAchievements.ARROW_FOR_EVERY_FOE.id,1);
         }else if(!achievement.isAcquired()){
            NbtCompound comp = achievement.getData();
            if(!comp.contains(arrowType.getId())){
               comp.putBoolean(arrowType.getId(), true);
               achievement.setData(comp);
               profile.setAchievement(itemId, achievement);
               ArcanaAchievements.progress(player, ArcanaAchievements.ARROW_FOR_EVERY_FOE.id, 1);
            }
         }
      }
   }
   
   public int getAugment(String id){
      for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
         if(entry.getKey().id.equals(id)) return entry.getValue();
      }
      return 0;
   }
   
   @Override
   protected void onBlockHit(BlockHitResult blockHitResult){
      if(arrowType != null){
         arrowType.blockHit(this,blockHitResult);
         this.discard();
      }
      super.onBlockHit(blockHitResult);
   }
   
   public TreeMap<ArcanaAugment, Integer> getAugments(){
      return augments;
   }
   
   public NbtCompound getData(){
      return data;
   }
   
   @Override
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      if(augments != null){
         NbtCompound augsCompound = new NbtCompound();
         for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
            augsCompound.putInt(entry.getKey().id,entry.getValue());
         }
         nbt.put("runicAugments",augsCompound);
      }
      if(arrowType != null){
         nbt.putString("runicArrowType",arrowType.getId());
      }
      if(data != null){
         nbt.put("runicArrowData",data);
      }
   }
   
   @Override
   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      augments = new TreeMap<>();
      if(nbt.contains("runicAugments")){
         NbtCompound augCompound = nbt.getCompound("runicAugments");
         for(String key : augCompound.getKeys()){
            ArcanaAugment aug = ArcanaAugments.registry.get(key);
            if(aug != null) augments.put(aug,augCompound.getInt(key));
         }
      }
      if(nbt.contains("runicArrowType")){
         MagicItem magicItem = ArcanaRegistry.registry.get(nbt.getString("runicArrowType"));
         if(magicItem instanceof RunicArrow ra) arrowType = ra;
      }
      if(nbt.contains("runicArrowData")){
         data = nbt.getCompound("runicArrowData");
      }
   }
}
