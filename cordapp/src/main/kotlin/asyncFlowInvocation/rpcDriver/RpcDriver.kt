package asyncFlowInvocation.rpcDriver


import net.corda.client.rpc.CordaRPCClient
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort


class RpcDriver {

    fun main():CordaRPCOps {

        val client = CordaRPCClient(NetworkHostAndPort.parse("localhost:10008"))
        val connection = client.start("user1", "test")
        return  connection.proxy
       // connection.notifyServerAndClose()
    }
}

