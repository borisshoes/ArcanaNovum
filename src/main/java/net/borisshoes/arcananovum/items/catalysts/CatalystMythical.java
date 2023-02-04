package net.borisshoes.arcananovum.items.catalysts;

import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.recipes.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class CatalystMythical extends MagicItem implements UsableItem {
   
   public CatalystMythical(){
      id = "catalyst_mythical";
      name = "Mythical Augment Catalyst";
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.MUNDANE, ArcaneTome.TomeFilter.CATALYSTS};
      rarity = MagicRarity.MUNDANE;
      
      ItemStack item = new ItemStack(Items.AMETHYST_CLUSTER);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Mythical Augment Catalyst\",\"italic\":false,\"color\":\"light_purple\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Augment \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"Catalysts\",\"color\":\"blue\"},{\"text\":\" can be used to \",\"color\":\"gray\"},{\"text\":\"augment \"},{\"text\":\"your \",\"color\":\"gray\"},{\"text\":\"Magic Items\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Augments \",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"require more \",\"color\":\"gray\"},{\"text\":\"powerful \",\"color\":\"green\"},{\"text\":\"Catalysts \",\"color\":\"blue\"},{\"text\":\"at higher levels\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Apply \",\"italic\":false,\"color\":\"green\"},{\"text\":\"these \",\"color\":\"gray\"},{\"text\":\"Catalysts \",\"color\":\"blue\"},{\"text\":\"in the \",\"color\":\"gray\"},{\"text\":\"Tinkering Menu\",\"color\":\"blue\"},{\"text\":\" of your \",\"color\":\"gray\"},{\"text\":\"Tome\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Mundane \",\"italic\":false,\"color\":\"gray\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.CRYING_OBSIDIAN,64,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.OBSIDIAN,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.NETHER_STAR,16,null);
      GenericMagicIngredient h = new GenericMagicIngredient(MagicItems.RUNIC_MATRIX,1);
      GenericMagicIngredient m = new GenericMagicIngredient(MagicItems.CATALYST_LEGENDARY,1);
   
      MagicItemIngredient[][] ingredients = {
            {a,b,a,b,a},
            {b,g,h,g,b},
            {a,h,m,h,a},
            {b,g,h,g,b},
            {a,b,a,b,a}};
      return new MagicItemRecipe(ingredients);
   
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\" Mythical Augmentation\\n         Catalyst\\n\\nRarity: Mundane\\n\\n!Temp. Crafting Recipe!\\n\\nThe Mythical Artifacts are examples of divine Arcana, the Runic Matrix should be able to replicate that divine magic to some degree. But how?\"}");
      list.add("{\"text\":\" Mythical Augmentation\\n         Catalyst\\n\\nAha! I need to expose my best Catalyst to some divine Arcana.\\nMaybe there is a demi-god somewhere I can summon to bring out the Gods' power.\\nThe Wither is an interesting creature, perhaps there lies my answer.\"}");
      return list;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand){
      return true;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result){
      return false;
   }
}
