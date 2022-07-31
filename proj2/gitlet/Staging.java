package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

/** data structure for staging area to store the to be added files, to be removed files and to be tracked file
 * for the add, rm and commit commands
 * the staging area stored files (key: file name, value: SHA-1 from blob)
 * staging information is saved as file name index
 * tracked files = tracked + fileToAdd + fileToRemove */
public class Staging implements Serializable {
    /** storing staged files to be modified or added, runtime constriction, choose hashMap or treeMap */
    private HashMap<String, String> fileToAdd;
    /** storing files that are removed and to be untracked */
    private HashMap<String, String> fileToRemove;
    /** cached files from previous commit; facilitates add() when file is modified, then changed back to
     * current working version */
    private HashMap<String, String> tracked;

    /** constructor for staging. */
    public Staging(){
        this.fileToAdd = new HashMap<String, String>();
        this.tracked = new HashMap<String, String>();
        this.fileToRemove = new HashMap<String, String>();
    }
    /** get the files staged for adding. */
    public HashMap<String,String> getAdd(){
        return fileToAdd;
    }
    /** get the files staged for removing. */
    public HashMap<String, String> getRemove(){
        return fileToRemove;
    }
    /** add a file to fileToAdd. */
    public void add(File file) {
        Blob fileToBlob = new Blob(file);
        String fileName = file.getName();
        String blobID = fileToBlob.generateBlobID();
        String prevID = tracked.get(fileName);

        /** if tracked is null or filename is not in previous commit,
         * the file is not previously tracked/no current working version, so should be added for commit;
         * overwrite the existing version. save fileToBlob
         */
        if (prevID == null){
            fileToBlob.save();
            fileToAdd.put(fileName,blobID);
        }
        /** if it is tracked, compare if the blobID is the same with equals not ==. if it is the same as current
         * working version, remove from fileToAdd and fileToRemove; do not save.
         TODO: NEED TO UPDATE
         */
        else if (prevID.equals(blobID)){
            rm(file);
            String removeID = fileToRemove.get(fileName);
            if (removeID != null){
                fileToRemove.remove(fileName);
            }
            /** if it is tracked but does not change back, update the latest version and save the fileToBlob.
             */
        } else {
            fileToBlob.save();
            fileToAdd.put(fileName,blobID);
        }
    }

    /**1. if the file is staged for add, remove it from stage.1)
     * 2. else if the file is tracked, remove the file from CWD and stage it for removal => untrack for new commit
     * failure case: neither staged nor tracked;
     */
    public void rm(File file){
        String fileName = file.getName();
        String staged = fileToAdd.get(fileName);
        String prevID = tracked.get(fileName);
        /** 1) staged */
        if (staged != null){
            fileToAdd.remove(fileName);
            /** 2) tracked */
        } else if(prevID != null){
            fileToRemove.put(fileName, prevID);
            Utils.restrictedDelete(file);
            //failure case;
        } else {
            System.out.println("No reason to remove the file.");
        }
    }

    /** track file. for commit() */
    public void setTracked(HashMap<String, String> map){
        tracked = map;
    }
    /** clear the staging area after commit */
    public void clear(){
        fileToAdd.clear();
        fileToRemove.clear();
    }
    /** save staging information. */
    public void save() {
        Utils.writeObject(Repository.INDEX, this);
    }
    /** read staging information. */
    public static Staging fromFile(){
        return Utils.readObject(Repository.INDEX, Staging.class);
    }
    public HashMap<String, String> getTracked(){
        HashMap<String, String> newBlobs = new HashMap<>();
        newBlobs.putAll(tracked);
        if (fileToAdd != null){
            newBlobs.putAll(fileToAdd);
        }
        if (fileToRemove != null){
            for (String i : fileToRemove.keySet()){
                if (tracked.containsKey(i)){
                    tracked.remove(i);
                }
            }
        }
        return newBlobs;
    }




}
