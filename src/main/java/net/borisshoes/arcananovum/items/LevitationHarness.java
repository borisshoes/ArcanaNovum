package net.borisshoes.arcananovum.items;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import io.github.ladysnake.pal.VanillaAbilities;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.gui.levitationharness.LevitationHarnessGui;
import net.borisshoes.arcananovum.gui.levitationharness.LevitationHarnessInventoryListener;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.ArcanaRegistry.EQUIPMENT_ASSET_REGISTRY_KEY;
import static net.borisshoes.arcananovum.ArcanaRegistry.LEVITATION_HARNESS_ABILITY;

public class LevitationHarness extends EnergyItem {
   public static final String ID = "levitation_harness";
   
   public static final String SOULS_TAG = "souls";
   public static final String GLOWSTONE_TAG = "glowstone";
   public static final String STALL_TAG = "stall";
   public static final String WAS_FLYING_TAG = "wasFlying";
   public static final String STONE_DATA_TAG = "stoneData";
   
   public LevitationHarness(){
      id = ID;
      name = "Levitation Harness";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.EQUIPMENT};
      initEnergy = 3599; // 1 hour of default charge (1 soul + 16 glowstone dust = 60 seconds of flight)
      itemVersion = 1;
      vanillaItem = Items.LEATHER_CHESTPLATE;
      item = new LevitationHarnessItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_SHULKER_CORE, ResearchTasks.OBTAIN_NETHERITE_INGOT, ResearchTasks.UNLOCK_STELLAR_CORE, ResearchTasks.ADVANCEMENT_ELYTRA, ResearchTasks.UNLOCK_ARCANE_SINGULARITY, ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack, SOULS_TAG, 500.0);
      putProperty(stack, GLOWSTONE_TAG, 960.0);
      putProperty(stack, STALL_TAG, -1);
      putProperty(stack, WAS_FLYING_TAG, false);
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("Mastery over the nature of ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("Shulkers ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("has yielded the ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("Levitation Harness!").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Grants ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("creative flight").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" while consuming ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("Shulker ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("souls ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("and ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("Glowstone").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.WHITE)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("Harness ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("is quite ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("fragile ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("and the slightest bump causes it to ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("stall").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.WHITE)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("Harness ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("also provides ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("no protection").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(" against ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("damage").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.WHITE)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right click").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" while ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("holding ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("the ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("harness ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("to open the refuelling menu.").withStyle(ChatFormatting.WHITE)));
      lore.add(Component.literal(""));
      
      String duration = itemStack != null ? getDuration(itemStack) : "60 Minutes";
      
      lore.add(Component.literal("")
            .append(Component.literal("Flight Duration ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("- ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(duration).withStyle(ChatFormatting.YELLOW)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      return Integer.MAX_VALUE;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      CompoundTag stoneData = getCompoundProperty(stack, STONE_DATA_TAG);
      double souls = getDoubleProperty(stack, SOULS_TAG);
      int stall = getIntProperty(stack, STALL_TAG);
      double glowstone = getDoubleProperty(stack, GLOWSTONE_TAG);
      boolean wasFlying = getBooleanProperty(stack, WAS_FLYING_TAG);
      ItemStack newStack = super.updateItem(stack, server);
      putProperty(newStack, STONE_DATA_TAG, stoneData);
      putProperty(newStack, SOULS_TAG, souls);
      putProperty(newStack, STALL_TAG, stall);
      putProperty(newStack, GLOWSTONE_TAG, glowstone);
      putProperty(newStack, WAS_FLYING_TAG, wasFlying);
      return buildItemLore(newStack, server);
   }
   
   public String getDuration(ItemStack item){
      int energy = getEnergy(item);
      String duration;
      if(energy >= 6000){
         duration = ((energy / 3600) + 1) + " Hours";
      }else if(energy >= 100){
         duration = ((energy / 60) + 1) + " Minutes";
      }else{
         duration = energy + " Seconds";
      }
      return duration;
   }
   
   public void recalculateEnergy(ItemStack stack){
      int soulsPerHour = ArcanaNovum.CONFIG.getInt(ArcanaConfig.LEVITATION_HARNESS_SOUL_PER_HOUR);
      int glowstonePerHour = ArcanaNovum.CONFIG.getInt(ArcanaConfig.LEVITATION_HARNESS_GLOWSTONE_PER_HOUR);
      double souls = getDoubleProperty(stack, SOULS_TAG);
      double glowstone = getDoubleProperty(stack, GLOWSTONE_TAG);
      double soulHours = souls / (double) soulsPerHour;
      double glowHours = glowstone / (double) glowstonePerHour;
      setEnergy(stack, (int) (3600 * Math.min(soulHours, glowHours)));
      buildItemLore(stack, BorisLib.SERVER);
   }
   
   public double getGlow(ItemStack stack){
      return getDoubleProperty(stack, GLOWSTONE_TAG);
   }
   
   public double getSouls(ItemStack stack){
      return getDoubleProperty(stack, SOULS_TAG);
   }
   
   public void setStone(ItemStack stack, ItemStack stone){
      if(stone == null || stone.isEmpty()){
         putProperty(stack, STONE_DATA_TAG, new CompoundTag());
         putProperty(stack, SOULS_TAG, -1);
      }else{
         putProperty(stack, STONE_DATA_TAG, ItemStack.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, BorisLib.SERVER.registryAccess()), stone).getOrThrow());
         putProperty(stack, SOULS_TAG, Soulstone.getSouls(stone));
      }
   }
   
   public void addGlow(ItemStack stack, double glow){
      double newGlow = Math.max(0, getDoubleProperty(stack, GLOWSTONE_TAG) + glow);
      putProperty(stack, GLOWSTONE_TAG, newGlow);
   }
   
   public int getStall(ItemStack stack){
      return getIntProperty(stack, STALL_TAG);
   }
   
   public void setStall(ItemStack stack, int seconds){
      putProperty(stack, STALL_TAG, seconds);
   }
   
   public void buildGui(ItemStack stack, LevitationHarnessGui gui){
      int souls = (int) getSouls(stack);
      int glow = (int) getGlow(stack);
      int energy = getEnergy(stack);
      
      String soulText = souls > -1 ? TextUtils.readableInt(souls) + " Shulker Souls" : "No Soulstone Inserted";
      String durationText = energy > 0 ? "Flight Time Remaining: " + getDuration(stack) : "No Fuel!";
      String glowText = glow > 0 ? TextUtils.readableInt(glow) + " Glowstone Left" : "No Glowstone Remaining";
      GuiElementBuilder soulPane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP, souls > -1 ? ArcanaColors.ARCANA_COLOR : ArcanaColors.DARK_COLOR));
      GuiElementBuilder durationPane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP, energy > 0 ? ArcanaColors.LIGHT_COLOR : 0x880000));
      GuiElementBuilder glowPane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP, glow > 0 ? 0xffdd00 : ArcanaColors.DARK_COLOR));
      ChatFormatting soulTextColor = souls > -1 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.RED;
      ChatFormatting durationTextColor = energy > 0 ? ChatFormatting.GRAY : ChatFormatting.RED;
      ChatFormatting glowTextColor = glow > 0 ? ChatFormatting.GOLD : ChatFormatting.RED;
      
      gui.setSlot(0, soulPane.setName(Component.literal(soulText).withStyle(soulTextColor)));
      gui.setSlot(2, durationPane.setName(Component.literal(durationText).withStyle(durationTextColor)));
      gui.setSlot(4, glowPane.setName(Component.literal(glowText).withStyle(glowTextColor)));
   }
   
   public void openGui(Player playerEntity, ItemStack stack){
      if(!(playerEntity instanceof ServerPlayer player))
         return;
      LevitationHarnessGui gui = new LevitationHarnessGui(MenuType.HOPPER, player, this, stack);
      
      double souls = getSouls(stack);
      
      for(int i = 0; i < gui.getSize(); i++){
         gui.clearSlot(i);
      }
      
      buildGui(stack, gui);
      
      SimpleContainer inv = new SimpleContainer(2);
      LevitationHarnessInventoryListener listener = new LevitationHarnessInventoryListener(this, gui, stack);
      inv.addListener(listener);
      listener.setUpdating();
      
      gui.setSlotRedirect(1, new Slot(inv, 0, 0, 0));
      gui.setSlotRedirect(3, new Slot(inv, 1, 0, 0));
      if(souls > -1){
         CompoundTag stoneData = getCompoundProperty(stack, STONE_DATA_TAG);
         ItemStack stone;
         if(stoneData == null || stoneData.isEmpty()){
            stone = Soulstone.setType(ArcanaRegistry.SOULSTONE.getNewItem(), EntityType.SHULKER);
         }else{
            stone = ItemStack.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, player.registryAccess()), stoneData).result().orElse(ItemStack.EMPTY);
         }
         stone = Soulstone.setSouls(stone, (int) souls);
         inv.setItem(0, stone);
         gui.validStone(stone);
      }else{
         gui.notValidStone();
      }
      gui.setTitle(Component.literal("Levitation Harness"));
      listener.finishUpdate();
      
      gui.open();
   }
   
   @Override
   public ItemStack forgeItem(Container inv, List<Integer> centerpieces, StarlightForgeBlockEntity starlightForge){
      ItemStack newArcanaItem = getNewItem();
      if(centerpieces.isEmpty()) return newArcanaItem;
      ItemStack coreStack = inv.getItem(centerpieces.getFirst()); // Should be the Core
      // Souls n stuff
      if(ArcanaItemUtils.identifyItem(coreStack) instanceof ShulkerCore core){
         newArcanaItem = getNewItem();
         setStone(newArcanaItem, core.getStone(coreStack));
         buildItemLore(newArcanaItem, BorisLib.SERVER);
         
         ArcanaAugments.copyAugment(coreStack, newArcanaItem, ArcanaAugments.SHULKER_RECYCLER, ArcanaAugments.HARNESS_RECYCLER);
      }
      
      return newArcanaItem;
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("Levitation Harness").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nThe sheer amount of effort and research that has gone into this is incomparable. A crowning achievement to be sure. The ability to fly freely through the sky is at my command, albeit fueled by innocent souls.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Levitation Harness").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nGlowstone was an adequate moderator for the Shulker Core, but now it is an absolute necessity that is consumed in large quantities to stabilize the flight reaction. Even with more Glowstone, the reaction is incredibly sensitive to damage.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Levitation Harness").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nWearing the Harness grants creative flight. The Harness provides no armor value, and taking even the slightest bump while in flight will destabilize the flight process, dealing half my health in damage, and taking a few seconds to restabilize.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class LevitationHarnessItem extends ArcanaPolymerItem {
      public LevitationHarnessItem(){
         super(getThis(), getEquipmentArcanaItemComponents()
               .humanoidArmor(ArcanaRegistry.NON_PROTECTIVE_ARMOR_MATERIAL, ArmorType.CHESTPLATE)
               .component(DataComponents.DYED_COLOR, new DyedItemColor(0x966996))
         );
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
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
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerLevel serverWorld && entity instanceof ServerPlayer player)) return;
         boolean chestItem = ItemStack.isSameItemSameComponents(player.getItemBySlot(EquipmentSlot.CHEST), stack);
         boolean riding = player.isPassenger();
         boolean survival = !(player.isCreative() || player.isSpectator());
         boolean flying = VanillaAbilities.ALLOW_FLYING.getTracker(player).isEnabled() &&
               VanillaAbilities.ALLOW_FLYING.getTracker(player).isGrantedBy(LEVITATION_HARNESS_ABILITY) &&
               VanillaAbilities.FLYING.isEnabledFor(player) && !riding;
         boolean wasFlying = getBooleanProperty(stack, WAS_FLYING_TAG);
         
         int efficiency = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.HARNESS_RECYCLER);
         double efficiencyChance = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.HARNESS_CORE_RECYCLER_EFFICIENCY).get(efficiency);
         int soulsPerHour = ArcanaNovum.CONFIG.getInt(ArcanaConfig.LEVITATION_HARNESS_SOUL_PER_HOUR);
         int glowstonePerHour = ArcanaNovum.CONFIG.getInt(ArcanaConfig.LEVITATION_HARNESS_GLOWSTONE_PER_HOUR);
         double soulsPerSecond = soulsPerHour / 3600.0;
         double glowstonePerSecond = glowstonePerHour / 3600.0;
         
         if(world.getServer().getTickCount() % 20 == 0){
            if(chestItem && flying && survival){
               if(entity.random.nextFloat() >= efficiencyChance){
                  addEnergy(stack, -1);
                  putProperty(stack, SOULS_TAG, getSouls(stack) - soulsPerSecond);
                  putProperty(stack, GLOWSTONE_TAG, getGlow(stack) - glowstonePerSecond);
                  buildItemLore(stack, player.level().getServer());
               }
               
               ArcanaAchievements.progress(player, ArcanaAchievements.FREQUENT_FLIER, 1);
               if(player.getY() >= 1000) ArcanaAchievements.grant(player, ArcanaAchievements.TO_THE_MOON);
               
               boolean hasAllay = false, hasBlaze = false, hasBreeze = false, hasBee = false, hasDragon = false, hasPhantom = false,
                     hasGhast = false, hasHappyGhast = false, hasWither = false, hasParrot = false, hasVex = false, hasBat = false;
               for(Entity other : world.getEntities(entity, entity.getBoundingBox().inflate(32.0))){
                  EntityType<?> type = other.getType();
                  if(type == EntityType.ALLAY){
                     hasAllay = true;
                  }else if(type == EntityType.BLAZE){
                     hasBlaze = true;
                  }else if(type == EntityType.BREEZE){
                     hasBreeze = true;
                  }else if(type == EntityType.BEE){
                     hasBee = true;
                  }else if(type == EntityType.ENDER_DRAGON){
                     hasDragon = true;
                  }else if(type == EntityType.PHANTOM){
                     hasPhantom = true;
                  }else if(type == EntityType.GHAST){
                     hasGhast = true;
                  }else if(type == EntityType.HAPPY_GHAST){
                     hasHappyGhast = true;
                  }else if(type == EntityType.WITHER){
                     hasWither = true;
                  }else if(type == EntityType.PARROT){
                     hasParrot = true;
                  }else if(type == EntityType.VEX){
                     hasVex = true;
                  }else if(type == EntityType.BAT){
                     hasBat = true;
                  }
               }
               if(hasAllay && hasBlaze && hasBreeze && hasBee && hasDragon && hasPhantom && hasGhast && hasHappyGhast && hasWither && hasParrot && hasVex && hasBat){
                  ArcanaAchievements.grant(player, ArcanaAchievements.AIR_TRAFFIC_CONTROL);
               }
               
               ArcanaEffectUtils.harnessFly(serverWorld, player, 10);
               ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_LEVITATION_HARNESS_PER_SECOND));
               
               if(world.getServer().getTickCount() % 120 == 0){
                  SoundUtils.playSongToPlayer(player, SoundEvents.BEACON_AMBIENT, 1f, 0.8f);
               }
            }
            int stall = getIntProperty(stack, STALL_TAG);
            if(stall > 0){
               if(stall == 1){
                  SoundUtils.playSongToPlayer(player, SoundEvents.BEACON_POWER_SELECT, 0.5f, 1.6f);
                  player.displayClientMessage(Component.literal("Your Harness Reboots").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC), true);
                  putProperty(stack, STALL_TAG, -1);
               }else{
                  putProperty(stack, STALL_TAG, stall - 1);
               }
            }
         }
         
         if(!chestItem && wasFlying){
            putProperty(stack, WAS_FLYING_TAG, false);
         }else if(chestItem && survival){
            if(wasFlying && !flying){
               // Deactivate sound
               SoundUtils.playSongToPlayer(player, SoundEvents.BEACON_DEACTIVATE, 0.5f, 0.9f);
               putProperty(stack, WAS_FLYING_TAG, false);
            }else if(!wasFlying && flying){
               // Activate Sound
               SoundUtils.playSongToPlayer(player, SoundEvents.BEACON_ACTIVATE, 0.5f, 1.7f);
               putProperty(stack, WAS_FLYING_TAG, true);
            }
         }
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         if(playerEntity.isShiftKeyDown()){
            ItemStack item = playerEntity.getItemInHand(hand);
            openGui(playerEntity, item);
            if(playerEntity instanceof ServerPlayer player){
               Inventory inv = player.getInventory();
               player.connection.send(new ClientboundContainerSetSlotPacket(player.inventoryMenu.containerId, player.inventoryMenu.incrementStateId(), hand == InteractionHand.MAIN_HAND ? 36 + inv.getSelectedSlot() : 45, item));
               player.connection.send(new ClientboundContainerSetSlotPacket(player.inventoryMenu.containerId, player.inventoryMenu.incrementStateId(), 6, player.getItemBySlot(EquipmentSlot.CHEST)));
            }
            return InteractionResult.SUCCESS_SERVER;
         }else{
            return super.use(world, playerEntity, hand);
         }
      }
   }
}

