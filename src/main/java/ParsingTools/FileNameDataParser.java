package ParsingTools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
/**
 * Created by rahul on 5/9/15.
 */
public class FileNameDataParser {
    public static Parser parser = new Parser();
    public static Pattern yearPattern = Pattern.compile("(1[8-9][0-9][0-9])|(20[0-1][0-9])");

    public static String ParseName(String Name){
        String[] directories = Name.split("/");
        int last = directories.length - 1;
        return directories[last];
    }

    public static Date ParseDateFromFile(String Name){
        String year = ParseYear(Name);
        int index = Name.indexOf(year);
        List<DateGroup> DateGroups = parser.parse(Name.substring(index).replaceAll("_", " "));
        if(year != "" && DateGroups.size() > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateGroups.get(0).getDates().get(0));
            calendar.set(Calendar.YEAR, Integer.parseInt(year));
            return calendar.getTime();
        }
        return null;
    }

    public static String ParseYear(String Name) {
        Matcher matches = yearPattern.matcher(Name);
        ArrayList<String> years = new ArrayList<>();
        while(matches.find()){
            years.add(matches.group());
        }
        return years.get(years.size() - 1);
    }
}
