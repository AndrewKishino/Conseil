package tech.cryptonomic.conseil.tezos

object Types {

  case class Block(
                    hash: String,
                    net_id: String,
                    operations: Seq[Seq[BlockOperation]],
                    protocol: String,
                    level: Int,
                    proto: Int,
                    predecessor: String,
                    timestamp: java.sql.Timestamp,
                    validation_pass: Int,
                    operations_hash: String,
                    fitness: Seq[String],
                    data: String
                  )

  case class BlockOperation(
                           hash: String,
                           branch: String,
                           data: String
                           )

}