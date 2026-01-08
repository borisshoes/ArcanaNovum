package net.borisshoes.arcananovum.datastorage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.borisshoes.arcananovum.bosses.BossFights;
import net.borisshoes.borislib.datastorage.DataKey;
import net.borisshoes.borislib.datastorage.DataRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.bosses.BossFights.BOSS_FIGHTS_CODEC;

public class BossFightData {
   
   public Tuple<BossFights, CompoundTag> bossFight;
   private final ResourceKey<Level> worldKey;
   
   // Codec for the bossFight Tuple (can be null)
   private static final Codec<Tuple<BossFights, CompoundTag>> BOSS_FIGHT_TUPLE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
         BOSS_FIGHTS_CODEC.fieldOf("boss").forGetter(Tuple::getA),
         CompoundTag.CODEC.fieldOf("data").forGetter(Tuple::getB)
   ).apply(instance, Tuple::new));
   
   public static final Codec<BossFightData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
         Level.RESOURCE_KEY_CODEC.fieldOf("worldKey").forGetter(data -> data.worldKey),
         BOSS_FIGHT_TUPLE_CODEC.optionalFieldOf("bossFight").forGetter(data ->
               data.bossFight == null ? java.util.Optional.empty() : java.util.Optional.of(data.bossFight))
   ).apply(instance, (worldKey, bossFight) -> {
      BossFightData data = new BossFightData(worldKey);
      bossFight.ifPresent(fight -> data.bossFight = fight);
      return data;
   }));
   
   public static final DataKey<BossFightData> KEY = DataRegistry.register(DataKey.ofWorld(Identifier.fromNamespaceAndPath(MOD_ID, "boss_fight"), CODEC, BossFightData::new));
   
   public BossFightData(ResourceKey<Level> worldKey){
      this.worldKey = worldKey;
   }
   
   public boolean setBossFight(BossFights boss, CompoundTag data){
      if(bossFight == null || bossFight.getA() == boss){
         bossFight = new Tuple<>(boss,data);
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
         return true;
      }
   }
   
   public Tuple<BossFights, CompoundTag> getBossFight(){
      return bossFight;
   }
}
