package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.sound.Sound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class AncientDowsingRod extends EnergyItem implements UsableItem, TickingItem{
   
   public AncientDowsingRod(){
      id = "ancient_dowsing_rod";
      name = "Ancient Dowsing Rod";
      rarity = MagicRarity.EMPOWERED;
      maxEnergy = 30; // 30 second recharge
   
      ItemStack item = new ItemStack(Items.BLAZE_ROD);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Ancient Dowsing Rod\",\"italic\":false,\"bold\":true,\"color\":\"dark_red\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Ancient civilizations\",\"italic\":false,\"color\":\"gold\"},{\"text\":\" in the \",\"color\":\"red\"},{\"text\":\"nether \",\"color\":\"dark_red\"},{\"text\":\"had ways of finding \",\"color\":\"red\"},{\"text\":\"netherite\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"red\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"This \",\"italic\":false,\"color\":\"red\"},{\"text\":\"dowsing rod \",\"color\":\"dark_red\"},{\"text\":\"is based on \"},{\"text\":\"ancient designs\",\"color\":\"gold\"},{\"text\":\" to locate \"},{\"text\":\"netherite scrap\",\"color\":\"dark_red\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right Click\",\"italic\":false,\"color\":\"gold\"},{\"text\":\" to search for \",\"color\":\"red\"},{\"text\":\"ancient debris\",\"color\":\"dark_red\"},{\"text\":\".\",\"color\":\"red\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Empowered \",\"italic\":false,\"color\":\"green\",\"bold\":true},{\"text\":\"Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
   
      setBookLore(makeLore());
      //setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
   
      item.setNbt(prefNBT);
      prefItem = item;
   
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand){
      ItemStack item = playerEntity.getStackInHand(hand);
      if (playerEntity instanceof ServerPlayerEntity player){
         int curEnergy = getEnergy(item);
         if(curEnergy == maxEnergy){
            setEnergy(item,0);
            final int scanRange = 25;
            BlockPos curBlock = playerEntity.getBlockPos();
            SoundUtils.playSound(world, curBlock, SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1f, .5f);
      
            List<BlockPos> debris = new ArrayList<>();
            for(BlockPos block : BlockPos.iterateOutwards(curBlock,scanRange,scanRange/2,scanRange)){
               if(world.getBlockState(block).getBlock() == Blocks.ANCIENT_DEBRIS){
                  debris.add(new BlockPos(block));
               }
            }
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
               @Override
               public void run() {
                  SoundUtils.playSound(world, playerEntity.getBlockPos(), SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.PLAYERS, 1f, .5f);
               }
            }, 1500);
            timer.schedule(new TimerTask() {
               @Override
               public void run() {
                  int[] locations = new int[8]; // N, NE, E, SE, S, SW, W, NW
                  final double t1 = Math.tan(Math.toRadians(45*3.0/2));
                  final double t2 = Math.tan(Math.toRadians(45/2.0));
                  final Vec3d playerPos = playerEntity.getPos();
            
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
                        if (mod == 6/radius)
                           break;
                        Vec3d parPos = playerPos.add(pPos.multiply(1+mod)).add(0,0.7,0);
                        player.getWorld().spawnParticles(ParticleTypes.DRIPPING_LAVA,parPos.x,parPos.y,parPos.z,15,.12,.12,.12,1);
                     }
                     
                  }
                  if(debris.size() > 0){
                     BlockPos closest = debris.get(0);
                     Vec3d eyePos = playerEntity.getEyePos();
                     Vec3d pPos = eyePos.subtract(closest.getX(),closest.getY(),closest.getZ()).normalize().multiply(-1.5);
                     for(int i = 0; i < 5; i++){
                        Vec3d parPos = eyePos.add(pPos.multiply(1+.5*i));
                        player.getWorld().spawnParticles(ParticleTypes.FLAME,parPos.x,parPos.y,parPos.z,15,.12,.12,.12,0.001);
                     }
   
                     PLAYER_DATA.get(player).addXP(100*debris.size()); // Add xp
                     SoundUtils.playSound(world, playerEntity.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1f, .5f);
                  }else{
                     SoundUtils.playSound(world, playerEntity.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS,1,.5f);
                  }
               }
            }, 7000);
      
         }else{
            playerEntity.sendMessage(Text.translatable("Dowsing Rod Recharging: "+(curEnergy*100/maxEnergy)+"%").formatted(Formatting.GOLD),true);
            SoundUtils.playSongToPlayer(player,SoundEvents.BLOCK_FIRE_EXTINGUISH,1,.5f);
         }
      }
      
      return false;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result){
      return false;
   }
   
   @Override
   public void onTick(ServerWorld world, ServerPlayerEntity player, ItemStack item){
      if(world.getServer().getTicks() % 20 == 0){
         addEnergy(item, 1); // Recharge
      }
   }
   
   private MagicItemRecipe makeRecipe(){
      //TODO make recipe
      return null;
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\" Ancient Dowsing Rod\\n\\nRarity: Empowered\\n\\nModern Piglins seem to be incapable of finding Netherite, but their bastions contain fragments of it.\\nI recovered some pieces of a tool used by their ancestors, perhaps I can reconstruct it.\"}");
      list.add("{\"text\":\" Ancient Dowsing Rod\\n\\nRight click the rod to send out a resonating signal that bounces of nearby Ancient Debris.\\n\\nThe sound's echo triggers a compass of flame to indicate how much debris is nearby. And a flaming arrow to show the nearest pile of debris.\"}");
      return list;
   }
   
   
}
