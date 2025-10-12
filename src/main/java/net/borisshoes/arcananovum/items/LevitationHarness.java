package net.borisshoes.arcananovum.items;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import io.github.ladysnake.pal.VanillaAbilities;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.gui.levitationharness.LevitationHarnessGui;
import net.borisshoes.arcananovum.gui.levitationharness.LevitationHarnessInventoryListener;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.ShulkerCoreIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
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
import static net.borisshoes.arcananovum.ArcanaRegistry.LEVITATION_HARNESS_ABILITY;

public class LevitationHarness extends EnergyItem {
	public static final String ID = "levitation_harness";
   
   public static final String SOULS_TAG = "souls";
   public static final String GLOWSTONE_TAG = "glowstone";
   public static final String STALL_TAG = "stall";
   public static final String WAS_FLYING_TAG = "wasFlying";
   public static final String STONE_DATA_TAG = "stoneData";
   
   private static final double[] efficiencyChance = {0,.1,.25,.5};
   
   public LevitationHarness(){
      id = ID;
      name = "Levitation Harness";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.EQUIPMENT};
      initEnergy = 3599; // 1 hour of charge (1 soul + 16 glowstone dust = 60 seconds of flight)
      itemVersion = 1;
      vanillaItem = Items.LEATHER_CHESTPLATE;
      item = new LevitationHarnessItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.GRAY);
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_SHULKER_CORE,ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.UNLOCK_STELLAR_CORE,ResearchTasks.ADVANCEMENT_ELYTRA,ResearchTasks.UNLOCK_ARCANE_SINGULARITY,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,SOULS_TAG,500);
      putProperty(stack,GLOWSTONE_TAG,960);
      putProperty(stack,STALL_TAG,-1);
      putProperty(stack,WAS_FLYING_TAG,false);
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("Mastery over the nature of ").formatted(Formatting.WHITE))
            .append(Text.literal("Shulkers ").formatted(Formatting.YELLOW))
            .append(Text.literal("has yielded the ").formatted(Formatting.WHITE))
            .append(Text.literal("Levitation Harness!").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Grants ").formatted(Formatting.WHITE))
            .append(Text.literal("creative flight").formatted(Formatting.AQUA))
            .append(Text.literal(" while consuming ").formatted(Formatting.WHITE))
            .append(Text.literal("Shulker ").formatted(Formatting.YELLOW))
            .append(Text.literal("souls ").formatted(Formatting.DARK_RED))
            .append(Text.literal("and ").formatted(Formatting.WHITE))
            .append(Text.literal("Glowstone").formatted(Formatting.YELLOW))
            .append(Text.literal(".").formatted(Formatting.WHITE)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.WHITE))
            .append(Text.literal("Harness ").formatted(Formatting.GRAY))
            .append(Text.literal("is quite ").formatted(Formatting.WHITE))
            .append(Text.literal("fragile ").formatted(Formatting.YELLOW))
            .append(Text.literal("and the slightest bump causes it to ").formatted(Formatting.WHITE))
            .append(Text.literal("stall").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.WHITE)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.WHITE))
            .append(Text.literal("Harness ").formatted(Formatting.GRAY))
            .append(Text.literal("also provides ").formatted(Formatting.WHITE))
            .append(Text.literal("no protection").formatted(Formatting.DARK_RED))
            .append(Text.literal(" against ").formatted(Formatting.WHITE))
            .append(Text.literal("damage").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.WHITE)));
      lore.add(Text.literal("")
            .append(Text.literal("Sneak Right click").formatted(Formatting.BLUE))
            .append(Text.literal(" while ").formatted(Formatting.WHITE))
            .append(Text.literal("holding ").formatted(Formatting.AQUA))
            .append(Text.literal("the ").formatted(Formatting.WHITE))
            .append(Text.literal("harness ").formatted(Formatting.GRAY))
            .append(Text.literal("to open the refuelling menu.").formatted(Formatting.WHITE)));
      lore.add(Text.literal(""));
      
      String duration = itemStack != null ? getDuration(itemStack) : "60 Minutes";
      
      lore.add(Text.literal("")
            .append(Text.literal("Flight Duration ").formatted(Formatting.AQUA))
            .append(Text.literal("- ").formatted(Formatting.WHITE))
            .append(Text.literal(duration).formatted(Formatting.YELLOW)));
      
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      return Integer.MAX_VALUE;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound stoneData = getCompoundProperty(stack,STONE_DATA_TAG);
      int souls = getIntProperty(stack,SOULS_TAG);
      int stall = getIntProperty(stack,STALL_TAG);
      int glowstone = getIntProperty(stack,GLOWSTONE_TAG);
      boolean wasFlying = getBooleanProperty(stack,WAS_FLYING_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,STONE_DATA_TAG,stoneData);
      putProperty(newStack,SOULS_TAG,souls);
      putProperty(newStack,STALL_TAG,stall);
      putProperty(newStack,GLOWSTONE_TAG,glowstone);
      putProperty(newStack,WAS_FLYING_TAG,wasFlying);
      return buildItemLore(newStack,server);
   }
   
   public String getDuration(ItemStack item){
      int energy = getEnergy(item);
      String duration;
      if(energy >= 6000){
         duration = ((energy/3600)+1)+" Hours";
      }else if(energy >= 100){
         duration = ((energy/60)+1)+" Minutes";
      }else{
         duration = energy+" Seconds";
      }
      return duration;
   }
   
   public void recalculateEnergy(ItemStack stack){
      int souls = getIntProperty(stack,SOULS_TAG);
      int glowstone = getIntProperty(stack,GLOWSTONE_TAG) / 16;
      setEnergy(stack,60*Math.min(souls,glowstone));
      buildItemLore(stack, BorisLib.SERVER);
   }
   
   public int getGlow(ItemStack stack){
      return getIntProperty(stack,GLOWSTONE_TAG);
   }
   
   public int getSouls(ItemStack stack){
      return getIntProperty(stack,SOULS_TAG);
   }
   
   public void setStone(ItemStack stack, ItemStack stone){
      if(stone == null || stone.isEmpty()){
         putProperty(stack,STONE_DATA_TAG,new NbtCompound());
         putProperty(stack,SOULS_TAG,-1);
      }else{
         putProperty(stack,STONE_DATA_TAG,ItemStack.CODEC.encodeStart(RegistryOps.of(NbtOps.INSTANCE,BorisLib.SERVER.getRegistryManager()),stone).getOrThrow());
         putProperty(stack,SOULS_TAG,Soulstone.getSouls(stone));
      }
   }
   
   public void addGlow(ItemStack stack, int glow){
      int newGlow = Math.max(0,getIntProperty(stack,GLOWSTONE_TAG)+glow);
      putProperty(stack,GLOWSTONE_TAG,newGlow);
   }
   
   public int getStall(ItemStack stack){
      return getIntProperty(stack,STALL_TAG);
   }
   
   public void setStall(ItemStack stack, int seconds){
      putProperty(stack,STALL_TAG,seconds);
   }
   
   public void buildGui(ItemStack stack, LevitationHarnessGui gui){
      int souls = getSouls(stack);
      int glow = getGlow(stack);
      int energy = getEnergy(stack);
      String soulText = souls > -1 ? LevelUtils.readableInt(souls) + " Shulker Souls" : "No Soulstone Inserted";
      String durationText = energy > 0 ? "Flight Time Remaining: "+getDuration(stack) : "No Fuel!";
      String glowText = glow > 0 ? LevelUtils.readableInt(glow) + " Glowstone Left" : "No Glowstone Remaining";
      GuiElementBuilder soulPane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP,souls > -1 ? ArcanaColors.ARCANA_COLOR : ArcanaColors.DARK_COLOR));
      GuiElementBuilder durationPane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP,energy > 0 ? ArcanaColors.LIGHT_COLOR : 0x880000));
      GuiElementBuilder glowPane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP,glow > 0 ? 0xffdd00 : ArcanaColors.DARK_COLOR));
      Formatting soulTextColor = souls > -1 ? Formatting.LIGHT_PURPLE : Formatting.RED;
      Formatting durationTextColor = energy > 0 ? Formatting.GRAY : Formatting.RED;
      Formatting glowTextColor = glow > 0 ? Formatting.GOLD : Formatting.RED;
      
      gui.setSlot(0,soulPane.setName(Text.literal(soulText).formatted(soulTextColor)));
      gui.setSlot(2,durationPane.setName(Text.literal(durationText).formatted(durationTextColor)));
      gui.setSlot(4,glowPane.setName(Text.literal(glowText).formatted(glowTextColor)));
   }
   
   public void openGui(PlayerEntity playerEntity, ItemStack stack){
      if(!(playerEntity instanceof ServerPlayerEntity player))
         return;
      LevitationHarnessGui gui = new LevitationHarnessGui(ScreenHandlerType.HOPPER,player,this, stack);
      
      int souls = getSouls(stack);
      
      for(int i = 0; i < gui.getSize(); i++){
         gui.clearSlot(i);
      }
      
      buildGui(stack, gui);
      
      SimpleInventory inv = new SimpleInventory(2);
      LevitationHarnessInventoryListener listener = new LevitationHarnessInventoryListener(this,gui,stack);
      inv.addListener(listener);
      listener.setUpdating();
      
      gui.setSlotRedirect(1, new Slot(inv,0,0,0));
      gui.setSlotRedirect(3, new Slot(inv,1,0,0));
      if(souls > -1){
         NbtCompound stoneData = getCompoundProperty(stack,STONE_DATA_TAG);
         ItemStack stone;
         if(stoneData == null || stoneData.isEmpty()){
            stone = Soulstone.setType(ArcanaRegistry.SOULSTONE.getNewItem(), EntityType.SHULKER);
         }else{
            stone = ItemStack.CODEC.parse(RegistryOps.of(NbtOps.INSTANCE,player.getRegistryManager()),stoneData).result().orElse(ItemStack.EMPTY);
         }
         stone = Soulstone.setSouls(stone,souls);
         inv.setStack(0,stone);
         gui.validStone(stone);
      }else{
         gui.notValidStone();
      }
      gui.setTitle(Text.literal("Levitation Harness"));
      listener.finishUpdate();
      
      gui.open();
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv, StarlightForgeBlockEntity starlightForge){
      // Souls n stuff
      ItemStack coreStack = inv.getStack(12); // Should be the Core
      ItemStack newArcanaItem = null;
      if(ArcanaItemUtils.identifyItem(coreStack) instanceof ShulkerCore core){
         newArcanaItem = getNewItem();
         setStone(newArcanaItem,core.getStone(coreStack));
         buildItemLore(newArcanaItem,BorisLib.SERVER);
         
         ArcanaAugments.copyAugment(coreStack,newArcanaItem,ArcanaAugments.SHULKER_RECYCLER.id,ArcanaAugments.HARNESS_RECYCLER.id);
      }
      
      return newArcanaItem;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ShulkerCoreIngredient m = new ShulkerCoreIngredient(true,500);
      ArcanaIngredient a = new ArcanaIngredient(Items.GLOWSTONE,32);
      ArcanaIngredient b = new ArcanaIngredient(Items.SHULKER_SHELL,24);
      ArcanaIngredient c = new ArcanaIngredient(Items.ELYTRA,1);
      ArcanaIngredient g = new ArcanaIngredient(Items.NETHER_STAR,4);
      ArcanaIngredient h = new ArcanaIngredient(Items.NETHERITE_INGOT,4);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withEnchanter().withCore().withAnvil().withSingularity());
      
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("Levitation Harness").formatted(Formatting.GRAY,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nThe sheer amount of effort and research that has gone into this is incomparable. A crowning achievement to be sure. The ability to fly freely through the sky is at my command, albeit fueled by innocent souls.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("Levitation Harness").formatted(Formatting.GRAY,Formatting.BOLD),Text.literal("\nGlowstone was an adequate moderator for the Shulker Core, but now it is an absolute necessity that is consumed in large quantities to stabilize the flight reaction. Even with more Glowstone, the reaction is incredibly sensitive to damage.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("Levitation Harness").formatted(Formatting.GRAY,Formatting.BOLD),Text.literal("\nWearing the Harness grants creative flight. The Harness provides no armor value, and taking even the slightest bump while in flight will destabilize the flight process, dealing half my health in damage, and taking a few seconds to restabilize.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class LevitationHarnessItem extends ArcanaPolymerItem {
      public LevitationHarnessItem(){
         super(getThis(),getEquipmentArcanaItemComponents()
               .armor(ArcanaRegistry.NON_PROTECTIVE_ARMOR_MATERIAL, EquipmentType.CHESTPLATE)
               .component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x966996))
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
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld serverWorld && entity instanceof ServerPlayerEntity player)) return;
         boolean chestItem = ItemStack.areItemsAndComponentsEqual(player.getEquippedStack(EquipmentSlot.CHEST),stack);
         boolean riding = player.hasVehicle();
         boolean survival = !(player.isCreative() || player.isSpectator());
         boolean flying = VanillaAbilities.ALLOW_FLYING.getTracker(player).isEnabled() &&
               VanillaAbilities.ALLOW_FLYING.getTracker(player).isGrantedBy(LEVITATION_HARNESS_ABILITY) &&
               VanillaAbilities.FLYING.isEnabledFor(player) && !riding;
         boolean wasFlying = getBooleanProperty(stack,WAS_FLYING_TAG);
         
         int efficiency = Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.HARNESS_RECYCLER.id));
         
         if(world.getServer().getTicks() % 20 == 0){
            if(chestItem && flying && survival){
               if(Math.random() >= efficiencyChance[efficiency]){
                  addEnergy(stack,-1);
                  if(getEnergy(stack) % 60 == 0){
                     putProperty(stack,SOULS_TAG,getSouls(stack)-1);
                     putProperty(stack,GLOWSTONE_TAG,getGlow(stack)-16);
                  }
                  buildItemLore(stack,player.getServer());
               }
               
               ArcanaAchievements.progress(player,ArcanaAchievements.FREQUENT_FLIER.id,1);
               if(player.getY() >= 1000) ArcanaAchievements.grant(player,ArcanaAchievements.TO_THE_MOON.id);
               
               boolean hasAllay = false, hasBlaze = false, hasBreeze = false, hasBee = false, hasDragon = false, hasPhantom = false,
                     hasGhast = false, hasHappyGhast = false, hasWither = false, hasParrot = false, hasVex = false, hasBat = false;
               for(Entity other : world.getOtherEntities(entity, entity.getBoundingBox().expand(32.0))){
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
                  ArcanaAchievements.grant(player,ArcanaAchievements.AIR_TRAFFIC_CONTROL.id);
               }
               
               ArcanaEffectUtils.harnessFly(serverWorld,player,10);
               ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.LEVITATION_HARNESS_PER_SECOND));
               
               if(world.getServer().getTicks() % 120 == 0){
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_AMBIENT,1f,0.8f);
               }
            }
            int stall = getIntProperty(stack,STALL_TAG);
            if(stall > 0){
               if(stall == 1){
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_POWER_SELECT,0.5f,1.6f);
                  player.sendMessage(Text.literal("Your Harness Reboots").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
                  putProperty(stack,STALL_TAG,-1);
               }else{
                  putProperty(stack,STALL_TAG,stall-1);
               }
            }
         }
         
         if(!chestItem && wasFlying){
            putProperty(stack,WAS_FLYING_TAG,false);
         }else if(chestItem && survival){
            if(wasFlying && !flying){
               // Deactivate sound
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_DEACTIVATE,0.5f,0.9f);
               putProperty(stack,WAS_FLYING_TAG,false);
            }else if(!wasFlying && flying){
               // Activate Sound
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_ACTIVATE,0.5f,1.7f);
               putProperty(stack,WAS_FLYING_TAG,true);
            }
         }
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
         if(playerEntity.isSneaking()){
            ItemStack item = playerEntity.getStackInHand(hand);
            openGui(playerEntity,item);
            if(playerEntity instanceof ServerPlayerEntity player){
               PlayerInventory inv = player.getInventory();
               player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), hand == Hand.MAIN_HAND ? 36 + inv.getSelectedSlot() : 45, item));
               player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), 6, player.getEquippedStack(EquipmentSlot.CHEST)));
            }
            return ActionResult.SUCCESS_SERVER;
         }else{
            return super.use(world, playerEntity, hand);
         }
      }
   }
}

