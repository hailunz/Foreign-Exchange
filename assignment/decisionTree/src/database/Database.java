package database;

import com.datastax.driver.core.*;
import com.datastax.driver.core.utils.Bytes;
import decisionTree.TreeNode;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;

/**
 * Hailun Zhu
 * ID: hailunz
 * Date: 10/26/15
 */
public class Database {

    Cluster cluster;

    String ip;
    String keyspace;
    public Session session;

    public Database(String ip,String keyspace){
        this.ip = ip;
        this.keyspace = keyspace;
    }

    public void connectDB(){
        cluster = Cluster.builder().addContactPoint(ip).build();
        session = cluster.connect(keyspace);
    }

    public void execute(String query){
        this.session.execute(query);
    }

    public ResultSet executeWithResult(String query){
        return session.execute(query);
    }

    public boolean tableExist(String tableName){
        KeyspaceMetadata ks = cluster.getMetadata().getKeyspace(this.keyspace);
        TableMetadata table = ks.getTable(tableName);
        return table != null;
    }

    public ResultSet selectAllFromTable(String tablename){
        String query = "Select * from "+ tablename;
        return session.execute(query);
    }

    public void truncate(String tableName){
        session.execute("truncate " + tableName);
    }

    public void createTable(String tableName){
        session.execute("create table if not exists " +
                tableName + "(lineID int primary key,f0 int,f1 int,f2 int," +
                "f3 int,f4 int,f5 int,label int);");
    }

    public void insertRow(String tableName, String row){
        session.execute("INSERT INTO " + tableName + "(lineID, f0, f1, f2, f3,f4,f5, label) VALUES ("
        + row + ")");
    }

    public void insertResult(String tableName,Date timestamp, String row){
        session.execute("INSERT INTO " + tableName
                + "(timestamp, forest,tree0,tree1,tree2,tree3,tree4) values ("
                + timestamp.getTime() + ","
                + row + ")");
    }

    public void insertForestByte(String tableName,Date timestamp, ArrayList<TreeNode> forest) throws IOException {
        System.out.println(timestamp);
        PreparedStatement ps = session.prepare("insert into "+ tableName + "(uuid, timestamp, object,length) values (now(),?,?,?)");
        BoundStatement boundStatement = new BoundStatement(ps);

        for(TreeNode root: forest){
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = null;
            out = new ObjectOutputStream(bos);
            out.writeObject(root);
            byte[] bytes = bos.toByteArray();
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            session.execute(boundStatement.bind(timestamp,buf,bytes.length));
        }
    }

    public void insertTreeByte(String tableName,Date timestamp, TreeNode root) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        out = new ObjectOutputStream(bos);
        out.writeObject(root);
        byte[] bytes = bos.toByteArray();
        ByteBuffer buf = ByteBuffer.wrap(bytes);

        PreparedStatement ps = session.prepare("insert into "+ tableName + "(uuid, timestamp, object,length) values (now(),?,?,?)");
        BoundStatement boundStatement = new BoundStatement(ps);
        session.execute(boundStatement.bind(timestamp,buf,bytes.length));
    }

    public ArrayList<TreeNode> getForestFromTable(String tableName,Date time){
        ArrayList<TreeNode> forest = new ArrayList<>();

        PreparedStatement ps1 = session.prepare("select object,length from " + tableName +
                " where timestamp=?");

        BoundStatement boundStatement = new BoundStatement(ps1);
        ResultSet rs = session.execute(boundStatement.bind(time));

        ByteBuffer buf=null;
        int length =0 ;
        for (Row row : rs) {
            buf = row.getBytes("object");
            length = row.getInt("length");
            byte bytes[]= new byte[length];
            bytes= Bytes.getArray(buf);

            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInput in = null;

            try {
                in = new ObjectInputStream(bis);
                TreeNode o = (TreeNode) in.readObject();
                forest.add(o);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return forest;
    }

    public TreeNode getTreeByte(String tableName, Date time){
        PreparedStatement ps1 = session.prepare("select object,length from " + tableName +
                " where timestamp=?");

        BoundStatement boundStatement = new BoundStatement(ps1);
        ResultSet rs = session.execute(boundStatement.bind(time));

        ByteBuffer buf=null;
        int length =0 ;
        for (Row row : rs) {
            buf = row.getBytes("object");
            length = row.getInt("length");
        }

        byte bytes[]= new byte[length];
        bytes= Bytes.getArray(buf);

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;

        try {
            in = new ObjectInputStream(bis);
            TreeNode o = (TreeNode) in.readObject();
            return o;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close(){
        this.cluster.close();
    }
}
