package net.borisshoes.arcananovum.achievements;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.items.core.MagicItem;
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

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

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
   private final MagicItem magicItem;
   private final String[] description;
   public final int xpReward;
   public final int pointsReward;
   
   protected ArcanaAchievement(String name, String id, int type, ItemStack displayItem, MagicItem magicItem, int xpReward, int pointsReward, String[] description){
      this.name = name;
      this.id = id;
      this.type = type;
      this.displayItem = displayItem;
      this.magicItem = magicItem;
      this.description = description;
      this.xpReward = xpReward;
      this.pointsReward = pointsReward;
      this.acquired = false;
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
   
   public MagicItem getMagicItem(){
      return magicItem;
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
         MutableText acquiredMsg;
         if(pointsReward >= 5){
            acquiredMsg = Text.literal("")
                  .append(player.getDisplayName())
                  .append(Text.literal(" has made the Arcana Achievement ").formatted(Formatting.DARK_PURPLE))
                  .append(Text.literal("[" + name + "]").styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                              Text.literal("")
                                    .append(Text.literal(name).formatted(Formatting.DARK_AQUA))
                                    .append(Text.literal(descriptionText.toString()).formatted(Formatting.DARK_PURPLE))
                                    .append(Text.literal("")
                                          .append(Text.literal("\n"+xpReward).formatted(Formatting.AQUA))
                                          .append(Text.literal(" XP").formatted(Formatting.DARK_AQUA))
                                          .append(Text.literal(" | ").formatted(Formatting.DARK_AQUA))
                                          .append(Text.literal(""+pointsReward).formatted(Formatting.AQUA))
                                          .append(Text.literal(" Skill Points").formatted(Formatting.DARK_AQUA)))))
                        .withColor(Formatting.DARK_AQUA).withBold(true)))
                  .append(Text.literal("!!!").formatted(Formatting.DARK_PURPLE));
            SoundUtils.playSongToPlayer(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,1,1);
         }else{
            acquiredMsg = Text.literal("")
                  .append(player.getDisplayName())
                  .append(Text.literal(" has made the Arcana Achievement ").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.literal("[" + name + "]").styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                              Text.literal("")
                                    .append(Text.literal(name).formatted(Formatting.AQUA))
                                    .append(Text.literal(descriptionText.toString()).formatted(Formatting.LIGHT_PURPLE))
                                    .append(Text.literal("")
                                          .append(Text.literal("\n"+xpReward).formatted(Formatting.AQUA))
                                          .append(Text.literal(" XP").formatted(Formatting.DARK_AQUA))
                                          .append(Text.literal(" | ").formatted(Formatting.DARK_AQUA))
                                          .append(Text.literal(""+pointsReward).formatted(Formatting.AQUA))
                                          .append(Text.literal(" Skill Points").formatted(Formatting.DARK_AQUA)))))
                        .withColor(Formatting.AQUA)))
                  .append(Text.literal("!").formatted(Formatting.LIGHT_PURPLE));
            
            SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_PLAYER_LEVELUP,1,1);
         }
         if((boolean) Arcananovum.config.getValue("announceAchievements")){
            server.getPlayerManager().broadcast(acquiredMsg, false);
         }else{
            player.sendMessage(acquiredMsg,false);
         }
      }
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      profile.addXP(xpReward); // Add xp
   
      boolean abyssCheck = true;
      for(ArcanaAchievement achievement : ArcanaAchievements.allNonMythical){
         if(!profile.hasAcheivement(achievement.getMagicItem().getId(),achievement.id)){
            abyssCheck = false;
            break;
         }
      }
      if(abyssCheck){
         for(ArcanaAchievement achievement : ArcanaAchievements.allMythical){
            ArcanaAchievements.grant(player,achievement.id);
         }
         ArcanaAchievements.grant(player,"all_achievements");
      }
   }
}
