package net.borisshoes.arcananovum.recipes.arcana;

import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ForgeRequirement {
   
   private boolean fletchery,anvil,enchanter,core,singularity;
   
   public ForgeRequirement(){}
   
   public boolean forgeMeetsRequirement(StarlightForgeBlockEntity forge, boolean message, ServerPlayerEntity player){
      if(!(forge.getWorld() instanceof ServerWorld world)) return false;
      boolean ret = true;
      if(fletchery && forge.getForgeAddition(world, ArcanaRegistry.RADIANT_FLETCHERY_BLOCK_ENTITY) == null) ret = false;
      if(anvil && forge.getForgeAddition(world, ArcanaRegistry.TWILIGHT_ANVIL_BLOCK_ENTITY) == null) ret = false;
      if(enchanter && forge.getForgeAddition(world, ArcanaRegistry.MIDNIGHT_ENCHANTER_BLOCK_ENTITY) == null) ret = false;
      if(core && forge.getForgeAddition(world, ArcanaRegistry.STELLAR_CORE_BLOCK_ENTITY) == null) ret = false;
      if(singularity && forge.getForgeAddition(world, ArcanaRegistry.ARCANE_SINGULARITY_BLOCK_ENTITY) == null) ret = false;
      if(!ret && message){
         player.sendMessage(Text.literal("This Forge does not have the necessary additions nearby").formatted(Formatting.RED));
      }
      return ret;
   }
   
   public boolean needsFletchery(){
      return fletchery;
   }
   
   public ForgeRequirement withFletchery(){
      this.fletchery = true;
      return this;
   }
   
   public boolean needsAnvil(){
      return anvil;
   }
   
   public ForgeRequirement withAnvil(){
      this.anvil = true;
      return this;
   }
   
   public boolean needsEnchanter(){
      return enchanter;
   }
   
   public ForgeRequirement withEnchanter(){
      this.enchanter = true;
      return this;
   }
   
   public boolean needsCore(){
      return core;
   }
   
   public ForgeRequirement withCore(){
      this.core = true;
      return this;
   }
   
   public boolean needsSingularity(){
      return singularity;
   }
   
   public ForgeRequirement withSingularity(){
      this.singularity = true;
      return this;
   }
}
