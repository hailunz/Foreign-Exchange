package decisionTree;

import java.io.*;
import java.util.ArrayList;

/**
 * Hailun Zhu
 * ID: hailunz
 * Date: 10/25/15
 */
public class DecisionTree implements Serializable {


    TreeNode root;

    public void setRoot(String filename, ArrayList<Integer> set0) throws IOException {
        root = getNode(filename,set0);
    }

    public TreeNode getRoot() throws IOException {
        return root;
    }


    public TreeNode getNode(ArrayList<Integer> set0, String []lines){
        ArrayList<Integer> set = new ArrayList<>(set0);
        int n = set.size();
        TreeNode root = null;

        // if the node is in the last level
        if (n==1){
            root = new TreeNode(set.get(0));
            String[] line;
            int [] count= new int[4];
            int num = 0;

            for(String l : lines){
                line = l.split("\n")[0].split("\t");
                num++;
                for(int i=0;i<n;i++){
                    int f = set.get(i);
                    int x = Integer.parseInt(line[f]);
                    int y = Integer.parseInt(line[6]);
                    if (x==0 && y==0){
                        count[0]++;
                    }else if (x==0 && y==1){
                        count[1]++;
                    }else if (x==1 && y==0){
                        count[2]++;
                    }else{
                        count[3]++;
                    }
                }
            }
            // compute probability
            int x0,x1;
            double px0, px1,px0y0, px0y1, px1y0, px1y1;
            x0 = count[0] + count[1];
            x1 = count[2] + count[3];
            px0y0 = (double) count[0]/x0;
            if (px0y0>= 0.5){
                root.left = new TreeNode(0,true);
            }else{
                root.left = new TreeNode(1,true);
            }
            px1y0 = (double) count[2]/x1;
            if (px1y0>=0.5){
                root.right = new TreeNode(0,true);
            }else{
                root.right = new TreeNode(1,true);
            }
            return root;
        }


        // x=0y=0, x=0,y=1, x=1y=0, x=1y=1;
        int [][]count = new int[n][4];

        String []line;
        int num = 0;
        for(String l : lines) {
            line = l.split("\t");
            num++;
            for(int i=0;i<n;i++){
                int f = set.get(i);
                int x = Integer.parseInt(line[f]);
                int y = Integer.parseInt(line[6]);
                if (x==0 && y==0){
                    count[i][0]++;
                }else if (x==0 && y==1){
                    count[i][1]++;
                }else if (x==1 && y==0){
                    count[i][2]++;
                }else{
                    count[i][3]++;
                }
            }
        }

        // compute probability
        double[] prob = new double[n];
        int x0,x1;
        double px0, px1,px0y0, px0y1, px1y0, px1y1;
        for(int i=0;i<n;i++){
            x0 = count[i][0] + count[i][1];
            x1 = count[i][2] + count[i][3];
            px0 = (double) x0/num;
            px1 = 1.0 - px0;
            px0y0 = (double) count[i][0]/x0;
            px0y1 = (double) count[i][1]/x0;
            px1y0 = (double) count[i][2]/x1;
            px1y1 = (double) count[i][3]/x1;
            prob[i] = px0*(-px0y0*Math.log(px0y0)-px0y1*Math.log(px0y1))
                    + px1*(-px1y0*Math.log(px1y0) - px1y1*Math.log(px1y1));
        }

        int index = 0;
        for(int i=1;i<n;i++){
            if (prob[i]<prob[index])
                index = i;
            //System.out.println(prob[i-1]);
        }

        int curFeature = set.get(index);
        set.remove(index);

        // read again and split the file.
        StringBuffer leftFile = new StringBuffer();
        StringBuffer rightFile = new StringBuffer();
        for(String l : lines){
            line = l.split("\t");
            //System.out.println(l);
            if (line[curFeature].equals("0")){
                leftFile.append(l + "\n");
            }else{
                rightFile.append(l + "\n");
            }
        }

        // System.out.println(curFeature);
        root = new TreeNode(curFeature);
        root.left = getNode(set, leftFile.toString().split("\n"));
        root.right = getNode(set, rightFile.toString().split("\n"));

        return root;
    }
    /**
     * Get the Current feature node
     * @param filename
     * @param set0
     * @return
     * @throws IOException
     */
    public TreeNode getNode(String filename, ArrayList<Integer> set0) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        ArrayList<Integer> set = new ArrayList<>(set0);
        int n = set.size();
        TreeNode root = null;

        // if the node is in the last level
        if (n==1){
            root = new TreeNode(set.get(0));
            String l = br.readLine();
            String[] line;
            int [] count= new int[4];
            int num = 0;
            while(l!=null){
                line = l.split("\n")[0].split("\t");
                num++;
                for(int i=0;i<n;i++){
                    int f = set.get(i);
                    int x = Integer.parseInt(line[f]);
                    int y = Integer.parseInt(line[6]);
                    if (x==0 && y==0){
                        count[0]++;
                    }else if (x==0 && y==1){
                        count[1]++;
                    }else if (x==1 && y==0){
                        count[2]++;
                    }else{
                        count[3]++;
                    }
                }
                l = br.readLine();
            }

            // compute probability
            int x0,x1;
            double px0, px1,px0y0, px0y1, px1y0, px1y1;
            x0 = count[0] + count[1];
            x1 = count[2] + count[3];
            px0y0 = (double) count[0]/x0;
            if (px0y0>= 0.5){
                root.left = new TreeNode(0,true);
            }else{
                root.left = new TreeNode(1,true);
            }
            px1y0 = (double) count[2]/x1;
            if (px1y0>=0.5){
                root.right = new TreeNode(0,true);
            }else{
                root.right = new TreeNode(1,true);
            }
            return root;
        }


        // x=0y=0, x=0,y=1, x=1y=0, x=1y=1;
        int [][]count = new int[n][4];

        try {
            String []line;
            String l = br.readLine();
            int num = 0;
            while (l != null) {
                line = l.split("\t");
                num++;
                for(int i=0;i<n;i++){
                    int f = set.get(i);
                    int x = Integer.parseInt(line[f]);
                    int y = Integer.parseInt(line[6]);
                    if (x==0 && y==0){
                        count[i][0]++;
                    }else if (x==0 && y==1){
                        count[i][1]++;
                    }else if (x==1 && y==0){
                        count[i][2]++;
                    }else{
                        count[i][3]++;
                    }
                }
                l = br.readLine();
            }
            br.close();

            // compute probability
            double[] prob = new double[n];
            int x0,x1;
            double px0, px1,px0y0, px0y1, px1y0, px1y1;
            for(int i=0;i<n;i++){
                x0 = count[i][0] + count[i][1];
                x1 = count[i][2] + count[i][3];
                px0 = (double) x0/num;
                px1 = 1.0 - px0;
                px0y0 = (double) count[i][0]/x0;
                px0y1 = (double) count[i][1]/x0;
                px1y0 = (double) count[i][2]/x1;
                px1y1 = (double) count[i][3]/x1;
                prob[i] = px0*(-px0y0*Math.log(px0y0)-px0y1*Math.log(px0y1))
                        + px1*(-px1y0*Math.log(px1y0) - px1y1*Math.log(px1y1));
            }

            int index = 0;
            for(int i=1;i<n;i++){
                if (prob[i]<prob[index])
                    index = i;
                //System.out.println(prob[i-1]);
            }

            int curFeature = set.get(index);
            set.remove(index);

            // read again and split the file.
            String leftFile = String.valueOf(curFeature) + "left";
            String rightFile = String.valueOf(curFeature) + "right";
            BufferedWriter left = new BufferedWriter(new FileWriter(leftFile));
            BufferedWriter right = new BufferedWriter(new FileWriter(rightFile));

            br = new BufferedReader(new FileReader(filename));
            l = br.readLine();
            while(l!=null){
                line = l.split("\t");
                //System.out.println(l);
                if (line[curFeature].equals("0")){
                    left.write(l+"\n");
                }else{
                    right.write(l+"\n");
                }
                l = br.readLine();
            }

            left.close();
            right.close();

            // System.out.println(curFeature);
            root = new TreeNode(curFeature);
            root.left = getNode(leftFile,set);
            root.right = getNode(rightFile,set);

        } finally {
            br.close();

        }
        return root;
    }



}