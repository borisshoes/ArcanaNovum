package net.borisshoes.arcananovum.research;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;

public class EffectResearchTask extends ResearchTask {
   
   private final Holder<MobEffect> effect;
   
   public EffectResearchTask(String id, Holder<MobEffect> effect, Component name, Component[] description, ItemStack displayItem){
      super(id, Type.CUSTOM_EVENT, name, description, displayItem);
      this.effect = effect;
   }
   
   public EffectResearchTask(String id, Holder<MobEffect> effect, Component name, Component[] description, ItemStack displayItem, ResourceKey<ResearchTask>... prerequisites){
      super(id, Type.CUSTOM_EVENT, name, description, displayItem, prerequisites);
      this.effect = effect;
   }
   
   public Holder<MobEffect> getEffect(){
      return effect;
   }
   
   @Override
   public boolean isAcquired(ServerPlayer player){
      return ArcanaNovum.data(player).completedResearchTask(id);
   }
}
