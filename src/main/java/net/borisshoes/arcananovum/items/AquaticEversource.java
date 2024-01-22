package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
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

public class AquaticEversource extends MagicItem {
   
   private static final String TXT_PLACE = "item/aquatic_eversource_place";
   private static final String TXT_REMOVE = "item/aquatic_eversource_remove";
   
   public AquaticEversource(){
      id = "aquatic_eversource";
      name = "Aquatic Eversource";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED,ArcaneTome.TomeFilter.ITEMS};
      vanillaItem = Items.WATER_BUCKET;
      item = new AquaticEversourceItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT_PLACE));
      models.add(new Pair<>(vanillaItem,TXT_REMOVE));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Aquatic Eversource\",\"italic\":false,\"bold\":true,\"color\":\"blue\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      buildItemLore(stack, ArcanaNovum.SERVER);
      
      setBookLore(makeLore());
//setRecipe(makeRecipe());
      tag = this.addMagicNbt(tag);
      tag.getCompound("arcananovum").putInt("mode",0); // 0 place, 1 remove
      prefNBT = tag;
      
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"Two \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"buckets can make an \",\"color\":\"aqua\"},{\"text\":\"ocean\"},{\"text\":\", but \",\"color\":\"aqua\"},{\"text\":\"one \"},{\"text\":\"should be \",\"color\":\"aqua\"},{\"text\":\"enough\",\"color\":\"dark_aqua\"},{\"text\":\".\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"blue\"},{\"text\":\" to \",\"color\":\"aqua\"},{\"text\":\"create \",\"color\":\"dark_aqua\"},{\"text\":\"or \",\"color\":\"aqua\"},{\"text\":\"evaporate \",\"color\":\"dark_aqua\"},{\"text\":\"water.\",\"color\":\"aqua\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Sneak Right Click\",\"italic\":false,\"color\":\"blue\"},{\"text\":\" to switch between \",\"color\":\"aqua\"},{\"text\":\"placing \",\"color\":\"dark_aqua\"},{\"text\":\"and \",\"color\":\"aqua\"},{\"text\":\"removing \",\"color\":\"dark_aqua\"},{\"text\":\"water.\",\"color\":\"aqua\"}]"));
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
   
   public class AquaticEversourceItem extends MagicPolymerItem {
      public AquaticEversourceItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!MagicItemUtils.isMagic(itemStack)) return ArcanaRegistry.MODELS.get(TXT_PLACE).value();
         int mode = itemStack.getNbt().getCompound("arcananovum").getInt("mode");
         return mode == 1 ? ArcanaRegistry.MODELS.get(TXT_REMOVE).value() : ArcanaRegistry.MODELS.get(TXT_PLACE).value();
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
