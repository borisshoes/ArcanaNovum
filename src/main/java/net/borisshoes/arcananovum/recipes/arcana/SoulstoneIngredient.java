package net.borisshoes.arcananovum.recipes.arcana;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SoulstoneIngredient extends MagicItemIngredient{
   
   private final int souls;
   private final String type;
   private final boolean consume;
   private final boolean repeatable;
   private final boolean allowBosses;
   
   public SoulstoneIngredient(int souls, boolean repeatable, boolean consume, boolean allowBosses, @Nullable String type){
      super(ArcanaRegistry.SOULSTONE.getPrefItem().getItem(), 1, null);
      this.souls = souls;
      this.repeatable = repeatable;
      this.consume = consume;
      this.allowBosses = allowBosses;
      this.type = type;
   }
   
   @Override
   public MagicItemIngredient copyWithCount(int newCount){
      return new SoulstoneIngredient(souls,repeatable,consume,allowBosses,type);
   }
   
   @Override
   public boolean validStack(ItemStack stack){
      if(MagicItemUtils.identifyItem(stack) instanceof Soulstone){
         if(type != null){
            if(!type.equals(Soulstone.getType(stack)))
               return false;
         }
         if(!allowBosses){
            if((Soulstone.getType(stack).equals("minecraft:ender_dragon") || Soulstone.getType(stack).equals("minecraft:wither")))
               return false;
         }
         return Soulstone.getSouls(stack) >= souls;
      }else{
         return false;
      }
   }
   
   @Override
   public ItemStack getRemainder(ItemStack stack, int resourceLvl){
      if(!validStack(stack)){
         return ItemStack.EMPTY;
      }else{
         if(consume){
            return ItemStack.EMPTY;
         }else{
            if(souls == 0) {
               return stack;
            }
            if(repeatable){
               return Soulstone.setSouls(stack,Soulstone.getSouls(stack) % souls);
            }else{
               return Soulstone.setSouls(stack,Soulstone.getSouls(stack)-souls);
            }
         }
      }
   }
   
   @Override
   public String getName(){
      String name = ArcanaRegistry.SOULSTONE.getNameString();
      if(!consume){
         name += " (Not Consumed)";
      }
      if(type != null){
         name += " ("+souls+"+ "+EntityType.get(type).get().getName().getString()+")";
      }
      return name;
   }
   
   @Override
   public ItemStack ingredientAsStack(){
      return Soulstone.getShowcaseItem(souls,type);
   }
}
