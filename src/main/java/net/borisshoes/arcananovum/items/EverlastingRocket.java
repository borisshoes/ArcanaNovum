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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.YELLOW);
      initEnergy = 16;
      researchTasks = new RegistryKey[]{ResearchTasks.USE_FIREWORK,ResearchTasks.ACTIVATE_MENDING,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("A ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Rocket").formatted(Formatting.YELLOW))
            .append(Text.literal(" that has near ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("infinite ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("uses.").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Can be used for ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("everything").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" a ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("normal rocket").formatted(Formatting.YELLOW))
            .append(Text.literal(" is used for.").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Stores ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("charges ").formatted(Formatting.YELLOW))
            .append(Text.literal("that slowly ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("recharge ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("over ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("time").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal(""));
      
      int maxCharges = itemStack == null ? 16 : getMaxEnergy(itemStack);
      int charges = itemStack == null ? 16 : getEnergy(itemStack);
      
      lore.add(Text.literal("")
            .append(Text.literal("Charges ").formatted(Formatting.YELLOW))
            .append(Text.literal("- ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(charges+" / "+maxCharges).formatted(Formatting.LIGHT_PURPLE)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv, StarlightForgeBlockEntity starlightForge){
      ItemStack rocketStack = inv.getStack(12); // Should be the rocket
      ItemStack newArcanaItem = getNewItem();
      
      FireworksComponent fireworks = rocketStack.getOrDefault(DataComponentTypes.FIREWORKS, new FireworksComponent(1,List.of()));
      newArcanaItem.set(DataComponentTypes.FIREWORKS,fireworks);
      
      return newArcanaItem;
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      FireworksComponent fireworks = stack.getOrDefault(DataComponentTypes.FIREWORKS, new FireworksComponent(1,List.of()));
      ItemStack newStack = super.updateItem(stack,server);
      newStack.set(DataComponentTypes.FIREWORKS,fireworks);
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
      FireworksComponent fireworks = stack.getOrDefault(DataComponentTypes.FIREWORKS, new FireworksComponent(1,List.of()));
      itemStack.set(DataComponentTypes.FIREWORKS,fireworks);
      putProperty(itemStack,FIREWORK_ID_TAG,getUUID(stack));
      return itemStack;
   }
   
   public static void decreaseRocket(ItemStack stack, ServerPlayerEntity player){
      if(!hasProperty(stack,FIREWORK_ID_TAG)) return;
      String rocketId = getStringProperty(stack,FIREWORK_ID_TAG);
      
      PlayerInventory inv = player.getInventory();
      for(int invSlot = 0; invSlot<inv.size(); invSlot++){
         ItemStack item = inv.getStack(invSlot);
         if(item.isEmpty()){
            continue;
         }
         
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
         if(arcanaItem instanceof EverlastingRocket rocket && getUUID(item).equals(rocketId)){
            rocket.addEnergy(item,-1);
            rocket.buildItemLore(stack,player.getServer());
            ArcanaAchievements.progress(player,ArcanaAchievements.MISSILE_LAUNCHER.id, 1);
            ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.EVERLASTING_ROCKET_USE)); // Add xp
            return;
         }
      }
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.FIREWORK_ROCKET,16);
      ArcanaIngredient b = new ArcanaIngredient(Items.GUNPOWDER,8);
      ArcanaIngredient c = new ArcanaIngredient(Items.PAPER,24);
      ArcanaIngredient g = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MinecraftUtils.getEnchantment(Enchantments.MENDING),1));
      ArcanaIngredient h = new ArcanaIngredient(Items.FIREWORK_STAR,8);
      ArcanaIngredient i = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MinecraftUtils.getEnchantment(Enchantments.UNBREAKING),3));
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,i,b},
            {c,h,a,h,c},
            {b,i,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withEnchanter());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("Everlasting Rocket").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nI have blown through so much gunpowder on rockets. Using a combination of Mending and Unbreaking enchantments, I think I can extend one rocket into thousands.\nThe Everlasting Rocket is used the ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("Everlasting Rocket").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nsame way as a normal rocket. However it uses charges instead of being expended.\n\nCharges regenerate over time. The properties of the rocket come from the center item from crafting.").formatted(Formatting.BLACK)));
      return list;
   }
   
   
   public class EverlastingRocketItem extends ArcanaPolymerItem implements PolymerItem {
      public EverlastingRocketItem(){
         super(getThis(),getArcanaItemComponents()
               .component(DataComponentTypes.FIREWORKS, new FireworksComponent(1, List.of()))
         );
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         List<String> stringList = new ArrayList<>();
         int percentage = (int) Math.ceil(3.0 * getEnergy(itemStack) / getMaxEnergy(itemStack));
         boolean adjustable = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.ADJUSTABLE_FUSE.id) >= 1;
         
         if(adjustable){
            if(percentage == 0){
               stringList.add("0");
            }else{
               FireworksComponent fireworks = itemStack.getOrDefault(DataComponentTypes.FIREWORKS, new FireworksComponent(1,List.of()));
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
         if(!(world instanceof ServerWorld serverWorld && entity instanceof ServerPlayerEntity player)) return;
         
         if(player.getServer().getTicks() % (600-(100*Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SULFUR_REPLICATION.id)))) == 0){
            addEnergy(stack,1);
            buildItemLore(stack,player.getServer());
         }
      }
      
      @Override
      public ActionResult useOnBlock(ItemUsageContext context){
         World world = context.getWorld();
         if(!world.isClient && context.getPlayer() instanceof ServerPlayerEntity player){
            if(((EnergyItem)getThis()).getEnergy(context.getStack()) > 0){
               ItemStack itemStack = context.getStack();
               Vec3d vec3d = context.getHitPos();
               Direction direction = context.getSide();
               FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(world, context.getPlayer(), vec3d.x + (double)direction.getOffsetX() * 0.15, vec3d.y + (double)direction.getOffsetY() * 0.15, vec3d.z + (double)direction.getOffsetZ() * 0.15, getFireworkStack(itemStack));
               world.spawnEntity(fireworkRocketEntity);
               ((EnergyItem)getThis()).addEnergy(context.getStack(),-1);
               buildItemLore(itemStack,player.getServer());
               ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.EVERLASTING_ROCKET_USE)); // Add xp
            }else{
               player.sendMessage(Text.literal("The Rocket is out of Charges").formatted(Formatting.YELLOW),true);
               SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,0.8f);
            }
         }
         return ActionResult.SUCCESS_SERVER;
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity user, Hand hand){
         ItemStack itemStack = user.getStackInHand(hand);
         if(user.isSneaking() && ArcanaAugments.getAugmentOnItem(itemStack, ArcanaAugments.ADJUSTABLE_FUSE.id) > 0 && user instanceof ServerPlayerEntity player){
            FireworksComponent fireworks = itemStack.getOrDefault(DataComponentTypes.FIREWORKS, new FireworksComponent(1,List.of()));
            int flight = fireworks.flightDuration();
            flight = ((flight % 3) + 1);
            itemStack.set(DataComponentTypes.FIREWORKS, new FireworksComponent(flight,fireworks.explosions()));
            player.sendMessage(Text.literal("Fuse Adjusted to "+flight).formatted(Formatting.YELLOW),true);
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_NOTE_BLOCK_SNARE, 1,0.8f);
         }else if(user.isGliding()){
            if(!world.isClient && user instanceof ServerPlayerEntity player){
               if(((EnergyItem)getThis()).getEnergy(itemStack) > 0){
                  FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(world, getFireworkStack(itemStack), user);
                  world.spawnEntity(fireworkRocketEntity);
                  if(!user.isCreative()){
                     ((EnergyItem)getThis()).addEnergy(itemStack,-1);
                     buildItemLore(itemStack,player.getServer());
                     ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.EVERLASTING_ROCKET_USE)); // Add xp
                  }
                  user.incrementStat(Stats.USED.getOrCreateStat(this));
                  if(player.getPos().getY() > 500){
                     ArcanaAchievements.grant(player,ArcanaAchievements.ROCKETMAN.id);
                  }
               }else{
                  player.sendMessage(Text.literal("The Rocket is out of Charges").formatted(Formatting.YELLOW),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,0.8f);
               }
            }
            return ActionResult.SUCCESS_SERVER;
         }
         return ActionResult.PASS;
      }
   }
}

