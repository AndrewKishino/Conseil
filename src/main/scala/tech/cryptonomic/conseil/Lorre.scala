package tech.cryptonomic.conseil

import com.typesafe.scalalogging.LazyLogging
import tech.cryptonomic.conseil.tezos.{TezosDatabaseOperations, TezosNodeOperations}
import tech.cryptonomic.conseil.util.DatabaseUtil

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

/**
  * Entry point for synchronizing data between the Tezos blockchain and the Conseil database.
  */
object Lorre extends App with LazyLogging {

  lazy val db = DatabaseUtil.db
  processTezosBlocks()
  processTezosAccounts()
  db.close()

  /**
    * Fetches all blocks not in the database from the Tezos network and adds them to the database.
    */
  def processTezosBlocks(): Unit = {
    logger.info("Processing Tezos Blocks..")
    TezosNodeOperations.getBlocksNotInDatabase("alphanet") match {
      case Success(blocks) => {
        Try {
          val dbFut = TezosDatabaseOperations.writeBlocksToDatabase(blocks, db)
          dbFut onComplete {
            _ match {
              case Success(_) => logger.info(s"Wrote ${blocks.size} blocks to the database.")
              case Failure(e) => logger.error(s"Could not write blocks to the database because ${e}")
            }
          }
          Await.result(dbFut, Duration.Inf)
        }
      }
      case Failure(e) => logger.error(s"Could not fetch blocks from client because ${e}")
    }
  }

  /**
    * Fetches and stores all accounts from the latest block stored in the database.
    */
  def processTezosAccounts(): Unit = {
    logger.info("Processing latest Tezos accounts data..")
    TezosNodeOperations.getLatestAccounts("alphanet") match {
      case Success(accountsInfo) =>
        Try {
          val dbFut = TezosDatabaseOperations.writeAccountsToDatabase(accountsInfo, db)
          dbFut onComplete {
            _ match {
              case Success(_) => logger.info(s"Wrote ${accountsInfo.accounts.size} accounts to the database.")
              case Failure(e) => logger.error(s"Could not write accounts to the database because ${e}")
            }
          }
          Await.result(dbFut, Duration.Inf)
        }
      case Failure(e) => logger.error(s"Could not fetch accounts from client because ${e}")
    }
  }


}
