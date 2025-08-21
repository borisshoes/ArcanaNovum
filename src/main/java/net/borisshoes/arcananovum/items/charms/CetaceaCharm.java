package net.borisshoes.arcananovum.items.charms;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class CetaceaCharm extends ArcanaItem {
   public static final String ID = "cetacea_charm";
   
   public CetaceaCharm(){
      id = ID;
      name = "Charm of Cetacea";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ITEMS, TomeGui.TomeFilter.CHARMS};
      itemVersion = 0;
      vanillaItem = Items.COD;
      item = new CetaceaCharmItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BLUE,Formatting.BOLD);
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_CONDUIT, ResearchTasks.CATCH_FISH, ResearchTasks.EFFECT_DOLPHINS_GRACE, ResearchTasks.DROWNING_DAMAGE};
      attributions = new Pair[]{new Pair<>(Text.translatable("credits_and_attribution.arcananovum.texture_by"),Text.literal("Rookodzol"))};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,ACTIVE_TAG, true);
      setPrefStack(stack);
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      boolean active = getBooleanProperty(stack,ACTIVE_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,ACTIVE_TAG,active);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.AQUA))
            .append(Text.literal("charm ").formatted(Formatting.BLUE))
            .append(Text.literal("is ").formatted(Formatting.AQUA))
            .append(Text.literal("slippery ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("and ").formatted(Formatting.AQUA))
            .append(Text.literal("wet ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("like it just went for a ").formatted(Formatting.AQUA))
            .append(Text.literal("swim").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("Wearing the ").formatted(Formatting.AQUA))
            .append(Text.literal("charm ").formatted(Formatting.BLUE))
            .append(Text.literal("gives you the grace of a ").formatted(Formatting.AQUA))
            .append(Text.literal("dolphin ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("when in the ").formatted(Formatting.AQUA))
            .append(Text.literal("water").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("Sneak Right Click").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" to ").formatted(Formatting.AQUA))
            .append(Text.literal("disable ").formatted(Formatting.BLUE))
            .append(Text.literal("the ").formatted(Formatting.AQUA))
            .append(Text.literal("charm's").formatted(Formatting.BLUE))
            .append(Text.literal(" effect.").formatted(Formatting.AQUA)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.POTION,1).withPotions(Potions.LONG_WATER_BREATHING);
      ArcanaIngredient q = new ArcanaIngredient(Items.COD,16);
      ArcanaIngredient b = new ArcanaIngredient(Items.PRISMARINE_CRYSTALS,4);
      ArcanaIngredient c = new ArcanaIngredient(Items.TURTLE_SCUTE,4);
      ArcanaIngredient s = new ArcanaIngredient(Items.SALMON,16);
      ArcanaIngredient e = new ArcanaIngredient(Items.POTION,1).withPotions(Potions.STRONG_SWIFTNESS);
      ArcanaIngredient g = new ArcanaIngredient(Items.PUFFERFISH,16);
      ArcanaIngredient h = new ArcanaIngredient(Items.NAUTILUS_SHELL,1);
      ArcanaIngredient i = new ArcanaIngredient(Items.TROPICAL_FISH,16);
      ArcanaIngredient m = new ArcanaIngredient(Items.CONDUIT,1,true);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,e},
            {b,g,h,i,b},
            {c,h,m,h,c},
            {b,q,h,s,b},
            {e,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal(" Charm of Cetacea").formatted(Formatting.BLUE,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nDolphins are such graceful creatures, and this conduit that I have reconstructed opens up some possibilities. I believe I can miniaturize the conduit such that it gives me similar aquatic  ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal(" Charm of Cetacea").formatted(Formatting.BLUE,Formatting.BOLD),Text.literal("\nmaneuverability as dolphins.\n\nSneak Using the Charm toggles the aquatic buffs.\n").formatted(Formatting.BLACK)));
      
      return list;
   }
   
   public class CetaceaCharmItem extends ArcanaPolymerItem {
      public CetaceaCharmItem(){
         super(getThis());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         boolean active = getBooleanProperty(itemStack,ACTIVE_TAG);
         boolean delphinidae = ArcanaAugments.getAugmentOnItem(itemStack, ArcanaAugments.DELPHINIDAE) > 0;
         
         List<String> stringList = new ArrayList<>();
         if(active){
            if(delphinidae){
               stringList.add("delphinidae_on");
            }else{
               stringList.add("on");
            }
         }else{
            if(delphinidae){
               stringList.add("delphinidae_off");
            }else{
               stringList.add("off");
            }
         }
         baseStack.set(DataComponentTypes.CUSTOM_MODEL_DATA,new CustomModelDataComponent(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld && entity instanceof ServerPlayerEntity player)) return;
         
         boolean active = getBooleanProperty(stack,ACTIVE_TAG);
         boolean delphinidae = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.DELPHINIDAE) > 0;
         boolean gills = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.GILLS) > 0;
         
         if(active){
            if(player.isTouchingWater()){
               StatusEffectInstance grace = new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 110, delphinidae ? 1 : 0, false, false, true);
               player.addStatusEffect(grace);
               
               if(world.getServer().getTicks() % 20 == 0){
                  ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.CETACEA_CHARM_PER_SECOND));
               }
            }
            if(player.isSubmergedInWater() && gills){
               StatusEffectInstance waterBreath = new StatusEffectInstance(StatusEffects.WATER_BREATHING, 110, 0, false, false, true);
               player.addStatusEffect(waterBreath);
            }
         }
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand){
         ItemStack stack = playerEntity.getStackInHand(hand);
         if(!(playerEntity instanceof ServerPlayerEntity player)) return ActionResult.PASS;
         
         if(player.isSneaking()){
            boolean active = !getBooleanProperty(stack,ACTIVE_TAG);
            putProperty(stack,ACTIVE_TAG,active);
            
            if(active){
               player.sendMessage(Text.literal("The Charm moistens").formatted(Formatting.BLUE,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_PUFFER_FISH_DEATH, 0.5f,0.8f);
            }else{
               player.sendMessage(Text.literal("The Charm dries out").formatted(Formatting.BLUE,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_PUFFER_FISH_BLOW_OUT, 0.5f,0.7f);
            }
            
            return ActionResult.SUCCESS_SERVER;
         }
         
         return ActionResult.PASS;
      }
   }
}
