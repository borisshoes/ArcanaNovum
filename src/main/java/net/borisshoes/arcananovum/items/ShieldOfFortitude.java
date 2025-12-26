package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.callbacks.ShieldTimerCallback;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ShieldOfFortitude extends ArcanaItem {
   public static final String ID = "shield_of_fortitude";
   public static final Identifier EFFECT_ID = Identifier.fromNamespaceAndPath(ArcanaNovum.MOD_ID,ID);
   
   public ShieldOfFortitude(){
      id = ID;
      name = "Shield of Fortitude";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.EQUIPMENT};
      vanillaItem = Items.SHIELD;
      item = new ShieldOfFortitudeItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.EFFECT_ABSORPTION,ResearchTasks.ADVANCEMENT_DEFLECT_ARROW,ResearchTasks.UNLOCK_STELLAR_CORE,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("This shield is ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("overflowing").withStyle(ChatFormatting.ITALIC, ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" with powerful ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("defensive magic").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Your will for ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("protection").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" becomes a tangible ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("fortitude").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Damage").withStyle(ChatFormatting.RED))
            .append(Component.literal(" blocked").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" becomes ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("absorption hearts").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" and the shield is ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("unbreakable").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      BannerPatternLayers patterns = stack.get(DataComponents.BANNER_PATTERNS);
      DyeColor color = stack.get(DataComponents.BASE_COLOR);
      ItemStack newStack = super.updateItem(stack,server);
      if(patterns != null) stack.set(DataComponents.BANNER_PATTERNS,patterns);
      if(color != null) stack.set(DataComponents.BASE_COLOR,color);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public ItemStack forgeItem(Container inv, StarlightForgeBlockEntity starlightForge){
      ItemStack shieldStack = inv.getItem(12); // Should be the Sword
      ItemStack newArcanaItem = getNewItem();
      
      if(shieldStack.isEnchanted()){
         EnchantmentHelper.setEnchantments(newArcanaItem,shieldStack.getEnchantments());
      }
      
      BannerPatternLayers patterns = shieldStack.get(DataComponents.BANNER_PATTERNS);
      DyeColor color = shieldStack.get(DataComponents.BASE_COLOR);
      if(color != null){
         newArcanaItem.set(DataComponents.BASE_COLOR,color);
      }
      if(patterns != null){
         newArcanaItem.set(DataComponents.BANNER_PATTERNS,patterns);
      }

      return newArcanaItem;
   }
   
   public void shieldBlock(LivingEntity entity, ItemStack item, double amount){
      float maxAbs = 10 + 2*Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.SHIELD_OF_FAITH.id));
      float curAbs = entity.getAbsorptionAmount();
      float addedAbs = (float) Math.min(maxAbs,amount*.5);
      int duration = 200 + 100*Math.max(0,ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.SHIELD_OF_RESILIENCE.id));
      if(entity instanceof ServerPlayer player){
         BorisLib.addTickTimerCallback(new ShieldTimerCallback(duration,item,player,addedAbs));
         SoundUtils.playSongToPlayer(player, SoundEvents.ENCHANTMENT_TABLE_USE, 1, 1.8f);
      }
      MinecraftUtils.addMaxAbsorption(entity, EFFECT_ID,addedAbs);
      entity.setAbsorptionAmount((curAbs + addedAbs));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("Shield of Fortitude").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nThis Shield is my attempt at pouring Arcana into a fully defensive item. The Netherite and obsidian reinforced Shield absorbs the energy of impacts and reconfigures it into a fortification barrier ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Shield of Fortitude").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\naround myself by invoking all four basic protection enchantments and mimicking the effect of golden apples.\n\nHalf of all damage blocked by the Shield becomes an absorption barrier lasting 10 seconds.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Shield of Fortitude").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nDisabling the Shield with an axe causes the absorption barrier to shatter prematurely.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.NETHER_STAR,2);
      ArcanaIngredient b = new ArcanaIngredient(Items.OBSIDIAN,32);
      ArcanaIngredient r = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentInstance(MinecraftUtils.getEnchantment(Enchantments.FIRE_PROTECTION),4));
      ArcanaIngredient c = new ArcanaIngredient(Items.NETHERITE_INGOT,1);
      ArcanaIngredient g = new ArcanaIngredient(Items.GOLDEN_APPLE,16);
      ArcanaIngredient h = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentInstance(MinecraftUtils.getEnchantment(Enchantments.BLAST_PROTECTION),4));
      ArcanaIngredient l = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentInstance(MinecraftUtils.getEnchantment(Enchantments.PROTECTION),4));
      ArcanaIngredient m = new ArcanaIngredient(Items.SHIELD,1, true);
      ArcanaIngredient n = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentInstance(MinecraftUtils.getEnchantment(Enchantments.PROJECTILE_PROTECTION),4));
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,l,m,n,c},
            {b,g,r,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withAnvil().withCore().withEnchanter());
      
   }
   
   public class ShieldOfFortitudeItem extends ArcanaPolymerItem {
      public ShieldOfFortitudeItem(){
         super(getThis(),
               getEquipmentArcanaItemComponents()
                     .equippableUnswappable(EquipmentSlot.OFFHAND)
                     .component(DataComponents.BLOCKS_ATTACKS,
                           new BlocksAttacks(
                                 0.25F,
                                 1.0F,
                                 List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)),
                                 new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                                 Optional.of(DamageTypeTags.BYPASSES_SHIELD),
                                 Optional.of(SoundEvents.SHIELD_BLOCK),
                                 Optional.of(SoundEvents.SHIELD_BREAK)
                           ))
                     .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK)
                     .component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
         );
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack stack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(stack.has(DataComponents.BASE_COLOR) && !stack.has(DataComponents.CUSTOM_NAME)){
            stack.set(DataComponents.CUSTOM_NAME, TextUtils.removeItalics(this.getName(stack)));
         }
         return stack;
      }
   }
}

