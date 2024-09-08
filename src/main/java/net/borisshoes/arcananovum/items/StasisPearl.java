package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.entities.StasisPearlEntity;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class StasisPearl extends EnergyItem {
	public static final String ID = "stasis_pearl";
   
   public static final String PEARL_ID_TAG = "pearlID";
   
   private static final String CHARGED_TXT = "item/stasis_pearl_charged";
   private static final String COOLDOWN_TXT = "item/stasis_pearl_cooldown";
   private static final String FLIGHT_TXT = "item/stasis_pearl_flight";
   private static final String STASIS_TXT = "item/stasis_pearl_stasis";
   
   public StasisPearl(){
      id = ID;
      name = "Stasis Pearl";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EXOTIC, TomeGui.TomeFilter.ITEMS};
      initEnergy = 60;
      vanillaItem = Items.ENDER_PEARL;
      item = new StasisPearlItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.BLUE))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,CHARGED_TXT));
      models.add(new Pair<>(vanillaItem,COOLDOWN_TXT));
      models.add(new Pair<>(vanillaItem,FLIGHT_TXT));
      models.add(new Pair<>(Items.ENDER_EYE,STASIS_TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_TEMPORAL_MOMENT,ResearchTasks.ADVANCEMENT_OBTAIN_ANCIENT_DEBRIS,ResearchTasks.USE_ENDER_PEARL,ResearchTasks.UNLOCK_TWILIGHT_ANVIL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,PEARL_ID_TAG,"");
      putProperty(stack,ACTIVE_TAG,false);
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("This ").formatted(Formatting.GRAY))
            .append(Text.literal("Ender Pearl").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" has the ability to ").formatted(Formatting.GRAY))
            .append(Text.literal("freeze ").formatted(Formatting.AQUA))
            .append(Text.literal("its passage through ").formatted(Formatting.GRAY))
            .append(Text.literal("time").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("When ").formatted(Formatting.GRAY))
            .append(Text.literal("frozen ").formatted(Formatting.AQUA))
            .append(Text.literal("the ").formatted(Formatting.GRAY))
            .append(Text.literal("pearl ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("looks like it's '").formatted(Formatting.GRAY))
            .append(Text.literal("hanging").formatted(Formatting.ITALIC,Formatting.AQUA))
            .append(Text.literal("' in the air.").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("It requires the ").formatted(Formatting.GRAY))
            .append(Text.literal("flowing of time").formatted(Formatting.BLUE))
            .append(Text.literal(" to ").formatted(Formatting.GRAY))
            .append(Text.literal("recharge ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("it.").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.GREEN))
            .append(Text.literal(" to ").formatted(Formatting.GRAY))
            .append(Text.literal("freeze ").formatted(Formatting.AQUA))
            .append(Text.literal("the ").formatted(Formatting.GRAY))
            .append(Text.literal("pearl ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("in flight.").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click again").formatted(Formatting.GREEN))
            .append(Text.literal(" to ").formatted(Formatting.GRAY))
            .append(Text.literal("release ").formatted(Formatting.AQUA))
            .append(Text.literal("the ").formatted(Formatting.GRAY))
            .append(Text.literal("pearl").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal(""));
      
      if(itemStack == null){
         lore.add(Text.literal("")
               .append(Text.literal("Charged - ").formatted(Formatting.DARK_AQUA))
               .append(Text.literal("100%").formatted(Formatting.BOLD,Formatting.BLUE)));
      }else{
         int charge = (getEnergy(itemStack)*100/getMaxEnergy(itemStack));
         String charging = charge == 100 ? "Charged" : "Charging";
         lore.add(Text.literal("")
               .append(Text.literal(charging+" - ").formatted(Formatting.DARK_AQUA))
               .append(Text.literal(charge+"%").formatted(Formatting.BOLD,Formatting.BLUE)));
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
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.ENDER_EYE,4);
      ArcanaIngredient b = new ArcanaIngredient(Items.OBSIDIAN,12);
      ArcanaIngredient c = new ArcanaIngredient(Items.NETHERITE_SCRAP,1);
      ArcanaIngredient g = new ArcanaIngredient(Items.NETHER_STAR,1);
      ArcanaIngredient h = new ArcanaIngredient(Items.ENDER_PEARL,8);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.TEMPORAL_MOMENT,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withAnvil());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("      Stasis Pearl\n\nRarity: Exotic\n\nSimilar to the Pearl of\nRecall, except instead of freezing the Pearl during activation its frozen while in flight.\nThe Pearl is highly volitile and can dematerialize if frozen for too long, or unloaded in flight.\n").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("      Stasis Pearl\n\nRight Click throws the pearl like normal.\nRight Clicking while it is in flight puts it in stasis.\nRight Clicking while frozen will remove it from stasis and continue moving.\nLike the Recall Pearl, this one takes time to resync to the timeline.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class StasisPearlItem extends ArcanaPolymerItem {
      public StasisPearlItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!ArcanaItemUtils.isArcane(itemStack)) return ArcanaRegistry.getModelData(CHARGED_TXT).value();
         boolean active = getBooleanProperty(itemStack,ACTIVE_TAG);
         String pearlID = getStringProperty(itemStack,PEARL_ID_TAG);
         
         if(pearlID.isEmpty()){
            return getEnergy(itemStack) >= getMaxEnergy(itemStack) ? ArcanaRegistry.getModelData(CHARGED_TXT).value() : ArcanaRegistry.getModelData(COOLDOWN_TXT).value();
         }else{
            return active ? ArcanaRegistry.getModelData(STASIS_TXT).value() : ArcanaRegistry.getModelData(FLIGHT_TXT).value();
         }
      }
      
      @Override
      public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!ArcanaItemUtils.isArcane(itemStack)) return vanillaItem;
         boolean active = getBooleanProperty(itemStack,ACTIVE_TAG);
         
         return active ? Items.ENDER_EYE : vanillaItem;
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld serverWorld && entity instanceof ServerPlayerEntity player)) return;
         boolean active = getBooleanProperty(stack,ACTIVE_TAG);
         String pearlID = getStringProperty(stack,PEARL_ID_TAG);
         
         if(!pearlID.isEmpty()){
            Entity foundEntity = null;
            for(ServerWorld possibleWorlds : serverWorld.getServer().getWorlds()){
               Entity possibleEntity = possibleWorlds.getEntity(MiscUtils.getUUID(pearlID));
               if(foundEntity == null && possibleEntity != null){
                  foundEntity = possibleEntity;
               }
            }
            if(!(foundEntity instanceof StasisPearlEntity pearlEntity)){
               putProperty(stack,ACTIVE_TAG,false);
               putProperty(stack,PEARL_ID_TAG,"");
            }
         }
         
         if(world.getServer().getTicks() % 20 == 0){
            addEnergy(stack, 1); // Recharge
            buildItemLore(stack,world.getServer());
         }
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
         ItemStack stack = playerEntity.getStackInHand(hand);
         boolean active = getBooleanProperty(stack,ACTIVE_TAG);
         String pearlID = getStringProperty(stack,PEARL_ID_TAG);
         boolean canDelete = playerEntity.isSneaking() && Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SPATIAL_FOLD.id)) >= 1;
         
         try{
            if(pearlID.isEmpty()){ // Throw new pearl
               if(getEnergy(stack) >= getMaxEnergy(stack)){
                  SoundUtils.playSound(world,playerEntity.getBlockPos(),SoundEvents.ENTITY_ENDER_PEARL_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F);
                  playerEntity.getItemCooldownManager().set(this, 0);
                  if (!world.isClient) {
                     StasisPearlEntity stasisPearlEntity = new StasisPearlEntity(world, playerEntity, getUUID(stack), getCompoundProperty(stack,AUGMENTS_TAG));
                     stasisPearlEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0F, 1.5F, 1.0F);
                     world.spawnEntity(stasisPearlEntity);
                     String newPearlID = stasisPearlEntity.getUuidAsString();
                     putProperty(stack,PEARL_ID_TAG,newPearlID);
                     
                     setEnergy(stack,0);
                     PLAYER_DATA.get(playerEntity).addXP(250);
                  }
               }else{
                  playerEntity.getItemCooldownManager().set(this, 0);
                  if(playerEntity instanceof ServerPlayerEntity player){
                     playerEntity.sendMessage(Text.literal("Pearl Recharging: "+(getEnergy(stack)*100/getMaxEnergy(stack))+"%").formatted(Formatting.BLUE),true);
                     SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH,1,.5f);
                  }
               }
            }else if(canDelete){ // Delete current pearl
               if(world instanceof ServerWorld serverWorld){
                  Entity foundEntity = null;
                  for(ServerWorld possibleWorlds : serverWorld.getServer().getWorlds()){
                     Entity possibleEntity = possibleWorlds.getEntity(MiscUtils.getUUID(pearlID));
                     if(foundEntity == null && possibleEntity != null){
                        foundEntity = possibleEntity;
                     }
                  }
                  if(foundEntity != null) foundEntity.kill();
                  playerEntity.getItemCooldownManager().set(this, 0);
               }
               // Reset data
               putProperty(stack,ACTIVE_TAG,false);
               putProperty(stack,PEARL_ID_TAG,"");
               if(playerEntity instanceof ServerPlayerEntity player){
                  playerEntity.sendMessage(Text.literal("Pearl Cancelled").formatted(Formatting.BLUE),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1, .5f);
               }
            }else if(active){ // Un-stasis to pearl
               if(world instanceof ServerWorld serverWorld){
                  Entity foundEntity = null;
                  for(ServerWorld possibleWorlds : serverWorld.getServer().getWorlds()){
                     Entity possibleEntity = possibleWorlds.getEntity(MiscUtils.getUUID(pearlID));
                     if(foundEntity == null && possibleEntity != null){
                        foundEntity = possibleEntity;
                     }
                  }
                  if(foundEntity instanceof StasisPearlEntity pearlEntity){
                     pearlEntity.setStasis(false);
                     putProperty(stack,ACTIVE_TAG,false);
                     playerEntity.getItemCooldownManager().set(this, 0);
                     return TypedActionResult.success(stack);
                  }
               }
               
               // If this is reached, something went wrong and the pearl needs to be reset
               putProperty(stack,ACTIVE_TAG,false);
               putProperty(stack,PEARL_ID_TAG,"");
            }else{ // Stasis the pearl
               if(world instanceof ServerWorld serverWorld){
                  Entity foundEntity = null;
                  for(ServerWorld possibleWorlds : serverWorld.getServer().getWorlds()){
                     Entity possibleEntity = possibleWorlds.getEntity(MiscUtils.getUUID(pearlID));
                     if(foundEntity == null && possibleEntity != null){
                        foundEntity = possibleEntity;
                     }
                  }
                  if(foundEntity instanceof StasisPearlEntity pearlEntity){
                     pearlEntity.setStasis(true);
                     putProperty(stack,ACTIVE_TAG,true);
                     playerEntity.getItemCooldownManager().set(this, 0);
                     return TypedActionResult.success(stack);
                  }
               }
               // If this is reached, something went wrong and the pearl needs to be reset
               putProperty(stack,ACTIVE_TAG,false);
               putProperty(stack,PEARL_ID_TAG,"");
            }
         }catch(Exception e){
            e.printStackTrace();
         }
         return TypedActionResult.success(stack);
      }
   }
}

