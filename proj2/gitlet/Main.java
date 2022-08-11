package gitlet;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;

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
        if (args.length == 0) {
            Utils.message("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            default:
                Utils.message("No command with that name exists.");
                break;
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
                    Utils.message("Incorrect operands.");
                }
                break;

            // TODO: FILL THE REST IN
            case "rm":
                String rmFileName;
                try {
                    rmFileName = args[1];
                    rm(rmFileName);
                } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                    Utils.message("Incorrect operands.");
                }
                break;

            // TODO: FILL THE REST IN
            case "commit":
                String message;
                try {
                    message = args[1];
                    if (message.isEmpty()) {
                        Utils.message("Please enter a commit message.");
                    } else {
                        commit("", message);
                    }
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
                    Utils.message("Incorrect operands.");
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
                    Utils.message("Incorrect operands.");
                }
                break;

            // TODO: FILL THE REST IN
            case "rm-branch":
                String branchNameToRemove;
                try{
                    branchNameToRemove = args[1];
                    rmBranch(branchNameToRemove);

                } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                    Utils.message("Incorrect operands.");
                }
                break;

            case "reset":
                String resetCommit;
                try {
                    resetCommit = args[1];
                    reset(resetCommit);
                } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                    Utils.message("Incorrect operands.");
                }
                break;

            case "merge":
                String mergeBranch;
                try {
                    mergeBranch = args[1];
                    boolean conflict = merge(mergeBranch);
                    if (conflict) {
                        System.out.println("Encountered a merge conflict.");
                    }
                } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                    Utils.message("Incorrect operands.");
                }
                break;

            /**have to put this at last, if put branch() after this,
             *  case 3 and 4 will run branch() and create branch -- and branch commitID for some reason.*/
            case "checkout":
                switch(args.length) {
                    case 3: //checkout -- [file name]
                        if (!args[1].equals("--")) {
                            Utils.message("Incorrect operands.");
                            System.exit(0);
                        }
                        String checkoutCurrentFile = args[2];
                        Repository.checkout(checkoutCurrentFile);
                        break;

                    case 4: //checkout [commit id] -- [file name]
                        if (!args[2].equals("--")) {
                            Utils.message("Incorrect operands.");
                            System.exit(0);
                        }
                        String checkoutCommit = args[1];
                        String checkoutCommitFile = args[3];
                        Repository.checkout(checkoutCommit, checkoutCommitFile);
                        break;

                    case 2: //checkout [branch name]
                        String branchName = args[1];
                        Repository.checkoutBranch(branchName);
                        break;
                    default:
                        Utils.message("Incorrect operands.");
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
