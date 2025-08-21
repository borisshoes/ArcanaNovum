package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.ArcanaRegistry.EQUIPMENT_ASSET_REGISTRY_KEY;

public class WingsOfEnderia extends EnergyItem {
	public static final String ID = "wings_of_enderia";
   
   public WingsOfEnderia(){
      id = ID;
      name = "Armored Wings of Enderia";
      rarity = ArcanaRarity.DIVINE;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.EQUIPMENT};
      itemVersion = 1;
      vanillaItem = Items.ELYTRA;
      item = new WingsOfEnderiaItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.DARK_PURPLE);
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_WINGS_OF_ENDERIA};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public void finalizePrefItem(MinecraftServer server){
      super.finalizePrefItem(server);
      ItemStack curPrefItem = this.getPrefItem();
      curPrefItem.set(DataComponentTypes.ENCHANTMENTS, MiscUtils.makeEnchantComponent(
            new EnchantmentLevelEntry(MiscUtils.getEnchantment(server.getRegistryManager(),Enchantments.PROTECTION),4)
      ));
      this.prefItem = buildItemLore(curPrefItem, server);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("Armored Wings").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(" will shield you from the ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("dangers ").formatted(Formatting.YELLOW))
            .append(Text.literal("of the land.").formatted(Formatting.LIGHT_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("Wings ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("act as a ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("Netherite Chestplate").formatted(Formatting.DARK_RED))
            .append(Text.literal(" with ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("Protection IV").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.LIGHT_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("They store ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("energy ").formatted(Formatting.YELLOW))
            .append(Text.literal("as you fly to ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("cushion impacts").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(" and are ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("unbreakable").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.LIGHT_PURPLE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack onAugment(ItemStack stack, ArcanaAugment augment, int level){
      if(ArcanaItemUtils.identifyItem(stack) instanceof WingsOfEnderia && augment == ArcanaAugments.SCALES_OF_THE_CHAMPION && level >= 1){
         EnhancedStatUtils.enhanceItem(stack,1);
      }
      return stack;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      return 10000; // Store up to 100 points of dmg mitigation at 5 seconds of flight per damage point stored aka 100 ticks/energy per 1 dmg point
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("   Armored Wings\n     of Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nMy expedition to the End was nothing like I imagined it would be… Seeing the mad tyrant, Enderia, in person… Nul and Equayus joining me in defeating her… ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Armored Wings\n     of Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD),Text.literal("\nThe ancient names they spoke of like they were family? How old was she?\n\nI appear to be missing a significant amount of history. Nevertheless, the Goddess of Wrath has been defeated; reduced to this small, ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Armored Wings\n     of Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD),Text.literal("\nblack, very angry egg. From her broken body, I collected a large portion of scales. Curiously enough, two of them contained runes. I have fashioned myself a copy of her wings.\n\nEven if they are only ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Armored Wings\n     of Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD),Text.literal("\na fraction as durable as hers, they will be nearly indestructible.\n\nThe Armored Wings of Enderia are Elytra that double as enchanted Netherite.\n\nThey store kinetic ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Armored Wings\n     of Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD),Text.literal("\nenergy as I fly through the air, which can be used to halve falling and kinetic damage if they have enough energy.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class WingsOfEnderiaItem extends ArcanaPolymerItem {
      public WingsOfEnderiaItem(){
         super(getThis(),getEquipmentArcanaItemComponents()
               .armor(ArmorMaterials.NETHERITE, EquipmentType.CHESTPLATE)
               .component(DataComponentTypes.GLIDER, Unit.INSTANCE)
         );
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         EquippableComponent equippableComponent = baseStack.get(DataComponentTypes.EQUIPPABLE);
         EquippableComponent newComp = EquippableComponent.builder(equippableComponent.slot()).equipSound(equippableComponent.equipSound()).model(RegistryKey.of(EQUIPMENT_ASSET_REGISTRY_KEY, Identifier.of(MOD_ID,ID))).build();
         baseStack.set(DataComponentTypes.EQUIPPABLE,newComp);
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot){
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         ItemStack item = player.getEquippedStack(EquipmentSlot.CHEST);
         if(ArcanaItemUtils.identifyItem(item) instanceof WingsOfEnderia wings){
            if(player.isGliding()){ // Wings of Enderia
               wings.addEnergy(item,1); // Add 1 energy for each tick of flying
               if(wings.getEnergy(item) % 1000 == 999)
                  player.sendMessage(Text.literal("Wing Energy Stored: "+ (wings.getEnergy(item) + 1)).formatted(Formatting.DARK_PURPLE),true);
               ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.WINGS_OF_ENDERIA_FLY)); // Add xp
            }
            NbtCompound leftShoulder = player.getShoulderEntityLeft();
            NbtCompound rightShoulder = player.getShoulderEntityRight();
            if(leftShoulder != null && rightShoulder != null && leftShoulder.contains("id") && rightShoulder.contains("id")){
               if(leftShoulder.getString("id", "").equals(EntityType.getId(EntityType.PARROT).toString()) && rightShoulder.getString("id", "").equals(EntityType.getId(EntityType.PARROT).toString())){
                  ArcanaAchievements.grant(player, ArcanaAchievements.CROW_FATHER.id);
               }
            }
         }
      }
   }
}

