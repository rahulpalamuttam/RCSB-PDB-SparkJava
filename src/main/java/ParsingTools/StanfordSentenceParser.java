package ParsingTools;

import java.io.Serializable;


/**
 * Created by rahul on 3/16/15.
 */

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Uses the Stanford NLP library to split a body of text into sentences
 */

public class StanfordSentenceParser implements Serializable {
    public static Properties props;
    public static StanfordCoreNLP pipeline;

    static {
        props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, cleanxml");
        pipeline = new StanfordCoreNLP(props);
    }

    /**
     * Takes in a string body and returns the sentences in the body.
     *
     * @param body
     * @return
     */

    public static List<String> SplitIntoSentences(String body) {

        // Remove strings that apparently have no closed tags or newlines
        Annotation annotation = new Annotation(body.replaceAll("\\r\\n|\\r|\\n", " ")
                .replaceAll("rdf|License|pub-id|article-id|object-id|Work|issue-id|related-article|ext-link|aff|xref|italic", ""));

        pipeline.annotate(annotation);
        // An Annotation is a Map and you can get and use the // various analyses individually. For instance, this
        // gets the parse tree of the 1st sentence in the text.
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        ArrayList<String> result = new ArrayList<>();
        if (sentences != null)
            for (CoreMap sentence : sentences) {
                result.add(sentence.toString());
            }
        return result;
    }
}
