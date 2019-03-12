package asyncFlowInvocation.flow

import asyncFlowInvocation.State.MessageState
import co.paralleluniverse.fibers.Suspendable
import asyncFlowInvocation.services.MessageService
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.util.concurrent.*

@InitiatingFlow
@StartableByRPC
class SendNewMessageFlowV2(private val message: MessageState) : FlowLogic<SignedTransaction>() {

    companion object {

        var executor: ExecutorService = ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, ArrayBlockingQueue(15))
        private val logger = LoggerFactory.getLogger(MessageService::class.java)
    }

    @Suspendable
    override  fun call(): SignedTransaction{

         var signedTx: SignedTransaction? = null

        try {
            executor.execute {

                println(executor.toString())
                signedTx = subFlow(SendMessageFlow(message))
                println("result : $signedTx")

            }
            // }
        } catch( e: Exception){

            println(e.message)

        }
        return signedTx!!
    }

}