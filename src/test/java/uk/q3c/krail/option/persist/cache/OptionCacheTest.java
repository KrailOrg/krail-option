package uk.q3c.krail.option.persist.cache;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import uk.q3c.krail.eventbus.mbassador.EventBusModule;
import uk.q3c.krail.option.Option;
import uk.q3c.krail.option.bind.OptionModule;
import uk.q3c.krail.persist.InMemory;
import uk.q3c.krail.persist.inmemory.InMemoryModule;
import uk.q3c.util.UtilModule;
import uk.q3c.util.guice.InjectorLocator;
import uk.q3c.util.guice.SerializationSupportModule;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.q3c.krail.option.persist.cache.AContext.key1;

/**
 * Created by David Sowerby on 19 Oct 2017
 */
public class OptionCacheTest {

    static Injector injector;
    private Option option;
    private AContext optionContext;

    @Before
    public void setup() {
        injector = Guice.createInjector(new OptionModule().activeSource(InMemory.class), new InMemoryModule().provideOptionDao(), new UtilModule(), new EventBusModule(), new SerializationSupportModule(), new LocatorModule());
        option = injector.getInstance(Option.class);
        optionContext = injector.getInstance(AContext.class);
    }

    @Test
    public void singletonCache() {
        assertThat(option.cache() == optionContext.optionInstance().cache()).isTrue();
    }

    /**
     * When a user option is set, its value remains in the cache and the bus handler responds
     *
     * @throws Exception
     */
    @Test
    public void valueFromCache() throws Exception {
        // when
        assertThat(option.getValueFromCache(key1, 0)).isNotPresent();

        // given
        option.set(key1, 999);

        // then
        assertThat(option.getValueFromCache(key1, 0)).isPresent();
        assertThat(option.getValueFromCache(key1, 0).get()).isEqualTo(999);
        Thread.sleep(200); // wait for async message
        assertThat(optionContext.optionValue).isEqualTo(999);
    }

    static class LocalInjectorLocator implements InjectorLocator {

        @NotNull
        @Override
        public Injector get() {
            return OptionCacheTest.injector;
        }

        @Override
        public void put(Injector injector) {
            OptionCacheTest.injector = injector;
        }
    }

    static class LocatorModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(InjectorLocator.class).toInstance(new LocalInjectorLocator());
        }
    }
}
