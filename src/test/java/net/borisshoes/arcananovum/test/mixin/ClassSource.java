package net.borisshoes.arcananovum.test.mixin;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reads raw class files straight from the classpath jars and folders, without class loading or mixin transformation.
 * An override jar (another Minecraft version) can be put in front of everything else.
 */
public class ClassSource implements AutoCloseable {
   
   private final List<Path> entries = new ArrayList<>();
   private final Map<Path, ZipFile> zips = new HashMap<>();
   private final Map<String, Optional<ClassNode>> cache = new HashMap<>();
   
   public ClassSource(List<Path> entries){
      this.entries.addAll(entries);
   }
   
   public static ClassSource fromClassPath(Path overrideJar){
      List<Path> entries = new ArrayList<>();
      if(overrideJar != null) entries.add(overrideJar);
      for(String entry : System.getProperty("java.class.path").split(File.pathSeparator)){
         if(entry.isBlank()) continue;
         Path path = Path.of(entry);
         if(Files.exists(path)) entries.add(path);
      }
      return new ClassSource(entries);
   }
   
   public byte[] readResource(String name){
      for(Path entry : entries){
         try{
            if(Files.isDirectory(entry)){
               Path file = entry.resolve(name);
               if(Files.isRegularFile(file)) return Files.readAllBytes(file);
            }else{
               ZipFile zip = zips.computeIfAbsent(entry, ClassSource::openZip);
               if(zip == null) continue;
               ZipEntry zipEntry = zip.getEntry(name);
               if(zipEntry == null) continue;
               try(InputStream in = zip.getInputStream(zipEntry)){
                  return in.readAllBytes();
               }
            }
         }catch(IOException e){
            throw new RuntimeException("Could not read " + name + " from " + entry, e);
         }
      }
      return null;
   }
   
   /** Returns the class with the given internal name (net/minecraft/...), or null if it is not on the classpath */
   public ClassNode getClass(String internalName){
      return cache.computeIfAbsent(internalName, name -> {
         byte[] bytes = readResource(name + ".class");
         if(bytes == null) return Optional.empty();
         ClassNode node = new ClassNode();
         new ClassReader(bytes).accept(node, 0);
         return Optional.of(node);
      }).orElse(null);
   }
   
   private static ZipFile openZip(Path path){
      try{
         return new ZipFile(path.toFile());
      }catch(IOException e){
         return null;
      }
   }
   
   @Override
   public void close(){
      for(ZipFile zip : zips.values()){
         try{
            if(zip != null) zip.close();
         }catch(IOException ignored){
         }
      }
      zips.clear();
   }
}
