package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.EnderCrate;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.gui.clockworkmultitool.ClockworkMultitoolEnderGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ClockworkMultitool extends EnergyItem {
   public static final String ID = "clockwork_multitool";
   
   public static final String SAVED_TAG = "saved";
   
   public ClockworkMultitool(){
      id = ID;
      name = "Clockwork Multitool";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS};
      itemVersion = 0;
      vanillaItem = Items.CLOCK;
      item = new ClockworkMultitoolItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_GOLD_INGOT, ResearchTasks.ADVANCEMENT_CRAFTERS_CRAFTING_CRAFTERS};
   }
   
   @Override
   public ItemStack initializeArcanaTag(ItemStack stack){
      super.initializeArcanaTag(stack);
      putProperty(stack, MODE_TAG, "crafting");
      putProperty(stack, SAVED_TAG, "");
      putProperty(stack, EnderCrate.CHANNEL_TAG, EnderCrate.colorsToTag(EnderCrate.DEFAULT_CHANNEL));
      putProperty(stack, EnderCrate.LOCK_TAG, "");
      return stack;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      String curMode = getStringProperty(stack, MODE_TAG);
      String saved = getStringProperty(stack, SAVED_TAG);
      ListTag channel = getListProperty(stack, EnderCrate.CHANNEL_TAG);
      String lock = getStringProperty(stack, EnderCrate.LOCK_TAG);
      ItemStack newStack = super.updateItem(stack, server);
      putProperty(newStack, MODE_TAG, curMode);
      putProperty(newStack, SAVED_TAG, saved);
      putProperty(newStack, EnderCrate.CHANNEL_TAG, channel);
      putProperty(newStack, EnderCrate.LOCK_TAG, lock);
      return buildItemLore(newStack, server);
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("A series of ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("gears ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("and ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("mechanisms ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("whirl around.").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("Every ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("workbench").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" you need can now fit in your ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("pocket").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("Left Click").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("cycles").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" the ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("active ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("workbench.").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("opens ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("the ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("active").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" workbench.").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right Click").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("opens ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("the ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("saved ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("workbench.").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right Click").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" in offhand").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("saves ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("the ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("active ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("workbench.").withStyle(ChatFormatting.YELLOW)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("Clockwork Multitool").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nCarrying crafting tables around has always been a chore, even more so if I need something like a grindstone or stonecutter. \n\nIf I adapt the mechanism of some ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Clockwork Multitool").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\ncrafting devices, I can use a touch of Arcana to make a portable all-in-one tool to suit my needs.\n\nPunching will cycle the current worktable, and Sneaking will reverse the cycle.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Clockwork Multitool").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nUsing the Multitool will open its selected worktable.\n\nSneak Using the Multitool in my Offhand will favorite the current worktable, and doing so in my Mainhand will open my favorite worktable.\n").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      return 2;
   }
   
   public class ClockworkMultitoolItem extends ArcanaPolymerItem {
      public ClockworkMultitoolItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context, HolderLookup.Provider lookup){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context, lookup);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         List<String> stringList = new ArrayList<>();
         stringList.add(getStringProperty(itemStack, MODE_TAG));
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(new ArrayList<>(), new ArrayList<>(), stringList, new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public @NonNull ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(@NonNull ItemStack stack, @NonNull ServerLevel world, @NonNull Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(entity instanceof ServerPlayer player)) return;
         addEnergy(stack, 1);
      }
      
      @Override
      public InteractionResult useOn(UseOnContext useOnContext){
         return InteractionResult.PASS;
      }
      
      @Override
      public @NonNull InteractionResult use(@NonNull Level world, Player playerEntity, @NonNull InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
         setEnergy(stack, 0);
         player.getCooldowns().addCooldown(playerEntity.getMainHandItem(), 2);
         player.getCooldowns().addCooldown(playerEntity.getOffhandItem(), 2);
         if(hand == InteractionHand.OFF_HAND && player.isShiftKeyDown()){
            MultitoolMode mode = MultitoolMode.fromName(getStringProperty(stack, MODE_TAG));
            putProperty(stack, SAVED_TAG, mode.getName());
            SoundUtils.playSongToPlayer(player, SoundEvents.SPYGLASS_USE, 0.75f, 1.25f + player.random.nextFloat() * 0.25f);
            player.sendSystemMessage(Component.literal("Set favorite mode to ").withStyle(ChatFormatting.GOLD).append(mode.getBlock().getName()), true);
            return InteractionResult.CONSUME;
         }else if(player.isShiftKeyDown()){
            MultitoolMode mode = MultitoolMode.fromName(getStringProperty(stack, SAVED_TAG));
            putProperty(stack, MODE_TAG, mode.getName());
            SoundUtils.playSongToPlayer(player, SoundEvents.SPYGLASS_USE, 0.75f, 0.2f + player.random.nextFloat() * 0.25f);
            player.sendSystemMessage(Component.literal("Reconfigured to ").withStyle(ChatFormatting.GOLD).append(mode.getBlock().getName()), true);
            openGui(player, stack, mode);
            return InteractionResult.CONSUME;
         }else{
            MultitoolMode mode = MultitoolMode.fromName(getStringProperty(stack, MODE_TAG));
            openGui(player, stack, mode);
            SoundUtils.playSongToPlayer(player, SoundEvents.SPYGLASS_USE, 0.75f, 0.2f + player.random.nextFloat() * 0.25f);
            return InteractionResult.CONSUME;
         }
      }
      
      public static void cycleMode(ServerPlayer player, ItemStack stack, boolean backwards){
         if(!(ArcanaItemUtils.identifyItem(stack) instanceof ClockworkMultitool multitool)) return;
         if(EnergyItem.getEnergy(stack) != multitool.getMaxEnergy(stack)) return;
         MultitoolMode mode = MultitoolMode.fromName(getStringProperty(stack, MODE_TAG));
         boolean enchant = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.ENCHANTMENT_MECHANISM) > 0;
         boolean anvil = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.REPAIRING_MECHANISM) > 0;
         boolean echest = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.ENDER_MECHANISM) > 0;
         mode = mode.cycle(backwards, anvil, enchant, echest);
         putProperty(stack, MODE_TAG, mode.getName());
         multitool.setEnergy(stack, 0);
         SoundUtils.playSongToPlayer(player, SoundEvents.SPYGLASS_USE, 0.75f, 0.7f + player.random.nextFloat() * 0.5f);
         player.sendSystemMessage(Component.literal("Reconfigured to ").withStyle(ChatFormatting.GOLD).append(mode.getBlock().getName()), true);
         ArcanaAchievements.progress(player, ArcanaAchievements.FIDGET_TOY, 1);
      }
      
      private void openGui(ServerPlayer player, ItemStack stack, MultitoolMode mode){
         player.getCooldowns().addCooldown(player.getMainHandItem(), 1);
         player.getCooldowns().addCooldown(player.getOffhandItem(), 1);
         ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_CLOCKWORK_MULTITOOL_USE));
         if(mode == MultitoolMode.ENDERCHEST && ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.ENDER_MECHANISM) > 1){
            ClockworkMultitoolEnderGui gui = new ClockworkMultitoolEnderGui(player, stack);
            gui.build();
            gui.open();
            return;
         }
         ContainerLevelAccess access = ContainerLevelAccess.create(player.level(), player.blockPosition());
         MenuProvider provider = switch(mode){
            case CRAFTING ->
                  new SimpleMenuProvider((i, inventory, p) -> new CraftingMenu(i, inventory, access), Component.translatable("container.crafting"));
            case SMITHING ->
                  new SimpleMenuProvider((i, inventory, p) -> new SmithingMenu(i, inventory, access), Component.translatable("container.upgrade"));
            case CARTOGRAPHY ->
                  new SimpleMenuProvider((i, inventory, p) -> new CartographyTableMenu(i, inventory, access), Component.translatable("container.cartography_table"));
            case LOOM ->
                  new SimpleMenuProvider((i, inventory, p) -> new LoomMenu(i, inventory, access), Component.translatable("container.loom"));
            case GRINDSTONE ->
                  new SimpleMenuProvider((i, inventory, p) -> new GrindstoneMenu(i, inventory, access), Component.translatable("container.grindstone_title"));
            case STONECUTTER ->
                  new SimpleMenuProvider((i, inventory, p) -> new StonecutterMenu(i, inventory, access), Component.translatable("container.stonecutter"));
            case ENDERCHEST ->
                  new SimpleMenuProvider((i, inventory, p) -> ChestMenu.threeRows(i, inventory, player.getEnderChestInventory()), Component.translatable("container.enderchest"));
            case ENCHANTING ->
                  new SimpleMenuProvider((i, inventory, p) -> new EnchantmentMenu(i, inventory, access), Component.translatable("container.enchant"));
            case ANVIL ->
                  new SimpleMenuProvider((i, inventory, p) -> new AnvilMenu(i, inventory, access), Component.translatable("container.repair"));
         };
         player.openMenu(provider);
      }
   }
   
   enum MultitoolMode {
      CRAFTING("crafting", Blocks.CRAFTING_TABLE),
      SMITHING("smithing", Blocks.SMITHING_TABLE),
      CARTOGRAPHY("cartography", Blocks.CARTOGRAPHY_TABLE),
      LOOM("loom", Blocks.LOOM),
      GRINDSTONE("grindstone", Blocks.GRINDSTONE),
      STONECUTTER("stonecutter", Blocks.STONECUTTER),
      ENDERCHEST("enderchest", Blocks.ENDER_CHEST),
      ENCHANTING("enchanting", Blocks.ENCHANTING_TABLE),
      ANVIL("anvil", Blocks.ANVIL);
      
      private final String name;
      private final Block block;
      
      MultitoolMode(String name, Block block){
         this.name = name;
         this.block = block;
      }
      
      public String getName(){
         return name;
      }
      
      public Block getBlock(){
         return block;
      }
      
      public static MultitoolMode fromName(String name){
         for(MultitoolMode mode : values()){
            if(mode.name.equals(name)){
               return mode;
            }
         }
         return CRAFTING; // Default fallback
      }
      
      public MultitoolMode cycle(boolean backwards, boolean anvil, boolean enchanting, boolean echest){
         List<MultitoolMode> validModes = new ArrayList<>();
         for(MultitoolMode mode : values()){
            if(mode == ANVIL && !anvil) continue;
            if(mode == ENCHANTING && !enchanting) continue;
            if(mode == ENDERCHEST && !echest) continue;
            validModes.add(mode);
         }
         if(validModes.isEmpty()) return CRAFTING;
         
         int currentIndex = validModes.indexOf(this);
         if(currentIndex == -1){
            currentIndex = 0;
         }else{
            if(backwards){
               currentIndex = (currentIndex - 1) % validModes.size();
               if(currentIndex < 0) currentIndex = validModes.size() - 1;
            }else{
               currentIndex = (currentIndex + 1) % validModes.size();
            }
         }
         return validModes.get(currentIndex);
      }
   }
}