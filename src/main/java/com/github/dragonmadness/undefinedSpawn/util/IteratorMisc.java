package com.github.dragonmadness.undefinedSpawn.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IteratorMisc {

    public static <T> List<T> asList(Iterator<T> iterator) {
        List<T> out = new ArrayList<>();
        while (iterator.hasNext()) {
            out.add(iterator.next());
        }
        return out;
    }

}
