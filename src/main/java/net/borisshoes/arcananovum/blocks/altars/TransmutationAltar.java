package net.borisshoes.arcananovum.blocks.altars;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TransmutationAltar extends MagicItem {
   
   public TransmutationAltar(){
      id = "transmutation_altar";
      name = "Transmutation Altar";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY,ArcaneTome.TomeFilter.BLOCKS,ArcaneTome.TomeFilter.ALTARS};
      vanillaItem = Items.DIAMOND_BLOCK;
      item = new TransmutationAltarItem(new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Transmutation Altar\",\"italic\":false,\"bold\":true,\"color\":\"aqua\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      buildItemLore(stack, ArcanaNovum.SERVER);
      
      setBookLore(makeLore());
//setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      addAltarLore(loreList);
      loreList.add(NbtString.of("[{\"text\":\"Transmutation Altar:\",\"italic\":false,\"bold\":true,\"color\":\"aqua\"},{\"text\":\"\",\"italic\":false,\"bold\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"Altar\",\"color\":\"aqua\"},{\"text\":\" beckons to an \"},{\"text\":\"ancient entity\",\"color\":\"aqua\"},{\"text\":\" of \"},{\"text\":\"balance\",\"color\":\"blue\"},{\"text\":\" and \"},{\"text\":\"trades\",\"color\":\"blue\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"Altar\",\"color\":\"aqua\"},{\"text\":\" can be called upon to\"},{\"text\":\" \",\"color\":\"aqua\"},{\"text\":\"exchange \",\"color\":\"dark_aqua\"},{\"text\":\"equivalent materials\",\"color\":\"blue\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Every \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"barter\",\"color\":\"dark_aqua\"},{\"text\":\" comes with its own \"},{\"text\":\"price.\",\"color\":\"blue\"}]"));
      return loreList;
   }
   
   
   //TODO: Make Recipe
   private MagicItemRecipe makeRecipe(){
      return null;
   }
   
   //TODO: Make Lore
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"TODO\"}");
      return list;
   }
   
   public class TransmutationAltarItem extends MagicPolymerItem {
      public TransmutationAltarItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
         
         return TypedActionResult.success(playerEntity.getStackInHand(hand));
      }
   }
}
