package gitlet;


import java.io.File;
import java.io.Serializable;

public class Branch implements Serializable {
    /** current branch name. */
    private String branchName;

    /** pointer to current commit */
    private String headCommitID;

    /** constructor for init. */
    public Branch(String initCommit) {
        this.branchName = "master";
        this.headCommitID = initCommit;
    }

    /** constructor for branch with branch name.
     TODO: NEED TO UPDATE
     * */
    public Branch(String commit, String name) {
        this.branchName = name;
        this.headCommitID = commit; //the SHA-1 ID for last commit of current branch
    }
    /** get head commit ID. */
    public String getHEAD() {
        return headCommitID;
    }
    /**save branch file.*/
    public void saveBranch() {
        File branchFile = Utils.join(Repository.BRANCH_DIR, branchName);
        Utils.writeObject(branchFile, this);
    }
    /** Read branch file.
     * TODO: NEED TO UPDATE */
    public static Branch fromFile(File f) {
        return Utils.readObject(f, Branch.class);
    }
    /** save current HEAD commit ID.*/
    public void saveHEAD() {
        File headFile = Utils.join(Repository.HEAD);
        Utils.writeContents(headFile,this.headCommitID);
    }

    /** Save current branch name. */
    public void saveCurBranchName() {
        File curBranch = Utils.join(Repository.CURBRANCH);
        Utils.writeContents(curBranch,branchName);
    }


}
