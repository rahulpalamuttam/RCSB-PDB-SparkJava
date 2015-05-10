package PDBTools;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by rahul on 3/16/15.
 */
public class PDBFinder {
    private static final long serialVersionUID = 1L;

    //	private static final Pattern SENTENCE_PATTERN = Pattern.compile("\\.\\s[A-Z]");
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("\\.\\s[^.!?/\\s\\n)]");

    /*
     * General pattern to match a pdb id. Note, we only allow all upper case or all lower case versions
     * Examples: 1XYZ, 1xyz, 1x2z, 123z (need to exclude integers)
     */
    private static final Pattern PDB_PATTERN = Pattern.compile("[1-9]([A-Z0-9]{3}|[a-z0-9]{3})");
    private static final Pattern ID_PATTERN = Pattern.compile("\\b(PDB ID:|pdb id:|PDBID|pdbid|PDB_ID|pdb_id)");
    private static final Pattern PDB_ID_PATTERN = Pattern.compile(ID_PATTERN.pattern() + "." + PDB_PATTERN.pattern() + "\\b");
    private static final Pattern PDB_DOI_PATTERN = Pattern.compile("10.2210/pdb" + PDB_PATTERN.pattern() + "/pdb");
    private static final Pattern PDB_FILE_PATTERN = Pattern.compile(PDB_PATTERN.pattern() + ".pdb");
    private static final Pattern RCSB_PDB_URL_PATTERN = Pattern.compile("http://www.rcsb.org/pdb/explore.do?structureId=" + PDB_PATTERN.pattern() + "\\b");
    private static final Pattern PDB_NONE_PATTERN = Pattern.compile("\\b" + PDB_PATTERN.pattern() + "\\b");
    /*
     * PDB ID in href='...."
     */
    private static final Pattern PDB_EXT_LINK_PATTERN = Pattern.compile("ext-link-type=\"pdb\" xlink:href=\""
            + PDB_PATTERN.pattern()
            + "\">" + PDB_PATTERN.pattern());

    /*
     * PDB skip one word pattern: PDB code 1XYZ, pdb code: 1xyz, PDB assession 1xyz, ...
     */
    private static final Pattern PDB_SKIP_0_PATTERN = Pattern.compile("\\b(pdb|PDB)\\W*" + PDB_PATTERN.pattern() + "\\b");
    private static final Pattern PDB_SKIP_1_PATTERN = Pattern.compile("\\b(pdb|PDB)\\W*\\w+\\W*" + PDB_PATTERN.pattern() + "\\b");
    private static final Pattern PDB_SKIP_2_PATTERN = Pattern.compile("\\b(pdb|PDB)\\W*\\w+\\W*\\w+\\W*" + PDB_PATTERN.pattern() + "\\b");
    /*
     * 4-digit integer regular expression
     */
    private static final Pattern DIGITS = Pattern.compile("\\d{4}");
    private static Map<String, Pattern> patterns = new LinkedHashMap<String, Pattern>();

    static {
        patterns.put("PDB_ID", PDB_ID_PATTERN);
        patterns.put("PDB_DOI", PDB_DOI_PATTERN);
        patterns.put("PDB_FILE", PDB_FILE_PATTERN);
        patterns.put("RCSB_PDB_URL", RCSB_PDB_URL_PATTERN);
        patterns.put("PDB_EXT_LINK", PDB_EXT_LINK_PATTERN);
        patterns.put("PDB_SKIP_0", PDB_SKIP_0_PATTERN);
        patterns.put("PDB_SKIP_1", PDB_SKIP_1_PATTERN);
        patterns.put("PDB_SKIP_2", PDB_SKIP_2_PATTERN);
        patterns.put("PDB_NONE", PDB_NONE_PATTERN);
    }

    public static String getPdbMatchType(String line) {
        for (Map.Entry<String, Pattern> pattern : patterns.entrySet()) {
            if (pattern.getValue().matcher(line).find()) {
                return pattern.getKey();
            }
        }
        return null;
    }

    public static boolean isPositivePattern(String type) {
        return (type != null && !(type.equals("PDB_NONE")));
    }

}
