package com.github.davidmoten.chained.processor;

import java.util.Map;
import java.util.TreeMap;

public final class Imports {

    private final String ownerClassName;
    private final Map<String, String> imports = new TreeMap<>();

    public Imports(String ownerClassName) {
        this.ownerClassName = ownerClassName;
    }
    
    public String add(Class<?> cls) {
        return add(cls.getCanonicalName());
    }

    public String add(String className) {
        if (className.contains("<")) {
            StringBuilder b = new StringBuilder();
            StringBuilder cls = new StringBuilder();
            for (int i = 0; i < className.length(); i++) {
                char c = className.charAt(i);
                if (c == '<' || c == '>' || c == ',' || c == '?' || c == ' ') {
                    b.append(addNoGenerics(cls.toString()));
                    b.append(c);
                    cls.setLength(0);
                } else {
                    cls.append(c);
                }
            }
            if (cls.length() > 0) {
                b.append(addNoGenerics(cls.toString()));
            }
            return b.toString();
        } else {
            return addNoGenerics(className);
        }
    }

    private String addNoGenerics(String className) {
        if (imports.containsKey(className)) {
            return imports.get(className);
        } else {
            String simpleName = Util.simpleClassName(className);
            if (imports.containsValue(simpleName)) {
                imports.put(className, className);
                return className;
            } else {
                imports.put(className, simpleName);
                return simpleName;
            }
        }
    }
    
    public String toCode() {
        StringBuilder b = new StringBuilder();
        for (String className : imports.keySet()) {
            String simpleName = imports.get(className);
            if (!className.equals(simpleName) && !Util.pkg(className).equals(Util.pkg(ownerClassName))) {
                b.append("import ").append(className).append(";\n");
            }
        }
        return b.toString();
    }
}
