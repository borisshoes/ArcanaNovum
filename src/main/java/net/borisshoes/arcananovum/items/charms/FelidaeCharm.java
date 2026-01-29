package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.GeomanticStele;
import net.borisshoes.arcananovum.blocks.GeomanticSteleBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class FelidaeCharm extends ArcanaItem implements GeomanticStele.Interaction{
	public static final String ID = "felidae_charm";
   
   public FelidaeCharm(){
      id = ID;
      name = "Charm of Felidae";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.CHARMS, ArcaneTomeGui.TomeFilter.ITEMS};
      itemVersion = 1;
      vanillaItem = Items.STRING;
      item = new FelidaeCharmItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_CREEPER_HEAD,ResearchTasks.TAME_CAT,ResearchTasks.CAT_SCARE,ResearchTasks.FEATHER_FALL,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("The charm ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("purrs ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("softly when worn.").withStyle(ChatFormatting.GOLD)));
      lore.add(Component.literal("")
            .append(Component.literal("Keeping this ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("charm ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("on your person gives you ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("cat-like").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" abilities.").withStyle(ChatFormatting.GOLD)));
      lore.add(Component.literal("")
            .append(Component.literal("Your ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("falls ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("become somewhat ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("graceful ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("and ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("cushioned").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.GOLD)));
      lore.add(Component.literal("")
            .append(Component.literal("Creepers ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("and ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("Phantoms ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("give you a ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("wide berth").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.GOLD)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal(" Charm of Felidae").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nCats are quite powerful creatures, managing to frighten phantoms and creepers. They can even fall from any height without care.\n\nThis Charm seeks to mimic a fraction of  ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Charm of Felidae").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\ntheir majestic power.\n\nThe Charm halves all fall damage, stops phantoms from swooping the holder, and gives creepers a good scare if they get too close.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   @Override
   public Vec3 getBaseRange(){
      return new Vec3(15,15,15);
   }
   
   @Override
   public void steleTick(ServerLevel world, GeomanticSteleBlockEntity stele, ItemStack stack, Vec3 range){
      if(world.random.nextFloat() < 0.01){
         SoundUtils.playSound(world, stele.getBlockPos(), SoundEvents.CAT_AMBIENT, SoundSource.BLOCKS,1f, (float) (0.5*(Math.random()-0.5)+1));
      }
   }
   
   public class FelidaeCharmItem extends ArcanaPolymerItem {
      public FelidaeCharmItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         List<String> stringList = new ArrayList<>();
         if(ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.PANTHERA) >= 1){
            stringList.add("panthera");
         }
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public @NonNull ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public InteractionResult use(Level world, Player playerEntity, InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
         SoundUtils.playSongToPlayer(player, SoundEvents.CAT_AMBIENT, 1f, (float) (0.5*(Math.random()-0.5)+1));
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(entity instanceof ServerPlayer player)) return;
         if(world.getServer().getTickCount() % 20 == 0 && !player.isSpectator()){
            Vec3 pos = player.position();
            AABB rangeBox = new AABB(pos.x+5,pos.y+3,pos.z+5,pos.x-5,pos.y-3,pos.z-5);
            List<Entity> entities = world.getEntities((Entity) null,rangeBox, e -> !e.isSpectator() && e instanceof Creeper);
            if(entities.size() >= 4) ArcanaAchievements.grant(player,ArcanaAchievements.INFILTRATION);
         }
      }
   }
}

