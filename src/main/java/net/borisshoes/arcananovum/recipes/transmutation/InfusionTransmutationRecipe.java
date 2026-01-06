package net.borisshoes.arcananovum.recipes.transmutation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class InfusionTransmutationRecipe extends TransmutationRecipe{
   
   private final List<Either<Item, TagKey<Item>>> input = new ArrayList<>();
   private final int inputCount;
   private final ItemStack output;
   private final int outputCount;
   private String inputName = "";
   
   public InfusionTransmutationRecipe(String id, ItemStack input, ItemStack output, ItemStack reagent1, ItemStack reagent2){
      super(id,reagent1,reagent2);
      this.input.add(Either.left(input.getItem()));
      this.inputCount = input.getCount();
      this.output = output;
      this.outputCount = output.getCount();
   }
   
   private InfusionTransmutationRecipe(String id, List<Either<Item, TagKey<Item>>> inputs, int inputCount, String inputName, ItemStack output, int outputCount, List<Either<Item, TagKey<Item>>> reagent1, int reagent1Count, List<Either<Item, TagKey<Item>>> reagent2, int reagent2Count){
      super(id, reagent1, reagent1Count, reagent2, reagent2Count);
      this.input.addAll(inputs);
      this.inputCount = inputCount;
      this.inputName = inputName;
      this.output = output;
      this.outputCount = outputCount;
   }
   
   @Override
   public List<ItemStack> doTransmutation(ItemStack positiveInput, ItemStack negativeInput, ItemStack reagent1, ItemStack reagent2, ItemStack aequalisInput, ServerPlayer player){
      List<ItemStack> returnItems = new ArrayList<>();
      int iterations = positiveInput.getCount() / inputCount;
      for(int i = 0; i < iterations; i++){
         ItemStack outputStack = output.copyWithCount(outputCount);
         if(ArcanaItemUtils.isArcane(outputStack)){
            ArcanaItem arcanaOutputItem = ArcanaItemUtils.identifyItem(outputStack);
            outputStack = arcanaOutputItem.addCrafter(arcanaOutputItem.getNewItem(),player.getStringUUID(),0,player.level().getServer());
         }
         returnItems.add(outputStack);
      }
      returnItems.add(aequalisInput);
      return returnItems;
   }
   
   @Override
   public List<Tuple<ItemStack,String>> doTransmutation(ItemEntity input1Entity, ItemEntity input2Entity, ItemEntity reagent1Entity, ItemEntity reagent2Entity, ItemEntity aequalisEntity, TransmutationAltarBlockEntity altar, ServerPlayer player){
      int bargainLvl = ArcanaAugments.getAugmentFromMap(altar.getAugments(),ArcanaAugments.HASTY_BARGAIN.id);
      ItemStack reagent1Stack = reagent1Entity != null ? reagent1Entity.getItem() : ItemStack.EMPTY;
      ItemStack reagent2Stack = reagent2Entity != null ? reagent2Entity.getItem() : ItemStack.EMPTY;
      ItemStack inputStack;
      ItemEntity inputEntity;
      String outputPos;
      if(input1Entity != null && validInput(input1Entity.getItem())){
         inputStack = input1Entity.getItem();
         inputEntity = input1Entity;
         outputPos = "negative";
      }else if(input2Entity != null && validInput(input2Entity.getItem())){
         inputStack = input2Entity.getItem();
         inputEntity = input2Entity;
         outputPos = "positive";
      }else{
         return new ArrayList<>();
      }
      if(!canTransmute(inputStack, ItemStack.EMPTY,reagent1Stack,reagent2Stack, ItemStack.EMPTY,altar)) return new ArrayList<>();
      
      List<Tuple<ItemStack,String>> outputs = new ArrayList<>();
      int iterations = inputStack.getCount() / inputCount;
      int consumedInput = iterations * inputCount;
      
      for(int i = 0; i < iterations; i++){
         ItemStack outputStack = output.copyWithCount(outputCount);
         if(ArcanaItemUtils.isArcane(outputStack)){
            ArcanaItem arcanaOutputItem = ArcanaItemUtils.identifyItem(outputStack);
            outputStack = arcanaOutputItem.addCrafter(arcanaOutputItem.getNewItem(),player == null ? null : player.getStringUUID(),0, BorisLib.SERVER);
         }
         
         outputs.add(new Tuple<>(outputStack,outputPos));
      }
      
      if(inputStack.getCount() == consumedInput){
         inputEntity.discard();
      }else{
         inputStack.shrink(consumedInput);
         inputEntity.setItem(inputStack);
      }
      
      boolean m11 = validReagent1(reagent1Stack,bargainLvl), m22 = validReagent2(reagent2Stack,bargainLvl), m12 = validReagent1(reagent2Stack,bargainLvl), m21 = validReagent2(reagent1Stack,bargainLvl);
      boolean straight = m11 && m22;
      boolean cross = !straight && m12 && m21;
      if (!straight && !cross) return new ArrayList<>(); // should be impossible
      
      ItemStack reagent1 = straight ? getComputedReagent1(reagent1Stack,bargainLvl) : getComputedReagent2(reagent1Stack,bargainLvl);
      ItemStack reagent2 = straight ? getComputedReagent2(reagent2Stack,bargainLvl) : getComputedReagent1(reagent2Stack,bargainLvl);
      
      if (reagent1Entity != null) {
         int take = reagent1.isEmpty() ? 0 : reagent1.getCount();
         if(take > 0){
            if(reagent1Stack.getCount() == take){
               reagent1Entity.discard();
            }else{
               reagent1Stack.shrink(take);
               reagent1Entity.setItem(reagent1Stack);
            }
         }
      }
      
      if (reagent2Entity != null) {
         int take = reagent2.isEmpty() ? 0 : reagent2.getCount();
         if(take > 0){
            if(reagent2Stack.getCount() == take){
               reagent2Entity.discard();
            }else{
               reagent2Stack.shrink(take);
               reagent2Entity.setItem(reagent2Stack);
            }
         }
      }
      
      return outputs;
   }
   
   @Override
   public boolean canTransmute(ItemStack input1, ItemStack input2, ItemStack reagent1Input, ItemStack reagent2Input, ItemStack aequalisInput, TransmutationAltarBlockEntity altar){
      int bargainLvl = ArcanaAugments.getAugmentFromMap(altar.getAugments(),ArcanaAugments.HASTY_BARGAIN.id);
      boolean reagentCheck1 = validReagent1(reagent1Input,bargainLvl) && validReagent2(reagent2Input,bargainLvl);
      boolean reagentCheck2 = validReagent1(reagent2Input,bargainLvl) && validReagent2(reagent1Input,bargainLvl);
      if (!(reagentCheck1 || reagentCheck2)) return false;

      ItemStack inputStack;
      if(validInput(input1)){
         inputStack = input1;
      }else if(validInput(input2)){
         inputStack = input2;
      }else{
         return false;
      }
      
      if(ArcanaItemUtils.isArcane(inputStack) && ArcanaItemUtils.isArcane(output) && altar.getLevel() instanceof ServerLevel serverWorld){
         ArcanaItem arcanaInputItem = ArcanaItemUtils.identifyItem(inputStack);
         ArcanaItem arcanaOutputItem = ArcanaItemUtils.identifyItem(output);
         ServerPlayer player = serverWorld.getServer().getPlayerList().getPlayer(AlgoUtils.getUUID(arcanaInputItem.getCrafter(inputStack)));
         return player == null || ArcanaNovum.data(player).hasResearched(arcanaOutputItem);
      }
      
      return true;
   }
   
   public boolean validInput(ItemStack provided){
      boolean itemMatch = this.input.stream().anyMatch(e -> (e.left().isPresent() && provided.is(e.left().get())) || (e.right().isPresent() && provided.is(e.right().get())));
      return itemMatch && provided.getCount() >= inputCount;
   }
   
   @Override
   public ItemStack getViewStack(){
      return MinecraftUtils.removeLore(output.copyWithCount(this.outputCount));
   }
   
   public int getInputCount(){
      return inputCount;
   }
   
   public MutableComponent getInputName(){
      if(inputName != null && !inputName.isBlank()) return Component.translatable(inputName);
      Item first = null;
      Item second = null;
      for(Either<Item, TagKey<Item>> either : this.input){
         if(either.left().isPresent()){
            first = either.left().get();
            break;
         }else if(either.right().isPresent()){
            TagKey<Item> itemTag = either.right().get();
            Optional<HolderSet.Named<Item>> opt = BuiltInRegistries.ITEM.get(itemTag);
            if(opt.isPresent()){
               for(Holder<Item> holder : opt.get()){
                  second = holder.value();
                  break;
               }
            }
         }
      }
      if(first != null){
         return first.getName().copy();
      }
      if(second != null){
         return second.getName().copy();
      }
      return Component.literal("???");
   }
   
   public Predicate<ItemStack> getInputPredicate(){
      return (stack) -> this.input.stream().anyMatch(e ->
            ((e.left().isPresent() && stack.is(e.left().get())) || (e.right().isPresent() && stack.is(e.right().get()))) &&
            stack.getCount() >= this.inputCount);
   }
   
   public ItemStack getOutput(){
      return output;
   }
   
   public JsonObject toJson(){
      JsonObject json = new JsonObject();
      json.addProperty("type", "arcananovum:infusion_transmutation");
      json.addProperty("id", id);
      
      if(inputName != null && !inputName.isBlank()){
         json.addProperty("input_name", inputName);
      }
      
      // Serialize inputs
      JsonArray inputsArray = new JsonArray();
      for(Either<Item, TagKey<Item>> entry : input){
         if(entry.left().isPresent()){
            inputsArray.add(BuiltInRegistries.ITEM.getKey(entry.left().get()).toString());
         }else if(entry.right().isPresent()){
            inputsArray.add("#" + entry.right().get().location().toString());
         }
      }
      json.add("inputs", inputsArray);
      json.addProperty("input_count", inputCount);
      
      // Serialize reagent 1
      if(!reagent1.isEmpty()){
         JsonArray reagent1Array = new JsonArray();
         for(Either<Item, TagKey<Item>> entry : reagent1){
            if(entry.left().isPresent()){
               reagent1Array.add(BuiltInRegistries.ITEM.getKey(entry.left().get()).toString());
            }else if(entry.right().isPresent()){
               reagent1Array.add("#" + entry.right().get().location().toString());
            }
         }
         json.add("reagent_1", reagent1Array);
         json.addProperty("reagent_1_count", reagent1Count);
      }
      
      // Serialize reagent 2
      if(!reagent2.isEmpty()){
         JsonArray reagent2Array = new JsonArray();
         for(Either<Item, TagKey<Item>> entry : reagent2){
            if(entry.left().isPresent()){
               reagent2Array.add(BuiltInRegistries.ITEM.getKey(entry.left().get()).toString());
            }else if(entry.right().isPresent()){
               reagent2Array.add("#" + entry.right().get().location().toString());
            }
         }
         json.add("reagent_2", reagent2Array);
         json.addProperty("reagent_2_count", reagent2Count);
      }
      
      // Serialize output - use SNBT if it has components, otherwise just item id
      if(ItemStack.isSameItemSameComponents(output,new ItemStack(output.getItem()))){
         json.addProperty("output", BuiltInRegistries.ITEM.getKey(output.getItem()).toString());
      }else{
         RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, BorisLib.SERVER.registryAccess());
         json.add("output", ItemStack.CODEC.encodeStart(ops, output).getOrThrow());
      }
      json.addProperty("output_count", outputCount);
      
      return json;
   }
   
   public static InfusionTransmutationRecipe fromJson(JsonObject json){
      if(!json.has("type") || !json.get("type").getAsString().equals("arcananovum:infusion_transmutation")) return null;
      
      String id = json.get("id").getAsString();
      String inputName = json.has("input_name") ? json.get("input_name").getAsString() : "";
      
      // Parse inputs
      List<Either<Item, TagKey<Item>>> inputs = new ArrayList<>();
      JsonArray inputsArray = json.getAsJsonArray("inputs");
      for(JsonElement element : inputsArray){
         Either<Item, TagKey<Item>> parsed = MinecraftUtils.parseItemOrTag(element.getAsString());
         if(parsed != null) inputs.add(parsed);
      }
      int inputCount = json.get("input_count").getAsInt();
      
      // Parse reagent 1
      List<Either<Item, TagKey<Item>>> reagent1List = new ArrayList<>();
      int reagent1Count = 0;
      if(json.has("reagent_1")){
         JsonArray reagent1Array = json.getAsJsonArray("reagent_1");
         for(JsonElement element : reagent1Array){
            Either<Item, TagKey<Item>> parsed = MinecraftUtils.parseItemOrTag(element.getAsString());
            if(parsed != null) reagent1List.add(parsed);
         }
         reagent1Count = json.has("reagent_1_count") ? json.get("reagent_1_count").getAsInt() : 1;
      }
      
      // Parse reagent 2
      List<Either<Item, TagKey<Item>>> reagent2List = new ArrayList<>();
      int reagent2Count = 0;
      if(json.has("reagent_2")){
         JsonArray reagent2Array = json.getAsJsonArray("reagent_2");
         for(JsonElement element : reagent2Array){
            Either<Item, TagKey<Item>> parsed = MinecraftUtils.parseItemOrTag(element.getAsString());
            if(parsed != null) reagent2List.add(parsed);
         }
         reagent2Count = json.has("reagent_2_count") ? json.get("reagent_2_count").getAsInt() : 1;
      }
      // Parse output
      ItemStack output;
      JsonElement outputElement = json.get("output");
      if(outputElement.isJsonPrimitive()){
         // Simple item id
         Item outputItem = BuiltInRegistries.ITEM.getValue(Identifier.parse(outputElement.getAsString()));
         output = new ItemStack(outputItem);
      }else{
         // SNBT/complex item stack
         RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, BorisLib.SERVER.registryAccess());
         output = ItemStack.CODEC.parse(ops, outputElement).getOrThrow();
      }
      int outputCount = json.get("output_count").getAsInt();
      
      return new InfusionTransmutationRecipe(id, inputs, inputCount, inputName, output, outputCount, reagent1List, reagent1Count, reagent2List, reagent2Count);
   }
}
