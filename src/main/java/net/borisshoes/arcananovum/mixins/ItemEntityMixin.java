package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.BinaryBlades;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
   
   @Inject(method= "setItem",at=@At("HEAD"))
   private void arcananovum$destroyFake(ItemStack stack, CallbackInfo ci){
      ItemEntity itemEntity = (ItemEntity) (Object) this;
      if(BinaryBlades.isFakeBlade(stack)){
         itemEntity.discard();
      }
      if(ArcanaItem.hasProperty(stack, BinaryBlades.SPLIT_TAG)){
         ArcanaItem.putProperty(stack, BinaryBlades.SPLIT_TAG,false);
      }
   }
   
   @Inject(method= "tick",at=@At("TAIL"))
   private void arcananovum$worldDetection(CallbackInfo ci){
      ItemEntity itemEntity = (ItemEntity) (Object) this;
      ItemStack stack = itemEntity.getItem();
      Level world = itemEntity.level();
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
      if(!(itemEntity.level() instanceof ServerLevel serverWorld)) return;
      
      if(arcanaItem instanceof Soulstone){
         boolean hasAnnihilation = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SOUL_ANNIHILATION.id) > 0;
         if(hasAnnihilation){
            BlockState state = world.getBlockState(itemEntity.blockPosition());
            if(state.is(Blocks.SOUL_FIRE)){
               if(!Soulstone.getType(stack).equals("unattuned")){
                  itemEntity.setItem(Soulstone.setUnattuned(stack));
                  
                  SoundUtils.soulSounds(serverWorld,itemEntity.blockPosition(),30,20);
                  serverWorld.sendParticles(ParticleTypes.SOUL, itemEntity.getX(), itemEntity.getY()+0.125, itemEntity.getZ(), 100,0.25,0.25,0.25,0.07);
               }
            }
         }
      }else if(arcanaItem instanceof ArcaneTome){ // Crafting a Starlight Forge
         int craftTick = 0;
         if(ArcanaItem.hasProperty(stack,ArcaneTome.FORGE_TAG)){
            craftTick = ArcanaItem.getIntProperty(stack,ArcaneTome.FORGE_TAG);
         }
         List<ItemEntity> otherEntities = world.getEntities(EntityType.ITEM,itemEntity.getBoundingBox().inflate(1.25), e -> (!e.getUUID().equals(itemEntity.getUUID()) && !ArcanaItemUtils.isArcane(e.getItem())));
         
         boolean proceed = false;
         ItemEntity gappleEntity = null;
         for(ItemEntity other : otherEntities){
            ItemStack otherStack = other.getItem();
            if(otherStack.is(Items.ENCHANTED_GOLDEN_APPLE)){
               proceed = true;
               gappleEntity = other;
               break;
            }
         }
         
         long timeOfDay = serverWorld.getDayTime();
         int day = (int) (timeOfDay/24000L % Integer.MAX_VALUE);
         int curTime = (int) (timeOfDay % 24000L);
         int curPhase = day % 8;
         if(!(curPhase == 4 && curTime >= 14000 && curTime <= 22000)) proceed = false;
         
         BlockPos smithPos = BlockPos.containing(itemEntity.position().add(0,-0.25,0));
         BlockState state = world.getBlockState(smithPos);
         if(!state.is(Blocks.SMITHING_TABLE)) proceed = false;
         ArcanaItem forgeItem = ArcanaRegistry.STARLIGHT_FORGE;
         String playerId = forgeItem.getCrafter(stack);
         ServerPlayer player = serverWorld.getServer().getPlayerList().getPlayer(AlgoUtils.getUUID(playerId));
         
         if(player != null && !ArcanaNovum.data(player).hasResearched(forgeItem)){
            proceed = false;
         }
         
         if(proceed){
            craftTick++;
            if(craftTick == 100){
               serverWorld.setBlock(smithPos, ((ArcanaBlock)ArcanaRegistry.STARLIGHT_FORGE).getBlock().defaultBlockState(),3);
               BlockEntity blockEntity = serverWorld.getBlockEntity(smithPos);
               if(blockEntity instanceof StarlightForgeBlockEntity forge){
                  ItemStack newForgeStack = forgeItem.addCrafter(forgeItem.getNewItem(),playerId,0,serverWorld.getServer());
                  ArcanaPolymerBlockEntity.initializeArcanaBlock(newForgeStack,forge);
                  
                  if(player != null){
                     if(!ArcanaNovum.data(player).addCrafted(newForgeStack)){
                        ArcanaNovum.data(player).addXP(ArcanaRarity.getCraftXp(forgeItem.getRarity()));
                     }
                  }
                  
                  ItemStack gappleStack = gappleEntity.getItem();
                  if(gappleStack.getCount() == 1){
                     gappleEntity.discard();
                  }else{
                     gappleStack.shrink(1);
                     gappleEntity.setItem(gappleStack);
                  }
               }
               ArcanaItem.removeProperty(stack,ArcaneTome.FORGE_TAG);
            }else{
               ArcanaItem.putProperty(stack,ArcaneTome.FORGE_TAG,craftTick);
            }
            ArcanaEffectUtils.craftForge(serverWorld,smithPos,craftTick);
         }else{
            ArcanaItem.removeProperty(stack,ArcaneTome.FORGE_TAG);
         }
         itemEntity.setItem(stack);
      }
      
      
      if(!ArcanaItemUtils.isArcane(stack) && stack.is(ArcanaRegistry.MUNDANE_ARCANE_PAPER)){ // Crafting an Arcane Tome
         boolean proceed = false;
         ItemEntity eyeEntity = null;
         
         BlockPos enchantPos = BlockPos.containing(itemEntity.position().add(0,-0.25,0));
         BlockState state = world.getBlockState(enchantPos);
         if(state.is(Blocks.ENCHANTING_TABLE)){
            List<ItemEntity> otherEntities = world.getEntities(EntityType.ITEM,itemEntity.getBoundingBox().inflate(1.25), e -> (!e.getUUID().equals(itemEntity.getUUID()) && !ArcanaItemUtils.isArcane(e.getItem())));
            for(ItemEntity other : otherEntities){
               ItemStack otherStack = other.getItem();
               if(otherStack.is(Items.ENDER_EYE)){
                  proceed = true;
                  eyeEntity = other;
                  break;
               }
            }
         }
         
         
         if(proceed && stack.getCount() >= 4){
            int craftTick = 0;
            
            if(ArcanaItem.hasProperty(stack,ArcaneTome.TOME_TAG)){
               craftTick = ArcanaItem.getIntProperty(stack,ArcaneTome.TOME_TAG);
            }
            craftTick++;
            if(craftTick == 100){
               ItemStack tomeStack = ArcanaRegistry.ARCANE_TOME.getNewItem();
               Player tomeCrafter = serverWorld.getNearestPlayer(itemEntity.getX(),itemEntity.getY(),itemEntity.getZ(),50,false);
               if(itemEntity.getOwner() instanceof Player stackOwner){
                  tomeCrafter = stackOwner;
               }
               if(tomeCrafter != null){
                  ArcanaPlayerData profile = ArcanaNovum.data(tomeCrafter);
                  tomeStack = ArcanaRegistry.ARCANE_TOME.addCrafter(tomeStack,tomeCrafter.getStringUUID(),0,itemEntity.level().getServer());
                  if(!profile.addCrafted(tomeStack)){
                     profile.addXP(ArcanaRarity.getFirstCraftXp(ArcanaRegistry.ARCANE_TOME.getRarity()));
                  }
                  profile.addResearchedItem(ArcanaRegistry.ARCANE_TOME.getId());
               }
               
               ItemStack eyeStack = eyeEntity.getItem();
               if(eyeStack.getCount() == 1){
                  eyeEntity.discard();
               }else{
                  eyeStack.shrink(1);
                  eyeEntity.setItem(eyeStack);
               }
               
               if(stack.getCount() <= 4){
                  itemEntity.discard();
               }else{
                  stack.shrink(4);
                  itemEntity.setItem(stack);
               }
               
               ItemEntity tomeEntity = new ItemEntity(world, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), tomeStack);
               tomeEntity.setPickUpDelay(40);
               if(tomeCrafter != null){
                  tomeEntity.setTarget(tomeCrafter.getUUID());
               }
               
               float f = world.random.nextFloat() * 0.1F;
               float g = world.random.nextFloat() * 6.2831855F;
               tomeEntity.setDeltaMovement((double)(-Mth.sin(g) * f), 0.20000000298023224, (double)(Mth.cos(g) * f));
               world.addFreshEntity(tomeEntity);
               
               ArcanaItem.removeProperty(stack,ArcaneTome.TOME_TAG);
            }else{
               ArcanaItem.putProperty(stack,ArcaneTome.TOME_TAG,craftTick);
            }
            ArcanaEffectUtils.craftTome(serverWorld,enchantPos,craftTick);
         }else{
            ArcanaItem.removeProperty(stack,ArcaneTome.TOME_TAG);
         }
         itemEntity.setItem(stack);
      }
      
      if(!ArcanaItemUtils.isArcane(stack) && stack.is(Items.OBSIDIAN) && itemEntity.isInWater()){
         List<ItemEntity> otherEntities = world.getEntities(EntityType.ITEM,itemEntity.getBoundingBox().inflate(1.25), e -> (!e.getUUID().equals(itemEntity.getUUID()) && e.isInWater() && !ArcanaItemUtils.isArcane(e.getItem())));
   
         boolean create = false;
         for(ItemEntity other : otherEntities){
            ItemStack otherStack = other.getItem();
            int count = otherStack.getCount();
            if(otherStack.is(Items.GLOWSTONE_DUST) && count >= 4){
               if(count == 4){
                  other.discard();
               }else{
                  otherStack.shrink(4);
                  other.setItem(otherStack);
               }
               
               create = true;
               break;
            }else if(otherStack.is(Items.REDSTONE) && count >= 16){
               if(count == 16){
                  other.discard();
               }else{
                  otherStack.shrink(16);
                  other.setItem(otherStack);
               }
               
               create = true;
               break;
            }
         }
         
         if(create){
            ItemStack cObby = new ItemStack(Items.CRYING_OBSIDIAN);
            cObby.setCount(1);
            ItemEntity cryingObby = new ItemEntity(world, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), cObby);
            cryingObby.setPickUpDelay(40);
   
            float f = world.random.nextFloat() * 0.1F;
            float g = world.random.nextFloat() * 6.2831855F;
            cryingObby.setDeltaMovement((double)(-Mth.sin(g) * f), 0.20000000298023224, (double)(Mth.cos(g) * f));
            world.addFreshEntity(cryingObby);
            
            if(stack.getCount() == 1){
               itemEntity.discard();
            }else{
               stack.shrink(1);
               itemEntity.setItem(stack);
            }
         }
         
      }
   }
   
   @Inject(method= "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z",shift = At.Shift.BEFORE))
   private void arcananovum$removeCraftData(Player player, CallbackInfo ci, @Local LocalRef<ItemStack> itemStackRef){
      ItemStack stack = itemStackRef.get();
      if(stack.is(ArcanaRegistry.MUNDANE_ARCANE_PAPER)){
         ArcanaItem.removeProperty(stack,ArcaneTome.TOME_TAG);
         itemStackRef.set(stack);
      }
      if(stack.is(ArcanaRegistry.ARCANE_TOME.getItem())){
         ArcanaItem.removeProperty(stack,ArcaneTome.FORGE_TAG);
         itemStackRef.set(stack);
      }
   }
}
