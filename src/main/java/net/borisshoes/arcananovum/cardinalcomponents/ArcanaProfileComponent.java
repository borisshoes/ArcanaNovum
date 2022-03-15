package net.borisshoes.arcananovum.cardinalcomponents;

import net.borisshoes.arcananovum.items.MagicItem;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.Utils;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class ArcanaProfileComponent implements IArcanaProfileComponent{
   private final PlayerEntity player;
   private final List<String> crafted = new ArrayList<>();
   private final List<String> recipes = new ArrayList<>();
   private int level;
   private int xp;
   
   public ArcanaProfileComponent(PlayerEntity player){
      this.player = player;
   }
   
   @Override
   public void readFromNbt(NbtCompound tag){
      crafted.clear();
      recipes.clear();
      tag.getList("crafted", NbtType.STRING).forEach(item -> crafted.add(item.asString()));
      tag.getList("recipes", NbtType.STRING).forEach(item -> recipes.add(item.asString()));
      level = tag.getInt("level");
      xp = tag.getInt("xp");
   }
   
   @Override
   public void writeToNbt(NbtCompound tag){
      NbtList craftedTag = new NbtList();
      NbtList recipesTag = new NbtList();
      crafted.forEach(item -> {
         craftedTag.add(NbtString.of(item));
      });
      recipes.forEach(item -> {
         recipesTag.add(NbtString.of(item));
      });
      tag.put("crafted",craftedTag);
      tag.put("recipes",recipesTag);
      tag.putInt("level",level);
      tag.putInt("xp",xp);
   }
   
   @Override
   public List<String> getCrafted(){
      return crafted;
   }
   
   @Override
   public List<String> getRecipes(){
      return recipes;
   }
   
   @Override
   public int getLevel(){
      return level;
   }
   
   @Override
   public int getXP(){
      return xp;
   }
   
   @Override
   public boolean addXP(int xp){
      int newLevel = LevelUtils.levelFromXp(this.xp+xp);
      if(player instanceof ServerPlayerEntity && getLevel() != newLevel){
         if(getLevel()/5 < newLevel/5){
            MinecraftServer server = player.getServer();
            if(server != null){
               MutableText lvlUpMsg = new LiteralText("")
                     .append(player.getDisplayName()).formatted(Formatting.ITALIC)
                     .append(new LiteralText(" has reached Arcana Level ").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC))
                     .append(new LiteralText(Integer.toString(newLevel/5 * 5)).formatted(Formatting.DARK_PURPLE,Formatting.BOLD,Formatting.ITALIC, Formatting.UNDERLINE))
                     .append(new LiteralText("!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC));
               server.getPlayerManager().broadcast(lvlUpMsg, MessageType.SYSTEM,player.getUuid());
            }
         }
         Utils.playSongToPlayer((ServerPlayerEntity) player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, .5f,1.5f);
         player.sendMessage(new LiteralText(""),false);
         player.sendMessage(new LiteralText("Your Arcana has levelled up to level "+newLevel+"!").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD),false);
         player.sendMessage(new LiteralText("Max Concentration increased to "+LevelUtils.concFromLevel(newLevel)+"!").formatted(Formatting.AQUA,Formatting.ITALIC),false);
         player.sendMessage(new LiteralText(""),false);
      }
      this.xp += xp;
      this.level = newLevel;
      return true;
   }
   
   @Override
   public boolean setXP(int xp){
      this.xp = xp;
      setLevel(LevelUtils.levelFromXp(this.xp));
      return true;
   }
   
   @Override
   public boolean setLevel(int lvl){
      this.level = lvl;
      return true;
   }
   
   @Override
   public boolean addCrafted(String item){
      if (crafted.stream().anyMatch(i -> i.equalsIgnoreCase(item))) return false;
      MagicItem magicItem = MagicItemUtils.getItemFromId(item);
      if(player instanceof ServerPlayerEntity){
         MinecraftServer server = player.getServer();
         if(server != null){
            MutableText newCraftMsg = new LiteralText("")
                  .append(player.getDisplayName()).formatted(Formatting.ITALIC)
                  .append(new LiteralText(" has crafted their first ").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC))
                  .append(new LiteralText(magicItem.getName()).formatted(Formatting.DARK_PURPLE, Formatting.BOLD, Formatting.ITALIC, Formatting.UNDERLINE))
                  .append(new LiteralText("!").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
            server.getPlayerManager().broadcast(newCraftMsg, MessageType.SYSTEM, player.getUuid());
         }
      }
      addXP(MagicRarity.getFirstCraftXp(magicItem.getRarity()));
      return crafted.add(item);
   }
   
   @Override
   public boolean addRecipe(String item){
      if (recipes.stream().anyMatch(i -> i.equalsIgnoreCase(item))) return false;
      return recipes.add(item);
   }
   
   @Override
   public boolean removeCrafted(String item){
      if (crafted.stream().noneMatch(i -> i.equalsIgnoreCase(item))) return false;
      return crafted.removeIf(i -> i.equalsIgnoreCase(item));
   }
   
   @Override
   public boolean removeRecipe(String item){
      if (recipes.stream().noneMatch(i -> i.equalsIgnoreCase(item))) return false;
      return recipes.removeIf(i -> i.equalsIgnoreCase(item));
   }
   
   
}
