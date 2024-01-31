package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.GenericMagicIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TadpoleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class WildGrowthCharm extends MagicItem {
   
   private static final int[] RATES = new int[]{10, 7, 5, 3, 2};
   private static final String ON_TXT = "item/wild_growth_charm_on";
   private static final String OFF_TXT = "item/wild_growth_charm_off";
   
   public WildGrowthCharm(){
      id = "wild_growth_charm";
      name = "Wild Growth Charm";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ITEMS, ArcaneTome.TomeFilter.CHARMS};
      itemVersion = 0;
      vanillaItem = Items.DARK_OAK_SAPLING;
      item = new WildGrowthCharmItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,OFF_TXT));
      models.add(new Pair<>(vanillaItem,ON_TXT));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Charm of Wild Growth\",\"italic\":false,\"bold\":true,\"color\":\"green\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      buildItemLore(stack, ArcanaNovum.SERVER);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      tag = this.addMagicNbt(tag);
      tag.getCompound("arcananovum").putBoolean("active",false);
      tag.getCompound("arcananovum").putBoolean("harvest",false);
      prefNBT = tag;
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"charm\",\"color\":\"green\"},{\"text\":\" smells of a \"},{\"text\":\"cool\",\"color\":\"aqua\"},{\"text\":\" spring\"},{\"text\":\" \",\"color\":\"green\"},{\"text\":\"breeze.\",\"color\":\"gray\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Nearby \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"plants\",\"color\":\"green\"},{\"text\":\" and \"},{\"text\":\"animals\",\"color\":\"green\"},{\"text\":\" seem to be\"},{\"text\":\" \",\"color\":\"green\"},{\"text\":\"invigorated\",\"color\":\"aqua\"},{\"text\":\" by its \"},{\"text\":\"proximity\",\"color\":\"green\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Sneak Right Click\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" the \",\"color\":\"dark_green\"},{\"text\":\"charm \",\"color\":\"green\"},{\"text\":\"to toggle \",\"color\":\"dark_green\"},{\"text\":\"its \",\"color\":\"dark_green\"},{\"text\":\"effect\",\"color\":\"green\"},{\"text\":\".\",\"color\":\"dark_green\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      return loreList;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.BAMBOO,64,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.SPRUCE_SAPLING,64,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.BONE_MEAL,64,null);
      MagicItemIngredient d = new MagicItemIngredient(Items.BIRCH_SAPLING,64,null);
      MagicItemIngredient e = new MagicItemIngredient(Items.SUGAR_CANE,64,null);
      MagicItemIngredient f = new MagicItemIngredient(Items.OAK_SAPLING,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.POTATO,64,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.BONE_BLOCK,16,null);
      MagicItemIngredient i = new MagicItemIngredient(Items.POPPY,64,null);
      MagicItemIngredient j = new MagicItemIngredient(Items.JUNGLE_SAPLING,64,null);
      GenericMagicIngredient m = new GenericMagicIngredient(ArcanaRegistry.TEMPORAL_MOMENT,1);
      MagicItemIngredient p = new MagicItemIngredient(Items.MANGROVE_PROPAGULE,64,null);
      MagicItemIngredient q = new MagicItemIngredient(Items.WHEAT,64,null);
      MagicItemIngredient s = new MagicItemIngredient(Items.CARROT,64,null);
      MagicItemIngredient t = new MagicItemIngredient(Items.ACACIA_SAPLING,64,null);
      MagicItemIngredient u = new MagicItemIngredient(Items.SWEET_BERRIES,64,null);
      MagicItemIngredient v = new MagicItemIngredient(Items.CHERRY_SAPLING,64,null);
      MagicItemIngredient x = new MagicItemIngredient(Items.DARK_OAK_SAPLING,64,null);
      MagicItemIngredient y = new MagicItemIngredient(Items.GLOW_BERRIES,64,null);
      
      MagicItemIngredient[][] ingredients = {
            {a,b,c,d,e},
            {f,g,h,i,j},
            {c,h,m,h,c},
            {p,q,h,s,t},
            {u,v,c,x,y}};
      return new MagicItemRecipe(ingredients);
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\" Charm of Wild Growth\\n\\nRarity: Empowered\\n\\nLiving things constantly grow and change with time. \\nIt should be possible to accelerate this process in all organisms as long as adequate nutrients are supplied with a few temporal tricks.\"}");
      list.add("{\"text\":\" Charm of Wild Growth\\n\\nThe Charm causes nearby animals and plants to grow faster when activated.\\n \\nThe Charm also causes animals to breed periodically.\"}");
      return list;
   }
   
   public class WildGrowthCharmItem extends MagicPolymerItem {
      public WildGrowthCharmItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!MagicItemUtils.isMagic(itemStack)) return ArcanaRegistry.MODELS.get(OFF_TXT).value();
         boolean active = itemStack.getNbt().getCompound("arcananovum").getBoolean("active");
         return active ? ArcanaRegistry.MODELS.get(ON_TXT).value() : ArcanaRegistry.MODELS.get(OFF_TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         boolean active = stack.getNbt().getCompound("arcananovum").getBoolean("active");
         boolean harvest = stack.getNbt().getCompound("arcananovum").getBoolean("harvest");
         int fertLvl = Math.max(ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.FERTILIZATION.id),0);
         int tickTime = RATES[fertLvl];
         
         if(active && !world.isClient && player.getServer().getTicks() % tickTime == 0){
            boolean bloom = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.CHARM_OF_BLOOMING.id) >= 1;
            int reaping = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.REAPING.id);
            
            int count = 0;
            for(BlockPos blockPos : BlockPos.iterateRandomly(player.getRandom(), 100, player.getBlockPos(), 4)){
               BlockState state = world.getBlockState(blockPos);
               Block block = state.getBlock();
               if(count >= 2) break;
               count++;
               
               if(block instanceof ShortPlantBlock){
                  count--;
               }else if(block instanceof SugarCaneBlock ||
                     block instanceof NetherWartBlock ||
                     block instanceof CactusBlock ||
                     block instanceof ChorusFlowerBlock ||
                     block instanceof OxidizableBlock ||
                     block instanceof BuddingAmethystBlock){
                  state.randomTick(player.getServerWorld(),blockPos,world.getRandom());
                  world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos, 0);
               }else if((block instanceof AbstractPlantStemBlock ||
                     block instanceof CropBlock ||
                     block instanceof CocoaBlock ||
                     block instanceof StemBlock ||
                     block instanceof SweetBerryBushBlock) && BoneMealItem.useOnFertilizable(new ItemStack(Items.BONE_MEAL,64), world, blockPos)){
                  world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos, 0);
                  
                  if(world.getBlockState(blockPos).getBlock() instanceof CropBlock crop && crop.isMature(world.getBlockState(blockPos))){
                     ArcanaAchievements.progress(player,ArcanaAchievements.BOUNTIFUL_HARVEST.id,1);
                     PLAYER_DATA.get(player).addXP(25); // Add xp
                  }
               }else if (bloom && BoneMealItem.useOnFertilizable(new ItemStack(Items.BONE_MEAL,64), world, blockPos)) {
                  world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos, 0);
               }else if(bloom && BoneMealItem.useOnGround(new ItemStack(Items.BONE_MEAL,64), world, blockPos, Direction.random(player.getRandom()))){
                  world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos, 0);
               }else{
                  count--;
               }
               
               if(reaping >= 1 && harvest){
                  if(block instanceof CropBlock crop && crop.isMature(world.getBlockState(blockPos))){
                     world.breakBlock(blockPos,true,player);
                     if(reaping >= 2 && crop.canPlaceAt(world.getBlockState(blockPos.down()),world,blockPos)){
                        world.setBlockState(blockPos,block.getDefaultState());
                     }
                  }else if(block instanceof NetherWartBlock wart && world.getBlockState(blockPos).get(NetherWartBlock.AGE) == 3){
                     world.breakBlock(blockPos,true,player);
                     if(reaping >= 2 && wart.canPlaceAt(world.getBlockState(blockPos.down()),world,blockPos)){
                        world.setBlockState(blockPos,block.getDefaultState());
                     }
                  }
               }
            }
            
            List<Entity> animals = world.getOtherEntities(player,player.getBoundingBox().expand(5), e -> e instanceof AnimalEntity || e instanceof TadpoleEntity);
            Collections.shuffle(animals);
            for(Entity a : animals){
               
               if(a instanceof AnimalEntity animal){
                  int i = animal.getBreedingAge();
                  if(!animal.getWorld().isClient && i == 0 && animal.canEat()){
                     animal.lovePlayer(player);
                     break;
                  }
                  if(animal.isBaby()){
                     animal.growUp(AnimalEntity.toGrowUpAge(-i), true);
                     ((ServerWorld) world).spawnParticles(ParticleTypes.COMPOSTER,animal.getX(),animal.getY()+animal.getHeight()*0.5,animal.getZ(),3,0.2,0.2,0.2,1);
                     break;
                  }
               }else if(a instanceof TadpoleEntity tadpole){
                  tadpole.increaseAge(PassiveEntity.toGrowUpAge(tadpole.getTicksUntilGrowth()));
                  ((ServerWorld) world).spawnParticles(ParticleTypes.COMPOSTER,tadpole.getX(),tadpole.getY()+tadpole.getHeight()*0.5,tadpole.getZ(),3,0.2,0.2,0.2,1);
                  break;
               }
            }
            
            if(animals.stream().filter(e -> (e instanceof AnimalEntity animal && animal.isBaby())).count() >= 5){
               ArcanaAchievements.grant(player,ArcanaAchievements.THEY_GROW_UP_SO_FAST.id);
            }
            
            if(count >= 2){
               PLAYER_DATA.get(player).addXP(1); // Add xp
            }
         }
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
         ItemStack stack = playerEntity.getStackInHand(hand);
         if(!(playerEntity instanceof ServerPlayerEntity player)) return TypedActionResult.pass(stack);
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         boolean active = magicNbt.getBoolean("active");
         boolean harvest = magicNbt.getBoolean("harvest");
         int reaping = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.REAPING.id);
         
         if(playerEntity.isSneaking()){
            if(reaping > 0 && hand == Hand.OFF_HAND){
               harvest = !harvest;
               magicNbt.putBoolean("harvest",harvest);
               if(harvest){
                  player.sendMessage(Text.literal("The Charm Begins to Reap").formatted(Formatting.GREEN,Formatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_GRASS_PLACE, .7f,.7f);
               }else{
                  player.sendMessage(Text.literal("The Charm Ceases Reaping").formatted(Formatting.GREEN,Formatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_BONE_MEAL_USE, 2f,.5f);
               }
            }else{
               active = !active;
               magicNbt.putBoolean("active",active);
               if(active){
                  player.sendMessage(Text.literal("The Charm Begins to Bloom").formatted(Formatting.GREEN,Formatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_GRASS_PLACE, .7f,.7f);
               }else{
                  player.sendMessage(Text.literal("The Charm Recedes").formatted(Formatting.GREEN,Formatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_BONE_MEAL_USE, 2f,.5f);
               }
            }
            return TypedActionResult.success(stack);
         }
         return TypedActionResult.pass(stack);
      }
      
      @Override
      public ActionResult useOnBlock(ItemUsageContext context) {
         PlayerEntity playerEntity = context.getPlayer();
         World world = context.getWorld();
         BlockPos blockPos = context.getBlockPos();
         BlockPos blockPos2 = blockPos.offset(context.getSide());
         
         if(playerEntity != null && playerEntity.isSneaking() && playerEntity instanceof ServerPlayerEntity player){
            NbtCompound itemNbt = context.getStack().getNbt();
            NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
            boolean active = magicNbt.getBoolean("active");
            
            active = !active;
            magicNbt.putBoolean("active",active);
            itemNbt.put("arcananovum",magicNbt);
            if(active){
               playerEntity.sendMessage(Text.translatable("The Charm Begins to Bloom").formatted(Formatting.GREEN,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_GRASS_PLACE, .7f,.7f);
            }else{
               playerEntity.sendMessage(Text.translatable("The Charm Recedes").formatted(Formatting.GREEN,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_BONE_MEAL_USE, 2f,.5f);
            }
            return ActionResult.success(world.isClient);
         }
         
         if(ArcanaAugments.getAugmentOnItem(context.getStack(),ArcanaAugments.CHARM_OF_BLOOMING.id) < 1){
            return ActionResult.PASS;
         }
         
         if (BoneMealItem.useOnFertilizable(new ItemStack(Items.BONE_MEAL,64), world, blockPos)) {
            if (!world.isClient) {
               world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos, 0);
            }
            return ActionResult.success(world.isClient);
         }
         BlockState blockState = world.getBlockState(blockPos);
         boolean bl = blockState.isSideSolidFullSquare(world, blockPos, context.getSide());
         if (bl && BoneMealItem.useOnGround(new ItemStack(Items.BONE_MEAL,64), world, blockPos2, context.getSide())) {
            if (!world.isClient) {
               world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos2, 0);
            }
            return ActionResult.success(world.isClient);
         }
         return ActionResult.PASS;
      }
   }
}
