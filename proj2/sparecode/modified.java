public static void status(){
        //branches
        StringBuilder status = new StringBuilder();
        List<String> allBranch = Utils.plainFilenamesIn(BRANCH_DIR);
        /** list of file names in CWD */
        List<String> allFilesCWD = Utils.plainFilenamesIn(CWD);
        StringBuilder add = new StringBuilder();
        StringBuilder remove = new StringBuilder();
        StringBuilder modified_d = new StringBuilder();
        StringBuilder modified_m = new StringBuilder();
        StringBuilder untracked = new StringBuilder();
        status.append("=== Branches ===\n");
        for (String i : allBranch){
            status.append(i + "\n");
        }
        status.append("\n=== Staged Files ===\n");
        for (String j : allFilesCWD){
            //staged files
            String aSha = stagingArea.getAdd().get(j);
            String rSha = stagingArea.getRemove().get(j);
            String tSha = stagingArea.getTracked().get(j);
            File file = Utils.join(CWD,j);
            String jSha = Utils.sha1(j, Utils.readContents(file));
            if (aSha != null) {
                if (aSha.equals(jSha)) {    //staged for addition, same content
                    add.append(j + "\n");
                } else {                    //staged for addition, but with different content => modified 2)
                    modified_m.append(j +" (modified)\n");
                }
            } else if (rSha != null) {      //staged for removal but still exists => untracked
                untracked.append(j + "\n");
            } else if (tSha != null){  //tracked but not staged for add.
                if(!tSha.equals(jSha)){ //tracked, changed but not staged for add. => modified 1)
                    modified_m.append(j + " (modified)\n");
                }
            } else {                    //untracked, not staged in CWD.
                untracked.append(j + "\n");
            }
        }
        for (String i: stagingArea.getTracked().keySet()){
            //staged but deleted
            if (!allFilesCWD.contains(i)){
                String del = stagingArea.getRemove().get(i);
                if (del == null){   //tracked, deleted but not staged for removal => modified 4)
                    modified_d.append(i+ " (deleted)\n");
                } else {                //tracked, deleted and staged for removal
                    remove.append(i+ " (deleted)\n");
                }
            }
        }
        for (String k: stagingArea.getAdd().keySet()){
            if (!allFilesCWD.contains(k)){  //staged for addition but deleted. => modified 3)
                modified_d.append(k + "\n");
            }
        }

        status.append(add);
        status.append("\n=== Removed Files ===\n");
        status.append(remove);
        status.append("\n=== Modifications Not Staged For Commit ===\n");
        status.append(modified_d);
        status.append(modified_m);
        status.append("\n=== Untracked Files ===\n");
        status.append(untracked);
        System.out.println(status);
    }