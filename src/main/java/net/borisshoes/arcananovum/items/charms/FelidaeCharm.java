package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class FelidaeCharm extends ArcanaItem {
	public static final String ID = "felidae_charm";
   
   private static final String TXT = "item/felidae_charm";
   private static final String TXT_PANTHERA = "item/felidae_charm_panthera";
   
   public FelidaeCharm(){
      id = ID;
      name = "Charm of Felidae";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EXOTIC, TomeGui.TomeFilter.CHARMS, TomeGui.TomeFilter.ITEMS};
      itemVersion = 1;
      vanillaItem = Items.STRING;
      item = new FelidaeCharmItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.YELLOW))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      models.add(new Pair<>(vanillaItem,TXT_PANTHERA));
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_CREEPER_HEAD,ResearchTasks.ADVANCEMENT_COMPLETE_CATALOGUE,ResearchTasks.CAT_SCARE,ResearchTasks.FEATHER_FALL,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("The charm ").formatted(Formatting.GOLD))
            .append(Text.literal("purrs ").formatted(Formatting.YELLOW))
            .append(Text.literal("softly when worn.").formatted(Formatting.GOLD)));
      lore.add(Text.literal("")
            .append(Text.literal("Keeping this ").formatted(Formatting.GOLD))
            .append(Text.literal("charm ").formatted(Formatting.YELLOW))
            .append(Text.literal("on your person gives you ").formatted(Formatting.GOLD))
            .append(Text.literal("cat-like").formatted(Formatting.GRAY))
            .append(Text.literal(" abilities.").formatted(Formatting.GOLD)));
      lore.add(Text.literal("")
            .append(Text.literal("Your ").formatted(Formatting.GOLD))
            .append(Text.literal("falls ").formatted(Formatting.GRAY))
            .append(Text.literal("become somewhat ").formatted(Formatting.GOLD))
            .append(Text.literal("graceful ").formatted(Formatting.AQUA))
            .append(Text.literal("and ").formatted(Formatting.GOLD))
            .append(Text.literal("cushioned").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.GOLD)));
      lore.add(Text.literal("")
            .append(Text.literal("Creepers ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("and ").formatted(Formatting.GOLD))
            .append(Text.literal("Phantoms ").formatted(Formatting.BLUE))
            .append(Text.literal("give you a ").formatted(Formatting.GOLD))
            .append(Text.literal("wide berth").formatted(Formatting.YELLOW))
            .append(Text.literal(".").formatted(Formatting.GOLD)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.GUNPOWDER,16);
      ArcanaIngredient b = new ArcanaIngredient(Items.STRING,12);
      ArcanaIngredient c = new ArcanaIngredient(Items.PUFFERFISH,16);
      ArcanaIngredient g = new ArcanaIngredient(Items.PHANTOM_MEMBRANE,4);
      ArcanaIngredient w = new ArcanaIngredient(Items.SALMON,16);
      ArcanaIngredient h = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.FEATHER_FALLING),4));
      ArcanaIngredient k = new ArcanaIngredient(Items.COD,16);
      ArcanaIngredient m = new ArcanaIngredient(Items.CREEPER_HEAD,1, true);
      ArcanaIngredient o = new ArcanaIngredient(Items.TROPICAL_FISH,16);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {k,h,m,h,o},
            {b,g,h,g,b},
            {a,b,w,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withEnchanter());
      
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("   Charm of Felidae\n\nRarity: Empowered\n\nCats are quite powerful creatures, managing to frighten phantoms and scare creepers. They can even fall from any height without care.\nThis Charm seeks to mimic a fraction of that power.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Charm of Felidae\n\nThe Charm halves all fall damage, stops phantoms from swooping the holder, and gives creepers a good scare every now and then.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class FelidaeCharmItem extends ArcanaPolymerItem {
      public FelidaeCharmItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!ArcanaItemUtils.isArcane(itemStack)) return ArcanaRegistry.getModelData(TXT).value();
         return ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.PANTHERA.id) >= 1 ? ArcanaRegistry.getModelData(TXT_PANTHERA).value() : ArcanaRegistry.getModelData(TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand){
         ItemStack stack = playerEntity.getStackInHand(hand);
         if(!(playerEntity instanceof ServerPlayerEntity player)) return TypedActionResult.pass(stack);
         SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_CAT_AMBIENT, 1f, (float) (0.5*(Math.random()-0.5)+1));
         return TypedActionResult.success(stack);
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         if(world.getServer().getTicks() % 20 == 0 && !player.isSpectator()){
            Vec3d pos = player.getPos();
            Box rangeBox = new Box(pos.x+5,pos.y+3,pos.z+5,pos.x-5,pos.y-3,pos.z-5);
            List<Entity> entities = world.getOtherEntities(null,rangeBox, e -> !e.isSpectator() && e instanceof CreeperEntity);
            if(entities.size() >= 4) ArcanaAchievements.grant(player,ArcanaAchievements.INFILTRATION.id);
         }
      }
   }
}

