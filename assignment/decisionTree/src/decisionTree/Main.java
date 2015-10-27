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

        String trainFile = "/Users/hailunzhu/cmu/course/11676/parseData/train";
        RandomForest rf = new RandomForest(5,trainFile);
        rf.setForest();
        ArrayList<TreeNode> forest = rf.getForest();

        // test the decision tree
        String testFile = "/Users/hailunzhu/cmu/course/11676/parseData/test";

        Test test = new Test();
        double correct = test.testRandomForest(testFile,forest);
        System.out.println("Accuracy:");
        System.out.println(correct);

        for(TreeNode root : forest){
            root.BFS();
            correct = test.testTree(testFile, root);
            System.out.println(correct);
        }

    }


}
