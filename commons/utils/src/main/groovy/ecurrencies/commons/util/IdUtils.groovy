package ecurrencies.commons.util

import static java.lang.String.format

import java.util.concurrent.atomic.AtomicInteger

abstract class IdUtils {

    static final def COUNTER_MAP = [:]

    static String generateId(Class clazz) {
        generateId(clazz.getSimpleName())
    }

    static String generateId(String prefix) {
        def hash = prefix.hashCode()
        synchronized (COUNTER_MAP) {
            if (!COUNTER_MAP.hash) {
                COUNTER_MAP.hash = new AtomicInteger()
            }
        }
        format('%s-%02d',prefix,COUNTER_MAP.hash.incrementAndGet())
    }
}
