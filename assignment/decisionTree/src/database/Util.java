package database;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Hailun Zhu
 * ID: hailunz
 * Date: 10/26/15
 */
public class Util {

    public HashMap<String,String> map;
    public Util(){
        map = new HashMap<>();
    }

    public void getDBProperties(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line = null;
        line = br.readLine();
        String []props;
        while(line!=null){
            props = line.split("=");
            map.put(props[0],props[1]);
            line = br.readLine();
        }
        br.close();
    }

    public String getInsertQuery(String tableName,int num, String line){
        String res = "INSERT INTO " + tableName + " (lineID, f0, f1, f2, f3,f4,f5, label) VALUES (";
        res = res + String.valueOf(num) + "," + line + ")";
        return res;
    }
    public void loadDataIntoDB(String ip, String keyspace, String tableName, String filename)
            throws FileNotFoundException {
        Database db = new Database(ip,keyspace);
        db.connectDB();

        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        String l = null;
        try {
            l = br.readLine();
            int num = 0;
            while (l != null) {
                line = l.replaceAll("\t",",");
                db.execute(getInsertQuery(tableName,num, line));
                num++;
                l = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        db.close();
    }
}
