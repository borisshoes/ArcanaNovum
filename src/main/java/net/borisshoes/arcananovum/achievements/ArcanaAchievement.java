package net.borisshoes.arcananovum.achievements;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
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
import java.util.Set;
import java.util.UUID;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public abstract class ArcanaAchievement {
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
   public final int xpReward;
   public final int pointsReward;
   public boolean hidden;
   
   protected ArcanaAchievement(String id, int type, ItemStack displayItem, ArcanaItem arcanaItem, int xpReward, int pointsReward){
      this.id = id;
      this.type = type;
      this.displayItem = displayItem;
      this.arcanaItem = arcanaItem;
      this.xpReward = xpReward;
      this.pointsReward = pointsReward;
      this.acquired = false;
      this.hidden = false;
   }
   
   public ArcanaAchievement setHidden(boolean hidden){
      this.hidden = hidden;
      return this;
   }
   
   public String getTranslationKey(){
      return "achievement." + MOD_ID + ".name." + this.id;
   }
   
   public String getDescriptionTranslationKey(){
      return "achievement." + MOD_ID + ".description." + this.id;
   }
   
   public MutableComponent getTranslatedName(){
      return Component.translatable(getTranslationKey());
   }
   
   protected void setAcquired(boolean acquired){
      this.acquired = acquired;
   }
   
   public boolean isAcquired(){
      return acquired;
   }
   
   public boolean isHidden(){
      return hidden;
   }
   
   public ItemStack getDisplayItem(){
      return displayItem;
   }
   
   public ArcanaItem getArcanaItem(){
      return arcanaItem;
   }
   
   public List<Component> getDescription(){
      String fullText = Component.translatable(getDescriptionTranslationKey()).getString();
      String[] lines = fullText.split("\n");
      List<Component> components = new ArrayList<>();
      for(String line : lines){
         components.add(Component.literal(line));
      }
      return components;
   }
   
   public abstract CompoundTag toNbt();
   
   public abstract ArcanaAchievement fromNbt(String id, CompoundTag nbt);
   
   public abstract MutableComponent[] getStatusDisplay(ServerPlayer player);
   
   public abstract ArcanaAchievement makeNew();
   
   public void announceAcquired(UUID playerId){
      ArcanaPlayerData data = ArcanaNovum.data(playerId);
      MinecraftServer server = BorisLib.SERVER;
      ServerPlayer player = server.getPlayerList().getPlayer(playerId);
      List<MutableComponent> msgs = new ArrayList<>();
      MutableComponent descComp = Component.literal("\n");
      if(hidden){
         descComp.append(Component.literal("???")).withStyle(ChatFormatting.DARK_PURPLE);
      }else{
         List<Component> descLines = getDescription();
         for(int i = 0; i < descLines.size(); i++){
            if(i > 0) descComp.append(Component.literal("\n"));
            descComp.append(descLines.get(i).copy().withStyle(ChatFormatting.DARK_PURPLE));
         }
      }
      
      if(id.equals(ArcanaAchievements.ALL_ACHIEVEMENTS.id)){
         msgs.add(Component.literal("=============================================").withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE));
         msgs.add(Component.literal("")
               .append(Component.literal("=== ").withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.BOLD, ChatFormatting.BLACK))
               .append(player == null ? Component.literal(data.getUsername()).withStyle(ChatFormatting.LIGHT_PURPLE) : player.getDisplayName())
               .append(Component.literal(" has mastered all Arcana Achievements and became ").withStyle(ChatFormatting.DARK_PURPLE))
               .append((Component.literal("[").append(getTranslatedName()).append(Component.literal("]"))).withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(
                           Component.literal("")
                                 .append(getTranslatedName().withStyle(ChatFormatting.DARK_AQUA))
                                 .append(descComp)
                                 .append(Component.literal("")
                                       .append(Component.literal("\n" + TextUtils.readableInt(xpReward)).withStyle(ChatFormatting.AQUA))
                                       .append(Component.literal(" XP").withStyle(ChatFormatting.DARK_AQUA))
                                       .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_AQUA))
                                       .append(Component.literal("" + pointsReward).withStyle(ChatFormatting.AQUA))
                                       .append(Component.literal(" Skill Points").withStyle(ChatFormatting.DARK_AQUA)))))
                     .withColor(ChatFormatting.DARK_AQUA).withBold(true)))
               .append(Component.literal("!!!").withStyle(ChatFormatting.DARK_PURPLE))
               .append(Component.literal(" ===").withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.BOLD, ChatFormatting.BLACK)));
         msgs.add(Component.literal("=============================================").withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE));
         if(player != null) SoundUtils.playSongToPlayer(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
      }else if(pointsReward >= 5){
         msgs.add(Component.literal("")
               .append(player == null ? Component.literal(data.getUsername()).withStyle(ChatFormatting.LIGHT_PURPLE) : player.getDisplayName())
               .append(Component.literal(" has made the Arcana Achievement ").withStyle(ChatFormatting.DARK_PURPLE))
               .append((Component.literal("[").append(getTranslatedName()).append(Component.literal("]"))).withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(
                           Component.literal("")
                                 .append(getTranslatedName().withStyle(ChatFormatting.DARK_AQUA))
                                 .append(descComp)
                                 .append(Component.literal("")
                                       .append(Component.literal("\n" + TextUtils.readableInt(xpReward)).withStyle(ChatFormatting.AQUA))
                                       .append(Component.literal(" XP").withStyle(ChatFormatting.DARK_AQUA))
                                       .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_AQUA))
                                       .append(Component.literal("" + pointsReward).withStyle(ChatFormatting.AQUA))
                                       .append(Component.literal(" Skill Points").withStyle(ChatFormatting.DARK_AQUA)))))
                     .withColor(ChatFormatting.DARK_AQUA).withBold(true)))
               .append(Component.literal("!!!").withStyle(ChatFormatting.DARK_PURPLE)));
         if(player != null) SoundUtils.playSongToPlayer(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
      }else{
         msgs.add(Component.literal("")
               .append(player == null ? Component.literal(data.getUsername()).withStyle(ChatFormatting.LIGHT_PURPLE) : player.getDisplayName())
               .append(Component.literal(" has made the Arcana Achievement ").withStyle(ChatFormatting.LIGHT_PURPLE))
               .append((Component.literal("[").append(getTranslatedName()).append(Component.literal("]"))).withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(
                           Component.literal("")
                                 .append(getTranslatedName().withStyle(ChatFormatting.AQUA))
                                 .append(descComp)
                                 .append(Component.literal("")
                                       .append(Component.literal("\n" + TextUtils.readableInt(xpReward)).withStyle(ChatFormatting.AQUA))
                                       .append(Component.literal(" XP").withStyle(ChatFormatting.DARK_AQUA))
                                       .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_AQUA))
                                       .append(Component.literal("" + pointsReward).withStyle(ChatFormatting.AQUA))
                                       .append(Component.literal(" Skill Points").withStyle(ChatFormatting.DARK_AQUA)))))
                     .withColor(ChatFormatting.AQUA)))
               .append(Component.literal("!").withStyle(ChatFormatting.LIGHT_PURPLE)));
         
         if(player != null) SoundUtils.playSongToPlayer(player, SoundEvents.PLAYER_LEVELUP, 1, 1);
      }
      if(ArcanaNovum.CONFIG.getBoolean(ArcanaConfig.ANNOUNCE_ACHIEVEMENTS)){
         for(MutableComponent msg : msgs){
            server.getPlayerList().broadcastSystemMessage(msg, false);
         }
      }else{
         if(player != null){
            for(MutableComponent msg : msgs){
               player.displayClientMessage(msg, false);
            }
         }
      }
      data.addXP(xpReward); // Add xp
      
      boolean abyssCheck = true;
      boolean acolyteCheck = true;
      Set<ArcanaAchievement> abyssalExcluded = ArcanaAchievements.getOwtAExcludedAchievements(playerId);
      Set<ArcanaAchievement> acolyteExcluded = ArcanaAchievements.getAcolyteExcludedAchievements(playerId);
      for(ArcanaAchievement achievement : ArcanaAchievements.ARCANA_ACHIEVEMENTS.values()){
         if(!data.hasAcheivement(achievement)){
            if(!abyssalExcluded.contains(achievement)) abyssCheck = false;
            if(!acolyteExcluded.contains(achievement)) acolyteCheck = false;
         }
         if(!abyssCheck && !acolyteCheck) break;
      }
      if(abyssCheck){
         ArcanaAchievements.grant(playerId, ArcanaAchievements.ALL_ACHIEVEMENTS);
      }else if(acolyteCheck){
         ArcanaAchievements.grant(playerId, ArcanaAchievements.MOST_ACHIEVEMENTS);
      }
   }
}
