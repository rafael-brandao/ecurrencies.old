package ecurrencies.commons.util

import static java.lang.String.format

abstract class PropertyResolver {

    private static final String KEY_NOT_FOUND_FORMAT = "Key '%s' was not found."
    private static final String VALUE_CAST_EXCEPTION_FORMAT = "Property value associated with key '%s' is not an instance of %s."

    static boolean containsKey(Object key, Map<?, ?> properties) {
        if(properties[key])
            true
        else
            false
    }

    static String getProperty(final Object key, final Map<?, ?> properties) {
        getProperty(key, properties, String.class)
    }

    static String getProperty(Object key, Map<?, ?> properties, String defaultProperty) {
        getProperty(key, properties, String.class, defaultProperty)
    }

    static String getRequiredProperty(Object key, Map<?, ?> properties) {
        getRequiredProperty(key, properties, String.class)
    }

    static <T> T getProperty(Object key, Map<?, ?> properties, Class<T> requiredType) {
        try {
            requiredType.cast(property(key, properties))
        } catch (ClassCastException e) {
            throw new IllegalStateException(format(VALUE_CAST_EXCEPTION_FORMAT, key, requiredType.name), e)
        }
    }

    static <T> T getProperty(Object key, Map<?, ?> properties, Class<T> requiredType, T defaultProperty) {
        T value = getProperty(key, properties, requiredType)
        if (value)
            value
        else
            defaultProperty
    }

    static <T> T getRequiredProperty(Object key, Map<?, ?> properties, Class<T> requiredType) {
        try {
            requiredType.cast(property(key, properties, true))
        } catch (ClassCastException e) {
            throw new IllegalStateException(format(VALUE_CAST_EXCEPTION_FORMAT, key, requiredType.name), e)
        }
    }

    static <K, V> V getValue(K key, Map<K, V> properties) {
        value(key, properties)
    }

    static <K, V> V getRequiredValue(K key, Map<K, V> properties) {
        value(key, properties, true)
    }

    private static <K, V> V value(K key, Map<K, V> properties, required = false) {
        (V) property(key, properties, required)
    }

    private static def property(key, properties, required = false) {
        def value = properties[key]
        if (value || !required)
            value
        else
            throw new IllegalStateException(format(KEY_NOT_FOUND_FORMAT, key))
    }
}
