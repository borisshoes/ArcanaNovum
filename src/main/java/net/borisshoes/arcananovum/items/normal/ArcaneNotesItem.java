package net.borisshoes.arcananovum.items.normal;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.NormalPolymerItem;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

public class ArcaneNotesItem extends NormalPolymerItem {
   
   public static final String UNLOCK_ID_TAG = "research_id";
   public static final String AUTHOR_TAG = "author";
   public static final String COST_TAG = "paper_cost";
   
   public ArcaneNotesItem(String id, Settings settings){
      super(id, settings);
   }
   
   public static ItemStack buildLore(ItemStack stack){
      if(!stack.isOf(ArcanaRegistry.ARCANE_NOTES)) return stack;
      
      List<Text> loreText = new ArrayList<>();
      String id = ArcanaItem.getStringProperty(stack,UNLOCK_ID_TAG);
      ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(id);
      
      if(arcanaItem != null){
         loreText.add(Text.literal("")
               .append(TextUtils.removeItalics(Text.literal("Contains notes for the ").formatted(Formatting.DARK_PURPLE)))
               .append(TextUtils.removeItalics(Text.translatable(arcanaItem.getItem().getTranslationKey()).formatted(ArcanaRarity.getColor(arcanaItem.getRarity())))));
      
         String author = ArcanaItem.getStringProperty(stack,AUTHOR_TAG);
         if(!author.isEmpty()){
            loreText.add(Text.literal("")
                  .append(Text.literal("Scribed by ").formatted(Formatting.DARK_PURPLE,Formatting.ITALIC))
                  .append(TextUtils.removeItalics(Text.literal(author).formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC))));
         }
      }
      
      stack.set(DataComponentTypes.LORE,new LoreComponent(loreText,loreText));
      
      return stack;
   }
   
   @Override
   public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks){
      String id = ArcanaItem.getStringProperty(stack,UNLOCK_ID_TAG);
      ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(id);
      if(arcanaItem != null && user instanceof ServerPlayerEntity player){
         ParticleEffectUtils.arcaneNotesAnim(player,arcanaItem,remainingUseTicks);
         int maxUseTime = getMaxUseTime(stack,user);
         boolean alreadyUnlocked = ArcanaNovum.data(player).hasResearched(arcanaItem);
         
         if(alreadyUnlocked){
            float pitch = 1f + ((float) (maxUseTime - remainingUseTicks) / maxUseTime);
            if(remainingUseTicks % 8 == 0){
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_CHISELED_BOOKSHELF_INSERT_ENCHANTED, 2, pitch);
            }else if(remainingUseTicks % 4 == 0){
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_CHISELED_BOOKSHELF_PICKUP_ENCHANTED, 2, pitch);
            }
         }else{
            if(remainingUseTicks % 7 == 0){
               float pitch = 0.7f + 1.3f*((float) (maxUseTime - remainingUseTicks) / maxUseTime);
               SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_BOOK_PAGE_TURN, 1, pitch);
            }
         }
         
      }
      
      super.usageTick(world, user, stack, remainingUseTicks);
   }
   
   @Override
   public ActionResult use(World world, PlayerEntity user, Hand hand){
      ItemStack itemStack = user.getStackInHand(hand);
      if(ArcanaItem.hasProperty(itemStack,UNLOCK_ID_TAG)){
         user.setCurrentHand(hand);
         return ActionResult.CONSUME;
      }else{
         return super.use(world, user, hand);
      }
   }
   
   @Override
   public int getMaxUseTime(ItemStack stack, LivingEntity user){
      return ArcanaItem.hasProperty(stack,UNLOCK_ID_TAG) ? 120 : 0;
   }
   
   @Override
   public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user){
      String id = ArcanaItem.getStringProperty(stack,UNLOCK_ID_TAG);
      ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(id);
      if(arcanaItem != null && user instanceof ServerPlayerEntity player){
         boolean alreadyUnlocked = ArcanaNovum.data(player).hasResearched(arcanaItem);
         if(alreadyUnlocked){
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_AZALEA_BREAK, 2, 0.6f);
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1, 0.5f);
            
            ItemStack paper = new ItemStack(ArcanaRarity.getArcanePaper(arcanaItem.getRarity()));
            int cost = ArcanaItem.getIntProperty(stack,COST_TAG);
            if(cost != 0){
               paper.setCount(cost);
            }else{
               paper.setCount(player.getRandom().nextBetween(5,24));
               ArcanaNovum.data(player).addXP(ArcanaRarity.getCraftXp(arcanaItem.getRarity()));
            }
            MiscUtils.returnItems(new SimpleInventory(paper),player);
         }else{
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 2, 0.8f);
            ArcanaNovum.data(player).addResearchedItem(arcanaItem.getId());
         }
         ParticleEffectUtils.arcaneNotesFinish(player,arcanaItem);
      }
      if(!user.isInCreativeMode()) stack.decrement(1);
      return stack;
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, PacketContext context){
      return Items.BOOK;
   }
}
