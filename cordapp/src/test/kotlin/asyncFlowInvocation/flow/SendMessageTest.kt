package asyncFlowInvocation.flow

import asyncFlowInvocation.State.MessageState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import org.junit.After
import org.junit.Before
import org.junit.Test

class SendMessageTest {

    private lateinit var mockNetwork: MockNetwork
    private lateinit var partyA: StartedMockNode
    private lateinit var partyB: StartedMockNode
    private lateinit var notaryNode: MockNetworkNotarySpec

    @Before
    fun setup() {
        notaryNode = MockNetworkNotarySpec(CordaX500Name("Notary", "London", "GB"))
        mockNetwork = MockNetwork(
                listOf(
                        "asyncFlowInvocation"
                ),
                notarySpecs = listOf(notaryNode),
                threadPerNode = true,
                networkSendManuallyPumped = false
        )
        partyA =
                mockNetwork.createNode(MockNodeParameters(legalName = CordaX500Name("PartyA", "Berlin", "DE")))

        partyB =
                mockNetwork.createNode(MockNodeParameters(legalName = CordaX500Name("PartyB", "Berlin", "DE")))
        mockNetwork.startNodes()
    }

    @After
    fun tearDown() {
        mockNetwork.stopNodes()
    }

    @Test
    fun `Flow runs without errors`() {
        val message1=  MessageState(
                contents = "hey",
                recipient = partyB.info.singleIdentity(),
                sender = partyA.info.singleIdentity(),
                linearId = UniqueIdentifier()
        )

        val message2=  MessageState(
                contents = "How is it going?",
                recipient = partyB.info.singleIdentity(),
                sender = partyA.info.singleIdentity(),
                linearId = UniqueIdentifier()
        )


        // Calling the service directly

       // val result1= partyA.startFlow(SendNewMessageFlow(message1)).getOrThrow()

        val result2= partyA.startFlow(SendMessageFlow(message2)).getOrThrow()

        var statesB = listOf<StateAndRef<MessageState>>()

        partyB.transaction {

            statesB = partyB.services.vaultService.queryBy<MessageState>().states
        }


        println("statesB : ${statesB.size}")


        var statesA = listOf<StateAndRef<MessageState>>()


        partyA.transaction {

            statesA = partyA.services.vaultService.queryBy<MessageState>().states
        }

        println("statesA : ${statesA.size}")


    }
}