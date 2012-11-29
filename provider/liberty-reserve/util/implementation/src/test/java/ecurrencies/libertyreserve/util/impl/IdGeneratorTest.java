package ecurrencies.libertyreserve.util.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import ecurrencies.libertyreserve.util.IdGenerator;

@ContextConfiguration(classes = UtilConfiguration.class)
public class IdGeneratorTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private IdGenerator idGenerator1;

    @Autowired
    private IdGenerator idGenerator2;

    @Test
    public void assertNotNull() {
        assertThat(idGenerator1, is(notNullValue()));
    }

    @Test
    public void ensureSingletonScope() {
        assertThat(idGenerator1 == idGenerator2, is(true));
        assertThat(idGenerator1, is(idGenerator2));
    }

    @Test
    public void testOutputLenght() {
        assertThat(idGenerator1.createId().length(), is(20));
    }

}
