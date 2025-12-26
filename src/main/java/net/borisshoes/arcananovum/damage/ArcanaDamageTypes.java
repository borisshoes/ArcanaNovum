package net.borisshoes.arcananovum.damage;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ArcanaDamageTypes {
   public static final ResourceKey<DamageType> CONCENTRATION = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,"concentration"));
   public static final ResourceKey<DamageType> PHOTONIC = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,"photonic"));
   public static final ResourceKey<DamageType> DETONATION_TERRAIN = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,"detonation_arrows_terrain"));
   public static final ResourceKey<DamageType> DETONATION_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,"detonation_arrows_damage"));
   public static final ResourceKey<DamageType> NUL = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,"nul_damage"));
   public static final ResourceKey<DamageType> VENGEANCE_TOTEM = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,"vengeance_totem"));
   public static final ResourceKey<DamageType> ARCANE_LIGHTNING = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,"arcane_lightning"));
   
   public static DamageSource of(Level world, ResourceKey<DamageType> key){
      return new DamageSource((world.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key)));
   }
   
   public static DamageSource of(Level world, ResourceKey<DamageType> key, Entity attacker){
      return new DamageSource((world.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key)),attacker);
   }
   
   public static DamageSource of(Level world, ResourceKey<DamageType> key, Entity source, Entity attacker){
      return new DamageSource((world.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key)),source,attacker);
   }
}
