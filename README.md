Directors to Run : <br />

Run Variables <br />
        $ARTICLES_DIRECTORY_PATH = the directory where all the PubMed articles are located <br />
        $SERIALIZED_ARTICLES_PATH = the directory where the serialized table of articles will be located (if implemented) <br />
        $SPARK_MASTER_IP = by default this should be set to local. However when connecting to a cluster set it to the ip address of the master node followed by ":4040" <br />

Standalone mode <br />
    1. mvn package <br />
    2. run : java -jar target/PDB-Finder-Java-1.0-SNAPSHOT.jar $ARTICLES_DIRECTORY_PATH $SERIALIZED_ARTICLES_PATH $SPARK_MASTER_IP PDBID_FalsePositives.csv <br />


CLuster Mode <br />
    1. Make sure you have apache spark installed on a multi-node cluster, and is running <br />
    2. run spark-submit --class Main target/PDBFinder-Java-1.0-SNAPSHOT.jar $ARTICLES_DIRECTORY_PATH $SERIALIZED_ARTICLES_PATH $SPARK_MASTER_IP PDBID_FalsePositives.csv <br />
    3. Additional Options for cluster mode <br />
        It is recommended to run with these options in cluster mode <br />

        --conf spark.driver.memory=10g    | Sets the driver memory (the program that launches spark jobs)
        --conf spark.executor.memory=10g  | Sets memory for the node-specific processes that do the bulk processing work
        --conf spark.akka.frameSize=25000 | The max size of objects that are communicated to the driver node (specfically on collect tasks)
        --conf spark.akka.timeout=300     | The amount of time to wait for objects to be transmitted to driver before failing the task

Additional Notes :
    The following build uses spark 1.1.0, and java 8. I used java 8 for it's support for lambda expressions which make writing map functions much simpler.
    However I also use the retrolambda plugin, to allow lambda expressions to be compiled into bytecode that is interpreted by earlier versions of the JVM i.e. Java 7.
    Thus the program can be deployed on clusters supporting java 7 if necessary and java 8.