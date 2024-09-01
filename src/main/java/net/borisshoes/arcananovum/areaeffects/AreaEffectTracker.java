package net.borisshoes.arcananovum.areaeffects;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public abstract class AreaEffectTracker {
   
   protected final String type;
   protected final List<AreaEffectSource> sources;
   protected final Identifier id;
   
   public AreaEffectTracker(String type){
      this.type = type;
      this.sources = new ArrayList<>();
      this.id = Identifier.of(MOD_ID,type);
   }
   
   public abstract void onTick(MinecraftServer server);
   
   public abstract void addSource(AreaEffectSource source);
   
   public String getType(){
      return type;
   }
   
   public Identifier getId(){
      return id;
   }
}
