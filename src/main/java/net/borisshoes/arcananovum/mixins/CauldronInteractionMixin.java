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
import net.minecraft.ChatFormatting;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(CauldronInteraction.class)
public interface CauldronInteractionMixin {
   
   @Inject(method = "bootStrap", at = @At("TAIL"))
   private static void arcananovum$cauldronInteractions(CallbackInfo ci, @Local(ordinal = 0) Map<Item, CauldronInteraction> emptyCauldronMap, @Local(ordinal = 1) Map<Item, CauldronInteraction> waterCauldronMap, @Local(ordinal = 2) Map<Item, CauldronInteraction> lavaCauldronMap, @Local(ordinal = 3) Map<Item, CauldronInteraction> snowCauldronMap){
      
      // Dyed Item Washing
      final CauldronInteraction CLEAN_DYEABLE_ITEM = (state, world, pos, player, hand, stack) -> {
         if(!stack.is(ItemTags.DYEABLE)){
            return InteractionResult.TRY_WITH_EMPTY_HAND;
         }else if(!stack.has(DataComponents.DYED_COLOR)){
            return InteractionResult.TRY_WITH_EMPTY_HAND;
         }else{
            if(!world.isClientSide()){
               stack.remove(DataComponents.DYED_COLOR);
               player.awardStat(Stats.CLEAN_ARMOR);
               LayeredCauldronBlock.lowerFillLevel(state, world, pos);
            }
            return InteractionResult.SUCCESS_SERVER;
         }
      };
      
      waterCauldronMap.put(ArcanaRegistry.LEVITATION_HARNESS.getItem(), CLEAN_DYEABLE_ITEM);
      waterCauldronMap.put(ArcanaRegistry.SOJOURNER_BOOTS.getItem(), CLEAN_DYEABLE_ITEM);
      
      // Shield Washing
      waterCauldronMap.put(ArcanaRegistry.SHIELD_OF_FORTITUDE.getItem(), (state, world, pos, player, hand, stack) -> {
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
      emptyCauldronMap.put(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
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
      
      waterCauldronMap.put(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
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
      
      lavaCauldronMap.put(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
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
      
      snowCauldronMap.put(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
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
      
      
      emptyCauldronMap.put(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.TRY_WITH_EMPTY_HAND;
         if(!(ArcanaItemUtils.identifyItem(stack) instanceof MagmaticEversource eversource))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
         int mode = ArcanaItem.getIntProperty(stack, MagmaticEversource.MODE_TAG);
         int charges = ArcanaItem.getIntProperty(stack, MagmaticEversource.USES_TAG);
         
         if(mode != 1 && charges <= 0){
            player.displayClientMessage(Component.literal("The Eversource is Recharging").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
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
      
      lavaCauldronMap.put(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
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
      
      waterCauldronMap.put(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
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
      
      snowCauldronMap.put(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
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
