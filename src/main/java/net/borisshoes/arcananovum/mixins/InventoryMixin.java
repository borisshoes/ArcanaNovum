package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
   
   @Shadow
   @Final
   public Player player;
   
   @Unique
   private final Map<Integer, ItemStack> anchoredItems = new HashMap<>();
   
   @Inject(method = "dropAll", at = @At("HEAD"))
   private void arcananovum$fateAnchorSave(CallbackInfo ci){
      Inventory inv = (Inventory) (Object) this;
      for(int i = 0; i < inv.getContainerSize(); i++){
         ItemStack itemStack = (ItemStack) inv.getItem(i);
         if(!itemStack.isEmpty() && (player.isAlive() || EnchantmentHelper.getItemEnchantmentLevel(MinecraftUtils.getEnchantment(ArcanaRegistry.FATE_ANCHOR), itemStack) > 0)){
            anchoredItems.put(i, itemStack);
            inv.setItem(i, ItemStack.EMPTY);
         }
      }
   }
   
   @Inject(method = "dropAll", at = @At("RETURN"))
   private void arcananovum$fateAnchorRestore(CallbackInfo ci){
      Inventory inv = (Inventory) (Object) this;
      for(Map.Entry<Integer, ItemStack> entry : anchoredItems.entrySet()){
         inv.setItem(entry.getKey(), entry.getValue());
      }
      anchoredItems.clear();
   }
}
