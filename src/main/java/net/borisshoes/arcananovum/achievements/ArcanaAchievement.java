package net.borisshoes.arcananovum.achievements;

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
         MutableText acquiredMsg = Text.literal("")
               .append(player.getDisplayName())
               .append(Text.literal(" has made the Arcana Achievement ").formatted(Formatting.LIGHT_PURPLE))
               .append(Text.literal("["+name+"]").styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                           Text.literal("")
                                 .append(Text.literal(name).formatted(Formatting.AQUA))
                                 .append(Text.literal(descriptionText.toString()).formatted(Formatting.LIGHT_PURPLE))))
                     .withColor(Formatting.AQUA)))
               .append(Text.literal("!").formatted(Formatting.LIGHT_PURPLE));
         server.getPlayerManager().broadcast(acquiredMsg, false);
      }
      SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_PLAYER_LEVELUP,1,1);
   }
}
