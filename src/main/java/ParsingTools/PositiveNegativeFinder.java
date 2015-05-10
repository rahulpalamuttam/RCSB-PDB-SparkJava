package ParsingTools;

import PDBTools.PDBDictionary;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *
 * Used to search through content, and find positive and negative ID's given a dictionary
 * Created by rahul on 5/9/15.
 */
public class PositiveNegativeFinder {
    static Pattern PATTERN = Pattern.compile("\\b(([1-9][a-z0-9]{3})|([1-9][A-Z0-9]{3}))\\b");
    public static Set<String> FindNegatives(String Content, PDBDictionary dictionary, Date datePublished){
        Set<String> NegativeSet = new HashSet<>();
        Matcher matching = PATTERN.matcher(Content);
        while(matching.find()){
            String match = matching.group();
            if(dictionary.isNotReleased(match, datePublished)) NegativeSet.add(matching.group());
        }
        return NegativeSet;
    }

    public static Set<String> FindPositives(String Content, PDBDictionary dictionary, Date datePublished){
        Set<String> PositiveSet = new HashSet<>();
        Matcher matching = PATTERN.matcher(Content);
        while(matching.find()){
            String match = matching.group();
            if(dictionary.isReleased(match, datePublished)) PositiveSet.add(matching.group());
        }
        return PositiveSet;
    }
}
