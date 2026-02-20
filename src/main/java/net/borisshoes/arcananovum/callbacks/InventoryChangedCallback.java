package net.borisshoes.arcananovum.callbacks;

import com.mojang.datafixers.util.Either;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.research.ObtainResearchTask;
import net.borisshoes.arcananovum.research.ResearchTask;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Map;

public class InventoryChangedCallback {
   
   public static void onSlotUpdate(ServerPlayer player, Inventory inventory, ItemStack stack){
      
      // Obtain Research Task Check
      for(Map.Entry<ResourceKey<ResearchTask>, ResearchTask> entry : ResearchTasks.RESEARCH_TASKS.entrySet()){
         ResearchTask task = entry.getValue();
         if(task instanceof ObtainResearchTask obtainTask){
            Either<Item, TagKey<Item>> item = obtainTask.getItem();
            if((item.left().isPresent() && stack.is(item.left().get())) || (item.right().isPresent() && stack.is(item.right().get()))){
               ArcanaNovum.data(player).setResearchTask(entry.getKey(), true);
            }
         }
      }
      
      if(EnchantmentHelper.getItemEnchantmentLevel(MinecraftUtils.getEnchantment(Enchantments.SILK_TOUCH),stack) > 0){
         ArcanaNovum.data(player).setResearchTask(ResearchTasks.OBTAIN_SILK_TOUCH, true);
      }
   }
}
