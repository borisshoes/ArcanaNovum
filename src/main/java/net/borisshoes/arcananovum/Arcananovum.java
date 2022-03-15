package net.borisshoes.arcananovum;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.callbacks.LoginCallback;
import net.borisshoes.arcananovum.callbacks.LoginCallbacks;
import net.borisshoes.arcananovum.callbacks.TickTimerCallback;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.borisshoes.arcananovum.gui.LoreGui;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.Utils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.screen.ScreenHandlerType;
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
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.jmx.Server;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.borisshoes.arcananovum.cardinalcomponents.LoginCallbackComponentInitializer.LOGIN_CALLBACK_LIST;
import static net.borisshoes.arcananovum.cardinalcomponents.MagicBlocksComponentInitializer.MAGIC_BLOCK_LIST;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.borisshoes.arcananovum.gui.TomeGui.getGuideBook;
import static net.minecraft.command.argument.EntityArgumentType.players;
import static net.minecraft.command.argument.EntityArgumentType.getPlayers;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Arcananovum implements ModInitializer {
   
   private static final Logger logger = LogManager.getLogger("Arcana Novum");
   private static final ArrayList<TickTimerCallback> TIMER_CALLBACKS = new ArrayList<>();
   private static final boolean devMode = true;
   
   @Override
   public void onInitialize(){
      ServerTickEvents.END_WORLD_TICK.register(this::onWorldTick);
      ServerTickEvents.END_SERVER_TICK.register(this::onTick);
      UseItemCallback.EVENT.register(this::useItem);
      UseBlockCallback.EVENT.register(this::useBlock);
      ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(this::killedEntity);
      AttackEntityCallback.EVENT.register(this::attackEntity);
      ServerPlayConnectionEvents.JOIN.register(this::onPlayerJoin);
   
      logger.info("Initializing Arcana Novum");
   
      CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
         dispatcher.register(literal("arcana")
               .then(literal("create").requires(source -> source.hasPermissionLevel(2))
                     .then(argument("id", string()).suggests(this::getItemSuggestions)
                           .executes(ctx -> createItem(ctx.getSource(), getString(ctx, "id")))))
               .then(literal("test").requires(source -> source.hasPermissionLevel(2)).executes(Arcananovum::test3))
               .then(literal("getbookdata").requires(source -> source.hasPermissionLevel(2)).executes(Arcananovum::getBookData))
               .then(literal("makerecipe").requires(source -> source.hasPermissionLevel(2)).executes(Arcananovum::makeCraftingRecipe))
               .then(literal("help").executes(Arcananovum::openGuideBook))
               .then(literal("guide").executes(Arcananovum::openGuideBook))
               .then(literal("xp").requires(source -> source.hasPermissionLevel(2))
                     .then(literal("add")
                           .then(argument("targets", players())
                                 .then(((argument("amount", integer())
                                       .executes(context -> xpCommand(context,getPlayers(context,"targets"),getInteger(context,"amount"),false,true)))
                                       .then(literal("points")
                                             .executes(context -> xpCommand(context,getPlayers(context,"targets"),getInteger(context,"amount"), false,true))))
                                       .then(literal("levels")
                                             .executes(context -> xpCommand(context,getPlayers(context,"targets"),getInteger(context,"amount"),false,false))))))
                     .then(literal("set")
                           .then(argument("targets", players()).then(((argument("amount", integer(0))
                                 .executes(context -> xpCommand(context,getPlayers(context,"targets"),getInteger(context,"amount"),true,true)))
                                 .then(literal("points")
                                       .executes(context -> xpCommand(context,getPlayers(context,"targets"),getInteger(context,"amount"),true,true))))
                                 .then(literal("levels")
                                       .executes(context -> xpCommand(context,getPlayers(context,"targets"),getInteger(context,"amount"),true,false)))))))
         );
      });
   }
   
   private static int openGuideBook(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException{
      ServerPlayerEntity player = ctx.getSource().getPlayer();
      ItemStack writablebook = new ItemStack(Items.WRITABLE_BOOK);
      writablebook.setNbt(getGuideBook());
      BookElementBuilder bookBuilder = BookElementBuilder.from(writablebook);
      LoreGui loreGui = new LoreGui(player,bookBuilder,null,-1);
      loreGui.open();
      return 1;
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
   
   private static int xpCommand(CommandContext<ServerCommandSource> ctx, Collection<? extends ServerPlayerEntity> targets, int amount, boolean set, boolean points){
      try{
         ServerCommandSource source = ctx.getSource();
         
         for (ServerPlayerEntity player : targets) {
            IArcanaProfileComponent profile = PLAYER_DATA.get(player);
            int oldValue = points ? profile.getXP() : profile.getLevel();
            int newAmount = set ? Math.max(amount, 0) : Math.max(oldValue + amount, 0);
            if(points){
               profile.setXP(newAmount);
            }else{
               newAmount = Math.max(newAmount, 1);
               profile.setXP(LevelUtils.levelToTotalXp(newAmount));
            }
         }
         
         if(targets.size() == 1 && set && points){
            source.sendFeedback(new LiteralText("Set Arcana XP to "+amount+" for ").append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() == 1 && set && !points){
            source.sendFeedback(new LiteralText("Set Arcana Level to "+amount+" for ").append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() == 1 && !set && points){
            source.sendFeedback(new LiteralText("Gave "+amount+" Arcana XP to ").append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() == 1 && !set && !points){
            source.sendFeedback(new LiteralText("Gave "+amount+" Arcana Levels to ").append(targets.iterator().next().getDisplayName()), true);
         }else if(targets.size() != 1 && set && points){
            source.sendFeedback(new LiteralText("Set Arcana XP to "+amount+" for " + targets.size() + " players"), true);
         }else if(targets.size() != 1 && set && !points){
            source.sendFeedback(new LiteralText("Set Arcana Level to "+amount+" for " + targets.size() + " players"), true);
         }else if(targets.size() != 1 && !set && points){
            source.sendFeedback(new LiteralText("Gave "+amount+" Arcana XP to " + targets.size() + " players"), true);
         }else if(targets.size() != 1 && !set && !points){
            source.sendFeedback(new LiteralText("Gave "+amount+" Arcana Levels to " + targets.size() + " players"), true);
         }
   
         return targets.size();
      }catch(Exception e){
         return 0;
      }
   }
   
   
   private static int getBookData(CommandContext<ServerCommandSource> objectCommandContext) {
      try {
         ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
         ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
         if(stack.isOf(Items.WRITTEN_BOOK)){
            NbtCompound tag = stack.getNbt();
            NbtList pages = tag.getList("pages", NbtElement.STRING_TYPE);
            //String path = "C:\\Users\\Boris\\Desktop\\bookdata.txt";
            //PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, true)));
            for(int i = 0; i < pages.size(); i++){
               //out.println("loreList.add(NbtString.of("+pages.getString(i)+"));");
               //loreList.add(NbtString.of(e));
               log("\n"+pages.getString(i));
            }
            //out.close();
         }else{
            player.sendMessage(new LiteralText("Hold a book to get data"),true);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return 0;
   }
   
   private static int makeCraftingRecipe(CommandContext<ServerCommandSource> objectCommandContext) {
      try {
         ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
         
      } catch (Exception e) {
         e.printStackTrace();
      }
      return 0;
   }
   
   private static int test3(CommandContext<ServerCommandSource> objectCommandContext) {
      try {
         ServerPlayerEntity player = objectCommandContext.getSource().getPlayer();
         ItemStack item1 = player.getStackInHand(Hand.MAIN_HAND);
         ItemStack item2 = player.getStackInHand(Hand.OFF_HAND);
         if(item1.hasNbt() && item2.hasNbt()){
            log("Testing My Thing: "+ MagicItemIngredient.validNbt(item1.getNbt(),item2.getNbt())+"\n");
         }else if(!item1.hasNbt() && item2.hasNbt()){
            log("false");
         }else if(item1.hasNbt() && !item2.hasNbt()){
            log("true");
         }else{
            log("true");
         }
         
      } catch (Exception e) {
         e.printStackTrace();
      }
      return 0;
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
                  }
               }
               result = ActionResult.SUCCESS;
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
         for(MagicBlock magicBlock : blocks){
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
                        MAGIC_BLOCK_LIST.get(serverWorld).removeBlock(magicBlock);
                     }
                     break;
                  }
               }
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   private CompletableFuture<Suggestions> getItemSuggestions(CommandContext<ServerCommandSource> serverCommandSourceCommandContext, SuggestionsBuilder builder){
      String start = builder.getRemaining().toLowerCase();
      Set<String> items = MagicItems.registry.keySet();
      items.stream().filter(s -> s.startsWith(start)).forEach(builder::suggest);
      return builder.buildFuture();
   }
   
   public static int createItem(ServerCommandSource source, String id) throws CommandSyntaxException{
      try{
         MagicItem magicItem = MagicItemUtils.getItemFromId(id);
         if(magicItem == null){
            source.getPlayer().sendMessage(new LiteralText("Invalid Magic Item ID: "+id).formatted(Formatting.RED, Formatting.ITALIC), false);
            return 0;
         }
         ItemStack item = magicItem.getNewItem();
   
         if(item == null){
            source.getPlayer().sendMessage(new LiteralText("No Preferred Item Found For: "+magicItem.getName()).formatted(Formatting.RED, Formatting.ITALIC), false);
            return 0;
         }else{
            NbtCompound magicTag = item.getNbt().getCompound("arcananovum");
            String uuid = magicTag.getString("UUID");
            source.getPlayer().sendMessage(new LiteralText("Generated New: "+magicItem.getName()+" with UUID "+uuid).formatted(Formatting.GREEN), false);
            source.getPlayer().giveItemStack(item);
            return 1;
         }
      }catch(Exception e){
         e.printStackTrace();
         return -1;
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
