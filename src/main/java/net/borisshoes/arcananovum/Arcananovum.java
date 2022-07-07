package net.borisshoes.arcananovum;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import net.borisshoes.arcananovum.callbacks.CommandRegisterCallback;
import net.borisshoes.arcananovum.callbacks.LoginCallback;
import net.borisshoes.arcananovum.callbacks.ShieldLoginCallback;
import net.borisshoes.arcananovum.callbacks.TickTimerCallback;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.borisshoes.arcananovum.gui.LoreGui;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.Utils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.borisshoes.arcananovum.cardinalcomponents.LoginCallbackComponentInitializer.LOGIN_CALLBACK_LIST;
import static net.borisshoes.arcananovum.cardinalcomponents.MagicBlocksComponentInitializer.MAGIC_BLOCK_LIST;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.borisshoes.arcananovum.gui.TomeGui.getGuideBook;
import static net.minecraft.command.argument.EntityArgumentType.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Arcananovum implements ModInitializer {
   
   private static final Logger logger = LogManager.getLogger("Arcana Novum");
   private static final ArrayList<TickTimerCallback> TIMER_CALLBACKS = new ArrayList<>();
   public static final boolean devMode = true;
   
   @Override
   public void onInitialize(){
      ServerTickEvents.END_WORLD_TICK.register(this::onWorldTick);
      ServerTickEvents.END_SERVER_TICK.register(this::onTick);
      UseItemCallback.EVENT.register(this::useItem);
      UseBlockCallback.EVENT.register(this::useBlock);
      PlayerBlockBreakEvents.BEFORE.register(this::breakBlock);
      ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(this::killedEntity);
      AttackEntityCallback.EVENT.register(this::attackEntity);
      ServerPlayConnectionEvents.JOIN.register(this::onPlayerJoin);
      CommandRegistrationCallback.EVENT.register(CommandRegisterCallback::registerCommands);
   
      logger.info("Initializing Arcana Novum");
   }
   
   private void onPlayerJoin(ServerPlayNetworkHandler netHandler, PacketSender sender, MinecraftServer server){
      ServerPlayerEntity player = netHandler.player;
      //log(player.getEntityName()+" has joined the game");
      
      ArrayList<LoginCallback> toBeRemoved = new ArrayList<>();
      for(LoginCallback callback : LOGIN_CALLBACK_LIST.get(server.getWorld(ServerWorld.OVERWORLD)).getCallbacks()){
         if(callback.getPlayer().equals(player.getUuidAsString())){
            //log("Running login callback for "+player.getEntityName()+". ID: "+callback.getId());
            callback.onLogin(netHandler,server);
            toBeRemoved.add(callback);
         }
      }
      for(LoginCallback callback :toBeRemoved){
         LOGIN_CALLBACK_LIST.get(server.getWorld(ServerWorld.OVERWORLD)).removeCallback(callback);
      }
   }
   
   private boolean breakBlock(World world, PlayerEntity playerEntity, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity){
      List<MagicBlock> blocks = MAGIC_BLOCK_LIST.get(world).getBlocks();
      for(MagicBlock magicBlock : blocks){
         if(magicBlock.getPos().equals(blockPos)){
            NbtCompound data = magicBlock.getData();
            String magicId = data.getString("id");
            MagicItem itemType = MagicItemUtils.getItemFromId(magicId);
            if(itemType instanceof BlockItem){
               List<ItemStack> drops = ((BlockItem) itemType).dropFromBreak(world,playerEntity,blockPos,blockState,blockEntity,data);
               for(ItemStack drop : drops){
                  world.spawnEntity(new ItemEntity(world,blockPos.getX(),blockPos.getY(),blockPos.getZ(),drop));
               }
            }
            world.breakBlock(blockPos,false,playerEntity);
            return false;
         }
      }
      return true;
   }
   
   private ActionResult useBlock(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult blockHitResult){
      ItemStack item = playerEntity.getStackInHand(hand);
      //System.out.println(hand+" "+item);
      ActionResult result = ActionResult.PASS;
      try{
         UsableItem magicItem = null;
         if(MagicItemUtils.isUsableItem(item)){
            magicItem = MagicItemUtils.identifyUsableItem(item);
            boolean useReturn = magicItem.useItem(playerEntity,world,hand,blockHitResult);
            result = useReturn ? ActionResult.PASS : ActionResult.SUCCESS;
            if(playerEntity instanceof ServerPlayerEntity player){
               player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40, item));
            }
         }
         
         // Magic Block check
         List<MagicBlock> blocks = MAGIC_BLOCK_LIST.get(world).getBlocks();
         for(MagicBlock magicBlock : blocks){
            if(magicBlock.getPos().equals(blockHitResult.getBlockPos())){
               NbtCompound blockData = magicBlock.getData();
               if(blockData.contains("id")){
                  String id = blockData.getString("id");
                  if(id.equals(MagicItems.CONTINUUM_ANCHOR.getId())){ // Continuum Anchor Check
                     int curFuel = blockData.getInt("fuel");
                     if(magicItem instanceof ExoticMatter){ // Try to add fuel
                        blockData.putInt("fuel",curFuel+((ExoticMatter) magicItem).getEnergy(item));
                        item.decrement(item.getCount());
                        item.setNbt(new NbtCompound());
                     }else if(playerEntity.getMainHandStack().isEmpty() && playerEntity.getMainHandStack().isEmpty() && curFuel > 0){ // Remove fuel if both hands are empty
                        blockData.putInt("fuel",0);
                        ItemStack removedFuelItem = MagicItems.EXOTIC_MATTER.getNewItem();
                        ((ExoticMatter)MagicItemUtils.identifyEnergyItem(removedFuelItem)).setFuel(removedFuelItem,curFuel);
                        playerEntity.giveItemStack(removedFuelItem);
                     }
                     result = ActionResult.SUCCESS;
                  }
               }
            }
         }
         
         return result;
      }catch(Exception e){
         e.printStackTrace();
         return ActionResult.PASS;
      }
   }
   
   private TypedActionResult<ItemStack> useItem(PlayerEntity playerEntity, World world, Hand hand){
      ItemStack item = playerEntity.getStackInHand(hand);
      try{
         if(MagicItemUtils.isUsableItem(item)){
            UsableItem magicItem = MagicItemUtils.identifyUsableItem(item);
            boolean useReturn = magicItem.useItem(playerEntity,world,hand);
            if(playerEntity instanceof ServerPlayerEntity player){
               player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40, item));
            }
            return useReturn ? TypedActionResult.pass(item) : TypedActionResult.success(item);
         }
         return TypedActionResult.pass(item);
      }catch(Exception e){
         e.printStackTrace();
         return TypedActionResult.pass(item);
      }
   }
   
   private ActionResult attackEntity(PlayerEntity playerEntity, World world, Hand hand, Entity entity, @Nullable EntityHitResult entityHitResult){
      ItemStack item = playerEntity.getStackInHand(hand);
      try{
         if(MagicItemUtils.isAttackingItem(item)){
            AttackingItem magicItem = MagicItemUtils.identifyAttackingItem(item);
            boolean attackReturn = magicItem.attackEntity(playerEntity,world,hand,entity,entityHitResult);
            return attackReturn ? ActionResult.PASS : ActionResult.SUCCESS;
         }
         return ActionResult.PASS;
      }catch(Exception e){
         e.printStackTrace();
         return ActionResult.PASS;
      }
   }
   
   private void killedEntity(ServerWorld serverWorld, Entity entity, LivingEntity livingEntity){
      try{
         if(entity instanceof ServerPlayerEntity){
            ServerPlayerEntity player = (ServerPlayerEntity) entity;
            String entityTypeId = EntityType.getId(livingEntity.getType()).toString();
            String entityTypeName = EntityType.get(entityTypeId).get().getName().getString();
            
            // Check for soulstone then activate
            PlayerInventory inv = player.getInventory();
            for(int i=0; i<inv.size();i++){
               ItemStack item = inv.getStack(i);
               if(item.isEmpty())
                  continue;
               boolean isMagic = MagicItemUtils.isMagic(item);
               if(!isMagic)
                  continue; // Item not magic, skip
               MagicItem magicItem = MagicItemUtils.identifyItem(item);
               if(magicItem instanceof Soulstone){
                  Soulstone stone = (Soulstone) magicItem;
                  if(Soulstone.getType(item).equals(entityTypeId)){
                     stone.killedEntity(serverWorld,player,livingEntity, item);
                     break; // Only activate one soulstone per kill
                  }
               }
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   
   private void onTick(MinecraftServer server){
      try{
         ServerScoreboard scoreboard = server.getScoreboard();
         if(scoreboard.getNullableObjective("arcananovum_sojourn_walk") == null){
            ScoreboardCriterion walked = ScoreboardCriterion.getOrCreateStatCriterion("minecraft.custom:minecraft.walk_one_cm").orElseThrow();
            scoreboard.addObjective("arcananovum_sojourn_walk",walked,new LiteralText("dist_walked_sojourn"),walked.getDefaultRenderType());
         }
         if(scoreboard.getNullableObjective("arcananovum_sojourn_sprint") == null){
            ScoreboardCriterion sprinted = ScoreboardCriterion.getOrCreateStatCriterion("minecraft.custom:minecraft.sprint_one_cm").orElseThrow();
            scoreboard.addObjective("arcananovum_sojourn_sprint",sprinted,new LiteralText("dist_sprinted_sojourn"),sprinted.getDefaultRenderType());
         }
         
         List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
         for(ServerPlayerEntity player : players){
            IArcanaProfileComponent arcaneProfile = PLAYER_DATA.get(player);
            
            // Check each player's inventory for magic items
            PlayerInventory inv = player.getInventory();
            for(int i=0; i<inv.size();i++){
               ItemStack item = inv.getStack(i);
               if(item.isEmpty()){
                  if(item.getNbt() != null){
                     item.setNbt(null);
                  }else{
                     continue;
                  }
               }
               if(item.hasNbt()){
                  if(item.getNbt().contains("ArcanaGuideBook")){
                     ItemStack newArcanaTome = MagicItems.ARCANE_TOME.getNewItem();
                     inv.setStack(i,newArcanaTome);
                     arcaneProfile.addCrafted(MagicItems.ARCANE_TOME.getId());
                  }
               }
               
               boolean isMagic = MagicItemUtils.isMagic(item);
               if(!isMagic)
                  continue; // Item not magic, skip
               if(MagicItemUtils.needsVersionUpdate(item)){
                  MagicItem magicItem = MagicItemUtils.identifyItem(item);
                  magicItem.updateItem(item);
                  //System.out.println("updated item");
               }
         
               boolean needsTick = MagicItemUtils.needsMagicTick(item);
               //System.out.println("Inspecting "+item.getName().asString()+" needs ticking: "+needsTick);
               if(needsTick){
                  TickingItem magicItem = MagicItemUtils.identifyTickingItem(item);
                  magicItem.onTick(player.getWorld(),player,item);
               }
            }
            
            if(player.isFallFlying()){ // Wings of Zephyr
               ItemStack item = player.getEquippedStack(EquipmentSlot.CHEST);
               if(MagicItemUtils.isMagic(item)){
                  if(MagicItemUtils.identifyItem(item) instanceof WingsOfZephyr){
                     WingsOfZephyr wings = (WingsOfZephyr) MagicItemUtils.identifyItem(item);
                     wings.addEnergy(item,1); // Add 1 energy for each tick of flying
                     if(wings.getEnergy(item) % 1000 == 999)
                        player.sendMessage(new LiteralText("Wing Energy Stored: "+Integer.toString(wings.getEnergy(item)+1)).formatted(Formatting.GRAY),true);
                     PLAYER_DATA.get(player).addXP(2); // Add xp
                  }
               }
            }
            
            // Check to make sure everyone is under concentration limit
            int maxConc = LevelUtils.concFromXp(arcaneProfile.getXP());
            int curConc = MagicItemUtils.getUsedConcentration(player);
            if(curConc > maxConc && server.getTicks()%80 == 0 && !player.isCreative() && !player.isSpectator()){
               player.sendMessage(new LiteralText("Your mind burns as your Arcana overwhelms you!").formatted(Formatting.RED, Formatting.ITALIC, Formatting.BOLD), true);
               Utils.playSongToPlayer(player, SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,2,.1f);
               player.damage(DamageSource.OUT_OF_WORLD, 8);
               if(player.isDead()){
                  AbstractTeam abstractTeam = player.getScoreboardTeam();
                  Formatting playerColor = abstractTeam != null && abstractTeam.getColor() != null ? abstractTeam.getColor() : Formatting.LIGHT_PURPLE;
                  String[] deathStrings = {
                        " lost concentration on their Arcana",
                        "'s mind was consumed by their Arcana",
                        "'s was crushed by the power of their Arcana",
                        "'s items consumed too much concentration",
                        " couldn't channel enough Arcana to their items"
                  };
                  final Text deathMsg = new LiteralText("")
                        .append(new LiteralText(player.getEntityName()).formatted(playerColor).formatted())
                        .append(new LiteralText(deathStrings[(int)(Math.random()*deathStrings.length)]).formatted(Formatting.LIGHT_PURPLE));
                  server.getPlayerManager().broadcast(deathMsg, MessageType.SYSTEM, Util.NIL_UUID);
               }
            }
         }
         
         // Tick Timer Callbacks
         Iterator<TickTimerCallback> itr = TIMER_CALLBACKS.iterator();
         while(itr.hasNext()){
            TickTimerCallback t = itr.next();
            if(t.decreaseTimer() == 0){
               t.onTimer();
               itr.remove();
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   private void onWorldTick(ServerWorld serverWorld){
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
                  if(id.equals(MagicItems.CONTINUUM_ANCHOR.getId())){ // Continuum Anchor Tick
                     // First check that the block is still there
                     BlockState state = serverWorld.getBlockState(pos);
                     if(state.getBlock().asItem() == MagicItems.CONTINUUM_ANCHOR.getPrefItem().getItem()){
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
                              serverWorld.playSound(null,pos.getX(),pos.getY(),pos.getZ(),SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.MASTER,1,1.5f);
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
                     }else{ // If block is no longer there deload chunks, remove it from the blocklist.
                        //System.out.print("Block Gone, Removing chunks");
                        int range = blockData.getInt("range");
                        for(int i = -range; i <= range; i++){
                           for(int j = -range; j <= range; j++){
                              serverWorld.setChunkForced(chunkPos.x+i,chunkPos.z+j,false);
                           }
                        }
                        iter.remove();
                     }
                  }else if(id.equals(MagicItems.FRACTAL_SPONGE.getId())){ // Fractal Sponge Tick
                     // Check that the block is still there
                     BlockState state = serverWorld.getBlockState(pos);
                     if(state.getBlock().asItem() != MagicItems.FRACTAL_SPONGE.getPrefItem().getItem() && state.getBlock().asItem() != Items.WET_SPONGE){
                        iter.remove();
                     }
                  }else if(id.equals(MagicItems.IGNEOUS_COLLIDER.getId())){ // Igneous Collider Tick
                     // Check that the block is still there
                     BlockState state = serverWorld.getBlockState(pos);
                     if(state.getBlock().asItem() == MagicItems.IGNEOUS_COLLIDER.getPrefItem().getItem()){
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
                                 
                                 Utils.playSound(serverWorld,pos,SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.BLOCKS, 1, .6f);
                              }
                              
                              cooldown = IgneousCollider.COOLDOWN-1;
                           }
                           blockData.putInt("cooldown",cooldown);
                        }
                     }else{
                        iter.remove();
                     }
                  }
               }
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   public static boolean addTickTimerCallback(TickTimerCallback callback){
      return TIMER_CALLBACKS.add(callback);
   }
   
   public static boolean addLoginCallback(LoginCallback callback){
      return LOGIN_CALLBACK_LIST.get(callback.getWorld()).addCallback(callback);
   }
   
   public static void log(String msg){
      if(devMode){
         System.out.println(msg);
      }
   }
}
