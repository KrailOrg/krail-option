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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import uk.q3c.krail.eventbus.MessageBus;
import uk.q3c.krail.eventbus.MessageBusProvider;
import uk.q3c.krail.i18n.I18NKey;
import uk.q3c.krail.option.Option;
import uk.q3c.krail.option.OptionChangeMessage;
import uk.q3c.krail.option.OptionContext;
import uk.q3c.krail.option.OptionKey;
import uk.q3c.krail.option.OptionPermissionFailedException;
import uk.q3c.krail.option.UserHierarchy;
import uk.q3c.krail.option.mock.MockOptionContext;
import uk.q3c.krail.option.mock.MockOptionPermissionVerifier;
import uk.q3c.krail.option.persist.OptionCache;
import uk.q3c.krail.option.persist.OptionCacheKey;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.q3c.krail.option.RankOption.HIGHEST_RANK;
import static uk.q3c.krail.option.RankOption.LOWEST_RANK;
import static uk.q3c.krail.option.RankOption.SPECIFIC_RANK;

@RunWith(MockitoJUnitRunner.class)
public class DefaultOptionTest {

    DefaultOption option;
    MockOptionContext contextObject;
    Class<MockOptionContext> context = MockOptionContext.class;
    Class<MockContext2> context2 = MockContext2.class;

    @Mock
    MessageBusProvider singletonMessageBusProvider;

    @Mock
    MessageBus globalBus;

    @Mock
    private UserHierarchy defaultHierarchy;
    @Mock
    private OptionCache optionCache;
    private OptionKey<Integer> optionKey1;
    private OptionKey<Integer> optionKey2;
    private MockOptionPermissionVerifier permissionVerifier;

    ArgumentCaptor<OptionChangeMessage> messageCaptor;

    @Before
    public void setup() {
        permissionVerifier = new MockOptionPermissionVerifier();
        when(defaultHierarchy.highestRankName()).thenReturn("ds");
        when(singletonMessageBusProvider.get()).thenReturn(globalBus);
        contextObject = new MockOptionContext();
        option = new DefaultOption(optionCache, defaultHierarchy, permissionVerifier, singletonMessageBusProvider);
        optionKey1 = new OptionKey<>(5, context, TestLabelKey.key1, "q");
        optionKey2 = new OptionKey<>(5, context2, TestLabelKey.key1, "q");
        messageCaptor = ArgumentCaptor.forClass(OptionChangeMessage.class);
    }

    @Test(expected = OptionPermissionFailedException.class)
    public void setNoPermissions() {
        // given
        permissionVerifier.throwException(true);
        OptionCacheKey<Integer> cacheKey = new OptionCacheKey<>(defaultHierarchy, SPECIFIC_RANK, 0, optionKey1);
        //when
        option.set(optionKey1, 5);
        //then
    }

    @Test
    public void set_simplest() {
        //given

        when(defaultHierarchy.rankName(0)).thenReturn("specific");
        OptionCacheKey<Integer> cacheKey = new OptionCacheKey<>(defaultHierarchy, SPECIFIC_RANK, 0, optionKey1);
        //when
        option.set(optionKey1, 3);
        //then
        verify(optionCache).write(cacheKey, Optional.of(3));
        assertThat(option.getHierarchy()).isEqualTo(defaultHierarchy);
        verify(globalBus).publishASync(messageCaptor.capture());
        List<OptionChangeMessage> messages = messageCaptor.getAllValues();
        assertThat(messages.size()).isEqualTo(1);
        OptionChangeMessage msg = messages.get(0);
        assertThat(msg.getHierarchy()).isEqualTo(defaultHierarchy);
        assertThat(msg.getHierarchyRank()).isEqualTo(0);
        assertThat(msg.getOptionKey()).isEqualTo(optionKey1);
        assertThat(msg.getNewValue()).isEqualTo(3);
        assertThat(msg.getOldValue()).isEqualTo(5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void set_with_all_args_rank_too_low() {
        //given
        when(defaultHierarchy.rankName(2)).thenReturn("specific");
        OptionKey<Integer> optionKey2 = new OptionKey<>(999, context, TestLabelKey.key1, TestLabelKey.key1, "q");
        OptionCacheKey<Integer> cacheKey = new OptionCacheKey<>(defaultHierarchy, SPECIFIC_RANK, 2, optionKey2);
        //when
        option.set(optionKey2, -1, 5);
        //then
    }

    @Test
    public void get_highest() {
        //given
        when(defaultHierarchy.highestRankName()).thenReturn("high");
        OptionCacheKey<Integer> cacheKey = new OptionCacheKey<>(defaultHierarchy, HIGHEST_RANK, optionKey1);
        when(optionCache.get(Optional.of(5), cacheKey)).thenReturn(Optional.of(8));
        //when
        Integer actual = option.get(optionKey1);
        //then
        assertThat(actual).isEqualTo(8);
    }

    @Test
    public void get_none_found() {
        //given
        when(defaultHierarchy.highestRankName()).thenReturn("high");
        OptionCacheKey<Integer> cacheKey = new OptionCacheKey<>(defaultHierarchy, HIGHEST_RANK, optionKey2);
        when(optionCache.get(Optional.of(5), cacheKey)).thenReturn(Optional.empty());
        //when
        Integer actual = option.get(optionKey2);
        //then
        assertThat(actual).isEqualTo(5);
    }

    @Test
    public void get_specific() throws Exception {
        //given
        when(defaultHierarchy.lowestRankName()).thenReturn("low");
        OptionCacheKey<Integer> cacheKey = new OptionCacheKey<>(defaultHierarchy, SPECIFIC_RANK, optionKey2);
        when(optionCache.get(any(), any())).thenAnswer(answerOf(20));
        //when
        Integer actual = option.getSpecificRanked(0, optionKey2);
        //then
        assertThat(actual).isEqualTo(20);
    }

    @Test
    public void get_specific_not_found_return_default() throws Exception {
        //given
        when(defaultHierarchy.lowestRankName()).thenReturn("low");
        OptionCacheKey<Integer> cacheKey = new OptionCacheKey<>(defaultHierarchy, SPECIFIC_RANK, optionKey2);
        when(optionCache.get(any(), any())).thenReturn(Optional.empty());
        //when
        Integer actual = option.getSpecificRanked(0, optionKey2);
        //then
        assertThat(actual).isEqualTo(5);
    }

    @Test
    public void get_specific_null_return_default() throws Exception {
        //given
        when(defaultHierarchy.lowestRankName()).thenReturn("low");
        OptionCacheKey<Integer> cacheKey = new OptionCacheKey<>(defaultHierarchy, SPECIFIC_RANK, optionKey2);
        when(optionCache.get(any(), any())).thenReturn(null);
        //when
        Integer actual = option.getSpecificRanked(0, optionKey2);
        //then
        assertThat(actual).isEqualTo(5);

    }

    @Test
    public void get_lowest() {
        //given
        when(defaultHierarchy.lowestRankName()).thenReturn("low");
        OptionCacheKey<Integer> cacheKey = new OptionCacheKey<>(defaultHierarchy, LOWEST_RANK, optionKey2);
        when(optionCache.get(any(), any())).thenAnswer(answerOf(20));
        //when
        Integer actual = option.getLowestRanked(optionKey2);
        //then
        assertThat(actual).isEqualTo(20);
    }

    protected Answer<Optional<Integer>> answerOf(Integer value) {
        return new Answer<Optional<Integer>>() {
            @Override
            public Optional<Integer> answer(InvocationOnMock invocation) throws Throwable {
                return Optional.of(value);
            }
        };
    }

    @Test
    public void delete() {
        //given
        when(defaultHierarchy.rankName(1)).thenReturn("specific");
        OptionCacheKey<Integer> cacheKey = new OptionCacheKey<>(defaultHierarchy, SPECIFIC_RANK, 1, optionKey2);
        when(optionCache.delete(any())).thenAnswer(answerOf(3));
        //when
        Object actual = option.delete(optionKey2, 1);
        //then
        assertThat(actual).isEqualTo(3);
        verify(optionCache).delete(cacheKey);
        verify(globalBus).publishASync(messageCaptor.capture());
        List<OptionChangeMessage> messages = messageCaptor.getAllValues();
        assertThat(messages.size()).isEqualTo(1);
        OptionChangeMessage msg = messages.get(0);
        assertThat(msg.getHierarchy()).isEqualTo(defaultHierarchy);
        assertThat(msg.getHierarchyRank()).isEqualTo(1);
        assertThat(msg.getOptionKey()).isEqualTo(optionKey2);
        assertThat(msg.getNewValue()).isEqualTo(null);
        assertThat(msg.getOldValue()).isEqualTo(3);
    }

    @Test(expected = OptionPermissionFailedException.class)
    public void delete_no_permissions() {
        //given
        permissionVerifier.throwException(true);
        OptionCacheKey<Integer> cacheKey = new OptionCacheKey<>(defaultHierarchy, SPECIFIC_RANK, 1, optionKey2);
        when(optionCache.delete(cacheKey)).thenAnswer(answerOf(3));
        //when
        Object actual = option.delete(optionKey2, 1);
        //then
    }


    enum TestLabelKey implements I18NKey {
        key1, Blank, Large, Private_Static, Static, key2
    }

    static class MockContext2 implements OptionContext {

        public static final OptionKey<Integer> key3 = new OptionKey<>(125, MockContext2.class, TestLabelKey.Static, TestLabelKey.Large);
        private static final OptionKey<Integer> key4 = new OptionKey<>(126, MockContext2.class, TestLabelKey.Private_Static, TestLabelKey.Large);
        public final OptionKey<Integer> key2 = new OptionKey<>(124, this, TestLabelKey.key2, TestLabelKey.Blank);
        private final OptionKey<Integer> key1 = new OptionKey<>(123, this, TestLabelKey.key1);


        @Override
        public Option optionInstance() {
            return null;
        }




    }


}