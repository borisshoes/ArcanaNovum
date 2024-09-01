package net.borisshoes.arcananovum.research;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class EffectResearchTask extends ResearchTask {
   
   private final RegistryEntry<StatusEffect> effect;
   
   public EffectResearchTask(String id, RegistryEntry<StatusEffect> effect, Text name, Text[] description, ItemStack displayItem){
      super(id, Type.CUSTOM_EVENT, name, description, displayItem);
      this.effect = effect;
   }
   
   public EffectResearchTask(String id, RegistryEntry<StatusEffect> effect, Text name, Text[] description, ItemStack displayItem, RegistryKey<ResearchTask>... prerequisites){
      super(id, Type.CUSTOM_EVENT, name, description, displayItem, prerequisites);
      this.effect = effect;
   }
   
   public RegistryEntry<StatusEffect> getEffect(){
      return effect;
   }
   
   @Override
   public boolean isAcquired(ServerPlayerEntity player){
      return PLAYER_DATA.get(player).completedResearchTask(id);
   }
}
