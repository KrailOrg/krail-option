package uk.q3c.krail.persist.inmemory.store;

import org.junit.Before;
import org.junit.Test;
import uk.q3c.krail.option.RankOption;
import uk.q3c.krail.option.hierarchy.DefaultUserHierarchy;
import uk.q3c.krail.option.persist.OptionCacheKey;
import uk.q3c.krail.option.persist.OptionId;
import uk.q3c.krail.option.persist.cache.AContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by David Sowerby on 21 Oct 2017
 */
public class DefaultInMemoryOptionStoreTest {

    DefaultInMemoryOptionStore store;

    private OptionCacheKey<Integer> cacheKey1;
    private OptionCacheKey<Integer> cacheKey2;
    private OptionCacheKey<Integer> cacheKey3;
    private OptionId id1;
    private OptionId id2;
    private OptionId id3;

    @Before
    public void setup() {
        cacheKey1 = new OptionCacheKey<>(new DefaultUserHierarchy(), RankOption.SPECIFIC_RANK, AContext.key1);
        cacheKey2 = new OptionCacheKey<>(new DefaultUserHierarchy(), RankOption.SPECIFIC_RANK, AContext.key1);
        cacheKey3 = new OptionCacheKey<>(new DefaultUserHierarchy(), RankOption.SPECIFIC_RANK, AContext.key2);

        id1 = new OptionId(cacheKey1);
        id2 = new OptionId(cacheKey2);
        id3 = new OptionId(cacheKey3);

        store = new DefaultInMemoryOptionStore();

    }


    @Test
    public void addAndRetrieve() throws Exception {

        //when
        store.add(id1, "a");
        store.add(id3, "c");

        // then
        assertThat(store.getEntity(id1).get().getOptionId()).isEqualTo(id1);
        assertThat(store.getEntity(id1).get().getValue()).isEqualTo("a");

        // when
        store.add(id2, "b");

        // then
        assertThat(store.getEntity(id1).get().getValue()).isEqualTo("b");
        assertThat(store.getValue(id1).get()).isEqualTo("b");
        assertThat(store.size()).isEqualTo(2);
        assertThat(store.asEntities()).containsOnly(store.getEntity(id1).get(), store.getEntity(id3).get());

        // when
        store.delete(id1);

        //then
        assertThat(store.size()).isEqualTo(1);
        assertThat(store.getValue(id1)).isNotPresent();

        // when
        store.clear();

        //then
        assertThat(store.size()).isEqualTo(0);

    }
}