package net.borisshoes.arcananovum.damage;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ArcanaDamageTypes {
   public static final RegistryKey<DamageType> CONCENTRATION = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(ArcanaNovum.MOD_ID,"concentration"));
   public static final RegistryKey<DamageType> PHOTONIC = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(ArcanaNovum.MOD_ID,"photonic"));
   public static final RegistryKey<DamageType> DETONATION_TERRAIN = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(ArcanaNovum.MOD_ID,"detonation_arrows_terrain"));
   public static final RegistryKey<DamageType> DETONATION_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(ArcanaNovum.MOD_ID,"detonation_arrows_damage"));
   public static final RegistryKey<DamageType> NUL = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(ArcanaNovum.MOD_ID,"nul_damage"));
   public static final RegistryKey<DamageType> VENGEANCE_TOTEM = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(ArcanaNovum.MOD_ID,"vengeance_totem"));
   public static final RegistryKey<DamageType> ARCANE_LIGHTNING = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(ArcanaNovum.MOD_ID,"arcane_lightning"));
   
   public static DamageSource of(World world, RegistryKey<DamageType> key){
      return new DamageSource((world.getRegistryManager().getOrThrow(RegistryKeys.DAMAGE_TYPE).getOrThrow(key)));
   }
   
   public static DamageSource of(World world, RegistryKey<DamageType> key, Entity attacker){
      return new DamageSource((world.getRegistryManager().getOrThrow(RegistryKeys.DAMAGE_TYPE).getOrThrow(key)),attacker);
   }
   
   public static DamageSource of(World world, RegistryKey<DamageType> key, Entity source, Entity attacker){
      return new DamageSource((world.getRegistryManager().getOrThrow(RegistryKeys.DAMAGE_TYPE).getOrThrow(key)),source,attacker);
   }
}
