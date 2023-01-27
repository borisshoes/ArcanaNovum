package net.borisshoes.arcananovum.items;

import com.google.common.collect.Lists;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.items.core.LeftClickItem;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.TickingItem;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.block.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class PickaxeOfPluto extends MagicItem implements LeftClickItem, TickingItem {
   public static final ArrayList<Block> VEIN_ORES = new ArrayList<>(Arrays.asList(
         Blocks.COAL_ORE,
         Blocks.DEEPSLATE_COAL_ORE,
         Blocks.IRON_ORE,
         Blocks.DEEPSLATE_IRON_ORE,
         Blocks.COPPER_ORE,
         Blocks.DEEPSLATE_COPPER_ORE,
         Blocks.GOLD_ORE,
         Blocks.DEEPSLATE_GOLD_ORE,
         Blocks.REDSTONE_ORE,
         Blocks.DEEPSLATE_REDSTONE_ORE,
         Blocks.EMERALD_ORE,
         Blocks.DEEPSLATE_EMERALD_ORE,
         Blocks.LAPIS_ORE,
         Blocks.DEEPSLATE_LAPIS_ORE,
         Blocks.DIAMOND_ORE,
         Blocks.DEEPSLATE_DIAMOND_ORE,
         Blocks.NETHER_GOLD_ORE,
         Blocks.NETHER_QUARTZ_ORE,
         Blocks.ANCIENT_DEBRIS,
         Blocks.RAW_COPPER_BLOCK,
         Blocks.RAW_GOLD_BLOCK,
         Blocks.RAW_IRON_BLOCK
   ));
   
   public PickaxeOfPluto(){
      id = "pickaxe_of_pluto";
      name = "Ancient Pickaxe of Pluto";
      rarity = MagicRarity.MYTHICAL;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.MYTHICAL, ArcaneTome.TomeFilter.EQUIPMENT};
   
      ItemStack item = new ItemStack(Items.NETHERITE_PICKAXE);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      NbtCompound fortune = new NbtCompound();
      fortune.putString("id","fortune");
      fortune.putInt("lvl",5);
      NbtCompound efficiency = new NbtCompound();
      efficiency.putString("id","efficiency");
      efficiency.putInt("lvl",5);
      enchants.add(fortune);
      enchants.add(efficiency);
      display.putString("Name","[{\"text\":\"Ancient Pickaxe of Pluto\",\"italic\":false,\"bold\":true,\"color\":\"dark_aqua\"}]");
      loreList.add(NbtString.of("[{\"text\":\"A long lost \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"Pickaxe \",\"color\":\"dark_aqua\"},{\"text\":\"that could \"},{\"text\":\"shatter \",\"color\":\"gold\"},{\"text\":\"the \"},{\"text\":\"World\",\"color\":\"green\"},{\"text\":\".\",\"color\":\"dark_green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"An \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"unbreakable \",\"color\":\"blue\"},{\"text\":\"Netherite Pickaxe\",\"color\":\"dark_red\"},{\"text\":\" with \"},{\"text\":\"Fortune V\",\"color\":\"green\"},{\"text\":\" and \"},{\"text\":\"Efficiency V\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"dark_green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It gives ramping \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"haste \",\"color\":\"gold\"},{\"text\":\"and \"},{\"text\":\"mines \",\"color\":\"dark_aqua\"},{\"text\":\"whole \"},{\"text\":\"ore veins\",\"color\":\"green\"},{\"text\":\". \"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Mythical\",\"italic\":false,\"color\":\"light_purple\",\"bold\":true},{\"text\":\" Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("HideFlags",7);
      tag.putInt("Unbreakable",1);
   
      setBookLore(makeLore());
      prefNBT = addMagicNbt(tag);
      item.setNbt(prefNBT);
      prefItem = item;
   
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack){
      NbtCompound itemNbt = stack.getNbt();
      NbtList enchants = itemNbt.getList("Enchantments", NbtElement.COMPOUND_TYPE);
      NbtCompound newTag = super.updateItem(stack).getNbt();
      if(enchants != null) newTag.put("Enchantments", enchants);
      stack.setNbt(newTag);
      return stack;
   }
   
   @Override
   public boolean attackBlock(PlayerEntity playerEntity, World world, Hand hand, BlockPos blockPos, Direction direction){
      IArcanaProfileComponent profile = PLAYER_DATA.get(playerEntity);
      profile.addMiscData("plutoPickTick", NbtInt.of(0));
      NbtInt energy = (NbtInt) profile.getMiscData("plutoPickEnergy");
      if(energy == null){
         profile.addMiscData("plutoPickEnergy", NbtInt.of(0));
      }else{
         profile.addMiscData("plutoPickEnergy", NbtInt.of(Math.min(energy.intValue()+16,1000)));
      }
      return true;
   }
   
   @Override
   public void onTick(ServerWorld world, ServerPlayerEntity player, ItemStack item){
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      NbtInt energyEle = (NbtInt) profile.getMiscData("plutoPickEnergy");
      int energy = energyEle == null ? 0 : energyEle.intValue();
      
      if(energy > 0){
         int lastTick = ((NbtInt)profile.getMiscData("plutoPickTick")).intValue();
         profile.addMiscData("plutoPickTick", NbtInt.of(lastTick+1));
         profile.addMiscData("plutoPickEnergy", NbtInt.of(Math.max(0,energy-lastTick/3)));
         int speed = energy / 100;

         //System.out.println("Last Tick: "+lastTick + " | Cur energy: "+energy+" | Haste Amp: "+speed);
         StatusEffectInstance haste = new StatusEffectInstance(StatusEffects.HASTE, 20, speed, false, false, false);
         if(speed == 10) ArcanaAchievements.progress(player,"back_in_the_mine",1);
         player.addStatusEffect(haste);
      }
   }
   
   public void veinMine(World world, PlayerEntity player, ItemStack item, BlockPos pos){
      Block type = world.getBlockState(pos).getBlock();
      if(!VEIN_ORES.contains(type)) return;
   
      int maxDepth = 8;
      int maxBlocks = 64;
   
      Queue<Pair<BlockPos, Integer>> queue = Lists.newLinkedList();
      Queue<BlockPos> visited = Lists.newLinkedList();
      queue.add(new Pair(pos, 0));
      ArrayList<BlockPos> toMine = new ArrayList<>();
   
      while(!queue.isEmpty()) {
         Pair<BlockPos, Integer> pair = (Pair)queue.poll();
         BlockPos blockPos = (BlockPos)pair.getLeft();
         int depth = (Integer)pair.getRight();
         visited.add(blockPos);
         Block curType = world.getBlockState(blockPos).getBlock();
   
         if(curType == type){
            if(toMine.contains(blockPos)) continue;
            toMine.add(blockPos);
            //System.out.println("Found Matching Block At: "+blockPos.toShortString());
            if(toMine.size() >= maxBlocks) break;
            // Add Surrounding Blocks to Queue
            for(int i = -1; i <= 1; i++){
            for(int j = -1; j <= 1; j++){
            for(int k = -1; k <= 1; k++){
               if(!(i==0 && j==0 && k==0) && depth < maxDepth){
                  BlockPos pos2 = blockPos.add(i,j,k);
                  if(!queue.contains(pos2) && !visited.contains(pos2)){
                     queue.add(new Pair(pos2,depth+1));
                  }
               }
            }}}
         }
      }
   
      List<ItemStack> drops = new ArrayList<>();
      for(BlockPos blockPos : toMine){
         drops.addAll(Block.getDroppedStacks(world.getBlockState(blockPos), (ServerWorld)world, blockPos, null, player, player.getMainHandStack()));
         world.breakBlock(blockPos,false,player);
         if(type instanceof OreBlock ore){
            ore.onStacksDropped(world.getBlockState(blockPos),(ServerWorld)world, pos, player.getMainHandStack(),true);
         }
         if(type instanceof RedstoneOreBlock ore){
            ore.onStacksDropped(world.getBlockState(blockPos),(ServerWorld)world, pos, player.getMainHandStack(),true);
         }
         PLAYER_DATA.get(player).addXP(5);
      }
      for(ItemStack stack : drops){
         Block.dropStack(world, pos, stack);
      }
      if(toMine.size() >= 12 && (type == Blocks.DIAMOND_ORE || type == Blocks.DEEPSLATE_DIAMOND_ORE) && player instanceof ServerPlayerEntity serverPlayer) ArcanaAchievements.grant(serverPlayer,"mine_diamonds");
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"    Ancient Pickaxe\\n         Of Pluto\\n\\nRarity: Mythical\\n\\nThe third discovered Mythical Artifact left by the Gods.\\n\\nFound in the deepest parts of the world amongst the rubble of the ancients' dwellings from a calamity long \"}");
      list.add("{\"text\":\"    Ancient Pickaxe\\n         Of Pluto\\n\\npast. A pickaxe that was used to carve through the toughest deepslate like butter, aid in gathering obsidian and harvest vast fortunes from the Earth without suffering even a chip.\\n\"}");
      list.add("{\"text\":\"    Ancient Pickaxe\\n         Of Pluto\\n\\nThis unbreakable pickaxe contains the fortune enchantment pushed beyond its limit to the 5th level, as well as efficiency 5 that has been embued with a ramping haste boost that increases as you mine. The pickaxe also mines all\"}");
      list.add("{\"text\":\"    Ancient Pickaxe\\n         Of Pluto\\n\\nnearby ores of the same type and places their contents before you. This does not activate while sneaking.\\n\\n\"}");
      return list;
   }
}
