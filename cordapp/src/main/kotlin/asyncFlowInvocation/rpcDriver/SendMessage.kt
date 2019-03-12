package asyncFlowInvocation.rpcDriver

import asyncFlowInvocation.State.MessageState
import asyncFlowInvocation.flow.SendNewMessageFlow
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow


fun main(args: Array<String>) {

    val proxy = RpcDriver().main()

    val me = proxy.nodeInfo().legalIdentities[0]

    val otherParty = proxy.wellKnownPartyFromX500Name(CordaX500Name("PartyB", "New York", "US"))!!

    val message1=  MessageState(
            contents = "hey",
            recipient = otherParty,
            sender = me,
            linearId = UniqueIdentifier()
    )

    //proxy.startFlow { SendMessageFlow::java }


    val message2=  MessageState(
            contents = "How",
            recipient = otherParty,
            sender = me,
            linearId = UniqueIdentifier()
    )

    val message3=  MessageState(
            contents = "Are",
            recipient = otherParty,
            sender = me,
            linearId = UniqueIdentifier()
    )


    val result1 = proxy.startFlow(::SendNewMessageFlow,message1).returnValue.getOrThrow()

    val result2 = proxy.startFlowDynamic(SendNewMessageFlow::class.java,message2).returnValue.getOrThrow()



    println("result1: $result1")
    println("result2: $result2")






}












