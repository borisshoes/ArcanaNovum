package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.borisshoes.arcananovum.cardinalcomponents.MagicEntity;
import net.borisshoes.arcananovum.items.ContinuumAnchor;
import net.borisshoes.arcananovum.items.IgneousCollider;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.items.arrows.ArcaneFlakArrows;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.SculkShriekerBlockEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static net.borisshoes.arcananovum.Arcananovum.*;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.*;

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
                              ContinuumAnchor.removeChunk(serverWorld,new ChunkPos(chunkPos.x+i,chunkPos.z+j));
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
                  }else if(id.equals(MagicItems.SPAWNER_INFUSER.getId())){ // Spawner Infuser Tick
                     if(state.getBlock().asItem() == MagicItems.SPAWNER_INFUSER.getPrefItem().getItem()){ // Check that the block is still there
                        spawnerInfuserTick(serverWorld,pos,blockData);
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
                  ParticleEffect dust = new DustParticleEffect(Vec3d.unpackRgb(16711892).toVector3f(),3f);
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
            }else if(id.equals(MagicItems.ARCANE_FLAK_ARROWS.getId())){
               int armTime = magicData.getInt("armTime");
               if(armTime > 0){
                  armTime--;
                  magicData.putInt("armTime",armTime);
               }
   
               Entity found = serverWorld.getEntity(UUID.fromString(uuid));
               if(found instanceof PersistentProjectileEntity arrow && armTime == 0){
                  double senseRange = 4;
                  List<Entity> triggerTargets = serverWorld.getOtherEntities(found,found.getBoundingBox().expand(senseRange*2),
                        e -> !e.isSpectator() && e.distanceTo(found) <= senseRange && e instanceof LivingEntity && !e.isOnGround());
                  if(triggerTargets.size() > 0){
                     iter2.remove();
                     double radius = 4 + 1.25*Math.max(0, ArcanaAugments.getAugmentFromCompound(magicEntity.getData(),"airburst"));
                     ArcaneFlakArrows.detonate(arrow,radius);
                  }
               }
            }else if(id.equals(MagicItems.TETHER_ARROWS.getId())){
               String ownerId = magicData.getString("owner");
               boolean severed = magicData.getBoolean("severed");
               
               if(!ownerId.isEmpty() && !severed){
                  PlayerEntity owner = serverWorld.getPlayerByUuid(UUID.fromString(ownerId));
                  if(owner != null && owner.isSneaking() && ArcanaAugments.getAugmentFromCompound(magicData, "quick_release") == 1){
                     magicData.putBoolean("severed", true);
                     owner.sendMessage(Text.literal("Arcane Tethers Severed").formatted(Formatting.GRAY, Formatting.ITALIC), true);
                  }
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
   
   private static final double[] anchorEfficiency = {0,.05,.1,.15,.2,.5};
   private static void continuumAnchorTick(ServerWorld serverWorld, BlockPos pos, ChunkPos chunkPos, NbtCompound blockData){
      if(serverWorld.getServer().getTicks() % 5 == 0){ // Anchor only ticks redstone and load update every quarter second
      
         int fuel = blockData.getInt("fuel");
         boolean active = !serverWorld.isReceivingRedstonePower(pos) && fuel != 0; // Redstone low is ON
         boolean prevActive = blockData.getBoolean("active");
         int range = blockData.getInt("range");
         blockData.putBoolean("active",active); // Update redstone power
         if(active && serverWorld.getServer().getTicks() % 20 == 0){
            int lvl = Math.max(0,ArcanaAugments.getAugmentFromCompound(blockData,"temporal_relativity"));
            if(Math.random() >= anchorEfficiency[lvl]){
               fuel = Math.max(0, fuel - 1);
               blockData.putInt("fuel", fuel);
            }
            
            String crafterId = blockData.getString("crafter");
            if(!crafterId.isEmpty()){
               ServerPlayerEntity player = serverWorld.getServer().getPlayerManager().getPlayer(UUID.fromString(crafterId));
               if(player == null){
                  Arcananovum.addLoginCallback(new AnchorTimeLoginCallback(serverWorld.getServer(),crafterId,1));
                  if(serverWorld.getServer().getTicks() % 100 == 0) Arcananovum.addLoginCallback(new XPLoginCallback(serverWorld.getServer(),crafterId,1));
               }else{
                  ArcanaAchievements.progress(player,"timey_wimey",1);
                  if(serverWorld.getServer().getTicks() % 100 == 0) PLAYER_DATA.get(player).addXP(1);
               }
            }
         }
         int fuelMarks = (int)Math.min(Math.ceil(4.0*fuel/600000.0),4);
         serverWorld.setBlockState(pos, Blocks.RESPAWN_ANCHOR.getDefaultState().with(Properties.CHARGES,fuelMarks), Block.NOTIFY_ALL);
      
         // Do the chunk loading thing
         if(prevActive && !active){ // Power Down
            for(int i = -range; i <= range; i++){
               for(int j = -range; j <= range; j++){
                  ContinuumAnchor.removeChunk(serverWorld,new ChunkPos(chunkPos.x+i,chunkPos.z+j));
               }
            }
            serverWorld.playSound(null,pos.getX(),pos.getY(),pos.getZ(), SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.MASTER,1,1.5f,0L);
         }else if(!prevActive && active){ // Power Up
            for(int i = -range; i <= range; i++){
               for(int j = -range; j <= range; j++){
                  ContinuumAnchor.addChunk(serverWorld,new ChunkPos(chunkPos.x+i,chunkPos.z+j));
               }
            }
            serverWorld.playSound(null,pos.getX(),pos.getY(),pos.getZ(),SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.MASTER,1,.7f);
         }
      }
   }
   
   private static void spawnerInfuserTick(ServerWorld serverWorld, BlockPos pos, NbtCompound blockData){
      if(serverWorld.getServer().getTicks() % 5 == 0){ // Infuser only ticks redstone every quarter second
         // Check for spawner above, match soulstone type, update redstone power, do particles
         boolean prevActive = blockData.getBoolean("active");
         boolean hasRedstone = serverWorld.isReceivingRedstonePower(pos); // Redstone high is ON
         NbtCompound soulstone = blockData.getCompound("soulstone");
         boolean hasSoulstone = !soulstone.isEmpty();
         BlockPos spawnerPos = pos.add(0,2,0);
         BlockEntity blockEntity = serverWorld.getBlockEntity(spawnerPos);
         BlockState spawnerState = serverWorld.getBlockState(spawnerPos);
         boolean hasSpawner =  spawnerState.isOf(Blocks.SPAWNER) && blockEntity instanceof MobSpawnerBlockEntity;
         
         if(!hasRedstone || !hasSoulstone || !hasSpawner){
            if(prevActive) blockData.putBoolean("active", false); // Update active status
            serverWorld.setBlockState(pos, Blocks.SCULK_SHRIEKER.getDefaultState().with(Properties.CAN_SUMMON,false), Block.NOTIFY_ALL);
            return;
         }
         
         ItemStack stone = ItemStack.fromNbt(soulstone);
         String stoneType = Soulstone.getType(stone);
         MobSpawnerBlockEntity spawnerEntity = (MobSpawnerBlockEntity) blockEntity;
         NbtCompound spawnerData = spawnerEntity.getLogic().writeNbt(new NbtCompound());
         NbtCompound spawnData = spawnerData.getCompound("SpawnData");
         if(spawnData.isEmpty() || !spawnData.contains("entity") || !spawnData.getCompound("entity").contains("id")){
            if(prevActive) blockData.putBoolean("active", false); // Update active status
            serverWorld.setBlockState(pos, Blocks.SCULK_SHRIEKER.getDefaultState().with(Properties.CAN_SUMMON,false), Block.NOTIFY_ALL);
            return;
         }
         NbtCompound spawnEntity = spawnData.getCompound("entity");
         
         boolean correctType = stoneType.equals(spawnEntity.getString("id"));
         
         if(correctType){
            if(!prevActive) blockData.putBoolean("active", true); // Update active status
            serverWorld.setBlockState(pos, Blocks.SCULK_SHRIEKER.getDefaultState().with(Properties.CAN_SUMMON,true), Block.NOTIFY_ALL);
            ParticleEffectUtils.spawnerInfuser(serverWorld,pos,5);
            SoundUtils.soulSounds(serverWorld,pos,1,5);
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
            boolean canUseIce = Math.max(0,ArcanaAugments.getAugmentFromCompound(blockData,"cryogenic_cooling")) >= 1;
         
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
                  }else if(canUseIce && block2 == Blocks.BLUE_ICE){
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
               int efficiencyLvl = Math.max(0,ArcanaAugments.getAugmentFromCompound(blockData,"thermal_expansion"));
               if(Math.random() >= .1*efficiencyLvl){
                  if(serverWorld.getBlockState(hasLava).getBlock() == Blocks.LAVA){
                     serverWorld.setBlockState(hasLava, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                  }else if(serverWorld.getBlockState(hasLava).getBlock() == Blocks.LAVA_CAULDRON){
                     serverWorld.setBlockState(hasLava, Blocks.CAULDRON.getDefaultState(), Block.NOTIFY_ALL);
                  }
                  if(serverWorld.getBlockState(hasWater).getBlock() == Blocks.WATER){
                     serverWorld.setBlockState(hasWater, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                  }else if(serverWorld.getBlockState(hasWater).getBlock() == Blocks.WATER_CAULDRON){
                     serverWorld.setBlockState(hasWater, Blocks.CAULDRON.getDefaultState(), Block.NOTIFY_ALL);
                  }
               }
            
               SoundUtils.playSound(serverWorld,pos,SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.BLOCKS, 1, .6f);
   
               String crafterId = blockData.getString("crafter");
               if(!crafterId.isEmpty()){
                  ServerPlayerEntity player = serverWorld.getServer().getPlayerManager().getPlayer(UUID.fromString(crafterId));
                  if(player == null){
                     Arcananovum.addLoginCallback(new ColliderLoginCallback(serverWorld.getServer(),crafterId,1));
                     Arcananovum.addLoginCallback(new XPLoginCallback(serverWorld.getServer(),crafterId,10));
                  }else{
                     ArcanaAchievements.progress(player,"endless_extrusion",1);
                     PLAYER_DATA.get(player).addXP(10);
                     if(obby.isOf(Items.CRYING_OBSIDIAN)) ArcanaAchievements.grant(player,"expensive_infusion");
                  }
               }
            }
   
            int injectionLvl = Math.max(0,ArcanaAugments.getAugmentFromCompound(blockData,"magmatic_injection"));
            cooldown = IgneousCollider.COOLDOWN-1-2*injectionLvl;
         }
         blockData.putInt("cooldown",cooldown);
      }
   }
}
