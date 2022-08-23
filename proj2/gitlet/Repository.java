package gitlet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Jun Chen
 */
public class Repository {
    /**List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMIT_DIR = join(GITLET_DIR, ".commit");
    public static final File BRANCH_DIR = join(GITLET_DIR, ".branch");
    public static final File BLOB_DIR = join(GITLET_DIR, "blob");
    /** filepath for staging. */
    public static  File INDEX = join(GITLET_DIR, "index");

    public static Staging stagingArea;
    /** have to define stagingArea as belwo to instantiate Staging;
     * or else all the staging trees will be null and cannot proceed */
    static {
        if (INDEX.exists()) {
            stagingArea = Staging.fromFile();
        } else {
            stagingArea = new Staging();
        }
    }
    /** current head commit ID */
    public static File HEAD = join(GITLET_DIR, "HEAD");
    /** Current branch name. */
    public static File CURBRANCH = join(GITLET_DIR, "CURBRANCH");
    /** initial commit id. */
    public static File INITID = join(GITLET_DIR, "INITID");

    /** return current branch name */
    public static String getCurrBranchName() {
        return Utils.readContentsAsString(CURBRANCH);
    }
    /** init command
     * 1) check if .Gitlet directory already exists
     * 2) if not, creates .Gitlet directory
     * 3) init commit and save
     * 4) init branch (master)
     * */
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println(
                    "A Gitlet version-control system already exists in the current directory."); //1)
            System.exit(0);
        } else {
            //2
            COMMIT_DIR.mkdirs();
            BRANCH_DIR.mkdirs();
            BLOB_DIR.mkdirs();
            try {
                HEAD.createNewFile();
            } catch (IOException e) {
                System.out.println("Can't create HEAD");
            }
            try {
                CURBRANCH.createNewFile();
            } catch (IOException e) {
                System.out.println("Cant create CURBRANCH");
            }
            try {
                INITID.createNewFile();
            } catch (IOException e) {
                System.out.println("Cant create INITID");
            }
            Commit initCommit = new Commit();       //3)
            initCommit.saveCommit();
            Utils.writeContents(INITID,initCommit.getCommitID());
            Branch master = new Branch(initCommit.getCommitID()); //4)
            master.saveBranch();
            master.saveHEAD();
            master.saveCurBranchName();
            stagingArea = new Staging();
            stagingArea.save();
        }
    }

    /** check if .gitlet folder exists => initialized */
    public static void checkGitletDir() {
        if (!GITLET_DIR.exists()) {
            Utils.message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    /** Description: 1) Adds a copy of the file to the staging area,
     * The staging area should be somewhere in .gitlet.
     * 2) Staging an already-staged file overwrites the previous entry
     * in the staging area with the new contents.
     * 3) If the current working version of the file is identical to
     * the version in the current commit,
     * do not stage it to be added, and remove it from the staging area
     * if it is already there (as can happen when a file is changed, added,
     * and then changed back to it’s original version). The file will no longer
     * be staged for removal (see gitlet rm), if it was at the time of the command.*/
    public static void add(String fileName) {
        checkGitletDir();
        // check if the file exist.
        File f = Utils.join(CWD, fileName);
        if (f.exists()) {
            stagingArea.add(f);
            stagingArea.save();
        } else {
            System.out.println("File does not exist.");
            System.exit(0);
        }
    }

    /**1) clone the parent commit saved in HEAD;
     * 2) gather blob from staging area.
     * 3) master and head pointer.
     * 4) clear stage; set new tracked
     * */
    public static void commit(String mergeCommit, String message) {
        checkGitletDir();
        if (stagingArea.getAdd().isEmpty() && stagingArea.getRemove().isEmpty()) {
            Utils.message("No changes added to the commit.");
            System.exit(0);
        } else {
            /** clone the parent commit from HEAD and get informations.*/
            String parent = getHeadCommitID();
            /** new blobs for commit from staging area.*/
            HashMap<String, String> trackedFile = stagingArea.getTracked();

            /** create and save commit, the instruction clone the old commit */
            Commit newCommit = new Commit(message, parent, mergeCommit, trackedFile);
            newCommit.saveCommit();
            /** clear staging area and set the tracked file. */
            stagingArea.clear();
            stagingArea.setTracked(trackedFile);
            stagingArea.save();
            /** update branch pointer and head pointer.
             * Save branch, head and current branch name. */
            updateBranch(newCommit.getCommitID());
        }
    }

    /** update current branch with new commit.
     * Save Branch file and HEAD commit */
    public static void updateBranch(String commitID) {
        String currBranchName = getCurrBranchName();
        Branch currBranch = Branch.fromFile(currBranchName);
        currBranch.updateBranch(commitID);
        currBranch.saveBranch();
        currBranch.saveHEAD();
    }

    /** get HEAD commit id. */
    public static String getHeadCommitID() {
        return Utils.readContentsAsString(HEAD);
    }

    /** Update current HEAD commit id. */
    public static void setHead(String headCommitID) {
        Utils.writeContents(HEAD, headCommitID);
    }

    /** remove from stage or (CWD and tracked). */
    public static void rm(String fileName) {
        stagingArea.rm(fileName);
        stagingArea.save();
    }
    /** print current branch information from current commit.
     * Find the current commit (HEAD),recursive calls till first commit.
     * linear time, use stringBuilder - fastest way to concatenate strings.
     * */
    public static void log() {
        checkGitletDir();
        StringBuilder str = new StringBuilder();
        String currCommitID = getHeadCommitID();
        Commit currCommit = Commit.fromFile(currCommitID);
        String parentID = currCommit.getParent();
        str.append(currCommit.toLog());
        while (!parentID.isEmpty()) {
            currCommit = Commit.fromFile(parentID);
            str.append(currCommit.toLog());
            parentID = currCommit.getParent();
        }
        str.deleteCharAt(str.length() - 1);
        System.out.println(str);
    }
    /** print all the commits. */
    public static void globallog() {
        checkGitletDir();
        List<String> allCommits = Utils.plainFilenamesIn(COMMIT_DIR);
        StringBuilder globallog = new StringBuilder();
        for (String i : allCommits) {
            Commit commit = Commit.fromFile(i);
            globallog.append(commit.toLog());
        }
        globallog.deleteCharAt(globallog.length() - 1);
        System.out.println(globallog);
    }

    /**Prints out the ids of all commits that have the given commit message.
     * If there are multiple such commits, it prints the ids out on separate lines. */
    public static void find(String message) {
        checkGitletDir();
        List<String> allCommits = Utils.plainFilenamesIn(COMMIT_DIR);
        StringBuilder commitID = new StringBuilder();
        for (String i: allCommits) {
            Commit commit = Commit.fromFile(i);
            if (commit.getMessage().equals(message)) {
                commitID.append(commit.getCommitID() + "\n");
            }
        }
        if (commitID.isEmpty()) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        } else {
            commitID.deleteCharAt(commitID.length() - 1);
            System.out.println(commitID);
        }
    }

    /**  Displays what branches currently exist, and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal.
     * better to make it call several smaller methods */
    public static void status() {
        checkGitletDir();
        //branches
        StringBuilder status = new StringBuilder();
        String branch = getBranches();
        String add = getStaged();
        String remove = getRemovalList();
        String modified = getModfied();
        String untracked = getUntracked();
        status.append("=== Branches ===\n");
        if (!branch.isEmpty()) {
            status.append(branch + "\n");
        }
        status.append("\n=== Staged Files ===\n");
        if (!add.isEmpty()) {
            status.append(add + "\n");
        }
        status.append("\n=== Removed Files ===\n");
        if (!remove.isEmpty()) {
            status.append(remove + "\n");
        }
        status.append("\n=== Modifications Not Staged For Commit ===\n");
        if (!modified.isEmpty()) {
            status.append(modified + "\n");
        }
        status.append("\n=== Untracked Files ===\n");
        if (!untracked.isEmpty()) {
            status.append(untracked + "\n");
        }
        System.out.println(status);
    }
    /** get all branches. */
    public static String getBranches() {
        List<String> allBranch = Utils.plainFilenamesIn(BRANCH_DIR);
        ArrayList<String> branches = new ArrayList<>();
        String currBranch = getCurrBranchName();
        for (String i : allBranch) {
            if (i.equals(currBranch)) {
                branches.add(String.format("*%s", i));
            } else {
                branches.add(String.format("%s", i));
            }
        }
        Collections.sort(branches);
        String sortedBranch = String.join("\n", branches);
        return sortedBranch;
    }

    /** get staged files. */
    public static String getStaged() {
        ArrayList<String> staged = new ArrayList<>();
        for (String i: stagingArea.getAdd().keySet()) {
            staged.add(i);
        }
        Collections.sort(staged);
        String sortedStage = String.join("\n", staged);
        return sortedStage;
    }

    /** get staged for removal files. Can be deleted or not deleted. */
    public static String getRemovalList() {
        ArrayList<String> forRemoval = new ArrayList<>();
        for (String i: stagingArea.getRemove().keySet()) {
            forRemoval.add(i + "\n");
        }
        Collections.sort(forRemoval);
        String sortedRemoval = String.join("\n", forRemoval);
        return sortedRemoval;
    }

    /** return SHA-1 id of file that will be turned into a blob. */
    public static String getSha1(String fileName) {
        File file = Utils.join(CWD, fileName);
        return Utils.sha1(fileName, Utils.readContents(file));
    }

    /** get modified files.
     * 1) Tracked in current commit, changed in the CWD, but not staged(modified);
     * 2) Staged for addition, but with different contents than CWD (modified)
     * 3) Staged for addition, but deleted in CWD (deleted)
     * 4) Not staged for removal, but tracked in the current commit and
     * deleted from the working directory (deleted).*/
    public static String getModfied() {
        /** list of file names in CWD */
        List<String> allFilesCWD = Utils.plainFilenamesIn(CWD);
        ArrayList<String> modified = new ArrayList<>();
        ArrayList<String> modifiedD = new ArrayList<>();
        for (String i : allFilesCWD) {
            String aSha = stagingArea.getAdd().get(i);
            String iSha = getSha1(i);
            if (aSha != null) {  //staged
                if (!aSha.equals(iSha)) {    //file staged for addition, but modified later 2)
                    modified.add(String.format("%s (modified a)", i));
                }
            } else {
                String tSha = stagingArea.getTracked().get(i);
                if (tSha != null) {      //not staged, tracked.
                    if (!tSha.equals(iSha)) {    //tracked, modified but not staged 1)
                        modified.add(String.format("%s (modified b)", i));
                    }
                }
            }
        }
        for (String j : stagingArea.getAdd().keySet()) { //staged for addition but deleted in CWD 3)
            if (!allFilesCWD.contains(j)) {
                modifiedD.add(String.format("%s (deleted a)", j));
            }
        }
        //tracked, not added for removal (or addition since it is in 3), deleted in CWD. 4)
        for (String k: stagingArea.getTracked().keySet()) {
            String kaSha = stagingArea.getAdd().get(k);
            String krSha = stagingArea.getRemove().get(k);
            if (kaSha == null && krSha == null && !allFilesCWD.contains(k)) {
                modifiedD.add(String.format("%s (deleted b)", k));
            }
        }
        modified.addAll(modifiedD);
        Collections.sort(modified);
        String sortedModified = String.join("\n", modified);
        return sortedModified;
    }

    /** return untracked files */
    public static String getUntracked() {
        List<String> allFilesCWD = Utils.plainFilenamesIn(CWD);
        ArrayList<String> untracked = new ArrayList<>();
        Commit currCommit = Commit.fromFile(getHeadCommitID());

        for (String i : allFilesCWD) {
            //if it was tracked before
            String preSha = currCommit.getBlobList().get(i);
            if (preSha != null) {
                //staged files, getTracked = tracked + add - remove
                //therefore, if a file is removed then added back
                //it is not in getTracked.
                String tSha = stagingArea.getTracked().get(i);
                if (tSha == null) {
                    untracked.add(i);
                }
            }
        }
        Collections.sort(untracked);
        String sortedUntracked = String.join("\n", untracked);
        return sortedUntracked;
    }

    /** Return boolean value for existence of untracked file,
     * Untracked files are CWD files that are tracked in previous commit
     * but is not currently tracked because
     * 1) the fileName is not tracked by current commit;
     * 2) it was tracked by current commit but modified -- diff SHA-1
     *  for checkout branch, branch.getHEAD().
     *  for reset commitID, commitID */
    public static boolean hasUntracked(String commitID) {
        Commit commit = Commit.fromFile(commitID);
        List<String> allFilesCWD = Utils.plainFilenamesIn(CWD);
        ArrayList<String> untracked = new ArrayList<>();
        HashMap<String, String> prevTrack = commit.getBlobList();
        for (String i : allFilesCWD) {
            String pSha = prevTrack.get(i);
            if (pSha != null) {
                Commit currCommit = Commit.fromFile(getHeadCommitID());
                String tSha = currCommit.getBlobList().get(i);
                /** the filename not currently tracked */
                if (tSha == null) {
                    untracked.add(i);
                } else {
                    /** different SHA-1. */
                    String currSha = getSha1(i);
                    //if the CWD version is not the same as the HEAD tracked
                    // and previous/staged version, it is untracked.
                    if (!currSha.equals(tSha) && !currSha.equals(pSha)) {
                        untracked.add(i);
                    }
                }
            }
        }
        boolean b = !untracked.isEmpty();
        return b;
    }

    /** checkout -- [file name] Takes the version of the file as it exists
     * in the head commit and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged. */
    public static void checkout(String fileName) {
        /** find the corresponding file in the head commit. */
        String headCommitID = getHeadCommitID();
        checkout(headCommitID, fileName);
    }
    /** return blob content from saved Blob file. */
    public static String getContentFromSavedBlob(String blobID) {
        Blob blob = Blob.fromFile(blobID);
        byte[] content = blob.getContent();
        String str = new String(content, StandardCharsets.UTF_8);
        return str;
    }
    /** checkout
     * Takes the version of the file as it exists in the commit with the given id,
     * and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged.*/
    public static void checkout(String commitName, String file) {
        checkGitletDir();
        String commitID = commitName;
        if (commitName.length() == 8) {
            commitID = getCommitFromShortID(commitName);
        }
        if (commitID == null){
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = Commit.fromFile(commitID);
        if (commit == null){
            //wrong commit id.
            System.out.println("No commit with that id exists.");
            System.exit(0);
        } else {
            String blobID = commit.getBlobList().get(file);
            if (blobID != null) {
                saveNewContent(file, blobID);
            } else {
                System.out.println("File does not exist in that commit.");
                System.exit(0);
            }
        }
    }
    /** save new content from blobID to CWD file with the same file name. */
    public static void saveNewContent(String fileName, String blobID) {
        String str = getContentFromSavedBlob(blobID);
        File newVersion = Utils.join(CWD, fileName);
        Utils.writeContents(newVersion, str); //overwrite the version of file
    }

    /** get original SHA-1 ID from short commit ID - first 6 characters of SHA-1.
     * Git save time by storing commit in commit - first 2 char - last 38 char
     */
    public static String getCommitFromShortID(String shortID) {
        List<String> allCommits = plainFilenamesIn(COMMIT_DIR);
        for (String i : allCommits) {
            String shortCommit = i.substring(0, 8);
            if (shortCommit.equals(shortID)){
                return i;
            }
        }
        return null;
    }

    /** checkout branch
     * Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory, overwriting the versions of the files
     * that are already there if they exist.
     * Also, at the end of this command, the given branch will now be
     * considered the current branch (HEAD).
     * Any files that are tracked in the current branch but are not present
     * in the checked-out branch are deleted.
     * The staging area is cleared, unless the checked-out branch is the current branch*/
    public static void checkoutBranch(String branchName) {
        checkGitletDir();
        List<String> branchList = plainFilenamesIn(BRANCH_DIR);
        if (branchList.contains(branchName)) { //if the branch exist
            String curBranchName = getCurrBranchName();
            if (curBranchName.equals(branchName)) {
                System.out.println("No need to checkout the current branch.");
                System.exit(0);
                //there is untracked/uncommited changes in current commit
                //branch.head and HEAD.getBloblist()
            } else {
                Branch checkBranch = Branch.fromFile(branchName);
                String checkCommitID = checkBranch.getHEAD();
                if (hasUntracked(checkCommitID)) {
                    System.out.println(
                            "There is an untracked file in the way; delete it, or add and commit it first.");
                } else {
                    Commit checkCommit = Commit.fromFile(checkCommitID);
                    /**delete files that are in current commit but absent in check commit. */
                    deleteExtraFile(checkCommit);
                    /**copy all the files from check commit to CWD, overwrite existing files. */
                    copyFilesFromCommit(checkCommit);
                    stagingArea.clear();
                    //set tracked files to checkCommit tracked files.
                    stagingArea.setTracked(checkCommit.getBlobList());
                    stagingArea.save();
                    checkBranch.saveBranch();   //update checkBranch.
                    checkBranch.saveCurBranchName(); //update current branch to checkBranch.
                    checkBranch.saveHEAD(); //update current HEAD commit to check commit.
                }
            }
        } else {
            System.out.println("No such branch exists.");
            System.exit(0);  //wrong branch name
        }
    }
    /**copy all the files from check commit to CWD, overwrite existing files. */
    public static void copyFilesFromCommit(Commit checkCommit) {
        for (String i : checkCommit.getBlobList().keySet()) {
            File file = Utils.join(CWD, i);
            String blobID = checkCommit.getBlobList().get(i);
            Utils.writeContents(file, getContentFromSavedBlob(blobID));
        }
    }

    /** delete files that are in current commit but not in commit 1.*/
    public static void deleteExtraFile(Commit commit1) {
        for (String i : stagingArea.getTracked().keySet()) {
            String ifCheck = commit1.getBlobList().get(i); //if the file is tracked in check commit1
            if (ifCheck == null) {
                File file = Utils.join(CWD, i);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }
    public static void branch(String branchName) {
        checkGitletDir();
        File f = Utils.join(BRANCH_DIR, branchName);
        if (f.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        } else {
            String headCommitID = getHeadCommitID(); //current head commit
            Branch branch = new Branch(headCommitID, branchName);
            branch.saveBranch();
            //don't need to save head commit as current head commit does not change.
            //don't change the current branch name until checkout branchName.
        }
    }
    /** remove branch with the given name - remove branch file only.
     * Keep all the commits and blobs unchanged.*/
    public static void rmBranch(String branchName) {
        checkGitletDir();
        String currBranch = readContentsAsString(CURBRANCH);
        if (currBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        } else {
            File branchToRemove = Utils.join(BRANCH_DIR, branchName);
            if (branchToRemove.exists()) {
                branchToRemove.delete();
            } else {
                System.out.println("A branch with that name does not exist.");
                System.exit(0);
            }
        }
    }

    /**Checks out all the files tracked by the given commit.
     * Removes tracked files that are not present in that commit.
     * Also moves the current branch’s head to that commit node.
     * The [commit id] may be abbreviated as for checkout.
     * The staging area is cleared.
     * The command is essentially checkout of an arbitrary commit that
     * also changes the current branch head.*/
    public static void reset(String commitName) {
        checkGitletDir();
        String commitID = commitName;
        if (commitName.length() == 6) {
            commitID = getCommitFromShortID(commitName);
        }
        if (commitID == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit resetCommit = Commit.fromFile(commitID);
        if (resetCommit == null) {
            System.out.println("No commit with that id exists.");
        } else {
            //there is untracked/uncommited changes in current commit
            if (hasUntracked(commitID)){
                System.out.println("There is an untracked file in the way; " +
                        "delete it, or add and commit it first.");
            } else {
                /** update the branch head with new commit.
                 * Save the new branch and the HEAD commit */
                updateBranch(commitID);
                /** delete files that are in current commit
                 * but absent in check commit. */
                deleteExtraFile(resetCommit);
                /** copy all the files from check commit to CWD,
                 * overwrite existing files. */
                copyFilesFromCommit(resetCommit);
                /** clear staging area. */
                stagingArea.clear();
                /** set tracked files to original files. */
                stagingArea.setTracked(resetCommit.getBlobList());
                stagingArea.save();
            }
        }
    }

    /** Merges files from the given branch HEAD into the current branch HEAD.
     * Need to update staging area and CWD.
     * Return boolean value, true if there is a conflict.
     * For split point:
     * 1) If the split point is the same commit as the given branch head commit,
     *      the merge is complete, print message.
     * 2) If the split point is the current branch head commit,
     *   check out the given branch, and the operation ends after message 2 */
    public static boolean merge(String branchName) {
        /** Failure cases: 1) If there are staged additions or removals present */
        if (!stagingArea.getAdd().isEmpty() || !stagingArea.getRemove().isEmpty()) {
            Utils.message("You have uncommitted changes.");
            System.exit(0);
        }
        /** Failure cases: 2) If a branch with the given name does not exist. */
        File mergeBranchFile = Utils.join(BRANCH_DIR, branchName);
        if (!mergeBranchFile.exists()) {
            Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }
        /** Failure cases: 3) If attempting to merge a branch with itself. */
        String currentBranchName = getCurrBranchName();
        if (currentBranchName.equals(branchName)) {
            Utils.message("Cannot merge a branch with itself.");
            System.exit(0);
        }
        /**  Failure cases: 4) If an untracked file in the current commit
         * would be overwritten or deleted by the merge */
        Branch mergeBranch = Branch.fromFile(branchName);
        String mergeCommitID = mergeBranch.getHEAD();

        if (hasUntracked(mergeCommitID)) {
            Utils.message("There is an untracked file in the way; " +
                    "delete it, or add and commit it first.");
            System.exit(0);
        }

        /** check split point. */
        String splitPoint = findSplitPoint(branchName);
        /** flag for conflict. */
        boolean conflict = false;
        String currCommitID = getHeadCommitID();
        if (splitPoint.equals(mergeCommitID)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        } else if (splitPoint.equals(currCommitID)) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        /** find the head commit of current and given branch. */
        Commit currCommit = Commit.fromFile(currCommitID);
        Commit mergeCommit = Commit.fromFile(mergeCommitID);
        Commit splitCommit = Commit.fromFile(splitPoint);
        /** collect all the files in all 3 commits. */
        HashSet<String> allFiles = new HashSet<>();
        allFiles.addAll(currCommit.getBlobList().keySet());
        allFiles.addAll(mergeCommit.getBlobList().keySet());
        allFiles.addAll(splitCommit.getBlobList().keySet());
        /** change the content of file in CWD in 3 ways,
         * 8 possibilities
         update to merge; delete; update to conflict;
         * update in staging area */
        for (String key : allFiles) {
            /** Key is the file name,
             * get corresponding blob ID from each commit */
            String inSplit = splitCommit.getBlobList().get(key);
            String inCurr = currCommit.getBlobList().get(key);
            String inMerge = mergeCommit.getBlobList().get(key);
            /** split - absent */
            if (inSplit == null) {
                /** not in split, master and branch different version.
                 * => conflict. */
                if (inCurr != null && inMerge != null
                        && !inCurr.equals(inMerge)) {
                    conflict = mergeConflict(inCurr, inMerge, key);
                    /** not in split, nor in curr, in merge.
                     * => update to branch, add to stage. */
                } else if (inCurr == null && inMerge!= null) {
                    saveNewContent(key, inMerge);
                    add(key);
                }
                /** split present. */
            } else {
                /** deleted in curr. */
                if (inCurr == null) {
                    /** deleted in curr, modified in merge
                     * => conflict. */
                    if (inMerge != null && !inMerge.equals(inSplit)) {
                        conflict = mergeConflict(inCurr, inMerge, key);
                    }
                    /** same in curr */
                } else if (inCurr.equals(inSplit)) {
                    /** split = curr, deleted in merge
                     * => remove */
                    if (inMerge == null) {
                        rm(key);
                        /** split = curr != merge (present)
                         * => update CWD file to merge
                         * add to stage */
                    } else if (!inMerge.equals(inSplit)) {
                        saveNewContent(key, inMerge);
                        add(key);
                    }
                    /** modified in curr. */
                } else {
                    /** modified in curr, deleted in merge.
                     * => conflict */
                    if (inMerge == null) {
                        conflict = mergeConflict(inCurr, inMerge, key);
                        /** all 3 are different => conflict. */
                    } else if (!inMerge.equals(inSplit)
                            && !inMerge.equals(inCurr)) {
                        conflict = mergeConflict(inCurr, inMerge, key);
                    }
                }
            }
        }
        String message = String.format("Merged %s into %s.", branchName, currentBranchName);
        commit(mergeCommitID, message);
        return conflict;
    }

    /** find the split point/latest common ancestor of current branch
     * and given branch for merge function.*/
    public static String findSplitPoint(String branchName){
        String currBranchName = getCurrBranchName();
        Branch currBranch = Branch.fromFile(currBranchName);
        Branch givenBranch = Branch.fromFile(branchName);
        String currCommitID = currBranch.getHEAD();
        String givenCommitID = givenBranch.getHEAD();
        Commit currCommit = Commit.fromFile(currCommitID);
        Commit givenCommit = Commit.fromFile(givenCommitID);
        /** create graph for branches. Record the color for
         * ancestors. ancestor for currCommit = red
         * ancestor for mergeCommit: blue */
        HashMap<String, List<String>> graph = new HashMap<>();
        HashMap<String, List<String>> color = new HashMap<>();
        addEdge(currCommit, graph, color, "red");
        addEdge(givenCommit, graph, color,"blue");
        //array to record distance to root, index is the distance
        HashMap<String, Integer> disTo = new HashMap<>();
        bfs(graph, disTo);
        String splitPoint = "";
        int level = 0;
        /** common ancestors contain both colors */
        for (String i: color.keySet()) {
            //latest common ancestor has largest disTo
            if (color.get(i).size() == 2 && level <= disTo.get(i)) {
                splitPoint = i;
                level = disTo.get(i);
            }
        }
        return splitPoint;
    }
    /** Instead of DFS,
     * add parent -> curr, second parent -> curr to graph,
     * and color code ancestors for branch map. */
    public static void addEdge(Commit commit,
                               HashMap<String, List<String>> graph,
                               HashMap<String, List<String>> ancestor,
                               String color) {
        String currID = commit.getCommitID();
        String parentID = commit.getParent();
        String secondParentID = commit.getSecondParent();
        /** add the current ID to the tree, with no child,
         * so the graph can have all the vertice */
        if (graph.get(currID) == null) {
            graph.put(currID, new ArrayList<>());
            ancestor.put(currID, new ArrayList<>());
        }
        ancestor.get(currID).add(color);
        addEdgeHelp(currID, parentID, graph, ancestor, color);
        addEdgeHelp(currID, secondParentID, graph, ancestor, color);

    }
    /** add parent => curr edges to the graph.
     * starting from current commit */
    private static void addEdgeHelp(String currID, String parentID,
                                    HashMap<String, List<String>> graph,
                                    HashMap<String, List<String>> ancestor,
                                    String color) {
        /** base case no parent -> init */
       if (!parentID.isEmpty()) {
            if (graph.get(parentID) == null) {
                //if parent does not exist, add parent, empty arraylist
                graph.put(parentID, new ArrayList<>());
                graph.get(parentID).add(currID);
                ancestor.put(parentID, new ArrayList<>());
            } else if (!graph.get(parentID).contains(currID)) {
                //if parent exists, check if curr exists
                graph.get(parentID).add(currID);
            }
            ancestor.get(parentID).add(color);
            Commit nextCommit = Commit.fromFile(parentID);
            String nextCommitID = nextCommit.getCommitID();
            String nextParentID = nextCommit.getParent();
            addEdgeHelp(nextCommitID, nextParentID, graph, ancestor, color);
        } else {
           return; //no parent = init, done.
       }
    }

    /** measure the distance of the ancestors to in graph
     * initCommit with BFS */
    public static void bfs(HashMap<String, List<String>> graph,
                           HashMap<String, Integer> disTo) {
        //map to record if the vertex is visited
        HashMap<String, Boolean> marked = new HashMap<>();
        //queue to record node to visit.
        Queue<String> nodeToVisit = new LinkedList<>();
        // visit the init commit (root)
        String commitID = Utils.readContentsAsString(INITID);
        marked.put(commitID, true);
        //set root level = 0
        disTo.put(commitID,0);
        //add root to queue
        nodeToVisit.add(commitID);
        //if there is unvisited node in the queue
        while (!nodeToVisit.isEmpty()) {
            /** poll the node from queue to look for next level
             *  poll returns null when list is empty,
             *  remove throws an error */
            /** pull next node from queue, everything in
             * the queue is visited */
            String nextNode = nodeToVisit.poll();
            //for all the neighbor of next node
            if (graph.get(nextNode) != null) {
                for (String i : graph.get(nextNode)) {
                    //if the neighbor is not visited
                    if (!marked.containsKey(i)){
                        //vist neighbor
                        marked.put(i, true);
                        //distance to root = 1 + previous distance
                        int level = disTo.get(nextNode) + 1;
                        disTo.put(i, level);
                        //add the neighbor to queue
                        nodeToVisit.add(i);
                    }  //else if visited, skip
                }
            }

        }
    }
    /** update the content of CWD file with conflict from merge.
     * generate new blob
     * stage for addition */
    public static boolean mergeConflict(
            String CurrlobID, String mergeBlobID, String fileName) {
        //write content
        String currContent;
        String mergeContent;
        if (CurrlobID == null) {
            currContent = "";
        } else {
            currContent = getContentFromSavedBlob(CurrlobID);
        }
        if (mergeBlobID == null) {
            mergeContent = "";
        } else {
            mergeContent = getContentFromSavedBlob(mergeBlobID);
        }
        File newVersion = Utils.join(CWD, fileName);
        Utils.writeContents(newVersion, String.format(
                "<<<<<<< HEAD%n%s=======%n%s>>>>>>>%n",
                currContent, mergeContent));
        //save blob
        Blob blob = new Blob(newVersion);
        blob.save();
        /** update staging area. */
        add(fileName);
        return true;
    }


}
