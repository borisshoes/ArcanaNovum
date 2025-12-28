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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.*;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.minecraft.world.level.block.LightBlock.LEVEL;

public class LightCharm extends ArcanaItem {
	public static final String ID = "light_charm";
   
   public static final String VISION_TAG = "vision";
   public static final String BRIGHTNESS_TAG = "brightness";
   public static final String THRESHOLD_TAG = "threshold";
   public static final String NOVA_TAG = "novaCD";
   
   public LightCharm(){
      id = ID;
      name = "Charm of Light";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.CHARMS, TomeGui.TomeFilter.ITEMS};
      itemVersion = 1;
      vanillaItem = Items.SUNFLOWER;
      item = new LightCharmItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW);
      researchTasks = new ResourceKey[]{ResearchTasks.EFFECT_NIGHT_VISION, ResearchTasks.PLACE_TORCHES, ResearchTasks.ADVANCEMENT_CREATE_FULL_BEACON};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,VISION_TAG, false);
      putProperty(stack,ACTIVE_TAG, false);
      putProperty(stack,BRIGHTNESS_TAG, 11);
      putProperty(stack,MODE_TAG, 0); // 0 is on/off, 1 is vision, 2 is threshold, 3 is brightness, 4 is nova, 5 is manual
      putProperty(stack,THRESHOLD_TAG, 1);
      putProperty(stack,NOVA_TAG, 0);
      setPrefStack(stack);
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("The charm ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("radiates").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" a warm glow.").withStyle(ChatFormatting.GOLD)));
      lore.add(Component.literal("")
            .append(Component.literal("Its light seems to ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("linger").withStyle(ChatFormatting.RED))
            .append(Component.literal(" behind you.").withStyle(ChatFormatting.GOLD)));
      lore.add(Component.literal("")
            .append(Component.literal("Right click").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" to adjust the ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("setting").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.GOLD)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Right click").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" to toggle the charm ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("mode").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.GOLD)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   public void changeSetting(ServerPlayer player, ItemStack item){
      boolean vision = getBooleanProperty(item,VISION_TAG);
      boolean active = getBooleanProperty(item,ACTIVE_TAG);
      int mode = getIntProperty(item,MODE_TAG); // 0 is on/off, 1 is vision, 2 is threshold, 3 is brightness, 4 is nova, 5 is manual
      int threshold = getIntProperty(item,THRESHOLD_TAG);
      int brightness = getIntProperty(item,BRIGHTNESS_TAG);
      int novaCD = getIntProperty(item,NOVA_TAG);
      
      switch(mode){
         case 0:
            active = !active;
            putProperty(item,ACTIVE_TAG, active);
            if(active){
               player.displayClientMessage(Component.literal("The Charm's Light Brightens").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BEACON_ACTIVATE, 1,2f);
            }else{
               player.displayClientMessage(Component.literal("The Charm's Light Dims").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BEACON_DEACTIVATE, 1,.5f);
            }
            break;
         case 1:
            vision = !vision;
            putProperty(item,VISION_TAG, vision);
            if(vision){
               player.displayClientMessage(Component.literal("You can now see the arcane lights").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BEACON_ACTIVATE, 1,2f);
            }else{
               player.displayClientMessage(Component.literal("You can no longer see the arcane lights").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BEACON_DEACTIVATE, 1,.5f);
            }
            break;
         case 2:
            threshold = (threshold+1) % 16;
            player.displayClientMessage(Component.literal("Light Threshold: "+threshold).withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC),true);
            putProperty(item,THRESHOLD_TAG, threshold);
            break;
         case 3:
            brightness = (brightness+1) % 16;
            player.displayClientMessage(Component.literal("Light Brightness: "+brightness).withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC),true);
            putProperty(item,BRIGHTNESS_TAG, brightness);
            break;
         case 4:
            if(novaCD == 0){
               player.displayClientMessage(Component.literal("The Charm's Light Flares!").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC),true);
               nova(player,item);
               putProperty(item,NOVA_TAG, 30);
            }else{
               player.displayClientMessage(Component.literal("Radiant Nova Cooldown: "+novaCD+" seconds").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC),true);
            }
            break;
         case 5:
            break;
      }
   }
   
   private void nova(ServerPlayer player, ItemStack stack){
      ServerLevel world = player.level();
      BlockPos center = player.blockPosition();
      int range = 32;
      int threshold = 3;
      BlockPos max = center.offset(range,10,range);
      BlockPos min = center.offset(-range,-3*range,-range);
      
      HashSet<BlockPos> possiblePositions = new HashSet<>();
      
      for(int x = min.getX(); x <= max.getX(); x++){
         for(int y = min.getY(); y <= max.getY(); y++){
            for(int z = min.getZ(); z <= max.getZ(); z++){
               BlockPos pos = new BlockPos(x, y, z);
               
               if(world.isInWorldBounds(pos)){
                  BlockState state = world.getBlockState(pos);
                  if(state.isAir() && world.getBrightness(LightLayer.BLOCK,pos) <= threshold){
                     possiblePositions.add(pos);
                  }
               }
            }
         }
      }
      
      List<Tuple<BlockPos, Integer>> pairTree = new ArrayList<>();
      int limit = threshold == 15 ? 1 : (15-threshold);
      
      while(!possiblePositions.isEmpty()){
         BlockPos root = possiblePositions.iterator().next();
         iterativeNovaSearch(limit, new Tuple<>(root,0),possiblePositions,pairTree);
      }
      
      
      int placedCount = 0;
      for(Tuple<BlockPos, Integer> pair : pairTree){
         if(pair.getB() == 0){
            world.setBlock(pair.getA(), Blocks.LIGHT.defaultBlockState().setValue(LEVEL,15), Block.UPDATE_ALL);
            world.gameEvent(player, GameEvent.BLOCK_PLACE, pair.getA());
            
            placedCount++;
         }
      }
      
      ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.LIGHT_CHARM_NOVA_PER_LIGHT)*placedCount); // Add xp
      SoundUtils.playSongToPlayer(player, SoundEvents.FIRECHARGE_USE, 1f,0.5f);
   }
   
   // TODO maybe make this async in the future?
   private void iterativeNovaSearch(int limit, Tuple<BlockPos, Integer> startPair, Set<BlockPos> posSet, List<Tuple<BlockPos, Integer>> pairTree){
      Queue<Tuple<BlockPos, Integer>> queue = new LinkedList<>();
      queue.add(startPair);
      
      Vec3i[] diagonals = new Vec3i[]{
            new Vec3i(1,1,0),
            new Vec3i(-1,1,0),
            new Vec3i(0,1,1),
            new Vec3i(0,1,-1),
            new Vec3i(1,-1,0),
            new Vec3i(-1,-1,0),
            new Vec3i(0,-1,1),
            new Vec3i(0,-1,-1),
            new Vec3i(1,0,1),
            new Vec3i(-1,0,1),
            new Vec3i(1,0,-1),
            new Vec3i(-1,0,-1),
      };
      
      while(!queue.isEmpty()){
         Tuple<BlockPos, Integer> pair = queue.poll();
         BlockPos pos = pair.getA();
         
         if(posSet.remove(pos)){
            pairTree.add(pair);
            
            for(Direction direction : Direction.values()){
               BlockPos offset = pos.relative(direction);
               if(posSet.contains(offset) && pair.getB() + 1 <= limit){
                  queue.add(new Tuple<>(offset, pair.getB() + 1));
               }
            }
            
            for(Vec3i diagonal : diagonals){
               BlockPos offset = pos.mutable().offset(diagonal);
               if(posSet.contains(offset) && pair.getB() + 2 <= limit){
                  queue.add(new Tuple<>(offset, pair.getB() + 2));
               }
            }
         }
      }
   }
   
   public void selectMode(ServerPlayer player, ItemStack item){
      boolean vision = getBooleanProperty(item,VISION_TAG);
      boolean active = getBooleanProperty(item,ACTIVE_TAG);
      int mode = getIntProperty(item,MODE_TAG); // 0 is on/off, 1 is vision, 2 is threshold, 3 is brightness, 4 is nova, 5 is manual
      int threshold = getIntProperty(item,THRESHOLD_TAG);
      int brightness = getIntProperty(item,BRIGHTNESS_TAG);
      int novaCD = getIntProperty(item,NOVA_TAG);
      
      boolean hasThresh = ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.MOOD_LIGHTING.id) >= 1;
      boolean hasBright = ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.DIMMER_SWITCH.id) >= 1;
      boolean hasNova = ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.RADIANCE.id) >= 1;
      boolean hasManual = ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.SELECTIVE_PLACEMENT.id) >= 1;
      
      ArrayList<Integer> possibleModes = new ArrayList<>();
      possibleModes.add(0);
      possibleModes.add(1);
      if(hasThresh) possibleModes.add(2);
      if(hasBright) possibleModes.add(3);
      if(hasNova) possibleModes.add(4);
      if(hasManual) possibleModes.add(5);
      
      int curInd = possibleModes.indexOf(mode);
      int newInd = (curInd+1) % possibleModes.size();
      
      mode = possibleModes.get(newInd);
      switch(mode){
         case 0:
            player.displayClientMessage(Component.literal("Mode: Toggle Light Placement").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC),true);
            break;
         case 1:
            player.displayClientMessage(Component.literal("Mode: Toggle Light Visibility").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC),true);
            break;
         case 2:
            player.displayClientMessage(Component.literal("Mode: Threshold Selection").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC),true);
            break;
         case 3:
            player.displayClientMessage(Component.literal("Mode: Brightness Selection").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC),true);
            break;
         case 4:
            player.displayClientMessage(Component.literal("Mode: Radiant Nova").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC),true);
            break;
         case 5:
            player.displayClientMessage(Component.literal("Mode: Manual Placement").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC),true);
            break;
      }
      putProperty(item,MODE_TAG, mode);
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("   Charm of Light").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nI’ve placed too many torches, surely there is an alternative beyond risking the darkness. A beacon is a solid place to start.\n\nAfter combining some other light sources and a few potions, I ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Charm of Light").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nhave a renewable light source that leaves lingering, invisible, arcane lights behind in the darkness.\n\nThe potions in the Charm allow me to see the lights when needed, in case I wish to remove them.\n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Charm of Light").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nUsing the Charm switches its mode.\n\nSneak Using toggles the selected mode.\n\nThe Charm can be toggled to stop leaving lights, or to make the lights become visible.\n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("   Charm of Light").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nThe lights are immovable by pistons, and can be removed by placing a block in them.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.POTION,1).withPotions(Potions.LONG_NIGHT_VISION);
      ArcanaIngredient b = new ArcanaIngredient(Items.TORCH,32);
      ArcanaIngredient c = new ArcanaIngredient(Items.VERDANT_FROGLIGHT,16);
      ArcanaIngredient d = new ArcanaIngredient(Items.SOUL_LANTERN,32);
      ArcanaIngredient f = new ArcanaIngredient(Items.REDSTONE_LAMP,16);
      ArcanaIngredient g = new ArcanaIngredient(Items.SEA_LANTERN,8);
      ArcanaIngredient h = new ArcanaIngredient(Items.GLOWSTONE,8);
      ArcanaIngredient j = new ArcanaIngredient(Items.CANDLE,16);
      ArcanaIngredient k = new ArcanaIngredient(Items.PEARLESCENT_FROGLIGHT,16);
      ArcanaIngredient m = new ArcanaIngredient(Items.BEACON,1, true);
      ArcanaIngredient o = new ArcanaIngredient(Items.SHROOMLIGHT,16);
      ArcanaIngredient p = new ArcanaIngredient(Items.JACK_O_LANTERN,16);
      ArcanaIngredient t = new ArcanaIngredient(Items.COPPER_BULB,16);
      ArcanaIngredient v = new ArcanaIngredient(Items.LANTERN,32);
      ArcanaIngredient w = new ArcanaIngredient(Items.OCHRE_FROGLIGHT,16);
      ArcanaIngredient x = new ArcanaIngredient(Items.SOUL_TORCH,32);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,d,a},
            {f,g,h,g,j},
            {k,h,m,h,o},
            {p,g,h,g,t},
            {a,v,w,x,a}};
      return new ArcanaRecipe(this, ingredients,new ForgeRequirement());
   }
   
   public class LightCharmItem extends ArcanaPolymerItem {
      public LightCharmItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         boolean active = getBooleanProperty(itemStack,ACTIVE_TAG);
         
         List<String> stringList = new ArrayList<>();
         if(active){
            stringList.add("on");
         }else{
            stringList.add("off");
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
         if(!(world instanceof ServerLevel serverWorld && entity instanceof ServerPlayer player)) return;
         boolean vision = getBooleanProperty(stack,VISION_TAG);
         boolean active = getBooleanProperty(stack,ACTIVE_TAG);
         int threshold = getIntProperty(stack,THRESHOLD_TAG);
         int brightness = getIntProperty(stack,BRIGHTNESS_TAG);
         int novaCD = getIntProperty(stack,NOVA_TAG);
         
         if(world.getServer().getTickCount() % 60 == 0){
            BlockPos pos = player.blockPosition();
            if(active){
               if(world.getMaxLocalRawBrightness(pos) <= threshold && world.getBlockState(pos).isAir()){
                  world.setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(LEVEL,brightness), Block.UPDATE_ALL);
                  world.gameEvent(player, GameEvent.BLOCK_PLACE, pos);
                  SoundUtils.playSongToPlayer(player, SoundEvents.RESPAWN_ANCHOR_CHARGE, .3f,2f);
                  ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.LIGHT_CHARM_AUTOMATIC)); // Add xp
                  ArcanaAchievements.progress(player,ArcanaAchievements.ENLIGHTENED.id,1);
               }
            }
            
         }
         
         if(world.getServer().getTickCount() % 20 == 0){
            if(novaCD > 0) putProperty(stack,NOVA_TAG, novaCD-1);
            BlockPos pos = player.blockPosition();
            if(vision){
               // Search 10x10x10 area around player for light blocks
               for(BlockPos block : BlockPos.withinManhattan(pos, 10, 10, 10)){
                  BlockState state = world.getBlockState(block);
                  if(state.getBlock().equals(Blocks.LIGHT)){
                     serverWorld.sendParticles(player, new BlockParticleOption(ParticleTypes.BLOCK_MARKER, state), true,true, block.getX()+.5,block.getY()+.5,block.getZ()+.5, 1,0,0,0,0);
                  }
               }
            }
         }
      }
      
      @Override
      public InteractionResult useOn(UseOnContext context){
         Player playerEntity = context.getPlayer();
         Level world = context.getLevel();
         InteractionHand hand = context.getHand();
         ItemStack stack = context.getItemInHand();
         if(playerEntity == null) return InteractionResult.PASS;
         
         int mode = getIntProperty(stack,MODE_TAG); // 0 is on/off, 1 is vision, 2 is threshold, 3 is brightness, 4 is nova, 5 is manual
         int brightness = getIntProperty(stack,BRIGHTNESS_TAG);
         Direction side = context.getClickedFace();
         BlockPos pos = context.getClickedPos().offset(side.getUnitVec3i());
         boolean placeable = world.getBlockState(pos).canBeReplaced(new BlockPlaceContext(playerEntity, hand, new ItemStack(Items.LIGHT), new BlockHitResult(context.getClickLocation(),context.getClickedFace(),context.getClickedPos(),context.isInside())));
         if(mode == 5 && playerEntity instanceof ServerPlayer player && placeable){
            world.setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(LEVEL,brightness), Block.UPDATE_ALL);
            world.gameEvent(player, GameEvent.BLOCK_PLACE, pos);
            SoundUtils.playSongToPlayer(player, SoundEvents.RESPAWN_ANCHOR_CHARGE, .3f,2f);
            ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.LIGHT_CHARM_MANUAL)); // Add xp
            ArcanaAchievements.progress(player,ArcanaAchievements.ENLIGHTENED.id,1);
            return InteractionResult.SUCCESS_SERVER;
         }
         return InteractionResult.PASS;
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         if(playerEntity.isShiftKeyDown()){
            selectMode((ServerPlayer) playerEntity,playerEntity.getItemInHand(hand));
         }else{
            changeSetting((ServerPlayer) playerEntity,playerEntity.getItemInHand(hand));
         }
         
         return InteractionResult.SUCCESS_SERVER;
      }
   }
}

