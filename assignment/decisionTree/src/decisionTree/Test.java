package decisionTree;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import database.Database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Hailun Zhu
 * ID: hailunz
 * Date: 10/26/15
 */
public class Test implements Serializable{
    public Database db;
    public Test(Database db){
        this.db = db;
    }

    /**
     * testRandomForest
     * @param tablename
     * @param forest
     * @return
     * @throws IOException
     */
    public double testRandomForest(String tablename, ArrayList<TreeNode> forest) throws IOException {
        double res = 0.0;
        ResultSet results = db.selectAllFromTable(tablename);

        int line[] = new int[6];

        int correct = 0;
        int count = 0;
        int realLabal = 0;
        int testLabel = 0;

        int count0 = 0;
        int count1 = 0;

        for(Row row: results){
            count0 = 0;
            count1 = 0;
            count++;
            realLabal = row.getInt("label");

            for (int i=0;i<6;i++){
                line[i] = row.getInt(i+1);
            }

            for(TreeNode root : forest){
                testLabel = getLabel(root,line);
                if (testLabel == 1)
                    count1++;
                else
                    count0++;
            }

            testLabel = count1 >= count0 ? 1: 0;
            correct += realLabal == testLabel ?1:0;

        }
        res = (double) correct/count;
        return res;
    }


    /**
     * testTree : use the decision tree to test test data
     * @param tablename
     * @param root
     * @return
     * @throws IOException
     */
    public double testTree(String tablename, TreeNode root) throws IOException {
        double res = 0.0;

        ResultSet results = db.selectAllFromTable(tablename);

        int line[] = new int[6];
        int correct = 0;
        int count = 0;
        int realLabal = 0;
        int testLabel = 0;
        for(Row row: results){
            count++;

            realLabal = row.getInt("label");

            for (int i=0;i<6;i++){
                line[i] = row.getInt(i+1);
            }

            testLabel = getLabel(root,line);
            correct += realLabal == testLabel ?1:0;

        }
        res = (double) correct/count;
        return res;
    }

    /**
     * get the current feature vector's label
     * @param root
     * @param line
     * @return
     */
    public static int getLabel(TreeNode root, int[] line){
        int label = 0;
        TreeNode node = root;
        int feature;
        int testF;
        while(node!=null && !node.isLabel){
            feature = node.feature;
            testF = line[feature];
            if (testF == 0)
                node = node.left;
            else
                node = node.right;
        }
        return node.label;
    }
}
