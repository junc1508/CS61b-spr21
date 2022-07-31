package gitlet;


import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import gitlet.Utils;

/** Blob saves a snapshot of the file. The id of Blob is SHA-1, which is generated from file name + file content;
 * Without file name, 2 files with same content will share the same SHA-1.
 * the content of Blob is the content of the file. */
public class Blob implements Serializable {
    /** id of Blob. */
    private String blobID;
    /** file name. */
    private String fileName;
    /** content of Blob in byte. */
    private byte[] blobContent;

    /** constructor of Blob. */
    public Blob(File file){
        this.blobContent = Utils.readContents(file);
        this.fileName = file.getName();
        this.blobID = generateBlobID();
    }
    /** generate SHA-I id for blob. */
    public String generateBlobID(){
        return Utils.sha1(this.fileName, this.blobContent);
    }
    /** return the id of the blob. */
    public String getBlobID(){
        return blobID;
    }
    /** return the content of the blob. */
    public byte[] getContent(){
        return blobContent;
    }
    /** save blob file. */
    public void save(){
        File blobFile = Utils.join(Repository.BLOB_DIR, getBlobID());
        Utils.writeObject(blobFile, this);
    }
    /** Read Blob file. */
    public static Blob fromFile(String blobID){
        File f = Utils.join(Repository.BLOB_DIR, blobID);
        return Utils.readObject(f, Blob.class);
    }



}
