package net.rangaraj.kubemq.test
import java.util.concurrent.{Executors, ThreadPoolExecutor}

import io.kubemq.sdk.queue.{Message, Queue, SendMessageResult}
import io.kubemq.sdk.tools.Converter

import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import org.apache.commons.cli.{DefaultParser, HelpFormatter, Options}

import scala.concurrent.duration.Duration
import scala.io.Source
import scala.util.Random

class Producer(ec: ExecutionContext, threads: Int, host: String, messages: Int, file: Option[String], queueName: String) {
  def generateMessages(count: Int) : List[String] = {
    val r = Random
    val sentences = for (i <- 0 to count) yield {
      val sequences = for (q <- 0 to 200) yield {
        r.nextPrintableChar()
      }
      sequences.toString
    }
    sentences.toList
  }
  def produce(id: Int) : Unit = {
    println(s"${id}: Working on sending messages")
    val lines = if (file.isDefined) Source.fromFile(file.get).getLines().toList else generateMessages(messages)
    val nanos = System.nanoTime()

    try {

      val queue = new Queue(queueName, s"Producer-$id", host)
      for (i <- 0 to messages) {
        val resSend = queue.SendQueueMessage(new Message().setBody(Converter.ToByteArray(lines(i))).setMetadata(s"Sequence $id-$i").setExpiration(10))
        if (resSend.getIsError) println(s"$id: Message enqueue error, error: ${resSend.getError}") else println(s"$id: Sent message $i")
      }
    } catch {
      case e: Exception => println(s"$id: ${e.getMessage}")
    }
    println(s"${id}: Completed $messages in ${(System.nanoTime()-nanos)/1000000f}ms")
  }

  def run() : Unit = {
    val futures = for (i <- 0 until this.threads) yield {
      Future {produce(i)}(ec)
    }
    for (future <- futures) {
      Await.result(future, Duration.Inf)
    }
  }


}

object Producer {

  val defaultHost = "127.0.0.1"
  val defaultThreads = 1
  val defaultMessages = 10
  val defaultFile = None
  val defaultQueueName = "produce"
  def main(args: Array[String]) = {
    val options = new Options()
    options.addOption("H", true, "Host of kubemq cluster")
    options.addOption("t",  true,"Worker thread")
    options.addOption("c", true,"messages to send per worker")
    options.addOption("f", true,"message file, newline delimited")
    options.addOption("q", true, "queueName")
    options.addOption("h", "Help")
    val parsedArgs = new DefaultParser().parse(options, args)
    if (parsedArgs.hasOption("h")) {
      val formatter = new HelpFormatter
      formatter.printHelp("Producer", options)
      System.exit(0)
    }

    val host = if(parsedArgs.hasOption("H"))  parsedArgs.getOptionValue("H") else defaultHost
    println(s"Host: $host")
    val threads = if (parsedArgs.hasOption("t")) parsedArgs.getOptionValue("t").toInt else defaultThreads
    val messages = if (parsedArgs.hasOption("c")) parsedArgs.getOptionValue("c").toInt else defaultMessages
    val file = if (parsedArgs.hasOption("f")) Some(parsedArgs.getOptionValue("f")) else defaultFile
    val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(threads))
    val queue = if(parsedArgs.hasOption("q")) parsedArgs.getOptionValue("q") else defaultQueueName
    val producer = new Producer(ec, threads, host, messages, file, queue)
    producer.run()
    System.exit(0)

  }

}
