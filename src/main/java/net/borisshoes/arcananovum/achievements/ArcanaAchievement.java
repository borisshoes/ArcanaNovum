package net.borisshoes.arcananovum.achievements;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
   
   public MutableText getTranslatedName(){
      return Text.translatableWithFallback(getTranslationKey(),name);
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
   
   public abstract NbtCompound toNbt();
   
   public abstract ArcanaAchievement fromNbt(String id, NbtCompound nbt);
   
   public abstract MutableText[] getStatusDisplay(ServerPlayerEntity player);
   
   public abstract ArcanaAchievement makeNew();
   
   public void announceAcquired(ServerPlayerEntity player){
      StringBuilder descriptionText = new StringBuilder();
      for(String d : description){
         descriptionText.append("\n").append(d);
      }
      
      MinecraftServer server = player.getServer();
      if(server != null){
         List<MutableText> msgs = new ArrayList<>();
         
         if(id.equals(ArcanaAchievements.ALL_ACHIEVEMENTS.id)){
            msgs.add(Text.literal("=============================================").formatted(Formatting.BOLD,Formatting.LIGHT_PURPLE));
            msgs.add(Text.literal("")
                  .append(Text.literal("=== ").formatted(Formatting.OBFUSCATED,Formatting.BOLD,Formatting.BLACK))
                  .append(player.getDisplayName())
                  .append(Text.literal(" has mastered all Arcana Achievements and became ").formatted(Formatting.DARK_PURPLE))
                  .append((Text.literal("[").append(getTranslatedName()).append(Text.literal("]"))).styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                              Text.literal("")
                                    .append(getTranslatedName().formatted(Formatting.DARK_AQUA))
                                    .append(Text.literal(descriptionText.toString()).formatted(Formatting.DARK_PURPLE))
                                    .append(Text.literal("")
                                          .append(Text.literal("\n"+xpReward).formatted(Formatting.AQUA))
                                          .append(Text.literal(" XP").formatted(Formatting.DARK_AQUA))
                                          .append(Text.literal(" | ").formatted(Formatting.DARK_AQUA))
                                          .append(Text.literal(""+pointsReward).formatted(Formatting.AQUA))
                                          .append(Text.literal(" Skill Points").formatted(Formatting.DARK_AQUA)))))
                        .withColor(Formatting.DARK_AQUA).withBold(true)))
                  .append(Text.literal("!!!").formatted(Formatting.DARK_PURPLE))
                  .append(Text.literal(" ===").formatted(Formatting.OBFUSCATED,Formatting.BOLD,Formatting.BLACK)));
            msgs.add(Text.literal("=============================================").formatted(Formatting.BOLD,Formatting.LIGHT_PURPLE));
            SoundUtils.playSongToPlayer(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,1,1);
         }else if(pointsReward >= 5){
            msgs.add(Text.literal("")
                  .append(player.getDisplayName())
                  .append(Text.literal(" has made the Arcana Achievement ").formatted(Formatting.DARK_PURPLE))
                  .append((Text.literal("[").append(getTranslatedName()).append(Text.literal("]"))).styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                              Text.literal("")
                                    .append(getTranslatedName().formatted(Formatting.DARK_AQUA))
                                    .append(Text.literal(descriptionText.toString()).formatted(Formatting.DARK_PURPLE))
                                    .append(Text.literal("")
                                          .append(Text.literal("\n"+xpReward).formatted(Formatting.AQUA))
                                          .append(Text.literal(" XP").formatted(Formatting.DARK_AQUA))
                                          .append(Text.literal(" | ").formatted(Formatting.DARK_AQUA))
                                          .append(Text.literal(""+pointsReward).formatted(Formatting.AQUA))
                                          .append(Text.literal(" Skill Points").formatted(Formatting.DARK_AQUA)))))
                        .withColor(Formatting.DARK_AQUA).withBold(true)))
                  .append(Text.literal("!!!").formatted(Formatting.DARK_PURPLE)));
            SoundUtils.playSongToPlayer(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,1,1);
         }else{
            msgs.add(Text.literal("")
                  .append(player.getDisplayName())
                  .append(Text.literal(" has made the Arcana Achievement ").formatted(Formatting.LIGHT_PURPLE))
                  .append((Text.literal("[").append(getTranslatedName()).append(Text.literal("]"))).styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                              Text.literal("")
                                    .append(getTranslatedName().formatted(Formatting.AQUA))
                                    .append(Text.literal(descriptionText.toString()).formatted(Formatting.LIGHT_PURPLE))
                                    .append(Text.literal("")
                                          .append(Text.literal("\n"+xpReward).formatted(Formatting.AQUA))
                                          .append(Text.literal(" XP").formatted(Formatting.DARK_AQUA))
                                          .append(Text.literal(" | ").formatted(Formatting.DARK_AQUA))
                                          .append(Text.literal(""+pointsReward).formatted(Formatting.AQUA))
                                          .append(Text.literal(" Skill Points").formatted(Formatting.DARK_AQUA)))))
                        .withColor(Formatting.AQUA)))
                  .append(Text.literal("!").formatted(Formatting.LIGHT_PURPLE)));
            
            SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_PLAYER_LEVELUP,1,1);
         }
         if(ArcanaConfig.getBoolean(ArcanaRegistry.ANNOUNCE_ACHIEVEMENTS)){
            for(MutableText msg : msgs){
               server.getPlayerManager().broadcast(msg, false);
            }
         }else{
            for(MutableText msg : msgs){
               player.sendMessage(msg, false);
            }
         }
      }
      IArcanaProfileComponent profile = ArcanaNovum.data(player);
      profile.addXP(xpReward); // Add xp
   
      boolean abyssCheck = true;
      for(ArcanaAchievement achievement : ArcanaAchievements.registry.values()){
         if(ArcanaAchievements.excludedAchievements.contains(achievement)) continue;
         if(!profile.hasAcheivement(achievement.getArcanaItem().getId(),achievement.id)){
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
