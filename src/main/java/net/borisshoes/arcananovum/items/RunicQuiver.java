package net.borisshoes.arcananovum.items;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItemContainer;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.gui.quivers.QuiverGui;
import net.borisshoes.arcananovum.gui.quivers.QuiverSlot;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
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

public class RunicQuiver extends QuiverItem implements ArcanaItemContainer.ArcanaItemContainerHaver {
	public static final String ID = "runic_quiver";
   
   public static final int size = 9;
   private static final int[] refillReduction = {0,100,200,400,600,900};
   private static final double[] efficiencyChance = {0,.05,.1,.15,.2,.3};
   private static final Item textureItem = Items.ARROW;
   
   public RunicQuiver(){
      id = ID;
      name = "Runic Quiver";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ITEMS};
      color = Formatting.LIGHT_PURPLE;
      vanillaItem = Items.LEATHER;
      itemVersion = 1;
      item = new RunicQuiverItem(addArcanaItemComponents(new Item.Settings().maxCount(1)));
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.LIGHT_PURPLE);
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_OVERFLOWING_QUIVER,ResearchTasks.UNLOCK_RUNIC_MATRIX,ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER,ResearchTasks.UNLOCK_STELLAR_CORE,ResearchTasks.CONCENTRATION_DAMAGE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("runes ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("engraved ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("upon the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("quiver ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("hum in the presence of ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Runic Arrows").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Runic Arrows").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" placed within the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("quiver ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("regenerate ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("over ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("time").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Arrows ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("take reduced ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("concentration ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("when in the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("quiver").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" to put ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Arrows ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("in the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("quiver").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Left Click").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" with a ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Runic Bow").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" to swap which ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Runic Arrow").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" will be shot.").formatted(Formatting.DARK_PURPLE)));
      
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
                  .append(Text.literal("Contents: ").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.literal("Empty").formatted(Formatting.DARK_PURPLE)));
         }else{
            lore.add(Text.literal(""));
            lore.add(Text.literal("").append(Text.literal("Contents: ").formatted(Formatting.LIGHT_PURPLE)));
            for(int i = 0; i < inv.size(); i++){
               ItemStack stack = inv.getStack(i);
               if(stack.isEmpty()) continue;
               Style style = stack.getName().getStyle();
               boolean keepStyle = style.isBold() || style.isItalic() || style.isObfuscated() || style.isUnderlined() || style.isStrikethrough() || (style.getColor() != null && style.getColor().getRgb() != Formatting.WHITE.getColorValue());
               MutableText name = stack.getName().copy();
               if(!keepStyle) name = name.formatted(Formatting.DARK_PURPLE);
               
               if(stack.getCount() == 1 && stack.getMaxCount() == 1){
                  lore.add(Text.literal("")
                        .append(Text.literal(" - ").formatted(Formatting.LIGHT_PURPLE))
                        .append(name));
               }else{
                  lore.add(Text.literal("")
                        .append(Text.literal(" - ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(stack.getCount()+"x ").formatted(Formatting.DARK_AQUA))
                        .append(name));
               }
            }
         }
      }
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   protected int getRefillMod(ItemStack item){ // Ticks between arrow refill, once per minute
      int refillLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,"quiver_duplication"));
      return 1200 - refillReduction[refillLvl];
   }
   
   @Override
   protected double getEfficiencyMod(ItemStack item){ // Ticks between arrow refill, once per two and a half minutes
      int effLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,"runic_bottomless"));
      return efficiencyChance[effLvl];
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv, StarlightForgeBlockEntity starlightForge){
      ItemStack quiverStack = inv.getStack(12); // Should be the old quiver
      ItemStack newArcanaItem = getNewItem();
      newArcanaItem.set(DataComponentTypes.CONTAINER,quiverStack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT));
      ArcanaAugments.copyAugment(quiverStack,newArcanaItem,ArcanaAugments.OVERFLOWING_BOTTOMLESS.id,ArcanaAugments.RUNIC_BOTTOMLESS.id);
      ArcanaAugments.copyAugment(quiverStack,newArcanaItem,ArcanaAugments.ABUNDANT_AMMO.id,ArcanaAugments.QUIVER_DUPLICATION.id);
      return newArcanaItem;
   }
   
   private RunicQuiver getOuter(){
      return this;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.LEATHER,32);
      ArcanaIngredient b = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.INFINITY),1));
      ArcanaIngredient c = new ArcanaIngredient(Items.NETHER_STAR,2);
      ArcanaIngredient g = new ArcanaIngredient(Items.NETHERITE_INGOT,1);
      GenericArcanaIngredient h = new GenericArcanaIngredient(ArcanaRegistry.RUNIC_MATRIX,1);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.OVERFLOWING_QUIVER,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withCore().withFletchery().withEnchanter().withAnvil());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("    Runic Quiver").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nMy improvements upon the Overflowing Quiver have been completed and now the quiver is capable of sending some of my Arcana to Runic Arrows within.\nI even managed to make the quiver take a reduced amount of").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Runic Quiver").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD),Text.literal("\nconcentration, allowing for more Runic Arrows to be stored without overburdening my mind.\n\nThe quiver acts the same as its base counterpart, just with its ability to store Runic Arrows and a quicker restock time.").formatted(Formatting.BLACK)));
      return list;
   }
   
   @Override
   public ArcanaItemContainer getArcanaItemContainer(ItemStack item){
      int size = 9;
      ContainerComponent arrows = item.getOrDefault(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
      SimpleInventory inv = new SimpleInventory(size);
      List<ItemStack> streamList = arrows.streamNonEmpty().toList();
      for(int i = 0; i < streamList.size(); i++){
         inv.setStack(i,streamList.get(i));
      }
      double concMod = ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.SHUNT_RUNES.id) > 0 ? 0.25 : 0.5;
      
      return new ArcanaItemContainer(inv, size,3, "RQ", "Runic Quiver", concMod);
   }
   
   public class RunicQuiverItem extends ArcanaPolymerItem {
      public RunicQuiverItem(Item.Settings settings){
         super(getThis(),settings);
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
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         if(world.getServer().getTicks() % getRefillMod(stack) == 0) refillArrow(player, stack);
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
         // Open GUI
         if(playerEntity instanceof ServerPlayerEntity player){
            ItemStack stack = playerEntity.getStackInHand(hand);
            QuiverGui gui = new QuiverGui(player, getOuter(), stack,true);
            gui.build();
            gui.open();
         }
         return ActionResult.SUCCESS;
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
               if(!QuiverSlot.isValidItem(otherStack,true)){
                  SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_BUNDLE_INSERT_FAIL,1f,1f);
               }else{
                  int size = 9;
                  int count = otherStack.getCount();
                  Pair<ContainerComponent,ItemStack> addPair = MiscUtils.tryAddStackToContainerComp(beltItems,size,otherStack);
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

