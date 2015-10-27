import com.datastax.driver.core.*;
import database.Database;
import database.Util;
import decisionTree.RandomForest;
import decisionTree.Test;
import decisionTree.TreeNode;


import java.io.*;
import java.util.ArrayList;


/**
 * Hailun Zhu
 * ID: hailunz
 * Date: 9/25/15
 */

public class Main {


    public static void main(String[] args) throws IOException {

        Util util = new Util();
        util.getDBProperties("db.properties");

        Database db = new Database(util.map.get("ip"),util.map.get("keyspace"));
        db.connectDB();

        RandomForest rf = new RandomForest(5,"train",db);
        rf.setForest();
        ArrayList<TreeNode> forest = rf.getForest();

        Test test = new Test(db);
        double correct = test.testRandomForest("test",forest);
        System.out.println("Accuracy:");
        System.out.println(correct);

        for(TreeNode root : forest){
            root.BFS();
            correct = test.testTree("test", root);
            System.out.println(correct);
        }

        db.close();
    }

    public void loadTrainData() throws IOException {
        String trainFile = "/Users/hailunzhu/cmu/course/11676/parseData/train";
        Util util = new Util();
        util.getDBProperties("db.properties");
        util.loadDataIntoDB(util.map.get("ip"),util.map.get("keyspace"),"train",trainFile);
    }

    public void loadTestData() throws IOException {
        String trainFile = "/Users/hailunzhu/cmu/course/11676/parseData/test";
        Util util = new Util();
        util.getDBProperties("db.properties");
        util.loadDataIntoDB(util.map.get("ip"),util.map.get("keyspace"),"test",trainFile);
    }

}
