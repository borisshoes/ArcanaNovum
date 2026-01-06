package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.entities.StasisPearlEntity;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.ArcanaNovum.log;

public class StasisPearl extends EnergyItem {
	public static final String ID = "stasis_pearl";
   
   public static final String PEARL_ID_TAG = "pearlID";
   
   public StasisPearl(){
      id = ID;
      name = "Stasis Pearl";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS};
      initEnergy = 60;
      vanillaItem = Items.ENDER_PEARL;
      item = new StasisPearlItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_TEMPORAL_MOMENT,ResearchTasks.ADVANCEMENT_OBTAIN_ANCIENT_DEBRIS,ResearchTasks.USE_ENDER_PEARL,ResearchTasks.UNLOCK_TWILIGHT_ANVIL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,PEARL_ID_TAG,"");
      putProperty(stack,ACTIVE_TAG,false);
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("This ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Ender Pearl").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" has the ability to ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("freeze ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("its passage through ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("time").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("When ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("frozen ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("pearl ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("looks like it's '").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("hanging").withStyle(ChatFormatting.ITALIC, ChatFormatting.AQUA))
            .append(Component.literal("' in the air.").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("It requires the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("flowing of time").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" to ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("recharge ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("it.").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" to ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("freeze ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("pearl ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("in flight.").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click again").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" to ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("release ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("pearl").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal(""));
      
      if(itemStack == null){
         lore.add(Component.literal("")
               .append(Component.literal("Charged - ").withStyle(ChatFormatting.DARK_AQUA))
               .append(Component.literal("100%").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE)));
      }else{
         int charge = (getEnergy(itemStack)*100/getMaxEnergy(itemStack));
         String charging = charge == 100 ? "Charged" : "Charging";
         lore.add(Component.literal("")
               .append(Component.literal(charging+" - ").withStyle(ChatFormatting.DARK_AQUA))
               .append(Component.literal(charge+"%").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE)));
      }
      
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){ // 1 minute base recharge time
      return 60 - 10*Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.STASIS_ACCELERATION.id));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      boolean active = getBooleanProperty(stack,ACTIVE_TAG);
      String pearlID = getStringProperty(stack,PEARL_ID_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,ACTIVE_TAG,active);
      putProperty(newStack,PEARL_ID_TAG,pearlID);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("    Stasis Pearl").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nBy freezing an Ender Pearl in flight, I can leave it hanging in the air until I need it, out of phase from the timeline, unable to be destroyed. When unfrozen, it acts like a normal Ender Pearl.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Stasis Pearl").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD), Component.literal("\nUse the Pearl to throw it like normal.\n\nUsing it again will freeze or unfreeze the Pearl from stasis.\n\nThe Pearl takes time after activation to resync to the timeline before subsequent use.\n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Stasis Pearl").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD), Component.literal("\nThe Pearl cannot sustain stasis forever, and will decay if left in stasis for longer than 30 minutes.\n\nPerhaps a different item is more suitable for reliable teleportation over long periods of time.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class StasisPearlItem extends ArcanaPolymerItem {
      public StasisPearlItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         List<String> stringList = new ArrayList<>();
         
         boolean active = getBooleanProperty(itemStack,ACTIVE_TAG);
         String pearlID = getStringProperty(itemStack,PEARL_ID_TAG);
         
         if(pearlID.isEmpty()){
            stringList.add(getEnergy(itemStack) >= getMaxEnergy(itemStack) ? "charged" : "cooldown");
         }else{
            stringList.add(active ? "stasis" : "flight");
         }
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public Item getPolymerItem(ItemStack itemStack, PacketContext context){
         if(!ArcanaItemUtils.isArcane(itemStack)) return vanillaItem;
         boolean active = getBooleanProperty(itemStack,ACTIVE_TAG);
         
         return active ? Items.ENDER_EYE : vanillaItem;
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerLevel serverWorld && entity instanceof ServerPlayer player)) return;
         boolean active = getBooleanProperty(stack,ACTIVE_TAG);
         String pearlID = getStringProperty(stack,PEARL_ID_TAG);
         
         if(!pearlID.isEmpty()){
            Entity foundEntity = null;
            for(ServerLevel possibleWorlds : serverWorld.getServer().getAllLevels()){
               Entity possibleEntity = possibleWorlds.getEntity(AlgoUtils.getUUID(pearlID));
               if(foundEntity == null && possibleEntity != null){
                  foundEntity = possibleEntity;
               }
            }
            if(!(foundEntity instanceof StasisPearlEntity pearlEntity)){
               putProperty(stack,ACTIVE_TAG,false);
               putProperty(stack,PEARL_ID_TAG,"");
            }
         }
         
         if(world.getServer().getTickCount() % 20 == 0){
            addEnergy(stack, 1); // Recharge
            buildItemLore(stack,world.getServer());
         }
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         boolean active = getBooleanProperty(stack,ACTIVE_TAG);
         String pearlID = getStringProperty(stack,PEARL_ID_TAG);
         boolean canDelete = playerEntity.isShiftKeyDown() && Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SPATIAL_FOLD.id)) >= 1;
         
         try{
            if(pearlID.isEmpty()){ // Throw new pearl
               if(getEnergy(stack) >= getMaxEnergy(stack)){
                  SoundUtils.playSound(world,playerEntity.blockPosition(), SoundEvents.ENDER_PEARL_THROW, SoundSource.PLAYERS, 0.5F, 0.4F);
                  playerEntity.getCooldowns().addCooldown(stack, 0);
                  if(!world.isClientSide()){
                     StasisPearlEntity stasisPearlEntity = new StasisPearlEntity(world, playerEntity, getUUID(stack), getCompoundProperty(stack,AUGMENTS_TAG));
                     stasisPearlEntity.shootFromRotation(playerEntity, playerEntity.getXRot(), playerEntity.getYRot(), 0.0F, 1.5F, 1.0F);
                     world.addFreshEntity(stasisPearlEntity);
                     String newPearlID = stasisPearlEntity.getStringUUID();
                     putProperty(stack,PEARL_ID_TAG,newPearlID);
                     
                     setEnergy(stack,0);
                     ArcanaNovum.data(playerEntity).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_STASIS_PEARL_USE));
                  }
               }else{
                  playerEntity.getCooldowns().addCooldown(stack, 0);
                  if(playerEntity instanceof ServerPlayer player){
                     playerEntity.displayClientMessage(Component.literal("Pearl Recharging: "+(getEnergy(stack)*100/getMaxEnergy(stack))+"%").withStyle(ChatFormatting.BLUE),true);
                     SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH,1,.5f);
                  }
               }
            }else if(canDelete){ // Delete current pearl
               if(world instanceof ServerLevel serverWorld){
                  Entity foundEntity = null;
                  for(ServerLevel possibleWorlds : serverWorld.getServer().getAllLevels()){
                     Entity possibleEntity = possibleWorlds.getEntity(AlgoUtils.getUUID(pearlID));
                     if(foundEntity == null && possibleEntity != null){
                        foundEntity = possibleEntity;
                     }
                  }
                  if(foundEntity != null) foundEntity.kill(serverWorld);
                  playerEntity.getCooldowns().addCooldown(stack, 0);
               }
               // Reset data
               putProperty(stack,ACTIVE_TAG,false);
               putProperty(stack,PEARL_ID_TAG,"");
               if(playerEntity instanceof ServerPlayer player){
                  playerEntity.displayClientMessage(Component.literal("Pearl Cancelled").withStyle(ChatFormatting.BLUE),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.CHORUS_FRUIT_TELEPORT, 1, .5f);
               }
            }else if(active){ // Un-stasis to pearl
               if(world instanceof ServerLevel serverWorld){
                  Entity foundEntity = null;
                  for(ServerLevel possibleWorlds : serverWorld.getServer().getAllLevels()){
                     Entity possibleEntity = possibleWorlds.getEntity(AlgoUtils.getUUID(pearlID));
                     if(foundEntity == null && possibleEntity != null){
                        foundEntity = possibleEntity;
                     }
                  }
                  if(foundEntity instanceof StasisPearlEntity pearlEntity){
                     pearlEntity.setStasis(false);
                     putProperty(stack,ACTIVE_TAG,false);
                     playerEntity.getCooldowns().addCooldown(stack, 0);
                     return InteractionResult.SUCCESS_SERVER;
                  }
               }
               
               // If this is reached, something went wrong and the pearl needs to be reset
               log(1,"A stasis pearl was not found to unstasis properly, resetting pearl...");
               putProperty(stack,ACTIVE_TAG,false);
               putProperty(stack,PEARL_ID_TAG,"");
            }else{ // Stasis the pearl
               if(world instanceof ServerLevel serverWorld){
                  Entity foundEntity = null;
                  for(ServerLevel possibleWorlds : serverWorld.getServer().getAllLevels()){
                     Entity possibleEntity = possibleWorlds.getEntity(AlgoUtils.getUUID(pearlID));
                     if(foundEntity == null && possibleEntity != null){
                        foundEntity = possibleEntity;
                     }
                  }
                  if(foundEntity instanceof StasisPearlEntity pearlEntity){
                     pearlEntity.setStasis(true);
                     putProperty(stack,ACTIVE_TAG,true);
                     playerEntity.getCooldowns().addCooldown(stack, 0);
                     return InteractionResult.SUCCESS_SERVER;
                  }
               }
               // If this is reached, something went wrong and the pearl needs to be reset
               log(1,"A stasis pearl was not found to stasis properly, resetting pearl...");
               putProperty(stack,ACTIVE_TAG,false);
               putProperty(stack,PEARL_ID_TAG,"");
            }
         }catch(Exception e){
            e.printStackTrace();
         }
         return InteractionResult.SUCCESS_SERVER;
      }
   }
}

