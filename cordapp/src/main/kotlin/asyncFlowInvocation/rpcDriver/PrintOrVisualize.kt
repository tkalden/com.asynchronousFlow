package asyncFlowInvocation.rpcDriver


import net.corda.core.transactions.SignedTransaction
import org.graphstream.graph.implementations.MultiGraph
import org.graphstream.graph.Edge
import org.graphstream.graph.Node


class PrintOrVisualize {

    val proxy = RpcDriver().main()

    val transactions: List<SignedTransaction> = proxy.internalVerifiedTransactionsFeed().snapshot

    val futureTransactions: rx.Observable<SignedTransaction> = proxy.internalVerifiedTransactionsFeed().updates


    fun print() {

        futureTransactions.startWith(transactions).subscribe()
        { transaction ->
            println("NODE ${transaction.id}")
            transaction.tx.inputs.forEach { (txhash) ->
                println("EDGE $txhash ${transaction.id}")
            }
        }
    }

    fun visualize(){

        val graph = MultiGraph("transactions")

            transactions.forEach { transaction ->
                graph.addNode<Node>("${transaction.id}")
            }
            transactions.forEach { transaction ->
                transaction.tx.inputs.forEach { ref ->
                    graph.addEdge<Edge>("$ref", "${ref.txhash}", "${transaction.id}")
                }
            }
            futureTransactions.subscribe { transaction ->
                graph.addNode<Node>("${transaction.id}")
                transaction.tx.inputs.forEach { ref ->
                    graph.addEdge<Edge>("$ref", "${ref.txhash}", "${transaction.id}")
                }
            }
            graph.display()
        }

    }

 fun main (args: Array<String>){
     PrintOrVisualize().print()
     PrintOrVisualize().visualize()
 }




