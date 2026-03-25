package net.borisshoes.arcananovum.events.special;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.borislib.events.Event;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;

public class CeptyusStartEvent extends Event {
   public static final Identifier ID = ArcanaRegistry.arcanaId("ceptyus_start");
   
   private final ServerPlayer player;
   private boolean sentTinker = false;
   private boolean sentInvestigate = false;
   
   public CeptyusStartEvent(ServerPlayer player){
      super(ID, 12000);
      this.player = player;
   }
   
   public ServerPlayer getPlayer(){
      return player;
   }
   
   @Override
   public void tick(){
      super.tick();
      
      if(!this.sentInvestigate && this.timeAlive % 20 == 0){
         Structure structure = player.level().structureManager().registryAccess().lookupOrThrow(Registries.STRUCTURE).getValue(BuiltinStructures.ANCIENT_CITY);
         StructureStart start = player.level().structureManager().getStructureAt(player.blockPosition(),structure);
         if(!(start.isValid() && start.canBeReferenced())) return;
         for(StructurePiece piece : start.getPieces()){
            if(!(piece.getBoundingBox().isInside(player.blockPosition()))) continue;
            if(!(piece instanceof PoolElementStructurePiece poolPiece && poolPiece.getElement() instanceof SinglePoolElement elem)) continue;
            Identifier pieceId = elem.getTemplateLocation();
            String[] split = pieceId.getPath().split("/");
            if(split[split.length-1].contains("city_center")){
               player.sendSystemMessage(Component.literal("\n[Investigate the Portal]").withStyle(s ->
                     s.withBold(true).withColor(ChatFormatting.DARK_AQUA).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action c_investigate"))),false);
               this.sentInvestigate = true;
               this.timeAlive = 0;
               break;
            }
         }
         
      }
   }
   
   public boolean sentInvestigate(){
      return sentInvestigate;
   }
   
   public boolean sentTinker(){
      return sentTinker;
   }
   
   public void setSentTinker(){
      this.sentTinker = true;
   }
}
