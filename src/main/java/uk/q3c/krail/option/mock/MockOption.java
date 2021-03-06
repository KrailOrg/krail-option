/*
 *
 *  * Copyright (c) 2016. David Sowerby
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 *  * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 *  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations under the License.
 *
 */

package uk.q3c.krail.option.mock;

import com.google.inject.Inject;
import uk.q3c.krail.option.Option;
import uk.q3c.krail.option.OptionKey;
import uk.q3c.krail.option.UserHierarchy;
import uk.q3c.krail.option.persist.OptionCache;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Note that this will not necessarily convert data types correctly
 * <p>
 * Created by David Sowerby on 27/02/15.
 */
public class MockOption implements Option {

    private transient UserHierarchy hierarchy;
    private Map<OptionKey, Optional<Object>> optionMap;
    private transient OptionCache optionCache;

    @Inject
    public MockOption() {
        optionMap = new HashMap<>();
        setup();
    }

    @Override
    public <T> void set(OptionKey<T> optionKey, T value) {
        set(optionKey, 0, value);
    }

    @Override
    public synchronized <T> void set(OptionKey<T> optionKey, int hierarchyRank, T value) {
        checkArgument(hierarchyRank >= 0);
        checkNotNull(optionKey);
        optionMap.put(optionKey, Optional.of(value));
    }

    @Override

    public synchronized <T> T get(OptionKey<T> optionKey) {
        checkNotNull(hierarchy);
        checkNotNull(optionKey);
        T defaultValue = optionKey.getDefaultValue();
        Optional<Object> optionalValue = optionMap.get(optionKey);
        if (optionalValue == null) {
            return defaultValue;
        }
        if (optionalValue.isPresent()) {
            return (T) optionalValue.get();
        } else {
            return defaultValue;
        }
    }


    @Override
    public synchronized <T> T getLowestRanked(OptionKey<T> optionKey) {
        return get(optionKey);
    }


    @Override
    public synchronized <T> T getSpecificRanked(int hierarchyRank, OptionKey<T> optionKey) {
        return get(optionKey);
    }

    @Override
    public UserHierarchy getHierarchy() {
        return hierarchy;
    }

    @Override

    public <T> T delete(OptionKey<T> optionKey, int hierarchyRank) {
        checkNotNull(hierarchy);
        checkArgument(hierarchyRank >= 0);
        checkNotNull(optionKey);

        return (T) optionMap.remove(optionKey);
    }

    @Override
    public OptionCache cache() {
        return optionCache;
    }

    @Override
    public <T> Optional<T> getValueFromCache(OptionKey<T> key, int hierarchyRank) {
        throw new UnsupportedOperationException("Method 'getValueFromCache' is not supported by MockOption");
    }

    private void readObject(ObjectInputStream inputStream) throws ClassNotFoundException, IOException {
        inputStream.defaultReadObject();
        setup();
    }

    private void setup() {
        hierarchy = mock(UserHierarchy.class);
        optionCache = mock(OptionCache.class);
        when(hierarchy.lowestRank()).thenReturn(5);
    }


}

