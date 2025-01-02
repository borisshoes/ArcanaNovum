package net.borisshoes.arcananovum.utils;

import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {
   
   public static String camelToSnake(String str){
      return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase(Locale.ROOT);
   }
   
   public static MutableText withColor(MutableText text, int color){
      return text.setStyle(text.getStyle().withColor(color));
   }
   
   public static MutableText removeItalics(Text text){
      return removeItalics(Text.literal("").append(text));
   }
   
   public static MutableText removeItalics(MutableText text){
      Style parentStyle = Style.EMPTY.withColor(Formatting.DARK_PURPLE).withItalic(false).withBold(false).withUnderline(false).withObfuscated(false).withStrikethrough(false);
      return text.setStyle(text.getStyle().withParent(parentStyle));
   }
   
   public static MutableText parseString(String input){
      ArrayList<String> matchList = new ArrayList<>();
      MutableText text = Text.literal("");
      Pattern pattern = Pattern.compile("\\[(.*?)\\]\\(([1234567890abcdef]?[klmno]*)\\)");
      Matcher matcher = pattern.matcher(input);
      int lastEnd = 0;
      
      while (matcher.find()){
         if(!input.substring(lastEnd, matcher.start()).isEmpty())
            matchList.add("["+input.substring(lastEnd,matcher.start())+"](f)");
         matchList.add(matcher.group());
         lastEnd = matcher.end();
      }
      // Add the remaining part of the string
      if(!input.substring(lastEnd).isEmpty())
         matchList.add("["+input.substring(lastEnd)+"](f)");
      
      for (String str : matchList){
         matcher = pattern.matcher(str);
         
         // Find and print all matches
         while (matcher.find()){
            String content = matcher.group(1);
            String formatCode = matcher.group(2);
            
            text.append(Text.literal(content).formatted(parseFormatCode(formatCode)));
         }
      }
      
      return text;
   }
   
   private static Formatting[] parseFormatCode(String code){
      ArrayList<Formatting> formatting = new ArrayList<>();
      
      for(int i = 0; i < code.length(); i++){
         char style = code.charAt(i);
         
         Formatting f = Formatting.byCode(style);
         if(f != null){
            formatting.add(f);
         }
      }
      
      return formatting.toArray(new Formatting[0]);
   }
   
    public static String textToString(Text text){
       StringBuilder str = new StringBuilder();
       Style parentStyle = text.getStyle();
       
       char parentColor = 'f';
       boolean parentItalic = parentStyle.isItalic();
       boolean parentBold = parentStyle.isBold();
       boolean parentUnderlined = parentStyle.isUnderlined();
       boolean parentStrikethrough = parentStyle.isStrikethrough();
       boolean parentObfuscated = parentStyle.isObfuscated();
       
       TextColor parentTextColor = parentStyle.getColor();
       if(parentTextColor != null){
          Formatting formatting = Formatting.byName(parentTextColor.getName());
          if(formatting != null){
             parentColor = formatting.getCode();
          }
       }
       
       TextContent parentContent = text.getContent();
       if(parentContent instanceof PlainTextContent plainTextContent){
          String contentString = plainTextContent.string();
          
          if(!contentString.isEmpty()){
             String formatCodes = booleansToFormatCodes(parentItalic, parentBold, parentUnderlined, parentStrikethrough, parentObfuscated);
             str.append("[").append(contentString).append("](").append(parentColor).append(formatCodes).append(")");
          }
       }
       
       for(Text sibling : text.getSiblings()){
          TextContent siblingContent = sibling.getContent();
          
          if(siblingContent instanceof PlainTextContent plainTextContent){
             String contentString = plainTextContent.string();
             
             if(!contentString.isEmpty()){
                Style siblingStyle = sibling.getStyle();
                
                char color = parentColor;
                TextColor siblingColor = siblingStyle.getColor();
                if(siblingColor != null){
                   Formatting formatting = Formatting.byName(siblingColor.getName());
                   if(formatting != null){
                      color = formatting.getCode();
                   }
                }
                String formatCodes = booleansToFormatCodes(
                      siblingStyle.isItalic() || parentItalic,
                      siblingStyle.isBold() || parentBold,
                      siblingStyle.isUnderlined() || parentUnderlined,
                      siblingStyle.isStrikethrough() || parentStrikethrough,
                      siblingStyle.isObfuscated() || parentObfuscated);
                
                str.append("[").append(contentString).append("](").append(color).append(formatCodes).append(")");
             }
          }
       }
       
      return str.toString();
    }
   
   public static String textToCode(Text text){
      Style parentStyle = text.getStyle();
      ArrayList<String> codes = new ArrayList<>();
      
      Formatting parentColor = Formatting.WHITE;
      boolean parentItalic = parentStyle.isItalic();
      boolean parentBold = parentStyle.isBold();
      boolean parentUnderlined = parentStyle.isUnderlined();
      boolean parentStrikethrough = parentStyle.isStrikethrough();
      boolean parentObfuscated = parentStyle.isObfuscated();
      
      TextColor parentTextColor = parentStyle.getColor();
      if(parentTextColor != null){
         Formatting formatting = Formatting.byName(parentTextColor.getName());
         if(formatting != null){
            parentColor = formatting;
         }
      }
      
      TextContent parentContent = text.getContent();
      if(parentContent instanceof PlainTextContent plainTextContent){
         String contentString = plainTextContent.string();
         
         if(!contentString.isEmpty()){
            codes.add(textToCodeHelper(contentString,parentColor.getName(),parentItalic, parentBold, parentUnderlined, parentStrikethrough, parentObfuscated));
         }
      }
      
      for(Text sibling : text.getSiblings()){
         TextContent siblingContent = sibling.getContent();
         
         if(siblingContent instanceof PlainTextContent plainTextContent){
            String contentString = plainTextContent.string();
            
            if(!contentString.isEmpty()){
               Style siblingStyle = sibling.getStyle();
               
               Formatting color = parentColor;
               TextColor siblingColor = siblingStyle.getColor();
               if(siblingColor != null){
                  Formatting formatting = Formatting.byName(siblingColor.getName());
                  if(formatting != null){
                     color = formatting;
                  }
               }
               codes.add(textToCodeHelper(contentString,color.getName(),
                     siblingStyle.isItalic() || parentItalic,
                     siblingStyle.isBold() || parentBold,
                     siblingStyle.isUnderlined() || parentUnderlined,
                     siblingStyle.isStrikethrough() || parentStrikethrough,
                     siblingStyle.isObfuscated() || parentObfuscated));
            }
         }
      }
      
      if(codes.isEmpty()){
         return "Text.literal(\"\");";
      }else if(codes.size() == 1){
         return codes.getFirst();
      }else{
         String finalCode = "Text.literal(\"\")";
         for(String code : codes){
            finalCode += "\n\t.append("+code.replace(";","")+")";
         }
         return finalCode + ";";
      }
   }
   
   private static String textToCodeHelper(String content, String color, boolean italic, boolean bold, boolean underlined, boolean strikethrough, boolean obfuscated){
      String code = "Text.literal(\""+content+"\")";
      
      code += ".formatted(";
      code += "Formatting."+color.toUpperCase(Locale.ROOT)+",";
      if(italic){
         code += "Formatting.ITALIC,";
      }
      if(bold){
         code += "Formatting.BOLD,";
      }
      if(underlined){
         code += "Formatting.UNDERLINE,";
      }
      if(strikethrough){
         code += "Formatting.STRIKETHROUGH,";
      }
      if(obfuscated){
         code += "Formatting.OBFUSCATED,";
      }
      code = code.substring(0,code.length()-1) + ")";
      code += ";";
      return code;
   }
    
    private static String booleansToFormatCodes(boolean italic, boolean bold, boolean underlined, boolean strikethrough, boolean obfuscated){
      String str = "";
      if(italic){
         str += 'o';
      }
       if(bold){
          str += 'l';
       }
       if(underlined){
          str += 'n';
       }
       if(strikethrough){
          str += 'm';
       }
       if(obfuscated){
          str += 'k';
       }
      
      return str;
    }
}
