package PDBTools;

import java.io.Serializable;
import java.util.Date;

/**
 * Class meant to store information about PdbIds.
 * Currently stores <IDNAME, date released, doi>.
 * For PDB ID's that are obsolete or unreleased they
 * will have a null date and doi field.
 *
 * @author Rahul Palamuttam
 */
public class PDBId implements Serializable {
    private static final int MAXLISTSIZE = 500;
    private static final int FNV_32_INIT = 0x811c9dc5;
    private static final int FNV_32_PRIME = 0x01000193;
    private String Value;
    private Date DepositionDate;

    /**
     * Instantiates a new Pdb Id.
     *
     * @param Id        the id
     * @param doiString the doi string
     * @param date      the date
     */
    public PDBId(String Id, Date date) {
        Value = Id;
        DepositionDate = date;
    }

    /**
     * Checks if the PdbId is current.
     * It is not current if a release date has not been set.
     * That it is not released or it has been obsolete.
     *
     * @return whether it is Released
     */
    public boolean isReleased() {
        return DepositionDate != null;
    }

    /**
     * Overloaded method. Checks if the PdbId is current
     * given the date. If the input date is after the release date
     * then the PdbId has been released for that date.
     * If the release date is null then it also means an unreleased Id.
     *
     * @param date the date to check if it was released
     * @return whether it has been released at the date
     */
    public boolean isReleased(Date date) {
        if (DepositionDate == null || date == null) return false;
        return date.after(DepositionDate);
    }

    public boolean isNotReleased(Date date) {
        if (DepositionDate == null || date == null) return false;
        return date.before(DepositionDate);
    }

    public String IdName() {
        return Value;
    }

    public String toString() {
        return Value + DepositionDate;
    }

    public int hashCode() {
        int hash = FNV_32_INIT;
        final int len = Value.length();
        for (int i = 0; i < len; i++) {
            hash *= FNV_32_PRIME;
            hash += Value.charAt(i);
        }
        return Math.abs(hash);
    }

    public boolean equals(PDBId id) {
        return id.Value.equals(this.Value) && id.DepositionDate.equals(this.DepositionDate);
    }
}