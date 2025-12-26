package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;


public class MagnetismCharm extends ArcanaItem {
	public static final String ID = "magnetism_charm";
   
   public static final String FILTER_TAG = "filter";
   
   public MagnetismCharm(){
      id = ID;
      name = "Charm of Magnetism";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.CHARMS, TomeGui.TomeFilter.ITEMS};
      itemVersion = 2;
      vanillaItem = Items.IRON_INGOT;
      item = new MagnetismCharmItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_HEAVY_CORE,ResearchTasks.FISH_ITEM};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,MODE_TAG,0); // 0 off, 1 attract, 2 repel
      putProperty(stack,COOLDOWN_TAG,0);
      putProperty(stack,FILTER_TAG,new CompoundTag());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("You can feel the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("charm").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" tugging ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("on surrounding objects.").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" to ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("drag ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("nearby items to you.").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right Click").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" to toggle the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("magnetism ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("passively").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" to temporarily disable the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("passive").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(" pull").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      int cooldown = getIntProperty(stack,COOLDOWN_TAG);
      int mode = getIntProperty(stack,MODE_TAG);
      CompoundTag filter = getCompoundProperty(stack,FILTER_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,COOLDOWN_TAG,cooldown);
      putProperty(newStack,MODE_TAG,mode);
      putProperty(newStack,FILTER_TAG,filter);
      return buildItemLore(newStack,server);
   }
   
   public void activeUse(ServerPlayer player, Level world, ItemStack charm){
      int activeLength = 15 + 3*Math.max(0, ArcanaAugments.getAugmentOnItem(charm,ArcanaAugments.ELECTROMAGNET.id));;
      int activeRange = 3;
      int cooldown = getIntProperty(charm,COOLDOWN_TAG);
      if(cooldown != 0){
         return;
      }else{
         player.getCooldowns().addCooldown(charm,20);
         putProperty(charm,COOLDOWN_TAG,1);
      }
      
      Vec3 playerPos = player.getEyePosition();
      Vec3 view = player.getForward();
      Vec3 rayEnd = playerPos.add(view.scale(activeLength));
      
      AABB box = new AABB(playerPos,playerPos).inflate(activeLength+activeRange);
      List<ItemEntity> items = world.getEntities(EntityType.ITEM, box, (entity)->itemInRange(entity.position(),playerPos,rayEnd,activeRange) && canAffectItem(charm,entity.getItem().getItem()));
      SoundUtils.playSongToPlayer(player, SoundEvents.FOX_TELEPORT, 1,.9f);
      
      for(ItemEntity item : items){
         double x = playerPos.x() - item.getX();
         double y = playerPos.y() - item.getY();
         double z = playerPos.z() - item.getZ();
         double speed = .1;
         double heightMod = .08;
         item.setDeltaMovement(x * speed, y * speed + Math.sqrt(Math.sqrt(x * x + y * y + z * z)) * heightMod, z * speed);
      }
      ArcanaNovum.data(player).addXP(Math.min(ArcanaConfig.getInt(ArcanaRegistry.MAGNETISM_CHARM_CAP),ArcanaConfig.getInt(ArcanaRegistry.MAGNETISM_CHARM_PER_ITEM)*items.size())); // Add xp
      if(items.size() >= 25) ArcanaAchievements.grant(player,ArcanaAchievements.MAGNETS.id);
      
      if(ArcanaAugments.getAugmentOnItem(charm,ArcanaAugments.NEODYMIUM.id) >= 1){
         List<Entity> entities = world.getEntities(player, box, (entity)->itemInRange(entity.position(),playerPos,rayEnd,activeRange) && entity instanceof LivingEntity);
         for(Entity entity : entities){
            LivingEntity e = (LivingEntity) entity;
            if(e instanceof ServerPlayer hitPlayer){
               if(hitPlayer.isBlocking()){
                  hitPlayer.getCooldowns().addCooldown(hitPlayer.getItemBlockingWith(), 100);
                  hitPlayer.stopUsingItem();
                  hitPlayer.level().broadcastEntityEvent(hitPlayer, (byte)30);
               }
            }else{
               HashMap<EquipmentSlot, ItemStack> equipment = new HashMap<>();
               ItemStack head = e.getItemBySlot(EquipmentSlot.HEAD);
               ItemStack chest = e.getItemBySlot(EquipmentSlot.CHEST);
               ItemStack legs = e.getItemBySlot(EquipmentSlot.LEGS);
               ItemStack feet = e.getItemBySlot(EquipmentSlot.FEET);
               ItemStack hand1 = e.getItemBySlot(EquipmentSlot.MAINHAND);
               ItemStack hand2 = e.getItemBySlot(EquipmentSlot.OFFHAND);
               equipment.put(EquipmentSlot.HEAD,head);
               equipment.put(EquipmentSlot.CHEST,chest);
               equipment.put(EquipmentSlot.LEGS,legs);
               equipment.put(EquipmentSlot.FEET,feet);
               equipment.put(EquipmentSlot.MAINHAND,hand1);
               equipment.put(EquipmentSlot.OFFHAND,hand2);
               
               
               
               for(HashMap.Entry<EquipmentSlot, ItemStack> entry: equipment.entrySet()){
                  ItemStack item = entry.getValue();
                  if(item.is(ArcanaRegistry.NEODYMIUM_STEALABLE)){
                     ItemEntity droppedItem = e.spawnAtLocation(player.level(), item);
                     if(droppedItem != null){
                        double x = playerPos.x() - droppedItem.getX();
                        double y = playerPos.y() - droppedItem.getY();
                        double z = playerPos.z() - droppedItem.getZ();
                        double speed = .1;
                        double heightMod = .08;
                        droppedItem.setDeltaMovement(x * speed, y * speed + Math.sqrt(Math.sqrt(x * x + y * y + z * z)) * heightMod, z * speed);
                     }
                     e.setItemSlot(entry.getKey(), ItemStack.EMPTY);
                  }
               }
            }
         }
      }
   }
   
   private boolean itemInRange(Vec3 itemPos, Vec3 start, Vec3 end, int activeRange){
      double dist = itemPos.subtract(start).cross(end.subtract(start)).length() / end.subtract(start).length();
      return dist <= activeRange;
   }
   
   public void toggleMode(ServerPlayer player, ItemStack item){
      boolean canRepel = ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.POLARITY_REVERSAL.id) >= 1;
      int mode = (getIntProperty(item,MODE_TAG)+1) % (canRepel ? 3 : 2);
      putProperty(item,MODE_TAG,mode);
      if(mode == 1){
         player.displayClientMessage(Component.literal("The Charm's Pull Strengthens").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.ANVIL_LAND, 1,2f);
      }else if(mode == 2){
         player.displayClientMessage(Component.literal("The Charm's Pull Reverses").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.ANVIL_LAND, 1,1f);
      }else{
         player.displayClientMessage(Component.literal("The Charm's Pull Weakens").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.ANVIL_LAND, .3f,.5f);
      }
   }
   
   public boolean canAffectItem(ItemStack magnet, Item filterItem){
      CompoundTag filter = getCompoundProperty(magnet,FILTER_TAG);
      String itemId = BuiltInRegistries.ITEM.getKey(filterItem).toString();
      
      boolean hasWhitelist = filter.keySet().stream().anyMatch(s -> filter.getIntOr(s, 0) == 1);
      int status = filter.getIntOr(itemId, 0);
      return (hasWhitelist && status == 1) || (!hasWhitelist && status != 2); // Allow if item is in whitelist, or item isn't blacklisted if no whitelist exist
   }
   
   public void toggleFilterItem(ServerPlayer player, ItemStack magnet, Item filterItem){
      CompoundTag filter = getCompoundProperty(magnet,FILTER_TAG);
      String itemId = BuiltInRegistries.ITEM.getKey(filterItem).toString();
      
      int itemStatus = 0; // 0 = nothing, 1 = whitelist, 2 = blacklist
      if(filter.contains(itemId)){
         itemStatus = filter.getIntOr(itemId, 0);
      }
      itemStatus = (itemStatus+1) % 3;
      
      if(itemStatus == 0){
         filter.remove(itemId);
         player.displayClientMessage(Component.literal("")
               .append(Component.literal("Removed ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
               .append(Component.translatable(filterItem.getDescriptionId()).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))
               .append(Component.literal(" from the filter").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),true);
      }else if(itemStatus == 1){
         filter.putInt(itemId, itemStatus);
         player.displayClientMessage(Component.literal("")
               .append(Component.literal("Whitelisted ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
               .append(Component.translatable(filterItem.getDescriptionId()).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))
               .append(Component.literal(" in the filter").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),true);
      }else if(itemStatus == 2){
         filter.putInt(itemId, itemStatus);
         player.displayClientMessage(Component.literal("")
               .append(Component.literal("Blacklisted ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
               .append(Component.translatable(filterItem.getDescriptionId()).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))
               .append(Component.literal(" from the filter").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),true);
      }
      putProperty(magnet,FILTER_TAG,filter);
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.LIGHTNING_ROD,16);
      ArcanaIngredient b = new ArcanaIngredient(Items.IRON_INGOT,16);
      ArcanaIngredient c = new ArcanaIngredient(Items.IRON_BARS,16);
      ArcanaIngredient h = new ArcanaIngredient(Items.IRON_BLOCK,8);
      ArcanaIngredient m = new ArcanaIngredient(Items.HEAVY_CORE,1, true);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,c,h,c,b},
            {c,h,m,h,c},
            {b,c,h,c,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("      Charm of\n      Magnetism").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nMagnets, how do they work? Well, they pull stuff sometimes… Unfortunately, they only work on some materials, and with limited range. I believe this Heavy Core that I have found presents  ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("      Charm of\n      Magnetism").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\ngravitic properties that can supercharge a magnet’s abilities. Surrounding the Core in iron and striking it with lightning should leave an empowered permanent magnet.\n\nSneak Using the Charm toggles it passively ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("      Charm of\n      Magnetism").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\npulling items around me.\n\nUsing the charm directs a magnetic field a greater distance in the direction of my gaze that pulls items towards me. ").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class MagnetismCharmItem extends ArcanaPolymerItem {
      public MagnetismCharmItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         int mode = getIntProperty(itemStack,MODE_TAG);
         boolean neo = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.NEODYMIUM.id) >= 1;
         
         List<String> stringList = new ArrayList<>();
         if(neo){
            if(mode == 2){
               stringList.add("neo_reverse");
            }else if(mode == 1){
               stringList.add("neo_on");
            }else{
               stringList.add("neo_off");
            }
         }else{
            if(mode == 2){
               stringList.add("reverse");
            }else if(mode == 1){
               stringList.add("on");
            }else{
               stringList.add("off");
            }
         }
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerLevel && entity instanceof ServerPlayer player)) return;
         int passiveRange = 5 + Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.FERRITE_CORE.id));
         int cooldown = getIntProperty(stack,COOLDOWN_TAG);
         
         if(!player.isShiftKeyDown()){
            int mode = getIntProperty(stack,MODE_TAG);
            
            if(mode > 0 && world.getServer().getTickCount() % 10 == 0){
               Vec3 playerPos = player.getEyePosition();
               
               AABB box = new AABB(playerPos,playerPos).inflate(passiveRange);
               List<ItemEntity> items = world.getEntities(EntityType.ITEM, box, (e) -> canAffectItem(stack,e.getItem().getItem()));
               Collections.shuffle(items);
               
               int i = 0;
               for(ItemEntity item : items){
                  double x = playerPos.x() - item.getX();
                  double y = playerPos.y() - item.getY();
                  double z = playerPos.z() - item.getZ();
                  double speed = .06;
                  double heightMod = .04;
                  if(mode == 2){ // Repel items
                     x = -x;
                     z = -z;
                  }
                  item.setDeltaMovement(x * speed, y * speed + Math.sqrt(Math.sqrt(x * x + y * y + z * z)) * heightMod, z * speed);
                  if(i >= 25){
                     break;
                  }else{
                     i++;
                  }
               }
            }
         }
         
         if(world.getServer().getTickCount() % 20 == 0){
            if(cooldown > 0) putProperty(stack,COOLDOWN_TAG,cooldown-1);
         }
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
         boolean canFilter = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.FARADAY_CAGE.id) >= 1;
         ItemStack offHand = playerEntity.getItemInHand(InteractionHand.OFF_HAND);
         
         if(canFilter && hand == InteractionHand.OFF_HAND && playerEntity.isShiftKeyDown()){
            putProperty(stack,FILTER_TAG,new CompoundTag());
            player.displayClientMessage(Component.literal("Filter Cleared").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),true);
            SoundUtils.playSongToPlayer(player, SoundEvents.ANVIL_LAND, 0.5f,1f);
         }else if(canFilter && hand == InteractionHand.MAIN_HAND && !offHand.isEmpty() && playerEntity.isShiftKeyDown()){
            toggleFilterItem(player,stack,offHand.getItem());
         }else if(playerEntity.isShiftKeyDown()){
            toggleMode((ServerPlayer) playerEntity,stack);
         }else{
            activeUse((ServerPlayer) playerEntity, world, stack);
         }
         return InteractionResult.SUCCESS_SERVER;
      }
   }
}

