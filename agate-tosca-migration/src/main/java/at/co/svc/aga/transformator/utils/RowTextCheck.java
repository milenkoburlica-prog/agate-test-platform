package at.co.svc.aga.transformator.utils;
import java.util.*;
public class RowTextCheck {
 public static void main(String[] args) {
  Map<String,Object> table=new LinkedHashMap<>(Map.of("businessType","Table","moduleAttributeSurrogate","t","properties",Map.of("Tag","TABLE","Id","patient"),"actionMode","Select","value","{NULL}"));
  Map<String,Object> row=new LinkedHashMap<>(Map.of("businessType","Row","moduleAttributeSurrogate","r","explicitName","","value","Patient:","actionMode","517"));
  Map<String,Object> cell=new LinkedHashMap<>(Map.of("businessType","Cell","explicitName","#2","valueParents",List.of(table,row),"attributeParents",List.of(table,row)));
  check(ProcessGuiStep.target(cell),Map.of("rowText","Patient:","cell",2));
  row.put("value","Patient:*");check(ProcessGuiStep.target(cell),Map.of("rowTextGlob","Patient:*","cell",2));
  row.put("explicitName","#3");row.put("value","");check(ProcessGuiStep.target(cell),Map.of("row",3,"cell",2));
  row.put("explicitName","");
  try {ProcessGuiStep.target(cell);throw new AssertionError("empty row accepted");} catch(IllegalArgumentException expected) {}
  System.out.println("PASS: exact text, glob text, numeric row, empty row rejection (4 checks)");
 }
 static void check(Map<String,Object> actual,Map<String,Object> expected) {if(!expected.equals(actual.get("tableCell")))throw new AssertionError(actual.toString());}
}
