package driver;

import com.datastax.driver.core.*;
import com.datastax.driver.core.utils.Bytes;
import database.Database;
import database.Util;
import decisionTree.RandomForest;
import decisionTree.Test;
import decisionTree.TreeNode;


import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;


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

        // get random forest
        RandomForest rf = new RandomForest(5,"train",db);
        rf.setForest();
        ArrayList<TreeNode> forest = rf.getForest();

        // test random forest
        Test test = new Test(db);
        double correct = test.testRandomForest("test",forest);
        System.out.println("Accuracy:");
        System.out.println(correct);

        StringBuffer result = new StringBuffer();
        result.append(correct);
        result.append(',');

        for(TreeNode root : forest){
            root.BFS();
            correct = test.testTree("test", root);
            System.out.println(correct);
            result.append(correct);
            result.append(',');
        }

        result.deleteCharAt(result.length() - 1);

        Date timestamp = new Date();
        System.out.println(timestamp.getTime());

        // insert result and the trees
        db.insertResult("result", timestamp, result.toString());
        db.insertForestByte("tree", timestamp, forest);

        // test getting tree from the database
        forest = db.getForestFromTable("tree",timestamp);

        for(TreeNode root : forest){
            root.BFS();
            correct = test.testTree("test", root);
            System.out.println(correct);
            result.append(correct);
            result.append(',');
        }

        db.close();
    }


}
