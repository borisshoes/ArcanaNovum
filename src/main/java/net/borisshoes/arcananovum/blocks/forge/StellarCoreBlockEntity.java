package net.borisshoes.arcananovum.blocks.forge;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.*;
import net.borisshoes.arcananovum.gui.stellarcore.StellarCoreGui;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static net.borisshoes.arcananovum.blocks.forge.StellarCore.StellarCoreBlock.HORIZONTAL_FACING;

public class StellarCoreBlockEntity extends BlockEntity implements PolymerObject, MagicBlockEntity {
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private boolean synthetic;
   private String customName;
   private final Multiblock multiblock;
   private boolean assembled;
   
   public StellarCoreBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.STELLAR_CORE_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.STELLAR_CORE).getMultiblock();
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, boolean synthetic, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.synthetic = synthetic;
      this.customName = customName == null ? "" : customName;
   }
   
   public static <E extends BlockEntity> void ticker(World world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof StellarCoreBlockEntity core){
         core.tick();
      }
   }
   
   private void tick(){
      if (!(this.world instanceof ServerWorld serverWorld)) {
         return;
      }
      int ticks = serverWorld.getServer().getTicks();
      BlockState blockState = serverWorld.getBlockState(pos);
      
      if(ticks % 10 == 0){
         this.assembled = multiblock.matches(getMultiblockCheck());
      }
      
      if(assembled){
         Direction dir = blockState.get(StellarCore.StellarCoreBlock.HORIZONTAL_FACING);
         ParticleEffectUtils.stellarCoreAnim(serverWorld,pos.add(dir.getVector().multiply(-2)).toCenterPos().add(0,1,0),ticks % 300, dir);
      }
      
      
      boolean lit = blockState.get(StellarCore.StellarCoreBlock.LIT);
      if(lit ^ assembled){
         blockState = blockState.with(StellarCore.StellarCoreBlock.LIT,assembled);
         world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
      }
   }
   
   public List<ItemStack> salvageItem(ItemStack stack){
      List<ItemStack> salvage = new ArrayList<>();
      double salvageLvl = .25*(1+ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.DYSON_SPHERE.id));
      Item item = stack.getItem();
      if(MagicItemUtils.isMagic(stack)) return salvage;
      
      if(item instanceof ArmorItem armor){
         EquipmentSlot slot = armor.getSlotType();
         int baseAmt = switch(slot){
            case MAINHAND, OFFHAND -> 1;
            case FEET -> 4;
            case LEGS -> 7;
            case CHEST -> 8;
            case HEAD -> 5;
         };
         ItemStack[] repairItems = armor.getMaterial().getRepairIngredient().getMatchingStacks();
         if(repairItems[0].isOf(Items.NETHERITE_INGOT)){
            salvage.add(new ItemStack(Items.NETHERITE_SCRAP,(int) Math.round(4*salvageLvl)));
         }else{
            salvage.add(repairItems[0].copyWithCount((int) Math.round(baseAmt*salvageLvl)));
         }
      }else if(item instanceof ToolItem tool){
         int baseAmt;
         if(item instanceof PickaxeItem){
            baseAmt = 3;
         }else if(item instanceof HoeItem){
            baseAmt = 2;
         }else if(item instanceof SwordItem){
            baseAmt = 2;
         }else if(item instanceof AxeItem){
            baseAmt = 3;
         }else if(item instanceof ShovelItem){
            baseAmt = 1;
         }else{
            baseAmt = 1;
         }
         
         ItemStack[] repairItems = tool.getMaterial().getRepairIngredient().getMatchingStacks();
         if(repairItems.length > 0){
            if(repairItems[0].isOf(Items.NETHERITE_INGOT)){
               salvage.add(new ItemStack(Items.NETHERITE_SCRAP,(int) Math.max(1,Math.round(4*salvageLvl))));
            }else{
               salvage.add(repairItems[0].copyWithCount((int) Math.max(1,Math.round(baseAmt*salvageLvl))));
            }
         }
      }
      
      int stardust = (int) (MiscUtils.calcEssenceFromEnchants(stack) * (1 + .15*ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.FUSION_INJECTORS.id)));
      if(stardust > 0){
         while(stardust > 64){
            salvage.add(ArcanaRegistry.STARDUST.getDefaultStack().copyWithCount(64));
            stardust -= 64;
         }
         if(stardust > 0){
            salvage.add(ArcanaRegistry.STARDUST.getDefaultStack().copyWithCount(stardust));
         }
      }
      
      List<ItemStack> salvageReturn = new ArrayList<>();
      for(int i=0; i<stack.getCount(); i++){
         salvageReturn.addAll(salvage);
      }
      
      return salvageReturn;
   }
   
   public void openGui(ServerPlayerEntity player){
      StellarCoreGui gui = new StellarCoreGui(player,this);
      gui.buildGui();
      if(!gui.tryOpen(player)){
         player.sendMessage(Text.literal("Someone else is using the Core").formatted(Formatting.RED),true);
      }
   }
   
   public boolean isAssembled(){
      return assembled;
   }
   
   public Multiblock.MultiblockCheck getMultiblockCheck(){
      if (!(this.world instanceof ServerWorld serverWorld)) {
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld,pos,serverWorld.getBlockState(pos),new BlockPos(-2,-1,-4),serverWorld.getBlockState(pos).get(HORIZONTAL_FACING));
   }
   
   public TreeMap<ArcanaAugment, Integer> getAugments(){
      return augments;
   }
   
   public String getCrafterId(){
      return crafterId;
   }
   
   public String getUuid(){
      return uuid;
   }
   
   public boolean isSynthetic(){
      return synthetic;
   }
   
   public String getCustomArcanaName(){
      return customName;
   }
   
   public MagicItem getMagicItem(){
      return ArcanaRegistry.STELLAR_CORE;
   }
   
   @Override
   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      if (nbt.contains("arcanaUuid")) {
         this.uuid = nbt.getString("arcanaUuid");
      }
      if (nbt.contains("crafterId")) {
         this.crafterId = nbt.getString("crafterId");
      }
      if (nbt.contains("customName")) {
         this.customName = nbt.getString("customName");
      }
      if (nbt.contains("synthetic")) {
         this.synthetic = nbt.getBoolean("synthetic");
      }
      augments = new TreeMap<>();
      if(nbt.contains("arcanaAugments")){
         NbtCompound augCompound = nbt.getCompound("arcanaAugments");
         for(String key : augCompound.getKeys()){
            ArcanaAugment aug = ArcanaAugments.registry.get(key);
            if(aug != null) augments.put(aug,augCompound.getInt(key));
         }
      }
   }
   
   @Override
   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      if(augments != null){
         NbtCompound augsCompound = new NbtCompound();
         for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
            augsCompound.putInt(entry.getKey().id,entry.getValue());
         }
         nbt.put("arcanaAugments",augsCompound);
      }
      if(this.uuid != null){
         nbt.putString("arcanaUuid",this.uuid);
      }
      if(this.crafterId != null){
         nbt.putString("crafterId",this.crafterId);
      }
      if(this.customName != null){
         nbt.putString("customName",this.customName);
      }
      nbt.putBoolean("synthetic",this.synthetic);
   }
}