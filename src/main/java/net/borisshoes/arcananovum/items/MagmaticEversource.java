package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.EnergyItem;
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

public class MagmaticEversource extends EnergyItem {
   
   private static final String TXT_PLACE = "item/magmatic_eversource_place";
   private static final String TXT_REMOVE = "item/magmatic_eversource_remove";
   private static final String TXT_PLACE_COOLDOWN = "item/magmatic_eversource_place_cooldown";
   private static final String TXT_REMOVE_COOLDOWN = "item/magmatic_eversource_remove_cooldown";
   
   public MagmaticEversource(){
      id = "magmatic_eversource";
      name = "Magmatic Eversource";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC,ArcaneTome.TomeFilter.ITEMS};
      vanillaItem = Items.LAVA_BUCKET;
      item = new MagmaticEversourceItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT_PLACE));
      models.add(new Pair<>(vanillaItem,TXT_REMOVE));
      models.add(new Pair<>(vanillaItem,TXT_PLACE_COOLDOWN));
      models.add(new Pair<>(vanillaItem,TXT_REMOVE_COOLDOWN));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Magmatic Eversource\",\"italic\":false,\"bold\":true,\"color\":\"gold\"}]");
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
      loreList.add(NbtString.of("[{\"text\":\"Lava \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"is harder to create than \",\"color\":\"red\"},{\"text\":\"water\",\"color\":\"blue\"},{\"text\":\", luckily there's a \",\"color\":\"red\"},{\"text\":\"dimension \",\"color\":\"dark_red\"},{\"text\":\"made of it.\",\"color\":\"red\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Unfortunately, it takes \",\"italic\":false,\"color\":\"red\"},{\"text\":\"time \",\"color\":\"blue\"},{\"text\":\"to pull \"},{\"text\":\"lava \",\"color\":\"gold\"},{\"text\":\"between worlds.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"dark_red\"},{\"text\":\" to \",\"color\":\"red\"},{\"text\":\"materialize \"},{\"text\":\"or \",\"color\":\"red\"},{\"text\":\"dismiss \"},{\"text\":\"lava \",\"color\":\"gold\"},{\"text\":\"from the world.\",\"color\":\"red\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Sneak Right Click\",\"italic\":false,\"color\":\"dark_red\"},{\"text\":\" to switch between \",\"color\":\"red\"},{\"text\":\"placing \"},{\"text\":\"and \",\"color\":\"red\"},{\"text\":\"removing \"},{\"text\":\"lava\",\"color\":\"gold\"},{\"text\":\".\",\"color\":\"red\"}]"));
      return loreList;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      return 30;
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
   
   public class MagmaticEversourceItem extends MagicPolymerItem {
      public MagmaticEversourceItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!MagicItemUtils.isMagic(itemStack)) return ArcanaRegistry.MODELS.get(TXT_PLACE).value();
         int mode = itemStack.getNbt().getCompound("arcananovum").getInt("mode");
         boolean onCD = getEnergy(itemStack) != getMaxEnergy(itemStack);
         if(onCD){
            return mode == 1 ? ArcanaRegistry.MODELS.get(TXT_REMOVE_COOLDOWN).value() : ArcanaRegistry.MODELS.get(TXT_PLACE_COOLDOWN).value();
         }else{
            return mode == 1 ? ArcanaRegistry.MODELS.get(TXT_REMOVE).value() : ArcanaRegistry.MODELS.get(TXT_PLACE).value();
         }
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
