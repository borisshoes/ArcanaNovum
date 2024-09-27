package net.borisshoes.arcananovum.items.charms;

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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.minecraft.block.LightBlock.LEVEL_15;

public class LightCharm extends ArcanaItem {
	public static final String ID = "light_charm";
   
   public static final String VISION_TAG = "vision";
   public static final String BRIGHTNESS_TAG = "brightness";
   public static final String THRESHOLD_TAG = "threshold";
   public static final String NOVA_TAG = "novaCD";
   
   private static final String ON_TXT = "item/light_charm_on";
   private static final String OFF_TXT = "item/light_charm_off";
   
   public LightCharm(){
      id = ID;
      name = "Charm of Light";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EMPOWERED, TomeGui.TomeFilter.CHARMS, TomeGui.TomeFilter.ITEMS};
      itemVersion = 1;
      vanillaItem = Items.SUNFLOWER;
      item = new LightCharmItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.YELLOW))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,OFF_TXT));
      models.add(new Pair<>(vanillaItem,ON_TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.EFFECT_NIGHT_VISION, ResearchTasks.PLACE_TORCHES, ResearchTasks.ADVANCEMENT_CREATE_FULL_BEACON};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
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
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("The charm ").formatted(Formatting.GOLD))
            .append(Text.literal("radiates").formatted(Formatting.YELLOW))
            .append(Text.literal(" a warm glow.").formatted(Formatting.GOLD)));
      lore.add(Text.literal("")
            .append(Text.literal("Its light seems to ").formatted(Formatting.GOLD))
            .append(Text.literal("linger").formatted(Formatting.RED))
            .append(Text.literal(" behind you.").formatted(Formatting.GOLD)));
      lore.add(Text.literal("")
            .append(Text.literal("Right click").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" to adjust the ").formatted(Formatting.GOLD))
            .append(Text.literal("setting").formatted(Formatting.YELLOW))
            .append(Text.literal(".").formatted(Formatting.GOLD)));
      lore.add(Text.literal("")
            .append(Text.literal("Sneak Right click").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" to toggle the charm ").formatted(Formatting.GOLD))
            .append(Text.literal("mode").formatted(Formatting.YELLOW))
            .append(Text.literal(".").formatted(Formatting.GOLD)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   public void changeSetting(ServerPlayerEntity player, ItemStack item){
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
               player.sendMessage(Text.literal("The Charm's Light Brightens").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_ACTIVATE, 1,2f);
            }else{
               player.sendMessage(Text.literal("The Charm's Light Dims").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_DEACTIVATE, 1,.5f);
            }
            break;
         case 1:
            vision = !vision;
            putProperty(item,VISION_TAG, vision);
            if(vision){
               player.sendMessage(Text.literal("You can now see the arcane lights").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_ACTIVATE, 1,2f);
            }else{
               player.sendMessage(Text.literal("You can no longer see the arcane lights").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_DEACTIVATE, 1,.5f);
            }
            break;
         case 2:
            threshold = (threshold+1) % 16;
            player.sendMessage(Text.literal("Light Threshold: "+threshold).formatted(Formatting.YELLOW,Formatting.ITALIC),true);
            putProperty(item,THRESHOLD_TAG, threshold);
            break;
         case 3:
            brightness = (brightness+1) % 16;
            player.sendMessage(Text.literal("Light Brightness: "+brightness).formatted(Formatting.YELLOW,Formatting.ITALIC),true);
            putProperty(item,BRIGHTNESS_TAG, brightness);
            break;
         case 4:
            if(novaCD == 0){
               player.sendMessage(Text.literal("The Charm's Light Flares!").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
               nova(player,item);
               putProperty(item,NOVA_TAG, 30);
            }else{
               player.sendMessage(Text.literal("Radiant Nova Cooldown: "+novaCD+" seconds").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
            }
            break;
         case 5:
            break;
      }
   }
   
   private void nova(ServerPlayerEntity player, ItemStack stack){
      ServerWorld world = player.getServerWorld();
      BlockPos center = player.getBlockPos();
      int range = 32;
      int threshold = 3;
      BlockPos max = center.add(range,10,range);
      BlockPos min = center.add(-range,-3*range,-range);
      
      HashSet<BlockPos> possiblePositions = new HashSet<>();
      
      for(int x = min.getX(); x <= max.getX(); x++){
         for(int y = min.getY(); y <= max.getY(); y++){
            for(int z = min.getZ(); z <= max.getZ(); z++){
               BlockPos pos = new BlockPos(x, y, z);
               
               if(world.isInBuildLimit(pos)){
                  BlockState state = world.getBlockState(pos);
                  if(state.isAir() && world.getLightLevel(LightType.BLOCK,pos) <= threshold){
                     possiblePositions.add(pos);
                  }
               }
            }
         }
      }
      
      List<Pair<BlockPos, Integer>> pairTree = new ArrayList<>();
      int limit = threshold == 15 ? 1 : (15-threshold);
      
      while(!possiblePositions.isEmpty()){
         BlockPos root = possiblePositions.iterator().next();
         iterativeNovaSearch(limit, new Pair<>(root,0),possiblePositions,pairTree);
      }
      
      
      int placedCount = 0;
      for(Pair<BlockPos, Integer> pair : pairTree){
         if(pair.getRight() == 0){
            world.setBlockState(pair.getLeft(),Blocks.LIGHT.getDefaultState().with(LEVEL_15,15), Block.NOTIFY_ALL);
            world.emitGameEvent(player, GameEvent.BLOCK_PLACE, pair.getLeft());
            
            placedCount++;
         }
      }
      
      PLAYER_DATA.get(player).addXP(placedCount); // Add xp
      SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_FIRECHARGE_USE, 1f,0.5f);
   }
   
   // TODO maybe make this async in the future?
   private void iterativeNovaSearch(int limit, Pair<BlockPos, Integer> startPair, Set<BlockPos> posSet, List<Pair<BlockPos, Integer>> pairTree){
      Queue<Pair<BlockPos, Integer>> queue = new LinkedList<>();
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
         Pair<BlockPos, Integer> pair = queue.poll();
         BlockPos pos = pair.getLeft();
         
         if(posSet.remove(pos)){
            pairTree.add(pair);
            
            for(Direction direction : Direction.values()){
               BlockPos offset = pos.offset(direction);
               if(posSet.contains(offset) && pair.getRight() + 1 <= limit){
                  queue.add(new Pair<>(offset, pair.getRight() + 1));
               }
            }
            
            for(Vec3i diagonal : diagonals){
               BlockPos offset = pos.mutableCopy().add(diagonal);
               if(posSet.contains(offset) && pair.getRight() + 2 <= limit){
                  queue.add(new Pair<>(offset, pair.getRight() + 2));
               }
            }
         }
      }
   }
   
   public void selectMode(ServerPlayerEntity player, ItemStack item){
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
            player.sendMessage(Text.literal("Mode: Toggle Light Placement").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
            break;
         case 1:
            player.sendMessage(Text.literal("Mode: Toggle Light Visibility").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
            break;
         case 2:
            player.sendMessage(Text.literal("Mode: Threshold Selection").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
            break;
         case 3:
            player.sendMessage(Text.literal("Mode: Brightness Selection").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
            break;
         case 4:
            player.sendMessage(Text.literal("Mode: Radiant Nova").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
            break;
         case 5:
            player.sendMessage(Text.literal("Mode: Manual Placement").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
            break;
      }
      putProperty(item,MODE_TAG, mode);
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("    Charm of Light\n\nRarity: Empowered\n\nA Beacon's empowered light that has the ability to embue power seems like a solid base to start. After throwing in every light source under the sun and a couple of potions for good measure I have an ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Charm of Light\n\nitem that will leave lingering and invisible arcane lights behind me when it gets dark.\n\nThankfully those potions were added so I can see the lights by right clicking and remove them if they become a nuisance.").formatted(Formatting.BLACK)));
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
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   public class LightCharmItem extends ArcanaPolymerItem {
      public LightCharmItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!ArcanaItemUtils.isArcane(itemStack)) return ArcanaRegistry.getModelData(OFF_TXT).value();
         boolean active = getBooleanProperty(itemStack,ACTIVE_TAG);
         return active ? ArcanaRegistry.getModelData(ON_TXT).value() : ArcanaRegistry.getModelData(OFF_TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld serverWorld && entity instanceof ServerPlayerEntity player)) return;
         boolean vision = getBooleanProperty(stack,VISION_TAG);
         boolean active = getBooleanProperty(stack,ACTIVE_TAG);
         int threshold = getIntProperty(stack,THRESHOLD_TAG);
         int brightness = getIntProperty(stack,BRIGHTNESS_TAG);
         int novaCD = getIntProperty(stack,NOVA_TAG);
         
         if(world.getServer().getTicks() % 60 == 0){
            BlockPos pos = player.getBlockPos();
            if(active){
               if(world.getLightLevel(pos) <= threshold && world.getBlockState(pos).isAir()){
                  world.setBlockState(pos,Blocks.LIGHT.getDefaultState().with(LEVEL_15,brightness), Block.NOTIFY_ALL);
                  world.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, .3f,2f);
                  PLAYER_DATA.get(player).addXP((int) (10*brightness/2.0)); // Add xp
                  ArcanaAchievements.progress(player,ArcanaAchievements.ENLIGHTENED.id,1);
               }
            }
            
         }
         
         if(world.getServer().getTicks() % 20 == 0){
            if(novaCD > 0) putProperty(stack,NOVA_TAG, novaCD-1);
            BlockPos pos = player.getBlockPos();
            if(vision){
               // Search 10x10x10 area around player for light blocks
               for(BlockPos block : BlockPos.iterateOutwards(pos, 10, 10, 10)){
                  //System.out.println("looking at block "+block.toShortString());
                  BlockState state = world.getBlockState(block);
                  if(state.getBlock().equals(Blocks.LIGHT)){
                     serverWorld.spawnParticles(player, new BlockStateParticleEffect(ParticleTypes.BLOCK_MARKER, state), true, block.getX()+.5,block.getY()+.5,block.getZ()+.5, 1,0,0,0,0);
                  }
               }
            }
         }
      }
      
      @Override
      public ActionResult useOnBlock(ItemUsageContext context){
         PlayerEntity playerEntity = context.getPlayer();
         World world = context.getWorld();
         Hand hand = context.getHand();
         ItemStack stack = context.getStack();
         if(playerEntity == null) return ActionResult.PASS;
         
         int mode = getIntProperty(stack,MODE_TAG); // 0 is on/off, 1 is vision, 2 is threshold, 3 is brightness, 4 is nova, 5 is manual
         int brightness = getIntProperty(stack,BRIGHTNESS_TAG);
         Direction side = context.getSide();
         BlockPos pos = context.getBlockPos().add(side.getVector());
         boolean placeable = world.getBlockState(pos).canReplace(new ItemPlacementContext(playerEntity, hand, new ItemStack(Items.LIGHT), new BlockHitResult(context.getHitPos(),context.getSide(),context.getBlockPos(),context.hitsInsideBlock())));
         if(mode == 5 && playerEntity instanceof ServerPlayerEntity player && placeable){
            world.setBlockState(pos,Blocks.LIGHT.getDefaultState().with(LEVEL_15,brightness), Block.NOTIFY_ALL);
            world.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos);
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, .3f,2f);
            PLAYER_DATA.get(player).addXP(15); // Add xp
            ArcanaAchievements.progress(player,ArcanaAchievements.ENLIGHTENED.id,1);
            return ActionResult.SUCCESS;
         }
         return ActionResult.PASS;
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
         if(playerEntity.isSneaking()){
            selectMode((ServerPlayerEntity) playerEntity,playerEntity.getStackInHand(hand));
         }else{
            changeSetting((ServerPlayerEntity) playerEntity,playerEntity.getStackInHand(hand));
         }
         
         return ActionResult.SUCCESS;
      }
   }
}

