package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicBlock;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockEntity;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
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
import java.util.UUID;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
   
   @Inject(method="tick",at=@At("TAIL"))
   private void arcananovum_worldDetection(CallbackInfo ci){
      ItemEntity itemEntity = (ItemEntity) (Object) this;
      ItemStack stack = itemEntity.getStack();
      World world = itemEntity.getWorld();
      MagicItem magicItem = MagicItemUtils.identifyItem(stack);
      if(!(itemEntity.getWorld() instanceof ServerWorld serverWorld)) return;
      
      if(magicItem instanceof Soulstone){
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
      }else if(magicItem instanceof ArcaneTome){ // Crafting a Starlight Forge
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicTag = itemNbt.getCompound("arcananovum");
         int craftTick = 0;
         if(magicTag.contains("forgeCraftTick")){
            craftTick = magicTag.getInt("forgeCraftTick");
         }
         List<ItemEntity> otherEntities = world.getEntitiesByType(EntityType.ITEM,itemEntity.getBoundingBox().expand(1.25),e -> (!e.getUuid().equals(itemEntity.getUuid()) && !MagicItemUtils.isMagic(e.getStack())));
         
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
         
         if(proceed){
            craftTick++;
            if(craftTick == 100){
               serverWorld.setBlockState(smithPos, ((MagicBlock)ArcanaRegistry.STARLIGHT_FORGE).getBlock().getDefaultState(),3);
               BlockEntity blockEntity = serverWorld.getBlockEntity(smithPos);
               if(blockEntity instanceof StarlightForgeBlockEntity forge){
                  MagicItem forgeItem = ArcanaRegistry.STARLIGHT_FORGE;
                  String playerId = forgeItem.getCrafter(stack);
                  ItemStack newForgeStack = forgeItem.addCrafter(forgeItem.getNewItem(),playerId,false,serverWorld.getServer());
                  MagicPolymerBlockEntity.initializeMagicBlock(newForgeStack,forge);
                  
                  ServerPlayerEntity player = serverWorld.getServer().getPlayerManager().getPlayer(UUID.fromString(playerId));
                  if(player != null){
                     if(!PLAYER_DATA.get(player).addCrafted(newForgeStack)){
                        PLAYER_DATA.get(player).addXP(MagicRarity.getCraftXp(magicItem.getRarity()));
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
               magicTag.remove("forgeCraftTick");
            }else{
               magicTag.putInt("forgeCraftTick",craftTick);
            }
            ParticleEffectUtils.craftForge(serverWorld,smithPos,craftTick);
         }else{
            magicTag.remove("forgeCraftTick");
         }
         itemEntity.setStack(stack);
      }
      
      if(!MagicItemUtils.isMagic(stack) && stack.isOf(Items.OBSIDIAN) && itemEntity.isTouchingWater()){
         List<ItemEntity> otherEntities = world.getEntitiesByType(EntityType.ITEM,itemEntity.getBoundingBox().expand(1.25),e -> (!e.getUuid().equals(itemEntity.getUuid()) && e.isTouchingWater() && !MagicItemUtils.isMagic(e.getStack())));
   
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
}
