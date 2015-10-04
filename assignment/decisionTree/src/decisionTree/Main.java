package decisionTree;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

/**
 * Hailun Zhu
 * ID: hailunz
 * Date: 9/25/15
 */

public class Main {


    public static void main(String[] args) throws IOException {

        int N = 5;
        String trainFile = "/Users/hailunzhu/cmu/course/11676/parseData/train";
        ArrayList<TreeNode> forest = randomForest(N,trainFile);

        // test the decision tree
        String testFile = "/Users/hailunzhu/cmu/course/11676/parseData/test";

        double correct = testRandomForest(testFile,forest);
        System.out.println("Accuracy:");
        System.out.println(correct);

        for(TreeNode root : forest){
            root.BFS();
            correct = testTree(testFile, root);
            System.out.println(correct);
        }

    }

    /**
     * Add new features' set to the featureSet
     * @param featureSet
     * @param featureNum
     * @return
     */
    public static boolean getFeature(HashSet<ArrayList<Integer>> featureSet, int featureNum){
        Random ran = new Random();
        ArrayList<Integer> set = new ArrayList<>();
        HashSet<Integer> currentFeatures = new HashSet<>();
        int x ;
        int count = 0;
        while(count<featureNum){
            x = ran.nextInt(6);
            while(currentFeatures.contains(x))
                x = ran.nextInt(6);

            currentFeatures.add(x);
            set.add(x);
            count++;
        }

        Collections.sort(set);
        if (featureSet.contains(set)){
            return false;
        }else{
            featureSet.add(set);
        }
        return true;
    }

    /**
     * Get a list of root of the randomForest
     * @param N
     * @param filename
     * @return
     * @throws IOException
     */
    public static ArrayList<TreeNode> randomForest(int N,String filename) throws IOException {

        int featureNum = 3;
        HashSet<ArrayList<Integer>> featureSet = new HashSet<>();
        ArrayList<TreeNode> forest = new ArrayList<>();

        for(int i=0;i<N;){
           if (getFeature(featureSet,featureNum)){
               i++;
           }
        }

        for(ArrayList<Integer> set : featureSet){
            TreeNode root = getNode(filename,set);
            forest.add(root);
        }

        return forest;
    }

    /**
     * testRandomForest
     * @param filename
     * @param forest
     * @return
     * @throws IOException
     */
    public static double testRandomForest(String filename, ArrayList<TreeNode> forest) throws IOException {
        double res = 0.0;
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String l = br.readLine();
        String line[] = null;
        int correct = 0;
        int count = 0;
        int realLabal = 0;
        int testLabel = 0;

        int count0 = 0;
        int count1 = 0;

        while(l != null){
            count0 = 0;
            count1 = 0;
            count++;
            line = l.split("\t");
            realLabal = Integer.parseInt(line[6]);

            for(TreeNode root : forest){
                testLabel = getLabel(root,line);
                if (testLabel == 1)
                    count1++;
                else
                    count0++;
            }

            testLabel = count1 >= count0 ? 1: 0;
            correct += realLabal == testLabel ?1:0;
            l = br.readLine();
        }
        res = (double) correct/count;
        br.close();
        return res;
    }

    /**
     * Get the Current feature node
     * @param filename
     * @param set0
     * @return
     * @throws IOException
     */
    public static TreeNode getNode(String filename, ArrayList<Integer> set0) throws IOException {
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


    /**
     * testTree : use the decision tree to test test data
     * @param filename
     * @param root
     * @return
     * @throws IOException
     */
    public static double testTree(String filename, TreeNode root) throws IOException {
        double res = 0.0;
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String l = br.readLine();
        String line[] = null;
        int correct = 0;
        int count = 0;
        int realLabal = 0;
        int testLabel = 0;
        while(l!=null){
            count++;
            line = l.split("\t");
            realLabal = Integer.parseInt(line[6]);
            testLabel = getLabel(root,line);
            correct += realLabal == testLabel ?1:0;
            l = br.readLine();
        }
        res = (double) correct/count;
        br.close();
        return res;
    }

    /**
     * get the current feature vector's label
     * @param root
     * @param line
     * @return
     */
    public static int getLabel(TreeNode root, String[] line){
        int label = 0;
        TreeNode node = root;
        int feature;
        int testF;
        while(node!=null && !node.isLabel){
            feature = node.feature;
            testF = Integer.parseInt(line[feature]);
            if (testF == 0)
                node = node.left;
            else
                node = node.right;
        }
        return node.label;
    }

}
