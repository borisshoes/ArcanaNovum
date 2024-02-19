package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.items.AquaticEversource;
import net.borisshoes.arcananovum.items.MagmaticEversource;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

@Mixin(CauldronBehavior.class)
public interface CauldronBehaviorMixin {
   
   @Inject(method="registerBehavior",at=@At("TAIL"),locals = LocalCapture.CAPTURE_FAILHARD)
   private static void arcananovum_eversourceCauldrons(CallbackInfo ci, Map<Item, CauldronBehavior> emptyCauldronMap, Map<Item, CauldronBehavior> waterCauldronMap, Map<Item, CauldronBehavior> lavaCauldronMap, Map<Item, CauldronBehavior> snowCauldronMap){
      emptyCauldronMap.put(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
         if(!(MagicItemUtils.identifyItem(stack) instanceof AquaticEversource eversource)) return ActionResult.PASS;
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         int mode = magicNbt.getInt("mode");
         if (mode == 1) {
            return ActionResult.PASS;
         }
         if (!world.isClient) {
            player.incrementStat(Stats.USE_CAULDRON);
            world.setBlockState(pos, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
            world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
            serverPlayer.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            PLAYER_DATA.get(player).addXP(5); // Add xp
            ArcanaAchievements.progress(serverPlayer,ArcanaAchievements.POCKET_OCEAN.id,1);
         }
         return ActionResult.success(world.isClient);
      });
      
      waterCauldronMap.put(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
         if(!(MagicItemUtils.identifyItem(stack) instanceof AquaticEversource eversource)) return ActionResult.PASS;
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         int mode = magicNbt.getInt("mode");
         if (!world.isClient) {
            if(mode == 1) {
               player.incrementStat(Stats.USE_CAULDRON);
               world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
               world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
               world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }else{
               player.incrementStat(Stats.USE_CAULDRON);
               world.setBlockState(pos, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
               world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
               world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
               PLAYER_DATA.get(player).addXP(5); // Add xp
               ArcanaAchievements.progress(serverPlayer,ArcanaAchievements.POCKET_OCEAN.id,1);
            }
            serverPlayer.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
         }
         return ActionResult.success(world.isClient);
      });
      
      lavaCauldronMap.put(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
         if(!(MagicItemUtils.identifyItem(stack) instanceof AquaticEversource eversource)) return ActionResult.PASS;
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         int mode = magicNbt.getInt("mode");
         if (!world.isClient) {
            if(mode == 1) {
               player.incrementStat(Stats.USE_CAULDRON);
               world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
               world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
               world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
               serverPlayer.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            }else{
               return ActionResult.PASS;
            }
         }
         return ActionResult.success(world.isClient);
      });
      
      snowCauldronMap.put(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
         if(!(MagicItemUtils.identifyItem(stack) instanceof AquaticEversource eversource)) return ActionResult.PASS;
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         int mode = magicNbt.getInt("mode");
         if (!world.isClient) {
            if(mode == 1) {
               player.incrementStat(Stats.USE_CAULDRON);
               world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
               world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
               world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
               serverPlayer.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            }else{
               return ActionResult.PASS;
            }
         }
         return ActionResult.success(world.isClient);
      });
      
      
      emptyCauldronMap.put(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
         if(!(MagicItemUtils.identifyItem(stack) instanceof MagmaticEversource eversource)) return ActionResult.PASS;
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         int mode = magicNbt.getInt("mode");
         int charges = magicNbt.getInt("charges");
         
         if(mode != 1 && charges <= 0){
            player.sendMessage(Text.literal("The Eversource is Recharging").formatted(Formatting.RED,Formatting.ITALIC),true);
            SoundUtils.playSongToPlayer(serverPlayer, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,0.8f);
            return ActionResult.PASS;
         }
         
         if (mode == 1) {
            return ActionResult.PASS;
         }
         if (!world.isClient) {
            player.incrementStat(Stats.USE_CAULDRON);
            world.setBlockState(pos, Blocks.LAVA_CAULDRON.getDefaultState());
            world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
            serverPlayer.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            
            PLAYER_DATA.get(player).addXP(25); // Add xp
            ArcanaAchievements.progress(serverPlayer,ArcanaAchievements.HELLGATE.id,1);
            magicNbt.putInt("charges",charges-1);
            eversource.buildItemLore(stack, serverPlayer.getServer());
         }
         return ActionResult.success(world.isClient);
      });
      
      lavaCauldronMap.put(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
         if(!(MagicItemUtils.identifyItem(stack) instanceof MagmaticEversource eversource)) return ActionResult.PASS;
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         int mode = magicNbt.getInt("mode");
         
         if (!world.isClient) {
            if(mode == 1) {
               player.incrementStat(Stats.USE_CAULDRON);
               world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
               world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS, 1.0f, 1.0f);
               world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
               serverPlayer.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            }else{
               return ActionResult.PASS;
            }
         }
         return ActionResult.success(world.isClient);
      });
      
      waterCauldronMap.put(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
         if(!(MagicItemUtils.identifyItem(stack) instanceof MagmaticEversource eversource)) return ActionResult.PASS;
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         int mode = magicNbt.getInt("mode");
         
         if (!world.isClient) {
            if(mode == 1) {
               player.incrementStat(Stats.USE_CAULDRON);
               world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
               world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS, 1.0f, 1.0f);
               world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
               serverPlayer.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            }else{
               return ActionResult.PASS;
            }
         }
         return ActionResult.success(world.isClient);
      });
      
      snowCauldronMap.put(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem(), (state, world, pos, player, hand, stack) -> {
         if(!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
         if(!(MagicItemUtils.identifyItem(stack) instanceof MagmaticEversource eversource)) return ActionResult.PASS;
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
         int mode = magicNbt.getInt("mode");
         
         if (!world.isClient) {
            if(mode == 1) {
               player.incrementStat(Stats.USE_CAULDRON);
               world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
               world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS, 1.0f, 1.0f);
               world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
               serverPlayer.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
            }else{
               return ActionResult.PASS;
            }
         }
         return ActionResult.success(world.isClient);
      });
   }
}
