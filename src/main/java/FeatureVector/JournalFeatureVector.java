package FeatureVector;

import PDBTools.PDBDictionary;
import PDBTools.PDBFinder;
import ParsingTools.FileNameDataParser;
import ParsingTools.PositiveNegativeFinder;
import org.apache.spark.broadcast.Broadcast;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by rahul on 5/9/15.
 */
public class JournalFeatureVector implements Serializable {

    public String Name;
    public String Content;
    public Boolean Positive;
    public String referenceType;
    public Date DatePublished;
    public Set<String> Negatives;
    public Set<String> Positives;
    public Set<String> Vector;
    public Boolean NaiveBayesPrediction;
    public Double SVMScore;

    /**
     * The JournalFeatureVector class encapsulates a tuple of the following parameters.
     * The varying constructors extract the metadata from (Name, Sentence) tuples using
     * the PDBDictionary.
     * <p/>
     * This class depends onthe PDBDictionary, as well as the FileNameDataParser and the SentenceParser
     *
     * @param Name
     * @param Content
     * @param Positive
     * @param referenceType
     * @param DatePublished
     * @param Negatives
     * @param Positives
     * @param NaiveBayesPrediction
     * @param SVMScore
     */
    public JournalFeatureVector(String Name, String Content, Boolean Positive,
                                String referenceType, Date DatePublished,
                                Set<String> Negatives, Set<String> Positives,
                                Boolean NaiveBayesPrediction, Double SVMScore) {
        this.Content = Content;
        this.NaiveBayesPrediction = NaiveBayesPrediction;
        this.Name = Name;
        this.Positive = Positive;
        this.referenceType = referenceType;
        this.DatePublished = DatePublished;
        this.Negatives = Negatives;
        this.Positives = Positives;
        this.SVMScore = SVMScore;
    }

    /**
     * Constructor without bayesian and SVM Scores
     *
     * @param Name
     * @param Content
     * @param referenceType
     * @param datePublished
     * @param dictionary
     */
    public JournalFeatureVector(String Name, String Content, String referenceType, Date datePublished, PDBDictionary dictionary) {
        this(Name, Content, PDBFinder.isPositivePattern(referenceType), referenceType, datePublished, PositiveNegativeFinder.FindNegatives(Content, dictionary, datePublished), PositiveNegativeFinder.FindPositives(Content, dictionary, datePublished), false, 0.0);
    }

    /**
     * Constructor for general feature extraction given File Name, and Sentence
     *
     * @param Name
     * @param Content
     * @param dictionary
     */
    public JournalFeatureVector(String Name, String Content, Broadcast<PDBDictionary> dictionary) {
        this(Name, Content, PDBFinder.getPdbMatchType(Content), FileNameDataParser.ParseDateFromFile(Name), dictionary.value());
    }

    /**
     * Outputs the string format of the FeatureVector.
     * Commas are removed from the Content text as they have little
     * to no effect.
     * Name, Date, Content, WordSet, Positives, Negatives, Bayesian Prediction, SVMScore
     *
     * @return
     */
    public String toString() {
        String result = Name + " , " + DatePublished + " , " + Content.replaceAll(",", " ") + " , " +
                Vector().toString().replaceAll(",", " ") + " , ";
        String flags = referenceType + " , " + Positive + " , ";
        String ids = Positives.toString().replaceAll(",", " ") + " , " +
                Negatives.toString().replaceAll(",", " ") + " , " +
                NaiveBayesPrediction + " ," + SVMScore + "\n";

        return result + flags + ids;
    }

    /**
     * Replaces all PDB ID's in the content with a space char
     * Eliminates PDBID's found within substrings, and splits words
     * by those substrings - effectively adding the split components
     * as vectorizable elements.
     *
     * @return
     */
    public Set<String> Vector() {
        String solution = Content.toString();
        for (String id : Negatives) solution = solution.replace(id, " ");
        for (String id : Positives) solution = solution.replace(id, " ");
        String[] words = solution.split(" ");
        Set<String> wordVectorPointSet = new HashSet<>();
        for (String word : words) {
            if (!word.equals(" ")) wordVectorPointSet.add(word);
        }
        return wordVectorPointSet;
    }


    public Boolean isNegative() {
        return !isPositive() && Negatives.size() > 0 &&
                referenceType != null &&
                referenceType.equals("PDB_NONE");
    }

    public Boolean isPositive() {
        return Positive && Positives.size() > 0;
    }

    public Boolean isUndetermined() {
        return !isPositive() && Positives.size() > 0 &&
                referenceType != null &&
                referenceType.equals("PDB_NONE");
    }

    public void setScores(Double SVMscore, Double naiveBayesPrediction){
        this.SVMScore = SVMscore;
        this.NaiveBayesPrediction = (naiveBayesPrediction == 1.0);
    }
}


