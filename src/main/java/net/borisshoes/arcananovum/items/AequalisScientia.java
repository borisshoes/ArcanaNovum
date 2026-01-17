package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.altars.TransmutationAltarRecipeGui;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.recipes.RecipeManager;
import net.borisshoes.arcananovum.recipes.transmutation.*;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.Dialog;
import net.borisshoes.arcananovum.utils.DialogHelper;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class AequalisScientia extends ArcanaItem {
   public static final String ID = "aequalis_scientia";
   
   public static final String USES_TAG = "uses";
   public static final String TRANSMUTATION_TAG = "transmutation_id";
   
   public AequalisScientia(){
      id = ID;
      name = "Aequalis Scientia";
      rarity = ArcanaRarity.DIVINE;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity),ArcaneTomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.DIAMOND;
      itemVersion = 2;
      item = new AequalisScientiaItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_TRANSMUTATION_ALTAR,ResearchTasks.OBTAIN_DIVINE_CATALYST,ResearchTasks.ADVANCEMENT_ALLAY_DELIVER_CAKE_TO_NOTE_BLOCK,ResearchTasks.ADVANCEMENT_ALLAY_DELIVER_ITEM_TO_PLAYER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
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
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("A small ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("runestone ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("engraved with a lone, ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("ancient symbol").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Aspect of Balance").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" has granted you their ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("favor").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal(""));
      lore.add(Component.literal("")
            .append(Component.literal("Your ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("studies ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("have taken you far, but everything has its ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("limit").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("An ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("ancient being").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" offers a ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("trade ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("for their ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("timeless wisdom").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("").withStyle(ChatFormatting.ITALIC)
            .append(Component.literal("Knowledge ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("for ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Knowledge").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("; ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Skill ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("for ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Skill").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal(""));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("glowing rune").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" reacts ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("to your ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Arcane items").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("stone ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("gravitates ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("towards the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("shrine ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("of its ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("patron").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal(""));
      lore.add(Component.literal("")
            .append(Component.literal("When ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("transmuted").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(", the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("stone ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("will ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("deallocate ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("an ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("item's ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("skill points").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("These ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("skill points").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" must be ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("allocated ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("to a ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("new item").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal(""));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Aequalis").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" also can be ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("attuned ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("to a ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("transmutation recipe.").withStyle(ChatFormatting.DARK_AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("The chosen ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("recipe").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" can be performed ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("at will").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" at").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" half ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("reagent cost").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Right click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("attune").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" or ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("transmute").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      
      if(itemStack != null){
         int uses = getIntProperty(itemStack, USES_TAG);
         String useStr = ArcanaAugments.getAugmentOnItem(itemStack, ArcanaAugments.TIMELESS_WISDOM.id) > 0 ? "∞" : uses+"";
         lore.add(Component.literal(""));
         lore.add(Component.literal("")
               .append(Component.literal("Reallocation Uses").withStyle(ChatFormatting.AQUA))
               .append(Component.literal(" - ").withStyle(ChatFormatting.BLUE))
               .append(Component.literal(useStr).withStyle(ChatFormatting.DARK_AQUA)));
         
         String transmutationId = getStringProperty(itemStack, TRANSMUTATION_TAG);
         TransmutationRecipe recipe = RecipeManager.findMatchingTransmutationRecipe(transmutationId);
         MutableComponent transStr = recipe == null ? Component.translatable("gui.borislib.none") : recipe.getName();
         lore.add(Component.literal("")
               .append(Component.literal("Attuned Transmutation").withStyle(ChatFormatting.DARK_AQUA))
               .append(Component.literal(" - ").withStyle(ChatFormatting.BLUE))
               .append(transStr.withStyle(ChatFormatting.AQUA)));
      }
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   public void inventoryDialog(ServerPlayer player){
      ArrayList<Dialog> dialogOptions = new ArrayList<>();
      // Conditions: 0 - Crafted Wings, 1 - Crafted Memento, 2 - Has Ceptyus Pickaxe, 3 - Has Memento, 4 - Has Egg, 5 - Has Greaves, 6 - Has Spear
      boolean[] conditions = new boolean[]{
            ArcanaNovum.data(player).hasCrafted(ArcanaRegistry.WINGS_OF_ENDERIA),
            ArcanaNovum.data(player).hasCrafted(ArcanaRegistry.NUL_MEMENTO),
            ArcanaItemUtils.hasItemInInventory(player,ArcanaRegistry.PICKAXE_OF_CEPTYUS.getItem()),
            ArcanaItemUtils.hasItemInInventory(player,ArcanaRegistry.NUL_MEMENTO.getItem()),
            ArcanaItemUtils.hasItemInInventory(player, Items.DRAGON_EGG),
            ArcanaItemUtils.hasItemInInventory(player,ArcanaRegistry.GREAVES_OF_GAIALTUS.getItem()),
            ArcanaItemUtils.hasItemInInventory(player,ArcanaRegistry.SPEAR_OF_TENBROUS.getItem()),
      };
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nNul might claim to be the god of knowledge. He 'forgets' that my experience beats his one hundred-fold.").withStyle(ChatFormatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{},0,1,0b10));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nYou have one of Ceptyus's Picks? How in the world did you acquire this?! For once, I do not think I have anything equivalent to trade.").withStyle(ChatFormatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,1.3f))
      ),new int[]{},0,1,0b100));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nEverything has value, especially life. My Sister knows that better than anyone, I just wish she'd remember that more often.").withStyle(ChatFormatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{},0,1,0b1));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nKnowledge is a most interesting possession. Its value changes based on who holds it.").withStyle(ChatFormatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{},1,1,0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nA skill for a skill, an augment for an augment... Would you call that a fair trade?").withStyle(ChatFormatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{},1,1,0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nWhat do you think of my Brother? Is it worth dealing with Death?").withStyle(ChatFormatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{},0,1,0b10));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nVery few have had the wisdom to make my grand trade. I'm glad that you did.").withStyle(ChatFormatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{},1,1,0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nHave caution when taking my Brother's challenges. He forgets how fragile you can be.").withStyle(ChatFormatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{},0,1,0b10));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nPerfectly Balanced. As all things should be...").withStyle(ChatFormatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{},1,1,0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nYou too have a relic of Gaialtus? Treasure that rare gift!").withStyle(ChatFormatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{},0,1,0b100000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nThat Spear frightens me. Such cruel violence should never be endorsed.").withStyle(ChatFormatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{},0,1,0b1000000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nAs much as I wish to learn from your Spear, I feel it would be better forgotten.\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nAt least you have some sense in you. Bury that Spear in the deepest hole you can dig!").withStyle(ChatFormatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{0,80},0,1,0b1010000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nGaialtus's power is unlike any other's, the world itself becomes your sandbox.\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nYou speak too favorably of Gaialtus. Do not grant it favor for being the source of your power.\n").withStyle(ChatFormatting.DARK_GRAY)),
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nYou do not know Gaialtus as I do. And my true power comes not from divinity, but from the lifetimes I spent researching our world.").withStyle(ChatFormatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.6f))
      ),new int[]{0,80,100},0,1,0b101000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nTreasure those trousers, they will help you create anything your mind desires.").withStyle(ChatFormatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{},0,1,0b100000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nNul, I figured you would be watching this Player closely, you always had a fascination with them.\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nWere we not both like them once? And look at all that we have accomplished.\n").withStyle(ChatFormatting.DARK_GRAY)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nThat was so very long ago...\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nIndeed, so very long ago...").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.6f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.6f))
      ),new int[]{0,80,100,50},0,1,0b1000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nDo you think the end will ever come for us, Brother?\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nDeath is the fate of all things, even us.\n").withStyle(ChatFormatting.DARK_GRAY)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nThen we should face it together.\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nThere's no one else I would rather have at my side.").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.9f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,1.4f))
      ),new int[]{0,60,80,60},0,1,0b1000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nDo you still call yourself the 'God of Knowledge', Little Brother?\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nI am the God of Knowledge! And what of it?\n").withStyle(ChatFormatting.DARK_GRAY)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\n*Chuckles*").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal(" It's cute that you try to mimic me.\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nI shall keep that title for eons to come, even if only to make you laugh.").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITH_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,1.4f))
      ),new int[]{0,80,60,60},0,1,0b1000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nLittle Brother, what schemes have you been planning? You have always been so very busy with your plots...\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nMy convictions remain the same as they have always been. And I wouldn't trust this Player with my current plans, not yet.\n").withStyle(ChatFormatting.DARK_GRAY)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nPlayer, whatever he has in store for you, remember you can always find your own path instead.").withStyle(ChatFormatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.8f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{0,100,120},0,1,0b1000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nEnderia, we never talked much in my youth. What would you do if you were released, what have you learned?\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nYou could have stopped them! Why have all this power if you do not wield it!?\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nI am not like you and Nul, showing power through violence. My influence is subtle, like the rising tide - it's far stronger than you realize.").withStyle(ChatFormatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.4f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{0,120,100},0,1,0b10000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nEnderia, you are not at fault for what happened in your life before, but cruelty should not beget more cruelty.\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nYou know nothing of what it was like! Being enslaved and forced to slaughter everything!\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nYou must never have visited the Overworld and seen what is done to my kind. Maybe this Player will show you sometime.").withStyle(ChatFormatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.4f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f))
      ),new int[]{0,120,100},0,1,0b10000));
      
      DialogHelper helper = new DialogHelper(dialogOptions,conditions);
      helper.sendDialog(List.of(player),helper.getWeightedResult(),true);
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal(" Aequalis Scientia").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nI believe I have solved two mysteries in one! The entity that powers my Transmutation Altar is a Divine creature that calls themselves Equayus, the God of Balance, Trade, and Wisdom.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Aequalis Scientia").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nThe transmutations that I perform are actually an implicit barter with Equayus for items of equal value. They were kind enough to trade a few of my Sovereign Catalysts for some Divine energy to use in another Catalyst.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Aequalis Scientia").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nFrom there I was able to transmute a stone with Divine energy. Equayus was impressed with my understanding and has imbued the stone with their rune. This stone is the final piece to the Altar").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Aequalis Scientia").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nand is used in the keystone position to conduct advanced transmutations.\n\nEquayus told me that I have a solid grasp on the value of materials, but need to learn the value of knowledge.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Aequalis Scientia").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nThe Aequalis Scientia is supposed to guide me to that realization by allowing me to exchange skills for others. Sometimes Equayus offers additional wisdom through the stone.\n\nI can use the Aequalis in conjunction with two ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Aequalis Scientia").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nof my own Arcane Items and some reagents to transfer the skills I have learned from one item into skills I can learn for the other.\n\nHowever, the stone has a limited amount of energy and can only do this a few times.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Aequalis Scientia").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nMy connection with Equayus also allows me to attune the Aequalis to a single transmutation, and they will help me perform the transmutation at will for half of the normal reagents.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class AequalisScientiaItem extends ArcanaPolymerItem {
      public AequalisScientiaItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(entity instanceof ServerPlayer player)) return;
         
         if(Math.random() < 0.0000075){ // 0.0000075
            inventoryDialog(player);
         }
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         String transmutationId = getStringProperty(stack,TRANSMUTATION_TAG);
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.SUCCESS_SERVER;
         
         if(transmutationId.isEmpty()){
            TransmutationAltarRecipeGui transmutationGui = new TransmutationAltarRecipeGui(player,null, Optional.empty());
            transmutationGui.enableSelectionMode(stack);
            transmutationGui.buildPage();
            transmutationGui.open();
         }else{
            Optional<TransmutationRecipe> recipeOpt = RecipeManager.TRANSMUTATION_RECIPES.stream().filter(r -> r.getId().equals(transmutationId)).findAny();
            if(recipeOpt.isPresent()){
               TransmutationRecipe recipe = recipeOpt.get();
               List<ItemStack> results;
               List<ItemStack> items = null;
               
               if(recipe instanceof CommutativeTransmutationRecipe commieRecipe){
                  if(commieRecipe.validCommunalInput(playerEntity.getItemInHand(InteractionHand.OFF_HAND))){
                     ItemStack input = getAndSplitValidCommunalStack(stack,commieRecipe,playerEntity.getItemInHand(InteractionHand.OFF_HAND), player);
                     if(input == null){
                        player.displayClientMessage(Component.literal("You do not have a valid input item.").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),false);
                        return InteractionResult.SUCCESS_SERVER;
                     }
                     ItemStack reagent1 = getAndSplitValidReagent1(stack,recipe,player);
                     if(reagent1 == null){
                        MinecraftUtils.returnItems(new SimpleContainer(input),player);
                        player.displayClientMessage(Component.literal("").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC).append(Component.literal("You do not have enough ")).append(recipe.getExampleReagent1().getHoverName()),false);
                        return InteractionResult.SUCCESS_SERVER;
                     }
                     ItemStack reagent2 = getAndSplitValidReagent2(stack,recipe,player);
                     if(reagent2 == null){
                        MinecraftUtils.returnItems(new SimpleContainer(input),player);
                        MinecraftUtils.returnItems(new SimpleContainer(reagent1),player);
                        player.displayClientMessage(Component.literal("").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC).append(Component.literal("You do not have enough ")).append(recipe.getExampleReagent2().getHoverName()),false);
                        return InteractionResult.SUCCESS_SERVER;
                     }
                     ItemStack focus = playerEntity.getItemInHand(InteractionHand.OFF_HAND).split(1);
                     
                     items = List.of(input,focus,reagent1,reagent2,stack);
                     results = commieRecipe.doTransmutation(input,focus,reagent1,reagent2,stack,player);
                  }else{
                     results = null;
                     player.displayClientMessage(Component.literal("Your offhand must be a valid focus item.").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),false);
                     return InteractionResult.SUCCESS_SERVER;
                  }
               }else if(recipe instanceof InfusionTransmutationRecipe infusionRecipe){
                  ItemStack input = getAndSplitValidStack(stack,infusionRecipe.getInputCount(),infusionRecipe.getInputPredicate(),player,true);
                  if(input == null){
                     player.displayClientMessage(Component.literal("").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC).append(Component.literal("You do not have enough ")).append(infusionRecipe.getInputName()),false);
                     return InteractionResult.SUCCESS_SERVER;
                  }
                  ItemStack reagent1 = getAndSplitValidReagent1(stack,recipe,player);
                  if(reagent1 == null){
                     MinecraftUtils.returnItems(new SimpleContainer(input),player);
                     player.displayClientMessage(Component.literal("").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC).append(Component.literal("You do not have enough ")).append(recipe.getExampleReagent1().getHoverName()),false);
                     return InteractionResult.SUCCESS_SERVER;
                  }
                  ItemStack reagent2 = getAndSplitValidReagent2(stack,recipe,player);
                  if(reagent2 == null){
                     MinecraftUtils.returnItems(new SimpleContainer(input),player);
                     MinecraftUtils.returnItems(new SimpleContainer(reagent1),player);
                     player.displayClientMessage(Component.literal("").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC).append(Component.literal("You do not have enough ")).append(recipe.getExampleReagent2().getHoverName()),false);
                     return InteractionResult.SUCCESS_SERVER;
                  }
                  
                  items = List.of(input, ItemStack.EMPTY,reagent1,reagent2,stack);
                  results = infusionRecipe.doTransmutation(input,null,reagent1,reagent2,stack,player);
               }else if(recipe instanceof PermutationTransmutationRecipe permutationRecipe){
                  ItemStack input = getAndSplitValidStack(stack,permutationRecipe.getInput(),player,true);
                  if(input == null){
                     player.displayClientMessage(Component.literal("").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC).append(Component.literal("You do not have enough ")).append(permutationRecipe.getInput().getHoverName()),false);
                     return InteractionResult.SUCCESS_SERVER;
                  }
                  ItemStack reagent1 = getAndSplitValidReagent1(stack,recipe,player);
                  if(reagent1 == null){
                     MinecraftUtils.returnItems(new SimpleContainer(input),player);
                     player.displayClientMessage(Component.literal("").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC).append(Component.literal("You do not have enough ")).append(recipe.getExampleReagent1().getHoverName()),false);
                     return InteractionResult.SUCCESS_SERVER;
                  }
                  ItemStack reagent2 = getAndSplitValidReagent2(stack,recipe,player);
                  if(reagent2 == null){
                     MinecraftUtils.returnItems(new SimpleContainer(input),player);
                     MinecraftUtils.returnItems(new SimpleContainer(reagent1),player);
                     player.displayClientMessage(Component.literal("").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC).append(Component.literal("You do not have enough ")).append(recipe.getExampleReagent2().getHoverName()),false);
                     return InteractionResult.SUCCESS_SERVER;
                  }
                  
                  items = List.of(input, ItemStack.EMPTY,reagent1,reagent2,stack);
                  results = permutationRecipe.doTransmutation(input,null,reagent1,reagent2,stack,player);
               }else if(recipe instanceof AequalisUnattuneTransmutationRecipe unattuneRecipe){
                  Predicate<ItemStack> aeqPred = aeq -> !getStringProperty(aeq,TRANSMUTATION_TAG).isEmpty();
                  ItemStack input = getAndSplitValidStack(stack,ArcanaRegistry.AEQUALIS_SCIENTIA.getPrefItem(),aeqPred,player,true);
                  if(input == null){
                     player.displayClientMessage(Component.literal("").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC).append(Component.literal("You do not have a valid ")).append(ArcanaRegistry.AEQUALIS_SCIENTIA.getTranslatedName()),false);
                     return InteractionResult.SUCCESS_SERVER;
                  }
                  ItemStack reagent1 = getAndSplitValidReagent1(stack,recipe,player);
                  if(reagent1 == null){
                     MinecraftUtils.returnItems(new SimpleContainer(input),player);
                     player.displayClientMessage(Component.literal("").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC).append(Component.literal("You do not have enough ")).append(recipe.getExampleReagent1().getHoverName()),false);
                     return InteractionResult.SUCCESS_SERVER;
                  }
                  ItemStack reagent2 = getAndSplitValidReagent2(stack,recipe,player);
                  if(reagent2 == null){
                     MinecraftUtils.returnItems(new SimpleContainer(input),player);
                     MinecraftUtils.returnItems(new SimpleContainer(reagent1),player);
                     player.displayClientMessage(Component.literal("").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC).append(Component.literal("You do not have enough ")).append(recipe.getExampleReagent2().getHoverName()),false);
                     return InteractionResult.SUCCESS_SERVER;
                  }
                  
                  items = List.of(input, ItemStack.EMPTY,reagent1,reagent2,stack);
                  results = unattuneRecipe.doTransmutation(input,null,reagent1,reagent2,stack,player);
               }else{
                  results = null;
               }
               
               if(items != null && results != null){
                  playerEntity.getInventory().removeItem(stack);
                  Vec3 center = player.position().add(player.getLookAngle().multiply(3,0,3)).add(0,2,0);
                  ArcanaEffectUtils.aequalisTransmuteAnim(player.level(),center,0,player.getRotationVector(),1,items.get(0),items.get(1),items.get(2),items.get(3),items.get(4));
                  
                  BorisLib.addTickTimerCallback(player.level(), new GenericTimer(500, () -> {
                     for(ItemStack result : results){
                        if(result.is(ArcanaRegistry.AEQUALIS_SCIENTIA.getItem())){
                           ArcanaNovum.data(player).addCraftedSilent(result);
                           ArcanaAchievements.grant(player,ArcanaAchievements.PRICE_OF_KNOWLEDGE.id);
                        }
                        Containers.dropItemStack(world, center.x,center.y,center.z,result);
                     }
                     ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_AEQUALIS_SCIENTIA_ATTUNED_TRANSMUTE));
                  }));
               }
            }else{
               putProperty(stack, AequalisScientia.TRANSMUTATION_TAG,"");
            }
         }
         return InteractionResult.SUCCESS_SERVER;
      }
      
      
      private ItemStack getAndSplitValidReagent1(ItemStack aequalis, TransmutationRecipe recipe, ServerPlayer player){
         Inventory inventory = player.getInventory();
         
         for(int i = 0; i < inventory.getContainerSize(); i++){
            if(i == Inventory.SLOT_OFFHAND) continue;
            ItemStack invStack = inventory.getItem(i);
            if(invStack.equals(aequalis)) continue;
            
            ItemStack computedReagent1 = recipe.getComputedReagent1(invStack,-1);
            if(computedReagent1 == null) continue;
            int needed = computedReagent1.getCount();
            if(invStack.getCount() >= needed){
               return invStack.split(needed);
            }
         }
         return null;
      }
      
      private ItemStack getAndSplitValidReagent2(ItemStack aequalis, TransmutationRecipe recipe, ServerPlayer player){
         Inventory inventory = player.getInventory();
         
         for(int i = 0; i < inventory.getContainerSize(); i++){
            if(i == Inventory.SLOT_OFFHAND) continue;
            ItemStack invStack = inventory.getItem(i);
            if(invStack.equals(aequalis)) continue;
            
            ItemStack computedReagent1 = recipe.getComputedReagent2(invStack,-1);
            if(computedReagent1 == null) continue;
            int needed = computedReagent1.getCount();
            if(invStack.getCount() >= needed){
               return invStack.split(needed);
            }
         }
         return null;
      }
      
      private ItemStack getAndSplitValidCommunalStack(ItemStack aequalis, CommutativeTransmutationRecipe commieRecipe, ItemStack focus, ServerPlayer player){
         Inventory inventory = player.getInventory();
         
         for(int i = 0; i < inventory.getContainerSize(); i++){
            if(i == Inventory.SLOT_OFFHAND) continue;
            ItemStack invStack = inventory.getItem(i);
            if(invStack.equals(aequalis)) continue;
            if(invStack.is(focus.getItem())) continue;
            if(!commieRecipe.validCommunalInput(invStack)) continue;
            
            if(invStack.getCount() >= 1){
               int splitAmount = invStack.getCount();
               return invStack.split(splitAmount);
            }
         }
         return null;
      }
      
      private ItemStack getAndSplitValidStack(ItemStack aequalis, ItemStack stack, ServerPlayer player, boolean repeat){
         Inventory inventory = player.getInventory();
         
         for(int i = 0; i < inventory.getContainerSize(); i++){
            if(i == Inventory.SLOT_OFFHAND) continue;
            ItemStack invStack = inventory.getItem(i);
            if(invStack.equals(aequalis)) continue;
            
            if(invStack.is(stack.getItem()) && invStack.getCount() >= stack.getCount()){
               int splitAmount = repeat ? stack.getCount() * (invStack.getCount() / stack.getCount()) : stack.getCount();
               return invStack.split(splitAmount);
            }
         }
         return null;
      }
      
      private ItemStack getAndSplitValidStack(ItemStack aequalis, ItemStack stack, Predicate<ItemStack> pred, ServerPlayer player, boolean repeat){
         Inventory inventory = player.getInventory();
         
         for(int i = 0; i < inventory.getContainerSize(); i++){
            if(i == Inventory.SLOT_OFFHAND) continue;
            ItemStack invStack = inventory.getItem(i);
            if(invStack.equals(aequalis)) continue;
            if(!pred.test(invStack)) continue;
            
            if(invStack.is(stack.getItem()) && invStack.getCount() >= stack.getCount()){
               int splitAmount = repeat ? stack.getCount() * (invStack.getCount() / stack.getCount()) : stack.getCount();
               return invStack.split(splitAmount);
            }
         }
         return null;
      }
      
      private ItemStack getAndSplitValidStack(ItemStack aequalis, int neededCount, Predicate<ItemStack> pred, ServerPlayer player, boolean repeat){
         Inventory inventory = player.getInventory();
         
         for(int i = 0; i < inventory.getContainerSize(); i++){
            if(i == Inventory.SLOT_OFFHAND) continue;
            ItemStack invStack = inventory.getItem(i);
            if(invStack.equals(aequalis)) continue;
            if(!pred.test(invStack)) continue;
            
            if(invStack.getCount() >= neededCount){
               int splitAmount = repeat ? neededCount * (invStack.getCount() / neededCount) : neededCount;
               return invStack.split(splitAmount);
            }
         }
         return null;
      }
   }
}

