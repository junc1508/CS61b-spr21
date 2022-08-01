package gitlet;

import java.io.File;

import static gitlet.Repository.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                String addFileName;
                try {
                    addFileName = args[1];
                    add(addFileName);
                } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                    Utils.message("missing filename for add");
                }
                break;

            // TODO: FILL THE REST IN
            case "rm":
                String rmFileName;
                try {
                    rmFileName = args[1];
                    rm(rmFileName);
                } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                    Utils.message("missing filename for rm");
                }
                break;

            // TODO: FILL THE REST IN
            case "commit":
                String message;
                try {
                    message = args[1];
                    commit(message);
                } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                    Utils.message("Please enter a commit message.");
                }
                break;
            // TODO: FILL THE REST IN
            case "log":
                log();
                break;
            // TODO: FILL THE REST IN
            case "global-log":
                globallog();
                break;
            // TODO: FILL THE REST IN
            case "find":
                String findMessage;
                try {
                    findMessage = args[1];
                    find(findMessage);
                } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                    Utils.message("Please enter a commit message to find.");
                }
                break;

            // TODO: FILL THE REST IN
            case "status":
                status();
                break;

            case "branch":
                String newBranchName;
                try {
                    newBranchName= args[1];
                    branch(newBranchName);
                } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                    Utils.message("Please enter a branch name");
                }
                break;

            // TODO: FILL THE REST IN
            case "rm-branch":
                String branchNameToRemove;
                try{
                    branchNameToRemove = args[1];
                    rmBranch(branchNameToRemove);

                } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                    Utils.message("Please enter a branch name to remove");
                }
                break;

            case "reset":
                String resetCommit;
                try {
                    resetCommit = args[1];
                    reset(resetCommit);
                } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                    Utils.message("Please enter commit ID for reset.");
                }
                break;

            // TODO: FILL THE REST IN
            /**have to put this at last, if put branch() after this,
             *  case 3 and 4 will run branch() and create branch -- and branch commitID for some reason.*/
            case "checkout":
                switch(args.length) {
                    case 3: //checkout -- [file name]
                        String checkoutCurrentFile = args[2];
                        Repository.checkout(checkoutCurrentFile);
                        break;

                    case 4: //checkout [commit id] -- [file name]
                        String checkoutCommit = args[1];
                        String checkoutCommitFile = args[3];
                        Repository.checkout(checkoutCommit, checkoutCommitFile);
                        break;

                    case 2: //checkout [branch name]
                        String branchName = args[1];
                        Repository.checkout_branch(branchName);
                        break;
                }


            // TODO: FILL THE REST IN
            // TODO: FILL THE REST IN
            // TODO: FILL THE REST IN
            // TODO: FILL THE REST IN
            // TODO: FILL THE REST IN



        }
    }
}
