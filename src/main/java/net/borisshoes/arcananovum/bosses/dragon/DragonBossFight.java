package net.borisshoes.arcananovum.bosses.dragon;

import com.google.common.collect.ImmutableList;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.bosses.BossFight;
import net.borisshoes.arcananovum.bosses.BossFights;
import net.borisshoes.arcananovum.bosses.dragon.guis.PuzzleGui;
import net.borisshoes.arcananovum.bosses.dragon.guis.TowerGui;
import net.borisshoes.arcananovum.callbacks.DragonRespawnTimerCallback;
import net.borisshoes.arcananovum.callbacks.ItemReturnTimerCallback;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.entities.DragonPhantomEntity;
import net.borisshoes.arcananovum.entities.DragonWizardEntity;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.scoreboard.*;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.EndSpikeFeature;
import net.minecraft.world.gen.feature.EndSpikeFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static net.borisshoes.arcananovum.ArcanaNovum.devPrint;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.BOSS_FIGHT;
import static net.borisshoes.arcananovum.utils.SpawnPile.makeSpawnLocations;

public class DragonBossFight {
   private static boolean prepNotif = false;
   private static boolean phase1Notif = false;
   private static boolean startTimeAnnounced = false;
   private static boolean halfHPNotif = false;
   private static boolean quarterHPNotif = false;
   private static boolean endNotif = false;
   private static int numPlayers = 0;
   private static int forcedPlayerCount = -1;
   private static EnderDragonEntity dragon = null;
   private static ServerPlayerEntity gameMaster = null;
   private static boolean keepInventoryBefore = false;
   private static List<EndCrystalEntity> crystals = null;
   private static DragonPhantomEntity[] guardianPhantoms = null;
   private static DragonWizardEntity[] wizards = null;
   private static CommandBossBar[] phantomBossBars = null;
   private static List<ServerPlayerEntity> hasDied = null;
   private static DragonAbilities dragonAbilities = null;
   private static DragonLairActions lairActions = null;
   private static int phase = 0;
   private static int age = 0;
   private static int lastGoonSpawn = 0;
   private static int lastDragonAction = 0;
   private static int lastLairAction = 0;
   private static List<ReclaimState> reclaimStates = null;
   
   public static void resetVariables(){
      prepNotif = false;
      phase1Notif = false;
      halfHPNotif = false;
      quarterHPNotif = false;
      startTimeAnnounced = false;
      endNotif = false;
      numPlayers = 0;
      forcedPlayerCount = -1;
      dragon = null;
      gameMaster = null;
      crystals = null;
      guardianPhantoms = null;
      wizards = null;
      phantomBossBars = null;
      hasDied = null;
      dragonAbilities = null;
      lairActions = null;
      phase = 0;
      age = 0;
      lastGoonSpawn = 0;
      lastDragonAction = 0;
      lastLairAction = 0;
      reclaimStates = null;
   }
   
   public static void tick(MinecraftServer server, NbtCompound fightData){
      try{
         States state = States.fromLabel(fightData.getString("State"));
         ServerPlayerEntity gm = server.getPlayerManager().getPlayer(MiscUtils.getUUID(fightData.getString("GameMaster")));
         List<MutableText> gmNotifs = new ArrayList<>();
         ServerWorld endWorld = server.getWorld(World.END);
         assert endWorld != null;
         
         //if(age%40 == 1){gmNotifs.add(Text.literal("Age: "+age));}
      
         if(state == States.WAITING_START){ // Set players and dragon name/hp, set keep inventory, make scoreboards, set crystals to be invincible
            if(!prepNotif){
               numPlayers = calcPlayers(server,false);
               fightData.putInt("numPlayers",numPlayers);
               gameMaster = gm;
               hasDied = new ArrayList<>();
               dragon = endWorld.getAliveEnderDragons().get(0);
               dragon.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(1024);
               dragon.setHealth(1024);
               reclaimStates = new ArrayList<>();
   
               MutableText dragonName = Text.literal("").formatted(Formatting.BOLD)
                     .append(Text.literal("|").formatted(Formatting.LIGHT_PURPLE,Formatting.OBFUSCATED))
                     .append(Text.literal("|").formatted(Formatting.DARK_PURPLE,Formatting.OBFUSCATED))
                     .append(Text.literal("|").formatted(Formatting.LIGHT_PURPLE,Formatting.OBFUSCATED))
                     .append(Text.literal(" "))
                     .append(Text.literal("Enderia").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD,Formatting.UNDERLINE))
                     .append(Text.literal(" - ").formatted(Formatting.DARK_PURPLE,Formatting.BOLD,Formatting.UNDERLINE))
                     .append(Text.literal("Empress").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD,Formatting.UNDERLINE,Formatting.ITALIC))
                     .append(Text.literal(" of ").formatted(Formatting.DARK_PURPLE,Formatting.BOLD,Formatting.UNDERLINE,Formatting.ITALIC))
                     .append(Text.literal("The End").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD,Formatting.UNDERLINE,Formatting.ITALIC))
                     .append(Text.literal(" "))
                     .append(Text.literal("|").formatted(Formatting.LIGHT_PURPLE,Formatting.OBFUSCATED))
                     .append(Text.literal("|").formatted(Formatting.DARK_PURPLE,Formatting.OBFUSCATED))
                     .append(Text.literal("|").formatted(Formatting.LIGHT_PURPLE,Formatting.OBFUSCATED));
   
               dragon.setCustomName(dragonName);
   
               // Turn keep inventory on
               GameRules rules = server.getGameRules();
               GameRules.BooleanRule rule = rules.get(GameRules.KEEP_INVENTORY);
               keepInventoryBefore = rule.get();
               rule.set(true,server);
   
               // Make scoreboards
               makeScoreboards(server);
               
               // Set crystals to be invulnerable
               crystals = endWorld.getEntitiesByType(EntityType.END_CRYSTAL, new Box(new BlockPos(-50,25,-50).toCenterPos(), new BlockPos(50,115,50).toCenterPos()), EndCrystalEntity::shouldShowBottom);
               for(EndCrystalEntity crystal : crystals){
                  crystal.setInvulnerable(true);
               }
               
               MutableText notif = Text.literal("")
                     .append(Text.literal("Dragon Prepped, Awaiting Announcement & Start Commands. ").formatted(Formatting.LIGHT_PURPLE))
                     .append(Text.literal(" [Announce]").styled(s ->
                           s.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/arcana boss announce 5 Minutes"))
                                 .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to announce the Fight!")))
                                 .withColor(Formatting.AQUA).withBold(true)))
                     .append(Text.literal(" [Start]").styled(s ->
                           s.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/arcana boss begin"))
                                 .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to begin the Fight!")))
                                 .withColor(Formatting.AQUA).withBold(true)));
               gmNotifs.add(notif);
               prepNotif = true;
            }
         }else if(state == States.WAITING_ONE){ //Teleport players, spawn phantoms, spawn wizards
            if(!phase1Notif){
               numPlayers = calcPlayers(server,false);
               fightData.putInt("numPlayers",numPlayers);
               DragonDialog.announce(DragonDialog.Announcements.EVENT_START,server,null);
               ArcanaNovum.addTickTimerCallback(endWorld, new GenericTimer(100, () -> {
                     List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
                     for(ServerPlayerEntity player : players){
                        if(!player.isCreative() && !player.isSpectator()){
                           player.teleportTo(new TeleportTarget(endWorld, new Vec3d(100.5+(Math.random()*3-1.5),51,0.5+(Math.random()*3-1.5)), Vec3d.ZERO, 90, 0, TeleportTarget.ADD_PORTAL_CHUNK_TICKET));
                        }
                     }
   
                     guardianPhantoms = new DragonPhantomEntity[4];
                     phantomBossBars = new CommandBossBar[4];
                     NbtList phantomData = new NbtList();
                     for(int i=0;i<guardianPhantoms.length;i++){
                        guardianPhantoms[i] = DragonGoonHelper.makeGuardianPhantom(endWorld,numPlayers);
                        phantomBossBars[i] = endWorld.getServer().getBossBarManager().add(Identifier.of("guardianphantom-"+guardianPhantoms[i].getUuidAsString()),guardianPhantoms[i].getCustomName());
                        phantomBossBars[i].setColor(BossBar.Color.PURPLE);
                        guardianPhantoms[i].addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,100,4));
                        endWorld.spawnEntityAndPassengers(guardianPhantoms[i]);
                        phantomData.add(NbtString.of(guardianPhantoms[i].getUuidAsString()));
                     }
   
                     // Spawn wizards
                     wizards = new DragonWizardEntity[crystals.size()];
                     NbtList wizardData = new NbtList();
                     for(int i = 0; i < wizards.length; i++){
                        EndCrystalEntity crystal = crystals.get(i);
                        wizards[i] = DragonGoonHelper.makeWizard(endWorld,numPlayers);
                        wizards[i].setPosition(crystal.getPos().add(0,2,0));
                        wizards[i].setInvulnerable(true);
                        wizards[i].setCrystalId(crystal.getUuid());
                        wizards[i].addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,100,4));
                        endWorld.spawnEntityAndPassengers(wizards[i]);
                        wizardData.add(NbtString.of(wizards[i].getUuidAsString()));
                     }
   
                     fightData.put("phantoms",phantomData);
                     fightData.put("wizards",wizardData);
                     States.updateState(States.PHASE_ONE,server);
                     DragonDialog.announce(DragonDialog.Announcements.PHASE_ONE_START,server,null);
                     phase = 1;
                  }
               ));
               dragonAbilities = new DragonAbilities(server,endWorld,dragon,crystals);
               lairActions = new DragonLairActions(server,endWorld,dragon);
               phase1Notif = true;
            }
         }else if(state == States.PHASE_ONE){ // Tick guardian check, dragon invincibility
            List<ServerPlayerEntity> nearbyPlayers300 = endWorld.getPlayers(p -> p.squaredDistanceTo(new Vec3d(0,100,0)) <= 300*300);
            List<EndermanEntity> endermen = endWorld.getEntitiesByType(EntityType.ENDERMAN, new Box(new BlockPos(-100,25,-100).toCenterPos(), new BlockPos(100,115,100).toCenterPos()), e -> true);
   
            for(EndermanEntity enderman : endermen){ // Make endermen not attack Endermites
               if(enderman.getTarget() instanceof EndermiteEntity || endWorld.getEntity(enderman.getAngryAt()) instanceof EndermiteEntity){
                  enderman.setTarget(null);
                  enderman.setAngryAt(null);
               }
            }
            
            int aliveCount = 0;
            if(guardianPhantoms != null){
               for(int i = 0; i < guardianPhantoms.length; i++){
                  if(guardianPhantoms[i] != null){
                     if(guardianPhantoms[i].isAlive()){
                        aliveCount++;
                        float percent = guardianPhantoms[i].getHealth() / guardianPhantoms[i].getMaxHealth();
                        phantomBossBars[i].setPercent(percent);
                        for(ServerPlayerEntity player : nearbyPlayers300){
                           phantomBossBars[i].addPlayer(player);
                        }
                     }else{
                        phantomBossBars[i].clearPlayers();
                        server.getBossBarManager().remove(phantomBossBars[i]);
                        guardianPhantoms[i].discard();
                        guardianPhantoms[i] = null;
                        DragonDialog.announce(DragonDialog.Announcements.PHANTOM_DEATH,server,null);
                     }
                  }
               }
            }
            dragon.heal(50+25*numPlayers);
            
            if(aliveCount == 0){ // Progress to phase 2
               gmNotifs.add(Text.literal("All Phantoms Dead, progressing to Phase 2"));
               States.updateState(States.WAITING_TWO,server);
            }
         }else if(state == States.WAITING_TWO){ // Set wizards to be mortal
            numPlayers = Math.max(1,calcPlayers(server,false));
            fightData.putInt("numPlayers",numPlayers);
            for(DragonWizardEntity wizard : wizards){
               wizard.setInvulnerable(false);
            }
            
            DragonDialog.announce(DragonDialog.Announcements.PHASE_TWO_START,server,null);
            States.updateState(States.PHASE_TWO,server);
            phase = 2;
         }else if(state == States.PHASE_TWO){ // Tick wizards
            List<ServerPlayerEntity> nearbyPlayers300 = endWorld.getPlayers(p -> p.squaredDistanceTo(new Vec3d(0,100,0)) <= 300*300);
            
            if(wizards != null){
               for(int i = 0; i < wizards.length; i++){
                  if(wizards[i] != null){
                     if(wizards[i].isAlive()){
                        if(age % 130 == 0){
                           if(wizards[i].getCrystalId() != null){
                              Entity entity = endWorld.getEntity(wizards[i].getCrystalId());
                              if(entity instanceof EndCrystalEntity crystal){
                                 Vec3d crystalPos = crystal.getPos().add(0,-1,0);
                                 ParticleEffectUtils.dragonBossTowerCircleInvuln(endWorld,crystalPos,6000,0);
                              }
                           }
                        }
                     }else{
                        if(wizards[i].getCrystalId() != null){
                           Entity entity = endWorld.getEntity(wizards[i].getCrystalId());
                           if(entity instanceof EndCrystalEntity crystal){
                              crystal.setInvulnerable(false);
                           }
                        }
                        final int finalI = i;
                        List<ServerPlayerEntity> nearbyPlayers = endWorld.getPlayers(p -> p.squaredDistanceTo(wizards[finalI].getPos()) <= 20*20);
                        for(ServerPlayerEntity player : nearbyPlayers){
                           player.sendMessage(Text.literal("The Crystal's Shield Fades!").formatted(Formatting.AQUA,Formatting.ITALIC),true);
                        }
                        
                        wizards[i].discard();
                        wizards[i] = null;
                     }
                  }
               }
            }
            int aliveCount = 0;
            boolean crystalDestroyed = false;
            for(int i = 0; i < crystals.size(); i++){
               EndCrystalEntity crystal = crystals.get(i);
               if(crystal != null){
                  if(crystal.isAlive()){
                     aliveCount++;
                  }else{
                     ReclaimState newRecState = new ReclaimState(crystal.getPos(),endWorld);
                     newRecState.hologramVisible = false;
                     reclaimStates.add(newRecState);
                     crystals.set(i,null);
                     crystalDestroyed = true;
                  }
               }
            }
            
            if(crystalDestroyed){
               if(aliveCount == 9){
                  DragonDialog.announce(DragonDialog.Announcements.FIRST_CRYSTAL_DESTROYED,server,null);
               }else if(aliveCount == 5){
                  DragonDialog.announce(DragonDialog.Announcements.HALF_CRYSTALS_DESTROYED,server,null);
               }else{
                  DragonDialog.announce(DragonDialog.Announcements.CRYSTAL_DESTROYED,server,null);
               }
            }
            
            dragon.heal(50+25*numPlayers);
            if(aliveCount == 0){ // Progress to phase 3
               gmNotifs.add(Text.literal("All Crystals Dead, progressing to Phase 3"));
               States.updateState(States.WAITING_THREE,server);
            }
         }else if(state == States.WAITING_THREE){ // Set up crystal reclamation
            numPlayers = Math.max(1,calcPlayers(server,false));
            fightData.putInt("numPlayers",numPlayers);
            for(ReclaimState reclaimState : reclaimStates){
               reclaimState.hologramVisible = true;
            }
            
            DragonDialog.announce(DragonDialog.Announcements.PHASE_THREE_START,server,null);
            States.updateState(States.PHASE_THREE,server);
            phase = 3;
         }else if(state == States.PHASE_THREE){ // Dragon HP Updates, Endermen buff and aggro
            List<EndermanEntity> endermen = endWorld.getEntitiesByType(EntityType.ENDERMAN, new Box(new BlockPos(-100,25,-100).toCenterPos(), new BlockPos(100,115,100).toCenterPos()), e -> true);
            float dragonHP = dragon.getHealth();
            float dragonMax = dragon.getMaxHealth();
   
            for(EndermanEntity enderman : endermen){
               if(dragonHP/dragonMax <= 0.5){
                  int amp = dragonHP/dragonMax <= 0.25 ? 1 : 0;
                  StatusEffectInstance strength = new StatusEffectInstance(StatusEffects.STRENGTH,120,amp,false,false,false);
                  enderman.addStatusEffect(strength);
                  StatusEffectInstance resist = new StatusEffectInstance(StatusEffects.RESISTANCE,120,amp,false,false,false);
                  enderman.addStatusEffect(resist);
               }
               if(enderman.isAngry()){
                  PlayerEntity closestPlayer = endWorld.getClosestPlayer(enderman,4);
                  if(closestPlayer != null){
                     StatusEffectInstance slow = new StatusEffectInstance(StatusEffects.SLOWNESS,60,2,false,false,false);
                     closestPlayer.addStatusEffect(slow);
                  }
               }
            }
            if(age % 300 == 0){
               for(EndermanEntity enderman : endermen){
                  PlayerEntity closestPlayer = endWorld.getClosestPlayer(enderman,30);
                  if(closestPlayer != null){
                     enderman.setProvoked();
                     enderman.setTarget(closestPlayer);
                     enderman.setAngryAt(closestPlayer.getUuid());
                     enderman.setAngerTime(1200);
                  }else{
                     PlayerEntity randomPlayer = endWorld.getRandomAlivePlayer();
                     if(randomPlayer != null && Math.random() < .1*numPlayers){
                        enderman.setProvoked();
                        enderman.setTarget(randomPlayer);
                        enderman.setAngryAt(randomPlayer.getUuid());
                        enderman.setAngerTime(1200);
                     }
                  }
               }
            }
            for(ReclaimState reclaimState : reclaimStates){
               reclaimState.tick();
            }
            
            if(!halfHPNotif && dragonHP/dragonMax <= 0.5){
               DragonDialog.announce(DragonDialog.Announcements.DRAGON_HALF_HP,server,null);
               halfHPNotif = true;
            }
            if(!quarterHPNotif && dragonHP/dragonMax <= 0.25){
               DragonDialog.announce(DragonDialog.Announcements.DRAGON_QUARTER_HP,server,null);
               quarterHPNotif = true;
            }
            
            if(dragonHP <= 0 || !dragon.isAlive()){
               DragonDialog.announce(DragonDialog.Announcements.DRAGON_DEATH,server,null);
               gmNotifs.add(Text.literal("Dragon Dead, Concluding Fight"));
               States.updateState(States.WAITING_DEATH,server);
            }
         }else if(state == States.WAITING_DEATH){
            if(!endNotif){
               for(DragonBossFight.ReclaimState reclaimState : reclaimStates){
                  reclaimState.fightEnd();
               }
               ArcanaNovum.addTickTimerCallback(endWorld, new GenericTimer(100, () -> endFight(server,endWorld)));
               endNotif = true;
            }
         }
         
         if(fightData.getInt("numPlayers") != numPlayers){
            fightData.putInt("numPlayers",numPlayers);
         }
   
         // Handle lair actions, dragon actions, goon spawning, crystal effects
         if(state == States.PHASE_ONE || state == States.PHASE_TWO || state == States.PHASE_THREE){
            crystals.forEach(c -> tickCrystal(endWorld,c,phase));
            
            if(lastGoonSpawn > 20*45) spawnGoons(endWorld,phase);
            if(lastLairAction > 20*35 && lairActions.startAction(phase)) lastLairAction = 0;
            if(lastDragonAction > 20*25 && dragonAbilities.doAbility(phase)) lastDragonAction = 0;
         }
      
         if(gm != null) gmNotifs.forEach(gm::sendMessage);
         if(state != States.WAITING_RESPAWN && state != States.WAITING_START && state != States.WAITING_ONE){
            if(age % 3000 == 0){
               List<ShulkerBulletEntity> bullets = endWorld.getEntitiesByType(EntityType.SHULKER_BULLET, new Box(new BlockPos(-400,0,-400).toCenterPos(), new BlockPos(400,256,400).toCenterPos()), e -> true);
               for(ShulkerBulletEntity bullet : bullets){
                  bullet.kill(endWorld);
               }
            }
            
            age++;
            lastGoonSpawn++;
            lastLairAction++;
            lastDragonAction++;
            if(dragonAbilities != null) dragonAbilities.tick();
            if(lairActions != null) lairActions.tick();
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   private static void spawnGoons(ServerWorld endWorld, int phase){
      if(phase == 1){ // Endermite Goons
         // Count existing goons
         List<EndermiteEntity> curGoons = endWorld.getEntitiesByType(EntityType.ENDERMITE, new Box(new BlockPos(-300,25,-300).toCenterPos(), new BlockPos(300,255,300).toCenterPos()), e -> true);
         if(curGoons.size() > 35) return;
         double chance = curGoons.size() < 5 ? 0.005 : 0.002;
         if(Math.random() > chance) return; // Average 25+minTime seconds before goon spawn
   
         EndermiteEntity[] goons = new EndermiteEntity[MathHelper.clamp((int)(Math.random()*3*numPlayers+2+numPlayers),10,35)];
         ArrayList<BlockPos> poses = makeSpawnLocations(goons.length,50,endWorld);
         float endermiteHP = MathHelper.clamp(10 + 3*numPlayers,10,40);
         for(int i=0;i<goons.length;i++){
            goons[i] = new EndermiteEntity(EntityType.ENDERMITE, endWorld);
            goons[i].getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(endermiteHP);
            goons[i].setHealth(endermiteHP);
            goons[i].setPersistent();
            goons[i].getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(8f);
            BlockPos pos = poses.get(i);
            goons[i].setPos(pos.getX(),pos.getY()+1,pos.getZ());
      
            endWorld.spawnEntityAndPassengers(goons[i]);
         }
         
         DragonDialog.announce(DragonDialog.Announcements.PHASE_ONE_GOONS,endWorld.getServer(),null);
      }else if(phase == 2){ // Shulker Goons
         // Count existing goons
         List<ShulkerEntity> curGoons = endWorld.getEntitiesByType(EntityType.SHULKER, new Box(new BlockPos(-300,25,-300).toCenterPos(), new BlockPos(300,255,300).toCenterPos()), e -> true);
         if(curGoons.size() > 35) return;
         double chance = curGoons.size() < 5 ? 0.005 : 0.002;
         if(Math.random() > chance) return; // Average 25+minTime seconds before goon spawn
   
         ShulkerEntity[] goons = new ShulkerEntity[MathHelper.clamp((int)(Math.random()*2*numPlayers+2+numPlayers),10,35)];
         ArrayList<BlockPos> poses = makeSpawnLocations(goons.length,50,endWorld);
         float shulkerHP = MathHelper.clamp(20 + 4*numPlayers,20,80);
         for(int i=0;i<goons.length;i++){
            goons[i] = new ShulkerEntity(EntityType.SHULKER, endWorld);
            goons[i].getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(shulkerHP);
            goons[i].setHealth(shulkerHP);
            goons[i].setPersistent();
            BlockPos pos = poses.get(i);
            goons[i].setPos(pos.getX(),pos.getY(),pos.getZ());
      
            endWorld.spawnEntityAndPassengers(goons[i]);
         }
   
         DragonDialog.announce(DragonDialog.Announcements.PHASE_TWO_GOONS,endWorld.getServer(),null);
      }else if(phase == 3){ // Enderman Goons
         // Count existing goons
         List<EndermanEntity> curGoons = endWorld.getEntitiesByType(EntityType.ENDERMAN, new Box(new BlockPos(-300,25,-300).toCenterPos(), new BlockPos(300,255,300).toCenterPos()), e -> true);
         if(curGoons.size() > 50) return;
         double chance = curGoons.size() < 5 ? 0.005 : 0.002;
         if(Math.random() > chance) return; // Average 25+minTime seconds before goon spawn
   
         EndermanEntity[] goons = new EndermanEntity[MathHelper.clamp((int)(Math.random()*3*numPlayers+2+numPlayers*2),20,50)];
         ArrayList<BlockPos> poses = makeSpawnLocations(goons.length,50,endWorld);
         float endermanHP = MathHelper.clamp(20 + 4*numPlayers,20,80);
         for(int i=0;i<goons.length;i++){
            goons[i] = new EndermanEntity(EntityType.ENDERMAN, endWorld);
            goons[i].getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(endermanHP);
            goons[i].setHealth(endermanHP);
            goons[i].getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(8f);
            BlockPos pos = poses.get(i);
            goons[i].setPos(pos.getX(),pos.getY(),pos.getZ());
      
            endWorld.spawnEntityAndPassengers(goons[i]);
         }
         
         DragonDialog.announce(DragonDialog.Announcements.PHASE_THREE_GOONS,endWorld.getServer(),null);
      }
      lastGoonSpawn = 0;
   }
   
   private static void endFight(MinecraftServer server, ServerWorld endWorld){
      try{
         DragonDialog.announce(DragonDialog.Announcements.EVENT_END,server,null);
         ArcanaItem arcanaWings = ArcanaRegistry.WINGS_OF_ENDERIA;
         
         // Give reward
         List<ServerPlayerEntity> players = endWorld.getServer().getPlayerManager().getPlayerList();
         for(ServerPlayerEntity player : players){
            ItemStack wings = arcanaWings.addCrafter(arcanaWings.getNewItem(),player.getUuidAsString(),false,player.getServer());
            wings.addEnchantment(MiscUtils.getEnchantment(ArcanaRegistry.FATE_ANCHOR),1);
            ArcanaNovum.data(player).addCraftedSilent(wings);
            ArcanaNovum.addTickTimerCallback(new ItemReturnTimerCallback(wings,player));
            ArcanaNovum.addTickTimerCallback(new ItemReturnTimerCallback(new ItemStack(Items.DRAGON_EGG,1),player));
            
            player.addExperience(5500);
         }
      
         List<ScoreboardEntry> scores = getLeaderboard(server);
         ArrayList<MutableText> message = new ArrayList<>();
         // dmg dealt, taken, deaths, kills
      
         message.add(Text.literal(""));
         message.add(Text.literal("")
               .append(Text.literal("-----------------------------------").formatted(Formatting.DARK_AQUA,Formatting.BOLD)));
         message.add(Text.literal(""));
         message.add(Text.literal("")
               .append(Text.literal("                      ").formatted(Formatting.GREEN))
               .append(Text.literal("Leaderboard").formatted(Formatting.GREEN,Formatting.UNDERLINE)));
         message.add(Text.literal(""));
         message.add(Text.literal("")
               .append(Text.literal("  <> ").formatted(Formatting.AQUA,Formatting.BOLD))
               .append(Text.literal("Most Bloodthirsty").formatted(Formatting.DARK_RED,Formatting.BOLD,Formatting.UNDERLINE))
               .append(Text.literal(": ").formatted(Formatting.AQUA))
               .append(Text.literal(scores.get(0).owner()).formatted(Formatting.GOLD,Formatting.UNDERLINE,Formatting.BOLD))
               .append(Text.literal(" with ").formatted(Formatting.AQUA,Formatting.ITALIC))
               .append(Text.literal(Integer.toString(scores.get(0).value())).formatted(Formatting.GOLD,Formatting.ITALIC))
               .append(Text.literal(" damage points dealt!").formatted(Formatting.AQUA,Formatting.ITALIC)));
         message.add(Text.literal(""));
         message.add(Text.literal("")
               .append(Text.literal("  <> ").formatted(Formatting.AQUA,Formatting.BOLD))
               .append(Text.literal("Tankiest Fighter").formatted(Formatting.BLUE,Formatting.BOLD,Formatting.UNDERLINE))
               .append(Text.literal(": ").formatted(Formatting.AQUA))
               .append(Text.literal(scores.get(1).owner()).formatted(Formatting.GOLD,Formatting.UNDERLINE,Formatting.BOLD))
               .append(Text.literal(" with ").formatted(Formatting.AQUA,Formatting.ITALIC))
               .append(Text.literal(Integer.toString(scores.get(1).value())).formatted(Formatting.GOLD,Formatting.ITALIC))
               .append(Text.literal(" damage points taken!").formatted(Formatting.AQUA,Formatting.ITALIC)));
         message.add(Text.literal(""));
         message.add(Text.literal("")
               .append(Text.literal("  <> ").formatted(Formatting.AQUA,Formatting.BOLD))
               .append(Text.literal("Most Death Prone").formatted(Formatting.DARK_GRAY,Formatting.BOLD,Formatting.UNDERLINE))
               .append(Text.literal(": ").formatted(Formatting.AQUA))
               .append(Text.literal(scores.get(2).owner()).formatted(Formatting.GOLD,Formatting.UNDERLINE,Formatting.BOLD))
               .append(Text.literal(" with ").formatted(Formatting.AQUA,Formatting.ITALIC))
               .append(Text.literal(Integer.toString(scores.get(2).value())).formatted(Formatting.GOLD,Formatting.ITALIC))
               .append(Text.literal(" deaths!").formatted(Formatting.AQUA,Formatting.ITALIC)));
         message.add(Text.literal(""));
         message.add(Text.literal("")
               .append(Text.literal("  <> ").formatted(Formatting.AQUA,Formatting.BOLD))
               .append(Text.literal("Goon Slayer").formatted(Formatting.RED,Formatting.BOLD,Formatting.UNDERLINE))
               .append(Text.literal(": ").formatted(Formatting.AQUA))
               .append(Text.literal(scores.get(3).owner()).formatted(Formatting.GOLD,Formatting.UNDERLINE,Formatting.BOLD))
               .append(Text.literal(" with ").formatted(Formatting.AQUA,Formatting.ITALIC))
               .append(Text.literal(Integer.toString(scores.get(3).value())).formatted(Formatting.GOLD,Formatting.ITALIC))
               .append(Text.literal(" mob kills!").formatted(Formatting.AQUA,Formatting.ITALIC)));
         message.add(Text.literal(""));
         message.add(Text.literal("")
               .append(Text.literal("-----------------------------------").formatted(Formatting.DARK_AQUA,Formatting.BOLD)));
      
         ArcanaNovum.addTickTimerCallback(endWorld, new GenericTimer(400, () -> {
            for(MutableText msg : message){
               endWorld.getServer().getPlayerManager().broadcast(msg, false);
            }
         }));
      
         GameRules rules = server.getGameRules();
         GameRules.BooleanRule rule = rules.get(GameRules.KEEP_INVENTORY);
         rule.set(keepInventoryBefore,server);
      
         if(gameMaster != null){
            gameMaster.sendMessage(Text.literal("Fight Ended, Thanks For Playing!"));
         }
         resetVariables();
         removeScoreboards(server);
         BossFight.cleanBoss(server);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   private static void tickCrystal(ServerWorld endWorld, EndCrystalEntity crystal, int phase){
      if(crystal == null) return;
      Vec3d pos = crystal.getPos().add(0,-1,0);
      if(phase == 1){ // Knockback shield, Both particle effects
         if(age % 260 == 0){
            
            ParticleEffectUtils.dragonBossTowerCirclePush(endWorld,pos,12000,0);
         }
   
         //Actual Knockback
         List<ServerPlayerEntity> inRangePlayers = endWorld.getPlayers(p -> p.squaredDistanceTo(new Vec3d(pos.getX()+.5,pos.getY()-1,pos.getZ()+.5)) <= 5.5*5.5);
         for(ServerPlayerEntity player : inRangePlayers){
            BlockPos target = BlockPos.ofFloored(pos.getX()+.5,pos.getY()-1,pos.getZ()+.5);
            BlockPos playerPos = player.getBlockPos();
            Vec3d vec = new Vec3d(target.getX()-playerPos.getX(),0,target.getZ()-playerPos.getZ());
            vec = vec.normalize().multiply(3);
      
            player.setVelocity(-vec.x,1,-vec.z);
            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
      
            player.sendMessage(Text.literal("The Crystal Tower is Shielded!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC),true);
         }
      }
   }
   
   private static void makeScoreboards(MinecraftServer server){
      // Damage Dealt, Damage Taken, Deaths, Mob Kills
      try{
         ServerScoreboard scoreboard = server.getScoreboard();
         ScoreboardCriterion dmgDealt = ScoreboardCriterion.getOrCreateStatCriterion("minecraft.custom:minecraft.damage_dealt").orElseThrow();
         ScoreboardCriterion dmgTaken = ScoreboardCriterion.getOrCreateStatCriterion("minecraft.custom:minecraft.damage_taken").orElseThrow();
         ScoreboardCriterion deaths = ScoreboardCriterion.DEATH_COUNT;
         ScoreboardCriterion mobKills = ScoreboardCriterion.getOrCreateStatCriterion("minecraft.custom:minecraft.mob_kills").orElseThrow();
         
         scoreboard.addObjective("arcananovum_boss_dmg_dealt", dmgDealt, Text.literal("Event Damage Dealt"), dmgDealt.getDefaultRenderType(),false,null);
         scoreboard.addObjective("arcananovum_boss_dmg_taken", dmgTaken, Text.literal("Event Damage Taken"), dmgTaken.getDefaultRenderType(),false,null);
         scoreboard.addObjective("arcananovum_boss_deaths", deaths, Text.literal("Event Deaths"), deaths.getDefaultRenderType(),false,null);
         scoreboard.addObjective("arcananovum_boss_mob_kills", mobKills, Text.literal("Event Mob Kills"), mobKills.getDefaultRenderType(),false,null);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   private static void removeScoreboards(MinecraftServer server){
      // Damage Dealt, Damage Taken, Deaths, Mob Kills
      try{
         ServerScoreboard scoreboard = server.getScoreboard();
         ScoreboardObjective dmgDealt = scoreboard.getNullableObjective("arcananovum_boss_dmg_dealt");
         ScoreboardObjective dmgTaken = scoreboard.getNullableObjective("arcananovum_boss_dmg_taken");
         ScoreboardObjective deaths = scoreboard.getNullableObjective("arcananovum_boss_deaths");
         ScoreboardObjective mobKills = scoreboard.getNullableObjective("arcananovum_boss_mob_kills");
         
         
         scoreboard.removeObjective(dmgDealt);
         scoreboard.removeObjective(dmgTaken);
         scoreboard.removeObjective(deaths);
         scoreboard.removeObjective(mobKills);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   private static List<ScoreboardEntry> getLeaderboard(MinecraftServer server){
      ArrayList<ScoreboardEntry> list = new ArrayList<>();
      try{
         ServerScoreboard scoreboard = server.getScoreboard();
         ScoreboardObjective dmgDealt = scoreboard.getNullableObjective("arcananovum_boss_dmg_dealt");
         ScoreboardObjective dmgTaken = scoreboard.getNullableObjective("arcananovum_boss_dmg_taken");
         ScoreboardObjective deaths = scoreboard.getNullableObjective("arcananovum_boss_deaths");
         ScoreboardObjective mobKills = scoreboard.getNullableObjective("arcananovum_boss_mob_kills");
         
         List<ScoreboardEntry> dmgDealtScores = new ArrayList<>(scoreboard.getScoreboardEntries(dmgDealt));
         List<ScoreboardEntry> dmgTakenScores = new ArrayList<>(scoreboard.getScoreboardEntries(dmgTaken));
         List<ScoreboardEntry> deathsScores = new ArrayList<>(scoreboard.getScoreboardEntries(deaths));
         List<ScoreboardEntry> mobKillsScores = new ArrayList<>(scoreboard.getScoreboardEntries(mobKills));
         
         Comparator<ScoreboardEntry> scoreComparator = Comparator.comparingInt(ScoreboardEntry::value);
         dmgDealtScores.sort(scoreComparator);
         dmgTakenScores.sort(scoreComparator);
         deathsScores.sort(scoreComparator);
         mobKillsScores.sort(scoreComparator);
         
         if(dmgDealtScores.isEmpty()){
            ScoreboardEntry dummyScore = new ScoreboardEntry("-",0,Text.literal("-"),null);
            list.add(dummyScore);
         }else{
            list.add(dmgDealtScores.get(dmgDealtScores.size()-1));
         }
         
         if(dmgTakenScores.isEmpty()){
            ScoreboardEntry dummyScore = new ScoreboardEntry("-",0,Text.literal("-"),null);
            list.add(dummyScore);
         }else{
            list.add(dmgTakenScores.get(dmgTakenScores.size()-1));
         }
         
         if(deathsScores.isEmpty()){
            ScoreboardEntry dummyScore = new ScoreboardEntry("-",0,Text.literal("-"),null);
            list.add(dummyScore);
         }else{
            list.add(deathsScores.get(deathsScores.size()-1));
         }
         
         if(mobKillsScores.isEmpty()){
            ScoreboardEntry dummyScore = new ScoreboardEntry("-",0,Text.literal("-"),null);
            list.add(dummyScore);
         }else{
            list.add(mobKillsScores.get(mobKillsScores.size()-1));
         }
         
      }catch(Exception e){
         e.printStackTrace();
      }
      return list;
   }
   
   private static void resetTowers(ServerWorld endWorld){
      List<EndSpikeFeature.Spike> list = EndSpikeFeature.getSpikes(endWorld);
      for(EndSpikeFeature.Spike spike : list){
         List<EndCrystalEntity> nearCrystals = endWorld.getEntitiesByType(EntityType.END_CRYSTAL, new Box(BlockPos.ofFloored(spike.getCenterX()-10,0,spike.getCenterZ()-10).toCenterPos(), BlockPos.ofFloored(spike.getCenterX()+10,255,spike.getCenterZ()+10).toCenterPos()), EndCrystalEntity::shouldShowBottom);
         for(EndCrystalEntity nearCrystal : nearCrystals){
            nearCrystal.kill(endWorld);
         }
         
         Iterator<BlockPos> var16 = BlockPos.iterate(BlockPos.ofFloored(spike.getCenterX() - 10, spike.getHeight() - 10, spike.getCenterZ() - 10), BlockPos.ofFloored(spike.getCenterX() + 10, spike.getHeight() + 10, spike.getCenterZ() + 10)).iterator();
   
         while(var16.hasNext()){
            BlockPos blockPos = var16.next();
            endWorld.removeBlock(blockPos, false);
         }
         

         endWorld.createExplosion((Entity)null, (double)((float)spike.getCenterX() + 0.5F), (double)spike.getHeight(), (double)((float)spike.getCenterZ() + 0.5F), 5.0F, World.ExplosionSourceType.NONE);
         EndSpikeFeatureConfig endSpikeFeatureConfig = new EndSpikeFeatureConfig(false, ImmutableList.of(spike), (BlockPos)null);
         Feature.END_SPIKE.generateIfValid(endSpikeFeatureConfig, endWorld, endWorld.getChunkManager().getChunkGenerator(), Random.create(), BlockPos.ofFloored(spike.getCenterX(), 45, spike.getCenterZ()));
      }
   }
   
   public static int prepBoss(ServerPlayerEntity player){
      if(player.getEntityWorld().getRegistryKey() != World.END){
         player.sendMessage(Text.literal("The Dragon boss must take place in The End"), false);
         return -1;
      }
      ServerWorld endWorld = player.getServerWorld();
      NbtCompound dragonData = (NbtCompound) (EnderDragonFight.Data.CODEC.encodeStart(NbtOps.INSTANCE, endWorld.getEnderDragonFight().toData()).getOrThrow());
      NbtCompound fightData = new NbtCompound();
      if(dragonData.getBoolean("DragonKilled")){
         player.sendMessage(Text.literal("Dragon is Dead, Commencing Respawn"), false);
         int[] exitList = dragonData.getIntArray("ExitPortalLocation");
         BlockPos portalPos = new BlockPos(exitList[0],exitList[1],exitList[2]);
         BlockPos blockPos2 = portalPos.up(1);
         Iterator<Direction> var4 = net.minecraft.util.math.Direction.Type.HORIZONTAL.iterator();
         while(var4.hasNext()){
            Direction direction = var4.next();
            EndCrystalEntity crystal = new EndCrystalEntity(EntityType.END_CRYSTAL,endWorld);
            crystal.setPosition(Vec3d.ofBottomCenter(blockPos2.offset(direction, 3)));
            endWorld.spawnEntityAndPassengers(crystal);
         }
         endWorld.getEnderDragonFight().respawnDragon();
         fightData.putString("State", DragonBossFight.States.WAITING_RESPAWN.name());
         ArcanaNovum.addTickTimerCallback(new DragonRespawnTimerCallback(player.getServer()));
      }else{
         player.sendMessage(Text.literal("Co-opting Dragon"), false);
         if(endWorld.getEntitiesByType(EntityType.END_CRYSTAL, new Box(new BlockPos(-50,25,-50).toCenterPos(), new BlockPos(50,115,50).toCenterPos()), EndCrystalEntity::shouldShowBottom).size() != 10){
            player.sendMessage(Text.literal("Tower Anomaly Detected, Resetting").formatted(Formatting.RED,Formatting.ITALIC), false);
            resetTowers(endWorld);
         }
         fightData.putString("State", DragonBossFight.States.WAITING_START.name());
      }
      fightData.putString("GameMaster", player.getUuidAsString());
      fightData.putInt("numPlayers",0);
      player.sendMessage(Text.literal(""), false);
      player.sendMessage(Text.literal("You are now the Game Master, please stay in The End for the duration of the fight. All errors and updates will be sent to you.").formatted(Formatting.AQUA), false);
      player.sendMessage(Text.literal(""), false);
      BOSS_FIGHT.get(endWorld).setBossFight(BossFights.DRAGON,fightData);
      return 0;
   }
   
   public static int abortBoss(MinecraftServer server){
      Pair<BossFights, NbtCompound> bossFight = BOSS_FIGHT.get(server.getWorld(World.END)).getBossFight();
      NbtCompound data = bossFight.getRight();
      States state = States.valueOf(data.getString("State"));
      ServerPlayerEntity gm = server.getPlayerManager().getPlayer(MiscUtils.getUUID(data.getString("GameMaster")));
      ServerWorld endWorld = server.getWorld(World.END);
      if(gm != null){
         gm.sendMessage(Text.literal("Boss Has Been Aborted :(").formatted(Formatting.RED,Formatting.ITALIC));
      }
   
      for(EnderDragonEntity dragon : endWorld.getAliveEnderDragons()){
         dragon.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(200);
         dragon.setHealth(200);
         dragon.setCustomName(Text.literal("Ender Dragon"));
      }
      
      if(reclaimStates != null){
         for(ReclaimState reclaimState : reclaimStates){
            reclaimState.hologramVisible = false;
         }
      }
      
      if(guardianPhantoms != null && phase == 1){
         for(PhantomEntity phantom : guardianPhantoms){
            if(phantom != null && phantom.isAlive())
               phantom.kill(endWorld);
         }
         for(CommandBossBar bossBar : phantomBossBars){
            if(bossBar != null){
               bossBar.clearPlayers();
               server.getBossBarManager().remove(bossBar);
            }
         }
      }
   
      if(wizards != null && (phase == 1 || phase == 2)){
         for(DragonWizardEntity wizard : wizards){
            if(wizard != null && wizard.isAlive())
               wizard.kill(endWorld);
         }
      }
   
      for(EndCrystalEntity crystal : crystals){
         if(crystal != null){
            crystal.setInvulnerable(false);
         }
      }
   
      List<EndermiteEntity> mites = endWorld.getEntitiesByType(EntityType.ENDERMITE, new Box(new BlockPos(-300,25,-300).toCenterPos(), new BlockPos(300,255,300).toCenterPos()), e -> true);
      List<ShulkerEntity> shulkers = endWorld.getEntitiesByType(EntityType.SHULKER, new Box(new BlockPos(-300,25,-300).toCenterPos(), new BlockPos(300,255,300).toCenterPos()), e -> true);
      List<SkeletonEntity> skeletons = endWorld.getEntitiesByType(EntityType.SKELETON, new Box(new BlockPos(-300,25,-300).toCenterPos(), new BlockPos(300,255,300).toCenterPos()), e -> true);
      List<PhantomEntity> phantoms = endWorld.getEntitiesByType(EntityType.PHANTOM, new Box(new BlockPos(-300,25,-300).toCenterPos(), new BlockPos(300,255,300).toCenterPos()), e -> true);
      List<IllusionerEntity> illusioners = endWorld.getEntitiesByType(EntityType.ILLUSIONER, new Box(new BlockPos(-300,25,-300).toCenterPos(), new BlockPos(300,255,300).toCenterPos()), e -> true);
      mites.forEach(e -> e.kill(endWorld));
      shulkers.forEach(e -> e.kill(endWorld));
      skeletons.forEach(e -> e.kill(endWorld));
      phantoms.forEach(e -> e.kill(endWorld));
      illusioners.forEach(e -> e.kill(endWorld));
   
      GameRules rules = server.getGameRules();
      GameRules.BooleanRule rule = rules.get(GameRules.KEEP_INVENTORY);
      rule.set(keepInventoryBefore,server);
   
      resetVariables();
      removeScoreboards(server);
      BossFight.cleanBoss(server);
      return 0;
   }
   
   public static int beginBoss(MinecraftServer server, NbtCompound data){
      States state = States.valueOf(data.getString("State"));
      ServerPlayerEntity gm = server.getPlayerManager().getPlayer(MiscUtils.getUUID(data.getString("GameMaster")));
      if(state == States.WAITING_START){
         if(startTimeAnnounced){
            States.updateState(States.WAITING_ONE,server);
            if(gm != null){
               gm.sendMessage(Text.literal("Beginning Boss Fight. Good Luck, Have Fun!"));
            }
            return 0;
         }else{
            if(gm != null){
               gm.sendMessage(Text.literal("Make sure you have run /arcana boss announce <time> so your players know to get ready first!"));
            }
         }
      }else{
         if(gm != null){
            gm.sendMessage(Text.literal("The Event State is incompatible with this command. Have you run /arcana boss start dragon?"));
         }
      }
      return -1;
   }
   
   public static int announceBoss(MinecraftServer server, NbtCompound data, String time){
      States state = States.valueOf(data.getString("State"));
      if(state == States.WAITING_START){
         DragonDialog.announce(DragonDialog.Announcements.EVENT_PREP,server,time);
         startTimeAnnounced = true;
         return 0;
      }else{
         ServerPlayerEntity gm = server.getPlayerManager().getPlayer(MiscUtils.getUUID(data.getString("GameMaster")));
         if(gm != null){
            gm.sendMessage(Text.literal("The Event State is incompatible with this command. Have you run /arcana boss start dragon?"));
         }
      }
      return -1;
   }
   
   public static int calcPlayers(MinecraftServer server, boolean inEnd){
      if(forcedPlayerCount > 0) return forcedPlayerCount;
      List<ServerPlayerEntity> playerList = server.getPlayerManager().getPlayerList();
      int count = 0;
      if(!inEnd) return playerList.size();
      for(ServerPlayerEntity player : playerList){
         if(!player.isSpectator() && !player.isCreative()){
            if(player.getEntityWorld() == server.getWorld(World.END)){
               count++;
            }
         }
      }
      return count;
   }
   
   public static int resetDragonAbilities(MinecraftServer server, ServerCommandSource source, boolean doAbility){
      if(dragonAbilities == null) return -1;
      dragonAbilities.resetCooldowns();
      if(doAbility && dragonAbilities.doAbility(phase)) lastDragonAction = 0;
      return 1;
   }
   
   public static int forceLairAction(MinecraftServer server, ServerCommandSource source){
      if(lairActions == null) return -1;
      if(lairActions.startAction(phase)) lastLairAction = 0;
      return 1;
   }
   
   public static int bossStatus(MinecraftServer server, ServerCommandSource source){
      Pair<BossFights, NbtCompound> bossFight = BOSS_FIGHT.get(server.getWorld(World.END)).getBossFight();
      NbtCompound data = bossFight.getRight();
      States state = States.valueOf(data.getString("State"));
      ServerPlayerEntity gm = server.getPlayerManager().getPlayer(MiscUtils.getUUID(data.getString("GameMaster")));
      ArrayList<MutableText> msgs = new ArrayList<>();
   
      msgs.add(Text.literal(""));
      msgs.add(Text.literal("Dragon Boss Data: "));
      msgs.add(Text.literal("Game Master: "+gm.getName().getString()));
      msgs.add(Text.literal("State: "+state.name()));
      
      if(state != States.WAITING_RESPAWN){
         msgs.add(Text.literal("Num Players: "+numPlayers));
         msgs.add(Text.literal("Dragon HP: ("+dragon.getHealth()+"/"+dragon.getMaxHealth()+")"));
         msgs.add(Text.literal("Last Goon Spawn: "+lastGoonSpawn/20+" seconds ago."));
         msgs.add(Text.literal(""));
      }
      if(state == States.PHASE_ONE){
         for(PhantomEntity guardianPhantom : guardianPhantoms){
            if(guardianPhantom != null){
               msgs.add(Text.literal("Guardian Phantom HP: ("+guardianPhantom.getHealth()+"/"+guardianPhantom.getMaxHealth()+")"));
            }
         }
      }else if(state == States.PHASE_TWO){
         int wizardCount = 0;
         int crystalCount = 0;
         for(DragonWizardEntity wizard : wizards){
            if(wizard != null && wizard.isAlive())
               wizardCount++;
         }
         for(EndCrystalEntity crystal : crystals){
            if(crystal != null && crystal.isAlive())
               crystalCount++;
         }
   
         msgs.add(Text.literal("Wizards Alive: "+wizardCount+"/10"));
         msgs.add(Text.literal("Crystals Alive: "+crystalCount+"/10"));
      }
      msgs.add(Text.literal(""));
      
      if(state == States.PHASE_ONE || state == States.PHASE_TWO || state == States.PHASE_THREE){
         if(dragonAbilities != null){
            msgs.add(Text.literal("Dragon Ability Cooldowns: Next Check in "+ (600 - lastDragonAction)/20 + " Seconds"));
            for(Pair<DragonAbilities.DragonAbilityTypes, Integer> cooldown : dragonAbilities.getCooldowns(phase)){
               msgs.add(Text.literal(" - "+cooldown.getLeft().name()+": "+cooldown.getRight()/20+" Seconds"));
            }
         }
   
         msgs.add(Text.literal(""));
         msgs.add(Text.literal("Lair Action: Next Check in "+ (1100 - lastLairAction)/20 + " Seconds"));
      }
   
      for(MutableText msg : msgs){
         source.sendFeedback(()->msg,false);
      }
      return 0;
   }
   
   public static void playerDied(ServerPlayerEntity player){
      if(hasDied != null)
         hasDied.add(player);
   }
   
   public static void teleportPlayer(ServerPlayerEntity player, boolean override){
      ServerWorld endWorld = player.getServer().getWorld(World.END);
      if(hasDied.contains(player) || override){
         player.teleportTo(new TeleportTarget(endWorld, new Vec3d(100.5,51,0.5), Vec3d.ZERO, 90, 0, TeleportTarget.ADD_PORTAL_CHUNK_TICKET));
         StatusEffectInstance res = new StatusEffectInstance(StatusEffects.RESISTANCE, 20*30, 4, false, false, true);
         player.addStatusEffect(res);
         MutableText msg = Text.literal("")
               .append(Text.literal("You are ").formatted(Formatting.AQUA))
               .append(Text.literal("protected").formatted(Formatting.DARK_PURPLE, Formatting.BOLD))
               .append(Text.literal(" from damage for 30 seconds!").formatted(Formatting.AQUA));
         player.sendMessage(msg, false);
         hasDied.remove(player);
      }else{
         MutableText msg = Text.literal("")
               .append(Text.literal("You only get to teleport after dying.").formatted(Formatting.RED,Formatting.ITALIC));
         player.sendMessage(msg, false);
      }
   }
   
   public static void setForcedPlayerCount(MinecraftServer server, int pc){
      forcedPlayerCount = pc;
      numPlayers = calcPlayers(server,false);
   }
   
   public static List<ReclaimState> getReclaimStates(){
      return reclaimStates;
   }
   
   public static class ReclaimState{
      private final ElementHolder hologram;
      private final HolderAttachment attachment;
      private Vec3d pos;
      private ServerWorld endWorld;
      private ServerPlayerEntity player;
      private int solveCooldown;
      private Vec3d shieldPos;
      private int shieldTicks;
      private long ticks;
      private TowerGui towerGui;
      private long animTicks = 0;
      private boolean hologramVisible = false;
      
      // 0 - Waiting to be reclaimed
      // 1 - Puzzle Cooldown
      // 2 - In use
      // 3 - Destroyed
      private int state;
      
      public ReclaimState(Vec3d pos, ServerWorld endWorld){
         this.pos = pos;
         this.endWorld = endWorld;
         this.player = null;
         this.solveCooldown = 0;
         this.state = 0;
         this.shieldPos = null;
         this.shieldTicks = 0;
         this.hologram = getNewHologram();
         this.attachment = ChunkAttachment.ofTicking(this.hologram,this.endWorld, this.pos);
         
         ticks = 0;
         animTicks = 0;
      }
      
      private ElementHolder getNewHologram(){
         ReclaimState reclaimState = this;
         TextDisplayElement line1 = new TextDisplayElement(Text.literal("Click to Attempt").formatted(Formatting.YELLOW));
         TextDisplayElement line2 = new TextDisplayElement(Text.literal("Tower Reclamation").formatted(Formatting.BOLD,Formatting.LIGHT_PURPLE,Formatting.UNDERLINE));
         ItemDisplayElement icon = new ItemDisplayElement(Items.END_CRYSTAL);
         InteractionElement click = new InteractionElement(new VirtualElement.InteractionHandler(){
            public void click(ServerPlayerEntity player){
               if(reclaimState.getSolveCooldown() == 0 && reclaimState.hologramVisible){
                  PuzzleGui gui = new PuzzleGui(ScreenHandlerType.GENERIC_9X6,player,reclaimState);
                  gui.buildPuzzle();
                  gui.open();
                  reclaimState.setPlayer(player);
                  reclaimState.setSolveCooldown(1200);
               }
            }
            
            @Override
            public void interact(ServerPlayerEntity player, Hand hand){
               click(player);
            }
            
            @Override
            public void interactAt(ServerPlayerEntity player, Hand hand, Vec3d pos){
               click(player);
            }
            
            @Override
            public void attack(ServerPlayerEntity player){
               click(player);
            }
         });
         click.setSize(3,3);
         TextDisplayElement cooldownText = new TextDisplayElement(Text.literal("On Cooldown - "+(solveCooldown/20+1)+" Seconds").formatted(Formatting.AQUA));
         
         
         ElementHolder holder = new ElementHolder(){
            private final ReclaimState state = reclaimState;
            private final TextDisplayElement cdText = cooldownText;
            private final TextDisplayElement line1Text = line1;
            private final TextDisplayElement line2Text = line2;
            private final ItemDisplayElement iconElem = icon;
            private final InteractionElement clickElem = click;
            
            @Override
            protected void onTick(){
               super.onTick();
               
               if(!state.hologramVisible){
                  line1Text.setInvisible(true);
                  this.removeElement(line1Text);
                  
                  line2Text.setInvisible(true);
                  this.removeElement(line2Text);
                  
                  iconElem.setInvisible(true);
                  this.removeElement(iconElem);
               }else{
                  line1Text.setInvisible(false);
                  this.addElement(line1Text);
                  
                  line2Text.setInvisible(false);
                  this.addElement(line2Text);
                  
                  iconElem.setInvisible(false);
                  this.addElement(iconElem);
               }
               
               if(state.hologramVisible){
                  if(state.solveCooldown == 0){
                     this.removeElement(cdText);
                     cdText.setInvisible(true);
                  }else{
                     this.addElement(cdText);
                     cdText.setText(Text.literal("On Cooldown - "+(state.solveCooldown/20+1)+" Seconds").formatted(Formatting.AQUA));
                     cdText.setInvisible(false);
                  }
               }else{
                  cdText.setInvisible(true);
                  this.removeElement(cdText);
               }
            }
         };
         line1.setOffset(new Vec3d(0,1.75,0));
         line2.setOffset(new Vec3d(0,1.5,0));
         icon.setOffset(new Vec3d(0,1,0));
         cooldownText.setOffset(new Vec3d(0,0.25,0));
         click.setOffset(new Vec3d(0,1,0));
         
         line1.setBillboardMode(DisplayEntity.BillboardMode.VERTICAL);
         line2.setBillboardMode(DisplayEntity.BillboardMode.VERTICAL);
         icon.setBillboardMode(DisplayEntity.BillboardMode.VERTICAL);
         cooldownText.setBillboardMode(DisplayEntity.BillboardMode.VERTICAL);
         
         holder.addElement(line1);
         holder.addElement(line2);
         holder.addElement(icon);
         holder.addElement(click);
         holder.addElement(cooldownText);
         return holder;
      }
      
      public void tick(){
         try{
            ticks++;
            if(solveCooldown > 0){
               solveCooldown--;
               if(solveCooldown == 0){
                  if(state == 1) state = 0;
               }
            }
            if(shieldTicks > 0){
               shieldTicks--;
            }
            if(state == 2){
               animTicks++;
               if(player != null && player.squaredDistanceTo(pos.x,pos.y+1,pos.z) > 25){
                  player.requestTeleport(pos.x,pos.y+1,pos.z);
                  player.setVelocity(0,0,0);
                  player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
               }
               if(player != null && ticks % 5 == 0){
                  StatusEffectInstance res = new StatusEffectInstance(StatusEffects.RESISTANCE, 25, 4, false, true, true);
                  player.addStatusEffect(res);
                  endWorld.spawnParticles(ParticleTypes.CLOUD,player.getX(),player.getY(),player.getZ(),5,0.25,0.25,0.25,0);
               }
               if(animTicks % 150 == 0){
                  ParticleEffectUtils.dragonReclaimTowerCircle(endWorld,pos.add(0,-1,0),8000,1);
               }
            }
            if(shieldPos != null && shieldTicks > 0){
               if(shieldTicks % 5 == 0){
                  List<Entity> entities = endWorld.getOtherEntities(player,new Box(shieldPos.x+5,shieldPos.y+5,shieldPos.z+5,shieldPos.x-5,shieldPos.y-5,shieldPos.z-5),e -> (e instanceof LivingEntity && !(e instanceof ServerPlayerEntity)));
                  for(Entity entity : entities){
                     if(entity instanceof LivingEntity living){
                        Vec3d vec = new Vec3d(shieldPos.getX()-entity.getX(),0,shieldPos.getZ()-entity.getZ());
                        vec = vec.normalize().multiply(3);
                        living.setVelocity(-vec.x,1,-vec.z);
                     }
                  }
               }
               if(shieldTicks % 20 == 0){
                  List<ServerPlayerEntity> inRangePlayers = endWorld.getPlayers(p -> p.squaredDistanceTo(shieldPos) <= 5*5);
                  for(ServerPlayerEntity plyr: inRangePlayers){
                     StatusEffectInstance res = new StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 5 + 5, 0, false, true, true);
                     StatusEffectInstance regen = new StatusEffectInstance(StatusEffects.REGENERATION, 20 * 5 + 5, 2, false, true, true);
                     plyr.addStatusEffect(res);
                     plyr.addStatusEffect(regen);
                  }
               }
            }
         }catch(Exception e){
            e.printStackTrace();
         }
      }
      
      public void playerSolved(){
         state = 2;
         player.requestTeleport(pos.x,pos.y+1,pos.z);
         player.setVelocity(0,0,0);
         player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
         hologramVisible = false;
         towerGui = new TowerGui(player,this);
         towerGui.buildGui();
         towerGui.open();
         animTicks = 0;
         ParticleEffectUtils.dragonReclaimTowerCircle(endWorld,pos.add(0,-1,0),8000,1);
      }
      
      public void playerExits(){
         setSolveCooldown(2400);
         state = 1;
         hologramVisible = true;
         player = null;
      }
      
      public void fightEnd(){
         if(towerGui != null){
            towerGui.close();
         }
         hologramVisible = false;
         hologram.destroy();
      }
   
      public void destroyTower(){
         towerGui.close();
         
         List<ServerPlayerEntity> inRangePlayers = endWorld.getPlayers(p -> p.squaredDistanceTo(new Vec3d(pos.getX()+.5,pos.getY()-1,pos.getZ()+.5)) <= 8.5*8.5);
         for(ServerPlayerEntity player : inRangePlayers){
            BlockPos target = BlockPos.ofFloored(pos.getX()+.5,pos.getY()-1,pos.getZ()+.5);
            BlockPos playerPos = player.getBlockPos();
            Vec3d vec = new Vec3d(target.getX()-playerPos.getX(),0,target.getZ()-playerPos.getZ());
            vec = vec.normalize().multiply(5);
         
            player.setVelocity(-vec.x,1,-vec.z);
            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
         
            player.sendMessage(Text.literal("The Tower's Explosion Launches You!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC),true);
         }
         // Explosion Particles / Sounds
         endWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, pos.getX()+.5, pos.getY()-1, pos.getZ()+.5, 20, 2, 2, 2,0.5);
         // Tiered removal of obsidian
         for(BlockPos block : BlockPos.iterateOutwards(BlockPos.ofFloored(pos), 12, 12, 12)){
            boolean destroy = true;
            double dist = Math.sqrt(block.getSquaredDistance(pos));
            if(!block.isWithinDistance(pos, 5)){
               destroy = Math.random() < (-.01 * ((dist - 5) * (dist - 5)) + 1);
            }
            if(destroy)
               endWorld.breakBlock(block,false);
         }
      
         endWorld.createExplosion(dragon,null,null,pos.getX(),pos.getY(),pos.getZ(),10,true, World.ExplosionSourceType.NONE);
         hologramVisible = false;
         hologram.destroy();
         state = 3;
      }
      
      public void castShield(){
         Vec3d end = player.getEyePos().add(player.getRotationVector().normalize().multiply(100));
         BlockHitResult result = endWorld.raycast(new RaycastContext(player.getEyePos(),end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE,player));
         shieldPos = result.getPos();
         shieldTicks = 300;
         ParticleEffectUtils.dragonReclaimTowerShield(endWorld,shieldPos,0);
      }
      
      public void castLaser(){ // TODO: Update all this
         Vec3d end = player.getEyePos().add(player.getRotationVector().normalize().multiply(75));
         BlockHitResult result = endWorld.raycast(new RaycastContext(player.getEyePos(),end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE,player));
         Vec3d hit = result.getPos();
         ParticleEffectUtils.longDistLine(endWorld,player.getPos().add(0,.7,0),hit, ParticleTypes.GLOW,(int)(2*hit.length()),1,0.1,0);
         List<Entity> entities = endWorld.getOtherEntities(player,new Box(hit.x+2,hit.y+2,hit.z+2,hit.x-2,hit.y-2,hit.z-2),e -> (e instanceof LivingEntity && !(e instanceof ServerPlayerEntity)));
   
         Scoreboard scoreboard = endWorld.getServer().getScoreboard();
         ScoreAccess scoreboardPlayerScore = scoreboard.getOrCreateScore(ScoreHolder.fromProfile(player.getGameProfile()),scoreboard.getNullableObjective("arcananovum_boss_dmg_dealt"));
         
         for(Entity entity : entities){
            if(entity instanceof LivingEntity living){
               if(scoreboardPlayerScore != null)
                  scoreboardPlayerScore.setScore(scoreboardPlayerScore.getScore() + 50);
               living.damage(endWorld, endWorld.getDamageSources().playerAttack(player),5f);
            }
         }
         if(MiscUtils.distToLine(dragon.getPos(),player.getPos(),hit) < 10){
            float damage = Math.min(100,15+numPlayers*3);
            if(scoreboardPlayerScore != null)
               scoreboardPlayerScore.setScore(scoreboardPlayerScore.getScore() + (int)(damage*10));
            dragon.damage(endWorld, endWorld.getDamageSources().playerAttack(player),damage);
         }
      }
   
      public ServerPlayerEntity getPlayer(){
         return player;
      }
   
      public int getSolveCooldown(){
         return solveCooldown;
      }
   
      public int getState(){
         return state;
      }
   
      public Vec3d getPos(){
         return pos;
      }
   
      public void setPlayer(ServerPlayerEntity player){
         this.player = player;
      }
   
      public void setSolveCooldown(int solveCooldown){
         if(this.solveCooldown == 0 && solveCooldown > 0){
            state = 1;
         }else if(this.solveCooldown > 0 && solveCooldown == 0){
            if(state == 1) state = 0;
         }
         this.solveCooldown = solveCooldown;
      }
   
      public void setState(int state){
         this.state = state;
      }
   }
   
   public enum States{
      WAITING_RESPAWN, // If the dragon is dead, wait for respawn
      WAITING_START,   // Queue to set up fight
      WAITING_ONE,     // Waiting for phase one transition
      PHASE_ONE,       // During phase one
      WAITING_TWO,     // Waiting for phase two transition
      PHASE_TWO,       // During phase two
      WAITING_THREE,   // Waiting for phase three transition
      PHASE_THREE,     // During phase three
      WAITING_DEATH;   // Dragon is dead, give out rewards and end Fight
   
      public static States fromLabel(String id){
         return States.valueOf(id);
      }
      
      public static void updateState(States state, MinecraftServer server){
         Pair<BossFights, NbtCompound> bossFight = BOSS_FIGHT.get(server.getWorld(World.END)).getBossFight();
         if(bossFight.getLeft() == BossFights.DRAGON){
            bossFight.getRight().putString("State", state.name());
         }else{
            devPrint("Boss fight not valid");
         }
      }
   }
}
