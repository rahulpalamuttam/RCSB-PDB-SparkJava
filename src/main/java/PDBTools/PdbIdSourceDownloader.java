package PDBTools;

import PersistenceTools.ObjectSerializer;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;

import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * Downloads the PDB IDs from the source URLS.
 * Downloads the current, obsolete, and unreleased IDs.
 * Outputs each PDB ID in a tuple format consisting of
 * <PDB ID, (current|obsolete|unreleased), deposition date>
 * <p/>
 * Current entries are released entries and are being used
 * Obsolete entries are no longer used as their deposition date is not being used
 * Unreleased ID's have yet to be used but they still have a deposition date
 * The class uses the opensource jacabi xml parsing library for it's brevity
 *
 * @author Rahul Palamuttam
 * source http://www.rcsb.org/pdb/rest/customReport.xml?pdbids=*&customReportColumns=structureId,releaseDate,pdbDoi
 * source http://www.rcsb.org/pdb/rest/getObsolete
 * source http://www.rcsb.org/pdb/rest/getUnreleased
 * source http://xml.jcabi.com/
 * <p/>
 * TODO :: This class needs to be decoupled. It downloads PDB data and builds the dictionary.
 */

public class PdbIdSourceDownloader {
    private static String HASHSERIALFILE = "PDBIDDictionary.ser";
    private static String CURRENT_URL = "http://www.rcsb.org/pdb/rest/customReport.xml?pdbids=*&customReportColumns=structureId,depositionDate,pdbDoi";
    private static String OBSOLETE_URL = "http://www.rcsb.org/pdb/rest/getObsolete";
    private static String UNRELEASED_URL = "http://www.rcsb.org/pdb/rest/getUnreleased";
    private static List<XML> currentRecords;
    private static List<XML> unreleasedRecords;
    private static List<XML> obsoleteRecords;


    /**
     * Helper method to load the currently released
     * PDB records from the XML webpage at CURRENT_URL.
     */
    private static void loadCurrentRecords() {
        XML document;
        try {
            URL curr = new URL(CURRENT_URL);
            document = new XMLDocument(curr);
            currentRecords = document.nodes("//record");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to load the Unreleased PDBID records
     * from the XML webpage at UNRELEASED_URL.
     */
    private static void loadUnreleasedRecords() {
        XML document;
        try {
            URL curr = new URL(UNRELEASED_URL);
            document = new XMLDocument(curr);
            unreleasedRecords = document.nodes("//record");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to load the Obsolete PDBID records information
     * from the XML webpage at the OBSOLETE_URL.
     * <p/>
     * This method is deprecated since it
     */
    private static void loadObsoleteRecords() {
        XML document;
        try {
            URL curr = new URL(OBSOLETE_URL);
            document = new XMLDocument(curr);
            obsoleteRecords = document.nodes("//PDB");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Gets pdb hash table.
     * Constructs a PdbHashTable object based on
     * the XML structure lists. The PDBID's are parsed from the
     * XML structures. If the PdbHashTable serialized object
     * is saved in HASHSERIALFILE then the HashTable is loaded back
     * from the saved file. Otherwise it downloads, parses the XML webpages,
     * and constructs the hashtable.
     *
     * @return the pdb hash table
     */
    public static PDBDictionary getPdbHashTable() {
        PDBDictionary Dictionary;
        File SerialObjectFile = new File(HASHSERIALFILE);
        if (SerialObjectFile.exists()) {
            Dictionary = (PDBDictionary) ObjectSerializer.loadObject(HASHSERIALFILE);
        } else {
            loadCurrentRecords();
            loadUnreleasedRecords();
            Dictionary = new PDBDictionary();
            putCurrentInHashTable(Dictionary);
            putUnreleasedInHashTable(Dictionary);
            ObjectSerializer.writeObject(Dictionary, HASHSERIALFILE);
        }
        return Dictionary;
    }


    /**
     * Helper function that parses the XML records in the currentRecords list
     * for PDBID's, releaseDate, and the pdbDoi.
     *
     * @param hashTable returns the hashtable with Current ID's
     */
    private static void putCurrentInHashTable(PDBDictionary hashTable) {
        System.out.println(currentRecords.size());
        for (XML record : currentRecords) {
            String idName = record.xpath("//dimStructure.structureId/text()").get(0);
            String date = record.xpath("//dimStructure.depositionDate/text()").get(0);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String doi = record.xpath("//dimStructure.pdbDoi/text()").get(0);
            try {
                Date example = format.parse(date);
                hashTable.put(idName, example);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Helper function that parses the XML records in the obsoleteRecords list
     * for PDBID's. There is no release date or doi associated with these.
     *
     * @param hashTable returns the hashtable with obsolete ID's added
     * @Deprecated this is obsolete because obsolete ID's are still there
     */
    private static void putObsoleteInHashTable(PDBDictionary hashTable) {
        for (XML record : obsoleteRecords) {
            String idName = record.xpath("//@structureId").get(0);
            hashTable.put(idName, (Date) null);
        }
    }

    /**
     * Helper function that parses the XML records in the unreleasedRecords list
     * for PDBID's. There is no release date or doi associated with these.
     *
     * @param hashTable returns the hashtable with unreleased ID's added
     */
    private static void putUnreleasedInHashTable(PDBDictionary hashTable) {
        for (XML record : unreleasedRecords) {
            String idName = record.xpath("//@structureId").get(0);
            String depositionDate = record.xpath("//@initialDepositionDate").get(0);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date example = format.parse(depositionDate);
                hashTable.put(idName, example);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Deletes the opened file
     */
    public static void deleteFile() {
        try {
            File HashFile = new File(HASHSERIALFILE);
            HashFile.delete();
        } catch (Exception som) {
            som.printStackTrace();
        }
    }

}