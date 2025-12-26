package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
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
      item = new AncientDowsingRodItem();
      displayName =  Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED);
      researchTasks = new ResourceKey[]{ResearchTasks.RESONATE_BELL,ResearchTasks.ADVANCEMENT_OBTAIN_ANCIENT_DEBRIS,ResearchTasks.ADVANCEMENT_FIND_BASTION};
      initEnergy = 100;
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("Ancient civilizations").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" in the ").withStyle(ChatFormatting.RED))
            .append(Component.literal("nether ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("had ways of finding ").withStyle(ChatFormatting.RED))
            .append(Component.literal("netherite").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(".").withStyle(ChatFormatting.RED)));
      lore.add(Component.literal("")
            .append(Component.literal("This ").withStyle(ChatFormatting.RED))
            .append(Component.literal("dowsing rod ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("is based on ").withStyle(ChatFormatting.RED))
            .append(Component.literal("ancient designs").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" to locate ").withStyle(ChatFormatting.RED))
            .append(Component.literal("netherite scrap").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(".").withStyle(ChatFormatting.RED)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" to search for ").withStyle(ChatFormatting.RED))
            .append(Component.literal("ancient debris").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(".").withStyle(ChatFormatting.RED)));
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
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal(" Ancient Dowsing\n         Rod").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nModern Piglins seem to be incapable of finding Netherite, but their bastions contain fragments of it, and the smithing templates to forge it. There may be some history at play here.   ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Ancient Dowsing\n         Rod").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), Component.literal("\nI recovered some pieces of a tool, possibly used by their ancestors, perhaps I can reconstruct it.\n\nUse the rod to send out a resonating signal that reflects off Ancient Debris. ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Ancient Dowsing\n         Rod").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), Component.literal("\nThe echos trigger a compass of flame to indicate how much debris is nearby.\n\nAdditionally, a flaming arrow points to the closest debris nearby.\n").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class AncientDowsingRodItem extends ArcanaPolymerItem {
      public AncientDowsingRodItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         List<String> stringList = new ArrayList<>();
         if(getEnergy(itemStack) < getMaxEnergy(itemStack)){
            stringList.add("cooldown");
         }else{
            stringList.add("charged");
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
         if(!(world instanceof ServerLevel)) return;
         if(world.getServer().getTickCount() % 20 == 0){
            addEnergy(stack, 1); // Recharge
         }
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         ItemStack item = playerEntity.getItemInHand(hand);
         if(playerEntity instanceof ServerPlayer player){
            int curEnergy = getEnergy(item);
            if(curEnergy >= getMaxEnergy(item)){
               setEnergy(item,0);
               final int scanRange = 25 + 5*Math.max(0,ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.ENHANCED_RESONANCE.id));
               BlockPos curBlock = playerEntity.blockPosition();
               SoundUtils.playSound(world, curBlock, SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1f, .5f);
               
               List<BlockPos> debris = new ArrayList<>();
               for(BlockPos block : BlockPos.withinManhattan(curBlock,scanRange,scanRange/2,scanRange)){
                  if(world.getBlockState(block).getBlock() == Blocks.ANCIENT_DEBRIS){
                     debris.add(new BlockPos(block));
                  }
               }
               if(world instanceof ServerLevel serverWorld){
                  if(!debris.isEmpty()) ArcanaAchievements.progress(player,ArcanaAchievements.ARCHEOLOGIST.id,debris.size());
                  
                  BorisLib.addTickTimerCallback(new GenericTimer(30, () -> SoundUtils.playSound(world, playerEntity.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1f, .5f)));
                  BorisLib.addTickTimerCallback(new GenericTimer(140, () -> {
                     int[] locations = new int[8]; // N, NE, E, SE, S, SW, W, NW
                     final double t1 = Math.tan(Math.toRadians(45*3.0/2));
                     final double t2 = Math.tan(Math.toRadians(45/2.0));
                     final Vec3 playerPos = playerEntity.position();
                     int count = 0;
                     
                     for(BlockPos b : debris){
                        Vec3 rPos = new Vec3( b.getX() - playerPos.x, b.getY() - playerPos.y,b.getZ() - playerPos.z);
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
                           ArcanaEffectUtils.dowsingRodEmitter(serverWorld,new Vec3(b.getX(),b.getY(),b.getZ()),1,100 + 33*Math.max(0,ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.HARMONIC_FEEDBACK.id)));
                        count++;
                     }
                     double radius = 1.5;
                     for(int i = 0; i < locations.length; i++){
                        Vec3 pPos = switch(i){
                           case 0 -> new Vec3(-radius, 0, 0);
                           case 1 -> new Vec3(-radius, 0, radius);
                           case 2 -> new Vec3(0, 0, radius);
                           case 3 -> new Vec3(radius, 0, radius);
                           case 4 -> new Vec3(radius, 0, 0);
                           case 5 -> new Vec3(radius, 0, -radius);
                           case 6 -> new Vec3(0, 0, -radius);
                           case 7 -> new Vec3(-radius, 0, -radius);
                           default -> new Vec3(0,0,0);
                        };
                        for(int n = 0; n < locations[i]; n++){
                           double mod = Math.min(n*.6 , 6/radius);
                           if(mod == 6/radius)
                              break;
                           Vec3 parPos = playerPos.add(pPos.scale(1+mod)).add(0,0.7,0);
                           player.level().sendParticles(ParticleTypes.DRIPPING_LAVA,parPos.x,parPos.y,parPos.z,15,.12,.12,.12,1);
                        }
                        
                     }
                     if(!debris.isEmpty()){
                        BlockPos closest = debris.get(0);
                        Vec3 eyePos = playerEntity.getEyePosition();
                        Vec3 blockPos = new Vec3(closest.getX()+.5,closest.getY()+0.5,closest.getZ()+0.5);
                        Vec3 start = eyePos.add(blockPos.subtract(eyePos).normalize().scale(1.5));
                        Vec3 end = eyePos.add(blockPos.subtract(eyePos).normalize().scale(1.5+3));
                        ArcanaEffectUtils.dowsingRodArrow(player.level(),start,end,1);
                        
                        ArcanaNovum.data(player).addXP(Math.min(ArcanaConfig.getInt(ArcanaRegistry.ANCIENT_DOWSING_ROD_CAP),ArcanaConfig.getInt(ArcanaRegistry.ANCIENT_DOWSING_ROD_PER_DEBRIS)*debris.size())); // Add xp
                        SoundUtils.playSound(world, playerEntity.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1f, .5f);
                        
                        if(debris.size() >= 10){
                           ArcanaAchievements.grant(player,ArcanaAchievements.MOTHERLOAD.id);
                        }
                     }else{
                        SoundUtils.playSound(world, playerEntity.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS,1,.5f);
                     }
                  }));
               }
               
            }else{
               playerEntity.displayClientMessage(Component.literal("Dowsing Rod Recharging: "+(curEnergy*100/getMaxEnergy(item))+"%").withStyle(ChatFormatting.GOLD),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH,1,.5f);
            }
         }
         
         return InteractionResult.SUCCESS_SERVER;
      }
   }
}

