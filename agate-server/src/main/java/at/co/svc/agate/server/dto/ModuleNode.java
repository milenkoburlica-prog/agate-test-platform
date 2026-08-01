package at.co.svc.agate.server.dto;

import java.util.ArrayList;
import java.util.List;

//Folder čvor za explorer
public class ModuleNode {
 public String name;
 public String fullName;
 public String type; // "folder" ili "module"
 public List<ModuleNode> children;

 public ModuleNode(String name, String fullName, String type) {
     this.name = name;
     this.type = type;
     this.children = new ArrayList<>();
 }

 public String getName() {
    return name;
 }

 public void setName(String name) {
    this.name = name;
 }

 public String getFullName() {
    return fullName;
 }

 public void setFullName(String fullName) {
    this.fullName = fullName;
 }

 public String getType() {
    return type;
 }

 public void setType(String type) {
    this.type = type;
 }

 public List<ModuleNode> getChildren() {
    return children;
 }

 public void setChildren(List<ModuleNode> children) {
    this.children = children;
 }
 
 
}

