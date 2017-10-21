package uk.q3c.krail.persist.inmemory;

import org.junit.Before;
import org.junit.Test;
import uk.q3c.krail.option.RankOption;
import uk.q3c.krail.option.hierarchy.DefaultUserHierarchy;
import uk.q3c.krail.option.persist.OptionCacheKey;
import uk.q3c.krail.option.persist.OptionId;
import uk.q3c.krail.option.persist.cache.AContext;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Created by David Sowerby on 20 Oct 2017
 */
public class OptionEntityTest {

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

    }

    @Test(expected = NullPointerException.class)
    public void constructorNullKey() throws Exception {
        OptionEntity entity = new OptionEntity((OptionCacheKey) null, "x");
    }

    @Test(expected = NullPointerException.class)
    public void constructorNullValue() throws Exception {
        OptionEntity entity = new OptionEntity(cacheKey1, null);
    }

    @Test
    public void equalsFromCacheKey() throws Exception {
        // given
        OptionEntity entity1 = new OptionEntity(cacheKey1, "x");
        OptionEntity entity2 = new OptionEntity(cacheKey2, "x");
        OptionEntity entity3 = new OptionEntity(cacheKey3, "x");

        // expect
        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1).isNotEqualTo(entity3);
    }

    @Test(expected = NullPointerException.class)
    public void constructor2NullKey() throws Exception {
        OptionEntity entity = new OptionEntity((OptionId) null, "x");
    }

    @Test(expected = NullPointerException.class)
    public void constructor2NullValue() throws Exception {
        OptionEntity entity = new OptionEntity(id1, null);
    }

    @Test
    public void equalsFromOptionId() throws Exception {
        // given
        OptionEntity entity1 = new OptionEntity(id1, "x");
        OptionEntity entity2 = new OptionEntity(id2, "x");
        OptionEntity entity3 = new OptionEntity(id3, "x");

        // expect
        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
        assertThat(entity1).isNotEqualTo(entity3);
        assertThat(entity1.hashCode()).isNotEqualTo(entity3.hashCode());
    }

    @Test
    public void getters() throws Exception {
        // given
        OptionEntity entity1 = new OptionEntity(id1, "x");

        // expect
        assertThat(entity1.getContext()).isEqualTo(AContext.class.getName());
        assertThat(entity1.getOptionId()).isEqualTo(id1);
        assertThat(entity1.getOptionKey()).isEqualTo("AContext-Static");
        assertThat(entity1.getRankName()).isEqualTo("me");
        assertThat(entity1.getUserHierarchyName()).isEqualTo("DefaultUserHierarchy");
        assertThat(entity1.getValue()).isEqualTo("x");
    }
}