package PDBTools;

import java.util.Date;
import java.util.HashMap;

/**
 * Dictionary of PDB ID's. Each ID name is paired
 * with a PDBId object which consists of the ID metadata.
 * Since PDBDictionary is a subclass of HasMap it is also
 * serializable and should be used for broadcasting purposes.
 * Created by rahul on 3/16/15.
 */
public class PDBDictionary extends HashMap<String, PDBId> {
    //private static final long serialVersionUID = 140605814607823206L;

    /**
     * Creates a new PDBid object and inserts it into the dictionary
     *
     * @param name       the upper case name of the PDB Id
     * @param deposition the deposition date of the Id
     */
    public void put(String name, Date deposition) {
        put(name.toUpperCase(), new PDBId(name, deposition));
    }

    /**
     * Checks if a PDB ID is not released.
     * Return false if the PDB ID is current or
     * does not exist.
     *
     * @param value the value
     * @return the boolean
     */
    public boolean isNotReleased(String value, Date date) {
        String upperCaseValue = value.toUpperCase();
        PDBId id = get(upperCaseValue);
        // this is to ensure that the id is in the table
        if (id == null) {
            return false;
        }
        return id.isNotReleased(date);
    }

    /**
     * Checks if a PDB ID is released.
     * returns false if the PDB ID is not released
     * or does not exist.
     */
    public boolean isReleased(String value, Date date) {
        String upperCaseValue = value.toUpperCase();
        PDBId id = get(upperCaseValue);
        if (id != null) {
            return id.isReleased(date);
        }

        return false;
    }
}
