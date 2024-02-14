package net.borisshoes.arcananovum.items;

import com.google.common.collect.Lists;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerPickaxeItem;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterials;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class PickaxeOfCeptyus extends MagicItem {
   
   private static final String TXT = "item/pickaxe_of_ceptyus";
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
         Blocks.RAW_IRON_BLOCK,
         Blocks.AMETHYST_CLUSTER
   ));
   
   public PickaxeOfCeptyus(){
      id = "pickaxe_of_ceptyus";
      name = "Ancient Pickaxe of Ceptyus";
      rarity = MagicRarity.MYTHICAL;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.MYTHICAL, ArcaneTome.TomeFilter.EQUIPMENT};
      vanillaItem = Items.NETHERITE_PICKAXE;
      item = new PickaxeOfCeptyusItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
   
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      NbtCompound fortune = new NbtCompound();
      fortune.putString("id","fortune");
      fortune.putInt("lvl",5);
      NbtCompound efficiency = new NbtCompound();
      efficiency.putString("id","efficiency");
      efficiency.putInt("lvl",5);
      enchants.add(fortune);
      enchants.add(efficiency);
      display.putString("Name","[{\"text\":\"Ancient Pickaxe of Ceptyus\",\"italic\":false,\"bold\":true,\"color\":\"dark_aqua\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("HideFlags", 255);
      tag.putInt("Unbreakable",1);
      
      setBookLore(makeLore());
      stack.setNbt(addMagicNbt(tag));
      setPrefStack(stack);
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"A long-lost \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"Pickaxe \",\"color\":\"dark_aqua\"},{\"text\":\"that could \"},{\"text\":\"shatter \",\"color\":\"gold\"},{\"text\":\"the \"},{\"text\":\"World\",\"color\":\"green\"},{\"text\":\".\",\"color\":\"dark_green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"An \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"unbreakable \",\"color\":\"blue\"},{\"text\":\"Netherite Pickaxe\",\"color\":\"dark_red\"},{\"text\":\" with \"},{\"text\":\"Fortune V\",\"color\":\"green\"},{\"text\":\" and \"},{\"text\":\"Efficiency V\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"dark_green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It gives ramping \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"haste \",\"color\":\"gold\"},{\"text\":\"and \"},{\"text\":\"mines \",\"color\":\"dark_aqua\"},{\"text\":\"whole \"},{\"text\":\"ore veins\",\"color\":\"green\"},{\"text\":\". \"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      return loreList;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtList enchants = itemNbt.getList("Enchantments", NbtElement.COMPOUND_TYPE);
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      if(enchants != null) newTag.put("Enchantments", enchants);
      stack.setNbt(newTag);
      return buildItemLore(stack,server);
   }
   
   public void veinMine(World world, PlayerEntity player, ItemStack item, BlockPos pos){
      Block type = world.getBlockState(pos).getBlock();
      if(!VEIN_ORES.contains(type)) return;
   
      int veinLevel = Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.WITH_THE_DEPTHS.id));
      int maxDepth = 8 + 2*veinLevel;
      int maxBlocks = 64 + 32*veinLevel;
   
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
   
      int greedLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.GREED.id));
      final int[] greed = {0,1,3,5};
      
      List<ItemStack> drops = new ArrayList<>();
      ItemStack veinPick = new ItemStack(Items.NETHERITE_PICKAXE);
      NbtCompound tag = veinPick.getOrCreateNbt();
      NbtList enchants = new NbtList();
      NbtCompound fortune = new NbtCompound();
      fortune.putString("id","fortune");
      fortune.putInt("lvl",5 + greed[greedLvl]);
      enchants.add(fortune);
      tag.put("Enchantments",enchants);
      tag.putInt("Unbreakable",1);
      
      for(BlockPos blockPos : toMine){
         drops.addAll(Block.getDroppedStacks(world.getBlockState(blockPos), (ServerWorld)world, blockPos, null, player, veinPick));
         world.breakBlock(blockPos,false,player);
         if(type instanceof ExperienceDroppingBlock ore){
            ore.onStacksDropped(world.getBlockState(blockPos),(ServerWorld)world, pos, veinPick,true);
         }
         if(type instanceof RedstoneOreBlock ore){
            ore.onStacksDropped(world.getBlockState(blockPos),(ServerWorld)world, pos, veinPick,true);
         }
         PLAYER_DATA.get(player).addXP(5);
      }
      for(ItemStack stack : drops){
         Block.dropStack(world, pos, stack);
      }
      if(toMine.size() >= 12 && (type == Blocks.DIAMOND_ORE || type == Blocks.DEEPSLATE_DIAMOND_ORE) && player instanceof ServerPlayerEntity serverPlayer) ArcanaAchievements.grant(serverPlayer,ArcanaAchievements.MINE_DIAMONDS.id);
   }
   
   public void mining(ServerPlayerEntity player, ItemStack stack){
      int wHaste = Math.max(0, ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.WARDENS_HASTE.id));
      int energyGain = Math.random() < new double[]{0.2,0.4,0.5,0.6,0.8,1}[wHaste] ? 1+(wHaste/2) : 0;
      int maxEnergy = 1000 + 100 * wHaste;
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      
      profile.addMiscData("ceptyusPickTick", NbtInt.of(0));
      NbtInt energy = (NbtInt) profile.getMiscData("ceptyusPickEnergy");
      if(energy == null){
         profile.addMiscData("ceptyusPickEnergy", NbtInt.of(0));
      }else{
         profile.addMiscData("ceptyusPickEnergy", NbtInt.of(Math.min(energy.intValue() + energyGain, maxEnergy)));
      }
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"   Ancient Pickaxe\\n       Of Ceptyus\\n\\nRarity: Mythical\\n\\nThe third discovered Mythical Artifact left by the Gods.\\n\\nFound in the deepest parts of the world amongst the rubble of the ancients' dwellings from a calamity long \"}");
      list.add("{\"text\":\"   Ancient Pickaxe\\n       Of Ceptyus\\n\\npast. A pickaxe that was used to carve through the toughest deepslate like butter, aid in gathering obsidian and harvest vast fortunes from the Earth without suffering even a chip.\\n\"}");
      list.add("{\"text\":\"   Ancient Pickaxe\\n       Of Ceptyus\\n\\nThis unbreakable pickaxe contains the fortune enchantment pushed beyond its limit to the 5th level, as well as efficiency 5 that has been embued with a ramping haste boost that increases as you mine. The pickaxe also mines all\"}");
      list.add("{\"text\":\"   Ancient Pickaxe\\n       Of Ceptyus\\n\\nnearby ores of the same type and places their contents before you. This does not activate while sneaking.\\n\\n\"}");
      return list;
   }
   
   public class PickaxeOfCeptyusItem extends MagicPolymerPickaxeItem {
      public PickaxeOfCeptyusItem(Settings settings){
         super(getThis(),ToolMaterials.NETHERITE,1,-2.8f,settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.MODELS.get(TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         if(!(world instanceof ServerWorld serverWorld && entity instanceof ServerPlayerEntity player)) return;
         IArcanaProfileComponent profile = PLAYER_DATA.get(player);
         NbtInt energyEle = (NbtInt) profile.getMiscData("ceptyusPickEnergy");
         int energy = energyEle == null ? 0 : energyEle.intValue();
         
         if(energy > 0){
            int lastTick = ((NbtInt)profile.getMiscData("ceptyusPickTick")).intValue();
            profile.addMiscData("ceptyusPickTick", NbtInt.of(lastTick+1));
            profile.addMiscData("ceptyusPickEnergy", NbtInt.of(Math.max(0,energy-lastTick/3)));
            int speed = energy / 100;
            
            //System.out.println("Last Tick: "+lastTick + " | Cur energy: "+energy+" | Haste Amp: "+speed);
            StatusEffectInstance haste = new StatusEffectInstance(StatusEffects.HASTE, 100, speed, false, false, false);
            player.addStatusEffect(haste);
            if(speed == 10) ArcanaAchievements.progress(player,ArcanaAchievements.BACK_IN_THE_MINE.id,1);
         }
      }
      
      @Override
      public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner){
         if(miner instanceof ServerPlayerEntity player){
            int energyGain = 6 + 2 * Math.max(0, ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.WARDENS_HASTE.id));
            int maxEnergy = 1000 + 100 * Math.max(0, ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.WARDENS_HASTE.id));
            IArcanaProfileComponent profile = PLAYER_DATA.get(player);
            
            profile.addMiscData("ceptyusPickTick", NbtInt.of(0));
            NbtInt energy = (NbtInt) profile.getMiscData("ceptyusPickEnergy");
            if(energy == null){
               profile.addMiscData("ceptyusPickEnergy", NbtInt.of(0));
            }else{
               profile.addMiscData("ceptyusPickEnergy", NbtInt.of(Math.min(energy.intValue() + energyGain, maxEnergy)));
            }
         }
         return super.postMine(stack, world, state, pos, miner);
      }
   }
}
