package gitlet;

import java.io.File;
import java.io.IOException;
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
            Commit initCommit = new Commit();       //3)
            initCommit.saveCommit();
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
        if (commitName.length() == 6) {
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
            String shortCommit = i.substring(0, 6);
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
            Utils.error("You have uncommitted changes.");
        }
        /** Failure cases: 2) If a branch with the given name does not exist. */
        File mergeBranchFile = Utils.join(BRANCH_DIR, branchName);
        if (!mergeBranchFile.exists()){
            Utils.error("A branch with that name does not exist.");
        }
        /** Failure cases: 3) If attempting to merge a branch with itself. */
        String currentBranchName = getCurrBranchName();
        if (currentBranchName.equals(branchName)) {
            Utils.error("Cannot merge a branch with itself.");
        }
        /**  Failure cases: 4) If an untracked file in the current commit
         * would be overwritten or deleted by the merge */
        if (!getUntracked().isEmpty()) {
            Utils.error("There is an untracked file in the way; " +
                    "delete it, or add and commit it first.");
        }
        /** check split point. */
        String splitPoint = findSplitPoint(branchName);
        /** flag for conflict. */
        boolean conflict = false;
        if (splitPoint == "given") {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        } else if (splitPoint == "current") {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        /** find the head commit of current and given branch. */
        String currCommitID = getHeadCommitID();
        Commit currCommit = Commit.fromFile(currCommitID);
        Branch mergeBranch = Branch.fromFile(branchName);
        String mergeCommitID = mergeBranch.getHEAD();
        Commit mergeCommit = Commit.fromFile(mergeCommitID);
        Commit splitCommit = Commit.fromFile(splitPoint);

        /** collect all the files in all 3 commits. */
        HashSet<String> allFiles = new HashSet<>();
        allFiles.addAll(currCommit.getBlobList().keySet());
        allFiles.addAll(mergeCommit.getBlobList().keySet());
        allFiles.addAll(splitCommit.getBlobList().keySet());
        /** change the content of file in CWD in 3 ways,
         update to merge; delete; update to conflict;
         * update in staging area */
        for (String key : allFiles) {
            /** Key is the file name,
             * get corresponding blob ID from each commit */
            String inSplit = splitCommit.getBlobList().get(key);
            String inCurr = currCommit.getBlobList().get(key);
            String inMerge = mergeCommit.getBlobList().get(key);
            /** split - present; Current - present;
             * Compare with merge */
            if (inSplit != null && inCurr != null) {
                /** deleted in merge, rm from staging Area
                 * and CWD. */
                if (inMerge == null) {
                    rm(key);
                /** curr is different from merge. */
                } else if (!inCurr.equals(inMerge)) {
                    if (inCurr.equals(inSplit)) {
        /** curr == split, update CWD file to merge, and add to stage.*/
                        saveNewContent(key, inMerge);
                        add(key);
        /** curr != merge != split; create conflict. */
                    } else if (!inCurr.equals(inSplit)
                            && !inMerge.equals(inSplit)) {
                        mergeConflict(inCurr, inMerge, key);
                        conflict = true;
                    }
                }
        /** split - absent; Curr - present */
            } else if (inSplit == null && inCurr != null) {
                /** branch absent - remove file. */
                if (inMerge == null) {
                    rm(key);
                    /** curr != merge, conflict. */
                } else if (!inCurr.equals(inMerge)){
                    mergeConflict(inCurr, inMerge, key);
                    conflict = true;
                }
            }
        }
        String message = String.format("Merged %s into %s.", branchName, currentBranchName);
        commit(mergeCommitID, message);
        return conflict;
    }

    /** find the split point/latest common ancestor of current branch
     * and given branch for merge function.
     *
     *  3) carry on with merge */
    public static String findSplitPoint(String branchName){
        String currBranchName = getCurrBranchName();
        Branch currBranch = Branch.fromFile(currBranchName);
        Branch givenBranch = Branch.fromFile(branchName);
        ArrayList<String> currBranchList = currBranch.getCommitList();
        ArrayList<String> givenBranchList = givenBranch.getCommitList();
        String splitPoint = "";
        int givenSize = givenBranchList.size();
        int currSize = currBranchList.size();
        int index = 0;
        while (index < givenSize && index < currSize &&
                currBranchList.get(index).equals(givenBranchList.get(index))) {
            splitPoint = currBranchList.get(index);
            index++;
            }
        /** 1) same as given branch head commit. */
        if (index == givenSize) {     //in the last step index ++
            return "given";
        } else if (index == currSize) {
            checkoutBranch(branchName);
            return "current";
        } else {
            return splitPoint;
        }
    }

    /** update the content of CWD file with conflict from merge.
     * generate new blob
     * stage for addition */
    public static void mergeConflict(
            String CurrlobID, String mergeBlobID, String fileName) {
        //write content
        String currContent = getContentFromSavedBlob(CurrlobID);
        String mergeContent = getContentFromSavedBlob(mergeBlobID);
        File newVersion = Utils.join(CWD, fileName);
        Utils.writeContents(newVersion, String.format(
                "<<<<<<< HEAD%n%s=======%n%s>>>>>>>%n",
                currContent, mergeContent));
        //save blob
        Blob blob = new Blob(newVersion);
        blob.save();
        /** update staging area. */
        add(fileName);
    }

}
