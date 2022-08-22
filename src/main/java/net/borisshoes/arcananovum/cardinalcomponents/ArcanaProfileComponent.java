package net.borisshoes.arcananovum.cardinalcomponents;

import net.borisshoes.arcananovum.items.MagicItem;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ArcanaProfileComponent implements IArcanaProfileComponent{
   private final PlayerEntity player;
   private final List<String> crafted = new ArrayList<>();
   private final List<String> recipes = new ArrayList<>();
   private final HashMap<String, NbtElement> miscData = new HashMap<>();
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
      NbtCompound miscDataTag = tag.getCompound("miscData");
      Set<String> keys = miscDataTag.getKeys();
      keys.forEach(key ->{
         miscData.put(key,miscDataTag.get(key));
      });
      level = tag.getInt("level");
      xp = tag.getInt("xp");
   }
   
   @Override
   public void writeToNbt(NbtCompound tag){
      NbtList craftedTag = new NbtList();
      NbtList recipesTag = new NbtList();
      NbtCompound miscDataTag = new NbtCompound();
      crafted.forEach(item -> {
         craftedTag.add(NbtString.of(item));
      });
      recipes.forEach(item -> {
         recipesTag.add(NbtString.of(item));
      });
      miscData.forEach(miscDataTag::put);
      tag.put("crafted",craftedTag);
      tag.put("recipes",recipesTag);
      tag.put("miscData",miscDataTag);
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
   public NbtElement getMiscData(String id){
      return miscData.get(id);
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
               MutableText lvlUpMsg = Text.translatable("")
                     .append(player.getDisplayName()).formatted(Formatting.ITALIC)
                     .append(Text.translatable(" has reached Arcana Level ").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC))
                     .append(Text.translatable(Integer.toString(newLevel/5 * 5)).formatted(Formatting.DARK_PURPLE,Formatting.BOLD,Formatting.ITALIC, Formatting.UNDERLINE))
                     .append(Text.translatable("!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC));
               server.getPlayerManager().broadcast(lvlUpMsg, false);
            }
         }
         SoundUtils.playSongToPlayer((ServerPlayerEntity) player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, .5f,1.5f);
         player.sendMessage(Text.translatable(""),false);
         player.sendMessage(Text.translatable("Your Arcana has levelled up to level "+newLevel+"!").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD),false);
         player.sendMessage(Text.translatable("Max Concentration increased to "+LevelUtils.concFromLevel(newLevel)+"!").formatted(Formatting.AQUA,Formatting.ITALIC),false);
         player.sendMessage(Text.translatable(""),false);
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
            MutableText newCraftMsg = Text.translatable("")
                  .append(player.getDisplayName()).formatted(Formatting.ITALIC)
                  .append(Text.translatable(" has crafted their first ").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC))
                  .append(Text.translatable(magicItem.getName()).formatted(Formatting.DARK_PURPLE, Formatting.BOLD, Formatting.ITALIC, Formatting.UNDERLINE))
                  .append(Text.translatable("!").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
            server.getPlayerManager().broadcast(newCraftMsg, false);
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
   
   @Override
   public void addMiscData(String id, NbtElement data){
      miscData.put(id,data);
   }
   
   @Override
   public void removeMiscData(String id){
      miscData.remove(id);
   }
   
   
}
