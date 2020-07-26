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
  def main(args: Array[String]) = {
    val options = new Options()
    options.addOption("H", true, "Host of kubemq cluster")
    options.addOption("q", true, "queueName")
    options.addOption("c", true, "Name of consumer")
    options.addOption("t", false, "Transactional receives")
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

    while(true) {
      val resRec = queue.ReceiveQueueMessages(10, 10)
      if (resRec.getIsError) {
        println(s"${consumerName}: Message dequeue error, error: ${resRec.getError}")
      }
      println(s"${consumerName}: Received Messages ${resRec.getMessagesReceived}")
      for (msg <- resRec.getMessages.asScala) {
        println(s"${consumerName}: MessageID: ${msg.getMessageID}, Body:${Converter.FromByteArray(msg.getBody)}" )
      }
    }

    System.exit(0)
  }
}
