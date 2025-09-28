package net.borisshoes.arcananovum.items;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.gui.quivers.QuiverGui;
import net.borisshoes.arcananovum.gui.quivers.QuiverSlot;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class OverflowingQuiver extends QuiverItem{
	public static final String ID = "overflowing_quiver";
   
   private static final int[] refillReduction = {0,300,600,900,1200,1800};
   private static final double[] efficiencyChance = {0,.05,.1,.15,.2,.3};
   private static final Item textureItem = Items.ARROW;
   
   public OverflowingQuiver(){
      id = ID;
      name = "Overflowing Quiver";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ITEMS};
      color = Formatting.DARK_AQUA;
      vanillaItem = Items.RABBIT_HIDE;
      itemVersion = 1;
      item = new OverflowingQuiverItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.DARK_AQUA);
      researchTasks = new RegistryKey[]{ResearchTasks.ADVANCEMENT_SHOOT_ARROW,ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.OBTAIN_SPECTRAL_ARROW,ResearchTasks.OBTAIN_TIPPED_ARROW,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.UNLOCK_STELLAR_CORE,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("One can never have enough ").formatted(Formatting.AQUA))
            .append(Text.literal("arrows").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("...").formatted(Formatting.AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("Tipped Arrows").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" placed within the ").formatted(Formatting.AQUA))
            .append(Text.literal("quiver ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("restock ").formatted(Formatting.BLUE))
            .append(Text.literal("over ").formatted(Formatting.AQUA))
            .append(Text.literal("time").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.BLUE))
            .append(Text.literal(" to put ").formatted(Formatting.AQUA))
            .append(Text.literal("arrows ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("in the ").formatted(Formatting.AQUA))
            .append(Text.literal("quiver").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("Left Click ").formatted(Formatting.BLUE))
            .append(Text.literal("with a ").formatted(Formatting.AQUA))
            .append(Text.literal("bow ").formatted(Formatting.BLUE))
            .append(Text.literal("to ").formatted(Formatting.AQUA))
            .append(Text.literal("swap ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("which type of ").formatted(Formatting.AQUA))
            .append(Text.literal("arrow ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("will be shot.").formatted(Formatting.AQUA)));
      
      if(itemStack != null){
         ContainerComponent arrowItems = itemStack.getOrDefault(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
         SimpleInventory inv = new SimpleInventory(9);
         List<ItemStack> streamList = arrowItems.streamNonEmpty().toList();
         for(int i = 0; i < streamList.size(); i++){
            inv.setStack(i,streamList.get(i));
         }
         
         if(inv.isEmpty()){
            lore.add(Text.literal(""));
            lore.add(Text.literal("")
                  .append(Text.literal("Contents: ").formatted(Formatting.DARK_AQUA))
                  .append(Text.literal("Empty").formatted(Formatting.AQUA)));
         }else{
            lore.add(Text.literal(""));
            lore.add(Text.literal("").append(Text.literal("Contents: ").formatted(Formatting.DARK_AQUA)));
            for(int i = 0; i < inv.size(); i++){
               ItemStack stack = inv.getStack(i);
               if(stack.isEmpty()) continue;
               Style style = stack.getName().getStyle();
               boolean keepStyle = style.isBold() || style.isItalic() || style.isObfuscated() || style.isUnderlined() || style.isStrikethrough() || (style.getColor() != null && style.getColor().getRgb() != Formatting.WHITE.getColorValue());
               MutableText name = stack.getName().copy();
               if(!keepStyle) name = name.formatted(Formatting.AQUA);
               
               if(stack.getCount() == 1 && stack.getMaxCount() == 1){
                  lore.add(Text.literal("")
                        .append(Text.literal(" - ").formatted(Formatting.DARK_AQUA))
                        .append(name));
               }else{
                  lore.add(Text.literal("")
                        .append(Text.literal(" - ").formatted(Formatting.DARK_AQUA))
                        .append(Text.literal(stack.getCount()+"x ").formatted(Formatting.BLUE))
                        .append(name));
               }
            }
         }
      }
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   protected int getRefillMod(ItemStack item){ // Ticks between arrow refill, once per two and a half minutes
      int refillLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,"abundant_ammo"));
      return 3000 - refillReduction[refillLvl];
   }
   
   @Override
   protected double getEfficiencyMod(ItemStack item){ // Ticks between arrow refill, once per two and a half minutes
      int effLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,"overflowing_bottomless"));
      return efficiencyChance[effLvl];
   }
   
   private OverflowingQuiver getOuter(){
      return this;
   }
   
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.NETHER_STAR,1);
      ArcanaIngredient b = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MinecraftUtils.getEnchantment(Enchantments.INFINITY),1));
      ArcanaIngredient c = new ArcanaIngredient(Items.RABBIT_HIDE,12);
      ArcanaIngredient h = new ArcanaIngredient(Items.SPECTRAL_ARROW,32);
      ArcanaIngredient m = new ArcanaIngredient(Items.NETHERITE_INGOT,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,c,h,c,b},
            {c,h,m,h,c},
            {b,c,h,c,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withAnvil().withFletchery().withCore().withEnchanter());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("Overflowing Quiver").formatted(Formatting.DARK_AQUA,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nMore experienced archers keep a variety of arrows on hand. However, it is difficult to switch between them in the heat of a fight. This quiver has slots that not only save space, and keep arrows ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("Overflowing Quiver").formatted(Formatting.DARK_AQUA,Formatting.BOLD),Text.literal("\norganized, but it also contains a mechanism to help guide the archer’s hand to the desired arrow type.\n\nPunching with any bow or crossbow, cycles the preferred arrows.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("Overflowing Quiver").formatted(Formatting.DARK_AQUA,Formatting.BOLD),Text.literal("\nI have also managed to unlock greater potential from the Infinity enchantment and imbued it within the quiver.\n\nThe quiver now slowly regenerates all arrows placed inside of it.\n").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("Overflowing Quiver").formatted(Formatting.DARK_AQUA,Formatting.BOLD),Text.literal("\nIt is worth noting that the quiver is not sturdy enough to channel Arcana to arrows placed inside, restricting Runic Arrows from being contained within.\n\nI am looking into possible improvements to this design to").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("Overflowing Quiver").formatted(Formatting.DARK_AQUA,Formatting.BOLD),Text.literal("\naccommodate more powerful projectiles. The Quiver can also be accessed similar to a Bundle in my inventory.").formatted(Formatting.BLACK)));
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
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         if(world.getServer().getTicks() % getRefillMod(stack) == 0) refillArrow(player, stack);
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
         // Open GUI
         if(playerEntity instanceof ServerPlayerEntity player){
            ItemStack stack = playerEntity.getStackInHand(hand);
            QuiverGui gui = new QuiverGui(player, getOuter(), stack,false);
            gui.build();
            gui.open();
         }
         return ActionResult.SUCCESS_SERVER;
      }
      
      @Override
      public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity playerEntity, StackReference cursorStackReference) {
         if(playerEntity.getWorld().isClient || !(playerEntity instanceof ServerPlayerEntity player)) return false;
         if (clickType == ClickType.LEFT && otherStack.isEmpty()) {
            return false;
         } else {
            ContainerComponent beltItems = stack.getOrDefault(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
            List<ItemStack> beltList = beltItems.stream().toList();
            
            if(clickType == ClickType.LEFT && !otherStack.isEmpty()){ // Try insert
               if(!QuiverSlot.isValidItem(otherStack,false)){
                  SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_BUNDLE_INSERT_FAIL,1f,1f);
               }else{
                  int size = 9;
                  int count = otherStack.getCount();
                  Pair<ContainerComponent,ItemStack> addPair = MinecraftUtils.tryAddStackToContainerComp(beltItems,size,otherStack);
                  if(count == addPair.getRight().getCount()){
                     SoundUtils.playSongToPlayer(player,SoundEvents.ITEM_BUNDLE_INSERT_FAIL,1f,1f);
                  }else{
                     SoundUtils.playSongToPlayer(player,SoundEvents.ITEM_BUNDLE_INSERT,0.8F, 0.8F + player.getWorld().getRandom().nextFloat() * 0.4F);
                     stack.set(DataComponentTypes.CONTAINER,addPair.getLeft());
                  }
               }
               buildItemLore(stack,player.getServer());
               return true;
            }else if(clickType == ClickType.RIGHT && otherStack.isEmpty()){ // Try remove
               boolean found = false;
               for(ItemStack itemStack : beltList.reversed()){
                  if(!itemStack.isEmpty()){
                     cursorStackReference.set(itemStack.copyAndEmpty());
                     SoundUtils.playSongToPlayer(player,SoundEvents.ITEM_BUNDLE_REMOVE_ONE,0.8F, 0.8F + player.getWorld().getRandom().nextFloat() * 0.4F);
                     found = true;
                     break;
                  }
               }
               
               if(found){
                  stack.set(DataComponentTypes.CONTAINER,ContainerComponent.fromStacks(beltList));
                  buildItemLore(stack,player.getServer());
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

