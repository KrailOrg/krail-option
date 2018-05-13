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

package uk.q3c.krail.option.option;

import com.google.inject.Inject;
import uk.q3c.krail.eventbus.MessageBus;
import uk.q3c.krail.option.Option;
import uk.q3c.krail.option.OptionChangeMessage;
import uk.q3c.krail.option.OptionEditAction;
import uk.q3c.krail.option.OptionKey;
import uk.q3c.krail.option.OptionPermissionFailedException;
import uk.q3c.krail.option.OptionPermissionVerifier;
import uk.q3c.krail.option.RankOption;
import uk.q3c.krail.option.UserHierarchy;
import uk.q3c.krail.option.UserHierarchyDefault;
import uk.q3c.krail.option.persist.OptionCache;
import uk.q3c.krail.option.persist.OptionCacheKey;
import uk.q3c.krail.option.persist.OptionDaoDelegate;
import uk.q3c.krail.option.persist.cache.DefaultOptionCacheLoader;
import uk.q3c.util.guice.SerializationSupport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static uk.q3c.krail.option.RankOption.HIGHEST_RANK;
import static uk.q3c.krail.option.RankOption.LOWEST_RANK;
import static uk.q3c.krail.option.RankOption.SPECIFIC_RANK;

/**
 * Base implementation for {@link Option}. Uses {@link OptionCache}, which is configured to use some form of
 * persistence. All calls reference an implementation of {@link UserHierarchy}, either directly as a method
 * parameter, or by defaulting to {@link #hierarchy}.  The get() and set() default to using the highest rank
 * from {@link UserHierarchy}.  For getting or setting values at a specific hierarchyRank use the getSpecific() method, and
 * the set() methods which specify a rank. The delete() method is always specific
 * <br>
 * To create a hierarchy specific implementation, simply sub-class with the alternative hierarchy injected into it.
 * <br>
 * Permission is required to execute {@link #set(OptionKey, int, Object)}, {@link #set(OptionKey, Object)} or {@link #delete(OptionKey, int)}.  Permission is
 * verified by {@link OptionPermissionVerifier}
 * <p>
 * <b>NOTE:</b> All values to and from {@link Option} are natively typed.  All values to and from {@link OptionCache}, {@link DefaultOptionCacheLoader} and
 * {@link OptionDaoDelegate} are wrapped in Optional.
 * </p>
 * Created by David Sowerby on 03/12/14.
 */

public class DefaultOption implements Option, Serializable {

    private UserHierarchy hierarchy;
    private transient OptionCache optionCache;
    private OptionPermissionVerifier permissionVerifier;
    private transient MessageBus messageBus;
    private SerializationSupport serializationSupport;

    @Inject
    protected DefaultOption(OptionCache optionCache, @UserHierarchyDefault UserHierarchy hierarchy, OptionPermissionVerifier permissionVerifier, MessageBus messageBus, SerializationSupport serializationSupport) {
        this.hierarchy = hierarchy;
        this.optionCache = optionCache;
        this.permissionVerifier = permissionVerifier;
        this.messageBus = messageBus;
        this.serializationSupport = serializationSupport;
    }

    @Override
    public UserHierarchy getHierarchy() {
        return hierarchy;
    }

    @Override
    public OptionCache cache() {
        return optionCache;
    }




    @Override
    public <T> void set(OptionKey<T> optionKey, T value) {
        set(optionKey, 0, value);
    }

    @Override
    public synchronized <T> void set(OptionKey<T> optionKey, int hierarchyRank, T value) {
        checkArgument(hierarchyRank >= 0);
        checkNotNull(optionKey);
        if (permissionVerifier.userHasPermission(OptionEditAction.EDIT, hierarchy, hierarchyRank, optionKey)) {
            T oldValue = getSpecificRanked(hierarchyRank, optionKey);
            optionCache.write(new OptionCacheKey<>(hierarchy, SPECIFIC_RANK, hierarchyRank, optionKey), Optional.of(value));
            OptionChangeMessage<T> event = new OptionChangeMessage<>(optionKey, hierarchy, hierarchyRank, oldValue, value);
            messageBus.publishASync(event);
        } else {
            throw new OptionPermissionFailedException("Permission to edit option refused");
        }
    }


    @Override

    public synchronized <T> T get(OptionKey<T> optionKey) {
        checkNotNull(optionKey);
        return getRankedValue(optionKey, HIGHEST_RANK);
    }

    private <T> T getRankedValue(OptionKey<T> optionKey, RankOption rank) {
        T defaultValue = optionKey.getDefaultValue();
        Optional<T> optionalValue = optionCache.get(Optional.of(defaultValue), new OptionCacheKey<>(hierarchy, rank, 0, optionKey));
        if (optionalValue == null) {
            return defaultValue;
        }
        return optionalValue.orElse(defaultValue);
    }


    @Override
    public synchronized <T> T getLowestRanked(OptionKey<T> optionKey) {
        checkNotNull(optionKey);
        return getRankedValue(optionKey, LOWEST_RANK);
    }


    @Override
    public synchronized <T> T getSpecificRanked(int hierarchyRank, OptionKey<T> optionKey) {
        checkNotNull(optionKey);
        T defaultValue = optionKey.getDefaultValue();
        //noinspection unchecked
        Optional<T> optionalValue = optionCache.get(Optional.of(defaultValue), new OptionCacheKey(hierarchy, SPECIFIC_RANK, hierarchyRank, optionKey));

        if (optionalValue == null) {
            return defaultValue;
        }
        return optionalValue.orElse(defaultValue);
    }

    @Override
    public <T> Optional<T> getValueFromCache(OptionKey<T> optionKey, int hierarchyRank) {
        checkNotNull(optionKey);
        //noinspection unchecked
        Optional<T> value = (Optional<T>) optionCache.getIfPresent(new OptionCacheKey(hierarchy, SPECIFIC_RANK, hierarchyRank, optionKey));
        return value;
    }


    @Override

    public <T> T delete(OptionKey<T> optionKey, int hierarchyRank) {
        checkArgument(hierarchyRank >= 0);
        checkNotNull(optionKey);
        if (permissionVerifier.userHasPermission(OptionEditAction.EDIT, hierarchy, hierarchyRank, optionKey)) {
            //noinspection unchecked
            Optional<T> oldValueOpt = (Optional<T>) optionCache.delete(new OptionCacheKey(hierarchy, SPECIFIC_RANK, hierarchyRank, optionKey));
            T oldValue = oldValueOpt.orElse(null);
            OptionChangeMessage<T> event = new OptionChangeMessage<>(optionKey, hierarchy, hierarchyRank, oldValue, true);
            messageBus.publishASync(event);
            return oldValue;
        } else {
            throw new OptionPermissionFailedException("Permission to edit option refused");
        }
    }

    private void readObject(ObjectInputStream inputStream) throws ClassNotFoundException, IOException {
        inputStream.defaultReadObject();
        serializationSupport.deserialize(this);
    }

}
