package gitlet;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.text.SimpleDateFormat;

import java.util.Date;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Jun Chen
 */
public class Commit implements Serializable {
    /** List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private final String message;
    /** commit time. */
    private final Date currTime;
    /** mapping of blobs reference (key: SHA-1 file name) to original file names (value).
     * Have to use HashMap because a lot of functions asks for constant time */
    private final HashMap<String,String> blobList;
    /** parent reference, first */
    private final String parent;
    /** parent reference, second (For merging) */
    private final String secondParent;
    /** SHA-1 hash code. */
    private final String id;

    /** constructor for init */
    public Commit() {
        //has to instantiate in sequence,
        // cannot instantiate parent after id = generateID.
        this.message = "initial commit";
        this.parent = "";
        this.secondParent = "";
        this.currTime = new Date(0);
        this.blobList = new HashMap<>();
        this.id = generateID();

    }
    /** Constructor for commit with message.*/
    public Commit(String message,String parent, String secondParent, HashMap<String, String> blobList) {
        this.message = message;
        this.parent = parent;
        this.secondParent = secondParent;
        this.currTime = new Date();
        this.blobList = blobList;
        this.id = generateID();
    }
    /** get SHA-1 id of current commit. */
    public String getCommitID() {
        return id;
    }
    /** get parent id. */
    public String getParent() {
        return parent;
    }
    /** get second parent id. */
    public String getSecondParent() {
        return secondParent;
    }
    /** get bloblist. */
    public HashMap<String, String> getBlobList() {
        return blobList;
    }

    /** save the commit object to gitlet/commit/sha-1. */
    public void saveCommit() {
        // TODO (hint: don't forget dog names are unique)
        File commitFile = Utils.join(Repository.COMMIT_DIR, getCommitID());
        Utils.writeObject(commitFile, this);
    }
    /** read commit from SHA-ID/file name. */
    public static Commit fromFile(String commitID) {
        File commitFile = Utils.join(Repository.COMMIT_DIR, commitID);
        return Utils.readObject(commitFile,Commit.class);
    }
    /** get SHA-1 code for commit with message, time, blobList, parent commits;
     *  sha1 : parameters must be String or byte[] */
    public String generateID() {
        return Utils.sha1(this.message, generateTime(), this.blobList.toString(), this.parent, this.secondParent);
    }
    /** get first 8 characters of SHA-1 code. */
    public String getShort() {
        return id.substring(0,8);
    }
    /** Utils.sha1 only takes String or bytes.
     * So we need to convert curr-Time to String. */
    public String generateTime() {
        SimpleDateFormat formatter =new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z"); //SDF is locale sensitive.
        // formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String strDate = formatter.format(currTime);
        return strDate;
    }

    /** return current date. */
    public Date getDate() {
        return currTime;
    }
    /** return message. */
    public String getMessage() {
        return message;
    }
    /** build log information for each commit. */
    public String toLog() {

        if (secondParent.isEmpty()) {
            return String.format("=== %ncommit %s %nDate: %s%n%s%n%n",id,generateTime(),message);
        } else {
            return String.format("=== %ncommit %s %nMerge: %s %s %nDate: %s%n%s%n%n",id,parent.substring(0,8), secondParent.substring(0,8),generateTime(),message);
        }
    }

}
