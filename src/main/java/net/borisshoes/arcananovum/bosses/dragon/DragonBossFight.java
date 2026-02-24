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
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.datastorage.BossFightData;
import net.borisshoes.arcananovum.entities.DragonPhantomEntity;
import net.borisshoes.arcananovum.entities.DragonWizardEntity;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.callbacks.ItemReturnTimerCallback;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.MathUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.*;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static net.borisshoes.arcananovum.ArcanaNovum.devPrint;
import static net.borisshoes.arcananovum.ArcanaNovum.log;
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
   private static EnderDragon dragon = null;
   private static ServerPlayer gameMaster = null;
   private static boolean keepInventoryBefore = false;
   private static List<EndCrystal> crystals = null;
   private static DragonPhantomEntity[] guardianPhantoms = null;
   private static DragonWizardEntity[] wizards = null;
   private static CustomBossEvent[] phantomBossBars = null;
   private static List<ServerPlayer> hasDied = null;
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
   
   public static void tick(MinecraftServer server, CompoundTag fightData){
      try{
         States state = States.fromLabel(fightData.getStringOr("State", ""));
         ServerPlayer gm = server.getPlayerList().getPlayer(AlgoUtils.getUUID(fightData.getStringOr("GameMaster", "")));
         List<MutableComponent> gmNotifs = new ArrayList<>();
         ServerLevel endWorld = server.getLevel(Level.END);
         assert endWorld != null;
         
         //if(age%40 == 1){gmNotifs.add(Text.literal("Age: "+age));}
      
         if(state == States.WAITING_START){ // Set players and dragon name/hp, set keep inventory, make scoreboards, set crystals to be invincible
            if(!prepNotif){
               if(endWorld.getDragons().isEmpty()){
                  log(3,"Attempted to start Enderia fight without a dragon, cancelling boss fight");
                  abortBoss(server);
                  return;
               }
               numPlayers = calcPlayers(server,false);
               fightData.putInt("numPlayers",numPlayers);
               gameMaster = gm;
               hasDied = new ArrayList<>();
               dragon = endWorld.getDragons().getFirst();
               dragon.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1024);
               dragon.setHealth(1024);
               reclaimStates = new ArrayList<>();
   
               MutableComponent dragonName = Component.literal("").withStyle(ChatFormatting.BOLD)
                     .append(Component.literal("|").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.OBFUSCATED))
                     .append(Component.literal("|").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.OBFUSCATED))
                     .append(Component.literal("|").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.OBFUSCATED))
                     .append(Component.literal(" "))
                     .append(Component.literal("Enderia").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD, ChatFormatting.UNDERLINE))
                     .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD, ChatFormatting.UNDERLINE))
                     .append(Component.literal("Empress").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD, ChatFormatting.UNDERLINE, ChatFormatting.ITALIC))
                     .append(Component.literal(" of ").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD, ChatFormatting.UNDERLINE, ChatFormatting.ITALIC))
                     .append(Component.literal("The End").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD, ChatFormatting.UNDERLINE, ChatFormatting.ITALIC))
                     .append(Component.literal(" "))
                     .append(Component.literal("|").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.OBFUSCATED))
                     .append(Component.literal("|").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.OBFUSCATED))
                     .append(Component.literal("|").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.OBFUSCATED));
   
               dragon.setCustomName(dragonName);
   
               // Turn keep inventory on
               GameRules rules = endWorld.getGameRules();
               keepInventoryBefore = rules.get(GameRules.KEEP_INVENTORY);
               rules.set(GameRules.KEEP_INVENTORY, true, server);
   
               // Make scoreboards
               makeScoreboards(server);
               
               // Set crystals to be invulnerable
               crystals = endWorld.getEntities(EntityType.END_CRYSTAL, new AABB(new BlockPos(-50,25,-50).getCenter(), new BlockPos(50,115,50).getCenter()), EndCrystal::showsBottom);
               for(EndCrystal crystal : crystals){
                  crystal.setInvulnerable(true);
               }
               
               MutableComponent notif = Component.literal("")
                     .append(Component.literal("Dragon Prepped, Awaiting Announcement & Start Commands. ").withStyle(ChatFormatting.LIGHT_PURPLE))
                     .append(Component.literal(" [Announce]").withStyle(s ->
                           s.withClickEvent(new ClickEvent.SuggestCommand("/arcana boss announce 5 Minutes"))
                                 .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to announce the Fight!")))
                                 .withColor(ChatFormatting.AQUA).withBold(true)))
                     .append(Component.literal(" [Start]").withStyle(s ->
                           s.withClickEvent(new ClickEvent.SuggestCommand("/arcana boss begin"))
                                 .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to begin the Fight!")))
                                 .withColor(ChatFormatting.AQUA).withBold(true)));
               gmNotifs.add(notif);
               prepNotif = true;
            }
         }else if(state == States.WAITING_ONE){ //Teleport players, spawn phantoms, spawn wizards
            if(!phase1Notif){
               numPlayers = calcPlayers(server,false);
               fightData.putInt("numPlayers",numPlayers);
               DragonDialog.announce(DragonDialog.Announcements.EVENT_START,server,null);
               BorisLib.addTickTimerCallback(endWorld, new GenericTimer(100, () -> {
                     List<ServerPlayer> players = server.getPlayerList().getPlayers();
                     for(ServerPlayer player : players){
                        if(!player.isCreative() && !player.isSpectator()){
                           player.teleport(new TeleportTransition(endWorld, new Vec3(100.5+(Math.random()*3-1.5),51,0.5+(Math.random()*3-1.5)), Vec3.ZERO, 90, 0, TeleportTransition.PLACE_PORTAL_TICKET));
                        }
                     }
   
                     guardianPhantoms = new DragonPhantomEntity[4];
                     phantomBossBars = new CustomBossEvent[4];
                     ListTag phantomData = new ListTag();
                     for(int i=0;i<guardianPhantoms.length;i++){
                        guardianPhantoms[i] = DragonGoonHelper.makeGuardianPhantom(endWorld,numPlayers);
                        phantomBossBars[i] = endWorld.getServer().getCustomBossEvents().create(Identifier.parse("guardianphantom-"+guardianPhantoms[i].getStringUUID()),guardianPhantoms[i].getCustomName());
                        phantomBossBars[i].setColor(BossEvent.BossBarColor.PURPLE);
                        guardianPhantoms[i].addEffect(new MobEffectInstance(MobEffects.RESISTANCE,100,4));
                        endWorld.addFreshEntityWithPassengers(guardianPhantoms[i]);
                        phantomData.add(StringTag.valueOf(guardianPhantoms[i].getStringUUID()));
                     }
   
                     // Spawn wizards
                     wizards = new DragonWizardEntity[crystals.size()];
                     ListTag wizardData = new ListTag();
                     for(int i = 0; i < wizards.length; i++){
                        EndCrystal crystal = crystals.get(i);
                        wizards[i] = DragonGoonHelper.makeWizard(endWorld,numPlayers);
                        wizards[i].setPos(crystal.position().add(0,2,0));
                        wizards[i].setInvulnerable(true);
                        wizards[i].setCrystalId(crystal.getUUID());
                        wizards[i].addEffect(new MobEffectInstance(MobEffects.RESISTANCE,100,4));
                        endWorld.addFreshEntityWithPassengers(wizards[i]);
                        wizardData.add(StringTag.valueOf(wizards[i].getStringUUID()));
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
            List<ServerPlayer> nearbyPlayers300 = endWorld.getPlayers(p -> p.distanceToSqr(new Vec3(0,100,0)) <= 300*300);
            List<EnderMan> endermen = endWorld.getEntities(EntityType.ENDERMAN, new AABB(new BlockPos(-100,25,-100).getCenter(), new BlockPos(100,115,100).getCenter()), e -> true);
   
            for(EnderMan enderman : endermen){ // Make endermen not attack Endermites
               if(enderman.getTarget() instanceof Endermite || (enderman.getPersistentAngerTarget() != null && endWorld.getEntity(enderman.getPersistentAngerTarget().getUUID()) instanceof Endermite)){
                  enderman.setTarget(null);
                  enderman.setPersistentAngerTarget(null);
               }
            }
            
            int aliveCount = 0;
            if(guardianPhantoms != null){
               for(int i = 0; i < guardianPhantoms.length; i++){
                  if(guardianPhantoms[i] != null){
                     if(guardianPhantoms[i].isAlive()){
                        aliveCount++;
                        float percent = guardianPhantoms[i].getHealth() / guardianPhantoms[i].getMaxHealth();
                        phantomBossBars[i].setProgress(percent);
                        for(ServerPlayer player : nearbyPlayers300){
                           phantomBossBars[i].addPlayer(player);
                        }
                     }else{
                        phantomBossBars[i].removeAllPlayers();
                        server.getCustomBossEvents().remove(phantomBossBars[i]);
                        guardianPhantoms[i].discard();
                        guardianPhantoms[i] = null;
                        DragonDialog.announce(DragonDialog.Announcements.PHANTOM_DEATH,server,null);
                     }
                  }
               }
            }
            dragon.heal(50+25*numPlayers);
            
            if(aliveCount == 0){ // Progress to phase 2
               gmNotifs.add(Component.literal("All Phantoms Dead, progressing to Phase 2"));
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
            List<ServerPlayer> nearbyPlayers300 = endWorld.getPlayers(p -> p.distanceToSqr(new Vec3(0,100,0)) <= 300*300);
            
            if(wizards != null){
               for(int i = 0; i < wizards.length; i++){
                  if(wizards[i] != null){
                     if(wizards[i].isAlive()){
                        if(age % 130 == 0){
                           if(wizards[i].getCrystalId() != null){
                              Entity entity = endWorld.getEntity(wizards[i].getCrystalId());
                              if(entity instanceof EndCrystal crystal){
                                 Vec3 crystalPos = crystal.position().add(0,-1,0);
                                 ArcanaEffectUtils.dragonBossTowerCircleInvuln(endWorld,crystalPos,6000,0);
                              }
                           }
                        }
                     }else{
                        if(wizards[i].getCrystalId() != null){
                           Entity entity = endWorld.getEntity(wizards[i].getCrystalId());
                           if(entity instanceof EndCrystal crystal){
                              crystal.setInvulnerable(false);
                           }
                        }
                        final int finalI = i;
                        List<ServerPlayer> nearbyPlayers = endWorld.getPlayers(p -> p.distanceToSqr(wizards[finalI].position()) <= 20*20);
                        for(ServerPlayer player : nearbyPlayers){
                           player.displayClientMessage(Component.literal("The Crystal's Shield Fades!").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),true);
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
               EndCrystal crystal = crystals.get(i);
               if(crystal != null){
                  if(crystal.isAlive()){
                     aliveCount++;
                  }else{
                     ReclaimState newRecState = new ReclaimState(crystal.position(),endWorld);
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
               gmNotifs.add(Component.literal("All Crystals Dead, progressing to Phase 3"));
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
            List<EnderMan> endermen = endWorld.getEntities(EntityType.ENDERMAN, new AABB(new BlockPos(-100,25,-100).getCenter(), new BlockPos(100,115,100).getCenter()), e -> true);
            float dragonHP = dragon.getHealth();
            float dragonMax = dragon.getMaxHealth();
   
            for(EnderMan enderman : endermen){
               if(dragonHP/dragonMax <= 0.5){
                  int amp = dragonHP/dragonMax <= 0.25 ? 1 : 0;
                  MobEffectInstance strength = new MobEffectInstance(MobEffects.STRENGTH,120,amp,false,false,false);
                  enderman.addEffect(strength);
                  MobEffectInstance resist = new MobEffectInstance(MobEffects.RESISTANCE,120,amp,false,false,false);
                  enderman.addEffect(resist);
               }
               if(enderman.isCreepy()){
                  Player closestPlayer = endWorld.getNearestPlayer(enderman,4);
                  if(closestPlayer != null){
                     MobEffectInstance slow = new MobEffectInstance(MobEffects.SLOWNESS,60,2,false,false,false);
                     closestPlayer.addEffect(slow);
                  }
               }
            }
            if(age % 300 == 0){
               for(EnderMan enderman : endermen){
                  Player closestPlayer = endWorld.getNearestPlayer(enderman,30);
                  if(closestPlayer != null){
                     enderman.setBeingStaredAt();
                     enderman.setTarget(closestPlayer);
                     enderman.setPersistentAngerTarget(EntityReference.of(closestPlayer));
                     enderman.setPersistentAngerEndTime(enderman.tickCount + 1200);
                  }else{
                     Player randomPlayer = endWorld.getRandomPlayer();
                     if(randomPlayer != null && Math.random() < .1*numPlayers){
                        enderman.setBeingStaredAt();
                        enderman.setTarget(randomPlayer);
                        enderman.setPersistentAngerTarget(EntityReference.of(randomPlayer));
                        enderman.setPersistentAngerEndTime(enderman.tickCount + 1200);
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
               gmNotifs.add(Component.literal("Dragon Dead, Concluding Fight"));
               States.updateState(States.WAITING_DEATH,server);
            }
         }else if(state == States.WAITING_DEATH){
            if(!endNotif){
               for(DragonBossFight.ReclaimState reclaimState : reclaimStates){
                  reclaimState.fightEnd();
               }
               BorisLib.addTickTimerCallback(endWorld, new GenericTimer(100, () -> endFight(server,endWorld)));
               endNotif = true;
            }
         }
         
         if(fightData.getIntOr("numPlayers", 0) != numPlayers){
            fightData.putInt("numPlayers",numPlayers);
         }
   
         // Handle lair actions, dragon actions, goon spawning, crystal effects
         if(state == States.PHASE_ONE || state == States.PHASE_TWO || state == States.PHASE_THREE){
            crystals.forEach(c -> tickCrystal(endWorld,c,phase));
            
            if(lastGoonSpawn > 20*45) spawnGoons(endWorld,phase);
            if(lastLairAction > 20*35 && lairActions.startAction(phase)) lastLairAction = 0;
            if(lastDragonAction > 20*25 && dragonAbilities.doAbility(phase)) lastDragonAction = 0;
         }
      
         if(gm != null) gmNotifs.forEach(gm::sendSystemMessage);
         if(state != States.WAITING_RESPAWN && state != States.WAITING_START && state != States.WAITING_ONE){
            if(age % 3000 == 0){
               List<ShulkerBullet> bullets = endWorld.getEntities(EntityType.SHULKER_BULLET, new AABB(new BlockPos(-400,0,-400).getCenter(), new BlockPos(400,256,400).getCenter()), e -> true);
               for(ShulkerBullet bullet : bullets){
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
   
   private static void spawnGoons(ServerLevel endWorld, int phase){
      if(phase == 1){ // Endermite Goons
         // Count existing goons
         List<Endermite> curGoons = endWorld.getEntities(EntityType.ENDERMITE, new AABB(new BlockPos(-300,25,-300).getCenter(), new BlockPos(300,255,300).getCenter()), e -> true);
         if(curGoons.size() > 35) return;
         double chance = curGoons.size() < 5 ? 0.005 : 0.002;
         if(Math.random() > chance) return; // Average 25+minTime seconds before goon spawn
   
         Endermite[] goons = new Endermite[Mth.clamp((int)(Math.random()*3*numPlayers+2+numPlayers),10,35)];
         ArrayList<BlockPos> poses = makeSpawnLocations(goons.length,50,endWorld);
         float endermiteHP = Mth.clamp(10 + 3*numPlayers,10,40);
         for(int i=0;i<goons.length;i++){
            goons[i] = new Endermite(EntityType.ENDERMITE, endWorld);
            goons[i].getAttribute(Attributes.MAX_HEALTH).setBaseValue(endermiteHP);
            goons[i].setHealth(endermiteHP);
            goons[i].setPersistenceRequired();
            goons[i].getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(8f);
            BlockPos pos = poses.get(i);
            goons[i].setPosRaw(pos.getX(),pos.getY()+1,pos.getZ());
      
            endWorld.addFreshEntityWithPassengers(goons[i]);
         }
         
         DragonDialog.announce(DragonDialog.Announcements.PHASE_ONE_GOONS,endWorld.getServer(),null);
      }else if(phase == 2){ // Shulker Goons
         // Count existing goons
         List<Shulker> curGoons = endWorld.getEntities(EntityType.SHULKER, new AABB(new BlockPos(-300,25,-300).getCenter(), new BlockPos(300,255,300).getCenter()), e -> true);
         if(curGoons.size() > 35) return;
         double chance = curGoons.size() < 5 ? 0.005 : 0.002;
         if(Math.random() > chance) return; // Average 25+minTime seconds before goon spawn
   
         Shulker[] goons = new Shulker[Mth.clamp((int)(Math.random()*2*numPlayers+2+numPlayers),10,35)];
         ArrayList<BlockPos> poses = makeSpawnLocations(goons.length,50,endWorld);
         float shulkerHP = Mth.clamp(20 + 4*numPlayers,20,80);
         for(int i=0;i<goons.length;i++){
            goons[i] = new Shulker(EntityType.SHULKER, endWorld);
            goons[i].getAttribute(Attributes.MAX_HEALTH).setBaseValue(shulkerHP);
            goons[i].setHealth(shulkerHP);
            goons[i].setPersistenceRequired();
            BlockPos pos = poses.get(i);
            goons[i].setPosRaw(pos.getX(),pos.getY(),pos.getZ());
      
            endWorld.addFreshEntityWithPassengers(goons[i]);
         }
   
         DragonDialog.announce(DragonDialog.Announcements.PHASE_TWO_GOONS,endWorld.getServer(),null);
      }else if(phase == 3){ // Enderman Goons
         // Count existing goons
         List<EnderMan> curGoons = endWorld.getEntities(EntityType.ENDERMAN, new AABB(new BlockPos(-300,25,-300).getCenter(), new BlockPos(300,255,300).getCenter()), e -> true);
         if(curGoons.size() > 50) return;
         double chance = curGoons.size() < 5 ? 0.005 : 0.002;
         if(Math.random() > chance) return; // Average 25+minTime seconds before goon spawn
   
         EnderMan[] goons = new EnderMan[Mth.clamp((int)(Math.random()*3*numPlayers+2+numPlayers*2),20,50)];
         ArrayList<BlockPos> poses = makeSpawnLocations(goons.length,50,endWorld);
         float endermanHP = Mth.clamp(20 + 4*numPlayers,20,80);
         for(int i=0;i<goons.length;i++){
            goons[i] = new EnderMan(EntityType.ENDERMAN, endWorld);
            goons[i].getAttribute(Attributes.MAX_HEALTH).setBaseValue(endermanHP);
            goons[i].setHealth(endermanHP);
            goons[i].getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(8f);
            BlockPos pos = poses.get(i);
            goons[i].setPosRaw(pos.getX(),pos.getY(),pos.getZ());
      
            endWorld.addFreshEntityWithPassengers(goons[i]);
         }
         
         DragonDialog.announce(DragonDialog.Announcements.PHASE_THREE_GOONS,endWorld.getServer(),null);
      }
      lastGoonSpawn = 0;
   }
   
   private static void endFight(MinecraftServer server, ServerLevel endWorld){
      try{
         DragonDialog.announce(DragonDialog.Announcements.EVENT_END,server,null);
         ArcanaItem arcanaWings = ArcanaRegistry.WINGS_OF_ENDERIA;
         
         // Give reward
         List<ServerPlayer> players = endWorld.getServer().getPlayerList().getPlayers();
         for(ServerPlayer player : players){
            ItemStack wings = arcanaWings.addCrafter(arcanaWings.getNewItem(),player.getStringUUID(),0,player.level().getServer());
            wings.enchant(MinecraftUtils.getEnchantment(ArcanaRegistry.FATE_ANCHOR),1);
            ArcanaNovum.data(player).addCraftedSilent(wings);
            BorisLib.addTickTimerCallback(new ItemReturnTimerCallback(wings,player,0));
            BorisLib.addTickTimerCallback(new ItemReturnTimerCallback(new ItemStack(Items.DRAGON_EGG,1),player,0));
            
            player.giveExperiencePoints(5500);
         }
      
         List<PlayerScoreEntry> scores = getLeaderboard(server);
         ArrayList<MutableComponent> message = new ArrayList<>();
         // dmg dealt, taken, deaths, kills
      
         message.add(Component.literal(""));
         message.add(Component.literal("")
               .append(Component.literal("-----------------------------------").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)));
         message.add(Component.literal(""));
         message.add(Component.literal("")
               .append(Component.literal("                      ").withStyle(ChatFormatting.GREEN))
               .append(Component.literal("Leaderboard").withStyle(ChatFormatting.GREEN, ChatFormatting.UNDERLINE)));
         message.add(Component.literal(""));
         message.add(Component.literal("")
               .append(Component.literal("  <> ").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
               .append(Component.literal("Most Bloodthirsty").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD, ChatFormatting.UNDERLINE))
               .append(Component.literal(": ").withStyle(ChatFormatting.AQUA))
               .append(Component.literal(scores.get(0).owner()).withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE, ChatFormatting.BOLD))
               .append(Component.literal(" with ").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
               .append(Component.literal(Integer.toString(scores.get(0).value())).withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC))
               .append(Component.literal(" damage points dealt!").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
         message.add(Component.literal(""));
         message.add(Component.literal("")
               .append(Component.literal("  <> ").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
               .append(Component.literal("Tankiest Fighter").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD, ChatFormatting.UNDERLINE))
               .append(Component.literal(": ").withStyle(ChatFormatting.AQUA))
               .append(Component.literal(scores.get(1).owner()).withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE, ChatFormatting.BOLD))
               .append(Component.literal(" with ").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
               .append(Component.literal(Integer.toString(scores.get(1).value())).withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC))
               .append(Component.literal(" damage points taken!").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
         message.add(Component.literal(""));
         message.add(Component.literal("")
               .append(Component.literal("  <> ").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
               .append(Component.literal("Most Death Prone").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD, ChatFormatting.UNDERLINE))
               .append(Component.literal(": ").withStyle(ChatFormatting.AQUA))
               .append(Component.literal(scores.get(2).owner()).withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE, ChatFormatting.BOLD))
               .append(Component.literal(" with ").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
               .append(Component.literal(Integer.toString(scores.get(2).value())).withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC))
               .append(Component.literal(" deaths!").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
         message.add(Component.literal(""));
         message.add(Component.literal("")
               .append(Component.literal("  <> ").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
               .append(Component.literal("Goon Slayer").withStyle(ChatFormatting.RED, ChatFormatting.BOLD, ChatFormatting.UNDERLINE))
               .append(Component.literal(": ").withStyle(ChatFormatting.AQUA))
               .append(Component.literal(scores.get(3).owner()).withStyle(ChatFormatting.GOLD, ChatFormatting.UNDERLINE, ChatFormatting.BOLD))
               .append(Component.literal(" with ").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
               .append(Component.literal(Integer.toString(scores.get(3).value())).withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC))
               .append(Component.literal(" mob kills!").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
         message.add(Component.literal(""));
         message.add(Component.literal("")
               .append(Component.literal("-----------------------------------").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)));
      
         BorisLib.addTickTimerCallback(endWorld, new GenericTimer(400, () -> {
            for(MutableComponent msg : message){
               endWorld.getServer().getPlayerList().broadcastSystemMessage(msg, false);
            }
         }));
      
         GameRules rules = endWorld.getGameRules();
         rules.set(GameRules.KEEP_INVENTORY, keepInventoryBefore, server);
      
         if(gameMaster != null){
            gameMaster.sendSystemMessage(Component.literal("Fight Ended, Thanks For Playing!"));
         }
         resetVariables();
         removeScoreboards(server);
         BossFight.cleanBoss(server);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   private static void tickCrystal(ServerLevel endWorld, EndCrystal crystal, int phase){
      if(crystal == null) return;
      Vec3 pos = crystal.position().add(0,-1,0);
      if(phase == 1){ // Knockback shield, Both particle effects
         if(age % 260 == 0){
            
            ArcanaEffectUtils.dragonBossTowerCirclePush(endWorld,pos,12000,0);
         }
   
         //Actual Knockback
         List<ServerPlayer> inRangePlayers = endWorld.getPlayers(p -> p.distanceToSqr(new Vec3(pos.x()+.5,pos.y()-1,pos.z()+.5)) <= 5.5*5.5);
         for(ServerPlayer player : inRangePlayers){
            BlockPos target = BlockPos.containing(pos.x()+.5,pos.y()-1,pos.z()+.5);
            BlockPos playerPos = player.blockPosition();
            Vec3 vec = new Vec3(target.getX()-playerPos.getX(),0,target.getZ()-playerPos.getZ());
            vec = vec.normalize().scale(3);
      
            player.setDeltaMovement(-vec.x,1,-vec.z);
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
      
            player.displayClientMessage(Component.literal("The Crystal Tower is Shielded!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC),true);
         }
      }
   }
   
   private static void makeScoreboards(MinecraftServer server){
      // Damage Dealt, Damage Taken, Deaths, Mob Kills
      try{
         ServerScoreboard scoreboard = server.getScoreboard();
         ObjectiveCriteria dmgDealt = ObjectiveCriteria.byName("minecraft.custom:minecraft.damage_dealt").orElseThrow();
         ObjectiveCriteria dmgTaken = ObjectiveCriteria.byName("minecraft.custom:minecraft.damage_taken").orElseThrow();
         ObjectiveCriteria deaths = ObjectiveCriteria.DEATH_COUNT;
         ObjectiveCriteria mobKills = ObjectiveCriteria.byName("minecraft.custom:minecraft.mob_kills").orElseThrow();
         
         scoreboard.addObjective("arcananovum_boss_dmg_dealt", dmgDealt, Component.literal("Event Damage Dealt"), dmgDealt.getDefaultRenderType(),false,null);
         scoreboard.addObjective("arcananovum_boss_dmg_taken", dmgTaken, Component.literal("Event Damage Taken"), dmgTaken.getDefaultRenderType(),false,null);
         scoreboard.addObjective("arcananovum_boss_deaths", deaths, Component.literal("Event Deaths"), deaths.getDefaultRenderType(),false,null);
         scoreboard.addObjective("arcananovum_boss_mob_kills", mobKills, Component.literal("Event Mob Kills"), mobKills.getDefaultRenderType(),false,null);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   private static void removeScoreboards(MinecraftServer server){
      // Damage Dealt, Damage Taken, Deaths, Mob Kills
      try{
         ServerScoreboard scoreboard = server.getScoreboard();
         Objective dmgDealt = scoreboard.getObjective("arcananovum_boss_dmg_dealt");
         Objective dmgTaken = scoreboard.getObjective("arcananovum_boss_dmg_taken");
         Objective deaths = scoreboard.getObjective("arcananovum_boss_deaths");
         Objective mobKills = scoreboard.getObjective("arcananovum_boss_mob_kills");
         if(dmgDealt != null) scoreboard.removeObjective(dmgDealt);
         if(dmgTaken != null) scoreboard.removeObjective(dmgTaken);
         if(deaths != null) scoreboard.removeObjective(deaths);
         if(mobKills != null) scoreboard.removeObjective(mobKills);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   private static List<PlayerScoreEntry> getLeaderboard(MinecraftServer server){
      ArrayList<PlayerScoreEntry> list = new ArrayList<>();
      try{
         ServerScoreboard scoreboard = server.getScoreboard();
         Objective dmgDealt = scoreboard.getObjective("arcananovum_boss_dmg_dealt");
         Objective dmgTaken = scoreboard.getObjective("arcananovum_boss_dmg_taken");
         Objective deaths = scoreboard.getObjective("arcananovum_boss_deaths");
         Objective mobKills = scoreboard.getObjective("arcananovum_boss_mob_kills");
         
         List<PlayerScoreEntry> dmgDealtScores = new ArrayList<>(scoreboard.listPlayerScores(dmgDealt));
         List<PlayerScoreEntry> dmgTakenScores = new ArrayList<>(scoreboard.listPlayerScores(dmgTaken));
         List<PlayerScoreEntry> deathsScores = new ArrayList<>(scoreboard.listPlayerScores(deaths));
         List<PlayerScoreEntry> mobKillsScores = new ArrayList<>(scoreboard.listPlayerScores(mobKills));
         
         Comparator<PlayerScoreEntry> scoreComparator = Comparator.comparingInt(PlayerScoreEntry::value);
         dmgDealtScores.sort(scoreComparator);
         dmgTakenScores.sort(scoreComparator);
         deathsScores.sort(scoreComparator);
         mobKillsScores.sort(scoreComparator);
         
         if(dmgDealtScores.isEmpty()){
            PlayerScoreEntry dummyScore = new PlayerScoreEntry("-",0, Component.literal("-"),null);
            list.add(dummyScore);
         }else{
            list.add(dmgDealtScores.get(dmgDealtScores.size()-1));
         }
         
         if(dmgTakenScores.isEmpty()){
            PlayerScoreEntry dummyScore = new PlayerScoreEntry("-",0, Component.literal("-"),null);
            list.add(dummyScore);
         }else{
            list.add(dmgTakenScores.get(dmgTakenScores.size()-1));
         }
         
         if(deathsScores.isEmpty()){
            PlayerScoreEntry dummyScore = new PlayerScoreEntry("-",0, Component.literal("-"),null);
            list.add(dummyScore);
         }else{
            list.add(deathsScores.get(deathsScores.size()-1));
         }
         
         if(mobKillsScores.isEmpty()){
            PlayerScoreEntry dummyScore = new PlayerScoreEntry("-",0, Component.literal("-"),null);
            list.add(dummyScore);
         }else{
            list.add(mobKillsScores.get(mobKillsScores.size()-1));
         }
         
      }catch(Exception e){
         e.printStackTrace();
      }
      return list;
   }
   
   private static void resetTowers(ServerLevel endWorld){
      List<SpikeFeature.EndSpike> list = SpikeFeature.getSpikesForLevel(endWorld);
      for(SpikeFeature.EndSpike spike : list){
         List<EndCrystal> nearCrystals = endWorld.getEntities(EntityType.END_CRYSTAL, new AABB(BlockPos.containing(spike.getCenterX()-10,0,spike.getCenterZ()-10).getCenter(), BlockPos.containing(spike.getCenterX()+10,255,spike.getCenterZ()+10).getCenter()), EndCrystal::showsBottom);
         for(EndCrystal nearCrystal : nearCrystals){
            nearCrystal.kill(endWorld);
         }
         
         Iterator<BlockPos> var16 = BlockPos.betweenClosed(BlockPos.containing(spike.getCenterX() - 10, spike.getHeight() - 10, spike.getCenterZ() - 10), BlockPos.containing(spike.getCenterX() + 10, spike.getHeight() + 10, spike.getCenterZ() + 10)).iterator();
   
         while(var16.hasNext()){
            BlockPos blockPos = var16.next();
            endWorld.removeBlock(blockPos, false);
         }
         

         endWorld.explode((Entity)null, (double)((float)spike.getCenterX() + 0.5F), (double)spike.getHeight(), (double)((float)spike.getCenterZ() + 0.5F), 5.0F, Level.ExplosionInteraction.NONE);
         SpikeConfiguration endSpikeFeatureConfig = new SpikeConfiguration(false, ImmutableList.of(spike), (BlockPos)null);
         Feature.END_SPIKE.place(endSpikeFeatureConfig, endWorld, endWorld.getChunkSource().getGenerator(), RandomSource.create(), BlockPos.containing(spike.getCenterX(), 45, spike.getCenterZ()));
      }
   }
   
   public static int prepBoss(ServerPlayer player){
      if(player.level().dimension() != Level.END){
         player.displayClientMessage(Component.literal("The Dragon boss must take place in The End"), false);
         return -1;
      }
      ServerLevel endWorld = player.level();
      CompoundTag dragonData = (CompoundTag) (EndDragonFight.Data.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, BorisLib.SERVER.registryAccess()), endWorld.getDragonFight().saveData()).getOrThrow());
      CompoundTag fightData = new CompoundTag();
      if(dragonData.getBooleanOr("DragonKilled", false)){
         player.displayClientMessage(Component.literal("Dragon is Dead, Commencing Respawn"), false);
         int[] exitList = dragonData.getIntArray("ExitPortalLocation").get();
         BlockPos portalPos = new BlockPos(exitList[0], exitList[1], exitList[2]);
         BlockPos blockPos2 = portalPos.above(1);
         Iterator<Direction> var4 = Direction.Plane.HORIZONTAL.iterator();
         while(var4.hasNext()){
            Direction direction = var4.next();
            EndCrystal crystal = new EndCrystal(EntityType.END_CRYSTAL, endWorld);
            crystal.setPos(Vec3.atBottomCenterOf(blockPos2.relative(direction, 3)));
            endWorld.addFreshEntityWithPassengers(crystal);
         }
         endWorld.getDragonFight().tryRespawn();
         fightData.putString("State", States.WAITING_RESPAWN.name());
         BorisLib.addTickTimerCallback(new DragonRespawnTimerCallback(player.level().getServer()));
      }else{
         player.displayClientMessage(Component.literal("Co-opting Dragon"), false);
         if(endWorld.getEntities(EntityType.END_CRYSTAL, new AABB(new BlockPos(-50, 25, -50).getCenter(), new BlockPos(50, 115, 50).getCenter()), EndCrystal::showsBottom).size() != 10){
            player.displayClientMessage(Component.literal("Tower Anomaly Detected, Resetting").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), false);
            resetTowers(endWorld);
         }
         fightData.putString("State", States.WAITING_START.name());
      }
      fightData.putString("GameMaster", player.getStringUUID());
      fightData.putInt("numPlayers",0);
      player.displayClientMessage(Component.literal(""), false);
      player.displayClientMessage(Component.literal("You are now the Game Master, please stay in The End for the duration of the fight. All errors and updates will be sent to you.").withStyle(ChatFormatting.AQUA), false);
      player.displayClientMessage(Component.literal(""), false);
      DataAccess.getWorld(Level.END, BossFightData.KEY).setBossFight(BossFights.DRAGON,fightData);
      return 0;
   }
   
   public static int abortBoss(MinecraftServer server){
      Tuple<BossFights, CompoundTag> bossFight = DataAccess.getWorld(Level.END, BossFightData.KEY).getBossFight();
      CompoundTag data = bossFight.getB();
      States state = States.valueOf(data.getStringOr("State", ""));
      ServerPlayer gm = server.getPlayerList().getPlayer(AlgoUtils.getUUID(data.getStringOr("GameMaster", "")));
      ServerLevel endWorld = server.getLevel(Level.END);
      if(gm != null){
         gm.sendSystemMessage(Component.literal("Boss Has Been Aborted :(").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
      }
   
      for(EnderDragon dragon : endWorld.getDragons()){
         dragon.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
         dragon.setHealth(200);
         dragon.setCustomName(Component.literal("Ender Dragon"));
      }
      
      if(reclaimStates != null){
         for(ReclaimState reclaimState : reclaimStates){
            reclaimState.hologramVisible = false;
         }
      }
      
      if(guardianPhantoms != null && phase == 1){
         for(Phantom phantom : guardianPhantoms){
            if(phantom != null && phantom.isAlive())
               phantom.kill(endWorld);
         }
         for(CustomBossEvent bossBar : phantomBossBars){
            if(bossBar != null){
               bossBar.removeAllPlayers();
               server.getCustomBossEvents().remove(bossBar);
            }
         }
      }
   
      if(wizards != null && (phase == 1 || phase == 2)){
         for(DragonWizardEntity wizard : wizards){
            if(wizard != null && wizard.isAlive())
               wizard.kill(endWorld);
         }
      }
   
      if(crystals != null){
         for(EndCrystal crystal : crystals){
            if(crystal != null){
               crystal.setInvulnerable(false);
            }
         }
      }
      
      List<Endermite> mites = endWorld.getEntities(EntityType.ENDERMITE, new AABB(new BlockPos(-300,25,-300).getCenter(), new BlockPos(300,255,300).getCenter()), e -> true);
      List<Shulker> shulkers = endWorld.getEntities(EntityType.SHULKER, new AABB(new BlockPos(-300,25,-300).getCenter(), new BlockPos(300,255,300).getCenter()), e -> true);
      List<Skeleton> skeletons = endWorld.getEntities(EntityType.SKELETON, new AABB(new BlockPos(-300,25,-300).getCenter(), new BlockPos(300,255,300).getCenter()), e -> true);
      List<Phantom> phantoms = endWorld.getEntities(EntityType.PHANTOM, new AABB(new BlockPos(-300,25,-300).getCenter(), new BlockPos(300,255,300).getCenter()), e -> true);
      List<Illusioner> illusioners = endWorld.getEntities(EntityType.ILLUSIONER, new AABB(new BlockPos(-300,25,-300).getCenter(), new BlockPos(300,255,300).getCenter()), e -> true);
      mites.forEach(e -> e.kill(endWorld));
      shulkers.forEach(e -> e.kill(endWorld));
      skeletons.forEach(e -> e.kill(endWorld));
      phantoms.forEach(e -> e.kill(endWorld));
      illusioners.forEach(e -> e.kill(endWorld));
   
      GameRules rules = endWorld.getGameRules();
      rules.set(GameRules.KEEP_INVENTORY, keepInventoryBefore, server);
   
      resetVariables();
      removeScoreboards(server);
      BossFight.cleanBoss(server);
      return 0;
   }
   
   public static int beginBoss(MinecraftServer server, CompoundTag data){
      States state = States.valueOf(data.getStringOr("State", ""));
      ServerPlayer gm = server.getPlayerList().getPlayer(AlgoUtils.getUUID(data.getStringOr("GameMaster", "")));
      if(state == States.WAITING_START){
         if(startTimeAnnounced){
            States.updateState(States.WAITING_ONE,server);
            if(gm != null){
               gm.sendSystemMessage(Component.literal("Beginning Boss Fight. Good Luck, Have Fun!"));
            }
            return 0;
         }else{
            if(gm != null){
               gm.sendSystemMessage(Component.literal("Make sure you have run /arcana boss announce <time> so your players know to get ready first!"));
            }
         }
      }else{
         if(gm != null){
            gm.sendSystemMessage(Component.literal("The Event State is incompatible with this command. Have you run /arcana boss start dragon?"));
         }
      }
      return -1;
   }
   
   public static int announceBoss(MinecraftServer server, CompoundTag data, String time){
      States state = States.valueOf(data.getStringOr("State", ""));
      if(state == States.WAITING_START){
         DragonDialog.announce(DragonDialog.Announcements.EVENT_PREP,server,time);
         startTimeAnnounced = true;
         return 0;
      }else{
         ServerPlayer gm = server.getPlayerList().getPlayer(AlgoUtils.getUUID(data.getStringOr("GameMaster", "")));
         if(gm != null){
            gm.sendSystemMessage(Component.literal("The Event State is incompatible with this command. Have you run /arcana boss start dragon?"));
         }
      }
      return -1;
   }
   
   public static int calcPlayers(MinecraftServer server, boolean inEnd){
      if(forcedPlayerCount > 0) return forcedPlayerCount;
      List<ServerPlayer> playerList = server.getPlayerList().getPlayers();
      int count = 0;
      if(!inEnd) return playerList.size();
      for(ServerPlayer player : playerList){
         if(!player.isSpectator() && !player.isCreative()){
            if(player.level() == server.getLevel(Level.END)){
               count++;
            }
         }
      }
      return count;
   }
   
   public static int resetDragonAbilities(MinecraftServer server, CommandSourceStack source, boolean doAbility){
      if(dragonAbilities == null) return -1;
      dragonAbilities.resetCooldowns();
      if(doAbility && dragonAbilities.doAbility(phase)) lastDragonAction = 0;
      return 1;
   }
   
   public static int forceLairAction(MinecraftServer server, CommandSourceStack source){
      if(lairActions == null) return -1;
      if(lairActions.startAction(phase)) lastLairAction = 0;
      return 1;
   }
   
   public static int bossStatus(MinecraftServer server, CommandSourceStack source){
      Tuple<BossFights, CompoundTag> bossFight = DataAccess.getWorld(Level.END, BossFightData.KEY).getBossFight();
      CompoundTag data = bossFight.getB();
      States state = States.valueOf(data.getStringOr("State", ""));
      ServerPlayer gm = server.getPlayerList().getPlayer(AlgoUtils.getUUID(data.getStringOr("GameMaster", "")));
      ArrayList<MutableComponent> msgs = new ArrayList<>();
   
      msgs.add(Component.literal(""));
      msgs.add(Component.literal("Dragon Boss Data: "));
      msgs.add(Component.literal("Game Master: "+gm.getName().getString()));
      msgs.add(Component.literal("State: "+state.name()));
      
      if(state != States.WAITING_RESPAWN){
         msgs.add(Component.literal("Num Players: "+numPlayers));
         msgs.add(Component.literal("Dragon HP: ("+dragon.getHealth()+"/"+dragon.getMaxHealth()+")"));
         msgs.add(Component.literal("Last Goon Spawn: "+lastGoonSpawn/20+" seconds ago."));
         msgs.add(Component.literal(""));
      }
      if(state == States.PHASE_ONE){
         for(Phantom guardianPhantom : guardianPhantoms){
            if(guardianPhantom != null){
               msgs.add(Component.literal("Guardian Phantom HP: ("+guardianPhantom.getHealth()+"/"+guardianPhantom.getMaxHealth()+")"));
            }
         }
      }else if(state == States.PHASE_TWO){
         int wizardCount = 0;
         int crystalCount = 0;
         for(DragonWizardEntity wizard : wizards){
            if(wizard != null && wizard.isAlive())
               wizardCount++;
         }
         for(EndCrystal crystal : crystals){
            if(crystal != null && crystal.isAlive())
               crystalCount++;
         }
   
         msgs.add(Component.literal("Wizards Alive: "+wizardCount+"/10"));
         msgs.add(Component.literal("Crystals Alive: "+crystalCount+"/10"));
      }
      msgs.add(Component.literal(""));
      
      if(state == States.PHASE_ONE || state == States.PHASE_TWO || state == States.PHASE_THREE){
         if(dragonAbilities != null){
            msgs.add(Component.literal("Dragon Ability Cooldowns: Next Check in "+ (600 - lastDragonAction)/20 + " Seconds"));
            for(Tuple<DragonAbilities.DragonAbilityTypes, Integer> cooldown : dragonAbilities.getCooldowns(phase)){
               msgs.add(Component.literal(" - "+cooldown.getA().name()+": "+cooldown.getB()/20+" Seconds"));
            }
         }
   
         msgs.add(Component.literal(""));
         msgs.add(Component.literal("Lair Action: Next Check in "+ (1100 - lastLairAction)/20 + " Seconds"));
      }
   
      for(MutableComponent msg : msgs){
         source.sendSuccess(()->msg,false);
      }
      return 0;
   }
   
   public static void playerDied(ServerPlayer player){
      if(hasDied != null)
         hasDied.add(player);
   }
   
   public static void teleportPlayer(ServerPlayer player, boolean override){
      ServerLevel endWorld = player.level().getServer().getLevel(Level.END);
      if(hasDied.contains(player) || override){
         player.teleport(new TeleportTransition(endWorld, new Vec3(100.5,51,0.5), Vec3.ZERO, 90, 0, TeleportTransition.PLACE_PORTAL_TICKET));
         MobEffectInstance res = new MobEffectInstance(MobEffects.RESISTANCE, 20*30, 4, false, false, true);
         player.addEffect(res);
         MutableComponent msg = Component.literal("")
               .append(Component.literal("You are ").withStyle(ChatFormatting.AQUA))
               .append(Component.literal("protected").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
               .append(Component.literal(" from damage for 30 seconds!").withStyle(ChatFormatting.AQUA));
         player.displayClientMessage(msg, false);
         hasDied.remove(player);
      }else{
         MutableComponent msg = Component.literal("")
               .append(Component.literal("You only get to teleport after dying.").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
         player.displayClientMessage(msg, false);
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
      private Vec3 pos;
      private ServerLevel endWorld;
      private ServerPlayer player;
      private int solveCooldown;
      private Vec3 shieldPos;
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
      
      public ReclaimState(Vec3 pos, ServerLevel endWorld){
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
         TextDisplayElement line1 = new TextDisplayElement(Component.literal("Click to Attempt").withStyle(ChatFormatting.YELLOW));
         TextDisplayElement line2 = new TextDisplayElement(Component.literal("Tower Reclamation").withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE, ChatFormatting.UNDERLINE));
         ItemDisplayElement icon = new ItemDisplayElement(Items.END_CRYSTAL);
         InteractionElement click = new InteractionElement(new VirtualElement.InteractionHandler(){
            public void click(ServerPlayer player){
               if(reclaimState.getSolveCooldown() == 0 && reclaimState.hologramVisible){
                  PuzzleGui gui = new PuzzleGui(MenuType.GENERIC_9x6,player,reclaimState);
                  gui.buildPuzzle();
                  gui.open();
                  reclaimState.setPlayer(player);
                  reclaimState.setSolveCooldown(1200);
               }
            }
            
            @Override
            public void interact(ServerPlayer player, InteractionHand hand){
               click(player);
            }
            
            @Override
            public void interactAt(ServerPlayer player, InteractionHand hand, Vec3 pos){
               click(player);
            }
            
            @Override
            public void attack(ServerPlayer player){
               click(player);
            }
         });
         click.setSize(3,3);
         TextDisplayElement cooldownText = new TextDisplayElement(Component.literal("On Cooldown - "+(solveCooldown/20+1)+" Seconds").withStyle(ChatFormatting.AQUA));
         
         
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
                     cdText.setText(Component.literal("On Cooldown - "+(state.solveCooldown/20+1)+" Seconds").withStyle(ChatFormatting.AQUA));
                     cdText.setInvisible(false);
                  }
               }else{
                  cdText.setInvisible(true);
                  this.removeElement(cdText);
               }
            }
         };
         line1.setOffset(new Vec3(0,1.75,0));
         line2.setOffset(new Vec3(0,1.5,0));
         icon.setOffset(new Vec3(0,1,0));
         cooldownText.setOffset(new Vec3(0,0.25,0));
         click.setOffset(new Vec3(0,1,0));
         
         line1.setBillboardMode(Display.BillboardConstraints.VERTICAL);
         line2.setBillboardMode(Display.BillboardConstraints.VERTICAL);
         icon.setBillboardMode(Display.BillboardConstraints.VERTICAL);
         cooldownText.setBillboardMode(Display.BillboardConstraints.VERTICAL);
         
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
               if(player != null && player.distanceToSqr(pos.x,pos.y+1,pos.z) > 25){
                  player.teleportTo(pos.x,pos.y+1,pos.z);
                  player.setDeltaMovement(0,0,0);
                  player.connection.send(new ClientboundSetEntityMotionPacket(player));
               }
               if(player != null && ticks % 5 == 0){
                  MobEffectInstance res = new MobEffectInstance(MobEffects.RESISTANCE, 25, 4, false, true, true);
                  player.addEffect(res);
                  endWorld.sendParticles(ParticleTypes.CLOUD,player.getX(),player.getY(),player.getZ(),5,0.25,0.25,0.25,0);
               }
               if(animTicks % 150 == 0){
                  ArcanaEffectUtils.dragonReclaimTowerCircle(endWorld,pos.add(0,-1,0),8000,1);
               }
            }
            if(shieldPos != null && shieldTicks > 0){
               if(shieldTicks % 5 == 0){
                  List<Entity> entities = endWorld.getEntities(player,new AABB(shieldPos.x+5,shieldPos.y+5,shieldPos.z+5,shieldPos.x-5,shieldPos.y-5,shieldPos.z-5), e -> (e instanceof LivingEntity && !(e instanceof ServerPlayer)));
                  for(Entity entity : entities){
                     if(entity instanceof LivingEntity living){
                        Vec3 vec = new Vec3(shieldPos.x()-entity.getX(),0,shieldPos.z()-entity.getZ());
                        vec = vec.normalize().scale(3);
                        living.setDeltaMovement(-vec.x,1,-vec.z);
                     }
                  }
               }
               if(shieldTicks % 20 == 0){
                  List<ServerPlayer> inRangePlayers = endWorld.getPlayers(p -> p.distanceToSqr(shieldPos) <= 5*5);
                  for(ServerPlayer plyr: inRangePlayers){
                     MobEffectInstance res = new MobEffectInstance(MobEffects.RESISTANCE, 20 * 5 + 5, 0, false, true, true);
                     MobEffectInstance regen = new MobEffectInstance(MobEffects.REGENERATION, 20 * 5 + 5, 2, false, true, true);
                     plyr.addEffect(res);
                     plyr.addEffect(regen);
                  }
               }
            }
         }catch(Exception e){
            e.printStackTrace();
         }
      }
      
      public void playerSolved(){
         state = 2;
         player.teleportTo(pos.x,pos.y+1,pos.z);
         player.setDeltaMovement(0,0,0);
         player.connection.send(new ClientboundSetEntityMotionPacket(player));
         hologramVisible = false;
         towerGui = new TowerGui(player,this);
         towerGui.buildGui();
         towerGui.open();
         animTicks = 0;
         ArcanaEffectUtils.dragonReclaimTowerCircle(endWorld,pos.add(0,-1,0),8000,1);
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
         
         List<ServerPlayer> inRangePlayers = endWorld.getPlayers(p -> p.distanceToSqr(new Vec3(pos.x()+.5,pos.y()-1,pos.z()+.5)) <= 8.5*8.5);
         for(ServerPlayer player : inRangePlayers){
            BlockPos target = BlockPos.containing(pos.x()+.5,pos.y()-1,pos.z()+.5);
            BlockPos playerPos = player.blockPosition();
            Vec3 vec = new Vec3(target.getX()-playerPos.getX(),0,target.getZ()-playerPos.getZ());
            vec = vec.normalize().scale(5);
         
            player.setDeltaMovement(-vec.x,1,-vec.z);
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
         
            player.displayClientMessage(Component.literal("The Tower's Explosion Launches You!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC),true);
         }
         // Explosion Particles / Sounds
         endWorld.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x()+.5, pos.y()-1, pos.z()+.5, 20, 2, 2, 2,0.5);
         // Tiered removal of obsidian
         for(BlockPos block : BlockPos.withinManhattan(BlockPos.containing(pos), 12, 12, 12)){
            boolean destroy = true;
            double dist = Math.sqrt(block.distToCenterSqr(pos));
            if(!block.closerToCenterThan(pos, 5)){
               destroy = Math.random() < (-.01 * ((dist - 5) * (dist - 5)) + 1);
            }
            if(destroy)
               endWorld.destroyBlock(block,false);
         }
      
         endWorld.explode(dragon,null,null,pos.x(),pos.y(),pos.z(),10,true, Level.ExplosionInteraction.NONE);
         hologramVisible = false;
         hologram.destroy();
         state = 3;
      }
      
      public void castShield(){
         Vec3 end = player.getEyePosition().add(player.getLookAngle().normalize().scale(100));
         BlockHitResult result = endWorld.clip(new ClipContext(player.getEyePosition(),end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,player));
         shieldPos = result.getLocation();
         shieldTicks = 300;
         ArcanaEffectUtils.dragonReclaimTowerShield(endWorld,shieldPos,0);
      }
      
      public void castLaser(){ // TODO: Update all this
         Vec3 end = player.getEyePosition().add(player.getLookAngle().normalize().scale(75));
         BlockHitResult result = endWorld.clip(new ClipContext(player.getEyePosition(),end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,player));
         Vec3 hit = result.getLocation();
         ArcanaEffectUtils.longDistLine(endWorld,player.position().add(0,.7,0),hit, ParticleTypes.GLOW,(int)(2*hit.length()),1,0.1,0);
         List<Entity> entities = endWorld.getEntities(player,new AABB(hit.x+2,hit.y+2,hit.z+2,hit.x-2,hit.y-2,hit.z-2), e -> (e instanceof LivingEntity && !(e instanceof ServerPlayer)));
   
         Scoreboard scoreboard = endWorld.getServer().getScoreboard();
         ScoreAccess scoreboardPlayerScore = scoreboard.getOrCreatePlayerScore(ScoreHolder.fromGameProfile(player.getGameProfile()),scoreboard.getObjective("arcananovum_boss_dmg_dealt"));
         
         for(Entity entity : entities){
            if(entity instanceof LivingEntity living){
               if(scoreboardPlayerScore != null)
                  scoreboardPlayerScore.set(scoreboardPlayerScore.get() + 50);
               living.hurtServer(endWorld, endWorld.damageSources().playerAttack(player),5f);
            }
         }
         if(MathUtils.distToLine(dragon.position(),player.position(),hit) < 10){
            float damage = Math.min(100,15+numPlayers*3);
            if(scoreboardPlayerScore != null)
               scoreboardPlayerScore.set(scoreboardPlayerScore.get() + (int)(damage*10));
            dragon.hurtServer(endWorld, endWorld.damageSources().playerAttack(player),damage);
         }
      }
   
      public ServerPlayer getPlayer(){
         return player;
      }
   
      public int getSolveCooldown(){
         return solveCooldown;
      }
   
      public int getState(){
         return state;
      }
   
      public Vec3 getPos(){
         return pos;
      }
   
      public void setPlayer(ServerPlayer player){
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
         Tuple<BossFights, CompoundTag> bossFight = DataAccess.getWorld(Level.END, BossFightData.KEY).getBossFight();
         if(bossFight.getA() == BossFights.DRAGON){
            bossFight.getB().putString("State", state.name());
         }else{
            devPrint("Boss fight not valid");
         }
      }
   }
}
