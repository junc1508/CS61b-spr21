package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static gitlet.Repository.CURBRANCH;

public class Branch implements Serializable {
    /** current branch name. */
    private String branchName;
    /** store all the commits, from init (index 0) to current */
    private ArrayList<String> commitsInBranch;

    /** pointer to current commit */
    private String headCommitID;

    /** constructor for init. */
    public Branch(String initCommit) {
        this.branchName = "master";
        this.headCommitID = initCommit;
        this.commitsInBranch = new ArrayList<>();
        commitsInBranch.add(initCommit);
    }

    /** constructor for branch with branch name. */
    public Branch(String commit, String name) {
        this.branchName = name;
        this.headCommitID = commit; //the SHA-1 ID for last commit of current branch
        this.commitsInBranch = new ArrayList<String>();
        this.commitsInBranch.add(commit);
        Commit currCommit = Commit.fromFile(commit);
        String parentID = currCommit.getParent();
        while (!parentID.isEmpty()) {
            currCommit = Commit.fromFile(parentID);
            commitsInBranch.add(parentID);
            parentID = currCommit.getParent();
        }
        Collections.reverse(commitsInBranch);
    }
    /** get head commit ID. */
    public String getHEAD() {
        return headCommitID;
    }
    /** get list of commits in the branch. */
    public ArrayList<String> getCommitList() {
        return commitsInBranch;
    }
    public void updateBranch(String commitID){
        this.commitsInBranch.add(commitID);
        headCommitID = commitID;
    }
    /**save branch file.*/
    public void saveBranch() {
        File branchFile = Utils.join(Repository.BRANCH_DIR, branchName);
        Utils.writeObject(branchFile, this);
    }

    public void saveHEAD() {
        File headFile = Utils.join(Repository.HEAD);
        Utils.writeContents(headFile, this.headCommitID);
    }

    /** Save current branch name. */
    public void saveCurBranchName() {
        File curBranch = Utils.join(CURBRANCH);
        Utils.writeContents(curBranch, branchName);
    }
    /** Read branch file. */
    public static Branch fromFile(String branchName) {
        File f = Utils.join(Repository.BRANCH_DIR, branchName);
        return Utils.readObject(f, Branch.class);
    }


}
