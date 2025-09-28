package net.borisshoes.arcananovum.items;

import com.google.common.collect.Lists;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.block.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
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
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class PickaxeOfCeptyus extends ArcanaItem {
	public static final String ID = "pickaxe_of_ceptyus";
   
   public static final String CEPTYUS_TICK = "ceptyusPickTick";
   public static final String CEPTYUS_ENERGY = "ceptyusPickEnergy";
   
   public PickaxeOfCeptyus(){
      id = ID;
      name = "Ancient Pickaxe of Ceptyus";
      rarity = ArcanaRarity.DIVINE;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.EQUIPMENT};
      vanillaItem = Items.NETHERITE_PICKAXE;
      item = new PickaxeOfCeptyusItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.DARK_AQUA);
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
      curPrefItem.set(DataComponentTypes.ENCHANTMENTS, MinecraftUtils.makeEnchantComponent(
            new EnchantmentLevelEntry(MinecraftUtils.getEnchantment(server.getRegistryManager(),Enchantments.FORTUNE),5),
            new EnchantmentLevelEntry(MinecraftUtils.getEnchantment(server.getRegistryManager(),Enchantments.EFFICIENCY),5)
      ));
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
      if(!(world instanceof ServerWorld serverWorld)) return;
      Block type = world.getBlockState(pos).getBlock();
      if(!world.getBlockState(pos).isIn(ArcanaRegistry.CEPTYUS_VEIN_MINEABLE)) return;
      
      int veinLevel = Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.WITH_THE_DEPTHS.id));
      int maxDepth = 8 + 2*veinLevel;
      int maxBlocks = 64 + 32*veinLevel;
      
      Queue<Pair<BlockPos, Integer>> queue = Lists.newLinkedList();
      Queue<BlockPos> visited = Lists.newLinkedList();
      queue.add(new Pair<>(pos, 0));
      ArrayList<BlockPos> toMine = new ArrayList<>();
      
      while(!queue.isEmpty()){
         Pair<BlockPos, Integer> pair = queue.poll();
         BlockPos blockPos = pair.getLeft();
         int depth = pair.getRight();
         visited.add(blockPos);
         Block curType = world.getBlockState(blockPos).getBlock();
         
         if(curType == type){
            if(toMine.contains(blockPos)) continue;
            toMine.add(blockPos);
            if(toMine.size() >= maxBlocks) break;
            // Add Surrounding Blocks to Queue
            for(int i = -1; i <= 1; i++){
               for(int j = -1; j <= 1; j++){
                  for(int k = -1; k <= 1; k++){
                     if(!(i==0 && j==0 && k==0) && depth < maxDepth){
                        BlockPos pos2 = blockPos.add(i,j,k);
                        if(queue.stream().noneMatch(p -> p.getLeft().equals(pos2)) && !visited.contains(pos2)){
                           queue.add(new Pair<>(pos2,depth+1));
                        }
                     }
                  }}}
         }
      }
      
      int greedLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.GREED.id));
      final int[] greed = {0,1,3,5};
      
      List<ItemStack> drops = new ArrayList<>();
      ItemStack veinPick = new ItemStack(Items.NETHERITE_PICKAXE);
      veinPick.addEnchantment(MinecraftUtils.getEnchantment(Enchantments.FORTUNE),5 + greed[greedLvl]);
      veinPick.addEnchantment(MinecraftUtils.getEnchantment(Enchantments.UNBREAKING),5);
      
      for(BlockPos blockPos : toMine){
         drops.addAll(Block.getDroppedStacks(world.getBlockState(blockPos), serverWorld, blockPos, null, player, veinPick));
         world.breakBlock(blockPos,false,player);
         if(type instanceof ExperienceDroppingBlock ore){
            ore.onStacksDropped(world.getBlockState(blockPos),serverWorld, pos, veinPick,true);
         }
         if(type instanceof RedstoneOreBlock ore){
            ore.onStacksDropped(world.getBlockState(blockPos),serverWorld, pos, veinPick,true);
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
      list.add(List.of(Text.literal("  Ancient Pickaxe\n     of Ceptyus").formatted(Formatting.DARK_AQUA,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nA curious Divine Artifact found in the deepest parts of the world amongst the rubble of the ancients’ dwellings from a calamity long past. A Pickaxe that was used to carve  ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("  Ancient Pickaxe\n     of Ceptyus").formatted(Formatting.DARK_AQUA,Formatting.BOLD),Text.literal("\nthrough the toughest\ndeepslate like butter, aid in the gathering of obsidian, and harvest fast fortunes from the depths without suffering a single chip.\n\nThis Pickaxe contains  ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("  Ancient Pickaxe\n     of Ceptyus").formatted(Formatting.DARK_AQUA,Formatting.BOLD),Text.literal("\nthe Fortune enchantment pushed beyond its limit to the 5th level, as well as Efficiency imbued with a ramping hast boost that increases as I mine.\n\nThe Pickaxe also mines ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("  Ancient Pickaxe\n     of Ceptyus").formatted(Formatting.DARK_AQUA,Formatting.BOLD),Text.literal("\nall nearby ores of the same type in a single swing, and places the resulting valuables before me.\n\nThe ancients were careful folk, and so, such a noisy ability does not activate while sneaking.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class PickaxeOfCeptyusItem extends ArcanaPolymerItem {
      public PickaxeOfCeptyusItem(){
         super(getThis(),getEquipmentArcanaItemComponents()
               .pickaxe(ToolMaterial.NETHERITE,1,-2.8f)
         );
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot){
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

