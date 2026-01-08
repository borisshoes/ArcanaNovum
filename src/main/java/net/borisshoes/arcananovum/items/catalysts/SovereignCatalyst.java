package net.borisshoes.arcananovum.items.catalysts;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.entities.NulConstructEntity;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.entities.NulConstructEntity.getBaseConstructPattern;

public class SovereignCatalyst extends ArcanaItem {
	public static final String ID = "sovereign_catalyst";
   
   public SovereignCatalyst(){
      id = ID;
      name = "Sovereign Augment Catalyst";
      rarity = ArcanaRarity.MUNDANE;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.CATALYSTS};
      vanillaItem = Items.GOLD_INGOT;
      item = new SovereignCatalystItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_EXOTIC_CATALYST,ResearchTasks.OBTAIN_GOLD_INGOT,ResearchTasks.UNLOCK_TWILIGHT_ANVIL};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("Augment ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Catalysts").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" can be used to ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("augment ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("your ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Arcana Items").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Augments ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("require more ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("powerful ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("Catalysts ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("at higher levels").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Apply ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("these ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Catalysts ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("in the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Tinkering Menu").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" of a ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("Twilight Anvil").withStyle(ChatFormatting.GREEN)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("     Sovereign\n   Augmentation\n      Catalyst").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nGOLD! The gemstones already provide enough reinforcement. Gold lets the energy be more malleable to more creative applications. But, I ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("     Sovereign\n   Augmentation\n      Catalyst").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nthink there’s a little more potential here…").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class SovereignCatalystItem extends ArcanaPolymerItem {
      public SovereignCatalystItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public InteractionResult useOn(UseOnContext context){
         Level world = context.getLevel();
         Player playerEntity = context.getPlayer();
         BlockPos pos = context.getClickedPos();
         BlockState state = world.getBlockState(pos);
         boolean canSpawn = world.getDifficulty() != Difficulty.PEACEFUL;
         if(canSpawn && playerEntity instanceof ServerPlayer serverPlayer){
            canSpawn = ArcanaNovum.data(serverPlayer).hasResearched(ArcanaRegistry.DIVINE_CATALYST);
         }
         
         if(state.is(Blocks.ANCIENT_DEBRIS) && pos.getY() >= world.getMinY() && canSpawn){ // Check construct
            BlockPattern pattern = getBaseConstructPattern();
            BlockPattern.BlockPatternMatch patternResult = pattern.find(world, pos.offset(-1,-1,-1));
            if(patternResult != null){
               NulConstructEntity constructEntity = (NulConstructEntity) ArcanaRegistry.NUL_CONSTRUCT_ENTITY.create(world, EntitySpawnReason.TRIGGERED);
               if(constructEntity != null && world instanceof ServerLevel serverWorld){
                  CarvedPumpkinBlock.clearPatternBlocks(world, patternResult);
                  BlockPos blockPos = patternResult.getBlock(1, 1, 0).getPos();
                  constructEntity.snapTo((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.55, (double)blockPos.getZ() + 0.5, patternResult.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F, 0.0F);
                  constructEntity.yBodyRot = patternResult.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F;
                  constructEntity.onSummoned(playerEntity);
                  
                  world.addFreshEntity(constructEntity);
                  CarvedPumpkinBlock.updatePatternBlocks(world, patternResult);
                  
                  if(playerEntity instanceof ServerPlayer player){
                     ArcanaAchievements.grant(player,ArcanaAchievements.DOOR_OF_DIVINITY.id);
                  }
                  
                  context.getItemInHand().shrink(1);
               }
               return InteractionResult.SUCCESS_SERVER;
            }
         }
         return InteractionResult.PASS;
      }
   }
}

