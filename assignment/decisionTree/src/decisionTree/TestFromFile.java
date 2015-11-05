package decisionTree;

/**
 * Hailun Zhu
 * ID: hailunz
 * Date: 11/4/15
 */


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.ArrayList;

public class TestFromFile implements Serializable{

    public ArrayList<TreeNode> getRandomForest(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        Gson gson = new GsonBuilder().create();
        String line= br.readLine();
        RandomForest rf1 = gson.fromJson(line, RandomForest.class);
        br.close();
        return rf1.forest;
    }

    public double testRandomForest(String treeFile, String testFile) throws IOException {
        double res = 0.0;
        res = testRandomForest(testFile,getRandomForest(treeFile));
        return res;
    }
    /**
     * testRandomForest
     * @param filename
     * @param forest
     * @return
     * @throws IOException
     */
    public double testRandomForest(String filename, ArrayList<TreeNode> forest) throws IOException {
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
     * testTree : use the decision tree to test test data
     * @param filename
     * @param root
     * @return
     * @throws IOException
     */
    public double testTree(String filename, TreeNode root) throws IOException {
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
    public int getLabel(TreeNode root, String[] line){
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
