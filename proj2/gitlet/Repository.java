package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
    public static final File BLOB_DIR = join(GITLET_DIR,"blob");
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
    public static void commit(String message) {
        if (stagingArea.getAdd().isEmpty() && stagingArea.getRemove().isEmpty()) {
            Utils.message("No changes added to the commit.");
            System.exit(0);
        } else {
            /** clone the parent commit from HEAD and get informations.*/
            String parent = getHeadCommit();
            /** new blobs for commit from staging area.*/
            HashMap<String, String> trackedFile = stagingArea.getTracked();

            /** create and save commit, the instruction clone the old commit */
            Commit newCommit = new Commit(message, parent, "", trackedFile);
            newCommit.saveCommit();
            /** clear staging area and set the tracked file. */
            stagingArea.clear();
            stagingArea.setTracked(trackedFile);
            stagingArea.save();
            /** update branch pointer and head pointer.
             * Save branch, head and current branch name. */
            String currBranchName = Utils.readContentsAsString(CURBRANCH);
            Branch currBranch = new Branch(newCommit.getCommitID(), currBranchName);
            currBranch.saveBranch();
            currBranch.saveHEAD(); //don't need to update the current branch since it is the same.
        }
    }

    /** get HEAD commit id. */
    public static String getHeadCommit() {
        return Utils.readContentsAsString(HEAD);
    }
    /** Update current HEAD commit id. */
    public static void setHead(String headCommitID) {
        Utils.writeContents(HEAD, headCommitID);
    }

    /** remove from stage or (CWD and tracked). */
    public static void rm(String file) {
        File f = Utils.join(CWD, file);
        if (f.exists()) {
            stagingArea.rm(f);
            stagingArea.save();
        } else {
            System.out.println("File for rm does not exist.");
            System.exit(0);
        }
    }
    /** print current branch information.
     * Find the current commit (HEAD),recursive calls till first commit.
     * linear time, use stringBuilder - fastest way to concatenate strings.
     * */
    public static void log() {
        StringBuilder str = new StringBuilder();
        String currCommitID = getHeadCommit();
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
        String currBranch = Utils.readContentsAsString(CURBRANCH);
        for (String i : allBranch) {
            if (i.equals(currBranch)) {
                branches.add(String.format("* %s", i));
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
    /** get modified files.
     * 1) Tracked in current commit, changed in the CWD, but not staged(modified);
     * 2) Staged for addition, but with different contents than CWD (modified)
     * 3) Staged for addition, but deleted in CWD (deleted)
     * 4) Not staged for removal, but tracked in the current commit and
     * deleted from the working directory (deleted).*/
    public static String getModfied() {
        //branches
        /** list of file names in CWD */
        List<String> allFilesCWD = Utils.plainFilenamesIn(CWD);
        ArrayList<String> modified = new ArrayList<>();
        ArrayList<String> modifiedD = new ArrayList<>();
        for (String i : allFilesCWD) {
            String aSha = stagingArea.getAdd().get(i);
            File file = Utils.join(CWD, i);
            String iSha = Utils.sha1(i, Utils.readContents(file));
            if (aSha != null) {  //staged
                if (!aSha.equals(iSha)) {    //file staged for addition, but modified later 2)
                    modified.add(String.format("%s (modified)", i));
                }
            } else {
                String tSha = stagingArea.getTracked().get(i);
                if (tSha != null) {      //not staged, tracked.
                    if (!tSha.equals(iSha)) {    //tracked, modified but not staged 1)
                        modified.add(String.format("%s (modified)", i));
                    }
                }
            }
        }
        for (String j : stagingArea.getAdd().keySet()) { //staged for addition but deleted in CWD 3)
            if (!allFilesCWD.contains(j)) {
                modifiedD.add(String.format("%s (deleted)", j));
            }
        }
        //tracked, not added for removal (or addition since it is in 3), deleted in CWD. 4)
        for (String k: stagingArea.getTracked().keySet()) {
            String kaSha = stagingArea.getAdd().get(k);
            String krSha = stagingArea.getRemove().get(k);
            if (kaSha == null && krSha == null && !allFilesCWD.contains(k)) {
                modifiedD.add(String.format("%s (deleted)", k));
            }
        }
        modified.addAll(modifiedD);
        Collections.sort(modified);
        String sortedModified = String.join("\n", modified);
        return sortedModified;
    }

    /** get untracked files. */
    public static String getUntracked() {
        List<String> allFilesCWD = Utils.plainFilenamesIn(CWD);
        ArrayList<String> untracked = new ArrayList<>();
        for (String i : allFilesCWD) {
            //staged files
            String aSha = stagingArea.getAdd().get(i);
            String tSha = stagingArea.getTracked().get(i);
            //includes those in toRemove and deleted but added back.
            if (aSha == null && tSha == null) {
                untracked.add(i);
            }
        }
        Collections.sort(untracked);
        String sortedUntracked = String.join("\n", untracked);
        return sortedUntracked;
    }

    /** checkout -- [file name] Takes the version of the file as it exists
     * in the head commit and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged. */
    public static void checkout(String fileName) {
        /** find the corresponding file in the head commit. */
        String headCommitID = getHeadCommit();
        checkout(headCommitID, fileName);
    }
    /** return blob content from saved Blob file. */
    public static String getContentFromSavedBlob(String blobID) {
        File blobFile = Utils.join(BLOB_DIR, blobID);
        Blob blob = Utils.readObject(blobFile, Blob.class);
        byte[] content = blob.getContent();
        String str = new String(content, StandardCharsets.UTF_8);
        return str;
    }
    /** checkout
     * Takes the version of the file as it exists in the commit with the given id,
     * and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged.*/
    public static void checkout(String commit, String file) {
        File f = Utils.join(COMMIT_DIR,commit);
        if (f.exists()) {
            Commit newCommit = Utils.readObject(f, Commit.class);
            String blobID = newCommit.getBlobList().get(file);
            if (blobID != null) {
                String str = getContentFromSavedBlob(blobID);
                File newVersion = Utils.join(CWD, file);
                Utils.writeContents(newVersion, str); //overwrite the version of file
            } else {
                System.out.println("File does not exist in that commit.");
                System.exit(0);
            }
        } else { //wrong commit id.
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

    }
    /** checkout
     * Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory, overwriting the versions of the files
     * that are already there if they exist.
     * Also, at the end of this command, the given branch will now be
     * considered the current branch (HEAD).
     * Any files that are tracked in the current branch but are not present
     * in the checked-out branch are deleted.
     * The staging area is cleared, unless the checked-out branch is the current branch*/
    public static void checkoutBranch(String branchName) {
        File branchFile = Utils.join(BRANCH_DIR, branchName);
        if (branchFile.exists()) { //if the branch exist
            String curBranchName = Utils.readContentsAsString(CURBRANCH);
            if (curBranchName.equals(branchName)) {
                System.out.println("No need to checkout the current branch.");
                System.exit(0);
                //there is untracked/uncommited changes in current commit
            } else if (!getUntracked().isEmpty()) {
                System.out.println(
                        "There is an untracked file in the way; delete it, or add and commit it first.");
            } else {
                Branch checkBranch = Utils.readObject(branchFile,Branch.class);
                String checkCommitID = checkBranch.getHEAD();
                File checkCommitFile = Utils.join(COMMIT_DIR, checkCommitID);
                Commit checkCommit = Utils.readObject(checkCommitFile, Commit.class);
                /**copy all the files from check commit to CWD, overwrite existing files. */
                copyFilesFromCommit(checkCommit);
                /**delete files that are in current commit but absent in check commit. */
                deleteExtraFile(checkCommit);
                stagingArea.clear();
                //set tracked files to original files.
                stagingArea.setTracked(checkCommit.getBlobList());
                checkBranch.saveBranch();   //update checkBranch.
                checkBranch.saveCurBranchName(); //update current branch to checkBranch.
                checkBranch.saveHEAD(); //update current commit to check commit.
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
        File f = Utils.join(BRANCH_DIR, branchName);
        if (f.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        } else {
            String headCommitID = getHeadCommit(); //current head commit
            Branch branch = new Branch(headCommitID, branchName);
            branch.saveBranch();
            //don't need to save head commit as the current head commit does not change.
            //don't change the current branch name until checkout branchName.
        }
    }
    /** remove branch with the given name - remove branch file only.
     * Keep all the commits and blobs unchanged.*/
    public static void rmBranch(String branchName) {
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
    public static void reset(String commitID) {
        List<String> allCommits = Utils.plainFilenamesIn(COMMIT_DIR);
        if (!allCommits.contains(commitID)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
            //there is untracked/uncommited changes in current commit
        } else if (!getUntracked().isEmpty()) {
            System.out.println(
                    "There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        } else {
            File resetCommitFile = Utils.join(COMMIT_DIR, commitID);
            Commit resetCommit = Utils.readObject(resetCommitFile, Commit.class);
            /** copy all the files from check commit to CWD, overwrite existing files. */
            copyFilesFromCommit(resetCommit);
            /** delete files that are in current commit but absent in check commit. */
            deleteExtraFile(resetCommit);
            stagingArea.clear();
            /** set tracked files to original files. */
            stagingArea.setTracked(resetCommit.getBlobList());
            /** moves the current branch’s head to that commit node */
            setHead(commitID);
        }
    }
}
