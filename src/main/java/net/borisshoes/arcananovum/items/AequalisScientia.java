package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
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

public class AequalisScientia extends MagicItem {
   
   private static final String TXT = "item/aequalis_scientia";
   
   public AequalisScientia(){
      id = "aequalis_scientia";
      name = "Aequalis Scientia";
      rarity = MagicRarity.MYTHICAL;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.MYTHICAL,ArcaneTome.TomeFilter.ITEMS};
      vanillaItem = Items.DIAMOND;
      item = new AequalisScientiaItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Aequalis Scientia\",\"italic\":false,\"bold\":true,\"color\":\"aqua\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      buildItemLore(stack, ArcanaNovum.SERVER);
      
      setBookLore(makeLore());
      prefNBT = addMagicNbt(tag);
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"A small \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"runestone \",\"color\":\"aqua\"},{\"text\":\"engraved with a lone, \"},{\"text\":\"ancient symbol\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"Aspect of Balance\",\"color\":\"aqua\"},{\"text\":\" has granted your their \"},{\"text\":\"favor\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Your \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"studies \",\"color\":\"blue\"},{\"text\":\"have taken you far, but everything has its \"},{\"text\":\"limit\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"An \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"ancient being\",\"color\":\"aqua\"},{\"text\":\" offers a \"},{\"text\":\"trade \",\"color\":\"dark_aqua\"},{\"text\":\"for their \"},{\"text\":\"timeless wisdom\",\"color\":\"blue\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Knowledge \",\"italic\":true,\"color\":\"blue\"},{\"text\":\"for \",\"color\":\"gray\"},{\"text\":\"Knowledge\"},{\"text\":\"; \",\"color\":\"gray\"},{\"text\":\"Skill \",\"color\":\"aqua\"},{\"text\":\"for \",\"color\":\"gray\"},{\"text\":\"Skill\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"gray\",\"italic\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"glowing rune\",\"color\":\"aqua\"},{\"text\":\" \"},{\"text\":\"reacts \",\"color\":\"blue\"},{\"text\":\"to your \"},{\"text\":\"Arcane items\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"stone \",\"color\":\"aqua\"},{\"text\":\"gravitates \",\"color\":\"blue\"},{\"text\":\"towards the \"},{\"text\":\"shrine \",\"color\":\"dark_aqua\"},{\"text\":\"of its \"},{\"text\":\"patron\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"When \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"activated\",\"color\":\"dark_aqua\"},{\"text\":\", the \"},{\"text\":\"stone \",\"color\":\"aqua\"},{\"text\":\"will \"},{\"text\":\"deallocate \",\"color\":\"dark_aqua\"},{\"text\":\"an \"},{\"text\":\"item's \",\"color\":\"light_purple\"},{\"text\":\"skill points\",\"color\":\"blue\"},{\"text\":\".\",\"color\":\"gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"gray\"},{\"text\":\"skill points\",\"color\":\"blue\"},{\"text\":\" must be \"},{\"text\":\"allocated \",\"color\":\"aqua\"},{\"text\":\"to a \"},{\"text\":\"new item\",\"color\":\"light_purple\"},{\"text\":\".\"}]"));
      return loreList;
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("\"   Aequalis Scientia\\n\\nRarity: Mythical\\n\\nI believe I have solved two mysteries in one!\\nThe entity that powers my Transmutation Altar was, in fact, divine. They call themself Equayus, God of Balance. The transmutations that\"");
      list.add("\"   Aequalis Scientia\\n\\noccur are actually an implicit barter with Equayus for items of equal value.\\nThey were kind enough to trade a few of my Legendary Catalysts for some divine energy to use in another Catalyst. From there I was able to transmute a stone\"");
      list.add("\"   Aequalis Scientia\\n\\nwith the divine energy. Equayus was impressed with my understanding and has imbued the stone with their rune.\\nThis stone is the final piece to the Altar and is used in the keystone position to conduct advanced transmutations.\"");
      list.add("\"   Aequalis Scientia\\n\\nEquayus told me that I have a solid grasp on the value of materials, but need to learn the value of knowledge.\\n\\nThe Aequalis Scientia is supposed to guide me to that realization by letting me exchange some of the skills I have learned\"");
      list.add("\"   Aequalis Scientia\\n\\nfor others.\\n\\nI can use the Aequalis in conjunction with two of my own Arcane Items and some reagents to transfer the skills I have learned from one item into skills I can learn for the other. \"");
      return list;
   }
   
   public class AequalisScientiaItem extends MagicPolymerItem {
      public AequalisScientiaItem(Settings settings){
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
