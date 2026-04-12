package net.borisshoes.arcananovum.recipes;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.AquaticEversource;
import net.borisshoes.arcananovum.items.MagmaticEversource;
import net.borisshoes.arcananovum.mixins.CauldronInteractionDispatcherAccessor;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.gameevent.GameEvent;

public class ArcanaCauldronInteractions {
   public static void registerArcanaCauldronInteractions(CauldronInteractionDispatcherAccessor water, CauldronInteractionDispatcherAccessor lava, CauldronInteractionDispatcherAccessor empty, CauldronInteractionDispatcherAccessor snow){
      // Shield Washing
      water.callPut(ArcanaRegistry.SHIELD_OF_FORTITUDE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!stack.has(DataComponents.BANNER_PATTERNS) && !stack.has(DataComponents.BASE_COLOR)){
            return InteractionResult.TRY_WITH_EMPTY_HAND;
         }else{
            if(!world.isClientSide()){
               stack.remove(DataComponents.BANNER_PATTERNS);
               stack.remove(DataComponents.BASE_COLOR);
               LayeredCauldronBlock.lowerFillLevel(state, world, pos);
            }
            return InteractionResult.SUCCESS_SERVER;
         }
      });
      
      // Eversources
      empty.callPut(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.TRY_WITH_EMPTY_HAND;
         if(!(ArcanaItemUtils.identifyItem(stack) instanceof AquaticEversource eversource))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
         int mode = ArcanaItem.getIntProperty(stack, AquaticEversource.MODE_TAG);
         if(mode == 1){
            return InteractionResult.TRY_WITH_EMPTY_HAND;
         }
         if(!world.isClientSide()){
            player.awardStat(Stats.USE_CAULDRON);
            world.setBlockAndUpdate(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
            world.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
            world.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            serverPlayer.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_AQUATIC_EVERSOURCE_USE)); // Add xp
            ArcanaAchievements.progress(serverPlayer, ArcanaAchievements.POCKET_OCEAN, 1);
         }
         return InteractionResult.SUCCESS_SERVER;
      });
      
      water.callPut(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.TRY_WITH_EMPTY_HAND;
         if(!(ArcanaItemUtils.identifyItem(stack) instanceof AquaticEversource eversource))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
         int mode = ArcanaItem.getIntProperty(stack, AquaticEversource.MODE_TAG);
         if(!world.isClientSide()){
            if(mode == 1){
               player.awardStat(Stats.USE_CAULDRON);
               world.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
               world.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
               world.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }else{
               player.awardStat(Stats.USE_CAULDRON);
               world.setBlockAndUpdate(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
               world.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
               world.gameEvent(null, GameEvent.FLUID_PLACE, pos);
               ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_AQUATIC_EVERSOURCE_USE)); // Add xp
               ArcanaAchievements.progress(serverPlayer, ArcanaAchievements.POCKET_OCEAN, 1);
            }
            serverPlayer.awardStat(Stats.ITEM_USED.get(stack.getItem()));
         }
         return InteractionResult.SUCCESS_SERVER;
      });
      
      lava.callPut(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.TRY_WITH_EMPTY_HAND;
         if(!(ArcanaItemUtils.identifyItem(stack) instanceof AquaticEversource eversource))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
         int mode = ArcanaItem.getIntProperty(stack, AquaticEversource.MODE_TAG);
         if(!world.isClientSide()){
            if(mode == 1){
               player.awardStat(Stats.USE_CAULDRON);
               world.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
               world.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
               world.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
               serverPlayer.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            }else{
               return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
         }
         return InteractionResult.SUCCESS_SERVER;
      });
      
      snow.callPut(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.TRY_WITH_EMPTY_HAND;
         if(!(ArcanaItemUtils.identifyItem(stack) instanceof AquaticEversource eversource))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
         int mode = ArcanaItem.getIntProperty(stack, AquaticEversource.MODE_TAG);
         if(!world.isClientSide()){
            if(mode == 1){
               player.awardStat(Stats.USE_CAULDRON);
               world.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
               world.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
               world.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
               serverPlayer.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            }else{
               return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
         }
         return InteractionResult.SUCCESS_SERVER;
      });
      
      
      empty.callPut(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.TRY_WITH_EMPTY_HAND;
         if(!(ArcanaItemUtils.identifyItem(stack) instanceof MagmaticEversource eversource))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
         int mode = ArcanaItem.getIntProperty(stack, MagmaticEversource.MODE_TAG);
         int charges = ArcanaItem.getIntProperty(stack, MagmaticEversource.USES_TAG);
         
         if(mode != 1 && charges <= 0){
            serverPlayer.sendSystemMessage(Component.literal("The Eversource is Recharging").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
            SoundUtils.playSongToPlayer(serverPlayer, SoundEvents.FIRE_EXTINGUISH, 1, 0.8f);
            return InteractionResult.TRY_WITH_EMPTY_HAND;
         }
         
         if(mode == 1){
            return InteractionResult.TRY_WITH_EMPTY_HAND;
         }
         if(!world.isClientSide()){
            player.awardStat(Stats.USE_CAULDRON);
            world.setBlockAndUpdate(pos, Blocks.LAVA_CAULDRON.defaultBlockState());
            world.playSound(null, pos, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 1.0f, 1.0f);
            world.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            serverPlayer.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            
            ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_MAGMATIC_EVERSOURCE_USE)); // Add xp
            ArcanaAchievements.progress(serverPlayer, ArcanaAchievements.HELLGATE, 1);
            ArcanaItem.putProperty(stack, MagmaticEversource.USES_TAG, charges - 1);
            eversource.buildItemLore(stack, serverPlayer.level().getServer());
         }
         return InteractionResult.SUCCESS_SERVER;
      });
      
      lava.callPut(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.TRY_WITH_EMPTY_HAND;
         if(!(ArcanaItemUtils.identifyItem(stack) instanceof MagmaticEversource eversource))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
         int mode = ArcanaItem.getIntProperty(stack, MagmaticEversource.MODE_TAG);
         
         if(!world.isClientSide()){
            if(mode == 1){
               player.awardStat(Stats.USE_CAULDRON);
               world.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
               world.playSound(null, pos, SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 1.0f, 1.0f);
               world.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
               serverPlayer.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            }else{
               return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
         }
         return InteractionResult.SUCCESS_SERVER;
      });
      
      water.callPut(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.TRY_WITH_EMPTY_HAND;
         if(!(ArcanaItemUtils.identifyItem(stack) instanceof MagmaticEversource eversource))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
         int mode = ArcanaItem.getIntProperty(stack, MagmaticEversource.MODE_TAG);
         
         
         if(!world.isClientSide()){
            if(mode == 1){
               player.awardStat(Stats.USE_CAULDRON);
               world.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
               world.playSound(null, pos, SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 1.0f, 1.0f);
               world.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
               serverPlayer.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            }else{
               return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
         }
         return InteractionResult.SUCCESS_SERVER;
      });
      
      snow.callPut(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.TRY_WITH_EMPTY_HAND;
         if(!(ArcanaItemUtils.identifyItem(stack) instanceof MagmaticEversource eversource))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
         int mode = ArcanaItem.getIntProperty(stack, MagmaticEversource.MODE_TAG);
         
         if(!world.isClientSide()){
            if(mode == 1){
               player.awardStat(Stats.USE_CAULDRON);
               world.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
               world.playSound(null, pos, SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 1.0f, 1.0f);
               world.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
               serverPlayer.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            }else{
               return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
         }
         return InteractionResult.SUCCESS_SERVER;
      });
   }
}
