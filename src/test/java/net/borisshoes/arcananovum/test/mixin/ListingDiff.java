package net.borisshoes.arcananovum.test.mixin;

import java.util.ArrayList;
import java.util.List;

/** A small line diff (longest common subsequence) for printing what changed in a bytecode listing */
public class ListingDiff {
   
   private static final long MAX_CELLS = 25_000_000L;
   
   public static List<String> diff(List<String> before, List<String> after, int context){
      int prefix = 0;
      while(prefix < before.size() && prefix < after.size() && before.get(prefix).equals(after.get(prefix))) prefix++;
      int suffix = 0;
      while(suffix < before.size() - prefix && suffix < after.size() - prefix
            && before.get(before.size() - 1 - suffix).equals(after.get(after.size() - 1 - suffix))) suffix++;
      
      List<String> a = before.subList(prefix, before.size() - suffix);
      List<String> b = after.subList(prefix, after.size() - suffix);
      List<String> ops = new ArrayList<>(); // each line prefixed with ' ', '-' or '+'
      for(int i = 0; i < prefix; i++) ops.add(" " + before.get(i));
      
      if((long) a.size() * b.size() > MAX_CELLS){
         for(String line : a) ops.add("-" + line);
         for(String line : b) ops.add("+" + line);
      }else{
         int[][] lcs = new int[a.size() + 1][b.size() + 1];
         for(int i = a.size() - 1; i >= 0; i--){
            for(int j = b.size() - 1; j >= 0; j--){
               lcs[i][j] = a.get(i).equals(b.get(j)) ? lcs[i + 1][j + 1] + 1 : Math.max(lcs[i + 1][j], lcs[i][j + 1]);
            }
         }
         int i = 0, j = 0;
         while(i < a.size() || j < b.size()){
            if(i < a.size() && j < b.size() && a.get(i).equals(b.get(j))){
               ops.add(" " + a.get(i));
               i++;
               j++;
            }else if(j < b.size() && (i == a.size() || lcs[i][j + 1] >= lcs[i + 1][j])){
               ops.add("+" + b.get(j));
               j++;
            }else{
               ops.add("-" + a.get(i));
               i++;
            }
         }
      }
      for(int i = before.size() - suffix; i < before.size(); i++) ops.add(" " + before.get(i));
      
      // Only keep changed lines and their surrounding context
      boolean[] keep = new boolean[ops.size()];
      for(int i = 0; i < ops.size(); i++){
         if(ops.get(i).charAt(0) != ' '){
            for(int k = Math.max(0, i - context); k <= Math.min(ops.size() - 1, i + context); k++) keep[k] = true;
         }
      }
      List<String> out = new ArrayList<>();
      boolean skipping = false;
      for(int i = 0; i < ops.size(); i++){
         if(keep[i]){
            out.add(ops.get(i));
            skipping = false;
         }else if(!skipping){
            out.add("@@ ...");
            skipping = true;
         }
      }
      return out;
   }
}
