### Why we need Asynchronous Flow Invocation
 If a user kicks off a flow and waits for a response on a single thread, it makes the performance less effecient. In the production scale, users invoke flow multiple times and if each flow invocation is synchronous then the remaining flow must wait till the first flow finishes! This causes the deadlock, where users have to wait forever to get response.
   The simple solution to deadlock is to kick off flow asynchronously. Now this time when the user kicks off multiple flow, each flow will be run on different thread. Depending on the computation resources, one can definie the number of threads. If it's 3, then remaining flow has to wait till any of these 3 thread is available.

#### Background 
This repository contains a simple "Message State". The goal is send the message state between the nodes asynchronously

## Running the repository 

## Pre-Requisites

You will need the following installed on your machine before you can start:

* [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) 
  installed and available on your path (Minimum version: 1.8_131).
* [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) (Minimum version 2017.1)
* git
* Optional: [h2 web console](http://www.h2database.com/html/download.html)
  (download the "platform-independent zip")

For more detailed information, see the
[getting set up](https://docs.corda.net/getting-set-up.html) page on the
Corda docsite.

For IDE, compilation and JVM version issues, see the
[Troubleshooting](https://docs.corda.net/troubleshooting.html) page on the Corda docsite.

## Getting Set Up

To get started, clone this repository with:

     git clone https://github.com/corda/cordapp-template-kotlin.git

And change directories to the newly cloned repo:
https://github.com/tkalden/com.asynchronousFlow.git

cd com.asynchronousFlow

## Building the CorDapp:

**Unix:** 

     ./gradlew deployNodes

**Windows:**

     gradlew.bat deployNodes

Note: You'll need to re-run this build step after making any changes to
the template for these to take effect on the node.

## Running the Nodes

Once the build finishes, change directories to the folder where the newly
built nodes are located:

     cd build/nodes

The Gradle build script will have created a folder for each node. You'll
see three folders, one for each node and a `runnodes` script. You can
run the nodes with:

**Unix:**

     ./runnodes --log-to-console --logging-level=DEBUG

**Windows:**

    runnodes.bat --log-to-console --logging-level=DEBUG

You should now have four Corda nodes running on your machine 

When the nodes have booted up, you should see a message like the following 
in the console: 

     Node started up and registered in 5.007 sec
     
## Interacting node via RPC
Each node has rpc address when it boots up, user can proxy to that rpc address and kick off flows owned by that node.
In this case we have SendMessageFlow which sends message from one node to another node.

There is RpcDriver class where user can define rpc address, user and pw of node one is interested to test

SendMessage class is where you call the RpcDriver and kick off the flows asynchronously.





 
