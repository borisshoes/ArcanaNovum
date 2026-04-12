package net.borisshoes.arcananovum.items;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.callbacks.ShieldTimerCallback;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.skins.ArcanaSkin;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.TickTimerCallback;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.borislib.BorisLib.SERVER_TIMER_CALLBACKS;

public class ShieldOfFortitude extends ArcanaItem {
   public static final String ID = "shield_of_fortitude";
   
   public static final Identifier EFFECT_ID = ArcanaRegistry.arcanaId(ID);
   public static final String ABSORPTION_TAG = "absorption";
   
   public ShieldOfFortitude(){
      id = ID;
      name = "Shield of Fortitude";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.EQUIPMENT};
      vanillaItem = Items.SHIELD;
      item = new ShieldOfFortitudeItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_NETHERITE_INGOT, ResearchTasks.EFFECT_ABSORPTION, ResearchTasks.ADVANCEMENT_DEFLECT_ARROW, ResearchTasks.UNLOCK_STELLAR_CORE, ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("This shield is ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("overflowing").withStyle(ChatFormatting.ITALIC, ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" with powerful ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("defensive magic").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Your will for ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("protection").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" becomes a tangible ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("fortitude").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Damage").withStyle(ChatFormatting.RED))
            .append(Component.literal(" blocked").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" becomes ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("absorption hearts").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" and the shield is ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("unbreakable").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      BannerPatternLayers patterns = stack.get(DataComponents.BANNER_PATTERNS);
      DyeColor color = stack.get(DataComponents.BASE_COLOR);
      ItemStack newStack = super.updateItem(stack, server);
      if(patterns != null) stack.set(DataComponents.BANNER_PATTERNS, patterns);
      if(color != null) stack.set(DataComponents.BASE_COLOR, color);
      return buildItemLore(newStack, server);
   }
   
   @Override
   public ItemStack forgeItem(Container inv, List<Integer> centerpieces, StarlightForgeBlockEntity starlightForge){
      ItemStack newArcanaItem = getNewItem();
      if(centerpieces.isEmpty()) return newArcanaItem;
      ItemStack shieldStack = inv.getItem(centerpieces.getFirst()); // Should be the Sword
      
      if(shieldStack.isEnchanted()){
         EnchantmentHelper.setEnchantments(newArcanaItem, shieldStack.getEnchantments());
      }
      
      BannerPatternLayers patterns = shieldStack.get(DataComponents.BANNER_PATTERNS);
      DyeColor color = shieldStack.get(DataComponents.BASE_COLOR);
      if(color != null){
         newArcanaItem.set(DataComponents.BASE_COLOR, color);
      }
      if(patterns != null){
         newArcanaItem.set(DataComponents.BANNER_PATTERNS, patterns);
      }
      
      return newArcanaItem;
   }
   
   public void shieldBlock(LivingEntity entity, ItemStack item, float amount){
      float baseMax = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.SHIELD_OF_FORTITUDE_HIT_MAX);
      float extraMax = ArcanaNovum.CONFIG.getFloatList(ArcanaConfig.SHIELD_OF_FORTITUDE_HIT_MAX_PER_LVL).get(ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.SHIELD_OF_FAITH));
      float maxAbs = baseMax + extraMax;
      float curAbs = entity.getAbsorptionAmount();
      float conversionRate = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.SHIELD_OF_FORTITUDE_BLOCKED_ENERGY_CONVERSION_PERCENT);
      float addedAbs = Math.min(maxAbs, amount * conversionRate);
      int baseDuration = ArcanaNovum.CONFIG.getInt(ArcanaConfig.SHIELD_OF_FORTITUDE_DURATION);
      int extraDuration = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.SHIELD_OF_FORTITUDE_DURATION_PER_LVL).get(ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.SHIELD_OF_RESILIENCE));
      int duration = baseDuration + extraDuration;
      if(entity instanceof ServerPlayer player){
         BorisLib.addTickTimerCallback(new ShieldTimerCallback(duration, item, player, addedAbs));
         SoundUtils.playSongToPlayer(player, SoundEvents.ENCHANTMENT_TABLE_USE, 1, 1.8f);
         float shieldTotal = 0;
         float absAmt = player.getAbsorptionAmount();
         for(int i = 0; i < SERVER_TIMER_CALLBACKS.size(); i++){
            TickTimerCallback t = SERVER_TIMER_CALLBACKS.get(i);
            if(t instanceof ShieldTimerCallback st && st.getPlayer().getStringUUID().equals(player.getStringUUID())){
               shieldTotal += st.getHearts();
            }
         }
         putProperty(item, ABSORPTION_TAG, Math.min(absAmt, shieldTotal));
      }
      MinecraftUtils.addMaxAbsorption(entity, EFFECT_ID, addedAbs);
      entity.setAbsorptionAmount((curAbs + addedAbs));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("Shield of Fortitude").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nThis Shield is my attempt at pouring Arcana into a fully defensive item. The Netherite and obsidian reinforced Shield absorbs the energy of impacts and reconfigures it into a fortification barrier ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Shield of Fortitude").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\naround myself by invoking all four basic protection enchantments and mimicking the effect of golden apples.\n\nHalf of all damage blocked by the Shield becomes an absorption barrier lasting 10 seconds.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Shield of Fortitude").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nDisabling the Shield with an axe causes the absorption barrier to shatter prematurely.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class ShieldOfFortitudeItem extends ArcanaPolymerItem {
      public ShieldOfFortitudeItem(){
         super(getThis(),
               getEquipmentArcanaItemComponents()
                     .equippableUnswappable(EquipmentSlot.OFFHAND)
                     .delayedComponent(DataComponents.BLOCKS_ATTACKS,
                           context -> new BlocksAttacks(
                                 0.25F,
                                 1.0F,
                                 List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)),
                                 new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                                 Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                                 Optional.of(SoundEvents.SHIELD_BLOCK),
                                 Optional.of(SoundEvents.SHIELD_BREAK)
                           ))
                     .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK)
                     .component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
         );
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack itemStack, ServerLevel serverLevel, Entity entity, @org.jspecify.annotations.Nullable EquipmentSlot equipmentSlot){
         if(serverLevel.getServer().getTickCount() % 20 == 0 && entity instanceof ServerPlayer player){
            float shieldTotal = 0;
            float absAmt = player.getAbsorptionAmount();
            for(int i = 0; i < SERVER_TIMER_CALLBACKS.size(); i++){
               TickTimerCallback t = SERVER_TIMER_CALLBACKS.get(i);
               if(t instanceof ShieldTimerCallback st && st.getPlayer().getStringUUID().equals(player.getStringUUID())){
                  shieldTotal += st.getHearts();
               }
            }
            putProperty(itemStack, ABSORPTION_TAG, Math.min(absAmt, shieldTotal));
         }
      }
      
      @Override
      public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup){
         if(PolymerResourcePackUtils.hasMainPack(context)){
            ArcanaSkin skin = ArcanaItem.getSkin(stack);
            if(skin != null){
               return skin.getModelId();
            }else{
               ShieldDisplayMode mode = (ShieldDisplayMode) ArcanaNovum.CONFIG.getValue(ArcanaConfig.SHIELD_OF_FORTITUDE_DISPLAY_MODE);
               if(mode == ShieldDisplayMode.MODEL_ONLY){
                  return ArcanaRegistry.arcanaId(arcanaItem.getId());
               }else if(mode == ShieldDisplayMode.VANILLA_ONLY){
                  return ArcanaRegistry.arcanaId(arcanaItem.getId() + "_vanilla");
               }else{
                  if((stack.has(DataComponents.BANNER_PATTERNS) && !stack.get(DataComponents.BANNER_PATTERNS).layers().isEmpty()) || stack.has(DataComponents.BASE_COLOR)){
                     return ArcanaRegistry.arcanaId(arcanaItem.getId() + "_vanilla");
                  }else{
                     return ArcanaRegistry.arcanaId(arcanaItem.getId());
                  }
               }
            }
         }else{
            return BuiltInRegistries.ITEM.getResourceKey(arcanaItem.getVanillaItem().asItem()).get().identifier();
         }
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context, HolderLookup.Provider lookup){
         ItemStack stack = super.getPolymerItemStack(itemStack, tooltipType, context, lookup);
         List<String> stringList = new ArrayList<>();
         float abs = getFloatProperty(itemStack, ABSORPTION_TAG);
         if(abs >= 40){
            stringList.add("stage_6");
         }else if(abs > 32){
            stringList.add("stage_5");
         }else if(abs > 24){
            stringList.add("stage_4");
         }else if(abs > 16){
            stringList.add("stage_3");
         }else if(abs > 8){
            stringList.add("stage_2");
         }else if(abs > 0){
            stringList.add("stage_1");
         }else{
            stringList.add("stage_0");
         }
         if(stack.has(DataComponents.BASE_COLOR) && !stack.has(DataComponents.CUSTOM_NAME)){
            stack.set(DataComponents.CUSTOM_NAME, TextUtils.removeItalics(this.getName(stack)));
         }
         stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(new ArrayList<>(), new ArrayList<>(), stringList, new ArrayList<>()));
         return stack;
      }
   }
   
   public enum ShieldDisplayMode implements StringRepresentable {
      MODEL_ONLY,
      VANILLA_ONLY,
      HYBRID;
      
      @Override
      public @NonNull String getSerializedName(){
         return name().toLowerCase(java.util.Locale.ROOT);
      }
   }
}

