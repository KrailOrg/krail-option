package uk.q3c.krail.option.mock;

import uk.q3c.krail.option.Option;
import uk.q3c.krail.option.bind.OptionModule;
import uk.q3c.krail.option.persist.OptionCache;
import uk.q3c.krail.option.persist.cache.DefaultOptionCache;
import uk.q3c.krail.persist.InMemory;

public class TestOptionModule extends OptionModule {

    public TestOptionModule() {
        activeSource(InMemory.class);
    }

    @Override
    protected void bindOption() {
        bind(Option.class).to(MockOption.class);
    }

    @Override
    protected void bindOptionCache() {
        bind(OptionCache.class).to(DefaultOptionCache.class);
    }


}
