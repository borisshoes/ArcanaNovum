package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.ArcanaRegistry.EQUIPMENT_ASSET_REGISTRY_KEY;
import static net.borisshoes.borislib.utils.MinecraftUtils.makeEnchantComponent;

public class WingsOfEnderia extends EnergyItem {
   public static final String ID = "wings_of_enderia";
   
   public WingsOfEnderia(){
      id = ID;
      name = "Armored Wings of Enderia";
      rarity = ArcanaRarity.DIVINE;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.EQUIPMENT};
      itemVersion = 1;
      vanillaItem = Items.ELYTRA;
      item = new WingsOfEnderiaItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_WINGS_OF_ENDERIA};
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("These ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("Armored Wings").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(" will shield you from the ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("dangers ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("of the land.").withStyle(ChatFormatting.LIGHT_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("These ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("Wings ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("act as a ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("Netherite Chestplate").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(" with ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("Protection IV").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.LIGHT_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("They store ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("energy ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("as you fly to ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("cushion impacts").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(" and are ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("unbreakable").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.LIGHT_PURPLE)));
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack onAugment(ItemStack stack, ArcanaAugment augment, int level){
      if(ArcanaItemUtils.identifyItem(stack) instanceof WingsOfEnderia && augment == ArcanaAugments.SCALES_OF_THE_CHAMPION && level >= 1){
         EnhancedStatUtils.enhanceItem(stack, 1);
      }
      return stack;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      return ArcanaNovum.CONFIG.getInt(ArcanaConfig.WINGS_OF_ENDERIA_MAX_ENERGY); // Store up to 100 points of dmg mitigation at 5 seconds of flight per damage point stored aka 100 ticks/energy per 1 dmg point
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("   Armored Wings\n     of Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nMy expedition to the End was nothing like I imagined it would be… Seeing the mad tyrant, Enderia, in person… Nul and Equayus joining me in defeating her… ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Armored Wings\n     of Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), Component.literal("\nThe ancient names they spoke of like they were family? How old was she?\n\nI appear to be missing a significant amount of history. Nevertheless, the Goddess of Wrath has been defeated; reduced to this small, ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Armored Wings\n     of Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), Component.literal("\nblack, very angry egg. From her broken body, I collected a large portion of scales. Curiously enough, two of them contained runes. I have fashioned myself a copy of her wings.\n\nEven if they are only ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Armored Wings\n     of Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), Component.literal("\na fraction as durable as hers, they will be nearly indestructible.\n\nThe Armored Wings of Enderia are Elytra that double as enchanted Netherite.\n\nThey store kinetic ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Armored Wings\n     of Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), Component.literal("\nenergy as I fly through the air, which can be used to halve falling and kinetic damage if they have enough energy.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class WingsOfEnderiaItem extends ArcanaPolymerItem {
      public WingsOfEnderiaItem(){
         super(getThis(), getEquipmentArcanaItemComponents()
               .humanoidArmor(ArmorMaterials.NETHERITE, ArmorType.CHESTPLATE)
               .component(DataComponents.GLIDER, Unit.INSTANCE)
               .delayedComponent(DataComponents.ENCHANTMENTS, ctx -> makeEnchantComponent(new EnchantmentInstance(ctx.getOrThrow(Enchantments.PROTECTION),4)))
         );
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context, HolderLookup.Provider lookup){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context, lookup);
         Equippable equippableComponent = baseStack.get(DataComponents.EQUIPPABLE);
         Identifier modelId = ArcanaItem.getSkin(itemStack) != null ? ArcanaItem.getSkin(itemStack).getModelId() : ArcanaRegistry.arcanaId(ID);
         Equippable newComp = Equippable.builder(equippableComponent.slot()).setEquipSound(equippableComponent.equipSound()).setAsset(ResourceKey.create(EQUIPMENT_ASSET_REGISTRY_KEY, modelId)).build();
         baseStack.set(DataComponents.EQUIPPABLE, newComp);
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!(entity instanceof ServerPlayer player)) return;
         ItemStack item = player.getItemBySlot(EquipmentSlot.CHEST);
         if(ArcanaItemUtils.identifyItem(item) instanceof WingsOfEnderia wings){
            if(player.isFallFlying()){ // Wings of Enderia
               int beforeE = getEnergy(item);
               int toAdd = ArcanaNovum.CONFIG.getInt(ArcanaConfig.WINGS_OF_ENDERIA_ENERGY_RATE);
               wings.addEnergy(item, toAdd); // Add 1 energy for each tick of flying
               if(beforeE / 1000 != EnergyItem.getEnergy(item) / 1000)
                  player.sendSystemMessage(Component.literal("Wing Energy Stored: " + EnergyItem.getEnergy(item)).withStyle(ChatFormatting.DARK_PURPLE), true);
               ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_WINGS_OF_ENDERIA_FLY)); // Add xp
            }
            CompoundTag leftShoulder = player.getShoulderEntityLeft();
            CompoundTag rightShoulder = player.getShoulderEntityRight();
            if(leftShoulder.contains("id") && rightShoulder.contains("id")){
               if(leftShoulder.getStringOr("id", "").equals(EntityType.getKey(EntityType.PARROT).toString()) && rightShoulder.getStringOr("id", "").equals(EntityType.getKey(EntityType.PARROT).toString())){
                  ArcanaAchievements.grant(player, ArcanaAchievements.CROW_FATHER);
               }
            }
         }
      }
   }
}

