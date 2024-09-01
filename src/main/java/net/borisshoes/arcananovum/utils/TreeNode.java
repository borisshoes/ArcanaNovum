package net.borisshoes.arcananovum.utils;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TreeNode<T> {
   private final T data;
   private Set<TreeNode<T>> children;
   private TreeNode<T> parent;
   
   public TreeNode(T data, Set<TreeNode<T>> children, @Nullable TreeNode<T> parent){
      this.data = data;
      this.children = children;
      this.parent = parent;
   }
   
   public int getDepth(){
      if(parent == null){
         return 0;
      }else{
         return parent.getDepth()+1;
      }
   }
   
   public boolean isLeaf(){
      return children.isEmpty();
   }
   
   public boolean isRoot(){
      return parent == null;
   }
   
   public Set<TreeNode<T>> getChildren(){
      return children;
   }
   
   public void addChild(TreeNode<T> child){
      this.children.add(child);
   }
   
   public void removeChild(TreeNode<T> child){
      this.children.remove(child);
   }
   
   public TreeNode<?> getParent(){
      return parent;
   }
   
   public void setParent(TreeNode<T> parent){
      this.parent = parent;
   }
   
   public T getData(){
      return data;
   }
   
   public int countAllDescendants(){
      int count = 0;
      List<TreeNode<?>> nodes = new ArrayList<>(children);
      
      Iterator<TreeNode<?>> iter = nodes.iterator();
      while(iter.hasNext()){
         nodes.addAll(iter.next().children);
         iter.remove();
         count++;
      }
      
      return count;
   }
   
   public List<TreeNode<T>> getAllLeaves(List<TreeNode<T>> list){
      if(isLeaf()){
         list.add(this);
      }else{
         for(TreeNode<T> child : this.children){
            child.getAllLeaves(list);
         }
      }
      return list;
   }
   
   public List<TreeNode<T>> getAllNodes(List<TreeNode<T>> list){
      list.add(this);
      for(TreeNode<T> child : this.children){
         child.getAllNodes(list);
      }
      
      return list;
   }
}
