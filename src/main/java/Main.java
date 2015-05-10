import FeatureVector.JournalFeatureVector;
import MLPipeline.MLApplications;
import PDBTools.PDBDictionary;
import PDBTools.PDBFinder;
import PDBTools.PdbIdSourceDownloader;
import ParsingTools.FileNameDataParser;
import ParsingTools.StanfordSentenceParser;
import com.google.common.io.Files;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import scala.Tuple2;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by rahul on 5/9/15.
 */
public class Main {
    File ARTICLE_SERIALOBJECT = new File("Articles.txt.object.ser");
    int SVM_ITERATIONCOUNT = 1;

    public static void main(String[] args) {
        System.out.println("Hello World");
        System.out.println("Files will be read from this directory: " + args[0]);
        System.out.println("If it exists the object file will be deserialized here: " + args[1]);
        System.out.println("The spark cluster mode: " + args[2]);
        System.out.println("The false positives table will be read from here: " + args[3]);

        String[][] AbbreviationsTable = FalsePositiveTableGenerator(new File(args[3]));
        PDBDictionary Dictionary = PdbIdSourceDownloader.getPdbHashTable();
        System.out.println("Hashtable Done");
        SparkConf conf = new SparkConf().setAppName("PDB").setMaster(args[2]);
        JavaSparkContext sparkContext = new JavaSparkContext(conf);
        Broadcast<PDBDictionary> DictionaryBroadcast = sparkContext.broadcast(Dictionary);
        // RDD of (K, V) pairs mapped to (File Name, File Body)
        JavaPairRDD<String, String> FileTable = sparkContext.wholeTextFiles("file://" + args[0]).filter(p -> PDBFinder.getPdbMatchType(p._2()) != null);

        JavaRDD<Tuple2<String, String>> SentenceTable = FileTable.flatMap(p -> FileToSentencesTransform(p._1(), p._2()));
        List<Tuple2<String, String>> IntermediateFiltered = SentenceTable.collect();

        JavaRDD<JournalFeatureVector> FeatureVectorRDD = sparkContext.parallelize(IntermediateFiltered, 100).map(T -> new JournalFeatureVector(T._1(), T._2(), DictionaryBroadcast));

       List<JournalFeatureVector> collectedVectors = MLApplications.Application1(FeatureVectorRDD, 1, AbbreviationsTable);
        MLPipeline.CompositeScorer.RunCompositeScorer(collectedVectors, "SVMCompositeScorer.csv");
    }

    /**
     * Transforms a fileName, Array[Byte] (which is the body of the file in byte form).
     * to the (fileName, sentence) which are the individual sentences that compose the file.
     * The Stanford CoreNLP library is used to split sentences
     *
     * @param name
     * @param body
     * @return
     */
    public static List<Tuple2<String, String>> FileToSentencesTransform(String name, String body) {
        List<String> FileBody = StanfordSentenceParser.SplitIntoSentences(body);
        ArrayList<Tuple2<String, String>> tuplePairs = new ArrayList<Tuple2<String, String>>();
        for (String sentence : FileBody) {
            if (PDBFinder.getPdbMatchType(sentence) != null) {
                tuplePairs.add(new Tuple2<String, String>(FileNameDataParser.ParseName(name), sentence));
            }
        }
        return tuplePairs;
    }

    /**
     * Returns an array of 2 dimensional arrays.
     * The 2 dimensional arrays are meant to mimic the tuple pair of (Journal Name ID, and PDB-ID)
     * The Journal Name is parsed, such that there are no spaces read - just the text.
     *
     * @param file
     * @return
     */
    public static String[][] FalsePositiveTableGenerator(File file) {
        String table = null;
        try {
            table = Files.toString(file, Charset.defaultCharset());
        } catch (Exception fileIO) {
            System.out.println("A File IO Exception happened loading the FalsePositive Table");
            System.exit(1);
        }
        String[] lines = table.split("\n");
        String[][] TableTuple = new String[lines.length][];
        for (int i = 0; i < lines.length; i++) {
            TableTuple[i] = lines[i].split(",");
            TableTuple[i][0] = TableTuple[i][0].replaceAll(" ", "");
        }

        return TableTuple;
    }
}