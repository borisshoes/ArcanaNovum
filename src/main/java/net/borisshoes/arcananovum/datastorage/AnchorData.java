package net.borisshoes.arcananovum.datastorage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.borisshoes.borislib.datastorage.DataKey;
import net.borisshoes.borislib.datastorage.DataRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class AnchorData {
   
   private final ResourceKey<Level> worldKey;
   public final List<BlockPos> anchors = new ArrayList<>();
   
   public static final Codec<AnchorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
         Level.RESOURCE_KEY_CODEC.fieldOf("worldKey").forGetter(data -> data.worldKey),
         BlockPos.CODEC.listOf().fieldOf("anchors").forGetter(data -> data.anchors)
   ).apply(instance, (worldKey, anchors) -> {
      AnchorData data = new AnchorData(worldKey);
      data.anchors.addAll(anchors);
      return data;
   }));
   
   public static final DataKey<AnchorData> KEY = DataRegistry.register(DataKey.ofWorld(Identifier.fromNamespaceAndPath(MOD_ID, "anchors"), CODEC,AnchorData::new));
   
   public AnchorData(ResourceKey<Level> worldKey){
      this.worldKey = worldKey;
   }
   
   public List<BlockPos> getAnchors(){
      return anchors;
   }
   
   public boolean addAnchor(BlockPos anchor){
      if(anchors.contains(anchor)) return false;
      return anchors.add(anchor);
   }
   
   public boolean removeAnchor(BlockPos anchor){
      if(!anchors.contains(anchor)) return false;
      return anchors.remove(anchor);
   }
}
