package net.borisshoes.arcananovum.achievements;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public abstract class ArcanaAchievement {
   public final String name;
   public final String id;
   public final int type;
   // Types:
   // 0 - event achievement, single boolean stored
   // 1 - progress achievement, two ints and one boolean stored
   // 2 - conditionals achievement, one boolean and compound of booleans stored
   // 3 - timed achievement, two booleans, three ints stored and one compound
   private boolean acquired;
   private final ItemStack displayItem;
   private final ArcanaItem arcanaItem;
   private final String[] description;
   public final int xpReward;
   public final int pointsReward;
   
   protected ArcanaAchievement(String name, String id, int type, ItemStack displayItem, ArcanaItem arcanaItem, int xpReward, int pointsReward, String[] description){
      this.name = name;
      this.id = id;
      this.type = type;
      this.displayItem = displayItem;
      this.arcanaItem = arcanaItem;
      this.description = description;
      this.xpReward = xpReward;
      this.pointsReward = pointsReward;
      this.acquired = false;
   }
   
   public String getTranslationKey(){
      return "achievement."+MOD_ID+".name."+this.id;
   }
   
   public MutableComponent getTranslatedName(){
      return Component.translatableWithFallback(getTranslationKey(),name);
   }
   
   protected void setAcquired(boolean acquired){
      this.acquired = acquired;
   }
   
   public boolean isAcquired(){
      return acquired;
   }
   
   public ItemStack getDisplayItem(){
      return displayItem;
   }
   
   public ArcanaItem getArcanaItem(){
      return arcanaItem;
   }
   
   public String[] getDescription(){
      return description;
   }
   
   public String getName(){
      return name;
   }
   
   public abstract CompoundTag toNbt();
   
   public abstract ArcanaAchievement fromNbt(String id, CompoundTag nbt);
   
   public abstract MutableComponent[] getStatusDisplay(ServerPlayer player);
   
   public abstract ArcanaAchievement makeNew();
   
   public void announceAcquired(ServerPlayer player){
      StringBuilder descriptionText = new StringBuilder();
      for(String d : description){
         descriptionText.append("\n").append(d);
      }
      
      MinecraftServer server = player.level().getServer();
      List<MutableComponent> msgs = new ArrayList<>();
      
      if(id.equals(ArcanaAchievements.ALL_ACHIEVEMENTS.id)){
         msgs.add(Component.literal("=============================================").withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE));
         msgs.add(Component.literal("")
               .append(Component.literal("=== ").withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.BOLD, ChatFormatting.BLACK))
               .append(player.getDisplayName())
               .append(Component.literal(" has mastered all Arcana Achievements and became ").withStyle(ChatFormatting.DARK_PURPLE))
               .append((Component.literal("[").append(getTranslatedName()).append(Component.literal("]"))).withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(
                           Component.literal("")
                                 .append(getTranslatedName().withStyle(ChatFormatting.DARK_AQUA))
                                 .append(Component.literal(descriptionText.toString()).withStyle(ChatFormatting.DARK_PURPLE))
                                 .append(Component.literal("")
                                       .append(Component.literal("\n"+LevelUtils.readableInt(xpReward)).withStyle(ChatFormatting.AQUA))
                                       .append(Component.literal(" XP").withStyle(ChatFormatting.DARK_AQUA))
                                       .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_AQUA))
                                       .append(Component.literal(""+pointsReward).withStyle(ChatFormatting.AQUA))
                                       .append(Component.literal(" Skill Points").withStyle(ChatFormatting.DARK_AQUA)))))
                     .withColor(ChatFormatting.DARK_AQUA).withBold(true)))
               .append(Component.literal("!!!").withStyle(ChatFormatting.DARK_PURPLE))
               .append(Component.literal(" ===").withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.BOLD, ChatFormatting.BLACK)));
         msgs.add(Component.literal("=============================================").withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE));
         SoundUtils.playSongToPlayer(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,1,1);
      }else if(pointsReward >= 5){
         msgs.add(Component.literal("")
               .append(player.getDisplayName())
               .append(Component.literal(" has made the Arcana Achievement ").withStyle(ChatFormatting.DARK_PURPLE))
               .append((Component.literal("[").append(getTranslatedName()).append(Component.literal("]"))).withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(
                           Component.literal("")
                                 .append(getTranslatedName().withStyle(ChatFormatting.DARK_AQUA))
                                 .append(Component.literal(descriptionText.toString()).withStyle(ChatFormatting.DARK_PURPLE))
                                 .append(Component.literal("")
                                       .append(Component.literal("\n"+LevelUtils.readableInt(xpReward)).withStyle(ChatFormatting.AQUA))
                                       .append(Component.literal(" XP").withStyle(ChatFormatting.DARK_AQUA))
                                       .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_AQUA))
                                       .append(Component.literal(""+pointsReward).withStyle(ChatFormatting.AQUA))
                                       .append(Component.literal(" Skill Points").withStyle(ChatFormatting.DARK_AQUA)))))
                     .withColor(ChatFormatting.DARK_AQUA).withBold(true)))
               .append(Component.literal("!!!").withStyle(ChatFormatting.DARK_PURPLE)));
         SoundUtils.playSongToPlayer(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,1,1);
      }else{
         msgs.add(Component.literal("")
               .append(player.getDisplayName())
               .append(Component.literal(" has made the Arcana Achievement ").withStyle(ChatFormatting.LIGHT_PURPLE))
               .append((Component.literal("[").append(getTranslatedName()).append(Component.literal("]"))).withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(
                           Component.literal("")
                                 .append(getTranslatedName().withStyle(ChatFormatting.AQUA))
                                 .append(Component.literal(descriptionText.toString()).withStyle(ChatFormatting.LIGHT_PURPLE))
                                 .append(Component.literal("")
                                       .append(Component.literal("\n"+ LevelUtils.readableInt(xpReward)).withStyle(ChatFormatting.AQUA))
                                       .append(Component.literal(" XP").withStyle(ChatFormatting.DARK_AQUA))
                                       .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_AQUA))
                                       .append(Component.literal(""+pointsReward).withStyle(ChatFormatting.AQUA))
                                       .append(Component.literal(" Skill Points").withStyle(ChatFormatting.DARK_AQUA)))))
                     .withColor(ChatFormatting.AQUA)))
               .append(Component.literal("!").withStyle(ChatFormatting.LIGHT_PURPLE)));
         
         SoundUtils.playSongToPlayer(player, SoundEvents.PLAYER_LEVELUP,1,1);
      }
      if(ArcanaNovum.CONFIG.getBoolean(ArcanaRegistry.ANNOUNCE_ACHIEVEMENTS)){
         for(MutableComponent msg : msgs){
            server.getPlayerList().broadcastSystemMessage(msg, false);
         }
      }else{
         for(MutableComponent msg : msgs){
            player.displayClientMessage(msg, false);
         }
      }
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      profile.addXP(xpReward); // Add xp
   
      boolean abyssCheck = true;
      for(ArcanaAchievement achievement : ArcanaAchievements.registry.values()){
         if(ArcanaAchievements.excludedAchievements.contains(achievement)) continue;
         if(!profile.hasAcheivement(achievement)){
            abyssCheck = false;
            break;
         }
      }
      if(abyssCheck){
         for(ArcanaAchievement achievement : ArcanaAchievements.excludedAchievements){
            ArcanaAchievements.grant(player,achievement.id);
         }
      }
   }
}
