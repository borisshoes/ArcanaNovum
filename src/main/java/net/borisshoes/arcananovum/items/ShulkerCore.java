package net.borisshoes.arcananovum.items;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.gui.shulkercore.ShulkerCoreGui;
import net.borisshoes.arcananovum.gui.shulkercore.ShulkerCoreInventory;
import net.borisshoes.arcananovum.gui.shulkercore.ShulkerCoreInventoryListener;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.SoulstoneIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class ShulkerCore extends EnergyItem {
	public static final String ID = "shulker_core";
   
   public static final String SPEED_TAG = "speed";
   public static final String SPEED_CD_TAG = "speedCD";
   public static final String STONE_TAG = "stone";
   public static final String STONE_DATA_TAG = "stoneData";
   
   private static final String TXT = "item/shulker_core";
   
   public ShulkerCore(){
      id = ID;
      name = "Shulker Core";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EXOTIC, TomeGui.TomeFilter.ITEMS};
      initEnergy = 1000;
      vanillaItem = Items.SHULKER_BOX;
      item = new ShulkerCoreItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, TextUtils.withColor(Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD),0xFFFF99))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      itemVersion = 1;
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_SOULSTONE,ResearchTasks.ADVANCEMENT_LEVITATE,ResearchTasks.EFFECT_SLOW_FALLING,ResearchTasks.UNLOCK_STELLAR_CORE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,SPEED_TAG,1);
      putProperty(stack,SPEED_CD_TAG,0);
      putProperty(stack,STONE_TAG,true);
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("Shulkers ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("possess a ").formatted(Formatting.GRAY))
            .append(Text.literal("unique ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("ability to defy ").formatted(Formatting.GRAY))
            .append(Text.literal("gravity").formatted(Formatting.WHITE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("This ").formatted(Formatting.GRAY))
            .append(Text.literal("Core ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("has ").formatted(Formatting.GRAY))
            .append(Text.literal("harnessed").formatted(Formatting.YELLOW))
            .append(Text.literal(" that ability to allow ").formatted(Formatting.GRAY))
            .append(Text.literal("controlled").formatted(Formatting.YELLOW))
            .append(Text.literal(" levitation").formatted(Formatting.WHITE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.GRAY))
            .append(Text.literal("Core").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(" must be ").formatted(Formatting.GRAY))
            .append(Text.literal("refilled ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("with ").formatted(Formatting.GRAY))
            .append(Text.literal("Shulkers").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" to grant ").formatted(Formatting.GRAY))
            .append(Text.literal("levitation").formatted(Formatting.WHITE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Sneak Right Click").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" to change the ").formatted(Formatting.GRAY))
            .append(Text.literal("speed ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("of ").formatted(Formatting.GRAY))
            .append(Text.literal("levitation").formatted(Formatting.WHITE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Sneak Right Click in off-hand ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("to ").formatted(Formatting.GRAY))
            .append(Text.literal("refill ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("the ").formatted(Formatting.GRAY))
            .append(Text.literal("Core").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal(""));

      int energy = itemStack != null ? getEnergy(itemStack) : 1000;
      
      lore.add(Text.literal("")
            .append(Text.literal("Shulkers Left").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" - ").formatted(Formatting.GRAY))
            .append(Text.literal(""+energy).formatted(Formatting.YELLOW)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      return Integer.MAX_VALUE;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      int speed = getIntProperty(stack,SPEED_TAG);
      int speedCD = getIntProperty(stack,SPEED_CD_TAG);
      boolean stone = getBooleanProperty(stack,STONE_TAG);
      NbtCompound stoneData = getCompoundProperty(stack,STONE_DATA_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,SPEED_TAG,speed);
      putProperty(newStack,SPEED_CD_TAG,speedCD);
      putProperty(newStack,STONE_TAG,stone);
      putProperty(newStack,STONE_DATA_TAG,stoneData);
      return buildItemLore(newStack,server);
   }
   
   private void changeSpeed(PlayerEntity playerEntity, World world, Hand hand){
      ItemStack stack = playerEntity.getStackInHand(hand);
      int speed = getIntProperty(stack,SPEED_TAG);
      int speedCD = getIntProperty(stack,SPEED_CD_TAG);
      boolean reabsorb = Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.LEVITATIVE_REABSORPTION.id)) >= 1;
      int maxSpeed = reabsorb ? 11 : 9;
      
      if(speedCD == 0){
         // 1 3 5 7 9
         speed += 2;
         
         if(speed > maxSpeed){
            speed = 1;
         }else if(speed < 1){
            speed = maxSpeed;
         }
         
         putProperty(stack,SPEED_TAG,speed);
         putProperty(stack,SPEED_CD_TAG,5);
         if(playerEntity instanceof ServerPlayerEntity player){
            if(speed == 11){
               player.sendMessage(Text.literal("Shulker Core Mode: Reabsorption").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 1);
            }else{
               player.sendMessage(Text.literal("Shulker Core Speed: "+(speed/2+1)).formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC),true);
               float pitch = (float) (0.1875*speed+0.3125);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE, 0.5f, pitch);
            }
         }
      }
   }
   
   private void levitate(PlayerEntity playerEntity, World world, Hand hand){
      ItemStack stack = playerEntity.getStackInHand(hand);
      int speed = getIntProperty(stack,SPEED_TAG);
      final int duration = 100;
      
      if(speed == 11){
         if(playerEntity.hasStatusEffect(StatusEffects.LEVITATION)){
            playerEntity.removeStatusEffect(StatusEffects.LEVITATION);
            SoundUtils.playSound(world, playerEntity.getBlockPos(), SoundEvents.ENTITY_SHULKER_SHOOT, SoundCategory.PLAYERS, 1, 0.8f);
         }
      }else{
         if(getEnergy(stack) > 0){
            StatusEffectInstance effect = playerEntity.getStatusEffect(StatusEffects.LEVITATION);
            if(!(effect != null && effect.getEffectType() == StatusEffects.LEVITATION && effect.getAmplifier() >= speed && !(effect.getDuration() < 10 || effect.getDuration() > duration))){
               StatusEffectInstance levit = new StatusEffectInstance(StatusEffects.LEVITATION, duration, speed, false, false, false);
               if(Math.random() >= (new double[]{0,0.1,0.25,0.5})[Math.max(0, ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.SHULKER_RECYCLER.id))])
                  addEnergy(stack, -(speed / 2 + 1));
               playerEntity.addStatusEffect(levit);
               SoundUtils.playSound(world, playerEntity.getBlockPos(), SoundEvents.ENTITY_SHULKER_SHOOT, SoundCategory.PLAYERS, 1, 0.8f);
               PLAYER_DATA.get(playerEntity).addXP(50 * (speed / 2 + 1)); // Add xp
               if(world instanceof ServerWorld serverWorld){
                  ParticleEffectUtils.shulkerCoreLevitate(serverWorld, playerEntity, duration);
               }
               buildItemLore(stack,playerEntity.getServer());
            }
         }else{
            playerEntity.sendMessage(Text.literal("The Shulker Core is empty.").formatted(Formatting.YELLOW, Formatting.ITALIC), true);
            SoundUtils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,0.8f);
         }
      }
   }
   
   public void openGui(PlayerEntity playerEntity, ItemStack stack){
      if(!(playerEntity instanceof ServerPlayerEntity player))
         return;
      ShulkerCoreGui gui = new ShulkerCoreGui(ScreenHandlerType.HOPPER,player,this, stack);
      
      boolean hasStone = getBooleanProperty(stack,STONE_TAG);
      
      for(int i = 0; i < gui.getSize(); i++){
         gui.clearSlot(i);
      }
      
      GuiElementBuilder pane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP,hasStone ? ArcanaColors.ARCANA_COLOR : ArcanaColors.DARK_COLOR));
      String paneText = hasStone ? getEnergy(stack) + " Shulker Souls" : "No Soulstone Inserted";
      Formatting textColor = hasStone ? Formatting.YELLOW : Formatting.RED;
      
      gui.setSlot(0,pane.setName(Text.literal(paneText).formatted(textColor)));
      gui.setSlot(1,pane.setName(Text.literal(paneText).formatted(textColor)));
      gui.setSlot(3,pane.setName(Text.literal(paneText).formatted(textColor)));
      gui.setSlot(4,pane.setName(Text.literal(paneText).formatted(textColor)));
      
      ShulkerCoreInventory inv = new ShulkerCoreInventory();
      ShulkerCoreInventoryListener listener = new ShulkerCoreInventoryListener(this,gui,stack);
      inv.addListener(listener);
      listener.setUpdating();
      
      gui.setSlotRedirect(2, new Slot(inv,0,0,0));
      if(hasStone){
         NbtCompound stoneData = getCompoundProperty(stack,STONE_DATA_TAG);
         ItemStack stone;
         if(stoneData == null || stoneData.isEmpty()){
            stone = Soulstone.setType(ArcanaRegistry.SOULSTONE.getNewItem(), EntityType.SHULKER);
         }else{
            stone = ItemStack.fromNbt(playerEntity.getRegistryManager(),stoneData).orElse(ItemStack.EMPTY);
         }
         stone = Soulstone.setSouls(stone,getEnergy(stack));
         
         inv.setStack(0,stone);
         gui.validStone(stone);
      }else{
         gui.notValid();
      }
      gui.setTitle(Text.literal("Shulker Core"));
      listener.finishUpdate();
      
      gui.open();
   }
   
   public boolean hasStone(ItemStack stack){
      return getBooleanProperty(stack,STONE_TAG);
   }
   
   public ItemStack getStone(ItemStack stack){
      if(ArcanaItemUtils.identifyItem(stack) instanceof ShulkerCore){
         if(getBooleanProperty(stack,STONE_TAG)){
            NbtCompound stoneData = getCompoundProperty(stack,STONE_DATA_TAG);
            ItemStack stone;
            if(stoneData == null || stoneData.isEmpty()){
               stone = Soulstone.setType(ArcanaRegistry.SOULSTONE.getNewItem(), EntityType.SHULKER);
            }else{
               stone = ItemStack.fromNbt(ArcanaNovum.SERVER.getRegistryManager(),stoneData).orElse(null);
            }
            stone = Soulstone.setSouls(stone,getEnergy(stack));
            
            return stone;
         }
      }
      return null;
   }
   
   public void setStone(ItemStack stack, ItemStack stone){
      if(stone == null){
         putProperty(stack,STONE_TAG,false);
         putProperty(stack,STONE_DATA_TAG,new NbtCompound());
         setEnergy(stack,0);
      }else{
         putProperty(stack,STONE_TAG,true);
         putProperty(stack,STONE_DATA_TAG,stone.toNbtAllowEmpty(ArcanaNovum.SERVER.getRegistryManager()));
         setEnergy(stack,Soulstone.getSouls(stone));
      }
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv){
      // Souls n stuff
      ItemStack soulstoneStack = inv.getStack(12); // Should be the Soulstone
      ItemStack newArcanaItem = null;
      if(ArcanaItemUtils.identifyItem(soulstoneStack) instanceof Soulstone){
         newArcanaItem = getNewItem();
         setStone(newArcanaItem,soulstoneStack);
         buildItemLore(newArcanaItem,ArcanaNovum.SERVER);
      }
      return newArcanaItem;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      SoulstoneIngredient t = new SoulstoneIngredient(Soulstone.tiers[4],false,true, false,EntityType.getId(EntityType.SHULKER).toString());
      ArcanaIngredient a = new ArcanaIngredient(Items.PHANTOM_MEMBRANE,16);
      ArcanaIngredient b = new ArcanaIngredient(Items.SHULKER_SHELL,8);
      ArcanaIngredient c = new ArcanaIngredient(Items.POTION,1).withPotions(Potions.LONG_SLOW_FALLING);
      ArcanaIngredient g = new ArcanaIngredient(Items.GLOWSTONE_DUST,32);
      ArcanaIngredient h = new ArcanaIngredient(Items.NETHER_STAR,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,t,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withCore().withAnvil());
      
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("     Shulker Core\n\nRarity: Exotic\n\nShulkers are fascinating creatures, their unique levitation effect could be a precursor to true flight if I combined a bit of their essence... er... a lot of their essence... Whats a bit of genocide anyways?").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("     Shulker Core\n\nAfter a massacre that took too long to comprehend, I have enough essence to control their power.\n\nRight click to grant levitation.\nSneak right click to change the speed.\nLeft click to swap out the Soulstone inside.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class ShulkerCoreItem extends ArcanaPolymerItem {
      public ShulkerCoreItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.getModelData(TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public ActionResult useOnBlock(ItemUsageContext context){
         PlayerEntity playerEntity = context.getPlayer();
         ItemStack stack = context.getStack();
         if(playerEntity != null && playerEntity.isSneaking()){
            if(context.getHand() == Hand.MAIN_HAND){
               changeSpeed(playerEntity,context.getWorld(),context.getHand());
            }else{
               openGui(playerEntity,stack);
            }
         }else if(playerEntity != null){
            levitate(playerEntity,context.getWorld(),context.getHand());
         }
         return ActionResult.SUCCESS;
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
         if(playerEntity.isSneaking()){
            if(hand == Hand.MAIN_HAND){
               changeSpeed(playerEntity,world,hand);
            }else{
               openGui(playerEntity,playerEntity.getStackInHand(hand));
            }
         }else{
            levitate(playerEntity,world,hand);
         }
         return ActionResult.SUCCESS;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         int speedCD = getIntProperty(stack,SPEED_CD_TAG);
         if(speedCD > 0){
            speedCD--;
            putProperty(stack,SPEED_CD_TAG,speedCD);
         }
      }
   }
}

