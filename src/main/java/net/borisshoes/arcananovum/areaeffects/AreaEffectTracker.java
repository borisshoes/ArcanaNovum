package net.borisshoes.arcananovum.areaeffects;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public abstract class AreaEffectTracker {
   
   protected final String type;
   protected final List<AreaEffectSource> sources;
   protected final Identifier id;
   
   public AreaEffectTracker(String type){
      this.type = type;
      this.sources = new ArrayList<>();
      this.id = ArcanaRegistry.arcanaId(type);
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
