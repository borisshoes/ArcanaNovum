package net.borisshoes.arcananovum.datastorage;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.datastorage.DataKey;
import net.borisshoes.borislib.datastorage.DataRegistry;
import net.borisshoes.borislib.datastorage.StorableData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;

import java.util.ArrayList;
import java.util.List;

public class AnchorData implements StorableData {
   
   private final ResourceKey<Level> worldKey;
   public final List<BlockPos> anchors = new ArrayList<>();
   
   public static final DataKey<AnchorData> KEY = DataRegistry.register(DataKey.ofWorld(ArcanaRegistry.arcanaId("anchors"), AnchorData::new));
   
   public AnchorData(ResourceKey<Level> worldKey){
      this.worldKey = worldKey;
   }
   
   @Override
   public void read(ValueInput view){
      this.anchors.clear();
      view.listOrEmpty("anchors", BlockPos.CODEC).forEach(this.anchors::add);
   }
   
   @Override
   public void writeNbt(CompoundTag tag){
      ListTag anchorList = new ListTag();
      for(BlockPos pos : anchors){
         BlockPos.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, BorisLib.SERVER.registryAccess()), pos).result().ifPresent(anchorList::add);
      }
      tag.put("anchors", anchorList);
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
