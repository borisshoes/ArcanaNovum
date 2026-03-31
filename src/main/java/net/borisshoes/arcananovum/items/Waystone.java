package net.borisshoes.arcananovum.items;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity;
import net.borisshoes.arcananovum.blocks.astralgateway.AstralGatewayBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class Waystone extends ArcanaItem {
   public static final String LOCATION_TAG = "location";
   public static final String GATEWAY_TAG = "gateway";
   public static final String UNATTUNED_TAG = "unattuned";
   public static final String ID = "waystone";
   
   public Waystone(){
      id = ID;
      name = "Waystone";
      rarity = ArcanaRarity.MUNDANE;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.IRON_NUGGET;
      item = new Waystone.WaystoneItem();
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY);
      researchTasks = new ResourceKey[]{ResearchTasks.ADVANCEMENT_USE_LODESTONE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      putProperty(stack, LOCATION_TAG, getUnattunedTag());
      putProperty(stack, GATEWAY_TAG, false);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("      Waystone").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nThe channels of Arcana that course through the realms, Leylines, as the old texts call them, are quite interesting. My measurements suggest that they are not static, but their ebb and flow mostly").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("      Waystone").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\naround a path, and on a timescale I would consider to be glacial. It might not be the most precise thing, but I can use the faintly unique Arcana of each Leyline to uniquely mark a location in any dimension.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("      Waystone").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nIt seems like I can transmute a stone to hold on to this compound Leyline signature, as long as it doesn’t get too hot.\n\nUsing the Waystone marks my current location, rotation and dimension.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("      Waystone").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD), Component.literal("\nHeating the Waystone on a campfire for a few minutes releases the imbued location.\n\nHolding the Waystone in my offhand turns it into a compass that glows when facing its imbued location.\n").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack stack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("A ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("stone ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("engraved ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("with a singular").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" rune").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("rune").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(" pulses subtly ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("like the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("ocean").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("You feel a slight ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("pull").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(" towards a ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("far away").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(" place").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("to ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("imbue").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" the ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("stone").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(" with a ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("location").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
      
      if(stack != null){
         if(!isUnattuned(stack)){
            WaystoneTarget target = getTarget(stack);
            ChatFormatting dimColor;
            String dimensionName;
            String location;
            
            if(target.world().identifier().toString().equals(ServerLevel.OVERWORLD.identifier().toString())){
               dimColor = ChatFormatting.GREEN;
               dimensionName = "Overworld";
            }else if(target.world().identifier().toString().equals(ServerLevel.NETHER.identifier().toString())){
               dimColor = ChatFormatting.RED;
               dimensionName = "The Nether";
            }else if(target.world().identifier().toString().equals(ServerLevel.END.identifier().toString())){
               dimColor = ChatFormatting.DARK_PURPLE;
               dimensionName = "The End";
            }else{
               dimColor = ChatFormatting.YELLOW;
               dimensionName = target.world().identifier().toString();
            }
            
            location = dimensionName + " (" + (int) target.position().x() + "," + (int) target.position().y() + "," + (int) target.position().z() + ")";
            lore.add(Component.literal(""));
            lore.add(Component.literal((isForGateway(stack) ? "Astral Gateway" : "Location") + " - " + location).withStyle(dimColor));
         }else{
            lore.add(Component.literal(""));
            lore.add(Component.literal("Unattuned").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
         }
      }
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   public static CompoundTag getUnattunedTag(){
      CompoundTag newComp = new CompoundTag();
      newComp.putBoolean(UNATTUNED_TAG, true);
      return newComp;
   }
   
   public static void setUnattuned(ItemStack stack){
      putProperty(stack, LOCATION_TAG, getUnattunedTag());
      putProperty(stack, GATEWAY_TAG, false);
   }
   
   public static boolean isUnattuned(ItemStack stack){
      if(!stack.is(ArcanaRegistry.WAYSTONE.getItem())) return false;
      CompoundTag comp = ArcanaItem.getCompoundProperty(stack, LOCATION_TAG);
      return comp.getBooleanOr(UNATTUNED_TAG, true) || getTarget(stack) == null;
   }
   
   public static boolean isForGateway(ItemStack stack){
      if(!stack.is(ArcanaRegistry.WAYSTONE.getItem())) return false;
      CompoundTag comp = ArcanaItem.getCompoundProperty(stack, GATEWAY_TAG);
      return comp.getBooleanOr(UNATTUNED_TAG, true) || getTarget(stack) == null;
   }
   
   public static void setForGateway(ItemStack stack){
      putProperty(stack, GATEWAY_TAG, true);
   }
   
   public static void saveTarget(ItemStack stack, WaystoneTarget target){
      if(!stack.is(ArcanaRegistry.WAYSTONE.getItem()) || target == null) return;
      CompoundTag newComp = new CompoundTag();
      newComp.putString("dim", target.world().identifier().toString());
      newComp.putDouble("x", target.position().x);
      newComp.putDouble("y", target.position().y);
      newComp.putDouble("z", target.position().z);
      newComp.putFloat("yaw", target.yaw());
      newComp.putFloat("pitch", target.pitch());
      newComp.putBoolean("unattuned", false);
      ArcanaItem.putProperty(stack, LOCATION_TAG, newComp);
      stack.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(new GlobalPos(target.world(), BlockPos.containing(target.position()))), false));
   }
   
   public static WaystoneTarget getTarget(ItemStack stack){
      if(!stack.is(ArcanaRegistry.WAYSTONE.getItem())) return null;
      CompoundTag comp = ArcanaItem.getCompoundProperty(stack, LOCATION_TAG);
      String worldStr = comp.getStringOr("dim", "");
      ResourceKey<Level> worldKey = null;
      for(ServerLevel world : BorisLib.SERVER.getAllLevels()){
         if(world.dimension().identifier().toString().equals(worldStr)){
            worldKey = world.dimension();
            break;
         }
      }
      if(worldKey == null) return null;
      double x = comp.getDoubleOr("x", 0);
      double y = comp.getDoubleOr("y", 0);
      double z = comp.getDoubleOr("z", 0);
      float yaw = comp.getFloatOr("yaw", 0);
      float pitch = comp.getFloatOr("pitch", 0);
      return new WaystoneTarget(worldKey, new Vec3(x, y, z), yaw, pitch);
   }
   
   public class WaystoneItem extends ArcanaPolymerItem {
      public WaystoneItem(){
         super(getThis());
      }
      
      @Override
      public InteractionResult use(Level world, Player user, InteractionHand hand){
         ItemStack stack = user.getItemInHand(hand);
         if(isUnattuned(stack) && user instanceof ServerPlayer player){
            saveTarget(stack, new WaystoneTarget(user.level().dimension(), user.position(), user.getYRot(), user.getXRot()));
            buildItemLore(stack, player.level().getServer());
            SoundUtils.playSongToPlayer(player, SoundEvents.LODESTONE_COMPASS_LOCK, 1, 0.7f);
            return InteractionResult.SUCCESS_SERVER;
         }else{
            return InteractionResult.TRY_WITH_EMPTY_HAND;
         }
      }
      
      @Override
      public InteractionResult useOn(UseOnContext context){
         ItemStack stack = context.getItemInHand();
         if(context.getPlayer() != null && context.getPlayer().isShiftKeyDown() && !isUnattuned(stack)){
            if(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof StarpathAltarBlockEntity sabe){
               WaystoneTarget target = getTarget(stack);
               if(target != null && ArcanaAugments.getAugmentFromMap(sabe.getAugments(), ArcanaAugments.STARGATE) > 0 || target.world.identifier().equals(context.getLevel().dimension().identifier())){
                  sabe.setTarget(new StarpathAltarBlockEntity.TargetEntry(
                        MinecraftUtils.getFormattedDimName(target.world).getString() + " " + BlockPos.containing(target.position()).toShortString(),
                        target.world.identifier().toString(),
                        (int) target.position().x(),
                        (int) target.position().y(),
                        (int) target.position().z()
                  ));
                  SoundUtils.playSound(context.getPlayer().level(), context.getClickedPos(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1, 0.7f);
                  return InteractionResult.SUCCESS_SERVER;
               }
            }
         }else if(isUnattuned(stack) && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof AstralGatewayBlockEntity gateway){
            BlockPos pos = context.getClickedPos();
            saveTarget(stack, new WaystoneTarget(context.getLevel().dimension(), pos.getCenter(), 0, 0));
            setForGateway(stack);
            buildItemLore(stack, context.getLevel().getServer());
            if(context.getPlayer() instanceof ServerPlayer player)
               SoundUtils.playSongToPlayer(player, SoundEvents.LODESTONE_COMPASS_LOCK, 1, 1.1f);
            return InteractionResult.SUCCESS_SERVER;
         }
         return super.useOn(context);
      }
      
      @Override
      public Item getPolymerItem(ItemStack itemStack, PacketContext context){
         if(PolymerResourcePackUtils.hasMainPack(context.getPlayer())){
            return getThis().getVanillaItem();
         }else{
            return Items.LODESTONE;
         }
      }
      
      @Override
      public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot){
         super.inventoryTick(stack, world, entity, slot);
         boolean shouldAdd = slot == EquipmentSlot.OFFHAND && !stack.has(DataComponents.LODESTONE_TRACKER) && getTarget(stack) != null;
         if(shouldAdd){
            WaystoneTarget target = getTarget(stack);
            stack.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(new GlobalPos(target.world(), BlockPos.containing(target.position()))), false));
         }else if(slot != EquipmentSlot.OFFHAND && stack.has(DataComponents.LODESTONE_TRACKER)){
            stack.remove(DataComponents.LODESTONE_TRACKER);
         }
      }
      
      // TODO skin compat
      @Override
      public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context){
         if(PolymerResourcePackUtils.hasMainPack(context)){
            if(isUnattuned(stack)){
               return ArcanaRegistry.arcanaId("waystone_unattuned");
            }else{
               WaystoneTarget target = getTarget(stack);
               ResourceKey<Level> dim = target.world();
               if(dim.toString().equals(ServerLevel.OVERWORLD.toString())){
                  return ArcanaRegistry.arcanaId("waystone_overworld");
               }else if(dim.toString().equals(ServerLevel.NETHER.toString())){
                  return ArcanaRegistry.arcanaId("waystone_nether");
               }else if(dim.toString().equals(ServerLevel.END.toString())){
                  return ArcanaRegistry.arcanaId("waystone_end");
               }else{
                  return ArcanaRegistry.arcanaId("waystone_unknown");
               }
            }
         }else{
            return BuiltInRegistries.ITEM.getResourceKey(getPolymerItem(stack, context)).get().identifier();
         }
      }
   }
   
   public record WaystoneTarget(ResourceKey<Level> world, Vec3 position, float yaw, float pitch) {
   }
}
