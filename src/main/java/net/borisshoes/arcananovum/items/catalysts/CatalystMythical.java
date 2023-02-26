package net.borisshoes.arcananovum.items.catalysts;

import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.recipes.*;
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
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.MYTHICAL, ArcaneTome.TomeFilter.CATALYSTS};
      rarity = MagicRarity.MYTHICAL;
      itemVersion = 1;
      
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
      loreList.add(NbtString.of("[{\"text\":\"Mythical \",\"italic\":false,\"color\":\"light_purple\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
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
      ItemStack soulSand = new ItemStack(Items.SOUL_SAND);
      NbtCompound tag = soulSand.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Soul Sand or Soil\",\"italic\":false,\"color\":\"gray\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Construct a Wither Base with a heart of Netherite.\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
   
      ItemStack skull = new ItemStack(Items.WITHER_SKELETON_SKULL);
      tag = skull.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Wither Skeleton Skull\",\"italic\":false,\"color\":\"dark_gray\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Construct a Wither Base with a heart of Netherite.\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      
      ItemStack netherite = new ItemStack(Items.NETHERITE_BLOCK);
      tag = netherite.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Block of Netherite\",\"italic\":false,\"color\":\"dark_red\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"Construct a Wither Base with a heart of Netherite.\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
   
      ItemStack catalyst = MagicItems.CATALYST_LEGENDARY.getPrefItem().copy();
      tag = catalyst.getOrCreateNbt();
      display = tag.getCompound("display");
      loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"gold\"},{\"text\":\" the \",\"color\":\"dark_purple\"},{\"text\":\"Catalyst \"},{\"text\":\"on the \",\"color\":\"dark_purple\"},{\"text\":\"Netherite Heart\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"dark_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Divine Energy\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\" will flow into the \",\"color\":\"dark_purple\"},{\"text\":\"Construct\",\"color\":\"gray\"},{\"text\":\", empowering it.\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Defeat the \",\"italic\":false,\"color\":\"dark_purple\"},{\"text\":\"Construct \",\"color\":\"gray\"},{\"text\":\"without dying to receive a \"},{\"text\":\"Mythical Catalyst\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Warning! This fight is difficult, preparation is necessary.\",\"italic\":false,\"color\":\"red\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
   
      ItemStack pane = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
      tag = pane.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"In World Construct\",\"italic\":false,\"color\":\"gray\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Build This in the World\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      
      ExplainIngredient a = new ExplainIngredient(pane,"",false);
      ExplainIngredient s = new ExplainIngredient(soulSand,"Soul Sand or Soil");
      ExplainIngredient k = new ExplainIngredient(skull,"Wither Skeleton Skull");
      ExplainIngredient n = new ExplainIngredient(netherite,"Netherite Block");
      ExplainIngredient c = new ExplainIngredient(catalyst,"Legendary Augment Catalyst");
   
      ExplainIngredient[][] ingredients = {
            {a,a,a,a,a},
            {a,k,k,k,a},
            {a,s,n,s,c},
            {a,a,s,a,a},
            {a,a,a,a,a}};
      return new ExplainRecipe(ingredients);
   
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
