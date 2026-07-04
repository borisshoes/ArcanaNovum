package net.borisshoes.arcananovum.datastorage;

import com.mojang.datafixers.util.Pair;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.bosses.BossFights;
import net.borisshoes.borislib.datastorage.DataKey;
import net.borisshoes.borislib.datastorage.DataRegistry;
import net.borisshoes.borislib.datastorage.StorableData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;

public class BossFightData implements StorableData {
   
   public Pair<BossFights, CompoundTag> bossFight;
   private final ResourceKey<Level> worldKey;
   private Runnable dirtyCallback = () -> {};
   
   public static final DataKey<BossFightData> KEY = DataRegistry.register(DataKey.ofWorld(ArcanaRegistry.arcanaId("boss_fight"), BossFightData::new));
   
   public BossFightData(ResourceKey<Level> worldKey){
      this.worldKey = worldKey;
   }
   
   @Override
   public void setDirtyCallback(Runnable callback){
      this.dirtyCallback = callback == null ? () -> {} : callback;
   }
   
   @Override
   public void markDirty(){
      dirtyCallback.run();
   }
   
   @Override
   public void read(ValueInput view){
      if(view.getBooleanOr("hasBossFight", false)){
         view.read("bossFight", CompoundTag.CODEC).ifPresent(bossFightTag -> {
            String bossLabel = bossFightTag.getStringOr("boss", "");
            BossFights boss = BossFights.fromLabel(bossLabel);
            if(boss != null){
               CompoundTag data = bossFightTag.getCompoundOrEmpty("data");
               this.bossFight = Pair.of(boss, data);
            }
         });
      }
   }
   
   @Override
   public void writeNbt(CompoundTag tag){
      tag.putBoolean("hasBossFight", bossFight != null);
      if(bossFight != null){
         CompoundTag bossFightTag = new CompoundTag();
         bossFightTag.putString("boss", bossFight.getFirst().label);
         bossFightTag.put("data", bossFight.getSecond());
         tag.put("bossFight", bossFightTag);
      }else{
         tag.remove("bossFight");
      }
   }
   
   public boolean setBossFight(BossFights boss, CompoundTag data){
      if(bossFight == null || bossFight.getFirst() == boss){
         bossFight = Pair.of(boss, data);
         markDirty();
         return true;
      }else{
         return false;
      }
   }
   
   public boolean removeBossFight(){
      if(bossFight == null){
         return false;
      }else{
         bossFight = null;
         markDirty();
         return true;
      }
   }
   
   public Pair<BossFights, CompoundTag> getBossFight(){
      if(bossFight != null) markDirty();
      return bossFight;
   }
}
