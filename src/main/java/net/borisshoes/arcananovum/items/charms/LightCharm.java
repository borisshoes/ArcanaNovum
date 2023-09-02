package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class LightCharm extends MagicItem {
   public LightCharm(){
      id = "light_charm";
      name = "Charm of Light";
      rarity = MagicRarity.EMPOWERED;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EMPOWERED, ArcaneTome.TomeFilter.CHARMS, ArcaneTome.TomeFilter.ITEMS};
      itemVersion = 1;
      vanillaItem = Items.SUNFLOWER;
      item = new LightCharmItem(new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Charm of Light\",\"italic\":false,\"color\":\"yellow\",\"bold\":true}]");
      loreList.add(NbtString.of("[{\"text\":\"The charm \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"radiates\",\"color\":\"yellow\"},{\"text\":\" a warm glow.\",\"color\":\"gold\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Its light seems to \",\"italic\":false,\"color\":\"gold\"},{\"text\":\"linger\",\"italic\":false,\"color\":\"red\"},{\"text\":\" behind you.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right click\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\" to adjust the \",\"color\":\"gold\"},{\"text\":\"setting\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"gold\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Sneak Right click\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\" to toggle the charm \",\"color\":\"gold\"},{\"text\":\"mode\",\"color\":\"yellow\"},{\"text\":\".\",\"color\":\"gold\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Empowered\",\"italic\":false,\"color\":\"green\",\"bold\":true},{\"text\":\" Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      prefNBT.getCompound("arcananovum").putBoolean("vision",false);
      prefNBT.getCompound("arcananovum").putBoolean("active",true);
      prefNBT.getCompound("arcananovum").putInt("mode",0); // 0 is on/off, 1 is vision, 2 is threshold, 3 is brightness, 4 is nova, 5 is manual
      prefNBT.getCompound("arcananovum").putInt("brightness",15);
      prefNBT.getCompound("arcananovum").putInt("threshold",5);
      prefNBT.getCompound("arcananovum").putInt("novaCD",0);
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   public void changeSetting(ServerPlayerEntity player, ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      boolean vision = magicTag.getBoolean("vision");
      boolean active = magicTag.getBoolean("active");
      int mode = magicTag.getInt("mode"); // 0 is on/off, 1 is vision, 2 is threshold, 3 is brightness, 4 is nova, 5 is manual
      int threshold = magicTag.getInt("threshold");
      int brightness = magicTag.getInt("brightness");
      int novaCD = magicTag.getInt("novaCD");
   
      switch(mode){
         case 0:
            active = !active;
            magicTag.putBoolean("active",active);
            if(active){
               player.sendMessage(Text.translatable("The Charm's Light Brightens").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_ACTIVATE, 1,2f);
            }else{
               player.sendMessage(Text.translatable("The Charm's Light Dims").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_DEACTIVATE, 1,.5f);
            }
            break;
         case 1:
            vision = !vision;
            magicTag.putBoolean("vision",vision);
            if(vision){
               player.sendMessage(Text.translatable("You can now see the magical lights").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_ACTIVATE, 1,2f);
            }else{
               player.sendMessage(Text.translatable("You can no longer see the magical lights").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_BEACON_DEACTIVATE, 1,.5f);
            }
            break;
         case 2:
            threshold = (threshold+1) % 16;
            player.sendMessage(Text.translatable("Light Threshold: "+threshold).formatted(Formatting.YELLOW,Formatting.ITALIC),true);
            magicTag.putInt("threshold",threshold);
            break;
         case 3:
            brightness = (brightness+1) % 16;
            player.sendMessage(Text.translatable("Light Brightness: "+brightness).formatted(Formatting.YELLOW,Formatting.ITALIC),true);
            magicTag.putInt("brightness",brightness);
            break;
         case 4:
            if(novaCD == 0){
               player.sendMessage(Text.translatable("The Charm's Light Flares!").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
               nova(player,item);
               magicTag.putInt("novaCD",30);
            }else{
               player.sendMessage(Text.translatable("Radiant Nova Cooldown: "+novaCD+" seconds").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
            }
            break;
         case 5:
            break;
      }
   }
   
   private void nova(ServerPlayerEntity player, ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      ServerWorld world = player.getServerWorld();
      BlockPos center = player.getBlockPos();
      int range = 25;
      BlockPos max = center.add(range,5,range);
      BlockPos min = center.add(-range,-range,-range);
      int l = 7;
      
      
      for(int x = min.getX(); x <= max.getX(); x+=l){
         for(int y = min.getY(); y <= max.getY(); y+=l/2){
            for(int z = min.getZ(); z <= max.getZ(); z+=l){
               BlockPos pos = new BlockPos(x, y, z);
               if(world.getBlockState(pos).isAir() && world.getLightLevel(pos) < 7){
                  world.setBlockState(pos,Blocks.LIGHT.getDefaultState().with(IntProperty.of("level", 0, 15),15), Block.NOTIFY_ALL);
                  world.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos);
      
                  PLAYER_DATA.get(player).addXP(5); // Add xp
                  ArcanaAchievements.progress(player,ArcanaAchievements.ENLIGHTENED.id,1);
               }
               
            }
         }
      }
      
      SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_FIRECHARGE_USE, 1f,0.5f);
   }
   
   public void selectMode(ServerPlayerEntity player, ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicTag = item.getNbt().getCompound("arcananovum");
      boolean vision = magicTag.getBoolean("vision");
      boolean active = magicTag.getBoolean("active");
      int mode = magicTag.getInt("mode"); // 0 is on/off, 1 is vision, 2 is threshold, 3 is brightness, 4 is nova, 5 is manual
      int threshold = magicTag.getInt("threshold");
      int brightness = magicTag.getInt("brightness");
      
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
            player.sendMessage(Text.translatable("Mode: Toggle Light Placement").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
            break;
         case 1:
            player.sendMessage(Text.translatable("Mode: Toggle Light Visibility").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
            break;
         case 2:
            player.sendMessage(Text.translatable("Mode: Threshold Selection").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
            break;
         case 3:
            player.sendMessage(Text.translatable("Mode: Brightness Selection").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
            break;
         case 4:
            player.sendMessage(Text.translatable("Mode: Radiant Nova").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
            break;
         case 5:
            player.sendMessage(Text.translatable("Mode: Manual Placement").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
            break;
      }
      magicTag.putInt("mode",mode);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"    Charm of Light\\n\\nRarity: Empowered\\n\\nA Beacon's empowered light that has the ability to embue power seems like a solid base to start. After throwing in every light source under the sun and a couple of potions for good measure I have an \"}");
      list.add("{\"text\":\"    Charm of Light\\n\\nitem that will leave lingering and invisible magical lights behind me when it gets dark.\\n\\nThankfully those potions were added so I can see the lights by right clicking and remove them if they become a nuisance.\"}");
      return list;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient b = new MagicItemIngredient(Items.BEACON,1,null, true);
      MagicItemIngredient g = new MagicItemIngredient(Items.GLOWSTONE,64,null);
      MagicItemIngredient s = new MagicItemIngredient(Items.SHROOMLIGHT,64,null);
      MagicItemIngredient f = new MagicItemIngredient(Items.PEARLESCENT_FROGLIGHT,64,null);
      MagicItemIngredient o = new MagicItemIngredient(Items.OCHRE_FROGLIGHT,64,null);
      MagicItemIngredient v = new MagicItemIngredient(Items.VERDANT_FROGLIGHT,64,null);
      MagicItemIngredient l = new MagicItemIngredient(Items.SEA_LANTERN,64,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.BLAZE_POWDER,64,null);
      ItemStack potion = new ItemStack(Items.POTION);
      MagicItemIngredient n = new MagicItemIngredient(Items.POTION,1,PotionUtil.setPotion(potion,Potions.LONG_NIGHT_VISION).getNbt());
      
      MagicItemIngredient[][] ingredients = {
            {n,p,s,p,n},
            {p,l,g,l,p},
            {f,g,b,g,o},
            {p,l,g,l,p},
            {n,p,v,p,n}};
      return new MagicItemRecipe(ingredients);
   }
   
   public class LightCharmItem extends MagicPolymerItem {
      public LightCharmItem(Settings settings){
         super(getThis(),settings);
      }
      
      
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld serverWorld && entity instanceof ServerPlayerEntity player)) return;
         NbtCompound magicTag = stack.getNbt().getCompound("arcananovum");
         boolean vision = magicTag.getBoolean("vision");
         boolean active = magicTag.getBoolean("active");
         int threshold = magicTag.getInt("threshold");
         int brightness = magicTag.getInt("brightness");
         int novaCD = magicTag.getInt("novaCD");
         
         if(world.getServer().getTicks() % 60 == 0){
            BlockPos pos = player.getBlockPos();
            if(active){
               if(world.getLightLevel(pos) < threshold && world.getBlockState(pos).isAir()){
                  world.setBlockState(pos,Blocks.LIGHT.getDefaultState().with(IntProperty.of("level", 0, 15),brightness), Block.NOTIFY_ALL);
                  world.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, .3f,2f);
                  PLAYER_DATA.get(player).addXP((int) (10*brightness/2.0)); // Add xp
                  ArcanaAchievements.progress(player,ArcanaAchievements.ENLIGHTENED.id,1);
               }
            }
            
         }
         
         if(world.getServer().getTicks() % 20 == 0){
            if(novaCD > 0) magicTag.putInt("novaCD",novaCD-1);
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
         
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicTag = itemNbt.getCompound("arcananovum");
         int mode = magicTag.getInt("mode"); // 0 is on/off, 1 is vision, 2 is threshold, 3 is brightness, 4 is nova, 5 is manual
         int brightness = magicTag.getInt("brightness");
         Direction side = context.getSide();
         BlockPos pos = context.getBlockPos().add(side.getVector());
         boolean placeable = world.getBlockState(pos).canReplace(new ItemPlacementContext(playerEntity, hand, new ItemStack(Items.LIGHT), new BlockHitResult(context.getHitPos(),context.getSide(),context.getBlockPos(),context.hitsInsideBlock())));
         if(mode == 5 && playerEntity instanceof ServerPlayerEntity player && placeable){
            world.setBlockState(pos,Blocks.LIGHT.getDefaultState().with(IntProperty.of("level", 0, 15),brightness), Block.NOTIFY_ALL);
            world.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos);
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, .3f,2f);
            PLAYER_DATA.get(player).addXP(15); // Add xp
            return ActionResult.SUCCESS;
         }
         return ActionResult.PASS;
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand){
         if(playerEntity.isSneaking()){
            selectMode((ServerPlayerEntity) playerEntity,playerEntity.getStackInHand(hand));
         }else{
            changeSetting((ServerPlayerEntity) playerEntity,playerEntity.getStackInHand(hand));
         }
         
         return TypedActionResult.success(playerEntity.getStackInHand(hand));
      }
   }
}
