package com.fossil.vn.common;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MemoryCache {
    private Map<Integer, SoftReference<MapCached>> cache = Collections.synchronizedMap(new HashMap<Integer, SoftReference<MapCached>>());
    public MapCached get(int id) {
        if (!cache.containsKey(id))
            return null;
        SoftReference<MapCached> ref = cache.get(id);
        return ref.get();
    }

    public void put(int id, MapCached bitmap) {
        cache.put(id, new SoftReference<MapCached>(bitmap));
    }

    public void clear() {
        cache.clear();
    }
}
