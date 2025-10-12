package net.borisshoes.arcananovum.items;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class Waystone extends ArcanaItem {
   public static final String LOCATION_TAG = "location";
   public static final String ID = "waystone";
   
   public Waystone(){
      id = ID;
      name = "Waystone";
      rarity = ArcanaRarity.MUNDANE;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.ITEMS};
      vanillaItem = Items.IRON_NUGGET;
      item = new Waystone.WaystoneItem();
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.GRAY);
      researchTasks = new RegistryKey[]{ResearchTasks.ADVANCEMENT_USE_LODESTONE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      putProperty(stack,LOCATION_TAG,getUnattunedTag());
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("      Waystone").formatted(Formatting.GRAY,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nThe channels of Arcana that course through the realms, Leylines, as the old texts call them, are quite interesting. My measurements suggest that they are not static, but their ebb and flow mostly").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("      Waystone").formatted(Formatting.GRAY,Formatting.BOLD),Text.literal("\naround a path, and on a timescale I would consider to be glacial. It might not be the most precise thing, but I can use the faintly unique Arcana of each Leyline to uniquely mark a location in any dimension.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("      Waystone").formatted(Formatting.GRAY,Formatting.BOLD),Text.literal("\nIt seems like I can transmute a stone to hold on to this compound Leyline signature, as long as it doesn’t get too hot.\n\nUsing the Waystone marks my current location, rotation and dimension.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("      Waystone").formatted(Formatting.GRAY,Formatting.BOLD),Text.literal("\nHeating the Waystone on a campfire for a few minutes releases the imbued location.\n\nHolding the Waystone in my offhand turns it into a compass that glows when facing its imbued location.\n").formatted(Formatting.BLACK)));
      return list;
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack stack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("A ").formatted(Formatting.GRAY))
            .append(Text.literal("stone ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("engraved ").formatted(Formatting.YELLOW))
            .append(Text.literal("with a singular").formatted(Formatting.GRAY))
            .append(Text.literal(" rune").formatted(Formatting.DARK_GRAY))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.GRAY))
            .append(Text.literal("rune").formatted(Formatting.DARK_GRAY))
            .append(Text.literal(" pulses subtly ").formatted(Formatting.YELLOW))
            .append(Text.literal("like the ").formatted(Formatting.GRAY))
            .append(Text.literal("ocean").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("You feel a slight ").formatted(Formatting.GRAY))
            .append(Text.literal("pull").formatted(Formatting.DARK_GRAY))
            .append(Text.literal(" towards a ").formatted(Formatting.GRAY))
            .append(Text.literal("far away").formatted(Formatting.DARK_GRAY))
            .append(Text.literal(" place").formatted(Formatting.YELLOW))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click ").formatted(Formatting.YELLOW))
            .append(Text.literal("to ").formatted(Formatting.GRAY))
            .append(Text.literal("imbue").formatted(Formatting.YELLOW))
            .append(Text.literal(" the ").formatted(Formatting.GRAY))
            .append(Text.literal("stone").formatted(Formatting.DARK_GRAY))
            .append(Text.literal(" with a ").formatted(Formatting.GRAY))
            .append(Text.literal("location").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.GRAY)));
      
      if(stack != null){
         if(!isUnattuned(stack)){
            WaystoneTarget target = getTarget(stack);
            Formatting dimColor;
            String dimensionName;
            String location;
            
            if(target.world().getValue().toString().equals(ServerWorld.OVERWORLD.getValue().toString())){
               dimColor = Formatting.GREEN;
               dimensionName = "Overworld";
            }else if(target.world().getValue().toString().equals(ServerWorld.NETHER.getValue().toString())){
               dimColor = Formatting.RED;
               dimensionName = "The Nether";
            }else if(target.world().getValue().toString().equals(ServerWorld.END.getValue().toString())){
               dimColor = Formatting.DARK_PURPLE;
               dimensionName = "The End";
            }else{
               dimColor = Formatting.YELLOW;
               dimensionName = target.world().getValue().toString();
            }
            
            location = dimensionName + " ("+(int)target.position().getX()+","+(int)target.position().getY()+","+(int)target.position().getZ()+")";
            lore.add(Text.literal(""));
            lore.add(Text.literal("Location - "+location).formatted(dimColor));
         }else{
            lore.add(Text.literal(""));
            lore.add(Text.literal("Unattuned").formatted(Formatting.GRAY,Formatting.ITALIC));
         }
      }
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   public static NbtCompound getUnattunedTag(){
      NbtCompound newComp = new NbtCompound();
      newComp.putBoolean("unattuned",true);
      return newComp;
   }
   
   public static void setUnattuned(ItemStack stack){
      putProperty(stack,LOCATION_TAG,getUnattunedTag());
   }
   
   public static boolean isUnattuned(ItemStack stack){
      if(!stack.isOf(ArcanaRegistry.WAYSTONE.getItem())) return false;
      NbtCompound comp = ArcanaItem.getCompoundProperty(stack,LOCATION_TAG);
      return comp.getBoolean("unattuned", true) || getTarget(stack) == null;
   }
   
   public static void saveTarget(ItemStack stack, WaystoneTarget target){
      if(!stack.isOf(ArcanaRegistry.WAYSTONE.getItem()) || target == null) return;
      NbtCompound newComp = new NbtCompound();
      newComp.putString("dim",target.world().getValue().toString());
      newComp.putDouble("x",target.position().x);
      newComp.putDouble("y",target.position().y);
      newComp.putDouble("z",target.position().z);
      newComp.putFloat("yaw",target.yaw());
      newComp.putFloat("pitch",target.pitch());
      newComp.putBoolean("unattuned",false);
      ArcanaItem.putProperty(stack,LOCATION_TAG,newComp);
      stack.set(DataComponentTypes.LODESTONE_TRACKER,new LodestoneTrackerComponent(Optional.of(new GlobalPos(target.world(), BlockPos.ofFloored(target.position()))),false));
   }
   
   public static WaystoneTarget getTarget(ItemStack stack){
      if(!stack.isOf(ArcanaRegistry.WAYSTONE.getItem())) return null;
      NbtCompound comp = ArcanaItem.getCompoundProperty(stack,LOCATION_TAG);
      String worldStr = comp.getString("dim","");
      RegistryKey<World> worldKey = null;
      for(ServerWorld world : BorisLib.SERVER.getWorlds()){
         if(world.getRegistryKey().getValue().toString().equals(worldStr)){
            worldKey = world.getRegistryKey();
            break;
         }
      }
      if(worldKey == null) return null;
      double x = comp.getDouble("x",0);
      double y = comp.getDouble("y",0);
      double z = comp.getDouble("z",0);
      float yaw = comp.getFloat("yaw",0);
      float pitch = comp.getFloat("pitch",0);
      return new WaystoneTarget(worldKey,new Vec3d(x,y,z),yaw,pitch);
   }
   
   @Override
   protected ArcanaRecipe makeRecipe(){
      ExplainIngredient b = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_COLOR),1,"",false)
            .withName(Text.literal("Transmutation Recipe").formatted(Formatting.AQUA,Formatting.BOLD))
            .withLore(List.of(Text.literal("Use a Transmutation Altar").formatted(Formatting.DARK_AQUA)));
      ExplainIngredient w = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.LIGHT_COLOR),1,"",false)
            .withName(Text.literal("Transmutation Recipe").formatted(Formatting.AQUA,Formatting.BOLD))
            .withLore(List.of(Text.literal("Use a Transmutation Altar").formatted(Formatting.DARK_AQUA)));
      ExplainIngredient c = new ExplainIngredient(Items.REDSTONE,42,"Redstone Dust")
            .withName(Text.literal("Redstone Dust").formatted(Formatting.RED,Formatting.BOLD))
            .withLore(List.of(Text.literal("Transmutation Reagent").formatted(Formatting.GOLD)));
      ExplainIngredient t = new ExplainIngredient(ArcanaRegistry.TRANSMUTATION_ALTAR.getItem(),1,"",false)
            .withName(Text.literal("Transmutation Altar").formatted(Formatting.AQUA,Formatting.BOLD))
            .withLore(List.of(Text.literal("Use a Transmutation Altar").formatted(Formatting.DARK_AQUA)));
      ExplainIngredient d = new ExplainIngredient(Items.AMETHYST_SHARD,16,"Amethyst Shard")
            .withName(Text.literal("Amethyst Shards").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
            .withLore(List.of(Text.literal("Transmutation Reagent").formatted(Formatting.DARK_PURPLE)));
      ExplainIngredient p = new ExplainIngredient(Items.LODESTONE,1,"Lodestone")
            .withName(Text.literal("Lodestone").formatted(Formatting.GRAY,Formatting.BOLD))
            .withLore(List.of(Text.literal("Infusion Input").formatted(Formatting.WHITE)));
      
      ExplainIngredient[][] ingredients = {
            {b,b,p,b,b},
            {b,b,b,b,w},
            {c,b,t,w,d},
            {b,w,w,w,w},
            {w,w,w,w,w}};
      return new ExplainRecipe(ingredients);
   }
   
   public class WaystoneItem extends ArcanaPolymerItem {
      public WaystoneItem(){
         super(getThis());
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity user, Hand hand){
         ItemStack stack = user.getStackInHand(hand);
         if(isUnattuned(stack) && user instanceof ServerPlayerEntity player){
            saveTarget(stack,new WaystoneTarget(user.getWorld().getRegistryKey(), user.getPos(), user.getYaw(), user.getPitch()));
            buildItemLore(stack,player.getServer());
            SoundUtils.playSongToPlayer(player, SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, 1, 0.7f);
            return ActionResult.SUCCESS;
         }else{
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
         }
      }
      
      @Override
      public ActionResult useOnBlock(ItemUsageContext context){
         if(context.getPlayer() != null && context.getPlayer().isSneaking() && !isUnattuned(context.getStack())){
            if(context.getWorld().getBlockEntity(context.getBlockPos()) instanceof StarpathAltarBlockEntity sabe){
               WaystoneTarget target = getTarget(context.getStack());
               if(target != null && ArcanaAugments.getAugmentFromMap(sabe.getAugments(),ArcanaAugments.STARGATE.id) > 0 || target.world.getValue().equals(context.getWorld().getRegistryKey().getValue())){
                  sabe.setTarget(new StarpathAltarBlockEntity.TargetEntry(
                        ArcanaUtils.getFormattedDimName(target.world).getString()+" "+BlockPos.ofFloored(target.position()).toShortString(),
                        target.world.getValue().toString(),
                        (int) target.position().getX(),
                        (int) target.position().getY(),
                        (int) target.position().getZ()
                        ));
                  SoundUtils.playSound(context.getPlayer().getWorld(),context.getBlockPos(),SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS,1,0.7f);
               }
            }
         }
         return super.useOnBlock(context);
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
      public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot){
         super.inventoryTick(stack, world, entity, slot);
         boolean shouldAdd = slot == EquipmentSlot.OFFHAND && !stack.contains(DataComponentTypes.LODESTONE_TRACKER) && getTarget(stack) != null;
         if(shouldAdd){
            WaystoneTarget target = getTarget(stack);
            stack.set(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(Optional.of(new GlobalPos(target.world(),BlockPos.ofFloored(target.position()))),false));
         }else if(slot != EquipmentSlot.OFFHAND && stack.contains(DataComponentTypes.LODESTONE_TRACKER)){
            stack.remove(DataComponentTypes.LODESTONE_TRACKER);
         }
      }
      
      @Override
      public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context){
         if(PolymerResourcePackUtils.hasMainPack(context)){
            if(isUnattuned(stack)){
               return Identifier.of(MOD_ID,"waystone_unattuned");
            }else{
               WaystoneTarget target = getTarget(stack);
               RegistryKey<World> dim = target.world();
               if(dim.toString().equals(ServerWorld.OVERWORLD.toString())){
                  return Identifier.of(MOD_ID,"waystone_overworld");
               }else if(dim.toString().equals(ServerWorld.NETHER.toString())){
                  return Identifier.of(MOD_ID,"waystone_nether");
               }else if(dim.toString().equals(ServerWorld.END.toString())){
                  return Identifier.of(MOD_ID,"waystone_end");
               }else{
                  return Identifier.of(MOD_ID,"waystone_unknown");
               }
            }
         }else{
            return Registries.ITEM.getKey(getPolymerItem(stack,context)).get().getValue();
         }
      }
   }
   
   public record WaystoneTarget(RegistryKey<World> world, Vec3d position, float yaw, float pitch){}
}
