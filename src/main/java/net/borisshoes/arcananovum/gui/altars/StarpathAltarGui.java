package net.borisshoes.arcananovum.gui.altars;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity;
import net.borisshoes.arcananovum.gui.WatchedGui;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class StarpathAltarGui  extends SimpleGui implements WatchedGui {
   private final StarpathAltarBlockEntity blockEntity;
   
   public StarpathAltarGui(ServerPlayerEntity player, StarpathAltarBlockEntity blockEntity){
      super(ScreenHandlerType.HOPPER, player, false);
      this.blockEntity = blockEntity;
      
      setTitle(Text.literal("Starpath Altar"));
   }
   
   private void teleport(){
      if(!(blockEntity.getWorld() instanceof ServerWorld serverWorld)) return;
      Box teleportBox = (new Box(blockEntity.getPos().add(0,2,0))).expand(5,2,5);
      List<LivingEntity> targets = blockEntity.getWorld().getEntitiesByClass(LivingEntity.class,teleportBox,(e)->true);
      
      int tries = 0; int range = 4; int height = 2;
      ArrayList<BlockPos> locations;
      do{
         locations = SpawnPile.makeSpawnLocations(targets.size(),range, blockEntity.getTargetCoords().getY()+height, serverWorld, blockEntity.getTargetCoords());
         tries++; range++; height += 16; // Expand search area
      }while(locations.size() != targets.size() && tries < 5);
      if(locations.size() != targets.size()){
         player.sendMessage(Text.literal("The teleport goes awry, you are shunted uncontrollably!").formatted(Formatting.RED,Formatting.ITALIC),false);
         locations = new ArrayList<>();
         for(int i = 0; i < targets.size(); i++){
            locations.add(blockEntity.getTargetCoords());
         }
      }
      
      for(int i = 0; i < targets.size(); i++){
         LivingEntity target = targets.get(i);
         BlockPos location = locations.get(i);
         target.teleport(serverWorld, location.getX() + 0.5, location.getY(), location.getZ() + 0.5, Set.of(), target.getYaw(), target.getPitch());
         ParticleEffectUtils.recallTeleport(serverWorld,target.getPos());
         
         if(target instanceof ServerPlayerEntity p && Math.sqrt(blockEntity.getPos().getSquaredDistance(blockEntity.getTargetCoords())) >= 100000){
            ArcanaAchievements.grant(p,ArcanaAchievements.FAR_FROM_HOME.id);
         }
         if((target instanceof TameableEntity tameable && tameable.isOwner(player) || (target instanceof ServerPlayerEntity && target != player)) && targets.contains(player)){
            ArcanaAchievements.grant(player,ArcanaAchievements.ADVENTURING_PARTY.id);
         }
      }
      SoundUtils.playSound(serverWorld,blockEntity.getTargetCoords(), SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.BLOCKS, 2, 1.5f);
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      if(index == 2){
         blockEntity.openTargetGui(player);
      }else if(index == 4){
         if(blockEntity.getCooldown() <= 0){
            if(MiscUtils.removeItems(player,Items.ENDER_EYE,blockEntity.calculateCost())){
               ParticleEffectUtils.starpathAltarAnim(player.getServerWorld(),blockEntity.getPos().toCenterPos());
               blockEntity.setActiveTicks(500);
               blockEntity.resetCooldown();
               Arcananovum.addTickTimerCallback(player.getServerWorld(), new GenericTimer(500, new TimerTask() {
                  @Override
                  public void run(){
                     teleport();
                     PLAYER_DATA.get(player).addXP(1000);
                  }
               }));
               close();
            }else{
               player.sendMessage(Text.literal("You do not have enough Eyes to power the Altar").formatted(Formatting.RED,Formatting.ITALIC),false);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH,1,.5f);
               close();
            }
         }else{
            player.sendMessage(Text.literal("The Altar is on Cooldown").formatted(Formatting.RED,Formatting.ITALIC),false);
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH,1,.5f);
            close();
         }
      }
      return true;
   }
   
   @Override
   public void onTick(){
      build();
   }
   
   public void build(){
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         setSlot(i,new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.literal("Starpath Altar").formatted(Formatting.DARK_PURPLE)));
      }
      
      GuiElementBuilder cooldownItem = new GuiElementBuilder(Items.CLOCK).hideFlags();
      if(blockEntity.getCooldown() <= 0){
         cooldownItem.setName((Text.literal("")
               .append(Text.literal("Altar Ready").formatted(Formatting.AQUA))));
      }else{
         cooldownItem.setName((Text.literal("")
               .append(Text.literal("Altar Recharging").formatted(Formatting.DARK_AQUA))));
         cooldownItem.addLoreLine((Text.literal("")
               .append(Text.literal((blockEntity.getCooldown()/20)+" Seconds").formatted(Formatting.GRAY))));
      }
      setSlot(0,cooldownItem);
      
      BlockPos target = blockEntity.getTargetCoords();
      GuiElementBuilder locationItem = new GuiElementBuilder(Items.FILLED_MAP).hideFlags();
      locationItem.setName((Text.literal("")
            .append(Text.literal("Target Location").formatted(Formatting.GOLD))));
      locationItem.addLoreLine((Text.literal("")
            .append(Text.literal("X: "+target.getX()).formatted(Formatting.YELLOW))));
      locationItem.addLoreLine((Text.literal("")
            .append(Text.literal("Y: "+target.getY()).formatted(Formatting.YELLOW))));
      locationItem.addLoreLine((Text.literal("")
            .append(Text.literal("Z: "+target.getZ()).formatted(Formatting.YELLOW))));
      locationItem.addLoreLine((Text.literal("")
            .append(Text.literal("").formatted(Formatting.YELLOW))));
      locationItem.addLoreLine((Text.literal("")
            .append(Text.literal("Click to Change Target").formatted(Formatting.YELLOW))));
      setSlot(2,locationItem);
      
      int cost = blockEntity.calculateCost();
      int stacks = cost / 64;
      int leftover = cost % 64;
      GuiElementBuilder activateItem = new GuiElementBuilder(Items.ENDER_EYE);
      activateItem.setName((Text.literal("")
            .append(Text.literal("Activate Altar").formatted(Formatting.LIGHT_PURPLE))));
      activateItem.addLoreLine((Text.literal("")
            .append(Text.literal("Click to travel the Star's Path").formatted(Formatting.DARK_PURPLE))));
      activateItem.addLoreLine((Text.literal("")
            .append(Text.literal("").formatted(Formatting.DARK_PURPLE))));
      activateItem.addLoreLine((Text.literal("")
            .append(Text.literal("This Journey Costs: ").formatted(Formatting.AQUA))));
      activateItem.addLoreLine((Text.literal("")
            .append(Text.literal(cost+" Eye"+(cost != 1 ? "s" : "")+" of Ender").formatted(Formatting.DARK_AQUA))));
      if(cost > 64){
         activateItem.addLoreLine((Text.literal("")
               .append(Text.literal(stacks+" Stacks + "+leftover).formatted(Formatting.DARK_AQUA))));
      }
      setSlot(4,activateItem);
   }
   
   
   @Override
   public void close(){
      super.close();
   }
   
   @Override
   public BlockEntity getBlockEntity(){
      return blockEntity;
   }
   
   @Override
   public SimpleGui getGui(){
      return this;
   }
}
