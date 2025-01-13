package com.oakmac.standardclojurestyle;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GenClassKeys {
    public static final Set<String> keys = new HashSet<>(Arrays.asList(
        "name",
        "extends",
        "implements",
        "init",
        "constructors",
        "post-init",
        "methods",
        "main",
        "factory",
        "state",
        "exposes",
        "exposes-methods",
        "prefix",
        "impl-ns",
        "load-impl-ns"
    ));

    public static boolean contains(String key) {
        return keys.contains(key);
    }
}