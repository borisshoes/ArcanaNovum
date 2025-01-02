package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerArmorItem;
import net.borisshoes.arcananovum.events.SojournersMaxRunEvent;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
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
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.EQUIPMENT};
      vanillaItem = Items.LEATHER_BOOTS;
      item = new SojournerBootsItem(addArcanaItemComponents(new Item.Settings().maxCount(1).maxDamage(1024)
            .component(DataComponentTypes.DYED_COLOR,new DyedColorComponent(0x33A900,false))
            .component(DataComponentTypes.UNBREAKABLE,new UnbreakableComponent(false))
            .attributeModifiers(new AttributeModifiersComponent(List.of(
                  new AttributeModifiersComponent.Entry(EntityAttributes.STEP_HEIGHT, new EntityAttributeModifier(Identifier.of(ArcanaNovum.MOD_ID, STEP_TAG), 0.65, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.FEET)
            ),false))
      ));
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.DARK_GREEN);
      researchTasks = new RegistryKey[]{ResearchTasks.ADVANCEMENT_ADVENTURING_TIME,ResearchTasks.ADVANCEMENT_WALK_ON_POWDER_SNOW_WITH_LEATHER_BOOTS,ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.EFFECT_SWIFTNESS,ResearchTasks.EFFECT_JUMP_BOOST,ResearchTasks.UNLOCK_STELLAR_CORE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,ACTIVE_TAG,true);
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.GREEN))
            .append(Text.literal("Boots ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("shall take you to see the ").formatted(Formatting.GREEN))
            .append(Text.literal("world").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("...").formatted(Formatting.GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("Merely ").formatted(Formatting.GREEN))
            .append(Text.literal("wearing ").formatted(Formatting.BLUE))
            .append(Text.literal("them makes you want to go on an ").formatted(Formatting.GREEN))
            .append(Text.literal("adventure").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("These ").formatted(Formatting.GREEN))
            .append(Text.literal("Boots ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("are ").formatted(Formatting.GREEN))
            .append(Text.literal("unbreakable ").formatted(Formatting.BLUE))
            .append(Text.literal("and equal to ").formatted(Formatting.GREEN))
            .append(Text.literal("unenchanted netherite").formatted(Formatting.DARK_RED))
            .append(Text.literal(".").formatted(Formatting.GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("Wearing them gives ").formatted(Formatting.GREEN))
            .append(Text.literal("ramping move speed").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" and ").formatted(Formatting.GREEN))
            .append(Text.literal("uphill step assist").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("Sneak Right Click").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" the ").formatted(Formatting.GREEN))
            .append(Text.literal("Boots ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("to ").formatted(Formatting.GREEN))
            .append(Text.literal("toggle ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("their ").formatted(Formatting.GREEN))
            .append(Text.literal("step assist").formatted(Formatting.BLUE)));
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
      if(!stack.isOf(this.item)) return;
      boolean active = getBooleanProperty(stack,ACTIVE_TAG);
      AttributeModifiersComponent modifiers = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
      List<AttributeModifiersComponent.Entry> attributeList = new ArrayList<>();
      
      // Strip old step and speed stats
      for(AttributeModifiersComponent.Entry entry : modifiers.modifiers()){
         EntityAttributeModifier modifier = entry.modifier();
         
         if(modifier.id().toString().contains(SPEED_TAG) || modifier.id().toString().contains(STEP_TAG)){
            continue;
         }
         attributeList.add(entry);
      }
      
      if(active){
         double height = 0.65 + Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.HIKING_BOOTS.id));
         attributeList.add(new AttributeModifiersComponent.Entry(EntityAttributes.STEP_HEIGHT,new EntityAttributeModifier(Identifier.of(ArcanaNovum.MOD_ID,STEP_TAG),height,EntityAttributeModifier.Operation.ADD_VALUE),AttributeModifierSlot.FEET));
      }
      
      attributeList.add(new AttributeModifiersComponent.Entry(EntityAttributes.MOVEMENT_SPEED,new EntityAttributeModifier(Identifier.of(ArcanaNovum.MOD_ID,SPEED_TAG),getEnergy(stack)/100.0,EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE),AttributeModifierSlot.FEET));
      
      AttributeModifiersComponent newComponent = new AttributeModifiersComponent(attributeList,false);
      stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS,newComponent);
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv, StarlightForgeBlockEntity starlightForge){
      ItemStack bootStack = inv.getStack(12); // Should be the Boots
      ItemStack newArcanaItem = getNewItem();
   
      if(bootStack.hasEnchantments()){
         EnchantmentHelper.set(newArcanaItem,bootStack.getEnchantments());
      }
      
      ArmorTrim trim = bootStack.get(DataComponentTypes.TRIM);
      if(trim != null){
         newArcanaItem.set(DataComponentTypes.TRIM, trim);
      }
      
      if(hasProperty(bootStack,EnhancedStatUtils.ENHANCED_STAT_TAG)){
         EnhancedStatUtils.enhanceItem(newArcanaItem,getDoubleProperty(bootStack,EnhancedStatUtils.ENHANCED_STAT_TAG));
      }
      
      return newArcanaItem;
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("Sojourner's Boots").formatted(Formatting.DARK_GREEN,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nInstead of focusing on combative properties, I have looked into integrating energy storage into armor, all while maintaining the basic protective properties of Netherite. The result is a pair of").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("Sojourner's Boots").formatted(Formatting.DARK_GREEN,Formatting.BOLD),Text.literal("\nboots that lack any inherent protective enchantments (which I may be able to add via traditional methods) but come with the ability to store kinetic energy, amplify it and reapply it. This manifests as a ramping speed boost.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("Sojourner's Boots").formatted(Formatting.DARK_GREEN,Formatting.BOLD),Text.literal("\nThe stored energy can also be used to carry me up short hills without jumping.").formatted(Formatting.BLACK)));
      return list;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.OBSIDIAN,32);
      ArcanaIngredient b = new ArcanaIngredient(Items.TUFF,16);
      ArcanaIngredient c = new ArcanaIngredient(Items.NETHER_STAR,2);
      ArcanaIngredient d = new ArcanaIngredient(Items.RED_SAND,16);
      ArcanaIngredient f = new ArcanaIngredient(Items.ROOTED_DIRT,16);
      ArcanaIngredient g = new ArcanaIngredient(Items.NETHERITE_INGOT,1);
      ArcanaIngredient h = new ArcanaIngredient(Items.POTION,1).withPotions(Potions.STRONG_SWIFTNESS);
      ArcanaIngredient j = new ArcanaIngredient(Items.PACKED_MUD,16);
      ArcanaIngredient l = new ArcanaIngredient(Items.POTION,1).withPotions(Potions.STRONG_LEAPING);
      ArcanaIngredient m = new ArcanaIngredient(Items.NETHERITE_BOOTS,1, true);
      ArcanaIngredient p = new ArcanaIngredient(Items.GRASS_BLOCK,16);
      ArcanaIngredient t = new ArcanaIngredient(Items.GRAVEL,16);
      ArcanaIngredient v = new ArcanaIngredient(Items.SAND,16);
      ArcanaIngredient x = new ArcanaIngredient(Items.TERRACOTTA,16);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,d,a},
            {f,g,h,g,j},
            {c,l,m,l,c},
            {p,g,h,g,t},
            {a,v,c,x,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withCore().withAnvil());
   }
   
   public class SojournerBootsItem extends ArcanaPolymerArmorItem {
      public SojournerBootsItem(Item.Settings settings){
         super(getThis(),ArmorMaterials.NETHERITE,EquipmentType.BOOTS,settings);
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
      public ActionResult use(World world, PlayerEntity user, Hand hand){
         if(user.isSneaking()){
            ItemStack stack = user.getStackInHand(hand);
            boolean active = !getBooleanProperty(stack,ACTIVE_TAG);
            putProperty(stack,ACTIVE_TAG,active);
            
            if(active){
               user.sendMessage(Text.literal("The Boots become energized with Arcana").formatted(Formatting.DARK_GREEN,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayerEntity)user, SoundEvents.BLOCK_BEACON_POWER_SELECT, 0.8f,2f);
            }else{
               user.sendMessage(Text.literal("The Boots' energy fades").formatted(Formatting.DARK_GREEN,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayerEntity)user, SoundEvents.BLOCK_BEACON_DEACTIVATE, 2,.8f);
            }
            
            rebuildAttributes(stack);
            if(user instanceof ServerPlayerEntity player){
               PlayerInventory inv = player.getInventory();
               player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), hand == Hand.MAIN_HAND ? 36 + inv.selectedSlot : 45, stack));
               player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), 8, player.getEquippedStack(EquipmentSlot.FEET)));
            }
            return ActionResult.SUCCESS;
         }else{
            return super.use(world,user,hand);
         }
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         try{
            if(stack == player.getEquippedStack(EquipmentSlot.FEET)){
               if(player.isSprinting()){
                  if(player.isOnGround()){
                     int curEnergy = getEnergy(stack);
                     int sprintLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SPRINTER.id));
                     addEnergy(stack,2*(1+sprintLvl));
                     int newEnergy = getEnergy(stack);
                     if((newEnergy % 50 == 0 || newEnergy % 50 == 1) && curEnergy != newEnergy)
                        player.sendMessage(Text.literal("Sojourner Boots Energy: "+newEnergy).formatted(Formatting.DARK_GREEN),true);
                     if(world.getServer().getTicks() % 20 == 0) ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.SOJOURNERS_BOOTS_RUN_PER_SECOND)); // Add xp
                     if(newEnergy >= getMaxEnergy(stack)){
                        ArcanaNovum.addArcanaEvent(new SojournersMaxRunEvent(player));
                        if(ArcanaNovum.getEventsOfType(SojournersMaxRunEvent.class).stream().filter(event -> event.getPlayer().equals(player)).count() >= ((TimedAchievement) ArcanaAchievements.RUNNING).getGoal()){
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

