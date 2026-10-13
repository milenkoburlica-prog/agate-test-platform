package at.co.svc.aga.transformator.utils;

import java.nio.file.*;
import java.io.*;
import java.util.*;
import org.yaml.snakeyaml.Yaml;

/** Current export dependency closure. No global inventory or historical manifests. */
public final class UsedYamlGraph {
    private UsedYamlGraph() {}
    public record Result(Set<Path> used, List<String> unresolved) {
        public Result { used=Set.copyOf(used); unresolved=List.copyOf(unresolved); }
        public boolean complete() { return unresolved.isEmpty(); }
    }
    public static Result scan(Path applicationRoot, Collection<Path> roots) {
        Path root=applicationRoot.toAbsolutePath().normalize();
        Set<Path> used=new LinkedHashSet<>(); List<String> unresolved=new ArrayList<>();
        Deque<Path> pending=new ArrayDeque<>(); roots.forEach(p->pending.add(p.toAbsolutePath().normalize()));
        if(pending.isEmpty())unresolved.add("No root YAML found for current export");
        while(!pending.isEmpty()) {
            Path file=pending.removeFirst();
            if(!file.startsWith(root)){unresolved.add("Dependency outside application root: "+file);continue;}
            if(!used.add(file))continue; // prevents repeated scans and cycles
            if(!Files.isRegularFile(file)){unresolved.add("Missing YAML: "+file);continue;}
            try(Reader reader=Files.newBufferedReader(file)) {
                Object document=new Yaml().load(reader);
                if(!(document instanceof Map<?,?>) && !(document instanceof List<?>))throw new IOException("Root YAML is empty or has no step structure");
                walk(document,root,file,pending,unresolved,Collections.newSetFromMap(new IdentityHashMap<>()));
            } catch(Exception e) { unresolved.add("Cannot inspect "+file+": "+e.getMessage()); }
        }
        return new Result(used,unresolved);
    }
    private static void walk(Object node,Path root,Path source,Deque<Path> pending,List<String> unresolved,Set<Object> seen) {
        if(!(node instanceof Map<?,?>) && !(node instanceof List<?>))return;
        if(!seen.add(node))return;
        if(node instanceof Map<?,?> map) {
            if("CALL".equalsIgnoreCase(String.valueOf(map.get("type")))) {
                Object value=map.get("command");
                if(!(value instanceof String command) || command.isBlank())unresolved.add("CALL without command in "+source);
                else if(command.contains("{") || command.contains("}"))unresolved.add("Dynamic CALL in "+source+": "+command);
                else if(command.startsWith("reusable.")) {
                    String key=command.substring("reusable.".length());
                    if(key.isBlank() || key.contains("/") || key.contains("\\") || key.contains(".."))unresolved.add("Invalid reusable CALL in "+source+": "+command);
                    else {
                        Path yaml=root.resolve("reusable").resolve(key.replace('.',File.separatorChar)+".yaml").normalize();
                        pending.add(yaml);
                    }
                } else unresolved.add("Unsupported CALL reference in "+source+": "+command);
            }
            for(Object child:map.values())walk(child,root,source,pending,unresolved,seen);
        } else for(Object child:(List<?>)node)walk(child,root,source,pending,unresolved,seen);
    }
    public static List<Path> roots(Path applicationRoot,String baseName) {
        List<Path> result=new ArrayList<>();
        String stripped=baseName;
        boolean changed;
        do {changed=false;for(String prefix:List.of("TCD_","T_","TS_","Instance_"))
            if(stripped.regionMatches(true,0,prefix,0,prefix.length())){stripped=stripped.substring(prefix.length());changed=true;break;}
        }while(changed);
        for(Path directory:List.of(applicationRoot,applicationRoot.resolve("template")))
            for(String name:new LinkedHashSet<>(List.of(baseName,stripped)))
                for(String extension:List.of(".yaml",".yml")) {
                    Path candidate=directory.resolve(name+extension);
                    if(Files.isRegularFile(candidate))result.add(candidate);
                }
        return result;
    }
}
