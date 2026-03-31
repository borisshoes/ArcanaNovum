package net.borisshoes.arcananovum.events;

import com.mojang.datafixers.util.Either;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.borisshoes.borislib.conditions.ConditionInstance;
import net.borisshoes.borislib.events.Event;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;

public class CleansingCharmEvent extends Event {
   public static final Identifier ID = ArcanaRegistry.arcanaId("cleansing_charm_event");
   
   private final ServerPlayer player;
   private final Either<Holder<MobEffect>, ConditionInstance> either;
   
   public CleansingCharmEvent(ServerPlayer player, Either<Holder<MobEffect>, ConditionInstance> either){
      super(ID, ((TimedAchievement) ArcanaAchievements.CHRONIC_AILMENT).getTimeFrame());
      this.player = player;
      this.either = either;
   }
   
   public boolean isEffect(){
      return either.left().isPresent();
   }
   
   public boolean isCondition(){
      return either.right().isPresent();
   }
   
   public Holder<MobEffect> getEffect(){
      return either.left().orElse(null);
   }
   
   public ConditionInstance getCondition(){
      return either.right().orElse(null);
   }
   
   public ServerPlayer getPlayer(){
      return player;
   }
}
