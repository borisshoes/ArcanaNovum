package net.borisshoes.arcananovum.callbacks;

import io.github.ladysnake.pal.VanillaAbilities;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.ContinuumAnchor;
import net.borisshoes.arcananovum.blocks.GeomanticSteleBlockEntity;
import net.borisshoes.arcananovum.blocks.ItineranteurBlockEntity;
import net.borisshoes.arcananovum.bosses.BossFights;
import net.borisshoes.arcananovum.bosses.dragon.DragonBossFight;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.datastorage.BossFightData;
import net.borisshoes.arcananovum.events.NulMementoEvent;
import net.borisshoes.arcananovum.items.LevitationHarness;
import net.borisshoes.arcananovum.items.NulMemento;
import net.borisshoes.arcananovum.items.QuiverItem;
import net.borisshoes.arcananovum.items.ShulkerCore;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.borislib.events.Event;
import net.borisshoes.borislib.timers.TickTimerCallback;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.*;

import static net.borisshoes.arcananovum.ArcanaNovum.ACTIVE_ARCANA_BLOCKS;
import static net.borisshoes.arcananovum.ArcanaRegistry.DRAGON_TOWER_ABILITY;
import static net.borisshoes.arcananovum.ArcanaRegistry.LEVITATION_HARNESS_ABILITY;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.BOSS_FIGHT;

public class TickCallback {
   public static void onTick(MinecraftServer server){
      try{
         bossTickCheck(server);
         
         ArrayList<Tuple<BlockEntity, ArcanaBlockEntity>> toRemoveBlocks = new ArrayList<>();
         for(Map.Entry<Tuple<BlockEntity, ArcanaBlockEntity>, Integer> pair : ACTIVE_ARCANA_BLOCKS.entrySet()){
            if(pair.getValue()-1 > 0){
               ACTIVE_ARCANA_BLOCKS.put(pair.getKey(),pair.getValue()-1);
            }else{
               toRemoveBlocks.add(pair.getKey());
            }
         }
         toRemoveBlocks.forEach(ACTIVE_ARCANA_BLOCKS::remove);
         
         List<ServerPlayer> players = server.getPlayerList().getPlayers();
         players.forEach(p -> ArcanaNovum.data(p).tick(p));
         
         HashMap<ServerPlayer,Float> shieldTotals = new HashMap<>();
         for(TickTimerCallback callback : BorisLib.SERVER_TIMER_CALLBACKS){
            if(callback instanceof ShieldTimerCallback st){
               if(shieldTotals.containsKey(st.getPlayer())){
                  shieldTotals.put(st.getPlayer(),shieldTotals.get(st.getPlayer())+st.getHearts());
                  if(shieldTotals.get(st.getPlayer()) >= 200 && st.getPlayer().getAbsorptionAmount() >= 200) ArcanaAchievements.grant(st.getPlayer(),ArcanaAchievements.BUILT_LIKE_TANK);
               }else{
                  shieldTotals.put(st.getPlayer(),st.getHearts());
               }
            }
         }
         
         ContinuumAnchor.updateLoadedChunks(server);
         ItineranteurBlockEntity.tickZones();
         GeomanticSteleBlockEntity.tickZones();
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   private static void bossTickCheck(MinecraftServer server){
      for(ServerLevel world : server.getAllLevels()){
         Tuple<BossFights, CompoundTag> fight = DataAccess.getWorld(world.dimension(), BossFightData.KEY).getBossFight();
         if(fight != null){
            if(fight.getA() == BossFights.DRAGON){
               DragonBossFight.tick(server,fight.getB());
            }
         }
      }
   }
}
