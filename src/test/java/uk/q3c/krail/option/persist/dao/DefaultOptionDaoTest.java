package uk.q3c.krail.option.persist.dao;

import org.junit.Before;
import org.junit.Test;
import uk.q3c.krail.option.OptionKeyException;
import uk.q3c.krail.option.RankOption;
import uk.q3c.krail.option.hierarchy.DefaultUserHierarchy;
import uk.q3c.krail.option.persist.OptionCacheKey;
import uk.q3c.krail.option.persist.OptionDao;
import uk.q3c.krail.option.persist.OptionDaoDelegate;
import uk.q3c.krail.option.persist.OptionSource;
import uk.q3c.krail.option.persist.cache.AContext;
import uk.q3c.krail.persist.inmemory.InMemoryOptionStore;
import uk.q3c.krail.persist.inmemory.dao.InMemoryOptionDaoDelegate;
import uk.q3c.krail.persist.inmemory.store.DefaultInMemoryOptionStore;
import uk.q3c.util.clazz.DefaultClassNameUtils;
import uk.q3c.util.data.DataConverter;
import uk.q3c.util.data.DataItemConverter;
import uk.q3c.util.data.DefaultDataConverter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * OptionDao tested with InMemory store
 * <p>
 * Created by David Sowerby on 21 Oct 2017
 */
public class DefaultOptionDaoTest {

    OptionDao dao;
    OptionDaoDelegate delegate;
    private OptionCacheKey<Integer> cacheKey0;
    private OptionCacheKey<Integer> cacheKey1;
    private OptionCacheKey<Integer> cacheKeyHigh;
    private OptionCacheKey<Integer> cacheKeyLow;
    private OptionCacheKey<Integer> cacheKeyNonLow;
    private OptionCacheKey<Integer> cacheKeyNonSpecific;
    private DataConverter dataConverter;
    private OptionSource optionSource;
    private Map<Class<?>, DataItemConverter> customConverters;
    private DefaultClassNameUtils classNameUtils;
    private InMemoryOptionStore store;


    @Before
    public void setup() {
        cacheKey0 = new OptionCacheKey<>(new DefaultUserHierarchy(), RankOption.SPECIFIC_RANK, 0, AContext.key1);
        cacheKey1 = new OptionCacheKey<>(new DefaultUserHierarchy(), RankOption.SPECIFIC_RANK, 1, AContext.key1);
        cacheKeyHigh = new OptionCacheKey<>(new DefaultUserHierarchy(), RankOption.HIGHEST_RANK, AContext.key1);
        cacheKeyLow = new OptionCacheKey<>(new DefaultUserHierarchy(), RankOption.LOWEST_RANK, AContext.key1);
        cacheKeyNonLow = new OptionCacheKey<>(new DefaultUserHierarchy(), RankOption.LOWEST_RANK, AContext.key2);
        cacheKeyNonSpecific = new OptionCacheKey<>(new DefaultUserHierarchy(), RankOption.SPECIFIC_RANK, AContext.key2);

        store = new DefaultInMemoryOptionStore();
        delegate = new InMemoryOptionDaoDelegate(store);
        optionSource = mock(OptionSource.class);
        when(optionSource.getActiveDao()).thenReturn(delegate);


        customConverters = new HashMap<>();
        classNameUtils = new DefaultClassNameUtils();
        dataConverter = new DefaultDataConverter(customConverters, classNameUtils);

        dao = new DefaultOptionDao(dataConverter, optionSource);


    }

    @Test
    public void readAndWriteSpecificRank() throws Exception {
        // when
        dao.write(cacheKey0, Optional.of(7));

        // then
        assertThat(dao.getValue(cacheKey0)).isPresent();
        assertThat(dao.getValue(cacheKey0).get()).isEqualTo(7);
        assertThat(dao.getValue(cacheKeyNonSpecific)).isNotPresent();

        // when
        dao.deleteValue(cacheKey0);

        // then
        assertThat(dao.getValue(cacheKey0)).isNotPresent();
    }

    @Test
    public void readAndWriteHighestAndLowest() throws Exception {
        // when
        dao.write(cacheKey0, Optional.of(3));
        dao.write(cacheKey1, Optional.of(1));

        // then
        assertThat(dao.getValue(cacheKeyHigh).get()).isEqualTo(3);
        assertThat(dao.getValue(cacheKeyLow).get()).isEqualTo(1);
        assertThat(dao.getValue(cacheKeyNonLow)).isNotPresent();

        // when
        dao.clear();

        // then
        assertThat(dao.count()).isEqualTo(0);
    }

    @Test(expected = OptionKeyException.class)
    public void exceptionWhenWriteNonSpecific() throws Exception {
        // when
        dao.write(cacheKeyHigh, Optional.of(3));
    }

    @Test
    public void connectionUrl() throws Exception {
        assertThat(dao.connectionUrl()).isEqualTo("In Memory Store");
    }
}