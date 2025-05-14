package com.github.davidmoten.chained.processor;

import java.util.Map;
import java.util.TreeMap;

public final class Imports {

    private Map<String, String> imports = new TreeMap<>();

    public String add(Class<?> cls) {
        return add(cls.getCanonicalName());
    }

    public String add(String className) {
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
            if (!className.equals(simpleName)) {
                b.append("import ").append(className).append(";\n");
            }
        }
        return b.toString();
    }
}
