package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class AncientDowsingRod extends EnergyItem {
	public static final String ID = "ancient_dowsing_rod";
   
   public AncientDowsingRod(){
      id = ID;
      name = "Ancient Dowsing Rod";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.BLAZE_ROD;
      item = new AncientDowsingRodItem(addArcanaItemComponents(new Item.Settings().maxCount(1)));
      displayName =  Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.DARK_RED);
      researchTasks = new RegistryKey[]{ResearchTasks.RESONATE_BELL,ResearchTasks.ADVANCEMENT_OBTAIN_ANCIENT_DEBRIS,ResearchTasks.ADVANCEMENT_FIND_BASTION};
      initEnergy = 100;
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("Ancient civilizations").formatted(Formatting.GOLD))
            .append(Text.literal(" in the ").formatted(Formatting.RED))
            .append(Text.literal("nether ").formatted(Formatting.DARK_RED))
            .append(Text.literal("had ways of finding ").formatted(Formatting.RED))
            .append(Text.literal("netherite").formatted(Formatting.DARK_RED))
            .append(Text.literal(".").formatted(Formatting.RED)));
      lore.add(Text.literal("")
            .append(Text.literal("This ").formatted(Formatting.RED))
            .append(Text.literal("dowsing rod ").formatted(Formatting.DARK_RED))
            .append(Text.literal("is based on ").formatted(Formatting.RED))
            .append(Text.literal("ancient designs").formatted(Formatting.GOLD))
            .append(Text.literal(" to locate ").formatted(Formatting.RED))
            .append(Text.literal("netherite scrap").formatted(Formatting.DARK_RED))
            .append(Text.literal(".").formatted(Formatting.RED)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.GOLD))
            .append(Text.literal(" to search for ").formatted(Formatting.RED))
            .append(Text.literal("ancient debris").formatted(Formatting.DARK_RED))
            .append(Text.literal(".").formatted(Formatting.RED)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      // 30 second base recharge
      int lvl = Math.max(0,ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.SONIC_REABSORPTION.id));
      return 30 - 5*lvl;
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.GOLD_INGOT,16);
      ArcanaIngredient b = new ArcanaIngredient(Items.RED_NETHER_BRICKS,12);
      ArcanaIngredient c = new ArcanaIngredient(Items.FIRE_CHARGE,16);
      ArcanaIngredient d = new ArcanaIngredient(Items.BLAZE_ROD,8);
      ArcanaIngredient g = new ArcanaIngredient(Items.NETHERITE_SCRAP,1);
      ArcanaIngredient h = new ArcanaIngredient(Items.ANCIENT_DEBRIS,1);
      ArcanaIngredient l = new ArcanaIngredient(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE,1);
      ArcanaIngredient m = new ArcanaIngredient(Items.BELL,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,d,b},
            {b,g,h,g,d},
            {h,l,m,l,h},
            {d,g,h,g,b},
            {b,d,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal(" Ancient Dowsing\n         Rod").formatted(Formatting.DARK_RED,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nModern Piglins seem to be incapable of finding Netherite, but their bastions contain fragments of it, and the smithing templates to forge it. There may be some history at play here.   ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal(" Ancient Dowsing\n         Rod").formatted(Formatting.DARK_RED,Formatting.BOLD),Text.literal("\nI recovered some pieces of a tool, possibly used by their ancestors, perhaps I can reconstruct it.\n\nUse the rod to send out a resonating signal that reflects off Ancient Debris. ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal(" Ancient Dowsing\n         Rod").formatted(Formatting.DARK_RED,Formatting.BOLD),Text.literal("\nThe echos trigger a compass of flame to indicate how much debris is nearby.\n\nAdditionally, a flaming arrow points to the closest debris nearby.\n").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class AncientDowsingRodItem extends ArcanaPolymerItem {
      public AncientDowsingRodItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         List<String> stringList = new ArrayList<>();
         if(getEnergy(itemStack) < getMaxEnergy(itemStack)){
            stringList.add("cooldown");
         }else{
            stringList.add("charged");
         }
         baseStack.set(DataComponentTypes.CUSTOM_MODEL_DATA,new CustomModelDataComponent(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld)) return;
         if(world.getServer().getTicks() % 20 == 0){
            addEnergy(stack, 1); // Recharge
         }
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
         ItemStack item = playerEntity.getStackInHand(hand);
         if(playerEntity instanceof ServerPlayerEntity player){
            int curEnergy = getEnergy(item);
            if(curEnergy >= getMaxEnergy(item)){
               setEnergy(item,0);
               final int scanRange = 25 + 5*Math.max(0,ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.ENHANCED_RESONANCE.id));
               BlockPos curBlock = playerEntity.getBlockPos();
               SoundUtils.playSound(world, curBlock, SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1f, .5f);
               
               List<BlockPos> debris = new ArrayList<>();
               for(BlockPos block : BlockPos.iterateOutwards(curBlock,scanRange,scanRange/2,scanRange)){
                  if(world.getBlockState(block).getBlock() == Blocks.ANCIENT_DEBRIS){
                     debris.add(new BlockPos(block));
                  }
               }
               if(world instanceof ServerWorld serverWorld){
                  if(!debris.isEmpty()) ArcanaAchievements.progress(player,ArcanaAchievements.ARCHEOLOGIST.id,debris.size());
                  
                  ArcanaNovum.addTickTimerCallback(new GenericTimer(30, () -> SoundUtils.playSound(world, playerEntity.getBlockPos(), SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.PLAYERS, 1f, .5f)));
                  ArcanaNovum.addTickTimerCallback(new GenericTimer(140, () -> {
                     int[] locations = new int[8]; // N, NE, E, SE, S, SW, W, NW
                     final double t1 = Math.tan(Math.toRadians(45*3.0/2));
                     final double t2 = Math.tan(Math.toRadians(45/2.0));
                     final Vec3d playerPos = playerEntity.getPos();
                     int count = 0;
                     
                     for(BlockPos b : debris){
                        Vec3d rPos = new Vec3d( b.getX() - playerPos.x, b.getY() - playerPos.y,b.getZ() - playerPos.z);
                        double ratio = rPos.x == 0 ? 100 : rPos.z / rPos.x;
                        int ind = 0;
                        
                        if(ratio < 0 && ratio > -t2){
                           ind = 0;
                        }else if(ratio > -t1 && ratio < -t2){
                           ind = 1;
                        }else if(ratio < -t1 || ratio > t1){
                           ind = 2;
                        }else if(ratio < t1 && ratio > t2){
                           ind = 3;
                        }else if(ratio > 0 && ratio < t2){
                           ind = 4;
                        }
                        if(rPos.z < 0){
                           ind += 4;
                        }
                        if(ind > locations.length-1){
                           ind = 0;
                        }
                        locations[ind]++;
                        
                        if(count < 12)
                           ParticleEffectUtils.dowsingRodEmitter(serverWorld,new Vec3d(b.getX(),b.getY(),b.getZ()),1,100 + 33*Math.max(0,ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.HARMONIC_FEEDBACK.id)));
                        count++;
                     }
                     double radius = 1.5;
                     for(int i = 0; i < locations.length; i++){
                        Vec3d pPos = switch(i){
                           case 0 -> new Vec3d(-radius, 0, 0);
                           case 1 -> new Vec3d(-radius, 0, radius);
                           case 2 -> new Vec3d(0, 0, radius);
                           case 3 -> new Vec3d(radius, 0, radius);
                           case 4 -> new Vec3d(radius, 0, 0);
                           case 5 -> new Vec3d(radius, 0, -radius);
                           case 6 -> new Vec3d(0, 0, -radius);
                           case 7 -> new Vec3d(-radius, 0, -radius);
                           default -> new Vec3d(0,0,0);
                        };
                        for(int n = 0; n < locations[i]; n++){
                           double mod = Math.min(n*.6 , 6/radius);
                           if(mod == 6/radius)
                              break;
                           Vec3d parPos = playerPos.add(pPos.multiply(1+mod)).add(0,0.7,0);
                           player.getServerWorld().spawnParticles(ParticleTypes.DRIPPING_LAVA,parPos.x,parPos.y,parPos.z,15,.12,.12,.12,1);
                        }
                        
                     }
                     if(!debris.isEmpty()){
                        BlockPos closest = debris.get(0);
                        Vec3d eyePos = playerEntity.getEyePos();
                        Vec3d blockPos = new Vec3d(closest.getX()+.5,closest.getY()+0.5,closest.getZ()+0.5);
                        Vec3d start = eyePos.add(blockPos.subtract(eyePos).normalize().multiply(1.5));
                        Vec3d end = eyePos.add(blockPos.subtract(eyePos).normalize().multiply(1.5+3));
                        ParticleEffectUtils.dowsingRodArrow(player.getServerWorld(),start,end,1);
                        
                        ArcanaNovum.data(player).addXP(Math.min(ArcanaConfig.getInt(ArcanaRegistry.ANCIENT_DOWSING_ROD_CAP),ArcanaConfig.getInt(ArcanaRegistry.ANCIENT_DOWSING_ROD_PER_DEBRIS)*debris.size())); // Add xp
                        SoundUtils.playSound(world, playerEntity.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1f, .5f);
                        
                        if(debris.size() >= 10){
                           ArcanaAchievements.grant(player,ArcanaAchievements.MOTHERLOAD.id);
                        }
                     }else{
                        SoundUtils.playSound(world, playerEntity.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS,1,.5f);
                     }
                  }));
               }
               
            }else{
               playerEntity.sendMessage(Text.literal("Dowsing Rod Recharging: "+(curEnergy*100/getMaxEnergy(item))+"%").formatted(Formatting.GOLD),true);
               SoundUtils.playSongToPlayer(player,SoundEvents.BLOCK_FIRE_EXTINGUISH,1,.5f);
            }
         }
         
         return ActionResult.SUCCESS_SERVER;
      }
   }
}

