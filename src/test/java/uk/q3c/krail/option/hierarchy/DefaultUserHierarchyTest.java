package uk.q3c.krail.option.hierarchy;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Created by David Sowerby on 20 Oct 2017
 */
public class DefaultUserHierarchyTest {

    DefaultUserHierarchy hierarchy;

    @Before
    public void setup() {
        hierarchy = new DefaultUserHierarchy();
    }

    @Test
    public void displayName() throws Exception {
        assertThat(hierarchy.displayName()).isEqualTo("DefaultUserHierarchy");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rankTooHigh() throws Exception {
        // when
        hierarchy.rankName(2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rankTooLow() throws Exception {
        // when
        hierarchy.rankName(-1);
    }

    @Test
    public void rankNames() throws Exception {
        assertThat(hierarchy.ranksForCurrentUser()).isEqualTo(ImmutableList.of("me", "system"));
        assertThat(hierarchy.lowestRank()).isEqualTo(1);
        assertThat(hierarchy.highestRankName()).isEqualTo("me");
        assertThat(hierarchy.lowestRankName()).isEqualTo("system");
    }
}