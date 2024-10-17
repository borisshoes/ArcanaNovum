package net.borisshoes.arcananovum.items;

import com.google.common.collect.Lists;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerPickaxeItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.block.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterials;
import net.minecraft.nbt.NbtInt;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class PickaxeOfCeptyus extends ArcanaItem {
	public static final String ID = "pickaxe_of_ceptyus";
   
   public static final String CEPTYUS_TICK = "ceptyusPickTick";
   public static final String CEPTYUS_ENERGY = "ceptyusPickEnergy";
   
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
      id = ID;
      name = "Ancient Pickaxe of Ceptyus";
      rarity = ArcanaRarity.DIVINE;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.DIVINE, TomeGui.TomeFilter.EQUIPMENT};
      vanillaItem = Items.NETHERITE_PICKAXE;
      item = new PickaxeOfCeptyusItem(new Item.Settings().maxCount(1).fireproof().maxDamage(1024)
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.DARK_AQUA))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .component(DataComponentTypes.UNBREAKABLE,new UnbreakableComponent(false))
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_PICKAXE_OF_CEPTYUS};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public void finalizePrefItem(MinecraftServer server){
      super.finalizePrefItem(server);
      ItemStack curPrefItem = this.getPrefItem();
      curPrefItem.set(DataComponentTypes.ENCHANTMENTS, MiscUtils.makeEnchantComponent(
            new EnchantmentLevelEntry(MiscUtils.getEnchantment(server.getRegistryManager(),Enchantments.FORTUNE),5),
            new EnchantmentLevelEntry(MiscUtils.getEnchantment(server.getRegistryManager(),Enchantments.EFFICIENCY),5)
      ).withShowInTooltip(false));
      this.prefItem = buildItemLore(curPrefItem, server);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("A long-lost ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("Pickaxe ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("that could ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("shatter ").formatted(Formatting.GOLD))
            .append(Text.literal("the ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("World").formatted(Formatting.GREEN))
            .append(Text.literal(".").formatted(Formatting.DARK_GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("An ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("unbreakable ").formatted(Formatting.BLUE))
            .append(Text.literal("Netherite Pickaxe").formatted(Formatting.DARK_RED))
            .append(Text.literal(" with ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("Fortune V").formatted(Formatting.GREEN))
            .append(Text.literal(" and ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("Efficiency V").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.DARK_GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("It gives ramping ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("haste ").formatted(Formatting.GOLD))
            .append(Text.literal("and ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("mines ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("whole ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("ore veins").formatted(Formatting.GREEN))
            .append(Text.literal(". ").formatted(Formatting.DARK_GREEN)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
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
      veinPick.addEnchantment(MiscUtils.getEnchantment(Enchantments.FORTUNE),5 + greed[greedLvl]);
      veinPick.addEnchantment(MiscUtils.getEnchantment(Enchantments.UNBREAKING),5);
      
      for(BlockPos blockPos : toMine){
         drops.addAll(Block.getDroppedStacks(world.getBlockState(blockPos), (ServerWorld)world, blockPos, null, player, veinPick));
         world.breakBlock(blockPos,false,player);
         if(type instanceof ExperienceDroppingBlock ore){
            ore.onStacksDropped(world.getBlockState(blockPos),(ServerWorld)world, pos, veinPick,true);
         }
         if(type instanceof RedstoneOreBlock ore){
            ore.onStacksDropped(world.getBlockState(blockPos),(ServerWorld)world, pos, veinPick,true);
         }
         ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.PICKAXE_OF_CEPTYUS_VEIN_MINE_BLOCK));
      }
      for(ItemStack stack : drops){
         Block.dropStack(world, pos, stack);
      }
      if(toMine.size() >= 12 && (type == Blocks.DIAMOND_ORE || type == Blocks.DEEPSLATE_DIAMOND_ORE) && player instanceof ServerPlayerEntity serverPlayer) ArcanaAchievements.grant(serverPlayer,ArcanaAchievements.MINE_DIAMONDS.id);
   }
   
   public void mining(ServerPlayerEntity player, ItemStack stack){
      int wHaste = Math.max(0, ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.WARDENS_HASTE.id));
      int energyGain = 5+(wHaste * 2);
      int maxEnergy = 1000 + 100 * wHaste;
      IArcanaProfileComponent profile = ArcanaNovum.data(player);
      
      profile.addMiscData(CEPTYUS_TICK, NbtInt.of(0));
      NbtInt energy = (NbtInt) profile.getMiscData(CEPTYUS_ENERGY);
      if(energy == null){
         profile.addMiscData(CEPTYUS_ENERGY, NbtInt.of(0));
      }else{
         profile.addMiscData(CEPTYUS_ENERGY, NbtInt.of(Math.min(energy.intValue() + energyGain, maxEnergy)));
      }
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("   Ancient Pickaxe\n       Of Ceptyus\n\nRarity: Divine\n\nThe third discovered Divine Artifact left by the Gods.\n\nFound in the deepest parts of the world amongst the rubble of the ancients' dwellings from a calamity long ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Ancient Pickaxe\n       Of Ceptyus\n\npast. A pickaxe that was used to carve through the toughest deepslate like butter, aid in gathering obsidian and harvest vast fortunes from the Earth without suffering even a chip.\n").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Ancient Pickaxe\n       Of Ceptyus\n\nThis unbreakable pickaxe contains the fortune enchantment pushed beyond its limit to the 5th level, as well as efficiency 5 that has been embued with a ramping haste boost that increases as you mine. The pickaxe also mines all").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Ancient Pickaxe\n       Of Ceptyus\n\nnearby ores of the same type and places their contents before you. This does not activate while sneaking.\n\n").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class PickaxeOfCeptyusItem extends ArcanaPolymerPickaxeItem {
      public PickaxeOfCeptyusItem(Item.Settings settings){
         super(getThis(),ToolMaterials.NETHERITE,1,-2.8f,settings);
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
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld serverWorld && entity instanceof ServerPlayerEntity player)) return;
         IArcanaProfileComponent profile = ArcanaNovum.data(player);
         NbtInt energyEle = (NbtInt) profile.getMiscData(CEPTYUS_ENERGY);
         int energy = energyEle == null ? 0 : energyEle.intValue();
         
         if(energy > 0){
            int lastTick = ((NbtInt)profile.getMiscData(CEPTYUS_TICK)).intValue();
            profile.addMiscData(CEPTYUS_TICK, NbtInt.of(lastTick+1));
            profile.addMiscData(CEPTYUS_ENERGY, NbtInt.of(Math.max(0,energy-lastTick/3)));
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
            int energyGain = 3 + Math.max(0, ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.WARDENS_HASTE.id));
            int maxEnergy = 1000 + 100 * Math.max(0, ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.WARDENS_HASTE.id));
            IArcanaProfileComponent profile = ArcanaNovum.data(player);
            
            profile.addMiscData(CEPTYUS_TICK, NbtInt.of(0));
            NbtInt energy = (NbtInt) profile.getMiscData(CEPTYUS_ENERGY);
            if(energy == null){
               profile.addMiscData(CEPTYUS_ENERGY, NbtInt.of(0));
            }else{
               profile.addMiscData(CEPTYUS_ENERGY, NbtInt.of(Math.min(energy.intValue() + energyGain, maxEnergy)));
            }
         }
         return super.postMine(stack, world, state, pos, miner);
      }
   }
}

