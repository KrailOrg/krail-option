package uk.q3c.krail.option.option;

import uk.q3c.krail.eventbus.BusMessage;
import uk.q3c.krail.option.OptionKey;
import uk.q3c.krail.option.UserHierarchy;

import javax.annotation.concurrent.Immutable;

/**
 * A message (event) identifying a change to an Option value.
 * <p>
 * When a hierarchy rank is deleted, {@link #newValue} is always null - the overall value of the option will have changed,
 * but may depend on which user is accessing it - and therefore cannot be quantified for this event
 * <p>
 * When an option value is set, it is set at a specific rank.  The previous value is never null - all options have a default value.
 * <p>
 * <p>
 * Created by David Sowerby on 17 Oct 2017
 */
@Immutable
public class OptionChangeMessage<T> implements BusMessage {

    private final OptionKey<T> optionKey;
    private final UserHierarchy hierarchy;
    private final boolean rankDeleted;
    private final int hierarchyRank;
    private final T newValue;
    private final T oldValue;

    public OptionChangeMessage(OptionKey<T> optionKey, UserHierarchy hierarchy, int hierarchyRank, T oldValue, T newValue) {

        this.optionKey = optionKey;
        this.hierarchy = hierarchy;
        this.hierarchyRank = hierarchyRank;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.rankDeleted = false;
    }

    public OptionChangeMessage(OptionKey<T> optionKey, UserHierarchy hierarchy, int hierarchyRank, T oldValue, boolean rankDeleted) {

        this.optionKey = optionKey;
        this.hierarchy = hierarchy;
        this.hierarchyRank = hierarchyRank;
        this.oldValue = oldValue;
        this.rankDeleted = rankDeleted;
        this.newValue = null;
    }

    public boolean isRankDeleted() {
        return rankDeleted;
    }

    public OptionKey<T> getOptionKey() {
        return optionKey;
    }

    public UserHierarchy getHierarchy() {
        return hierarchy;
    }

    public int getHierarchyRank() {
        return hierarchyRank;
    }

    public T getNewValue() {
        return newValue;
    }

    public T getOldValue() {
        return oldValue;
    }
}
