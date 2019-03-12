package asyncFlowInvocation.services

import asyncFlowInvocation.State.MessageState
import asyncFlowInvocation.flow.SendMessageFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.util.concurrent.*

@CordaService
@StartableByRPC
@StartableByService
class MessageService(private val serviceHub:AppServiceHub): SingletonSerializeAsToken() {

    companion object {

        var executor: ExecutorService = ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, ArrayBlockingQueue(15))
        private val logger = LoggerFactory.getLogger(MessageService::class.java)
    }

    fun replyAll( messages : MessageState) {

       // var signedTx: SignedTransaction? = null

        try {
                executor.execute {

                     println(executor.toString())
                     val result: Future<SignedTransaction> =  serviceHub.startFlow(SendMessageFlow(messages)).returnValue
                     println("result : $result")
                     val  signedTx = result.getOrThrow()
                    println("signedTX : $signedTx")

                }
           // }
        } catch( e: Exception){

            println(e.message)

        }
       // return signedTx!!
    }
}