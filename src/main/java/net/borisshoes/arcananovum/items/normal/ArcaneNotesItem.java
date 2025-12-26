package net.borisshoes.arcananovum.items.normal;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.NormalPolymerItem;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

public class ArcaneNotesItem extends NormalPolymerItem {
   
   public static final String UNLOCK_ID_TAG = "research_id";
   public static final String AUTHOR_TAG = "author";
   public static final String COST_TAG = "paper_cost";
   
   public ArcaneNotesItem(String id, net.minecraft.world.item.Item.Properties settings){
      super(id, settings);
   }
   
   public static ItemStack buildLore(ItemStack stack){
      if(!stack.is(ArcanaRegistry.ARCANE_NOTES)) return stack;
      
      List<Component> loreText = new ArrayList<>();
      String id = ArcanaItem.getStringProperty(stack,UNLOCK_ID_TAG);
      ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(id);
      
      if(arcanaItem != null){
         loreText.add(Component.literal("")
               .append(TextUtils.removeItalics(Component.literal("Contains notes for the ").withStyle(ChatFormatting.DARK_PURPLE)))
               .append(TextUtils.removeItalics(Component.translatable(arcanaItem.getItem().getDescriptionId()).withStyle(ArcanaRarity.getColor(arcanaItem.getRarity())))));
      
         String author = ArcanaItem.getStringProperty(stack,AUTHOR_TAG);
         if(!author.isEmpty()){
            loreText.add(Component.literal("")
                  .append(Component.literal("Scribed by ").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))
                  .append(TextUtils.removeItalics(Component.literal(author).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC))));
         }
      }
      
      stack.set(DataComponents.LORE,new ItemLore(loreText,loreText));
      
      return stack;
   }
   
   @Override
   public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks){
      String id = ArcanaItem.getStringProperty(stack,UNLOCK_ID_TAG);
      ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(id);
      if(arcanaItem != null && user instanceof ServerPlayer player){
         ArcanaEffectUtils.arcaneNotesAnim(player,arcanaItem,remainingUseTicks);
         int maxUseTime = getUseDuration(stack,user);
         boolean alreadyUnlocked = ArcanaNovum.data(player).hasResearched(arcanaItem);
         
         if(alreadyUnlocked){
            float pitch = 1f + ((float) (maxUseTime - remainingUseTicks) / maxUseTime);
            if(remainingUseTicks % 8 == 0){
               SoundUtils.playSongToPlayer(player, SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED, 2, pitch);
            }else if(remainingUseTicks % 4 == 0){
               SoundUtils.playSongToPlayer(player, SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED, 2, pitch);
            }
         }else{
            if(remainingUseTicks % 7 == 0){
               float pitch = 0.7f + 1.3f*((float) (maxUseTime - remainingUseTicks) / maxUseTime);
               SoundUtils.playSongToPlayer(player, SoundEvents.BOOK_PAGE_TURN, 1, pitch);
            }
         }
         
      }
      
      super.onUseTick(world, user, stack, remainingUseTicks);
   }
   
   @Override
   public InteractionResult use(Level world, Player user, InteractionHand hand){
      ItemStack itemStack = user.getItemInHand(hand);
      if(ArcanaItem.hasProperty(itemStack,UNLOCK_ID_TAG)){
         user.startUsingItem(hand);
         return InteractionResult.CONSUME;
      }else{
         return super.use(world, user, hand);
      }
   }
   
   @Override
   public int getUseDuration(ItemStack stack, LivingEntity user){
      return ArcanaItem.hasProperty(stack,UNLOCK_ID_TAG) ? 120 : 0;
   }
   
   @Override
   public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user){
      String id = ArcanaItem.getStringProperty(stack,UNLOCK_ID_TAG);
      ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(id);
      if(arcanaItem != null && user instanceof ServerPlayer player){
         boolean alreadyUnlocked = ArcanaNovum.data(player).hasResearched(arcanaItem);
         if(alreadyUnlocked){
            SoundUtils.playSongToPlayer(player, SoundEvents.AZALEA_BREAK, 2, 0.6f);
            SoundUtils.playSongToPlayer(player, SoundEvents.ENCHANTMENT_TABLE_USE, 1, 0.5f);
            
            ItemStack paper = new ItemStack(ArcanaRarity.getArcanePaper(arcanaItem.getRarity()));
            int cost = ArcanaItem.getIntProperty(stack,COST_TAG);
            if(cost != 0){
               paper.setCount(cost);
            }else{
               paper.setCount(player.getRandom().nextIntBetweenInclusive(5,24));
               ArcanaNovum.data(player).addXP(ArcanaRarity.getCraftXp(arcanaItem.getRarity()));
            }
            MinecraftUtils.returnItems(new SimpleContainer(paper),player);
         }else{
            SoundUtils.playSongToPlayer(player, SoundEvents.ENCHANTMENT_TABLE_USE, 2, 0.8f);
            ArcanaNovum.data(player).addResearchedItem(arcanaItem.getId());
         }
         ArcanaEffectUtils.arcaneNotesFinish(player,arcanaItem);
      }
      if(!user.hasInfiniteMaterials()) stack.shrink(1);
      return stack;
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, PacketContext context){
      return Items.BOOK;
   }
}
