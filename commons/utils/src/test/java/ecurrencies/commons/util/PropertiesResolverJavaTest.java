package ecurrencies.commons.util;

import static ecurrencies.commons.util.PropertyResolver.containsKey;
import static ecurrencies.commons.util.PropertyResolver.getProperty;
import static ecurrencies.commons.util.PropertyResolver.getRequiredProperty;
import static ecurrencies.commons.util.PropertyResolver.getRequiredValue;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PropertiesResolverJavaTest {

    private Map<Integer, Object> map = new HashMap<>();

    @BeforeClass
    public void before() {
        map.put(1, "first");
        map.put(2, "second");
        map.put(3, "third");
        map.put(4, "fourth");
        map.put(100, 100);
    }

    @Test
    void test1() {
        for (int i = 1; i <= 4; i++) {
            assert containsKey(i, map);
        }
        assert containsKey(100, map);
        assert !containsKey(5, map);
        assert !containsKey(null, map);
    }

    @Test
    void test2() {
        assert getProperty(1, map) == "first";
        assert getProperty(80, map) == null;
        assert getProperty("8084", map) == null;
        assert getRequiredProperty(1, map) == "first";
    }

    @Test(expectedExceptions = IllegalStateException.class)
    void test3() {
        assert getRequiredProperty(800, map) == "I don't know";
    }

    @Test(expectedExceptions = IllegalStateException.class)
    void test4() {
        assert getRequiredProperty(800, map) == "I don't know";
    }

    @Test
    void test5() {
        assert getRequiredValue(1, map) instanceof String;
    }

    @Test
    void test6() {
        assert getRequiredProperty(4, map) instanceof String;
    }

    @Test(expectedExceptions = IllegalStateException.class)
    void test7() {
        assert getRequiredProperty(4, map, Map.class) instanceof Map;
    }

    @Test
    void test8() {
        assert getProperty(100, map, Integer.class, 90) == 100;
    }

    @Test
    void test9() {
        assert getProperty(200, map, Integer.class, 90) == 90;
    }

    // @Test
    // void test10() {
    // This test should compile only in groovy
    // assert getProperty(200, map, String.class, 90) == 90;
    // }

}
