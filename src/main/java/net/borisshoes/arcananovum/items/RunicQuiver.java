package net.borisshoes.arcananovum.items;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItemContainer;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.gui.quivers.QuiverGui;
import net.borisshoes.arcananovum.gui.quivers.QuiverSlot;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class RunicQuiver extends QuiverItem implements ArcanaItemContainer.ArcanaItemContainerHaver {
   public static final String ID = "runic_quiver";
   
   public static final int size = 9;
   private static final int[] refillReduction = {0, 100, 200, 400, 600, 900};
   private static final double[] efficiencyChance = {0, .05, .1, .15, .2, .3};
   private static final Item textureItem = Items.ARROW;
   
   public RunicQuiver(){
      id = ID;
      name = "Runic Quiver";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS};
      color = ChatFormatting.LIGHT_PURPLE;
      vanillaItem = Items.LEATHER;
      itemVersion = 1;
      item = new RunicQuiverItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_OVERFLOWING_QUIVER, ResearchTasks.UNLOCK_RUNIC_MATRIX, ResearchTasks.UNLOCK_RADIANT_FLETCHERY, ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER, ResearchTasks.UNLOCK_STELLAR_CORE, ResearchTasks.CONCENTRATION_DAMAGE};
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("runes ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("engraved ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("upon the ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("quiver ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("hum in the presence of ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Runic Arrows").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Runic Arrows").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" placed within the ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("quiver ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("regenerate ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("over ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("time").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Arrows ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("take reduced ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("concentration ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("when in the ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("quiver").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" to put ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Arrows ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("in the ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("quiver").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Left Click").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" with a ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Runic Bow").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" to swap which ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Runic Arrow").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" will be shot.").withStyle(ChatFormatting.DARK_PURPLE)));
      
      if(itemStack != null){
         ItemContainerContents arrowItems = itemStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
         SimpleContainer inv = new SimpleContainer(9);
         List<ItemStack> streamList = arrowItems.nonEmptyItemCopyStream().toList();
         for(int i = 0; i < streamList.size(); i++){
            inv.setItem(i, streamList.get(i));
         }
         
         if(inv.isEmpty()){
            lore.add(Component.literal(""));
            lore.add(Component.literal("")
                  .append(Component.literal("Contents: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                  .append(Component.literal("Empty").withStyle(ChatFormatting.DARK_PURPLE)));
         }else{
            lore.add(Component.literal(""));
            lore.add(Component.literal("").append(Component.literal("Contents: ").withStyle(ChatFormatting.LIGHT_PURPLE)));
            for(int i = 0; i < inv.getContainerSize(); i++){
               ItemStack stack = inv.getItem(i);
               if(stack.isEmpty()) continue;
               Style style = stack.getHoverName().getStyle();
               boolean keepStyle = style.isBold() || style.isItalic() || style.isObfuscated() || style.isUnderlined() || style.isStrikethrough() || (style.getColor() != null && style.getColor().getValue() != ChatFormatting.WHITE.getColor());
               MutableComponent name = stack.getHoverName().copy();
               if(!keepStyle) name = name.withStyle(ChatFormatting.DARK_PURPLE);
               
               if(stack.getCount() == 1 && stack.getMaxStackSize() == 1){
                  lore.add(Component.literal("")
                        .append(Component.literal(" - ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(name));
               }else{
                  lore.add(Component.literal("")
                        .append(Component.literal(" - ").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(stack.getCount() + "x ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(name));
               }
            }
         }
      }
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   protected int getRefillMod(ItemStack item){ // Ticks between arrow refill, once per minute
      int refillLvl = ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.QUIVER_DUPLICATION);
      int baseCooldown = ArcanaNovum.CONFIG.getInt(ArcanaConfig.RUNIC_QUIVER_RESTOCK_TIME);
      int cooldownReduction = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.RUNIC_QUIVER_RESTOCK_TIME_PER_LVL).get(refillLvl);
      return Math.max(1, baseCooldown - cooldownReduction);
   }
   
   @Override
   protected double getEfficiencyMod(ItemStack item){
      int effLvl = ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.RUNIC_BOTTOMLESS);
      return ArcanaNovum.CONFIG.getIntList(ArcanaConfig.QUIVER_EFFICIENCY_PER_LVL).get(effLvl);
   }
   
   @Override
   public ItemStack forgeItem(Container inv, List<Integer> centerpieces, StarlightForgeBlockEntity starlightForge){
      ItemStack newArcanaItem = getNewItem();
      if(centerpieces.isEmpty()) return newArcanaItem;
      ItemStack quiverStack = inv.getItem(centerpieces.getFirst()); // Should be the old quiver
      
      newArcanaItem.set(DataComponents.CONTAINER, quiverStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
      ArcanaAugments.copyAugment(quiverStack, newArcanaItem, ArcanaAugments.OVERFLOWING_BOTTOMLESS, ArcanaAugments.RUNIC_BOTTOMLESS);
      ArcanaAugments.copyAugment(quiverStack, newArcanaItem, ArcanaAugments.ABUNDANT_AMMO, ArcanaAugments.QUIVER_DUPLICATION);
      return newArcanaItem;
   }
   
   private RunicQuiver getOuter(){
      return this;
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("    Runic Quiver").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nMy improvements upon the Overflowing Quiver have been completed and now the quiver is capable of sending some of my Arcana to Runic Arrows within.\nI even managed to make the quiver take a reduced amount of").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Runic Quiver").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), Component.literal("\nconcentration, allowing for more Runic Arrows to be stored without overburdening my mind.\n\nThe quiver acts the same as its base counterpart, just with its ability to store Runic Arrows and a quicker restock time.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   @Override
   public ArcanaItemContainer getArcanaItemContainer(ItemStack item){
      int size = 9;
      ItemContainerContents arrows = item.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
      SimpleContainer inv = new SimpleContainer(size);
      List<ItemStack> streamList = arrows.nonEmptyItemCopyStream().toList();
      for(int i = 0; i < streamList.size(); i++){
         inv.setItem(i, streamList.get(i));
      }
      double concMod = ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.SHUNT_RUNES) > 0 ? 0.25 : 0.5;
      
      return new ArcanaItemContainer(
            ArcanaRegistry.arcanaId(this.id),
            inv, size, 20,
            Component.literal("RQ"),
            getTranslatedName(),
            concMod);
   }
   
   public class RunicQuiverItem extends ArcanaPolymerItem {
      public RunicQuiverItem(){
         super(getThis());
      }
      
      @Override
      public Item getPolymerItem(ItemStack itemStack, PacketContext context){
         if(PolymerResourcePackUtils.hasMainPack(context)){
            return textureItem;
         }
         return super.getPolymerItem(itemStack, context);
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(entity instanceof ServerPlayer player)) return;
         if(world.getServer().getTickCount() % getRefillMod(stack) == 0)
            refillArrow(world.getServer(), player.getUUID(), stack);
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         // Open GUI
         if(playerEntity instanceof ServerPlayer player){
            ItemStack stack = playerEntity.getItemInHand(hand);
            QuiverGui gui = new QuiverGui(player, getOuter(), stack, true);
            gui.build();
            gui.open();
         }
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Override
      public boolean overrideOtherStackedOnMe(final ItemStack self, final ItemStack other, final Slot slot, final ClickAction clickAction, final Player playerEntity, final SlotAccess carriedItem){
         return super.arcanaBundleOtherStackedOnMe(self, other, slot, clickAction, playerEntity, carriedItem, 9, QuiverSlot.RUNIC_PREDICATE);
      }
      
      @Override
      public boolean overrideStackedOnOther(final ItemStack self, final Slot slot, final ClickAction clickAction, final Player playerEntity){
         return super.arcanaBundleStackedOnOther(self, slot, clickAction, playerEntity, 9, QuiverSlot.RUNIC_PREDICATE);
      }
   }
}

