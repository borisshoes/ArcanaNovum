package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ArcaneTome extends ArcanaItem {
   public static final String ID = "arcane_tome";
   
   public static final String DISPLAY_TAG = "arcanaItemId";
   public static final String FORGE_TAG = "forgeCraftTick";
   public static final String TOME_TAG = "tomeCraftTick";
   
   public ArcaneTome(){
      id = ID;
      name = "Tome of Arcana Novum";
      rarity = ArcanaRarity.MUNDANE;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS};
      itemVersion = 1;
      vanillaItem = Items.KNOWLEDGE_BOOK;
      item = new ArcaneTomeItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_EYE_OF_ENDER, ResearchTasks.ADVANCEMENT_ENCHANT_ITEM};
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("The knowledge within shall be your ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("guide").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("There is so much ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("new magic").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" to explore...").withStyle(ChatFormatting.GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("Right click").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" to open the tome.").withStyle(ChatFormatting.GREEN)));
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("  Tome of Arcana           Novum").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nThis new type of paper has quite a few interesting properties.\n\nIt allows the inscription of active arcane elements. It makes an excellent parchment for this ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Tome of Arcana           Novum").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), Component.literal("\nnew notebook.\n\nEnchanting the pages together with an additional Eye of Ender should bind the whole Tome together nicely.\n\nIt is here that I shall \nscribe all the secrets ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Tome of Arcana           Novum").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), Component.literal("\nof this Arcana Novum that I seek to uncover.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class ArcaneTomeItem extends ArcanaPolymerItem {
      public ArcaneTomeItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
         ArcaneTomeGui tomeGui = new ArcaneTomeGui(player, ArcaneTomeGui.TomeMode.PROFILE).addModes();
         tomeGui.buildAndOpen();
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         
         removeProperty(stack, FORGE_TAG);
      }
   }
}

