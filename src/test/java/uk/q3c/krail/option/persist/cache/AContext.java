package uk.q3c.krail.option.persist.cache;

import com.google.inject.Inject;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import uk.q3c.krail.i18n.test.TestLabelKey;
import uk.q3c.krail.option.Option;
import uk.q3c.krail.option.OptionChangeMessage;
import uk.q3c.krail.option.OptionContext;
import uk.q3c.krail.option.OptionKey;

/**
 * Created by David Sowerby on 20 Oct 2017
 */
@Listener
public class AContext implements OptionContext {

    public static final OptionKey<Integer> key1 = new OptionKey<>(125, AContext.class, TestLabelKey.Static, TestLabelKey.Large);
    public static final OptionKey<Integer> key2 = new OptionKey<>(18, AContext.class, TestLabelKey.Blank, TestLabelKey.Ok);
    public Object optionValue;
    private Option option;

    @Inject
    public AContext(Option option) {
        this.option = option;
    }

    @Override
    public Option optionInstance() {
        return option;
    }

    @Handler
    public void handleOptionChange(OptionChangeMessage<?> optionChangeMessage) {
        optionValue = optionChangeMessage.getNewValue();
    }
}
