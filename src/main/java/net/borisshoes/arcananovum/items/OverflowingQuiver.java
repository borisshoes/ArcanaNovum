package net.borisshoes.arcananovum.items;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.gui.quivers.QuiverGui;
import net.borisshoes.arcananovum.gui.quivers.QuiverSlot;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
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
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class OverflowingQuiver extends QuiverItem{
	public static final String ID = "overflowing_quiver";
   
   private static final Item textureItem = Items.ARROW;
   
   public OverflowingQuiver(){
      id = ID;
      name = "Overflowing Quiver";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS};
      color = ChatFormatting.DARK_AQUA;
      vanillaItem = Items.RABBIT_HIDE;
      itemVersion = 1;
      item = new OverflowingQuiverItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA);
      researchTasks = new ResourceKey[]{ResearchTasks.ADVANCEMENT_SHOOT_ARROW,ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.OBTAIN_SPECTRAL_ARROW,ResearchTasks.OBTAIN_TIPPED_ARROW,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.UNLOCK_STELLAR_CORE,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("One can never have enough ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("arrows").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("...").withStyle(ChatFormatting.AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("Tipped Arrows").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" placed within the ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("quiver ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("restock ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("over ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("time").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" to put ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("arrows ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("in the ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("quiver").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("Left Click ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("with a ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("bow ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("to ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("swap ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("which type of ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("arrow ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("will be shot.").withStyle(ChatFormatting.AQUA)));
      
      if(itemStack != null){
         ItemContainerContents arrowItems = itemStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
         SimpleContainer inv = new SimpleContainer(9);
         List<ItemStack> streamList = arrowItems.nonEmptyStream().toList();
         for(int i = 0; i < streamList.size(); i++){
            inv.setItem(i,streamList.get(i));
         }
         
         if(inv.isEmpty()){
            lore.add(Component.literal(""));
            lore.add(Component.literal("")
                  .append(Component.literal("Contents: ").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal("Empty").withStyle(ChatFormatting.AQUA)));
         }else{
            lore.add(Component.literal(""));
            lore.add(Component.literal("").append(Component.literal("Contents: ").withStyle(ChatFormatting.DARK_AQUA)));
            for(int i = 0; i < inv.getContainerSize(); i++){
               ItemStack stack = inv.getItem(i);
               if(stack.isEmpty()) continue;
               Style style = stack.getHoverName().getStyle();
               boolean keepStyle = style.isBold() || style.isItalic() || style.isObfuscated() || style.isUnderlined() || style.isStrikethrough() || (style.getColor() != null && style.getColor().getValue() != ChatFormatting.WHITE.getColor());
               MutableComponent name = stack.getHoverName().copy();
               if(!keepStyle) name = name.withStyle(ChatFormatting.AQUA);
               
               if(stack.getCount() == 1 && stack.getMaxStackSize() == 1){
                  lore.add(Component.literal("")
                        .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(name));
               }else{
                  lore.add(Component.literal("")
                        .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.literal(stack.getCount()+"x ").withStyle(ChatFormatting.BLUE))
                        .append(name));
               }
            }
         }
      }
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   protected int getRefillMod(ItemStack item){ // Ticks between arrow refill, once per two and a half minutes
      int refillLvl = ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.ABUNDANT_AMMO);
      int baseCooldown = ArcanaNovum.CONFIG.getInt(ArcanaConfig.OVERFLOWING_QUIVER_RESTOCK_TIME);
      int cooldownReduction = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.OVERFLOWING_QUIVER_RESTOCK_TIME_PER_LVL).get(refillLvl);
      return Math.max(1, baseCooldown - cooldownReduction);
   }
   
   @Override
   protected double getEfficiencyMod(ItemStack item){
      int effLvl = ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.OVERFLOWING_BOTTOMLESS);
      return ArcanaNovum.CONFIG.getIntList(ArcanaConfig.QUIVER_EFFICIENCY_PER_LVL).get(effLvl);
   }
   
   private OverflowingQuiver getOuter(){
      return this;
   }
   
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("Overflowing Quiver").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nMore experienced archers keep a variety of arrows on hand. However, it is difficult to switch between them in the heat of a fight. This quiver has slots that not only save space, and keep arrows ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Overflowing Quiver").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\norganized, but it also contains a mechanism to help guide the archer’s hand to the desired arrow type.\n\nPunching with any bow or crossbow, cycles the preferred arrows.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Overflowing Quiver").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nI have also managed to unlock greater potential from the Infinity enchantment and imbued it within the quiver.\n\nThe quiver now slowly regenerates all arrows placed inside of it.\n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Overflowing Quiver").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nIt is worth noting that the quiver is not sturdy enough to channel Arcana to arrows placed inside, restricting Runic Arrows from being contained within.\n\nI am looking into possible improvements to this design to").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Overflowing Quiver").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\naccommodate more powerful projectiles. The Quiver can also be accessed similar to a Bundle in my inventory.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class OverflowingQuiverItem extends ArcanaPolymerItem {
      public OverflowingQuiverItem(){
         super(getThis());
      }
      
      @Override
      public Item getPolymerItem(ItemStack itemStack, PacketContext context){
         if(PolymerResourcePackUtils.hasMainPack(context.getPlayer())){
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
         if(world.getServer().getTickCount() % getRefillMod(stack) == 0) refillArrow(world.getServer(), player.getUUID(), stack);
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         // Open GUI
         if(playerEntity instanceof ServerPlayer player){
            ItemStack stack = playerEntity.getItemInHand(hand);
            QuiverGui gui = new QuiverGui(player, getOuter(), stack,false);
            gui.build();
            gui.open();
         }
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Override
      public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player playerEntity, SlotAccess cursorStackReference) {
         if(playerEntity.level().isClientSide() || !(playerEntity instanceof ServerPlayer player)) return false;
         if (clickType == ClickAction.PRIMARY && otherStack.isEmpty()) {
            return false;
         } else {
            ItemContainerContents beltItems = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            List<ItemStack> beltList = beltItems.stream().toList();
            
            if(clickType == ClickAction.PRIMARY && !otherStack.isEmpty()){ // Try insert
               if(!QuiverSlot.isValidItem(otherStack,false)){
                  SoundUtils.playSongToPlayer(player, SoundEvents.BUNDLE_INSERT_FAIL,1f,1f);
               }else{
                  int size = 9;
                  int count = otherStack.getCount();
                  Tuple<ItemContainerContents, ItemStack> addPair = MinecraftUtils.tryAddStackToContainerComp(beltItems,size,otherStack);
                  if(count == addPair.getB().getCount()){
                     SoundUtils.playSongToPlayer(player, SoundEvents.BUNDLE_INSERT_FAIL,1f,1f);
                  }else{
                     SoundUtils.playSongToPlayer(player, SoundEvents.BUNDLE_INSERT,0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
                     stack.set(DataComponents.CONTAINER,addPair.getA());
                  }
               }
               buildItemLore(stack,player.level().getServer());
               return true;
            }else if(clickType == ClickAction.SECONDARY && otherStack.isEmpty()){ // Try remove
               boolean found = false;
               for(ItemStack itemStack : beltList.reversed()){
                  if(!itemStack.isEmpty()){
                     cursorStackReference.set(itemStack.copyAndClear());
                     SoundUtils.playSongToPlayer(player, SoundEvents.BUNDLE_REMOVE_ONE,0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
                     found = true;
                     break;
                  }
               }
               
               if(found){
                  stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(beltList));
                  buildItemLore(stack,player.level().getServer());
                  return true;
               }else{
                  return false;
               }
            }else{ // Move item
               return false;
            }
         }
      }
   }
}

