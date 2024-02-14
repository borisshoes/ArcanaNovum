package net.borisshoes.arcananovum.damage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ArcanaDamageTypes {
   public static final RegistryKey<DamageType> CONCENTRATION = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier("arcananovum","concentration"));
   public static final RegistryKey<DamageType> PHOTONIC = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier("arcananovum","photonic"));
   public static final RegistryKey<DamageType> DETONATION_TERRAIN = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier("arcananovum","detonation_arrows_terrain"));
   public static final RegistryKey<DamageType> DETONATION_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier("arcananovum","detonation_arrows_damage"));
   public static final RegistryKey<DamageType> NUL = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier("arcananovum","nul_damage"));
   public static final RegistryKey<DamageType> VENGEANCE_TOTEM = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier("arcananovum","vengeance_totem"));
   public static final RegistryKey<DamageType> ARCANE_LIGHTNING = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier("arcananovum","arcane_lightning"));
   
   public static DamageSource of(World world, RegistryKey<DamageType> key){
      return new DamageSource((world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key)));
   }
   
   public static DamageSource of(World world, RegistryKey<DamageType> key, Entity attacker){
      return new DamageSource((world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key)),attacker);
   }
   
   public static DamageSource of(World world, RegistryKey<DamageType> key, Entity source, Entity attacker){
      return new DamageSource((world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key)),attacker,source);
   }
}
