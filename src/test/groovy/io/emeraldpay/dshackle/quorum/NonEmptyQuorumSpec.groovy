/**
 * Copyright (c) 2020 EmeraldPay, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.emeraldpay.dshackle.quorum

import io.emeraldpay.dshackle.test.TestingCommons
import io.emeraldpay.dshackle.upstream.Head
import io.emeraldpay.dshackle.upstream.Upstream
import io.emeraldpay.dshackle.upstream.rpcclient.JsonRpcException
import io.emeraldpay.etherjar.rpc.RpcException
import spock.lang.Specification

class NonEmptyQuorumSpec extends Specification {

    def "Fail if too many errors"() {
        setup:
        def q = Spy(new NonEmptyQuorum(3))
        def upstream1 = Stub(Upstream)
        def upstream2 = Stub(Upstream)
        def upstream3 = Stub(Upstream)

        when:
        q.init(Stub(Head))
        then:
        !q.isResolved()
        !q.isFailed()

        when:
        q.record(new JsonRpcException(1, "Internal"), null, upstream1)
        then:
        !q.isResolved()
        !q.isFailed()

        when:
        q.record(new JsonRpcException(1, "Internal"), null, upstream2)
        then:
        !q.isResolved()
        !q.isFailed()

        when:
        q.record(new JsonRpcException(1, "Internal"), null, upstream3)
        then:
        q.isFailed()
        !q.isResolved()
        q.signature == null
    }

    def "Fail first if not error"() {
        setup:
        def q = Spy(new NonEmptyQuorum(3))
        def upstream1 = Stub(Upstream)
        def upstream2 = Stub(Upstream)
        def upstream3 = Stub(Upstream)

        when:
        q.init(Stub(Head))
        then:
        !q.isResolved()
        !q.isFailed()

        when:
        q.record('"0x11"'.bytes, null, upstream1)
        then:
        q.isResolved()
        !q.isFailed()
    }

    def "Fail second if first is error"() {
        setup:
        def q = Spy(new NonEmptyQuorum(3))
        def upstream1 = Stub(Upstream)
        def upstream2 = Stub(Upstream)
        def upstream3 = Stub(Upstream)

        when:
        q.init(Stub(Head))
        then:
        !q.isResolved()
        !q.isFailed()

        when:
        q.record(new JsonRpcException(1, "Internal"), null, upstream1)
        then:
        !q.isFailed()
        !q.isResolved()
        q.signature == null

        when:
        q.record('"0x11"'.bytes, null, upstream2)
        then:
        q.isResolved()
        !q.isFailed()
    }

    def "Fail second if first is null"() {
        setup:
        def q = Spy(new NonEmptyQuorum(3))
        def upstream1 = Stub(Upstream)
        def upstream2 = Stub(Upstream)
        def upstream3 = Stub(Upstream)

        when:
        q.init(Stub(Head))
        then:
        !q.isResolved()
        !q.isFailed()

        when:
        q.record('null'.bytes, null, upstream2)
        then:
        !q.isFailed()
        !q.isResolved()


        when:
        q.record('"0x11"'.bytes, null, upstream2)
        then:
        q.isResolved()
        !q.isFailed()
    }


}
