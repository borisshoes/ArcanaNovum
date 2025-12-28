package net.borisshoes.arcananovum.items;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class EverlastingRocket extends EnergyItem {
	public static final String ID = "everlasting_rocket";
   
   public static final String FIREWORK_ID_TAG = "fireworkId";
   
   public EverlastingRocket(){
      id = ID;
      name = "Everlasting Rocket";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ITEMS};
      itemVersion = 0;
      vanillaItem = Items.FIREWORK_ROCKET;
      item = new EverlastingRocketItem();
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW);
      initEnergy = 16;
      researchTasks = new ResourceKey[]{ResearchTasks.USE_FIREWORK,ResearchTasks.ACTIVATE_MENDING,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("A ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Rocket").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" that has near ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("infinite ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("uses.").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Can be used for ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("everything").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" a ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("normal rocket").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" is used for.").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Stores ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("charges ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("that slowly ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("recharge ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("over ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("time").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal(""));
      
      int maxCharges = itemStack == null ? 16 : getMaxEnergy(itemStack);
      int charges = itemStack == null ? 16 : getEnergy(itemStack);
      
      lore.add(Component.literal("")
            .append(Component.literal("Charges ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("- ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(charges+" / "+maxCharges).withStyle(ChatFormatting.LIGHT_PURPLE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack forgeItem(Container inv, StarlightForgeBlockEntity starlightForge){
      ItemStack rocketStack = inv.getItem(12); // Should be the rocket
      ItemStack newArcanaItem = getNewItem();
      
      Fireworks fireworks = rocketStack.getOrDefault(DataComponents.FIREWORKS, new Fireworks(1,List.of()));
      newArcanaItem.set(DataComponents.FIREWORKS,fireworks);
      
      return newArcanaItem;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      Fireworks fireworks = stack.getOrDefault(DataComponents.FIREWORKS, new Fireworks(1,List.of()));
      ItemStack newStack = super.updateItem(stack,server);
      newStack.set(DataComponents.FIREWORKS,fireworks);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){
      return 16 + 3*Math.max(0,ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.POWDER_PACKING.id));
   }
   
   public ItemStack getFireworkStack(ItemStack stack){
      ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET, 64);
      Fireworks fireworks = stack.getOrDefault(DataComponents.FIREWORKS, new Fireworks(1,List.of()));
      itemStack.set(DataComponents.FIREWORKS,fireworks);
      putProperty(itemStack,FIREWORK_ID_TAG,getUUID(stack));
      return itemStack;
   }
   
   public static void decreaseRocket(ItemStack stack, ServerPlayer player){
      if(!hasProperty(stack,FIREWORK_ID_TAG)) return;
      String rocketId = getStringProperty(stack,FIREWORK_ID_TAG);
      
      Inventory inv = player.getInventory();
      for(int invSlot = 0; invSlot<inv.getContainerSize(); invSlot++){
         ItemStack item = inv.getItem(invSlot);
         if(item.isEmpty()){
            continue;
         }
         
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
         if(arcanaItem instanceof EverlastingRocket rocket && getUUID(item).equals(rocketId)){
            rocket.addEnergy(item,-1);
            rocket.buildItemLore(stack,player.level().getServer());
            ArcanaAchievements.progress(player,ArcanaAchievements.MISSILE_LAUNCHER.id, 1);
            ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.EVERLASTING_ROCKET_USE)); // Add xp
            return;
         }
      }
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.FIREWORK_ROCKET,16);
      ArcanaIngredient b = new ArcanaIngredient(Items.GUNPOWDER,8);
      ArcanaIngredient c = new ArcanaIngredient(Items.PAPER,24);
      ArcanaIngredient g = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.MENDING,1));
      ArcanaIngredient h = new ArcanaIngredient(Items.FIREWORK_STAR,8);
      ArcanaIngredient i = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new ArcanaIngredient.EnchantmentEntry(Enchantments.UNBREAKING,3));
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,i,b},
            {c,h,a,h,c},
            {b,i,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(this, ingredients,new ForgeRequirement().withEnchanter()).addCenterpiece(12);
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("Everlasting Rocket").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nI have blown through so much gunpowder on rockets. Using a combination of Mending and Unbreaking enchantments, I think I can extend one rocket into thousands.\nThe Everlasting Rocket is used the ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("Everlasting Rocket").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nsame way as a normal rocket. However it uses charges instead of being expended.\n\nCharges regenerate over time. The properties of the rocket come from the center item from crafting.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   
   public class EverlastingRocketItem extends ArcanaPolymerItem implements PolymerItem {
      public EverlastingRocketItem(){
         super(getThis(),getArcanaItemComponents()
               .component(DataComponents.FIREWORKS, new Fireworks(1, List.of()))
         );
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         List<String> stringList = new ArrayList<>();
         int percentage = (int) Math.ceil(3.0 * getEnergy(itemStack) / getMaxEnergy(itemStack));
         boolean adjustable = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.ADJUSTABLE_FUSE.id) >= 1;
         
         if(adjustable){
            if(percentage == 0){
               stringList.add("0");
            }else{
               Fireworks fireworks = itemStack.getOrDefault(DataComponents.FIREWORKS, new Fireworks(1,List.of()));
               int flight = fireworks.flightDuration();
               if(flight == 3){
                  stringList.add("3");
               }else if(flight == 2){
                  stringList.add("2");
               }else{
                  stringList.add("1");
               }
            }
         }else{
            if(percentage == 3){
               stringList.add("3");
            }else if(percentage == 2){
               stringList.add("2");
            }else if(percentage == 1){
               stringList.add("1");
            }else{
               stringList.add("0");
            }
         }

         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerLevel serverWorld && entity instanceof ServerPlayer player)) return;
         
         if(player.level().getServer().getTickCount() % (600-(100*Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SULFUR_REPLICATION.id)))) == 0){
            addEnergy(stack,1);
            buildItemLore(stack,player.level().getServer());
         }
      }
      
      @Override
      public InteractionResult useOn(UseOnContext context){
         Level world = context.getLevel();
         if(!world.isClientSide() && context.getPlayer() instanceof ServerPlayer player){
            if(((EnergyItem)getThis()).getEnergy(context.getItemInHand()) > 0){
               ItemStack itemStack = context.getItemInHand();
               Vec3 vec3d = context.getClickLocation();
               Direction direction = context.getClickedFace();
               FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(world, context.getPlayer(), vec3d.x + (double)direction.getStepX() * 0.15, vec3d.y + (double)direction.getStepY() * 0.15, vec3d.z + (double)direction.getStepZ() * 0.15, getFireworkStack(itemStack));
               world.addFreshEntity(fireworkRocketEntity);
               ((EnergyItem)getThis()).addEnergy(context.getItemInHand(),-1);
               buildItemLore(itemStack,player.level().getServer());
               ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.EVERLASTING_ROCKET_USE)); // Add xp
            }else{
               player.displayClientMessage(Component.literal("The Rocket is out of Charges").withStyle(ChatFormatting.YELLOW),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1,0.8f);
            }
         }
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Override
      public InteractionResult use(Level world, Player user, InteractionHand hand){
         ItemStack itemStack = user.getItemInHand(hand);
         if(user.isShiftKeyDown() && ArcanaAugments.getAugmentOnItem(itemStack, ArcanaAugments.ADJUSTABLE_FUSE.id) > 0 && user instanceof ServerPlayer player){
            Fireworks fireworks = itemStack.getOrDefault(DataComponents.FIREWORKS, new Fireworks(1,List.of()));
            int flight = fireworks.flightDuration();
            flight = ((flight % 3) + 1);
            itemStack.set(DataComponents.FIREWORKS, new Fireworks(flight,fireworks.explosions()));
            player.displayClientMessage(Component.literal("Fuse Adjusted to "+flight).withStyle(ChatFormatting.YELLOW),true);
            SoundUtils.playSongToPlayer(player, SoundEvents.NOTE_BLOCK_SNARE, 1,0.8f);
         }else if(user.isFallFlying()){
            if(!world.isClientSide() && user instanceof ServerPlayer player){
               if(((EnergyItem)getThis()).getEnergy(itemStack) > 0){
                  FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(world, getFireworkStack(itemStack), user);
                  world.addFreshEntity(fireworkRocketEntity);
                  if(!user.isCreative()){
                     ((EnergyItem)getThis()).addEnergy(itemStack,-1);
                     buildItemLore(itemStack,player.level().getServer());
                     ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.EVERLASTING_ROCKET_USE)); // Add xp
                  }
                  user.awardStat(Stats.ITEM_USED.get(this));
                  if(player.position().y() > 500){
                     ArcanaAchievements.grant(player,ArcanaAchievements.ROCKETMAN.id);
                  }
               }else{
                  player.displayClientMessage(Component.literal("The Rocket is out of Charges").withStyle(ChatFormatting.YELLOW),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1,0.8f);
               }
            }
            return InteractionResult.SUCCESS_SERVER;
         }
         return InteractionResult.PASS;
      }
   }
}

