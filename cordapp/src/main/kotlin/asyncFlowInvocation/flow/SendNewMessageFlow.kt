package asyncFlowInvocation.flow

import asyncFlowInvocation.State.MessageState
import co.paralleluniverse.fibers.Suspendable
import asyncFlowInvocation.services.MessageService
import net.corda.core.flows.*

@InitiatingFlow
@StartableByRPC
class SendNewMessageFlow(private val message: MessageState) : FlowLogic<Unit>() {


    @Suspendable
    override fun call(){

        val messageService= serviceHub.cordaService(MessageService::class.java)

        messageService.replyAll(message)



    }

}