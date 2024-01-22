package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TemporalMoment extends MagicItem {
   
   private static final String TXT = "item/temporal_moment";
   
   public TemporalMoment(){
      id = "temporal_moment";
      name = "Temporal Moment";
      rarity = MagicRarity.MUNDANE;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.MUNDANE, ArcaneTome.TomeFilter.ITEMS};
      vanillaItem = Items.CLOCK;
      item = new TemporalMomentItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Temporal Moment\",\"italic\":false,\"bold\":true,\"color\":\"dark_blue\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      buildItemLore(stack, ArcanaNovum.SERVER);

      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"A piece of \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"spacetime \",\"color\":\"dark_gray\"},{\"text\":\"collapsing\",\"color\":\"dark_aqua\",\"italic\":true},{\"text\":\" \",\"italic\":true},{\"text\":\"down to a single \"},{\"text\":\"moment\",\"color\":\"dark_blue\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"clock\",\"color\":\"aqua\"},{\"text\":\" itself is stuck between \"},{\"text\":\"one \",\"color\":\"dark_aqua\"},{\"text\":\"instant of \"},{\"text\":\"time\",\"color\":\"dark_blue\"},{\"text\":\" and \"},{\"text\":\"another\",\"color\":\"dark_aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"discovery\",\"color\":\"aqua\"},{\"text\":\" unlocks a whole \"},{\"text\":\"world\",\"color\":\"dark_gray\"},{\"text\":\" of \"},{\"text\":\"possibilites\",\"color\":\"dark_aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      return loreList;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient l = new MagicItemIngredient(Items.LAPIS_LAZULI,32,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.CRYING_OBSIDIAN,32,null);
      MagicItemIngredient o = new MagicItemIngredient(Items.OBSIDIAN,32,null);
      MagicItemIngredient d = new MagicItemIngredient(Items.DIAMOND,16,null);
      MagicItemIngredient e = new MagicItemIngredient(Items.ENDER_PEARL,16,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.CLOCK,64,null);
   
      MagicItemIngredient[][] ingredients = {
            {o,e,p,e,o},
            {e,d,l,d,e},
            {p,l,c,l,p},
            {e,d,l,d,e},
            {o,e,p,e,o}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"   Temporal Moment\\n\\nRarity: Mundane\\n\\nTime always moves forwards, but its rate can be changed from fluctuations in spacetime. With enough energy perhaps it could be slowed to a halt, freezing a moment in time to use later.\"}");
      return list;
   }
   
   public class TemporalMomentItem extends MagicPolymerItem {
      public TemporalMomentItem(Settings settings){
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
   }
}
