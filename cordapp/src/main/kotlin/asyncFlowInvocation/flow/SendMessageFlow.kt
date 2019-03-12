package asyncFlowInvocation.flow

import asyncFlowInvocation.State.MessageState
import asyncFlowInvocation.contract.MessageContract
import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
@StartableByService
class SendMessageFlow(private val message: MessageState) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        logger.info("Started sending message ${message.contents}")
        val stx = collectSignature(verifyAndSign(transaction()))
        logger.info("Suspending to finalise ${message.contents}")
        val tx =  subFlow(FinalityFlow(stx))
        logger.info("Finished sending message ${message.contents}")
        return tx
    }

    @Suspendable
    private fun collectSignature(
        transaction: SignedTransaction
    ): SignedTransaction {
        logger.info("Suspending to collect signatures ${message.contents}")
        return subFlow(CollectSignaturesFlow(transaction, listOf(initiateFlow(message.recipient))))
    }

    private fun verifyAndSign(transaction: TransactionBuilder): SignedTransaction {
        transaction.verify(serviceHub)
        return serviceHub.signInitialTransaction(transaction)
    }

    private fun transaction() =
        TransactionBuilder(notary()).apply {
            addOutputState(message, MessageContract.PROGRAM_ID)
            addCommand(Command(command(), message.participants.map(Party::owningKey)))
        }

    private fun notary() = serviceHub.networkMapCache.notaryIdentities.first()

    private fun command() = MessageContract.Commands.Send()
}

@InitiatedBy(SendMessageFlow::class)
class SendMessageResponder(private val flowSession: FlowSession): FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs[0].data as MessageState
                "This must be an FNOL creation" using (output is MessageState)
            }
        }
        subFlow(signedTransactionFlow)
    }
}