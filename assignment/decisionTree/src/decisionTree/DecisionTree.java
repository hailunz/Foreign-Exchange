package decisionTree;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import database.Database;

import java.io.*;
import java.util.ArrayList;

/**
 * Hailun Zhu
 * ID: hailunz
 * Date: 10/25/15
 */
public class DecisionTree implements Serializable {


    TreeNode root;
    Database db;

    public void setRoot(String tablename, ArrayList<Integer> set0, Database db) throws IOException {
        root = getNode(tablename,set0);
        this.db = db;
    }

    public TreeNode getRoot() throws IOException {
        return root;
    }

    /**
     * Get the Current feature node
     * @param tablename
     * @param set0
     * @return
     * @throws IOException
     */
    public TreeNode getNode(String tablename, ArrayList<Integer> set0) throws IOException {
        ResultSet results = db.selectAllFromTable(tablename);

        ArrayList<Integer> set = new ArrayList<>(set0);
        int n = set.size();
        TreeNode root = null;

        // if the node is in the last level
        if (n==1){
            root = new TreeNode(set.get(0));
            int [] count= new int[4];
            int num = 0;
            for(Row row : results){
                num++;
                for(int i=0;i<n;i++){
                    int f = set.get(i);
                    int x = row.getInt(f+1);
                    int y = row.getInt(7);
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

        try {
            int num = 0;
            for(Row row : results){
                num++;
                for(int i=0;i<n;i++){
                    int f = set.get(i);
                    int x = row.getInt(f+1);
                    int y = row.getInt(7);

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
            String left =  "left" + String.valueOf(curFeature);
            String right = "right" + String.valueOf(curFeature) ;

            if (db.tableExist(left))
                db.truncate(left);
            else
                db.createTable(left);

            if (db.tableExist(right))
                db.truncate(right);
            else
                db.createTable(right);

            results = db.selectAllFromTable(tablename);

            for(Row row: results){
                String r = row.toString();
                if (row.getInt(curFeature+1)==0){
                    db.insertRow(left,r.substring(4,r.length()-1));
                }else{
                    db.insertRow(right,r.substring(4,r.length()-1));
                }
            }

            // System.out.println(curFeature);
            root = new TreeNode(curFeature);
            root.left = getNode(left,set);
            root.right = getNode(right,set);

        }catch (Exception e){
            e.printStackTrace();
        }
        return root;
    }

}
