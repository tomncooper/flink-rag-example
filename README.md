# RAG Example

## Build the executable JARs

```shell
mvn package
```
This will create the data generator JAR in `datagen/target/flink-rag-datagen-<version>-jar-with-dependencies.jar`

## Running the example locally

1. Download a [Kafka distribution](https://kafka.apache.org/downloads). This demo is build using the 3.9.0 libraries:
1. Start Zookeeper:
   ```shell
   ./bin/zookeeper-server-start.sh config/zookeeper.properties 
   ```
1. Start Kafka:
   ```shell
   ./bin/kafka-server-start.sh config/server.properties 
   ```
1. Create the input `documents` topic:
   ```shell
   ./bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic documents --partitions 3 --replication-factor 1 --config retention.ms=300000 --config segment.ms=60000
   ```
1. Start a local Inifinispan server, which will serve as the vector store:
    ```shell
    podman run --net=host -p 11222:11222 -e USER="admin" -e PASS="secret" quay.io/infinispan/server:latest
    ```
1. Start the data generator. The first argument is the Kafka bootstrap address and the second is the per second message sending rate:
   ```shell
   java -jar datagen/target/flink-rag-datagen-1.0-SNAPSHOT-jar-with-dependencies.jar localhost:9092 0.2
   ```
1. Check that documents are flowing into the `documents` topic:
   ```shell
   ./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic documents
   ``` 
1. Download the [Flink 1.20 distribution](https://www.apache.org/dyn/closer.lua/flink/flink-1.20.0/flink-1.20.0-bin-scala_2.12.tgz) and start a session cluster:
   ```shell
   ./bin/start-cluster.sh
   ```
1. Run the datastream Flink JAR:
   ```shell
   ./bin/flink run datastream/target/flink-rag-datastream-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```
1. You can see the progress of the Flink Job in the [Flink UI](http://localhost:8081) and you should be able to see embeddings appearing in the [Infinispan UI](http://localhost:11222).