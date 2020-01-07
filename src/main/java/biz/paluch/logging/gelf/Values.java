package biz.paluch.logging.gelf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Value object to abstract multiple values mapped by a {@link String} key.
 *
 * @author Mark Paluch
 * @since 28.02.14 09:50
 */
public class Values {

    private Map<String, Object> keyValueMap = new HashMap<>();

    public Values() {
    }

    public Values(String name, Object value) {
        if (name != null && value != null) {
            keyValueMap.put(name, value);
        }
    }

    public boolean hasValues() {
        return ! keyValueMap.isEmpty();
    }

    public int size() {
        return keyValueMap.size();
    }

    public Set<String> getEntryNames() {
        return Collections.unmodifiableSet(keyValueMap.keySet());
    }

    public void setValue(String key, Object value) {
        keyValueMap.put(key, value);
    }

    @SuppressWarnings("unchecked")
	public <T> T getValue(String key) {
        return (T) keyValueMap.get(key);
    }
}
