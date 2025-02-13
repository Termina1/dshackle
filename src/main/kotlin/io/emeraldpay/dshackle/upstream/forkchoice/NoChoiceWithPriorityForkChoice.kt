package io.emeraldpay.dshackle.upstream.forkchoice

import io.emeraldpay.dshackle.data.BlockContainer
import io.emeraldpay.dshackle.data.BlockId
import io.emeraldpay.dshackle.data.RingSet
import java.util.concurrent.atomic.AtomicReference

class NoChoiceWithPriorityForkChoice(
    private val nodeRating: Int
) : ForkChoice {
    private val head = AtomicReference<BlockContainer>(null)
    private val seenBlocks = RingSet<BlockId>(100)

    override fun getHead(): BlockContainer? {
        return head.get()
    }

    override fun filter(block: BlockContainer): Boolean {
        return !seenBlocks.contains(block.hash)
    }

    override fun choose(block: BlockContainer): ForkChoice.ChoiceResult {
        val nwhead = head.updateAndGet { curr ->
            if (!filter(block)) {
                curr
            } else {
                seenBlocks.add(block.hash)
                block.copyWithRating(nodeRating)
            }
        }
        if (nwhead.hash == block.hash) {
            return ForkChoice.ChoiceResult.Updated(nwhead)
        }
        return ForkChoice.ChoiceResult.Same(nwhead)
    }
}
