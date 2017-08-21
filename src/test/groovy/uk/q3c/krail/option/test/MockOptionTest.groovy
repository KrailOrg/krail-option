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

package uk.q3c.krail.option.test

import spock.lang.Specification

/**
 * Created by David Sowerby on 06 Feb 2016
 */
class MockOptionTest extends Specification {

    MockOption option

    def setup() {
        option = new MockOption()
    }

    def "return default"() {
        expect:
        option.get(MockOptionContext.optionKeyFlipFlop) == 32
        option.getHierarchy() != null
        option.getHierarchy().toString().startsWith("Mock for UserHierarchy")
    }

    def "set, get and delete"() {
        when:
        option.set(MockOptionContext.optionKeyFlipFlop, 43)

        then:
        option.get(MockOptionContext.optionKeyFlipFlop) == 43
        option.getLowestRanked(MockOptionContext.optionKeyFlipFlop) == 43
        option.getSpecificRanked(0, MockOptionContext.optionKeyFlipFlop) == 43


        when:
        option.delete(MockOptionContext.optionKeyFlipFlop, 0)

        then:
        option.get(MockOptionContext.optionKeyFlipFlop) == 32
    }
}
