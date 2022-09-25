package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.borisshoes.arcananovum.cardinalcomponents.MagicEntity;
import net.borisshoes.arcananovum.items.IgneousCollider;
import net.borisshoes.arcananovum.items.MagicItem;
import net.borisshoes.arcananovum.items.MagicItems;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static net.borisshoes.arcananovum.Arcananovum.SERVER_TIMER_CALLBACKS;
import static net.borisshoes.arcananovum.Arcananovum.WORLD_TIMER_CALLBACKS;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_BLOCK_LIST;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_ENTITY_LIST;

public class WorldTickCallback {
   
   public static void onWorldTick(ServerWorld serverWorld){
      try{
         // Magic Block Tick
         List<MagicBlock> blocks = MAGIC_BLOCK_LIST.get(serverWorld).getBlocks();
         Iterator<MagicBlock> iter = blocks.iterator();
         while(iter.hasNext()){
            MagicBlock magicBlock = iter.next();
            BlockPos pos = magicBlock.getPos();
            long chunkPosL = ChunkPos.toLong(pos);
            ChunkPos chunkPos = new ChunkPos(pos);
            
            //System.out.println(serverWorld.isChunkLoaded(chunkPos.x,chunkPos.z));
            if(serverWorld.shouldTickBlocksInChunk(chunkPosL)){ // Only tick blocks in loaded chunks
               NbtCompound blockData = magicBlock.getData();
               if(blockData.contains("id")){
                  String id = blockData.getString("id");
                  BlockState state = serverWorld.getBlockState(pos);
                  if(!blockData.contains("UUID")){
                     blockData.putString("UUID", UUID.randomUUID().toString());
                  }
                  
                  if(id.equals(MagicItems.CONTINUUM_ANCHOR.getId())){ // Continuum Anchor Tick
                     if(state.getBlock().asItem() == MagicItems.CONTINUUM_ANCHOR.getPrefItem().getItem()){ // First check that the block is still there
                        continuumAnchorTick(serverWorld, pos, chunkPos, blockData);
                     }else{ // If block is no longer there deload chunks, remove it from the blocklist.
                        int range = blockData.getInt("range");
                        for(int i = -range; i <= range; i++){
                           for(int j = -range; j <= range; j++){
                              serverWorld.setChunkForced(chunkPos.x+i,chunkPos.z+j,false);
                           }
                        }
                        iter.remove();
                     }
                  }else if(id.equals(MagicItems.FRACTAL_SPONGE.getId())){ // Fractal Sponge Tick
                     if(state.getBlock().asItem() != MagicItems.FRACTAL_SPONGE.getPrefItem().getItem() && state.getBlock().asItem() != Items.WET_SPONGE){ // Check that the block is still there
                        iter.remove();
                     }
                  }else if(id.equals(MagicItems.IGNEOUS_COLLIDER.getId())){ // Igneous Collider Tick
                     if(state.getBlock().asItem() == MagicItems.IGNEOUS_COLLIDER.getPrefItem().getItem()){ // Check that the block is still there
                        igneousColliderTick(serverWorld, pos, blockData);
                     }else{
                        iter.remove();
                     }
                  }
               }
            }
         }
         
         // Keep tabs on magic entities
         List<MagicEntity> entities = MAGIC_ENTITY_LIST.get(serverWorld).getEntities();
         Iterator<MagicEntity> iter2 = entities.iterator();
         while(iter2.hasNext()){
            MagicEntity magicEntity = iter2.next();
            NbtCompound magicData = magicEntity.getData();
            String id = magicData.getString("id");
            String uuid = magicEntity.getUuid();
            
            if(id.equals(MagicItems.STASIS_PEARL.getId())){
               int keepAlive = magicData.getInt("keepAlive");
               boolean alive = magicData.getBoolean("alive");
               boolean stasis = magicData.getBoolean("stasis");
               boolean kill = false;
               magicData.putInt("keepAlive",keepAlive-1);
               if(keepAlive < 0 || !alive){ // Remove item
                  kill = true;
               }
               if(!stasis || kill){ // Clean up
                  Entity found = serverWorld.getEntity(UUID.fromString(uuid));
                  if(found == null){
                     iter2.remove();
                  }else if(kill){
                     found.kill();
                     iter2.remove();
                  }
               }else if(serverWorld.getServer().getTicks() % 3 == 0){ // Do stasis particle effects
                  NbtCompound pearlData = magicData.getCompound("pearlData");
                  NbtList pos = pearlData.getList("Pos", NbtList.DOUBLE_TYPE);
                  ParticleEffectUtils.stasisPearl(serverWorld,new Vec3d(pos.getDouble(0),pos.getDouble(1),pos.getDouble(2)));
               }
            }else if(id.equals("boss_dragon_phantom")){
               Entity found = serverWorld.getEntity(UUID.fromString(uuid));
               if(found != null){
                  ParticleEffect dust = new DustParticleEffect(new Vec3f(Vec3d.unpackRgb(16711892)),3f);
                  ServerWorld entityWorld = (ServerWorld) found.getEntityWorld();
                  entityWorld.spawnParticles(dust,found.getX(),found.getY(),found.getZ(),1,1.5,1,1.5,0);
               }
            }else if(id.equals("boss_dragon_wizard")){
               Entity found = serverWorld.getEntity(UUID.fromString(uuid));
               if(found != null && serverWorld.getServer().getTicks() % 4 == 0){
                  ServerWorld entityWorld = (ServerWorld) found.getEntityWorld();
                  entityWorld.spawnParticles(ParticleTypes.CLOUD,found.getX(),found.getY(),found.getZ(),5,0.25,0.25,0.25,0);
                  PlayerEntity nearestPlayer = entityWorld.getClosestPlayer(found,25);
                  if(nearestPlayer != null)
                     found.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES,nearestPlayer.getEyePos());
               }
            }
         }
   
         // Tick Timer Callbacks
         ArrayList<Pair<ServerWorld,TickTimerCallback>> toRemove = new ArrayList<>();
         for(int i = 0; i < WORLD_TIMER_CALLBACKS.size(); i++){
            Pair<ServerWorld,TickTimerCallback> pair = WORLD_TIMER_CALLBACKS.get(i);
            TickTimerCallback t = pair.getRight();
            if(pair.getLeft().getRegistryKey() == serverWorld.getRegistryKey()){
               if(t.decreaseTimer() == 0){
                  t.onTimer();
                  toRemove.add(pair);
               }
            }
         }
         WORLD_TIMER_CALLBACKS.removeIf(toRemove::contains);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   private static void continuumAnchorTick(ServerWorld serverWorld, BlockPos pos, ChunkPos chunkPos, NbtCompound blockData){
      if(serverWorld.getServer().getTicks() % 20 == 0){ // Anchor only ticks redstone and load update every second
      
         int fuel = blockData.getInt("fuel");
         boolean active = !serverWorld.isReceivingRedstonePower(pos) && fuel != 0; // Redstone low is ON
         boolean prevActive = blockData.getBoolean("active");
         int range = blockData.getInt("range");
         blockData.putBoolean("active",active); // Update redstone power
         //System.out.println();
         //System.out.println("ticking anchor at "+pos.toShortString());
         if(active){
            fuel = Math.max(0,fuel-1);
            blockData.putInt("fuel",fuel);
            //System.out.println("ticking active anchor at "+pos.toShortString()+" | "+chunkPos.x+", "+chunkPos.z);
         }
         int fuelMarks = (int)Math.min(Math.ceil(4.0*fuel/600000.0),4);
         //System.out.println("fuel marks: "+fuelMarks +" fuel:"+fuel);
         serverWorld.setBlockState(pos, Blocks.RESPAWN_ANCHOR.getDefaultState().with(Properties.CHARGES,fuelMarks), Block.NOTIFY_ALL);
      
         // Do the chunk loading thing
         if(prevActive && !active){ // Power Down
            //System.out.print("Deactivating chunks");
            for(int i = -range; i <= range; i++){
               for(int j = -range; j <= range; j++){
                  serverWorld.setChunkForced(chunkPos.x+i,chunkPos.z+j,false);
               }
            }
            serverWorld.playSound(null,pos.getX(),pos.getY(),pos.getZ(), SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.MASTER,1,1.5f);
         }else if(!prevActive && active){ // Power Up
            //System.out.print("Activating chunks");
            for(int i = -range; i <= range; i++){
               for(int j = -range; j <= range; j++){
                  serverWorld.setChunkForced(chunkPos.x+i,chunkPos.z+j,true);
               }
            }
            serverWorld.playSound(null,pos.getX(),pos.getY(),pos.getZ(),SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.MASTER,1,.7f);
         }
      }
   }
   
   
   private static void igneousColliderTick(ServerWorld serverWorld, BlockPos pos, NbtCompound blockData){
      if(serverWorld.getServer().getTicks() % 20 == 0){ // Tick the block every second
         int cooldown = blockData.getInt("cooldown");
         if(cooldown-- == 0){
            // Do the check
            BlockPos hasLava = null;
            BlockPos hasWater = null;
            BlockPos hasInventory = null;
            BlockPos hasNetherite = null;
            Inventory output = null;
         
            Direction[] dirs = Direction.values();
            int numDirs = dirs.length;
         
            for(int side = 0; side < numDirs; ++side){
               Direction direction = dirs[side];
               BlockPos pos2 = pos.offset(direction);
               BlockState state2 = serverWorld.getBlockState(pos2);
               Block block2 = state2.getBlock();
            
               if(direction.getAxis() != Direction.Axis.Y){ // Check for fluid
                  if(block2 == Blocks.LAVA && state2.getFluidState().isStill()){
                     hasLava = pos2;
                  }else if(block2 == Blocks.WATER  && state2.getFluidState().isStill()){
                     hasWater = pos2;
                  }else if(block2 == Blocks.LAVA_CAULDRON){
                     hasLava = pos2;
                  }else if(block2 == Blocks.WATER_CAULDRON){
                     hasWater = pos2;
                  }
               }else if(direction.getId() == 1){ // Check for chest
                  if (block2 instanceof InventoryProvider) {
                     output = ((InventoryProvider)block2).getInventory(state2, serverWorld, pos2);
                  } else if (state2.hasBlockEntity()) {
                     BlockEntity blockEntity = serverWorld.getBlockEntity(pos2);
                     if (blockEntity instanceof Inventory) {
                        output = (Inventory)blockEntity;
                        if (output instanceof ChestBlockEntity && block2 instanceof ChestBlock) {
                           output = ChestBlock.getInventory((ChestBlock)block2, state2, serverWorld, pos2, true);
                        }
                     }
                  }
                  if (output != null){
                     hasInventory = pos2;
                  }
               }else if(direction.getId() == 0){ //Check for netherite block
                  if(block2 == Blocks.NETHERITE_BLOCK){
                     hasNetherite = pos2;
                  }
               }
            }
            if(hasLava != null && hasWater != null){ // Produce Obsidian
               ItemStack obby;
               if(hasNetherite == null){
                  obby = new ItemStack(Items.OBSIDIAN);
               }else{
                  obby = new ItemStack(Items.CRYING_OBSIDIAN);
               }
            
               if(hasInventory == null){ // Drop above collider
                  serverWorld.spawnEntity(new ItemEntity(serverWorld,pos.getX()+0.5,pos.getY()+1.25,pos.getZ()+0.5,obby, 0, 0.2, 0));
               }else{ // Put in inventory
                  Inventory finalOutput = output;
                  boolean isFull = (output instanceof SidedInventory ? IntStream.of(((SidedInventory)output).getAvailableSlots(Direction.DOWN)) : IntStream.range(0, output.size())).allMatch((slot) -> {
                     ItemStack itemStack = finalOutput.getStack(slot);
                     return itemStack.getCount() >= itemStack.getMaxCount();
                  });
                  if(isFull){
                     serverWorld.spawnEntity(new ItemEntity(serverWorld,pos.getX()+0.5,pos.getY()+2.5,pos.getZ()+0.5,obby, 0, 0.2, 0));
                  }else{
                     boolean inserted = false;
                  
                     if (output instanceof SidedInventory sidedInventory) {
                        int[] is = sidedInventory.getAvailableSlots(Direction.DOWN);
                     
                        for(int i = 0; i < is.length; ++i) {
                           // Check if can be inserted
                           ItemStack toStack = output.getStack(i);
                           if(output.isValid(i, obby) && ((SidedInventory)output).canInsert(i, obby, Direction.DOWN)){
                              if(toStack.isEmpty()){
                                 output.setStack(i,obby);
                                 inserted = true;
                                 break;
                              }else if(toStack.isOf(obby.getItem()) && toStack.getCount()+obby.getCount() < obby.getMaxCount() && ItemStack.areNbtEqual(toStack,obby)){
                                 toStack.increment(obby.getCount());
                                 inserted = true;
                                 break;
                              }
                           }
                        }
                     } else {
                        int j = output.size();
                     
                        for(int k = 0; k < j; ++k) {
                           // Check if can be inserted
                           ItemStack toStack = output.getStack(k);
                           if(output.isValid(k, obby)){
                              if(toStack.isEmpty()){
                                 output.setStack(k,obby);
                                 inserted = true;
                                 break;
                              }else if(toStack.isOf(obby.getItem()) && toStack.getCount()+obby.getCount() < obby.getMaxCount() && ItemStack.areNbtEqual(toStack,obby)){
                                 toStack.increment(obby.getCount());
                                 inserted = true;
                                 break;
                              }
                           }
                        }
                     }
                  
                  
                     if(!inserted){
                        serverWorld.spawnEntity(new ItemEntity(serverWorld,pos.getX()+0.5,pos.getY()+2.5,pos.getZ()+0.5,obby, 0, 0.2, 0));
                     }else{
                        output.markDirty();
                     }
                  }
               }
            
               // Remove Source Blocks
               if(serverWorld.getBlockState(hasLava).getBlock() == Blocks.LAVA){
                  serverWorld.setBlockState(hasLava, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
               }else{
                  serverWorld.setBlockState(hasLava, Blocks.CAULDRON.getDefaultState(), Block.NOTIFY_ALL);
               }
               if(serverWorld.getBlockState(hasWater).getBlock() == Blocks.WATER){
                  serverWorld.setBlockState(hasWater, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
               }else{
                  serverWorld.setBlockState(hasWater, Blocks.CAULDRON.getDefaultState(), Block.NOTIFY_ALL);
               }
            
               SoundUtils.playSound(serverWorld,pos,SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.BLOCKS, 1, .6f);
            }
         
            cooldown = IgneousCollider.COOLDOWN-1;
         }
         blockData.putInt("cooldown",cooldown);
      }
   }
}
