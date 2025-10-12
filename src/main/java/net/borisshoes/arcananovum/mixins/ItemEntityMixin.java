package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.BinaryBlades;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
   
   @Inject(method="setStack",at=@At("HEAD"))
   private void arcananovum$destroyFake(ItemStack stack, CallbackInfo ci){
      ItemEntity itemEntity = (ItemEntity) (Object) this;
      if(BinaryBlades.isFakeBlade(stack)){
         itemEntity.discard();
      }
      if(ArcanaItem.hasProperty(stack, BinaryBlades.SPLIT_TAG)){
         ArcanaItem.putProperty(stack, BinaryBlades.SPLIT_TAG,false);
      }
   }
   
   @Inject(method="tick",at=@At("TAIL"))
   private void arcananovum$worldDetection(CallbackInfo ci){
      ItemEntity itemEntity = (ItemEntity) (Object) this;
      ItemStack stack = itemEntity.getStack();
      World world = itemEntity.getWorld();
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
      if(!(itemEntity.getWorld() instanceof ServerWorld serverWorld)) return;
      
      if(arcanaItem instanceof Soulstone){
         boolean hasAnnihilation = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SOUL_ANNIHILATION.id) > 0;
         if(hasAnnihilation){
            BlockState state = world.getBlockState(itemEntity.getBlockPos());
            if(state.isOf(Blocks.SOUL_FIRE)){
               if(!Soulstone.getType(stack).equals("unattuned")){
                  itemEntity.setStack(Soulstone.setUnattuned(stack));
                  
                  SoundUtils.soulSounds(serverWorld,itemEntity.getBlockPos(),30,20);
                  serverWorld.spawnParticles(ParticleTypes.SOUL, itemEntity.getX(), itemEntity.getY()+0.125, itemEntity.getZ(), 100,0.25,0.25,0.25,0.07);
               }
            }
         }
      }else if(arcanaItem instanceof ArcaneTome){ // Crafting a Starlight Forge
         int craftTick = 0;
         if(ArcanaItem.hasProperty(stack,ArcaneTome.FORGE_TAG)){
            craftTick = ArcanaItem.getIntProperty(stack,ArcaneTome.FORGE_TAG);
         }
         List<ItemEntity> otherEntities = world.getEntitiesByType(EntityType.ITEM,itemEntity.getBoundingBox().expand(1.25),e -> (!e.getUuid().equals(itemEntity.getUuid()) && !ArcanaItemUtils.isArcane(e.getStack())));
         
         boolean proceed = false;
         ItemEntity gappleEntity = null;
         for(ItemEntity other : otherEntities){
            ItemStack otherStack = other.getStack();
            if(otherStack.isOf(Items.ENCHANTED_GOLDEN_APPLE)){
               proceed = true;
               gappleEntity = other;
               break;
            }
         }
         
         long timeOfDay = serverWorld.getTimeOfDay();
         int day = (int) (timeOfDay/24000L % Integer.MAX_VALUE);
         int curTime = (int) (timeOfDay % 24000L);
         int curPhase = day % 8;
         if(!(curPhase == 4 && curTime >= 14000 && curTime <= 22000)) proceed = false;
         
         BlockPos smithPos = BlockPos.ofFloored(itemEntity.getPos().add(0,-0.25,0));
         BlockState state = world.getBlockState(smithPos);
         if(!state.isOf(Blocks.SMITHING_TABLE)) proceed = false;
         ArcanaItem forgeItem = ArcanaRegistry.STARLIGHT_FORGE;
         String playerId = forgeItem.getCrafter(stack);
         ServerPlayerEntity player = serverWorld.getServer().getPlayerManager().getPlayer(AlgoUtils.getUUID(playerId));
         
         if(player != null && !ArcanaNovum.data(player).hasResearched(forgeItem)){
            proceed = false;
         }
         
         if(proceed){
            craftTick++;
            if(craftTick == 100){
               serverWorld.setBlockState(smithPos, ((ArcanaBlock)ArcanaRegistry.STARLIGHT_FORGE).getBlock().getDefaultState(),3);
               BlockEntity blockEntity = serverWorld.getBlockEntity(smithPos);
               if(blockEntity instanceof StarlightForgeBlockEntity forge){
                  ItemStack newForgeStack = forgeItem.addCrafter(forgeItem.getNewItem(),playerId,0,serverWorld.getServer());
                  ArcanaPolymerBlockEntity.initializeArcanaBlock(newForgeStack,forge);
                  
                  if(player != null){
                     if(!ArcanaNovum.data(player).addCrafted(newForgeStack)){
                        ArcanaNovum.data(player).addXP(ArcanaRarity.getCraftXp(forgeItem.getRarity()));
                     }
                  }
                  
                  ItemStack gappleStack = gappleEntity.getStack();
                  if(gappleStack.getCount() == 1){
                     gappleEntity.discard();
                  }else{
                     gappleStack.decrement(1);
                     gappleEntity.setStack(gappleStack);
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
         itemEntity.setStack(stack);
      }
      
      
      if(!ArcanaItemUtils.isArcane(stack) && stack.isOf(ArcanaRegistry.MUNDANE_ARCANE_PAPER)){ // Crafting an Arcane Tome
         boolean proceed = false;
         ItemEntity eyeEntity = null;
         
         BlockPos enchantPos = BlockPos.ofFloored(itemEntity.getPos().add(0,-0.25,0));
         BlockState state = world.getBlockState(enchantPos);
         if(state.isOf(Blocks.ENCHANTING_TABLE)){
            List<ItemEntity> otherEntities = world.getEntitiesByType(EntityType.ITEM,itemEntity.getBoundingBox().expand(1.25),e -> (!e.getUuid().equals(itemEntity.getUuid()) && !ArcanaItemUtils.isArcane(e.getStack())));
            for(ItemEntity other : otherEntities){
               ItemStack otherStack = other.getStack();
               if(otherStack.isOf(Items.ENDER_EYE)){
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
               PlayerEntity tomeCrafter = serverWorld.getClosestPlayer(itemEntity.getX(),itemEntity.getY(),itemEntity.getZ(),50,false);
               if(itemEntity.getOwner() instanceof PlayerEntity stackOwner){
                  tomeCrafter = stackOwner;
               }
               if(tomeCrafter != null){
                  IArcanaProfileComponent profile = ArcanaNovum.data(tomeCrafter);
                  tomeStack = ArcanaRegistry.ARCANE_TOME.addCrafter(tomeStack,tomeCrafter.getUuidAsString(),0,itemEntity.getServer());
                  if(!profile.addCrafted(tomeStack)){
                     profile.addXP(ArcanaRarity.getFirstCraftXp(ArcanaRegistry.ARCANE_TOME.getRarity()));
                  }
                  profile.addResearchedItem(ArcanaRegistry.ARCANE_TOME.getId());
               }
               
               ItemStack eyeStack = eyeEntity.getStack();
               if(eyeStack.getCount() == 1){
                  eyeEntity.discard();
               }else{
                  eyeStack.decrement(1);
                  eyeEntity.setStack(eyeStack);
               }
               
               if(stack.getCount() <= 4){
                  itemEntity.discard();
               }else{
                  stack.decrement(4);
                  itemEntity.setStack(stack);
               }
               
               ItemEntity tomeEntity = new ItemEntity(world, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), tomeStack);
               tomeEntity.setPickupDelay(40);
               if(tomeCrafter != null){
                  tomeEntity.setOwner(tomeCrafter.getUuid());
               }
               
               float f = world.random.nextFloat() * 0.1F;
               float g = world.random.nextFloat() * 6.2831855F;
               tomeEntity.setVelocity((double)(-MathHelper.sin(g) * f), 0.20000000298023224, (double)(MathHelper.cos(g) * f));
               world.spawnEntity(tomeEntity);
               
               ArcanaItem.removeProperty(stack,ArcaneTome.TOME_TAG);
            }else{
               ArcanaItem.putProperty(stack,ArcaneTome.TOME_TAG,craftTick);
            }
            ArcanaEffectUtils.craftTome(serverWorld,enchantPos,craftTick);
         }else{
            ArcanaItem.removeProperty(stack,ArcaneTome.TOME_TAG);
         }
         itemEntity.setStack(stack);
      }
      
      if(!ArcanaItemUtils.isArcane(stack) && stack.isOf(Items.OBSIDIAN) && itemEntity.isTouchingWater()){
         List<ItemEntity> otherEntities = world.getEntitiesByType(EntityType.ITEM,itemEntity.getBoundingBox().expand(1.25),e -> (!e.getUuid().equals(itemEntity.getUuid()) && e.isTouchingWater() && !ArcanaItemUtils.isArcane(e.getStack())));
   
         boolean create = false;
         for(ItemEntity other : otherEntities){
            ItemStack otherStack = other.getStack();
            int count = otherStack.getCount();
            if(otherStack.isOf(Items.GLOWSTONE_DUST) && count >= 4){
               if(count == 4){
                  other.discard();
               }else{
                  otherStack.decrement(4);
                  other.setStack(otherStack);
               }
               
               create = true;
               break;
            }else if(otherStack.isOf(Items.REDSTONE) && count >= 16){
               if(count == 16){
                  other.discard();
               }else{
                  otherStack.decrement(16);
                  other.setStack(otherStack);
               }
               
               create = true;
               break;
            }
         }
         
         if(create){
            ItemStack cObby = new ItemStack(Items.CRYING_OBSIDIAN);
            cObby.setCount(1);
            ItemEntity cryingObby = new ItemEntity(world, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), cObby);
            cryingObby.setPickupDelay(40);
   
            float f = world.random.nextFloat() * 0.1F;
            float g = world.random.nextFloat() * 6.2831855F;
            cryingObby.setVelocity((double)(-MathHelper.sin(g) * f), 0.20000000298023224, (double)(MathHelper.cos(g) * f));
            world.spawnEntity(cryingObby);
            
            if(stack.getCount() == 1){
               itemEntity.discard();
            }else{
               stack.decrement(1);
               itemEntity.setStack(stack);
            }
         }
         
      }
   }
   
   @Inject(method="onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z",shift = At.Shift.BEFORE))
   private void arcananovum$removeCraftData(PlayerEntity player, CallbackInfo ci, @Local LocalRef<ItemStack> itemStackRef){
      ItemStack stack = itemStackRef.get();
      if(stack.isOf(ArcanaRegistry.MUNDANE_ARCANE_PAPER)){
         ArcanaItem.removeProperty(stack,ArcaneTome.TOME_TAG);
         itemStackRef.set(stack);
      }
      if(stack.isOf(ArcanaRegistry.ARCANE_TOME.getItem())){
         ArcanaItem.removeProperty(stack,ArcaneTome.FORGE_TAG);
         itemStackRef.set(stack);
      }
   }
}
