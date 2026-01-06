package net.borisshoes.arcananovum.items;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.gui.shulkercore.ShulkerCoreGui;
import net.borisshoes.arcananovum.gui.shulkercore.ShulkerCoreInventoryListener;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.SoulstoneIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ShulkerCore extends EnergyItem {
	public static final String ID = "shulker_core";
   
   public static final String SPEED_TAG = "speed";
   public static final String SPEED_CD_TAG = "speedCD";
   public static final String STONE_TAG = "stone";
   public static final String STONE_DATA_TAG = "stoneData";
   
   public ShulkerCore(){
      id = ID;
      name = "Shulker Core";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS};
      initEnergy = 1000;
      vanillaItem = Items.SHULKER_BOX;
      item = new ShulkerCoreItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.SHULKER_CORE_COLOR);
      itemVersion = 1;
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_SOULSTONE,ResearchTasks.ADVANCEMENT_LEVITATE,ResearchTasks.EFFECT_SLOW_FALLING,ResearchTasks.UNLOCK_STELLAR_CORE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,SPEED_TAG,1);
      putProperty(stack,SPEED_CD_TAG,0);
      putProperty(stack,STONE_TAG,true);
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("Shulkers ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("possess a ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("unique ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("ability to defy ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("gravity").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("This ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Core ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("has ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("harnessed").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" that ability to allow ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("controlled").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" levitation").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Core").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(" must be ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("refilled ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("with ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Shulkers").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" to grant ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("levitation").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right Click").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" to change the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("speed ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("of ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("levitation").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right Click in off-hand ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("to ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("refill ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Core").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal(""));

      int energy = itemStack != null ? getEnergy(itemStack) : 1000;
      
      lore.add(Component.literal("")
            .append(Component.literal("Shulkers Left").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(""+energy).withStyle(ChatFormatting.YELLOW)));
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
      CompoundTag stoneData = getCompoundProperty(stack,STONE_DATA_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,SPEED_TAG,speed);
      putProperty(newStack,SPEED_CD_TAG,speedCD);
      putProperty(newStack,STONE_TAG,stone);
      putProperty(newStack,STONE_DATA_TAG,stoneData);
      return buildItemLore(newStack,server);
   }
   
   private void changeSpeed(Player playerEntity, Level world, InteractionHand hand){
      ItemStack stack = playerEntity.getItemInHand(hand);
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
         if(playerEntity instanceof ServerPlayer player){
            if(speed == 11){
               player.displayClientMessage(Component.literal("Shulker Core Mode: Reabsorption").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 1);
            }else{
               player.displayClientMessage(Component.literal("Shulker Core Speed: "+(speed/2+1)).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC),true);
               float pitch = (float) (0.1875*speed+0.3125);
               SoundUtils.playSongToPlayer(player, SoundEvents.NOTE_BLOCK_XYLOPHONE, 0.5f, pitch);
            }
         }
      }
   }
   
   private void levitate(Player playerEntity, Level world, InteractionHand hand){
      ItemStack stack = playerEntity.getItemInHand(hand);
      int speed = getIntProperty(stack,SPEED_TAG);
      final int duration = 100;
      
      if(speed == 11){
         if(playerEntity.hasEffect(MobEffects.LEVITATION)){
            playerEntity.removeEffect(MobEffects.LEVITATION);
            SoundUtils.playSound(world, playerEntity.blockPosition(), SoundEvents.SHULKER_SHOOT, SoundSource.PLAYERS, 1, 0.8f);
         }
      }else{
         if(getEnergy(stack) > 0){
            MobEffectInstance effect = playerEntity.getEffect(MobEffects.LEVITATION);
            if(!(effect != null && effect.getEffect() == MobEffects.LEVITATION && effect.getAmplifier() >= speed && !(effect.getDuration() < 10 || effect.getDuration() > duration))){
               MobEffectInstance levit = new MobEffectInstance(MobEffects.LEVITATION, duration, speed, false, false, false);
               if(Math.random() >= (new double[]{0,0.1,0.25,0.5})[Math.max(0, ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.SHULKER_RECYCLER.id))])
                  addEnergy(stack, -(speed / 2 + 1));
               playerEntity.addEffect(levit);
               SoundUtils.playSound(world, playerEntity.blockPosition(), SoundEvents.SHULKER_SHOOT, SoundSource.PLAYERS, 1, 0.8f);
               ArcanaNovum.data(playerEntity).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_SHULKER_CORE_PER_SOUL) * (speed / 2 + 1)); // Add xp
               if(world instanceof ServerLevel serverWorld){
                  ArcanaEffectUtils.shulkerCoreLevitate(serverWorld, playerEntity, duration);
               }
               buildItemLore(stack,playerEntity.level().getServer());
            }
         }else{
            playerEntity.displayClientMessage(Component.literal("The Shulker Core is empty.").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC), true);
            SoundUtils.playSongToPlayer((ServerPlayer) playerEntity, SoundEvents.FIRE_EXTINGUISH, 1,0.8f);
         }
      }
   }
   
   public void openGui(Player playerEntity, ItemStack stack){
      if(!(playerEntity instanceof ServerPlayer player))
         return;
      ShulkerCoreGui gui = new ShulkerCoreGui(MenuType.HOPPER,player,this, stack);
      
      boolean hasStone = getBooleanProperty(stack,STONE_TAG);
      
      for(int i = 0; i < gui.getSize(); i++){
         gui.clearSlot(i);
      }
      
      GuiElementBuilder pane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP,hasStone ? ArcanaColors.ARCANA_COLOR : ArcanaColors.DARK_COLOR));
      String paneText = hasStone ? LevelUtils.readableInt(getEnergy(stack)) + " Shulker Souls" : "No Soulstone Inserted";
      ChatFormatting textColor = hasStone ? ChatFormatting.YELLOW : ChatFormatting.RED;
      
      gui.setSlot(0,pane.setName(Component.literal(paneText).withStyle(textColor)));
      gui.setSlot(1,pane.setName(Component.literal(paneText).withStyle(textColor)));
      gui.setSlot(3,pane.setName(Component.literal(paneText).withStyle(textColor)));
      gui.setSlot(4,pane.setName(Component.literal(paneText).withStyle(textColor)));
      
      SimpleContainer inv = new SimpleContainer(1);
      ShulkerCoreInventoryListener listener = new ShulkerCoreInventoryListener(this,gui,stack);
      inv.addListener(listener);
      listener.setUpdating();
      
      gui.setSlotRedirect(2, new Slot(inv,0,0,0));
      if(hasStone){
         CompoundTag stoneData = getCompoundProperty(stack,STONE_DATA_TAG);
         ItemStack stone;
         if(stoneData == null || stoneData.isEmpty()){
            stone = Soulstone.setType(ArcanaRegistry.SOULSTONE.getNewItem(), EntityType.SHULKER);
         }else{
            stone = ItemStack.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE,player.registryAccess()),stoneData).result().orElse(ItemStack.EMPTY);
         }
         stone = Soulstone.setSouls(stone,getEnergy(stack));
         
         inv.setItem(0,stone);
         gui.validStone(stone);
      }else{
         gui.notValid();
      }
      gui.setTitle(Component.literal("Shulker Core"));
      listener.finishUpdate();
      
      gui.open();
   }
   
   public boolean hasStone(ItemStack stack){
      return getBooleanProperty(stack,STONE_TAG);
   }
   
   public ItemStack getStone(ItemStack stack){
      if(ArcanaItemUtils.identifyItem(stack) instanceof ShulkerCore){
         if(getBooleanProperty(stack,STONE_TAG)){
            CompoundTag stoneData = getCompoundProperty(stack,STONE_DATA_TAG);
            ItemStack stone;
            if(stoneData == null || stoneData.isEmpty()){
               stone = Soulstone.setType(ArcanaRegistry.SOULSTONE.getNewItem(), EntityType.SHULKER);
            }else{
               stone = ItemStack.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, BorisLib.SERVER.registryAccess()),stoneData).result().orElse(ItemStack.EMPTY);
            }
            stone = Soulstone.setSouls(stone,getEnergy(stack));
            
            return stone;
         }
      }
      return null;
   }
   
   public void setStone(ItemStack stack, ItemStack stone){
      if(stone == null || stone.isEmpty()){
         putProperty(stack,STONE_TAG,false);
         putProperty(stack,STONE_DATA_TAG,new CompoundTag());
         setEnergy(stack,0);
      }else{
         putProperty(stack,STONE_TAG,true);
         putProperty(stack,STONE_DATA_TAG, ItemStack.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE,BorisLib.SERVER.registryAccess()),stone).getOrThrow());
         setEnergy(stack,Soulstone.getSouls(stone));
      }
   }
   
   @Override
   public ItemStack forgeItem(Container inv, StarlightForgeBlockEntity starlightForge){
      // Souls n stuff
      ItemStack soulstoneStack = inv.getItem(12); // Should be the Soulstone
      ItemStack newArcanaItem = null;
      if(ArcanaItemUtils.identifyItem(soulstoneStack) instanceof Soulstone){
         newArcanaItem = getNewItem();
         setStone(newArcanaItem,soulstoneStack);
         buildItemLore(newArcanaItem,BorisLib.SERVER);
      }
      return newArcanaItem;
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("    Shulker Core").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nShulkers are fascinating creatures. Their unique levitation effect could be a precursor to true flight if I combined a bit of their essence… er… a lot of their essence… What’s a bit of genocide anyways?").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Shulker Core").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\nAfter a massacre that took too long to comprehend, I have enough souls to control their power.\nUse the Core to grant levitation. Sneak Use to change the speed. Sneak Use in my off-hand to access the Soulstone for refuelling.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class ShulkerCoreItem extends ArcanaPolymerItem {
      public ShulkerCoreItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public InteractionResult useOn(UseOnContext context){
         Player playerEntity = context.getPlayer();
         ItemStack stack = context.getItemInHand();
         if(playerEntity != null && playerEntity.isShiftKeyDown()){
            if(context.getHand() == InteractionHand.MAIN_HAND){
               changeSpeed(playerEntity,context.getLevel(),context.getHand());
            }else{
               openGui(playerEntity,stack);
            }
         }else if(playerEntity != null){
            levitate(playerEntity,context.getLevel(),context.getHand());
         }
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         if(playerEntity.isShiftKeyDown()){
            if(hand == InteractionHand.MAIN_HAND){
               changeSpeed(playerEntity,world,hand);
            }else{
               openGui(playerEntity,playerEntity.getItemInHand(hand));
            }
         }else{
            levitate(playerEntity,world,hand);
         }
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerLevel && entity instanceof ServerPlayer player)) return;
         int speedCD = getIntProperty(stack,SPEED_CD_TAG);
         if(speedCD > 0){
            speedCD--;
            putProperty(stack,SPEED_CD_TAG,speedCD);
         }
      }
   }
}

