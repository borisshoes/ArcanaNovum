package net.borisshoes.arcananovum.utils;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;

public class CodecUtils {
   
   public static final Codec<List<BlockPos>> BLOCKPOS_LIST = BlockPos.CODEC.listOf();
   public static final Codec<List<NbtCompound>> COMPOUND_LIST = NbtCompound.CODEC.listOf();
   public static final Codec<List<String>> STRING_LIST = Codec.STRING.listOf();
   public static final Codec<String[]> STRING_ARRAY = STRING_LIST.xmap(l -> l.toArray(String[]::new), Arrays::asList);
}
