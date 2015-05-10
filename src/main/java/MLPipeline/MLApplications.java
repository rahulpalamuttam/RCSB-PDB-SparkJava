package MLPipeline;

import FeatureVector.JournalFeatureVector;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.classification.ClassificationModel;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.mllib.regression.LabeledPoint;
import scala.Tuple2;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rahul on 5/9/15.
 */
public class MLApplications {

    /**
     * Application takes an Array of JournalFeatureVectors to classify. It also uses the AbbreviationTable array which consist of
     * mapped (Journal ID, PDB ID) pairs that are known to be abbreviations.
     * @param lineRDD the Array of JournalFeatureVectors to compute
     * @param sparkContext main entry point for the spark functionality, and is used to connect to the cluster and perform compute operations
     * @param SVMIterationCount desired number of times for SVM to iterate
     * @param AbbreviationTable array of (Journal ID, PDB ID) pairs that correspond to commonly abbreviated ID's
     * @return
     */
    public static List<JournalFeatureVector> Application1(JavaRDD<JournalFeatureVector> lineRDD, int SVMIterationCount, String[][] AbbreviationTable){
        // separate the RDD's
        JavaRDD<JournalFeatureVector> posVectRDD = lineRDD.filter(vect -> vect.isPositive());
        JavaRDD<JournalFeatureVector> negVectRDD = lineRDD.filter(vect -> vect.isNegative());
        JavaRDD<JournalFeatureVector> undVectRDD = lineRDD.filter(vect -> vect.isUndetermined());

        HashingTF hashingTF = new HashingTF();
        //maps each sentence to LabeledPoint : (label, vector)
        JavaRDD<LabeledPoint> positiveRDD = posVectRDD.map(vector -> new LabeledPoint(1.0, hashingTF.transform(vector.Vector())));
        JavaRDD<LabeledPoint> negativeRDD = negVectRDD.map(vector -> new LabeledPoint(0.0, hashingTF.transform(vector.Vector())));
        JavaRDD<LabeledPoint> predeterminedSet = positiveRDD.union(negativeRDD);

        //randomly generate a training set and test set from data
        JavaRDD<LabeledPoint>[]  splits = predeterminedSet.randomSplit(new double[]{.35, .65});
        JavaRDD<LabeledPoint> training = splits[0];
        JavaRDD<LabeledPoint> test = splits[1];

        // train the models on the training set
        ClassificationModel[] models= MLPipelineOperations.Trainer(training, SVMIterationCount);
        NaiveBayesModel naiveBayesModel = (NaiveBayesModel)models[0];
        SVMModel svmModel = (SVMModel)models[1];

        //test the models on the test set and collect results
        Double[] testResults = MLPipelineOperations.Tester(naiveBayesModel, svmModel, test);
        double labeledPositive = testResults[0];
        double labeledNegative = testResults[1];
        double svmRocAU = testResults[2];
        double svmPRAU = testResults[3];
        double naiveRocAU = testResults[4];
        double naivePRocAU = testResults[5];
        double naiveAccuracy = testResults[6];

        FileWriter output = null;
        try {
            output = new FileWriter(new File("Metrics"));
            output.write("Number of Pre-Labeled Positives" + labeledPositive + "\n");
            output.write("Number of Pre-Labeled Negatives" + labeledNegative + "\n");
            output.write("SVM ROC area under curve: " + svmRocAU + "\n");
            output.write("SVM PR area under curve: " + svmPRAU + "\n");
            output.write("Naive ROC area under curve: " + naiveRocAU + "\n");
            output.write("Naive PR area under curve: " + naivePRocAU + "\n");
            output.write("Naive Accuracy :" + naiveAccuracy + "\n");
            output.close();
        } catch (Exception IO){
            System.out.println("An IO exception happened when writing Metrics");
        }

        //classifies the positive, negative, and undetermined JournalFeatureVectors
        JavaRDD<JournalFeatureVector> classifiedPositive = posVectRDD.map(p -> MLPipelineOperations.ClassifyVector(p, naiveBayesModel, hashingTF, svmModel));
        JavaRDD<JournalFeatureVector> classifiedNegative = negVectRDD.map(p -> MLPipelineOperations.ClassifyVector(p, naiveBayesModel, hashingTF, svmModel));
        JavaRDD<JournalFeatureVector> classifiedUndeterm = undVectRDD.map(p -> MLPipelineOperations.ClassifyVector(p, naiveBayesModel, hashingTF, svmModel));

        // collects the results
        List<JournalFeatureVector> positives = classifiedPositive.collect();
        List<JournalFeatureVector> negatives = classifiedNegative.collect();
        List<JournalFeatureVector> undeterms = classifiedUndeterm.collect();

        //saves the results to corresponding csv files
        SaveJVectToCSV(positives, "PositiveSentences.csv");
        SaveJVectToCSV(negatives, "NegativeSentences.csv");
        SaveJVectToCSV(undeterms, "UndeterminedSentences.csv");

        List<JournalFeatureVector> allCollected = new ArrayList<>();
        allCollected.addAll(positives);
        allCollected.addAll(negatives);
        allCollected.addAll(undeterms);

        FalsePositiveCompute(negVectRDD, posVectRDD, AbbreviationTable, hashingTF, svmModel, naiveBayesModel, "Negatives", true);
        FalsePositiveCompute(undVectRDD, posVectRDD, AbbreviationTable, hashingTF, svmModel, naiveBayesModel, "Undetermined", true);
        FalsePositiveCompute(negVectRDD, posVectRDD, AbbreviationTable, hashingTF, svmModel, naiveBayesModel, "Negatives", false);
        return allCollected;
    }

    /**
     * Takes the undetermined set and the positive vector set.
     * Searches for (Journal Name, ID) pairs in the Abbreviations Table.
     * Uses hashingTF to vectorize the sentence i.e the JournalFeatureVector.content
     *
     * @param undVectRDD
     * @param posVectRDD
     * @param AbbreviationsTable
     * @param hashingTf
     * @param SVMModel
     * @param NaiveBayesmodel
     */

    public static void FalsePositiveCompute(JavaRDD<JournalFeatureVector> negVectRDD, JavaRDD<JournalFeatureVector> posVectRDD, String[][] AbbreviationTable, HashingTF hashingTF, SVMModel svmModel, NaiveBayesModel naiveBayesModel, String negType, Boolean inTable){
        JavaRDD<JournalFeatureVector> negativeAbbreviations = negVectRDD.filter(vect -> isInAbbreviationTable(vect, AbbreviationTable ) == inTable);
        JavaRDD<JournalFeatureVector> positiveAbbreviations = posVectRDD.filter(vect -> isInAbbreviationTable(vect, AbbreviationTable) == inTable);
        JavaRDD<JournalFeatureVector> AllAbbreviations = negVectRDD.union(posVectRDD);
        // The RDD of false positives with predetermined false value
        JavaRDD<LabeledPoint> NegativeAbbreviationRDD = negativeAbbreviations.map(vector -> new LabeledPoint(0.0, hashingTF.transform(vector.Vector())));
        JavaRDD<LabeledPoint> PositiveAbbreviationRDD = positiveAbbreviations.map(vector -> new LabeledPoint(1.0, hashingTF.transform(vector.Vector())));
        JavaRDD<LabeledPoint> test = NegativeAbbreviationRDD.union(PositiveAbbreviationRDD);

        //test the models on the Abbreviation-test set
        Double[] testResults = MLPipelineOperations.Tester(naiveBayesModel, svmModel, test);
        double labeledPositive = testResults[0];
        double labeledNegative = testResults[1];
        double svmRocAU = testResults[2];
        double svmPRAU = testResults[3];
        double naiveRocAU = testResults[4];
        double naivePRocAU = testResults[5];
        double naiveAccuracy = testResults[6];

        FileWriter output = null;
        try {
            output = new FileWriter(new File(negType + "+PositiveAbbreviation_Metrics"));
            output.write("Number of Pre-Labeled Positives" + labeledPositive + "\n");
            output.write("Number of Pre-Labeled Negatives" + labeledNegative + "\n");
            output.write("SVM ROC area under curve: " + svmRocAU + "\n");
            output.write("SVM PR area under curve: " + svmPRAU + "\n");
            output.write("Naive ROC area under curve: " + naiveRocAU + "\n");
            output.write("Naive PR area under curve: " + naivePRocAU + "\n");
            output.write("Naive Accuracy :" + naiveAccuracy + "\n");
            output.close();
        } catch (Exception IO){
            System.out.println("An IO exception happened when writing Metrics");
        }

        //classifies the positive, negative, and undetermined JournalFeatureVectors
        JavaRDD<JournalFeatureVector> classifiedPositive = AllAbbreviations.map(p -> MLPipelineOperations.ClassifyVector(p, naiveBayesModel, hashingTF, svmModel));
        List<JournalFeatureVector> falsePositives = classifiedPositive.collect();
        SaveJVectToCSV(falsePositives, negType + "+PositiveAbbreviationWithSVM.csv");
        CompositeScorer.RunCompositeScorer(falsePositives, negType + "+PositiveSVMCompositeScorer.csv");
    }

    /**
     * Saves the corresponding collection of JournalFeatureVectors to a CSV file
     * @param vectors
     * @param CSVFileName
     */
    public static void SaveJVectToCSV(List<JournalFeatureVector> vectors, String CSVFileName){
        FileWriter pw = null;
        try{
            pw = new FileWriter(new File(CSVFileName));
            pw.write("File name, Date Published, Text, Word Set, PDBPattern, Positive, PositiveIds, NegativeIds, Prediction Naive Bayes, SVM Score");
            for(JournalFeatureVector vector : vectors)
            {
                pw.write(vector.toString());
                pw.flush();
            }
            pw.close();
        } catch (Exception IO){
            System.out.println("Caught IO exception writing to" + CSVFileName + "\n");
            IO.printStackTrace();
        }
    }

    public static Boolean isInAbbreviationTable(JournalFeatureVector vector, String[][] AbbreviationsTable) {
        for (int i = 0; i < AbbreviationsTable.length; i++) {
            if (vector.Name.contains(AbbreviationsTable[i][0])) {
                for (String positive : vector.Positives) {
                    if (positive.contains(AbbreviationsTable[i][1])) return true;
                }
                for (String negative : vector.Negatives) {
                    if (negative.contains(AbbreviationsTable[i][1])) return true;
                }
            }
        }
        return false;
    }
}
