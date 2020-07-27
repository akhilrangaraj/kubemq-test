package net.rangaraj.kubemq.test
import java.net.InetAddress
import java.util.concurrent.{Executors, ThreadPoolExecutor}

import io.kubemq.sdk.queue.{Message, Queue, SendMessageResult}
import io.kubemq.sdk.tools.Converter

import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import org.apache.commons.cli.{DefaultParser, HelpFormatter, Options}
import collection.JavaConverters._

import scala.concurrent.duration.Duration
import scala.io.Source
import scala.util.Random

object Consumer {

  val defaultHost = "127.0.0.1"
  val defaultQueueName = "produce"
  val defaultConsumerName = s"Consumer-${InetAddress.getLocalHost.getCanonicalHostName}"
  val defaultWorktime = 10
  def doNonTransactionalReceive(queue: Queue, workTime: Int, consumerName: String) : Int = {
    val resRec = queue.ReceiveQueueMessages(10, 10)
    if (resRec.getIsError) {
      println(s"${consumerName}: Message dequeue error, error: ${resRec.getError}")
    }
    for (msg <- resRec.getMessages.asScala) {
      Thread.sleep(workTime*1000)
      println(s"${consumerName}: MessageID: ${msg.getMessageID}" )
    }
    resRec.getMessagesReceived
  }
  def doTransactionalReceive(queue: Queue, workTime: Int, consumerName: String) : Int = {
    val transaction = queue.CreateTransaction()
    val resRec = transaction.Receive(10,10)
    if (resRec.getIsError) {
      println(s"${consumerName}: Message dequeue error, error: ${resRec.getError}")
    }
    println(s"${consumerName}: MessageID: ${resRec.getMessage.getMessageID}")
    Thread.sleep(workTime*1000)
    val ackRes = transaction.AckMessage()
    if (ackRes.getIsError) {
      println(s"${consumerName}: Message Ack error: ${ackRes.getError}")
      0
    } else {
      1
    }
  }
  def main(args: Array[String]) = {
    val options = new Options()
    options.addOption("H", true, "Host of kubemq cluster")
    options.addOption("q", true, "queueName")
    options.addOption("c", true, "Name of consumer")
    options.addOption("t", false, "Transactional receives")
    options.addOption("w", true, "Work Delay, in seconds")
    options.addOption("h", "Help")
    val parsedArgs = new DefaultParser().parse(options, args)
    if (parsedArgs.hasOption("h")) {
      val formatter = new HelpFormatter
      formatter.printHelp("Producer", options)
      System.exit(0)
    }
    val host = if(parsedArgs.hasOption("H")) parsedArgs.getOptionValue("H") else defaultHost
    val queueName = if(parsedArgs.hasOption("q")) parsedArgs.getOptionValue("q") else defaultQueueName
    val consumerName = if (parsedArgs.hasOption("c")) parsedArgs.getOptionValue("c") else defaultConsumerName
    val queue = new Queue(queueName, consumerName, host)
    val workTime = if (parsedArgs.hasOption("w")) parsedArgs.getOptionValue("w").toInt else defaultWorktime
    val transactional = if(parsedArgs.hasOption("t")) true else false
    var messageReceipt = 0

    while(true) {
      if (transactional) {
        messageReceipt += doTransactionalReceive(queue, workTime, consumerName)
      } else {
        messageReceipt += doNonTransactionalReceive(queue, workTime, consumerName)
      }

    }

    System.exit(0)
  }
}
