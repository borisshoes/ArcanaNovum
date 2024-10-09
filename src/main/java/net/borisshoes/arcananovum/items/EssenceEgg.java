package net.borisshoes.arcananovum.items;

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
import net.borisshoes.arcananovum.recipes.arcana.SoulstoneIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.Spawner;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class EssenceEgg extends ArcanaItem {
	public static final String ID = "essence_egg";
   
   public static final String USES_TAG = "uses";
   
   private static final String TXT = "item/essence_egg";
   
   public EssenceEgg(){
      id = ID;
      name = "Essence Egg";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EMPOWERED, TomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.GHAST_SPAWN_EGG;
      item = new EssenceEggItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.AQUA))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_SOULSTONE,ResearchTasks.FIND_SPAWNER,ResearchTasks.OBTAIN_EGG};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,Soulstone.TYPE_TAG,"unattuned");
      putProperty(stack,USES_TAG,0);
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("Harness the power of a filled ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("Soulstone").formatted(Formatting.DARK_RED))
            .append(Text.literal("...").formatted(Formatting.LIGHT_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("With enough ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("souls").formatted(Formatting.DARK_RED))
            .append(Text.literal(" a new form can be ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("spawned").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(".").formatted(Formatting.LIGHT_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Spawns").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" a mob of the ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("attuned type").formatted(Formatting.YELLOW))
            .append(Text.literal(".").formatted(Formatting.LIGHT_PURPLE)));
      lore.add(Text.literal(""));
      
      String attunedString = "Unattuned";
      int uses = 0;
      
      if(itemStack != null){
         String type = getType(itemStack);
         uses = getUses(itemStack);
         Optional<EntityType<?>> opt = EntityType.get(type);
         if(!type.equals("unattuned") && opt.isPresent()){
            String entityTypeName = opt.get().getName().getString();
            attunedString = "Attuned - "+entityTypeName;
         }
      }
      
      lore.add(Text.literal(attunedString).formatted(Formatting.LIGHT_PURPLE));
      lore.add(Text.literal(uses+" Uses Left").formatted(Formatting.GRAY));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      int uses = getIntProperty(stack,USES_TAG);
      String type = getStringProperty(stack,Soulstone.TYPE_TAG);
      ItemStack newStack = super.updateItem(stack, server);
      putProperty(newStack,USES_TAG,uses);
      putProperty(newStack,Soulstone.TYPE_TAG,type);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   public static void setType(ItemStack stack, String entityId){
      putProperty(stack,Soulstone.TYPE_TAG,entityId);
      ArcanaRegistry.ESSENCE_EGG.buildItemLore(stack,ArcanaNovum.SERVER);
   }
   
   public static String getType(ItemStack stack){
      return getStringProperty(stack,Soulstone.TYPE_TAG);
   }
   
   public static int getUses(ItemStack stack){
      return getIntProperty(stack,USES_TAG);
   }
   
   public static void setUses(ItemStack stack, int uses){
      uses = MathHelper.clamp(uses,0,1000000);
      
      if(uses <= 0){
         stack.decrement(stack.getCount());
      }else{
         putProperty(stack,USES_TAG,uses);
         ArcanaRegistry.ESSENCE_EGG.buildItemLore(stack,ArcanaNovum.SERVER);
      }
   }
   
   public static void addUses(ItemStack stack, int uses){
      int curUses = getIntProperty(stack,USES_TAG);
      int newUses = MathHelper.clamp(uses+curUses,0,1000000);
      if(newUses <= 0){
         stack.decrement(stack.getCount());
      }else{
         putProperty(stack,USES_TAG,newUses);
         ArcanaRegistry.ESSENCE_EGG.buildItemLore(stack,ArcanaNovum.SERVER);
      }
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv){
      // Souls n stuff
      ItemStack soulstoneStack = inv.getStack(12); // Should be the Soulstone
      ItemStack newArcanaItem = null;
      if(ArcanaItemUtils.identifyItem(soulstoneStack) instanceof Soulstone){
         int uses = (Soulstone.getSouls(soulstoneStack) / Soulstone.tiers[0]);
         String essenceType = Soulstone.getType(soulstoneStack);
         
         newArcanaItem = getNewItem();
         setType(newArcanaItem,essenceType);
         setUses(newArcanaItem,uses);
      }
      return newArcanaItem;
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("      Essence Egg\n\nRarity: Empowered\n\nAs making Soulstones has taught me, keeping souls imprisoned is hard.\nMaking so they can be released controllably, is even harder, thankfully I can build off of a Soulstone's solid foundation.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("      Essence Egg\n\nThis 'Egg' should take the souls from a Soulstone and keep them captive just long enough for me to release them into a new form. \nAlthough it takes 25 souls to make a new body for a single soul to inhabit.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("      Essence Egg\n\nThe Essence Egg can be used one at a time to spawn a mob, or 5 uses (125 souls) can be used on a spawner to change its attunement to the type of essence contained in the Egg.").formatted(Formatting.BLACK)));
      return list;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      SoulstoneIngredient t = new SoulstoneIngredient(Soulstone.tiers[0],true,false, false,null);
      ArcanaIngredient a = new ArcanaIngredient(Items.OBSIDIAN,16);
      ArcanaIngredient b = new ArcanaIngredient(Items.CRYING_OBSIDIAN,16);
      ArcanaIngredient c = new ArcanaIngredient(Items.IRON_BARS,16);
      ArcanaIngredient h = new ArcanaIngredient(Items.SOUL_SAND,16);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,c,h,c,b},
            {c,h,t,h,c},
            {b,c,h,c,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   public class EssenceEggItem extends ArcanaPolymerItem {
      public EssenceEggItem(Item.Settings settings){
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
         ItemStack stack = context.getStack();
         try{
            World world = context.getWorld();
            PlayerEntity playerEntity = context.getPlayer();
            
            if(!getType(stack).equals("unattuned")){
               // Check for use on spawner
               BlockPos blockPos = context.getBlockPos();
               BlockEntity blockEntity;
               BlockState blockState = world.getBlockState(blockPos);
               if(blockState.isOf(Blocks.SPAWNER) && (blockEntity = world.getBlockEntity(blockPos)) instanceof Spawner spawner){
                  int captiveLevel = Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.WILLING_CAPTIVE.id));
                  if(getUses(stack) >= 5-captiveLevel){
                     EntityType<?> entityType = EntityType.get(getType(stack)).get();
                     spawner.setEntityType(entityType, world.getRandom());
                     world.updateListeners(blockPos, blockState, blockState, Block.NOTIFY_ALL);
                     blockEntity.markDirty();
                     
                     if(playerEntity instanceof ServerPlayerEntity player){
                        player.sendMessage(Text.literal("The Spawner Assumes the Essence of "+EntityType.get(getType(stack)).get().getName().getString()).formatted(Formatting.DARK_AQUA, Formatting.ITALIC), true);
                        SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 1, .7f);
                        PLAYER_DATA.get(playerEntity).addXP(Math.min(0,2500-500*captiveLevel)); // Add xp
                        ArcanaAchievements.grant(player,ArcanaAchievements.SOUL_CONVERSION.id);
                     }
                     addUses(stack, -5+captiveLevel);
                  }
               }else{
                  int splitLevel = Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SOUL_SPLIT.id));
                  int efficiencyLevel = Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.DETERMINED_SPIRIT.id));
                  if(getUses(stack) > 0){
                     ServerWorld serverWorld = world.getServer().getWorld(world.getRegistryKey());
                     Vec3d summonPos = context.getHitPos().add(0,0.5,0);
                     
                     NbtCompound nbtCompound = new NbtCompound();
                     nbtCompound.putString("id", getType(stack));
                     int spawns = Math.random() >= 0.1*splitLevel ? 1 : 2;
                     
                     for(int i = 0; i < spawns; i++){
                        Entity newEntity = EntityType.loadEntityWithPassengers(nbtCompound, serverWorld, SpawnReason.SPAWN_ITEM_USE, entity -> {
                           entity.refreshPositionAndAngles(summonPos.getX(), summonPos.getY(), summonPos.getZ(), entity.getYaw(), entity.getPitch());
                           return entity;
                        });
                        if(newEntity instanceof MobEntity mobEntity){
                           mobEntity.initialize(serverWorld, serverWorld.getLocalDifficulty(newEntity.getBlockPos()), SpawnReason.SPAWN_ITEM_USE, null);
                        }
                        serverWorld.spawnNewEntityAndPassengers(newEntity);
                     }
                     
                     if(Math.random() >= 0.1*efficiencyLevel){
                        addUses(stack,-1);
                     }
                     if(playerEntity instanceof ServerPlayerEntity player){
                        SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_FIRECHARGE_USE, 1, 1.5f);
                        PLAYER_DATA.get(playerEntity).addXP(500); // Add xp
                        ArcanaAchievements.progress(player,ArcanaAchievements.SOUL_FOR_SOUL.id,1);
                     }
                  }
               }
            }
            
            return ActionResult.SUCCESS;
         }catch(Exception e){
            e.printStackTrace();
            return ActionResult.PASS;
         }
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
         ItemStack item = playerEntity.getStackInHand(hand);
         if(playerEntity.isCreative()){
            if(!getType(item).equals("unattuned"))
               addUses(item,1);
            return ActionResult.SUCCESS;
         }
         return ActionResult.PASS;
      }
      
      @Override
      public boolean postHit(ItemStack stack, LivingEntity entity, LivingEntity attacker){
         if(!(attacker instanceof PlayerEntity playerEntity)) return false;
         
         if(playerEntity.isCreative()){
            String type = getType(stack);
            
            if(type.equals("unattuned") && entity instanceof MobEntity attackedEntity && playerEntity instanceof ServerPlayerEntity player && !(entity instanceof EnderDragonEntity || entity instanceof WitherEntity)){
               String entityTypeId = EntityType.getId(attackedEntity.getType()).toString();
               String entityTypeName = EntityType.get(entityTypeId).get().getName().getString();
               
               setType(stack,entityTypeId);
               player.sendMessage(Text.literal("The Essence Egg attunes to the essence of "+entityTypeName).formatted(Formatting.DARK_RED,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1,.5f);
            }
         }
         
         return false;
      }
   }
}

