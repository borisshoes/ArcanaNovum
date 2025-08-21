package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.GRAY);
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_HEAVY_CORE,ResearchTasks.FISH_ITEM};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,MODE_TAG,0); // 0 off, 1 attract, 2 repel
      putProperty(stack,COOLDOWN_TAG,0);
      putProperty(stack,FILTER_TAG,new NbtCompound());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("You can feel the ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("charm").formatted(Formatting.GRAY))
            .append(Text.literal(" tugging ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("on surrounding objects.").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" to ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("drag ").formatted(Formatting.GRAY))
            .append(Text.literal("nearby items to you.").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Sneak Right Click").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" to toggle the ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("magnetism ").formatted(Formatting.GRAY))
            .append(Text.literal("passively").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Sneak").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" to temporarily disable the ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("passive").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(" pull").formatted(Formatting.GRAY))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      int cooldown = getIntProperty(stack,COOLDOWN_TAG);
      int mode = getIntProperty(stack,MODE_TAG);
      NbtCompound filter = getCompoundProperty(stack,FILTER_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,COOLDOWN_TAG,cooldown);
      putProperty(newStack,MODE_TAG,mode);
      putProperty(newStack,FILTER_TAG,filter);
      return buildItemLore(newStack,server);
   }
   
   public void activeUse(ServerPlayerEntity player, World world, ItemStack charm){
      int activeLength = 15 + 3*Math.max(0, ArcanaAugments.getAugmentOnItem(charm,ArcanaAugments.ELECTROMAGNET.id));;
      int activeRange = 3;
      int cooldown = getIntProperty(charm,COOLDOWN_TAG);
      if(cooldown != 0){
         return;
      }else{
         player.getItemCooldownManager().set(charm,20);
         putProperty(charm,COOLDOWN_TAG,1);
      }
      
      Vec3d playerPos = player.getEyePos();
      Vec3d view = player.getRotationVecClient();
      Vec3d rayEnd = playerPos.add(view.multiply(activeLength));
      
      Box box = new Box(playerPos,playerPos).expand(activeLength+activeRange);
      List<ItemEntity> items = world.getEntitiesByType(EntityType.ITEM, box, (entity)->itemInRange(entity.getPos(),playerPos,rayEnd,activeRange) && canAffectItem(charm,entity.getStack().getItem()));
      SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_FOX_TELEPORT, 1,.9f);
      
      for(ItemEntity item : items){
         double x = playerPos.getX() - item.getX();
         double y = playerPos.getY() - item.getY();
         double z = playerPos.getZ() - item.getZ();
         double speed = .1;
         double heightMod = .08;
         item.setVelocity(x * speed, y * speed + Math.sqrt(Math.sqrt(x * x + y * y + z * z)) * heightMod, z * speed);
      }
      ArcanaNovum.data(player).addXP(Math.min(ArcanaConfig.getInt(ArcanaRegistry.MAGNETISM_CHARM_CAP),ArcanaConfig.getInt(ArcanaRegistry.MAGNETISM_CHARM_PER_ITEM)*items.size())); // Add xp
      if(items.size() >= 25) ArcanaAchievements.grant(player,ArcanaAchievements.MAGNETS.id);
      
      if(ArcanaAugments.getAugmentOnItem(charm,ArcanaAugments.NEODYMIUM.id) >= 1){
         List<Entity> entities = world.getOtherEntities(player, box, (entity)->itemInRange(entity.getPos(),playerPos,rayEnd,activeRange) && entity instanceof LivingEntity);
         for(Entity entity : entities){
            LivingEntity e = (LivingEntity) entity;
            if(e instanceof ServerPlayerEntity hitPlayer){
               if(hitPlayer.isBlocking()){
                  hitPlayer.getItemCooldownManager().set(hitPlayer.getBlockingItem(), 100);
                  hitPlayer.clearActiveItem();
                  hitPlayer.getWorld().sendEntityStatus(hitPlayer, (byte)30);
               }
            }else{
               HashMap<EquipmentSlot,ItemStack> equipment = new HashMap<>();
               ItemStack head = e.getEquippedStack(EquipmentSlot.HEAD);
               ItemStack chest = e.getEquippedStack(EquipmentSlot.CHEST);
               ItemStack legs = e.getEquippedStack(EquipmentSlot.LEGS);
               ItemStack feet = e.getEquippedStack(EquipmentSlot.FEET);
               ItemStack hand1 = e.getEquippedStack(EquipmentSlot.MAINHAND);
               ItemStack hand2 = e.getEquippedStack(EquipmentSlot.OFFHAND);
               equipment.put(EquipmentSlot.HEAD,head);
               equipment.put(EquipmentSlot.CHEST,chest);
               equipment.put(EquipmentSlot.LEGS,legs);
               equipment.put(EquipmentSlot.FEET,feet);
               equipment.put(EquipmentSlot.MAINHAND,hand1);
               equipment.put(EquipmentSlot.OFFHAND,hand2);
               
               
               
               for(HashMap.Entry<EquipmentSlot,ItemStack> entry: equipment.entrySet()){
                  ItemStack item = entry.getValue();
                  if(item.isIn(ArcanaRegistry.NEODYMIUM_STEALABLE)){
                     ItemEntity droppedItem = e.dropStack(player.getServerWorld(), item);
                     if(droppedItem != null){
                        double x = playerPos.getX() - droppedItem.getX();
                        double y = playerPos.getY() - droppedItem.getY();
                        double z = playerPos.getZ() - droppedItem.getZ();
                        double speed = .1;
                        double heightMod = .08;
                        droppedItem.setVelocity(x * speed, y * speed + Math.sqrt(Math.sqrt(x * x + y * y + z * z)) * heightMod, z * speed);
                     }
                     e.equipStack(entry.getKey(),ItemStack.EMPTY);
                  }
               }
            }
         }
      }
   }
   
   private boolean itemInRange(Vec3d itemPos, Vec3d start, Vec3d end, int activeRange){
      double dist = itemPos.subtract(start).crossProduct(end.subtract(start)).length() / end.subtract(start).length();
      return dist <= activeRange;
   }
   
   public void toggleMode(ServerPlayerEntity player, ItemStack item){
      boolean canRepel = ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.POLARITY_REVERSAL.id) >= 1;
      int mode = (getIntProperty(item,MODE_TAG)+1) % (canRepel ? 3 : 2);
      putProperty(item,MODE_TAG,mode);
      if(mode == 1){
         player.sendMessage(Text.literal("The Charm's Pull Strengthens").formatted(Formatting.GRAY,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_ANVIL_LAND, 1,2f);
      }else if(mode == 2){
         player.sendMessage(Text.literal("The Charm's Pull Reverses").formatted(Formatting.GRAY,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_ANVIL_LAND, 1,1f);
      }else{
         player.sendMessage(Text.literal("The Charm's Pull Weakens").formatted(Formatting.GRAY,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_ANVIL_LAND, .3f,.5f);
      }
   }
   
   public boolean canAffectItem(ItemStack magnet, Item filterItem){
      NbtCompound filter = getCompoundProperty(magnet,FILTER_TAG);
      String itemId = Registries.ITEM.getId(filterItem).toString();
      
      boolean hasWhitelist = filter.getKeys().stream().anyMatch(s -> filter.getInt(s, 0) == 1);
      int status = filter.getInt(itemId, 0);
      return (hasWhitelist && status == 1) || (!hasWhitelist && status != 2); // Allow if item is in whitelist, or item isn't blacklisted if no whitelist exist
   }
   
   public void toggleFilterItem(ServerPlayerEntity player, ItemStack magnet, Item filterItem){
      NbtCompound filter = getCompoundProperty(magnet,FILTER_TAG);
      String itemId = Registries.ITEM.getId(filterItem).toString();
      
      int itemStatus = 0; // 0 = nothing, 1 = whitelist, 2 = blacklist
      if(filter.contains(itemId)){
         itemStatus = filter.getInt(itemId, 0);
      }
      itemStatus = (itemStatus+1) % 3;
      
      if(itemStatus == 0){
         filter.remove(itemId);
         player.sendMessage(Text.literal("")
               .append(Text.literal("Removed ").formatted(Formatting.GRAY,Formatting.ITALIC))
               .append(Text.translatable(filterItem.getTranslationKey()).formatted(Formatting.DARK_GRAY,Formatting.ITALIC))
               .append(Text.literal(" from the filter").formatted(Formatting.GRAY,Formatting.ITALIC)),true);
      }else if(itemStatus == 1){
         filter.putInt(itemId, itemStatus);
         player.sendMessage(Text.literal("")
               .append(Text.literal("Whitelisted ").formatted(Formatting.GRAY,Formatting.ITALIC))
               .append(Text.translatable(filterItem.getTranslationKey()).formatted(Formatting.DARK_GRAY,Formatting.ITALIC))
               .append(Text.literal(" in the filter").formatted(Formatting.GRAY,Formatting.ITALIC)),true);
      }else if(itemStatus == 2){
         filter.putInt(itemId, itemStatus);
         player.sendMessage(Text.literal("")
               .append(Text.literal("Blacklisted ").formatted(Formatting.GRAY,Formatting.ITALIC))
               .append(Text.translatable(filterItem.getTranslationKey()).formatted(Formatting.DARK_GRAY,Formatting.ITALIC))
               .append(Text.literal(" from the filter").formatted(Formatting.GRAY,Formatting.ITALIC)),true);
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
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("      Charm of\n      Magnetism").formatted(Formatting.GRAY,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nMagnets, how do they work? Well, they pull stuff sometimes… Unfortunately, they only work on some materials, and with limited range. I believe this Heavy Core that I have found presents  ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("      Charm of\n      Magnetism").formatted(Formatting.GRAY,Formatting.BOLD),Text.literal("\ngravitic properties that can supercharge a magnet’s abilities. Surrounding the Core in iron and striking it with lightning should leave an empowered permanent magnet.\n\nSneak Using the Charm toggles it passively ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("      Charm of\n      Magnetism").formatted(Formatting.GRAY,Formatting.BOLD),Text.literal("\npulling items around me.\n\nUsing the charm directs a magnetic field a greater distance in the direction of my gaze that pulls items towards me. ").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class MagnetismCharmItem extends ArcanaPolymerItem {
      public MagnetismCharmItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context){
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
         baseStack.set(DataComponentTypes.CUSTOM_MODEL_DATA,new CustomModelDataComponent(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         int passiveRange = 5 + Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.FERRITE_CORE.id));
         int cooldown = getIntProperty(stack,COOLDOWN_TAG);
         
         if(!player.isSneaking()){
            int mode = getIntProperty(stack,MODE_TAG);
            
            if(mode > 0 && world.getServer().getTicks() % 10 == 0){
               Vec3d playerPos = player.getEyePos();
               
               Box box = new Box(playerPos,playerPos).expand(passiveRange);
               List<ItemEntity> items = world.getEntitiesByType(EntityType.ITEM, box, (e) -> canAffectItem(stack,e.getStack().getItem()));
               Collections.shuffle(items);
               
               int i = 0;
               for(ItemEntity item : items){
                  double x = playerPos.getX() - item.getX();
                  double y = playerPos.getY() - item.getY();
                  double z = playerPos.getZ() - item.getZ();
                  double speed = .06;
                  double heightMod = .04;
                  if(mode == 2){ // Repel items
                     x = -x;
                     z = -z;
                  }
                  item.setVelocity(x * speed, y * speed + Math.sqrt(Math.sqrt(x * x + y * y + z * z)) * heightMod, z * speed);
                  if(i >= 25){
                     break;
                  }else{
                     i++;
                  }
               }
            }
         }
         
         if(world.getServer().getTicks() % 20 == 0){
            if(cooldown > 0) putProperty(stack,COOLDOWN_TAG,cooldown-1);
         }
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
         ItemStack stack = playerEntity.getStackInHand(hand);
         if(!(playerEntity instanceof ServerPlayerEntity player)) return ActionResult.PASS;
         boolean canFilter = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.FARADAY_CAGE.id) >= 1;
         ItemStack offHand = playerEntity.getStackInHand(Hand.OFF_HAND);
         
         if(canFilter && hand == Hand.OFF_HAND && playerEntity.isSneaking()){
            putProperty(stack,FILTER_TAG,new NbtCompound());
            player.sendMessage(Text.literal("Filter Cleared").formatted(Formatting.GRAY,Formatting.ITALIC),true);
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_ANVIL_LAND, 0.5f,1f);
         }else if(canFilter && hand == Hand.MAIN_HAND && !offHand.isEmpty() && playerEntity.isSneaking()){
            toggleFilterItem(player,stack,offHand.getItem());
         }else if(playerEntity.isSneaking()){
            toggleMode((ServerPlayerEntity) playerEntity,stack);
         }else{
            activeUse((ServerPlayerEntity) playerEntity, world, stack);
         }
         return ActionResult.SUCCESS_SERVER;
      }
   }
}

