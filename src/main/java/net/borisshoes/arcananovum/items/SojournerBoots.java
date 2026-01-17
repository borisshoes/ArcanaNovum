package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.events.SojournersMaxRunEvent;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.borisshoes.borislib.events.Event;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.ArcanaRegistry.EQUIPMENT_ASSET_REGISTRY_KEY;

public class SojournerBoots extends EnergyItem {
	public static final String ID = "sojourner_boots";
   
   public static final String SPEED_TAG = "sojourn_speed";
   public static final String STEP_TAG = "sojourn_step";
   public static final String ARMOR_TAG = "sojourn_armor";
   
   public SojournerBoots(){
      id = ID;
      name = "Sojourner's Boots";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.EQUIPMENT};
      vanillaItem = Items.LEATHER_BOOTS;
      item = new SojournerBootsItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_GREEN);
      researchTasks = new ResourceKey[]{ResearchTasks.SPRINT_TEN_KILOMETERS,ResearchTasks.VISIT_DOZEN_BIOMES,ResearchTasks.ADVANCEMENT_WALK_ON_POWDER_SNOW_WITH_LEATHER_BOOTS,ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.EFFECT_SWIFTNESS,ResearchTasks.EFFECT_JUMP_BOOST,ResearchTasks.UNLOCK_STELLAR_CORE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,ACTIVE_TAG,true);
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("These ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("Boots ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("shall take you to see the ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("world").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("...").withStyle(ChatFormatting.GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("Merely ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("wearing ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("them makes you want to go on an ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("adventure").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("These ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("Boots ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("are ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("unbreakable ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("and equal to ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("unenchanted netherite").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(".").withStyle(ChatFormatting.GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("Wearing them gives ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("ramping move speed").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" and ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("uphill step assist").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right Click").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" the ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("Boots ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("to ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("toggle ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("their ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("step assist").withStyle(ChatFormatting.BLUE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){ // +250% speed base
      int boostLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.MARATHON_RUNNER.id));
      return 250 + 50*boostLvl;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      boolean active = getBooleanProperty(stack,ACTIVE_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,ACTIVE_TAG,active);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public ItemStack onAugment(ItemStack stack, ArcanaAugment augment, int level){
      if(ArcanaItemUtils.identifyItem(stack) instanceof SojournerBoots && augment == ArcanaAugments.HIKING_BOOTS && level >= 1){
         rebuildAttributes(stack);
      }
      return stack;
   }
   
   public void rebuildAttributes(ItemStack stack){
      if(!stack.is(this.item)) return;
      boolean active = getBooleanProperty(stack,ACTIVE_TAG);
      ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
      List<ItemAttributeModifiers.Entry> attributeList = new ArrayList<>();
      
      // Strip old step and speed stats
      for(ItemAttributeModifiers.Entry entry : modifiers.modifiers()){
         AttributeModifier modifier = entry.modifier();
         
         if(modifier.id().toString().contains(SPEED_TAG) || modifier.id().toString().contains(STEP_TAG)){
            continue;
         }
         attributeList.add(entry);
      }
      
      if(active){
         double height = 0.65 + Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.HIKING_BOOTS.id));
         attributeList.add(new ItemAttributeModifiers.Entry(Attributes.STEP_HEIGHT,new AttributeModifier(Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,STEP_TAG),height, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.FEET));
      }
      
      attributeList.add(new ItemAttributeModifiers.Entry(Attributes.MOVEMENT_SPEED,new AttributeModifier(Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,SPEED_TAG),getEnergy(stack)/100.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.FEET));
      
      ItemAttributeModifiers newComponent = new ItemAttributeModifiers(attributeList);
      stack.set(DataComponents.ATTRIBUTE_MODIFIERS,newComponent);
   }
   
   @Override
   public ItemStack forgeItem(Container inv, List<Integer> centerpieces, StarlightForgeBlockEntity starlightForge){
      ItemStack newArcanaItem = getNewItem();
      if(centerpieces.isEmpty()) return newArcanaItem;
      ItemStack bootStack = inv.getItem(centerpieces.getFirst()); // Should be the Boots
   
      if(bootStack.isEnchanted()){
         EnchantmentHelper.setEnchantments(newArcanaItem,bootStack.getEnchantments());
      }
      
      ArmorTrim trim = bootStack.get(DataComponents.TRIM);
      if(trim != null){
         newArcanaItem.set(DataComponents.TRIM, trim);
      }
      
      if(hasProperty(bootStack,EnhancedStatUtils.ENHANCED_STAT_TAG)){
         EnhancedStatUtils.enhanceItem(newArcanaItem,getDoubleProperty(bootStack,EnhancedStatUtils.ENHANCED_STAT_TAG));
      }
      
      return newArcanaItem;
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("Sojourner's Boots").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nInstead of focusing on combative properties, I have looked into integrating energy storage into armor, all while maintaining the basic protective properties of Netherite. The result is a pair of").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Sojourner's Boots").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), Component.literal("\nboots that lack any inherent protective enchantments (which I may be able to add via traditional methods) but come with the ability to store kinetic energy, amplify it and reapply it. This manifests as a ramping speed boost.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Sojourner's Boots").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), Component.literal("\nThe stored energy can also be used to carry me up short hills without jumping.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class SojournerBootsItem extends ArcanaPolymerItem {
      public SojournerBootsItem(){
         super(getThis(),getEquipmentArcanaItemComponents()
               .humanoidArmor(ArmorMaterials.NETHERITE, ArmorType.BOOTS)
               .component(DataComponents.DYED_COLOR,new DyedItemColor(0x33A900))
               .attributes(ArmorMaterials.NETHERITE.createAttributes(ArmorType.BOOTS)
                     .withModifierAdded(Attributes.STEP_HEIGHT, new AttributeModifier(Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID, STEP_TAG), 0.65, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.FEET)
               )
         );
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         Equippable equippableComponent = baseStack.get(DataComponents.EQUIPPABLE);
         Equippable newComp = Equippable.builder(equippableComponent.slot()).setEquipSound(equippableComponent.equipSound()).setAsset(ResourceKey.create(EQUIPMENT_ASSET_REGISTRY_KEY, Identifier.fromNamespaceAndPath(MOD_ID,ID))).build();
         baseStack.set(DataComponents.EQUIPPABLE,newComp);
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public InteractionResult use(Level world, Player user, InteractionHand hand){
         if(user.isShiftKeyDown()){
            ItemStack stack = user.getItemInHand(hand);
            boolean active = !getBooleanProperty(stack,ACTIVE_TAG);
            putProperty(stack,ACTIVE_TAG,active);
            
            if(active){
               user.displayClientMessage(Component.literal("The Boots become energized with Arcana").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayer)user, SoundEvents.BEACON_POWER_SELECT, 0.8f,2f);
            }else{
               user.displayClientMessage(Component.literal("The Boots' energy fades").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayer)user, SoundEvents.BEACON_DEACTIVATE, 2,.8f);
            }
            
            rebuildAttributes(stack);
            if(user instanceof ServerPlayer player){
               Inventory inv = player.getInventory();
               player.connection.send(new ClientboundContainerSetSlotPacket(player.inventoryMenu.containerId, player.inventoryMenu.incrementStateId(), hand == InteractionHand.MAIN_HAND ? 36 + inv.getSelectedSlot() : 45, stack));
               player.connection.send(new ClientboundContainerSetSlotPacket(player.inventoryMenu.containerId, player.inventoryMenu.incrementStateId(), 8, player.getItemBySlot(EquipmentSlot.FEET)));
            }
            return InteractionResult.SUCCESS_SERVER;
         }else{
            return super.use(world,user,hand);
         }
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(entity instanceof ServerPlayer player)) return;
         try{
            if(stack == player.getItemBySlot(EquipmentSlot.FEET)){
               if(player.isSprinting()){
                  if(player.onGround()){
                     int curEnergy = getEnergy(stack);
                     int sprintLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SPRINTER.id));
                     addEnergy(stack,2*(1+sprintLvl));
                     int newEnergy = getEnergy(stack);
                     if((newEnergy % 50 == 0 || newEnergy % 50 == 1) && curEnergy != newEnergy)
                        player.displayClientMessage(Component.literal("Sojourner Boots Energy: "+newEnergy).withStyle(ChatFormatting.DARK_GREEN),true);
                     if(world.getServer().getTickCount() % 20 == 0) ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_SOJOURNERS_BOOTS_RUN_PER_SECOND)); // Add xp
                     if(newEnergy >= getMaxEnergy(stack)){
                        Event.addEvent(new SojournersMaxRunEvent(player));
                        if(Event.getEventsOfType(SojournersMaxRunEvent.class).stream().filter(event -> event.getPlayer().equals(player)).count() >= ((TimedAchievement) ArcanaAchievements.RUNNING).getGoal()){
                           ArcanaAchievements.grant(player,ArcanaAchievements.RUNNING);
                        }
                     }
                  }
               }else{
                  addEnergy(stack,-10);
               }
               
               rebuildAttributes(stack);
            }else{
               if(getEnergy(stack) != 0){
                  setEnergy(stack, 0);
               }
            }
         }catch(Exception e){
            e.printStackTrace();
         }
      }
   }
}

