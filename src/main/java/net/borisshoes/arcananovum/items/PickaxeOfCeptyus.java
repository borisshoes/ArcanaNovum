package net.borisshoes.arcananovum.items;

import com.google.common.collect.Lists;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.state.BlockState;
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
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.EQUIPMENT};
      vanillaItem = Items.NETHERITE_PICKAXE;
      item = new PickaxeOfCeptyusItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_PICKAXE_OF_CEPTYUS};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public void finalizePrefItem(MinecraftServer server){
      super.finalizePrefItem(server);
      ItemStack curPrefItem = this.getPrefItem();
      curPrefItem.set(DataComponents.ENCHANTMENTS, MinecraftUtils.makeEnchantComponent(
            new EnchantmentInstance(MinecraftUtils.getEnchantment(server.registryAccess(), Enchantments.FORTUNE), 5),
            new EnchantmentInstance(MinecraftUtils.getEnchantment(server.registryAccess(), Enchantments.EFFICIENCY), 5)
      ));
      this.prefItem = buildItemLore(curPrefItem, server);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("A long-lost ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("Pickaxe ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("that could ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("shatter ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("the ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("World").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("An ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("unbreakable ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("Netherite Pickaxe").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal(" with ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("Fortune V").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" and ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("Efficiency V").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GREEN)));
      lore.add(Component.literal("")
            .append(Component.literal("It gives ramping ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("haste ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("and ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("mines ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("whole ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("ore veins").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(". ").withStyle(ChatFormatting.DARK_GREEN)));
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   public void veinMine(Level world, Player player, ItemStack item, BlockPos pos){
      if(!(world instanceof ServerLevel serverWorld)) return;
      Block type = world.getBlockState(pos).getBlock();
      if(!world.getBlockState(pos).is(ArcanaRegistry.CEPTYUS_VEIN_MINEABLE)) return;
      
      int veinLevel = ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.WITH_THE_DEPTHS);
      int maxDepth = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.PICKAXE_OF_CEPTYUS_VEIN_RANGE_PER_LVL).get(veinLevel);
      int maxBlocks = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.PICKAXE_OF_CEPTYUS_VEIN_BLOCKS_PER_LVL).get(veinLevel);
      
      Queue<Tuple<BlockPos, Integer>> queue = Lists.newLinkedList();
      Queue<BlockPos> visited = Lists.newLinkedList();
      queue.add(new Tuple<>(pos, 0));
      ArrayList<BlockPos> toMine = new ArrayList<>();
      
      while(!queue.isEmpty()){
         Tuple<BlockPos, Integer> pair = queue.poll();
         BlockPos blockPos = pair.getA();
         int depth = pair.getB();
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
                     if(!(i == 0 && j == 0 && k == 0) && depth < maxDepth){
                        BlockPos pos2 = blockPos.offset(i, j, k);
                        if(queue.stream().noneMatch(p -> p.getA().equals(pos2)) && !visited.contains(pos2)){
                           queue.add(new Tuple<>(pos2, depth + 1));
                        }
                     }
                  }
               }
            }
         }
      }
      
      int greedLvl = ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.GREED);
      int greed = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.PICKAXE_OF_CEPTYUS_FORTUNE_PER_LVL).get(greedLvl);
      
      List<ItemStack> drops = new ArrayList<>();
      ItemStack veinPick = new ItemStack(Items.NETHERITE_PICKAXE);
      if(EnchantmentHelper.getItemEnchantmentLevel(MinecraftUtils.getEnchantment(Enchantments.SILK_TOUCH), item) <= 0){
         veinPick.enchant(MinecraftUtils.getEnchantment(Enchantments.FORTUNE), 5 + greed);
      }else{
         veinPick.enchant(MinecraftUtils.getEnchantment(Enchantments.SILK_TOUCH), 1);
      }
      veinPick.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
      
      for(BlockPos blockPos : toMine){
         drops.addAll(Block.getDrops(world.getBlockState(blockPos), serverWorld, blockPos, null, player, veinPick));
         world.destroyBlock(blockPos, false, player);
         if(type instanceof DropExperienceBlock ore){
            ore.spawnAfterBreak(world.getBlockState(blockPos), serverWorld, pos, veinPick, true);
         }
         if(type instanceof RedStoneOreBlock ore){
            ore.spawnAfterBreak(world.getBlockState(blockPos), serverWorld, pos, veinPick, true);
         }
         ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_PICKAXE_OF_CEPTYUS_VEIN_MINE_BLOCK));
      }
      for(ItemStack stack : drops){
         Block.popResource(world, pos, stack);
      }
      if(toMine.size() >= 12 && (type == Blocks.DIAMOND_ORE || type == Blocks.DEEPSLATE_DIAMOND_ORE) && player instanceof ServerPlayer serverPlayer)
         ArcanaAchievements.grant(serverPlayer, ArcanaAchievements.MINE_DIAMONDS);
   }
   
   public void mining(ServerPlayer player, ItemStack stack){
      int wardenLvl = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.WARDENS_HASTE);
      int baseGain = ArcanaNovum.CONFIG.getInt(ArcanaConfig.PICKAXE_OF_CEPTYUS_ENERGY_GAIN);
      int extraGain = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.PICKAXE_OF_CEPTYUS_ENERGY_GAIN_PER_LVL).get(wardenLvl);
      int baseMax = ArcanaNovum.CONFIG.getInt(ArcanaConfig.PICKAXE_OF_CEPTYUS_MAX_ENERGY);
      int extraMax = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.PICKAXE_OF_CEPTYUS_MAX_ENERGY_PER_LVL).get(wardenLvl);
      int energyGain = baseGain + extraGain;
      int maxEnergy = baseMax + extraMax;
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      
      profile.addMiscData(CEPTYUS_TICK, IntTag.valueOf(0));
      IntTag energy = (IntTag) profile.getMiscData(CEPTYUS_ENERGY);
      if(energy == null){
         profile.addMiscData(CEPTYUS_ENERGY, IntTag.valueOf(0));
      }else{
         profile.addMiscData(CEPTYUS_ENERGY, IntTag.valueOf(Math.min(energy.intValue() + energyGain, maxEnergy)));
      }
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("  Ancient Pickaxe\n     of Ceptyus").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nA curious Divine Artifact found in the deepest parts of the world amongst the rubble of the ancients’ dwellings from a calamity long past. A Pickaxe that was used to carve  ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Ancient Pickaxe\n     of Ceptyus").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nthrough the toughest\ndeepslate like butter, aid in the gathering of obsidian, and harvest fast fortunes from the depths without suffering a single chip.\n\nThis Pickaxe contains  ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Ancient Pickaxe\n     of Ceptyus").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nthe Fortune enchantment pushed beyond its limit to the 5th level, as well as Efficiency imbued with a ramping hast boost that increases as I mine.\n\nThe Pickaxe also mines ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Ancient Pickaxe\n     of Ceptyus").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nall nearby ores of the same type in a single swing, and places the resulting valuables before me.\n\nThe ancients were careful folk, and so, such a noisy ability does not activate while sneaking.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class PickaxeOfCeptyusItem extends ArcanaPolymerItem {
      public PickaxeOfCeptyusItem(){
         super(getThis(), getEquipmentArcanaItemComponents()
               .pickaxe(ToolMaterial.NETHERITE, 1, -2.8f)
         );
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerLevel serverWorld && entity instanceof ServerPlayer player)) return;
         ArcanaPlayerData profile = ArcanaNovum.data(player);
         IntTag energyEle = (IntTag) profile.getMiscData(CEPTYUS_ENERGY);
         int energy = energyEle == null ? 0 : energyEle.intValue();
         
         if(energy > 0){
            double energyPerHaste = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.PICKAXE_OF_CEPTYUS_ENERGY_PER_HASTE);
            int lastTick = ((IntTag) profile.getMiscData(CEPTYUS_TICK)).intValue();
            int grace = ArcanaNovum.CONFIG.getInt(ArcanaConfig.PICKAXE_OF_CEPTYUS_ENERGY_GRACE);
            if(lastTick > grace){
               int loss = ArcanaNovum.CONFIG.getInt(ArcanaConfig.PICKAXE_OF_CEPTYUS_ENERGY_LOSS);
               profile.addMiscData(CEPTYUS_ENERGY, IntTag.valueOf(Math.max(0, energy - loss)));
            }
            profile.addMiscData(CEPTYUS_TICK, IntTag.valueOf(lastTick + 1));
            int speed = (int) (energy / energyPerHaste);
            MobEffectInstance haste = new MobEffectInstance(MobEffects.HASTE, 100, speed, false, false, false);
            player.addEffect(haste);
            if(speed == 10) ArcanaAchievements.progress(player, ArcanaAchievements.BACK_IN_THE_MINE, 1);
         }
      }
      
      @Override
      public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner){
         if(miner instanceof ServerPlayer player){
            int wardenLvl = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.WARDENS_HASTE);
            int baseGain = ArcanaNovum.CONFIG.getInt(ArcanaConfig.PICKAXE_OF_CEPTYUS_ENERGY_GAIN);
            int extraGain = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.PICKAXE_OF_CEPTYUS_ENERGY_GAIN_PER_LVL).get(wardenLvl);
            int baseMax = ArcanaNovum.CONFIG.getInt(ArcanaConfig.PICKAXE_OF_CEPTYUS_MAX_ENERGY);
            int extraMax = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.PICKAXE_OF_CEPTYUS_MAX_ENERGY_PER_LVL).get(wardenLvl);
            int energyGain = baseGain + extraGain;
            int maxEnergy = baseMax + extraMax;
            ArcanaPlayerData profile = ArcanaNovum.data(player);
            
            profile.addMiscData(CEPTYUS_TICK, IntTag.valueOf(0));
            IntTag energy = (IntTag) profile.getMiscData(CEPTYUS_ENERGY);
            if(energy == null){
               profile.addMiscData(CEPTYUS_ENERGY, IntTag.valueOf(0));
            }else{
               profile.addMiscData(CEPTYUS_ENERGY, IntTag.valueOf(Math.min(energy.intValue() + energyGain, maxEnergy)));
            }
         }
         return super.mineBlock(stack, world, state, pos, miner);
      }
   }
}

