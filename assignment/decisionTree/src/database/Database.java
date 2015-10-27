package database;

import com.datastax.driver.core.*;

/**
 * Hailun Zhu
 * ID: hailunz
 * Date: 10/26/15
 */
public class Database {

    Cluster cluster;
    Session session;
    String ip;
    String keyspace;

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

    public void close(){
        this.cluster.close();
    }
}
