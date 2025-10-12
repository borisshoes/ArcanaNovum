package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.AquaticEversource;
import net.borisshoes.arcananovum.items.MagmaticEversource;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(CauldronBehavior.class)
public interface CauldronBehaviorMixin {
   
   @Inject(method = "registerBehavior", at = @At("TAIL"))
   private static void arcananovum$cauldronInteractions(CallbackInfo ci, @Local(ordinal = 0) Map<Item, CauldronBehavior> emptyCauldronMap, @Local(ordinal = 1) Map<Item, CauldronBehavior> waterCauldronMap, @Local(ordinal = 2) Map<Item, CauldronBehavior> lavaCauldronMap, @Local(ordinal = 3) Map<Item, CauldronBehavior> snowCauldronMap){
      
      // Dyed Item Washing
      final CauldronBehavior CLEAN_DYEABLE_ITEM = (state, world, pos, player, hand, stack) -> {
         if(!stack.isIn(ItemTags.DYEABLE)){
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         } else if(!stack.contains(DataComponentTypes.DYED_COLOR)){
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         }else{
            if(!world.isClient){
               stack.remove(DataComponentTypes.DYED_COLOR);
               player.incrementStat(Stats.CLEAN_ARMOR);
               LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
            }
            return ActionResult.SUCCESS_SERVER;
         }
      };
      
      waterCauldronMap.put(ArcanaRegistry.LEVITATION_HARNESS.getItem(), CLEAN_DYEABLE_ITEM);
      waterCauldronMap.put(ArcanaRegistry.SOJOURNER_BOOTS.getItem(), CLEAN_DYEABLE_ITEM);
      
      // Shield Washing
      waterCauldronMap.put(ArcanaRegistry.SHIELD_OF_FORTITUDE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!stack.contains(DataComponentTypes.BANNER_PATTERNS) && !stack.contains(DataComponentTypes.BASE_COLOR)){
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         }else{
            if(!world.isClient){
               stack.remove(DataComponentTypes.BANNER_PATTERNS);
               stack.remove(DataComponentTypes.BASE_COLOR);
               LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
            }
            return ActionResult.SUCCESS_SERVER;
         }
      });
      
      // Eversources
      emptyCauldronMap.put(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         if(!(ArcanaItemUtils.identifyItem(stack) instanceof AquaticEversource eversource)) return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         int mode = ArcanaItem.getIntProperty(stack,AquaticEversource.MODE_TAG);
         if(mode == 1){
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         }
         if(!world.isClient){
            player.incrementStat(Stats.USE_CAULDRON);
            world.setBlockState(pos, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
            world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
            serverPlayer.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.AQUATIC_EVERSOURCE_USE)); // Add xp
            ArcanaAchievements.progress(serverPlayer,ArcanaAchievements.POCKET_OCEAN.id,1);
         }
         return ActionResult.SUCCESS_SERVER;
      });
      
      waterCauldronMap.put(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         if(!(ArcanaItemUtils.identifyItem(stack) instanceof AquaticEversource eversource)) return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         int mode = ArcanaItem.getIntProperty(stack,AquaticEversource.MODE_TAG);
         if(!world.isClient){
            if(mode == 1){
               player.incrementStat(Stats.USE_CAULDRON);
               world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
               world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
               world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }else{
               player.incrementStat(Stats.USE_CAULDRON);
               world.setBlockState(pos, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
               world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
               world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
               ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.AQUATIC_EVERSOURCE_USE)); // Add xp
               ArcanaAchievements.progress(serverPlayer,ArcanaAchievements.POCKET_OCEAN.id,1);
            }
            serverPlayer.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
         }
         return ActionResult.SUCCESS_SERVER;
      });
      
      lavaCauldronMap.put(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         if(!(ArcanaItemUtils.identifyItem(stack) instanceof AquaticEversource eversource)) return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         int mode = ArcanaItem.getIntProperty(stack,AquaticEversource.MODE_TAG);
         if(!world.isClient){
            if(mode == 1){
               player.incrementStat(Stats.USE_CAULDRON);
               world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
               world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
               world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
               serverPlayer.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            }else{
               return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            }
         }
         return ActionResult.SUCCESS_SERVER;
      });
      
      snowCauldronMap.put(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         if(!(ArcanaItemUtils.identifyItem(stack) instanceof AquaticEversource eversource)) return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         int mode = ArcanaItem.getIntProperty(stack,AquaticEversource.MODE_TAG);
         if(!world.isClient){
            if(mode == 1){
               player.incrementStat(Stats.USE_CAULDRON);
               world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
               world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
               world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
               serverPlayer.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            }else{
               return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            }
         }
         return ActionResult.SUCCESS_SERVER;
      });
      
      
      emptyCauldronMap.put(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         if(!(ArcanaItemUtils.identifyItem(stack) instanceof MagmaticEversource eversource)) return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         int mode = ArcanaItem.getIntProperty(stack,MagmaticEversource.MODE_TAG);
         int charges = ArcanaItem.getIntProperty(stack,MagmaticEversource.USES_TAG);
         
         if(mode != 1 && charges <= 0){
            player.sendMessage(Text.literal("The Eversource is Recharging").formatted(Formatting.RED,Formatting.ITALIC),true);
            SoundUtils.playSongToPlayer(serverPlayer, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,0.8f);
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         }
         
         if(mode == 1){
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         }
         if(!world.isClient){
            player.incrementStat(Stats.USE_CAULDRON);
            world.setBlockState(pos, Blocks.LAVA_CAULDRON.getDefaultState());
            world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
            serverPlayer.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            
            ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.MAGMATIC_EVERSOURCE_USE)); // Add xp
            ArcanaAchievements.progress(serverPlayer,ArcanaAchievements.HELLGATE.id,1);
            ArcanaItem.putProperty(stack,MagmaticEversource.USES_TAG,charges-1);
            eversource.buildItemLore(stack, serverPlayer.getServer());
         }
         return ActionResult.SUCCESS_SERVER;
      });
      
      lavaCauldronMap.put(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         if(!(ArcanaItemUtils.identifyItem(stack) instanceof MagmaticEversource eversource)) return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         int mode = ArcanaItem.getIntProperty(stack,MagmaticEversource.MODE_TAG);
         
         if(!world.isClient){
            if(mode == 1){
               player.incrementStat(Stats.USE_CAULDRON);
               world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
               world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS, 1.0f, 1.0f);
               world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
               serverPlayer.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            }else{
               return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            }
         }
         return ActionResult.SUCCESS_SERVER;
      });
      
      waterCauldronMap.put(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         if(!(ArcanaItemUtils.identifyItem(stack) instanceof MagmaticEversource eversource)) return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         int mode = ArcanaItem.getIntProperty(stack,MagmaticEversource.MODE_TAG);
         
         
         if(!world.isClient){
            if(mode == 1){
               player.incrementStat(Stats.USE_CAULDRON);
               world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
               world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS, 1.0f, 1.0f);
               world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
               serverPlayer.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            }else{
               return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            }
         }
         return ActionResult.SUCCESS_SERVER;
      });
      
      snowCauldronMap.put(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         if(!(ArcanaItemUtils.identifyItem(stack) instanceof MagmaticEversource eversource)) return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         int mode = ArcanaItem.getIntProperty(stack,MagmaticEversource.MODE_TAG);
         
         if(!world.isClient){
            if(mode == 1){
               player.incrementStat(Stats.USE_CAULDRON);
               world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
               world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS, 1.0f, 1.0f);
               world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
               serverPlayer.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            }else{
               return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            }
         }
         return ActionResult.SUCCESS_SERVER;
      });
   }
}
