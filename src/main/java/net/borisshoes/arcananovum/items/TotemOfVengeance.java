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

public class TotemOfVengeance extends MagicItem {
   
   private static final String TXT = "item/totem_of_vengeance";
   
   public TotemOfVengeance(){
      id = "totem_of_vengeance";
      name = "Totem Of Vengeance";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.ITEMS,ArcaneTome.TomeFilter.EQUIPMENT};
      vanillaItem = Items.TOTEM_OF_UNDYING;
      item = new TotemOfVengeanceItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Totem of Vengeance\",\"italic\":false,\"bold\":true,\"color\":\"dark_red\"}]");
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
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"Totem's \",\"color\":\"dark_red\"},{\"text\":\"benevolent protection\",\"color\":\"green\"},{\"text\":\" has been \"},{\"text\":\"twisted \",\"color\":\"red\"},{\"text\":\"by \"},{\"text\":\"violence\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Once \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"regenerative magic\",\"color\":\"green\"},{\"text\":\" is now \"},{\"text\":\"fueled \",\"color\":\"red\"},{\"text\":\"by \"},{\"text\":\"rage \",\"color\":\"dark_red\"},{\"text\":\"for that which hunts you.\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Upon \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"fatal damage\",\"color\":\"red\"},{\"text\":\", you become \"},{\"text\":\"Death Warded\",\"color\":\"dark_red\"},{\"text\":\" for a \"},{\"text\":\"brief duration\",\"color\":\"green\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"You will be \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"unable \",\"color\":\"red\"},{\"text\":\"to drop to \"},{\"text\":\"zero health\",\"color\":\"green\"},{\"text\":\" and gain an \"},{\"text\":\"offensive boost\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"If you do not get \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"revenge \",\"color\":\"dark_red\"},{\"text\":\"before the protection \"},{\"text\":\"fades\",\"color\":\"red\"},{\"text\":\", you will \"},{\"text\":\"perish\",\"color\":\"dark_red\"},{\"text\":\".\"}]"));
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
   
   public class TotemOfVengeanceItem extends MagicPolymerItem {
      public TotemOfVengeanceItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.MODELS.get(TXT).value();
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
