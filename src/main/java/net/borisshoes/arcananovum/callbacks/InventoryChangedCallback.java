package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.research.ObtainResearchTask;
import net.borisshoes.arcananovum.research.ResearchTask;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

public class InventoryChangedCallback {
   
   public static void onSlotUpdate(ServerPlayerEntity player, PlayerInventory inventory, ItemStack stack){
      
      // Obtain Research Task Check
      for(Map.Entry<RegistryKey<ResearchTask>, ResearchTask> entry : ResearchTasks.RESEARCH_TASKS.getEntrySet()){
         ResearchTask task = entry.getValue();
         if(task instanceof ObtainResearchTask obtainTask){
            if(stack.isOf(obtainTask.getItem())){
               ArcanaNovum.data(player).setResearchTask(entry.getKey(), true);
            }
         }
      }
      
      if(EnchantmentHelper.getLevel(MiscUtils.getEnchantment(Enchantments.SILK_TOUCH),stack) > 0){
         ArcanaNovum.data(player).setResearchTask(ResearchTasks.OBTAIN_SILK_TOUCH, true);
      }
   }
}
