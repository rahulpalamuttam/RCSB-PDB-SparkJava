Directors to Run : <br />

Run Variables <br />
        $ARTICLES_DIRECTORY_PATH = the directory where all the PubMed articles are located <br />
        $SERIALIZED_ARTICLES_PATH = the directory where the serialized table of articles will be located (if implemented) <br />
        $SPARK_MASTER_IP = by default this should be set to local. However when connecting to a cluster set it to the ip address of the master node followed by :4040

Standalone mode
    1. mvn package
    2. run : java -jar target/PDB-Finder-Java-1.0-SNAPSHOT.jar $ARTICLES_DIRECTORY_PATH $SERIALIZED_ARTICLES_PATH $SPARK_MASTER_IP PDBID_FalsePositives.csv


CLuster Mode
    1. Make sure you have apache spark installed on a multi-node cluster, and is running
    2. run spark-submit --class Main target/PDBFinder-Java-1.0-SNAPSHOT.jar $ARTICLES_DIRECTORY_PATH $SERIALIZED_ARTICLES_PATH $SPARK_MASTER_IP PDBID_FalsePositives.csv
    3. Additional Options for cluster mode
        It is recommended to run with these options in cluster mode

        --conf spark.driver.memory=10g    | Sets the driver memory (the program that launches spark jobs)
        --conf spark.executor.memory=10g  | Sets memory for the node-specific processes that do the bulk processing work
        --conf spark.akka.frameSize=25000 | The max size of objects that are communicated to the driver node (specfically on collect tasks)
        --conf spark.akka.timeout=300     | The amount of time to wait for objects to be transmitted to driver before failing the task
