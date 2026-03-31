package net.borisshoes.arcananovum.recipes.arcana;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class ForgeRequirement {
   
   private boolean fletchery, anvil, enchanter, core, singularity;
   
   public ForgeRequirement(){
   }
   
   public boolean forgeMeetsRequirement(StarlightForgeBlockEntity forge, boolean message, ServerPlayer player){
      if(!(forge.getLevel() instanceof ServerLevel world)) return false;
      boolean ret = true;
      if(fletchery && forge.getForgeAddition(world, ArcanaRegistry.RADIANT_FLETCHERY_BLOCK_ENTITY) == null) ret = false;
      if(anvil && forge.getForgeAddition(world, ArcanaRegistry.TWILIGHT_ANVIL_BLOCK_ENTITY) == null) ret = false;
      if(enchanter && forge.getForgeAddition(world, ArcanaRegistry.MIDNIGHT_ENCHANTER_BLOCK_ENTITY) == null)
         ret = false;
      if(core && forge.getForgeAddition(world, ArcanaRegistry.STELLAR_CORE_BLOCK_ENTITY) == null) ret = false;
      if(singularity && forge.getForgeAddition(world, ArcanaRegistry.ARCANE_SINGULARITY_BLOCK_ENTITY) == null)
         ret = false;
      if(!ret && message){
         player.sendSystemMessage(Component.literal("This Forge does not have the necessary additions nearby").withStyle(ChatFormatting.RED));
      }
      return ret;
   }
   
   public List<ArcanaItem> forgeMissingRequirements(StarlightForgeBlockEntity forge){
      ArrayList<ArcanaItem> list = new ArrayList<>();
      if(!(forge.getLevel() instanceof ServerLevel world)) return list;
      if(fletchery && forge.getForgeAddition(world, ArcanaRegistry.RADIANT_FLETCHERY_BLOCK_ENTITY) == null)
         list.add(ArcanaRegistry.RADIANT_FLETCHERY);
      if(anvil && forge.getForgeAddition(world, ArcanaRegistry.TWILIGHT_ANVIL_BLOCK_ENTITY) == null)
         list.add(ArcanaRegistry.TWILIGHT_ANVIL);
      if(enchanter && forge.getForgeAddition(world, ArcanaRegistry.MIDNIGHT_ENCHANTER_BLOCK_ENTITY) == null)
         list.add(ArcanaRegistry.MIDNIGHT_ENCHANTER);
      if(core && forge.getForgeAddition(world, ArcanaRegistry.STELLAR_CORE_BLOCK_ENTITY) == null)
         list.add(ArcanaRegistry.STELLAR_CORE);
      if(singularity && forge.getForgeAddition(world, ArcanaRegistry.ARCANE_SINGULARITY_BLOCK_ENTITY) == null)
         list.add(ArcanaRegistry.ARCANE_SINGULARITY);
      return list;
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
   
   public String getCodeString(){
      StringBuilder builder = new StringBuilder("new ForgeRequirement()");
      if(needsAnvil()){
         builder.append(".withAnvil()");
      }
      if(needsEnchanter()){
         builder.append(".withEnchanter()");
      }
      if(needsCore()){
         builder.append(".withCore()");
      }
      if(needsFletchery()){
         builder.append(".withFletchery()");
      }
      if(needsSingularity()){
         builder.append(".withSingularity()");
      }
      return builder.toString();
   }
}
