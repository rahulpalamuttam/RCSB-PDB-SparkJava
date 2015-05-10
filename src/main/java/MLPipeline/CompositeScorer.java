package MLPipeline;

import FeatureVector.JournalFeatureVector;
import com.google.common.collect.HashMultimap;
import org.apache.commons.collections.MultiMap;
import scala.Tuple2;
import scala.Tuple4;
import scala.Tuple5;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by rahul on 5/9/15.
 */
public class CompositeScorer {
    /**
     * The composite scorer generates pairs of Journals, IDs mapped to a composite SVM score
     * The scores are also averaged out per instance - to give an idea of which ID's give an overall stronger score
     * Created by rahul on 4/14/15.
     */
    public static void RunCompositeScorer(List<JournalFeatureVector> vectors, String fileName){
        /**
         * (Journal Name, ID, SVM Score, count)
         */

        List<Tuple4<String, String, Double, Double>> Table = new ArrayList<>();
        for(JournalFeatureVector vector : vectors){
            for(String positive : vector.Positives){
                if(positive.length() > 0) Table.add(new Tuple4<String, String, Double, Double>(vector.Name, positive, vector.SVMScore, 1.0));
            }
            for(String negative : vector.Negatives){
                if(negative.length() > 0) Table.add(new Tuple4<String, String, Double, Double>(vector.Name, negative, vector.SVMScore, 1.0));
            }
        }

        /**
         * Table => group all vectors by (Journal Name, ID)
         * => reduce all grouped pairs to (Journal Name, ID, composite SVMscore, total count)
         * => map resulting tuples to (Journal Name, ID, composite SVMscore, total count, average SVMscore)
         */
        HashMultimap<Tuple2<String, String>, Tuple4<String, String, Double, Double>> GroupedTuples = HashMultimap.create();
        for(Tuple4<String, String, Double, Double> tuple : Table){
            Tuple2<String, String> JNameAndID = new Tuple2<>(tuple._1(), tuple._2());
            GroupedTuples.put(JNameAndID, tuple);
        }

        List<Tuple5<String, String, Double, Double, Double>> solutionMatrix = new ArrayList<>();
        for(Tuple2<String, String> key : GroupedTuples.keySet()){
            Double SVMSum = 0.0;
            Double count = 0.0;
            for(Tuple4<String, String, Double, Double> value : GroupedTuples.get(key)){
                SVMSum += value._3();
                count += value._4();
            }
            solutionMatrix.add(new Tuple5<String, String, Double, Double, Double>(key._1(), key._2(), SVMSum, count, SVMSum / count));
        }

        PrintWriter pw = null;
        try{
            pw = new PrintWriter(new File(fileName));
            pw.write("Journal Name, ID, Composite SVM score, Total number of occurrences, Avg SVM Score per occurrence\n");
            for(Tuple5<String, String, Double, Double, Double> row : solutionMatrix){
                pw.write((row.toString().replace("( ", "") + "\n").replace(")\n", "\n"));
                pw.flush();
            }
            pw.close();
        } catch (Exception io){
            io.printStackTrace();
        }

        System.out.println(solutionMatrix.size());
    }
}
