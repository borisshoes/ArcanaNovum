package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.altars.TransmutationAltarRecipeGui;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.arcananovum.recipes.transmutation.*;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class AequalisScientia extends ArcanaItem {
   public static final String ID = "aequalis_scientia";
   
   private static final String TXT = "item/aequalis_scientia";
   
   public static final String USES_TAG = "uses";
   public static final String TRANSMUTATION_TAG = "transmutation_id";
   
   public AequalisScientia(){
      id = ID;
      name = "Aequalis Scientia";
      rarity = ArcanaRarity.DIVINE;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.DIVINE,TomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.DIAMOND;
      itemVersion = 2;
      item = new AequalisScientiaItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.AQUA))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_TRANSMUTATION_ALTAR,ResearchTasks.OBTAIN_DIVINE_CATALYST,ResearchTasks.ADVANCEMENT_ALLAY_DELIVER_CAKE_TO_NOTE_BLOCK,ResearchTasks.ADVANCEMENT_ALLAY_DELIVER_ITEM_TO_PLAYER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,USES_TAG,5);
      putProperty(stack,TRANSMUTATION_TAG,"");
      setPrefStack(stack);
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      int uses = getIntProperty(stack,USES_TAG);
      String transmutationId = getStringProperty(stack,TRANSMUTATION_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,USES_TAG,uses);
      putProperty(newStack,TRANSMUTATION_TAG,transmutationId);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("A small ").formatted(Formatting.GRAY))
            .append(Text.literal("runestone ").formatted(Formatting.AQUA))
            .append(Text.literal("engraved with a lone, ").formatted(Formatting.GRAY))
            .append(Text.literal("ancient symbol").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.GRAY))
            .append(Text.literal("Aspect of Balance").formatted(Formatting.AQUA))
            .append(Text.literal(" has granted you their ").formatted(Formatting.GRAY))
            .append(Text.literal("favor").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal(""));
      lore.add(Text.literal("")
            .append(Text.literal("Your ").formatted(Formatting.GRAY))
            .append(Text.literal("studies ").formatted(Formatting.BLUE))
            .append(Text.literal("have taken you far, but everything has its ").formatted(Formatting.GRAY))
            .append(Text.literal("limit").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("An ").formatted(Formatting.GRAY))
            .append(Text.literal("ancient being").formatted(Formatting.AQUA))
            .append(Text.literal(" offers a ").formatted(Formatting.GRAY))
            .append(Text.literal("trade ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("for their ").formatted(Formatting.GRAY))
            .append(Text.literal("timeless wisdom").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("").formatted(Formatting.ITALIC)
            .append(Text.literal("Knowledge ").formatted(Formatting.BLUE))
            .append(Text.literal("for ").formatted(Formatting.GRAY))
            .append(Text.literal("Knowledge").formatted(Formatting.BLUE))
            .append(Text.literal("; ").formatted(Formatting.GRAY))
            .append(Text.literal("Skill ").formatted(Formatting.AQUA))
            .append(Text.literal("for ").formatted(Formatting.GRAY))
            .append(Text.literal("Skill").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal(""));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.GRAY))
            .append(Text.literal("glowing rune").formatted(Formatting.AQUA))
            .append(Text.literal(" reacts ").formatted(Formatting.BLUE))
            .append(Text.literal("to your ").formatted(Formatting.GRAY))
            .append(Text.literal("Arcane items").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.GRAY))
            .append(Text.literal("stone ").formatted(Formatting.AQUA))
            .append(Text.literal("gravitates ").formatted(Formatting.BLUE))
            .append(Text.literal("towards the ").formatted(Formatting.GRAY))
            .append(Text.literal("shrine ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("of its ").formatted(Formatting.GRAY))
            .append(Text.literal("patron").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal(""));
      lore.add(Text.literal("")
            .append(Text.literal("When ").formatted(Formatting.GRAY))
            .append(Text.literal("transmuted").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(", the ").formatted(Formatting.GRAY))
            .append(Text.literal("stone ").formatted(Formatting.AQUA))
            .append(Text.literal("will ").formatted(Formatting.GRAY))
            .append(Text.literal("deallocate ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("an ").formatted(Formatting.GRAY))
            .append(Text.literal("item's ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("skill points").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.GRAY))
            .append(Text.literal("skill points").formatted(Formatting.BLUE))
            .append(Text.literal(" must be ").formatted(Formatting.GRAY))
            .append(Text.literal("allocated ").formatted(Formatting.AQUA))
            .append(Text.literal("to a ").formatted(Formatting.GRAY))
            .append(Text.literal("new item").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal(""));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.GRAY))
            .append(Text.literal("Aequalis").formatted(Formatting.AQUA))
            .append(Text.literal(" also can be ").formatted(Formatting.GRAY))
            .append(Text.literal("attuned ").formatted(Formatting.BLUE))
            .append(Text.literal("to a ").formatted(Formatting.GRAY))
            .append(Text.literal("transmutation recipe.").formatted(Formatting.DARK_AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("The chosen ").formatted(Formatting.GRAY))
            .append(Text.literal("recipe").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" can be performed ").formatted(Formatting.GRAY))
            .append(Text.literal("at will").formatted(Formatting.AQUA))
            .append(Text.literal(" at").formatted(Formatting.GRAY))
            .append(Text.literal(" half ").formatted(Formatting.BLUE))
            .append(Text.literal("reagent cost").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Right click").formatted(Formatting.AQUA))
            .append(Text.literal(" to ").formatted(Formatting.GRAY))
            .append(Text.literal("attune").formatted(Formatting.BLUE))
            .append(Text.literal(" or ").formatted(Formatting.GRAY))
            .append(Text.literal("transmute").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      
      if(itemStack != null){
         int uses = getIntProperty(itemStack, USES_TAG);
         String useStr = ArcanaAugments.getAugmentOnItem(itemStack, ArcanaAugments.TIMELESS_WISDOM.id) > 0 ? "∞" : uses+"";
         lore.add(Text.literal(""));
         lore.add(Text.literal("")
               .append(Text.literal("Reallocation Uses").formatted(Formatting.AQUA))
               .append(Text.literal(" - ").formatted(Formatting.BLUE))
               .append(Text.literal(useStr).formatted(Formatting.DARK_AQUA)));
         
         String transmutationId = getStringProperty(itemStack, TRANSMUTATION_TAG);
         String transStr = transmutationId.isEmpty() ? "None" : transmutationId;
         lore.add(Text.literal("")
               .append(Text.literal("Attuned Transmutation").formatted(Formatting.DARK_AQUA))
               .append(Text.literal(" - ").formatted(Formatting.BLUE))
               .append(Text.literal(transStr).formatted(Formatting.AQUA)));
      }
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   public void inventoryDialog(ServerPlayerEntity player){
      ArrayList<Dialog> dialogOptions = new ArrayList<>();
      // Conditions: 0 - Crafted Wings, 1 - Crafted Memento, 2 - Has Ceptyus Pickaxe, 3 - Has Memento, 4 - Has Egg
      boolean[] conditions = new boolean[]{
            PLAYER_DATA.get(player).hasCrafted(ArcanaRegistry.WINGS_OF_ENDERIA),
            PLAYER_DATA.get(player).hasCrafted(ArcanaRegistry.NUL_MEMENTO),
            ArcanaItemUtils.hasItemInInventory(player,ArcanaRegistry.PICKAXE_OF_CEPTYUS.getItem()),
            ArcanaItemUtils.hasItemInInventory(player,ArcanaRegistry.NUL_MEMENTO.getItem()),
            ArcanaItemUtils.hasItemInInventory(player,Items.DRAGON_EGG),
      };
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nNul might claim to be the god of knowledge. He 'forgets' that I have a few centuries of experience on him.").formatted(Formatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{},0,1,1));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nYou have one of Ceptyus's Picks? How in the world did you acquire this?! For once, I do not think I have anything equivalent to trade.").formatted(Formatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,1.3f))
      ),new int[]{},0,1,2));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nEverything has value, especially life. My Sister knows that better than anyone, I just wish she'd remember that more often.").formatted(Formatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{},0,1,0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nKnowledge is a most interesting possession. Its value changes based on who holds it.").formatted(Formatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{},1,1,-1));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nA skill for a skill, an augment for an augment... Would you call that a fair trade?").formatted(Formatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{},1,1,-1));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nWhat do you think of my Brother? Is it worth dealing with Death?").formatted(Formatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{},0,1,1));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nVery few have had the wisdom to make my grand trade. I'm glad that you did.").formatted(Formatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{},1,1,-1));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nHave caution when taking my Brother's challenges. He forgets how fragile you can be.").formatted(Formatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{},0,1,1));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nPerfectly Balanced. As all things should be...").formatted(Formatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{},1,1,-1));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nNul, I figured you would be watching this Player closely, you always had a fascination with them.\n").formatted(Formatting.AQUA)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nWere we not both like them once? And look at all that we have accomplished.\n").formatted(Formatting.DARK_GRAY)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nThat was so very long ago...\n").formatted(Formatting.AQUA)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nIndeed, so very long ago...").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.6f),
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.6f))
      ),new int[]{0,80,100,50},0,1,3));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nDo you think the end will ever come for us, Brother?\n").formatted(Formatting.AQUA)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nDeath is the fate of all things, even us.\n").formatted(Formatting.DARK_GRAY)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nThen we should face it together.\n").formatted(Formatting.AQUA)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nThere's no one else I would rather have at my side.").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.9f),
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,1.4f))
      ),new int[]{0,60,80,60},0,1,3));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nDo you still call yourself the 'God of Knowledge', Little Brother?\n").formatted(Formatting.AQUA)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nI am the God of Knowledge! And what of it?\n").formatted(Formatting.DARK_GRAY)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\n*Chuckles*").formatted(Formatting.AQUA, Formatting.ITALIC))
                  .append(Text.literal(" It's cute that you try to mimic me.\n").formatted(Formatting.AQUA)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nI shall keep that title for eons to come, even if only to make you laugh.").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITH_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,1.4f))
      ),new int[]{0,80,60,60},0,1,3));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nLittle Brother, what schemes have you been planning? You have always been so very busy with your plots...\n").formatted(Formatting.AQUA)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nMy convictions remain the same as they have always been. And I wouldn't trust this Player with my current plans, not yet.\n").formatted(Formatting.DARK_GRAY)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nPlayer, whatever he has in store for you, remember you can always find your own path instead.").formatted(Formatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.8f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{0,100,120},0,1,3));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nEnderia, we never talked much in my youth. What would you do if you were released, what have you learned?\n").formatted(Formatting.AQUA)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nYou could have stopped them! Why have all this power if you do not wield it!?\n").formatted(Formatting.DARK_PURPLE)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nI am not like you and Nul, showing power through violence. My influence is subtle, like the rising tide - it's far stronger than you realize.").formatted(Formatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.3f,1.4f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{0,120,100},0,1,4));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nEnderia, you are not at fault for what happened in your life before, but cruelty should not beget more cruelty.\n").formatted(Formatting.AQUA)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nYou know nothing of what it was like! Being enslaved and forced to slaughter everything!\n").formatted(Formatting.DARK_PURPLE)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nYou must never have visited the Overworld and seen what is done to my kind. Maybe this Player will show you sometime.").formatted(Formatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.3f,1.4f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{0,120,100},0,1,4));
      
      DialogHelper helper = new DialogHelper(dialogOptions,conditions);
      helper.sendDialog(List.of(player),helper.getWeightedResult(),true);
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ExplainIngredient b = new ExplainIngredient(GraphicalItem.withColor(GraphicItems.PAGE_BG, ArcanaColors.DARK_COLOR),1,"",false)
            .withName(Text.literal("Transmutation Recipe").formatted(Formatting.AQUA,Formatting.BOLD))
            .withLore(List.of(Text.literal("Use a Transmutation Altar").formatted(Formatting.DARK_AQUA)));
      ExplainIngredient w = new ExplainIngredient(GraphicalItem.withColor(GraphicItems.PAGE_BG, ArcanaColors.LIGHT_COLOR),1,"",false)
            .withName(Text.literal("Transmutation Recipe").formatted(Formatting.AQUA,Formatting.BOLD))
            .withLore(List.of(Text.literal("Use a Transmutation Altar").formatted(Formatting.DARK_AQUA)));
      ExplainIngredient a = new ExplainIngredient(Items.AMETHYST_BLOCK,64,"Amethyst Blocks")
            .withName(Text.literal("Amethyst Blocks").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
            .withLore(List.of(Text.literal("Transmutation Reagent").formatted(Formatting.LIGHT_PURPLE)));
      ExplainIngredient t = new ExplainIngredient(ArcanaRegistry.TRANSMUTATION_ALTAR.getItem(),1,"",false)
            .withName(Text.literal("Transmutation Altar").formatted(Formatting.AQUA,Formatting.BOLD))
            .withLore(List.of(Text.literal("Use a Transmutation Altar").formatted(Formatting.DARK_AQUA)));
      ExplainIngredient d = new ExplainIngredient(Items.DIAMOND_BLOCK,1,"Diamond Block")
            .withName(Text.literal("Diamond Block").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
            .withLore(List.of(Text.literal("Transmutation Reagent").formatted(Formatting.LIGHT_PURPLE)));
      ExplainIngredient c = new ExplainIngredient(ArcanaRegistry.DIVINE_CATALYST.getItem(),1,"Divine Augment Catalyst")
            .withName(Text.literal("Divine Augmentation Catalyst").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
            .withLore(List.of(Text.literal("Infusion Input").formatted(Formatting.WHITE)));
      
      ExplainIngredient[][] ingredients = {
            {b,b,c,b,b},
            {b,b,b,b,w},
            {a,b,t,w,d},
            {b,w,w,w,w},
            {w,w,w,w,w}};
      return new ExplainRecipe(ingredients);
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("   Aequalis Scientia\n\nRarity: Divine\n\nI believe I have solved two mysteries in one!\nThe entity that powers my Transmutation Altar was, in fact, divine. They call themself Equayus, God of Balance. The transmutations that")));
      list.add(List.of(Text.literal("   Aequalis Scientia\n\noccur are actually an implicit barter with Equayus for items of equal value.\nThey were kind enough to trade a few of my Sovereign Catalysts for some divine energy to use in another Catalyst. From there I was able to transmute a stone")));
      list.add(List.of(Text.literal("   Aequalis Scientia\n\nwith the divine energy. Equayus was impressed with my understanding and has imbued the stone with their rune.\nThis stone is the final piece to the Altar and is used in the keystone position to conduct advanced transmutations.")));
      list.add(List.of(Text.literal("   Aequalis Scientia\n\nEquayus told me that I have a solid grasp on the value of materials, but need to learn the value of knowledge.\n\nThe Aequalis Scientia is supposed to guide me to that realization by letting me exchange some of the skills I have learned")));
      list.add(List.of(Text.literal("   Aequalis Scientia\n\nfor others.\n\nI can use the Aequalis in conjunction with two of my own Arcane Items and some reagents to transfer the skills I have learned from one item into skills I can learn for the other. ")));
      return list;
   }
   
   public class AequalisScientiaItem extends ArcanaPolymerItem {
      public AequalisScientiaItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.getModelData(TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         
         if(Math.random() < 0.0000075){
            inventoryDialog(player);
         }
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
         ItemStack stack = playerEntity.getStackInHand(hand);
         String transmutationId = getStringProperty(stack,TRANSMUTATION_TAG);
         if(!(playerEntity instanceof ServerPlayerEntity player)) return TypedActionResult.success(playerEntity.getStackInHand(hand));
         
         if(transmutationId.isEmpty()){
            TransmutationAltarRecipeGui transmutationGui = new TransmutationAltarRecipeGui(player,null, Optional.empty());
            transmutationGui.enableSelectionMode(stack);
            transmutationGui.buildRecipeListGui();
            transmutationGui.open();
         }else{
            Optional<TransmutationRecipe> recipeOpt = TransmutationRecipes.TRANSMUTATION_RECIPES.stream().filter(r -> r.getName().equals(transmutationId)).findAny();
            if(recipeOpt.isPresent()){
               TransmutationRecipe recipe = recipeOpt.get();
               List<ItemStack> results;
               List<ItemStack> items = null;
               
               if(recipe instanceof CommutativeTransmutationRecipe commieRecipe){
                  if(commieRecipe.validCommunalInput(playerEntity.getStackInHand(Hand.OFF_HAND))){
                     ItemStack input = null;
                     
                     for(ItemStack communalInput : commieRecipe.getCommunalInputs()){
                        if(communalInput.isOf(playerEntity.getStackInHand(Hand.OFF_HAND).getItem())) continue;
                        ItemStack removed = getAndSplitValidStack(communalInput,player,true);
                        if(removed != null){
                           input = removed;
                           break;
                        }
                     }
                     if(input == null){
                        player.sendMessage(Text.literal("You do not have a valid input item.").formatted(Formatting.RED,Formatting.ITALIC),false);
                        return TypedActionResult.success(playerEntity.getStackInHand(hand));
                     }
                     ItemStack reagent1 = getAndSplitValidStack(recipe.getAequalisReagent(recipe.getReagent1()),player,false);
                     if(reagent1 == null){
                        MiscUtils.returnItems(new SimpleInventory(input),player);
                        player.sendMessage(Text.literal("").formatted(Formatting.RED,Formatting.ITALIC).append(Text.literal("You do not have enough ")).append(Text.translatable(recipe.getAequalisReagent(recipe.getReagent1()).getTranslationKey())),false);
                        return TypedActionResult.success(playerEntity.getStackInHand(hand));
                     }
                     ItemStack reagent2 = getAndSplitValidStack(recipe.getAequalisReagent(recipe.getReagent2()),player,false);
                     if(reagent2 == null){
                        MiscUtils.returnItems(new SimpleInventory(input),player);
                        MiscUtils.returnItems(new SimpleInventory(reagent1),player);
                        player.sendMessage(Text.literal("").formatted(Formatting.RED,Formatting.ITALIC).append(Text.literal("You do not have enough ")).append(Text.translatable(recipe.getAequalisReagent(recipe.getReagent2()).getTranslationKey())),false);
                        return TypedActionResult.success(playerEntity.getStackInHand(hand));
                     }
                     ItemStack focus = playerEntity.getStackInHand(Hand.OFF_HAND).split(1);
                     
                     items = List.of(input,focus,reagent1,reagent2,stack);
                     results = commieRecipe.doTransmutation(input,focus,reagent1,reagent2,stack,player);
                  }else{
                     results = null;
                     player.sendMessage(Text.literal("Your offhand must be a valid focus item.").formatted(Formatting.RED,Formatting.ITALIC),false);
                     return TypedActionResult.success(playerEntity.getStackInHand(hand));
                  }
               }else if(recipe instanceof InfusionTransmutationRecipe infusionRecipe){
                  ItemStack input = getAndSplitValidStack(infusionRecipe.getInput(),player,true);
                  if(input == null){
                     player.sendMessage(Text.literal("").formatted(Formatting.RED,Formatting.ITALIC).append(Text.literal("You do not have enough ")).append(Text.translatable(infusionRecipe.getInput().getTranslationKey())),false);
                     return TypedActionResult.success(playerEntity.getStackInHand(hand));
                  }
                  ItemStack reagent1 = getAndSplitValidStack(recipe.getAequalisReagent(recipe.getReagent1()),player,false);
                  if(reagent1 == null){
                     MiscUtils.returnItems(new SimpleInventory(input),player);
                     player.sendMessage(Text.literal("").formatted(Formatting.RED,Formatting.ITALIC).append(Text.literal("You do not have enough ")).append(Text.translatable(recipe.getAequalisReagent(recipe.getReagent1()).getTranslationKey())),false);
                     return TypedActionResult.success(playerEntity.getStackInHand(hand));
                  }
                  ItemStack reagent2 = getAndSplitValidStack(recipe.getAequalisReagent(recipe.getReagent2()),player,false);
                  if(reagent2 == null){
                     MiscUtils.returnItems(new SimpleInventory(input),player);
                     MiscUtils.returnItems(new SimpleInventory(reagent1),player);
                     player.sendMessage(Text.literal("").formatted(Formatting.RED,Formatting.ITALIC).append(Text.literal("You do not have enough ")).append(Text.translatable(recipe.getAequalisReagent(recipe.getReagent2()).getTranslationKey())),false);
                     return TypedActionResult.success(playerEntity.getStackInHand(hand));
                  }
                  
                  items = List.of(input,ItemStack.EMPTY,reagent1,reagent2,stack);
                  results = infusionRecipe.doTransmutation(input,null,reagent1,reagent2,stack,player);
               }else if(recipe instanceof PermutationTransmutationRecipe permutationRecipe){
                  ItemStack input = getAndSplitValidStack(permutationRecipe.getInput(),player,true);
                  if(input == null){
                     player.sendMessage(Text.literal("").formatted(Formatting.RED,Formatting.ITALIC).append(Text.literal("You do not have enough ")).append(Text.translatable(permutationRecipe.getInput().getTranslationKey())),false);
                     return TypedActionResult.success(playerEntity.getStackInHand(hand));
                  }
                  ItemStack reagent1 = getAndSplitValidStack(recipe.getAequalisReagent(recipe.getReagent1()),player,false);
                  if(reagent1 == null){
                     MiscUtils.returnItems(new SimpleInventory(input),player);
                     player.sendMessage(Text.literal("").formatted(Formatting.RED,Formatting.ITALIC).append(Text.literal("You do not have enough ")).append(Text.translatable(recipe.getAequalisReagent(recipe.getReagent1()).getTranslationKey())),false);
                     return TypedActionResult.success(playerEntity.getStackInHand(hand));
                  }
                  ItemStack reagent2 = getAndSplitValidStack(recipe.getAequalisReagent(recipe.getReagent2()),player,false);
                  if(reagent2 == null){
                     MiscUtils.returnItems(new SimpleInventory(input),player);
                     MiscUtils.returnItems(new SimpleInventory(reagent1),player);
                     player.sendMessage(Text.literal("").formatted(Formatting.RED,Formatting.ITALIC).append(Text.literal("You do not have enough ")).append(Text.translatable(recipe.getAequalisReagent(recipe.getReagent2()).getTranslationKey())),false);
                     return TypedActionResult.success(playerEntity.getStackInHand(hand));
                  }
                  
                  items = List.of(input,ItemStack.EMPTY,reagent1,reagent2,stack);
                  results = permutationRecipe.doTransmutation(input,null,reagent1,reagent2,stack,player);
               }else{
                  results = null;
               }
               
               if(items != null && results != null){
                  playerEntity.getInventory().removeOne(stack);
                  Vec3d center = player.getPos().add(player.getRotationVector().multiply(3,0,3)).add(0,2,0);
                  ParticleEffectUtils.aequalisTransmuteAnim(player.getServerWorld(),center,0,player.getRotationClient(),1,items.get(0),items.get(1),items.get(2),items.get(3),items.get(4));
                  
                  ArcanaNovum.addTickTimerCallback(player.getServerWorld(), new GenericTimer(500, () -> {
                     for(ItemStack result : results){
                        ItemScatterer.spawn(world, center.x,center.y,center.z,result);
                     }
                  }));
               }
            }else{
               putProperty(stack, AequalisScientia.TRANSMUTATION_TAG,"");
            }
         }
         return TypedActionResult.success(playerEntity.getStackInHand(hand));
      }
      
      private ItemStack getAndSplitValidStack(ItemStack stack, ServerPlayerEntity player, boolean repeat){
         PlayerInventory inventory = player.getInventory();
         
         for(int i = 0; i < inventory.size(); i++){
            if(i == PlayerInventory.OFF_HAND_SLOT) continue;
            ItemStack invStack = inventory.getStack(i);
            
            if(invStack.isOf(stack.getItem()) && invStack.getCount() >= stack.getCount()){
               int splitAmount = repeat ? stack.getCount() * (invStack.getCount() / stack.getCount()) : stack.getCount();
               return invStack.split(splitAmount);
            }
         }
         return null;
      }
   }
}

