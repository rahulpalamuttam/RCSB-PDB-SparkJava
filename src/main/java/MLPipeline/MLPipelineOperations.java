package MLPipeline;

import FeatureVector.JournalFeatureVector;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.classification.*;
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics;
import org.apache.spark.mllib.evaluation.binary.BinaryClassificationMetricComputer;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.rdd.RDD;
import scala.Tuple2;

import java.util.Set;

/**
 * Created by rahul on 5/9/15.
 */
public class MLPipelineOperations {
    /**
     * Takes a journal feature vector and classifies it. The produced vector has its NaiveBayesPrediction field set to true or false,
     * and has it's SVMScore computed and set, based on the hashingTf scheme for vectorization, NaiveBayesModel, and SVMModel parameters.
     * @param Jvect JournalFeatureVector to be classified
     * @param NaiveBayesmodel trained bayesian model used for classification
     * @param hashingTf uses the hashing trick to produce feature vector of size 2 ^ 20
     * @param SVMModel trained SVM model used for classification
     * @return
     */
    public static JournalFeatureVector ClassifyVector(JournalFeatureVector jvect, NaiveBayesModel naiveBayesModel, HashingTF hashingTF, SVMModel svmModel){
        Vector vectorPointHash = hashingTF.transform(jvect.Vector());
        double SVMScore = svmModel.predict(vectorPointHash);
        double NaiveBayesScore = naiveBayesModel.predict(vectorPointHash);
        jvect.setScores(SVMScore, NaiveBayesScore);
        return jvect;
    }

    /**
     * Takes the labeled negative and positive training set of labeled point vectors and trains both SVM and NiaveBayes
     * @param ClassificationModel[0] the training set
     * @param ClassificationModel[1] the number of SVM iterations
     * @return
     */
    public static ClassificationModel[] Trainer(JavaRDD<LabeledPoint> training, int SVMIterationCount) {
        NaiveBayesModel naiveBayesModel = NaiveBayes.train(training.rdd(), 1.0);
        SVMModel svmModel = SVMWithSGD.train(training.rdd(), SVMIterationCount).clearThreshold();
        ClassificationModel[] models = new ClassificationModel[]{naiveBayesModel, svmModel};
        return models;
    }

    /**
     * Takes a test RDD  of LabeledPoints (label = 0.0 or 1.0, hashedVector) and trained Naive Bayes and SVM models.
     * Runs the classifiers on the test vectors and outputs the accuracy for both classifiers.
     * The NaiveBayes accuracy is the number of correct predictions divided by total test set.
     * The SVM accuracy is the area under the Received operating characteristic curve (ROC)
     * @param naiveBayesModel
     * @param svmModel
     * @param test
     * @return
     */
    public static Double[] Tester(NaiveBayesModel naiveBayesModel, SVMModel svmModel, JavaRDD<LabeledPoint> test){
        Double positives = 1.0 * test.filter(p -> p.label() == 1.0).count();
        Double negatives = 1.0 * test.filter(p -> p.label() == 0.0).count();

        JavaRDD<Tuple2<Object, Object>> SVMPredictionAndLabel = test.map(p -> (new Tuple2<Object, Object>(svmModel.predict(p.features()), p.label())));
        JavaRDD<Tuple2<Object, Object>> NaiveBayesPredictionAndLable = test.map(p -> new Tuple2<Object, Object>(naiveBayesModel.predict(p.features()), p.label()));

        BinaryClassificationMetrics SVMMetrics = new BinaryClassificationMetrics(JavaRDD.toRDD(SVMPredictionAndLabel));
        Double SVMRoc = SVMMetrics.areaUnderROC();
        Double SVMPRoc = SVMMetrics.areaUnderPR();

        BinaryClassificationMetrics NaiveMetrics = new BinaryClassificationMetrics(JavaRDD.toRDD(NaiveBayesPredictionAndLable));
        Double NaiveRoc = NaiveMetrics.areaUnderROC();
        Double NaivePRoc = NaiveMetrics.areaUnderPR();
        Double NaiveBayesAccuracy = 1.0 * NaiveBayesPredictionAndLable.filter(x -> x._1() == x._2()).count() / test.count();

        return new Double[]{positives, negatives, SVMRoc, SVMPRoc, NaiveBayesAccuracy, NaiveRoc, NaivePRoc};
    }
}
