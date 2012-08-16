package sketchit.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class Multimap<K,V> {
    private Map<K,List<V>> values = new HashMap<K, List<V>>();

    public void add(K key, V value) {
        List<V> list = values.get(key);
        if(list==null) {
            list = new ArrayList<V>();
            values.put(key, list);
        }
        list.add(value);
    }

    public Set<Map.Entry<K,List<V>>> entrySet() {
        return values.entrySet();
    }
}
