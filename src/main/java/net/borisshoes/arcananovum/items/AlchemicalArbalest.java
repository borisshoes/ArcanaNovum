package net.borisshoes.arcananovum.items;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerCrossbowItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class AlchemicalArbalest extends ArcanaItem {
   public static final String ID = "alchemical_arbalest";
   
   public AlchemicalArbalest(){
      id = ID;
      name = "Alchemical Arbalest";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.EQUIPMENT};
      itemVersion = 0;
      vanillaItem = Items.CROSSBOW;
      item = new AlchemicalArbalestItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_AQUA);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.UNLOCK_STELLAR_CORE,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER,ResearchTasks.ADVANCEMENT_OL_BETSY,ResearchTasks.ADVANCEMENT_WHOS_THE_PILLAGER_NOW,ResearchTasks.ADVANCEMENT_ARBALISTIC,ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.OBTAIN_TIPPED_ARROW,ResearchTasks.ADVANCEMENT_BREW_POTION,ResearchTasks.ADVANCEMENT_DRAGON_BREATH};
      attributions = new Tuple[]{new Tuple<>(Component.translatable("credits_and_attribution.arcananovum.inspired_by"), Component.literal("Sethzilla42"))};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public void finalizePrefItem(MinecraftServer server){
      super.finalizePrefItem(server);
      ItemStack curPrefItem = this.getPrefItem();
      curPrefItem.set(DataComponents.ENCHANTMENTS, MinecraftUtils.makeEnchantComponent(new EnchantmentInstance(MinecraftUtils.getEnchantment(server.registryAccess(), Enchantments.MULTISHOT),1)));
      this.prefItem = buildItemLore(curPrefItem, server);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("This ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Crossbow ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("is outfitted with ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("enchanted ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("clockwork mechanisms").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("Tipped Arrows").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" fired from the ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("bow ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("create a ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("lingering ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("field").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("Spectral Arrows").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" create a zone of ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("damage ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("amplification").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Crossbow ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("is ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Unbreakable ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("and comes with ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Multishot").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      ItemStack newStack = super.updateItem(stack,server);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public ItemStack onAugment(ItemStack stack, ArcanaAugment augment, int level){
      if(augment == ArcanaAugments.RUNIC_ARBALEST && stack.isEnchanted()){ // Remove Multi-Shot type enchants
         ItemEnchantments.Mutable enchantBuilder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
         ItemEnchantments comp = stack.getEnchantments();
         Object2IntOpenHashMap<Holder<Enchantment>> enchants = new Object2IntOpenHashMap<>();
         comp.entrySet().forEach(entry -> enchants.addTo(entry.getKey(),entry.getIntValue()));
         
         enchants.forEach((e,num) -> {
            if(!e.value().effects().has(EnchantmentEffectComponents.PROJECTILE_COUNT)){
               enchantBuilder.upgrade(e,num);
            }
         });
         EnchantmentHelper.setEnchantments(stack,enchantBuilder.toImmutable());
      }
      return stack;
   }
   
   @Override
   public ItemStack forgeItem(Container inv, List<Integer> centerpieces, StarlightForgeBlockEntity starlightForge){
      ItemStack newArcanaItem = getNewItem();
      if(centerpieces.isEmpty()) return newArcanaItem;
      ItemStack bowStack = inv.getItem(centerpieces.getFirst()); // Should be the Crossbow
      if(bowStack.isEnchanted()){
         ItemEnchantments.Mutable enchantBuilder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
         ItemEnchantments comp = bowStack.getEnchantments();
         Object2IntOpenHashMap<Holder<Enchantment>> enchants = new Object2IntOpenHashMap<>();
         comp.entrySet().forEach(entry -> enchants.addTo(entry.getKey(),entry.getIntValue()));
         
         enchants.forEach((e,num) -> {
            if(!e.value().effects().has(EnchantmentEffectComponents.PROJECTILE_PIERCING)){
               enchantBuilder.upgrade(e,num);
            }
         });
         EnchantmentHelper.setEnchantments(newArcanaItem,enchantBuilder.toImmutable());
      }
      newArcanaItem.enchant(MinecraftUtils.getEnchantment(Enchantments.MULTISHOT),1);
      return newArcanaItem;
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("     Alchemical\n      Arbalest").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nWhile bows are excellent for sustained damage, crossbows have always been good at bursts of damage and area suppression. I believe I can enhance this niche further…").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("     Alchemical\n      Arbalest").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nThe Arbalest overcharges Tipped Arrows so that their effects cover a wide space and linger. It also comes with the multishot enchantment.\n\nSpectral Arrows are where things get interesting.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("     Alchemical\n      Arbalest").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\nThey carry no discrete effect but cause creatures to glow. Tweaking that ability a bit when used in the Arbalest, Spectral Arrows now create a cloud that makes weak spots on enemies easier to see, ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("     Alchemical\n      Arbalest").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD), Component.literal("\ncausing them to take increased damage from all sources.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class AlchemicalArbalestItem extends ArcanaPolymerCrossbowItem {
      
      public AlchemicalArbalestItem(){
         super(getThis(),getEquipmentArcanaItemComponents());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         List<String> stringList = new ArrayList<>();
         if(ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.RUNIC_ARBALEST) > 0){
            stringList.add("runic");
         }
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      public void verifyEnchantments(ItemStack stack){
         boolean hasMulti = EnchantmentHelper.has(stack, EnchantmentEffectComponents.PROJECTILE_COUNT);
         boolean hasPierce = EnchantmentHelper.has(stack, EnchantmentEffectComponents.PROJECTILE_PIERCING);
         boolean hasScatter = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SCATTERSHOT) > 0;
         boolean hasRunic = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.RUNIC_ARBALEST) > 0;
         
         ItemEnchantments.Mutable enchantBuilder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
         ItemEnchantments comp = stack.getEnchantments();
         Object2IntOpenHashMap<Holder<Enchantment>> enchants = new Object2IntOpenHashMap<>();
         comp.entrySet().forEach(entry -> enchants.addTo(entry.getKey(),entry.getIntValue()));
         
         if(hasRunic && hasMulti){ // Remove multishot
            enchants.forEach((e,num) -> {
               if(!e.value().effects().has(EnchantmentEffectComponents.PROJECTILE_COUNT)){
                  enchantBuilder.upgrade(e,num);
               }
            });
            EnchantmentHelper.setEnchantments(stack,enchantBuilder.toImmutable());

            verifyEnchantments(stack);
         }
         if(hasScatter && hasPierce){ // Remove pierce
            enchants.forEach((e,num) -> {
               if(!e.value().effects().has(EnchantmentEffectComponents.PROJECTILE_PIERCING)){
                  enchantBuilder.upgrade(e,num);
               }
            });
            EnchantmentHelper.setEnchantments(stack,enchantBuilder.toImmutable());
            
            verifyEnchantments(stack);
         }
         if(hasScatter && !hasMulti){ // Re-add multishot
            stack.enchant(MinecraftUtils.getEnchantment(Enchantments.MULTISHOT),1);
            verifyEnchantments(stack);
         }
      }
      
      @Override
      public InteractionResult use(Level world, Player user, InteractionHand hand){
         ItemStack itemStack = user.getItemInHand(hand);
         verifyEnchantments(itemStack);
         return super.use(world,user,hand);
      }
      
      
   }
}

