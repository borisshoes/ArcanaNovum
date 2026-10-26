package net.borisshoes.arcananovum.test.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Stream;

/**
 * A static picture of every mixin in arcananovum.mixins.json, resolved against the target classes on the classpath.
 * <p>
 * For each injector it records which target methods it hits and how its injection point matches: the number of matching
 * instructions, which ordinal is used, and whether each match runs on every path through the method ("always") or only
 * inside a branch ("conditional"). It also stores a normalized bytecode listing of every targeted method, so a Minecraft
 * update that edits a hooked method shows up as a diff even when the mixin still applies.
 * <p>
 * Compared with the baseline in src/test/mixin-baseline by {@link MixinSnapshotTests} and {@link MixinSnapshotTool}.
 */
public class MixinSnapshot {
   
   public static final String MIXIN_CONFIG = "arcananovum.mixins.json";
   public static final String ENTRIES_FILE = "injectors.txt";
   public static final String METHODS_DIR = "methods";
   
   private static final Set<String> INJECTOR_PACKAGES = Set.of("org/spongepowered/asm/mixin/injection/", "com/llamalad7/mixinextras/");
   private static final String OVERWRITE = "Lorg/spongepowered/asm/mixin/Overwrite;";
   private static final String SHADOW = "Lorg/spongepowered/asm/mixin/Shadow;";
   private static final String ACCESSOR = "Lorg/spongepowered/asm/mixin/gen/Accessor;";
   private static final String INVOKER = "Lorg/spongepowered/asm/mixin/gen/Invoker;";
   private static final String MIXIN = "Lorg/spongepowered/asm/mixin/Mixin;";
   
   /** Entry key (Mixin#handler) to its description lines */
   public final TreeMap<String, List<String>> entries = new TreeMap<>();
   /** Method listing file name to normalized listing lines */
   public final TreeMap<String, List<String>> methods = new TreeMap<>();
   /** Problems that would make a mixin fail to apply, independent of the baseline */
   public final List<String> errors = new ArrayList<>();
   /** Mixin class (internal name) to the target classes it declares, for mixins that apply on a dedicated server */
   public final TreeMap<String, List<String>> serverTargets = new TreeMap<>();
   /** Mixin classes with no fields or methods, which do nothing and can be deleted */
   public final Set<String> emptyMixins = new TreeSet<>();
   
   // ===================================
   //            CAPTURE
   // ===================================
   
   public static MixinSnapshot capture(ClassSource source){
      MixinSnapshot snapshot = new MixinSnapshot();
      byte[] configBytes = source.readResource(MIXIN_CONFIG);
      if(configBytes == null){
         snapshot.errors.add("Could not find " + MIXIN_CONFIG + " on the classpath");
         return snapshot;
      }
      JsonObject config = JsonParser.parseString(new String(configBytes, StandardCharsets.UTF_8)).getAsJsonObject();
      String pkg = config.get("package").getAsString().replace('.', '/');
      for(String section : List.of("mixins", "server", "client")){
         if(!config.has(section)) continue;
         for(JsonElement element : config.getAsJsonArray(section)){
            String mixinName = pkg + "/" + element.getAsString().replace('.', '/');
            ClassNode mixin = source.getClass(mixinName);
            if(mixin == null){
               snapshot.errors.add("Mixin class " + mixinName + " is listed in " + MIXIN_CONFIG + " but was not found");
               continue;
            }
            List<String> targets = snapshot.captureMixin(source, mixin);
            if(!section.equals("client")) snapshot.serverTargets.put(mixin.name, targets);
         }
      }
      return snapshot;
   }
   
   private List<String> captureMixin(ClassSource source, ClassNode mixin){
      String mixinName = mixin.name.substring(mixin.name.lastIndexOf('/') + 1);
      AnnotationNode mixinAnnotation = findAnnotation(mixin.invisibleAnnotations, mixin.visibleAnnotations, MIXIN);
      if(mixinAnnotation == null){
         errors.add(mixinName + " has no @Mixin annotation");
         return List.of();
      }
      List<String> targetNames = new ArrayList<>();
      for(Object type : asList(attr(mixinAnnotation, "value"))) targetNames.add(((Type) type).getInternalName());
      for(Object name : asList(attr(mixinAnnotation, "targets"))) targetNames.add(((String) name).replace('.', '/'));
      
      List<ClassNode> targets = new ArrayList<>();
      List<String> header = new ArrayList<>();
      for(String targetName : targetNames){
         ClassNode target = source.getClass(targetName);
         if(target == null){
            errors.add(mixinName + ": target class " + targetName + " not found");
            header.add("target " + targetName + " MISSING");
            continue;
         }
         targets.add(target);
         // Anonymous and inner targets (ServerPlayer$2) get renumbered between versions, so record their shape
         header.add("target " + targetName + " extends " + target.superName + (target.interfaces.isEmpty() ? "" : " implements " + String.join(", ", target.interfaces)));
      }
      if(mixin.fields.isEmpty() && mixin.methods.stream().allMatch(m -> m.name.equals("<init>") || m.name.equals("<clinit>"))){
         emptyMixins.add(mixin.name);
         header.add("empty mixin: does nothing, can be deleted");
      }
      entries.put(mixinName, header);
      
      for(FieldNode field : mixin.fields){
         AnnotationNode shadow = findAnnotation(field.invisibleAnnotations, field.visibleAnnotations, SHADOW);
         if(shadow == null) continue;
         String name = stripPrefix(field.name, shadow);
         List<String> lines = new ArrayList<>();
         for(ClassNode target : targets){
            boolean found = findField(source, target, name, field.desc) != null;
            lines.add("@Shadow field " + name + " " + field.desc + " in " + target.name + ": " + (found ? "present" : "MISSING"));
            if(!found) errors.add(mixinName + ": @Shadow field " + name + " not found in " + target.name);
         }
         entries.put(mixinName + "#" + field.name, lines);
      }
      
      for(MethodNode handler : mixin.methods){
         List<String> lines = new ArrayList<>();
         String key = mixinName + "#" + handler.name;
         if(entries.containsKey(key)) key += handler.desc;
         
         List<AnnotationNode> annotations = new ArrayList<>();
         if(handler.invisibleAnnotations != null) annotations.addAll(handler.invisibleAnnotations);
         if(handler.visibleAnnotations != null) annotations.addAll(handler.visibleAnnotations);
         for(AnnotationNode annotation : annotations){
            String type = annotation.desc.substring(1, annotation.desc.length() - 1);
            String simple = "@" + type.substring(type.lastIndexOf('/') + 1);
            if(annotation.desc.equals(SHADOW)){
               String name = stripPrefix(handler.name, annotation);
               for(ClassNode target : targets){
                  boolean found = findMethod(source, target, name, handler.desc) != null;
                  lines.add("@Shadow method " + name + handler.desc + " in " + target.name + ": " + (found ? "present" : "MISSING"));
                  if(!found) errors.add(mixinName + ": @Shadow method " + name + handler.desc + " not found in " + target.name);
               }
            }else if(annotation.desc.equals(ACCESSOR) || annotation.desc.equals(INVOKER)){
               boolean accessor = annotation.desc.equals(ACCESSOR);
               String name = (String) attr(annotation, "value");
               if(name == null || name.isEmpty()) name = inferAccessorName(handler.name, accessor);
               for(ClassNode target : targets){
                  boolean found = accessor ? findField(source, target, name, null) != null : findMethodByName(source, target, name) != null;
                  lines.add(simple + " " + name + " in " + target.name + ": " + (found ? "present" : "MISSING"));
                  if(!found) errors.add(mixinName + ": " + simple + " target " + name + " not found in " + target.name);
               }
            }else if(annotation.desc.equals(OVERWRITE)){
               // An overwrite silently drops any vanilla change to the method, so its listing is tracked like an injector target
               for(ClassNode target : targets){
                  MethodNode targetMethod = findDeclared(target, handler.name, handler.desc);
                  if(targetMethod == null){
                     lines.add("@Overwrite " + target.name + "." + handler.name + handler.desc + ": MISSING");
                     errors.add(mixinName + ": @Overwrite target " + handler.name + handler.desc + " not found in " + target.name);
                  }else{
                     lines.add("@Overwrite " + target.name + "." + handler.name + handler.desc + " listing " + recordListing(target, targetMethod));
                  }
               }
            }else if(INJECTOR_PACKAGES.stream().anyMatch(type::startsWith) && attr(annotation, "method") != null){
               captureInjector(mixinName, simple, annotation, targets, lines);
            }
         }
         if(!lines.isEmpty()) entries.put(key, lines);
      }
      return targetNames;
   }
   
   private void captureInjector(String mixinName, String simple, AnnotationNode annotation, List<ClassNode> targets, List<String> lines){
      List<AnnotationNode> ats = new ArrayList<>();
      for(Object at : asList(attr(annotation, "at"))) ats.add((AnnotationNode) at);
      boolean sliced = !asList(attr(annotation, "slice")).isEmpty();
      int[] totals = new int[ats.size()]; // Mixin only needs a match in one of the selected methods, not in each
      boolean anyMethod = false;
      
      for(Object selectorObj : asList(attr(annotation, "method"))){
         String selector = (String) selectorObj;
         for(ClassNode target : targets){
            List<MethodNode> matched = selectMethods(target, selector);
            if(matched.isEmpty()){
               lines.add(simple + " method \"" + selector + "\" in " + target.name + ": NO TARGET METHOD");
               errors.add(mixinName + ": " + simple + " found no method matching \"" + selector + "\" in " + target.name);
               continue;
            }
            for(MethodNode method : matched){
               anyMethod = true;
               lines.add(simple + " -> " + target.name + "." + method.name + method.desc + " listing " + recordListing(target, method));
               if(simple.equals("@ModifyConstant")){
                  lines.add("  constant: not analyzed");
               }
               for(int i = 0; i < ats.size(); i++){
                  AnnotationNode at = ats.get(i);
                  List<AbstractInsnNode> matches = findMatches((String) attr(at, "value"), (String) attr(at, "target"), (Integer) attr(at, "opcode"), method);
                  totals[i] += matches == null ? 1 : matches.size();
                  lines.add("  " + describeAt(mixinName, at, method, matches, sliced));
               }
            }
         }
      }
      for(int i = 0; i < ats.size(); i++){
         if(anyMethod && totals[i] == 0 && !sliced){
            errors.add(mixinName + ": " + simple + " injection point " + attr(ats.get(i), "value") + " " + attr(ats.get(i), "target") + " has no matches in any selected method");
         }
      }
   }
   
   private String describeAt(String mixinName, AnnotationNode at, MethodNode method, List<AbstractInsnNode> matches, boolean sliced){
      String value = (String) attr(at, "value");
      String targetString = (String) attr(at, "target");
      Integer ordinal = (Integer) attr(at, "ordinal");
      Integer opcode = (Integer) attr(at, "opcode");
      String[] shiftEnum = (String[]) attr(at, "shift");
      
      StringBuilder desc = new StringBuilder("at ").append(value);
      if(targetString != null && !targetString.isEmpty()) desc.append(" ").append(targetString);
      if(ordinal != null) desc.append(" ordinal=").append(ordinal);
      if(opcode != null) desc.append(" opcode=").append(opcode);
      if(shiftEnum != null) desc.append(" shift=").append(shiftEnum[1]);
      if(sliced) desc.append(" (sliced, counts are for the whole method)");
      desc.append(": ");
      
      if(matches == null){
         return desc.append("not analyzed").toString();
      }
      if(matches.isEmpty()){
         return desc.append("NO MATCHES").toString();
      }
      
      List<String> reach = new ArrayList<>();
      for(AbstractInsnNode match : matches){
         reach.add(isAlwaysReached(method, match) ? "always" : "conditional");
      }
      desc.append(matches.size()).append(matches.size() == 1 ? " match " : " matches ").append(reach);
      if(ordinal != null && ordinal >= 0){
         if(ordinal >= matches.size()){
            errors.add(mixinName + ": ordinal " + ordinal + " of " + value + " " + targetString + " is out of range in " + method.name + " (" + matches.size() + " matches)");
            desc.append(", ordinal OUT OF RANGE");
         }else{
            desc.append(", ordinal ").append(ordinal).append(" is ").append(reach.get(ordinal));
         }
      }
      return desc.toString();
   }
   
   /** Returns the instructions an injection point matches, or null if this kind of point is not analyzed */
   private static List<AbstractInsnNode> findMatches(String value, String target, Integer opcode, MethodNode method){
      List<AbstractInsnNode> matches = new ArrayList<>();
      AbstractInsnNode[] insns = method.instructions.toArray();
      switch(value){
         case "HEAD" -> {
            for(AbstractInsnNode insn : insns){
               if(insn.getOpcode() >= 0){
                  matches.add(insn);
                  break;
               }
            }
         }
         case "RETURN", "TAIL" -> {
            for(AbstractInsnNode insn : insns){
               if(insn.getOpcode() >= Opcodes.IRETURN && insn.getOpcode() <= Opcodes.RETURN) matches.add(insn);
            }
            if(value.equals("TAIL") && !matches.isEmpty()) return List.of(matches.getLast());
         }
         case "INVOKE", "INVOKE_ASSIGN", "INVOKE_STRING" -> {
            MemberRef ref = MemberRef.parse(target);
            if(ref == null) return null;
            for(AbstractInsnNode insn : insns){
               if(insn instanceof MethodInsnNode call && ref.matches(call.owner, call.name, call.desc)) matches.add(insn);
            }
         }
         case "FIELD" -> {
            MemberRef ref = MemberRef.parse(target);
            if(ref == null) return null;
            for(AbstractInsnNode insn : insns){
               if(insn instanceof FieldInsnNode field && ref.matches(field.owner, field.name, field.desc)
                     && (opcode == null || opcode == field.getOpcode())) matches.add(insn);
            }
         }
         case "NEW" -> {
            if(target == null) return null;
            String type = target.startsWith("L") && target.contains(";") ? target.substring(1, target.indexOf(';')) : target.replace('.', '/');
            for(AbstractInsnNode insn : insns){
               if(insn instanceof TypeInsnNode typeInsn && insn.getOpcode() == Opcodes.NEW && typeInsn.desc.equals(type)) matches.add(insn);
            }
         }
         case "JUMP" -> {
            for(AbstractInsnNode insn : insns){
               if(insn instanceof JumpInsnNode && (opcode == null || opcode == insn.getOpcode())) matches.add(insn);
            }
         }
         default -> {
            return null; // CONSTANT, LOAD, STORE, MIXINEXTRAS:EXPRESSION, ... depend on discriminators we don't model
         }
      }
      return matches;
   }
   
   /**
    * True when every normal path from the start of the method to a return goes through the instruction.
    * A hook on a "conditional" instruction only runs when the surrounding branch is taken, which is how the
    * Bone Smasher injection broke: getCombatTracker() moved inside an if(hasCustomName()) in die().
    */
   static boolean isAlwaysReached(MethodNode method, AbstractInsnNode target){
      InsnList list = method.instructions;
      int size = list.size();
      int avoid = list.indexOf(target);
      List<List<Integer>> successors = new ArrayList<>(size);
      for(int i = 0; i < size; i++) successors.add(new ArrayList<>());
      for(int i = 0; i < size; i++){
         AbstractInsnNode insn = list.get(i);
         int op = insn.getOpcode();
         if(insn instanceof JumpInsnNode jump){
            successors.get(i).add(list.indexOf(jump.label));
            if(op != Opcodes.GOTO && op != Opcodes.JSR && i + 1 < size) successors.get(i).add(i + 1);
         }else if(insn instanceof TableSwitchInsnNode table){
            successors.get(i).add(list.indexOf(table.dflt));
            for(LabelNode label : table.labels) successors.get(i).add(list.indexOf(label));
         }else if(insn instanceof LookupSwitchInsnNode lookup){
            successors.get(i).add(list.indexOf(lookup.dflt));
            for(LabelNode label : lookup.labels) successors.get(i).add(list.indexOf(label));
         }else if((op >= Opcodes.IRETURN && op <= Opcodes.RETURN) || op == Opcodes.ATHROW){
            // exit
         }else if(i + 1 < size){
            successors.get(i).add(i + 1);
         }
      }
      for(TryCatchBlockNode tryCatch : method.tryCatchBlocks){
         int start = list.indexOf(tryCatch.start), end = list.indexOf(tryCatch.end), handler = list.indexOf(tryCatch.handler);
         for(int i = start; i < end; i++) successors.get(i).add(handler);
      }
      
      boolean[] seen = new boolean[size];
      ArrayDeque<Integer> queue = new ArrayDeque<>();
      if(size > 0 && avoid != 0){
         queue.add(0);
         seen[0] = true;
      }
      while(!queue.isEmpty()){
         int i = queue.poll();
         int op = list.get(i).getOpcode();
         if(op >= Opcodes.IRETURN && op <= Opcodes.RETURN) return false;
         for(int next : successors.get(i)){
            if(next == avoid || seen[next]) continue;
            seen[next] = true;
            queue.add(next);
         }
      }
      return true;
   }
   
   private String recordListing(ClassNode owner, MethodNode method){
      List<String> listing = normalizedListing(owner, method);
      String file = owner.name.replace('/', '.') + "." + method.name.replace('<', '_').replace('>', '_') + "." + hash(method.desc).substring(0, 8) + ".txt";
      methods.put(file, listing);
      return hash(String.join("\n", listing)).substring(0, 12);
   }
   
   /** Bytecode listing without line numbers, frames or local variable names, so only real code changes show up */
   static List<String> normalizedListing(ClassNode owner, MethodNode method){
      MethodNode copy = new MethodNode(method.access, method.name, method.desc, method.signature, method.exceptions.toArray(new String[0]));
      method.accept(copy);
      Set<LabelNode> usedLabels = new HashSet<>();
      for(AbstractInsnNode insn : copy.instructions){
         if(insn instanceof JumpInsnNode jump) usedLabels.add(jump.label);
         if(insn instanceof TableSwitchInsnNode table){
            usedLabels.add(table.dflt);
            usedLabels.addAll(table.labels);
         }
         if(insn instanceof LookupSwitchInsnNode lookup){
            usedLabels.add(lookup.dflt);
            usedLabels.addAll(lookup.labels);
         }
      }
      for(TryCatchBlockNode tryCatch : copy.tryCatchBlocks){
         usedLabels.add(tryCatch.start);
         usedLabels.add(tryCatch.end);
         usedLabels.add(tryCatch.handler);
      }
      for(AbstractInsnNode insn : copy.instructions.toArray()){
         if(insn instanceof LineNumberNode || insn instanceof FrameNode || (insn instanceof LabelNode label && !usedLabels.contains(label))){
            copy.instructions.remove(insn);
         }
      }
      copy.localVariables = null;
      copy.visibleLocalVariableAnnotations = null;
      copy.invisibleLocalVariableAnnotations = null;
      copy.parameters = null;
      
      Textifier textifier = new Textifier();
      copy.accept(new TraceMethodVisitor(textifier));
      List<String> lines = new ArrayList<>();
      lines.add("// " + owner.name + "." + method.name + method.desc);
      flatten(textifier.getText(), lines);
      return lines;
   }
   
   private static void flatten(List<?> text, List<String> out){
      for(Object part : text){
         if(part instanceof List<?> nested){
            flatten(nested, out);
         }else{
            for(String line : part.toString().split("\n")){
               if(!line.isBlank()) out.add(line.stripTrailing());
            }
         }
      }
   }
   
   // ===================================
   //            LOOKUPS
   // ===================================
   
   private static List<MethodNode> selectMethods(ClassNode target, String selector){
      String name = selector;
      String desc = null;
      if(name.startsWith("L") && name.contains(";")) name = name.substring(name.indexOf(';') + 1);
      int paren = name.indexOf('(');
      if(paren >= 0){
         desc = name.substring(paren);
         name = name.substring(0, paren);
      }
      int colon = name.indexOf(':');
      if(colon >= 0){
         desc = name.substring(colon + 1);
         name = name.substring(0, colon);
      }
      List<MethodNode> matched = new ArrayList<>();
      for(MethodNode method : target.methods){
         boolean nameMatch = name.endsWith("*") ? method.name.startsWith(name.substring(0, name.length() - 1)) : method.name.equals(name);
         if(nameMatch && (desc == null || method.desc.equals(desc))) matched.add(method);
      }
      return matched;
   }
   
   private static MethodNode findDeclared(ClassNode target, String name, String desc){
      for(MethodNode method : target.methods){
         if(method.name.equals(name) && method.desc.equals(desc)) return method;
      }
      return null;
   }
   
   private static MethodNode findMethod(ClassSource source, ClassNode target, String name, String desc){
      for(ClassNode node = target; node != null; node = node.superName == null ? null : source.getClass(node.superName)){
         MethodNode method = findDeclared(node, name, desc);
         if(method != null) return method;
         for(String itf : node.interfaces){
            ClassNode itfNode = source.getClass(itf);
            if(itfNode != null && findDeclared(itfNode, name, desc) != null) return findDeclared(itfNode, name, desc);
         }
      }
      return null;
   }
   
   private static MethodNode findMethodByName(ClassSource source, ClassNode target, String name){
      for(ClassNode node = target; node != null; node = node.superName == null ? null : source.getClass(node.superName)){
         for(MethodNode method : node.methods){
            if(method.name.equals(name)) return method;
         }
      }
      return null;
   }
   
   private static FieldNode findField(ClassSource source, ClassNode target, String name, String desc){
      for(ClassNode node = target; node != null; node = node.superName == null ? null : source.getClass(node.superName)){
         for(FieldNode field : node.fields){
            if(field.name.equals(name) && (desc == null || field.desc.equals(desc))) return field;
         }
      }
      return null;
   }
   
   private static String inferAccessorName(String handlerName, boolean accessor){
      List<String> prefixes = accessor ? List.of("get", "set", "is") : List.of("call", "invoke", "new", "create");
      for(String prefix : prefixes){
         if(handlerName.startsWith(prefix) && handlerName.length() > prefix.length()){
            String rest = handlerName.substring(prefix.length());
            if(rest.equals(rest.toUpperCase(Locale.ROOT))) return rest; // getDATA_HEALTH_ID targets DATA_HEALTH_ID
            return Character.toLowerCase(rest.charAt(0)) + rest.substring(1);
         }
      }
      return handlerName;
   }
   
   private static String stripPrefix(String name, AnnotationNode shadow){
      String prefix = (String) attr(shadow, "prefix");
      if(prefix == null) prefix = "shadow$";
      return name.startsWith(prefix) ? name.substring(prefix.length()) : name;
   }
   
   private static AnnotationNode findAnnotation(List<AnnotationNode> invisible, List<AnnotationNode> visible, String desc){
      for(List<AnnotationNode> list : Arrays.asList(invisible, visible)){
         if(list == null) continue;
         for(AnnotationNode annotation : list){
            if(annotation.desc.equals(desc)) return annotation;
         }
      }
      return null;
   }
   
   private static Object attr(AnnotationNode annotation, String name){
      if(annotation.values == null) return null;
      for(int i = 0; i < annotation.values.size(); i += 2){
         if(annotation.values.get(i).equals(name)) return annotation.values.get(i + 1);
      }
      return null;
   }
   
   private static List<?> asList(Object value){
      if(value == null) return List.of();
      if(value instanceof List<?> list) return list;
      return List.of(value);
   }
   
   static String hash(String text){
      try{
         byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
         return HexFormat.of().formatHex(digest);
      }catch(NoSuchAlgorithmException e){
         throw new RuntimeException(e);
      }
   }
   
   /** A method or field reference in mixin target syntax: Lowner;name(desc)ret, Lowner;name:desc, name(desc) or name */
   private record MemberRef(String owner, String name, String desc) {
      static MemberRef parse(String target){
         if(target == null || target.isEmpty()) return null;
         String owner = null;
         String rest = target;
         if(rest.startsWith("L") && rest.contains(";")){
            owner = rest.substring(1, rest.indexOf(';'));
            rest = rest.substring(rest.indexOf(';') + 1);
         }
         String name = rest;
         String desc = null;
         int paren = rest.indexOf('(');
         int colon = rest.indexOf(':');
         if(paren >= 0){
            name = rest.substring(0, paren);
            desc = rest.substring(paren);
         }else if(colon >= 0){
            name = rest.substring(0, colon);
            desc = rest.substring(colon + 1);
         }
         return new MemberRef(owner, name, desc);
      }
      
      boolean matches(String owner, String name, String desc){
         return (this.owner == null || this.owner.equals(owner)) && this.name.equals(name) && (this.desc == null || this.desc.equals(desc));
      }
   }
   
   // ===================================
   //          READ / WRITE
   // ===================================
   
   public void write(Path dir) throws IOException{
      Files.createDirectories(dir);
      Path methodDir = dir.resolve(METHODS_DIR);
      if(Files.isDirectory(methodDir)){
         try(Stream<Path> old = Files.list(methodDir)){
            for(Path file : old.toList()) Files.delete(file);
         }
      }
      Files.createDirectories(methodDir);
      List<String> lines = new ArrayList<>();
      for(Map.Entry<String, List<String>> entry : entries.entrySet()){
         lines.add("[" + entry.getKey() + "]");
         lines.addAll(entry.getValue());
         lines.add("");
      }
      Files.write(dir.resolve(ENTRIES_FILE), lines, StandardCharsets.UTF_8);
      for(Map.Entry<String, List<String>> method : methods.entrySet()){
         Files.write(methodDir.resolve(method.getKey()), method.getValue(), StandardCharsets.UTF_8);
      }
   }
   
   public static MixinSnapshot read(Path dir) throws IOException{
      MixinSnapshot snapshot = new MixinSnapshot();
      Path entriesFile = dir.resolve(ENTRIES_FILE);
      if(!Files.isRegularFile(entriesFile)) return null;
      String key = null;
      for(String line : Files.readAllLines(entriesFile, StandardCharsets.UTF_8)){
         if(line.startsWith("[") && line.endsWith("]")){
            key = line.substring(1, line.length() - 1);
            snapshot.entries.put(key, new ArrayList<>());
         }else if(!line.isEmpty() && key != null){
            snapshot.entries.get(key).add(line);
         }
      }
      Path methodDir = dir.resolve(METHODS_DIR);
      if(Files.isDirectory(methodDir)){
         try(Stream<Path> files = Files.list(methodDir)){
            for(Path file : files.toList()){
               snapshot.methods.put(file.getFileName().toString(), Files.readAllLines(file, StandardCharsets.UTF_8));
            }
         }
      }
      return snapshot;
   }
   
   // ===================================
   //            COMPARE
   // ===================================
   
   /** Builds a readable report of everything that differs from the baseline. Empty when nothing changed. */
   public static List<String> compare(MixinSnapshot baseline, MixinSnapshot current){
      List<String> summary = new ArrayList<>();
      List<String> details = new ArrayList<>();
      
      Set<String> keys = new TreeSet<>(baseline.entries.keySet());
      keys.addAll(current.entries.keySet());
      for(String key : keys){
         List<String> before = baseline.entries.getOrDefault(key, List.of());
         List<String> after = current.entries.getOrDefault(key, List.of());
         if(withoutListingHashes(before).equals(withoutListingHashes(after))) continue; // Body changes are reported per method below
         // Same hooks but different matches: an injection point gained or lost call sites, or moved in or out of a branch
         boolean reachChanged = !before.isEmpty() && !after.isEmpty() && !reachability(before).equals(reachability(after));
         String marker = reachChanged ? "!! " : "   ";
         if(before.isEmpty()){
            summary.add(marker + "added    " + key);
         }else if(after.isEmpty()){
            summary.add(marker + "removed  " + key);
         }else{
            summary.add(marker + "changed  " + key + (reachChanged ? "  (injection point matches changed: " + String.join(" ", reachability(before)) + " -> " + String.join(" ", reachability(after)) + ")" : ""));
         }
         details.add("[" + key + "]");
         details.addAll(ListingDiff.diff(before, after, 100));
         details.add("");
      }
      
      Set<String> files = new TreeSet<>(baseline.methods.keySet());
      files.addAll(current.methods.keySet());
      for(String file : files){
         List<String> before = baseline.methods.get(file);
         List<String> after = current.methods.get(file);
         if(Objects.equals(before, after)) continue;
         if(before == null || after == null){
            continue; // Covered by the entry diff that added or removed the hook
         }
         summary.add("   method body changed  " + after.getFirst().substring(3));
         details.add("=== " + after.getFirst().substring(3) + " ===");
         details.addAll(ListingDiff.diff(before, after, 4));
         details.add("");
      }
      
      if(summary.isEmpty()) return List.of();
      List<String> report = new ArrayList<>();
      report.add("Mixin snapshot differs from src/test/mixin-baseline.");
      report.add("Lines marked !! are hooks whose injection point now matches different call sites, or moved between");
      report.add("running on every path (always) and running inside a branch (conditional). Check those first.");
      report.add("Review each change against the new vanilla code, fix any broken hooks, then accept the new state with:");
      report.add("    ./gradlew test -PupdateMixinBaseline");
      report.add("");
      report.addAll(summary);
      report.add("");
      report.add("---------------- details ----------------");
      report.addAll(details);
      return report;
   }
   
   private static List<String> withoutListingHashes(List<String> lines){
      List<String> result = new ArrayList<>();
      for(String line : lines){
         int index = line.lastIndexOf(" listing ");
         result.add(index < 0 ? line : line.substring(0, index));
      }
      return result;
   }
   
   private static List<String> reachability(List<String> lines){
      List<String> result = new ArrayList<>();
      for(String line : lines){
         int start = line.indexOf("match [");
         if(start < 0) start = line.indexOf("matches [");
         if(start < 0) continue;
         start = line.indexOf('[', start);
         result.add(line.substring(start, line.indexOf(']', start) + 1));
      }
      return result;
   }
}
