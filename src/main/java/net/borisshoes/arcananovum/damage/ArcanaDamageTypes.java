package net.borisshoes.arcananovum.damage;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ArcanaDamageTypes {
   public static final ResourceKey<DamageType> CONCENTRATION = ResourceKey.create(Registries.DAMAGE_TYPE, ArcanaRegistry.arcanaId("concentration"));
   public static final ResourceKey<DamageType> PHOTONIC = ResourceKey.create(Registries.DAMAGE_TYPE, ArcanaRegistry.arcanaId("photonic"));
   public static final ResourceKey<DamageType> DETONATION_TERRAIN = ResourceKey.create(Registries.DAMAGE_TYPE, ArcanaRegistry.arcanaId("detonation_arrows_terrain"));
   public static final ResourceKey<DamageType> DETONATION_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, ArcanaRegistry.arcanaId("detonation_arrows_damage"));
   public static final ResourceKey<DamageType> NUL = ResourceKey.create(Registries.DAMAGE_TYPE, ArcanaRegistry.arcanaId("nul_damage"));
   public static final ResourceKey<DamageType> VENGEANCE_TOTEM = ResourceKey.create(Registries.DAMAGE_TYPE, ArcanaRegistry.arcanaId("vengeance_totem"));
   public static final ResourceKey<DamageType> ARCANE_LIGHTNING = ResourceKey.create(Registries.DAMAGE_TYPE, ArcanaRegistry.arcanaId("arcane_lightning"));
   
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
