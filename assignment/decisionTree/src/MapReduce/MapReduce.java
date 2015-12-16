//package MapReduce;
//
///**
// * Hailun Zhu
// * ID: hailunz
// * Date: 11/1/15
// */
//import java.io.*;
//import java.util.*;
//
//import com.datastax.driver.core.ResultSet;
//import com.datastax.driver.core.Row;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import database.Database;
//import decisionTree.DecisionTree;
//import decisionTree.RandomForest;
//import decisionTree.TreeNode;
//import org.apache.cassandra.hadoop.ColumnFamilyInputFormat;
//import org.apache.cassandra.hadoop.ConfigHelper;
//import org.apache.cassandra.thrift.SlicePredicate;
//import org.apache.cassandra.thrift.SliceRange;
//import org.apache.cassandra.utils.ByteBufferUtil;
//import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.conf.*;
//import org.apache.hadoop.io.*;
//import org.apache.hadoop.mapreduce.*;
//import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
//import org.apache.hadoop.mapreduce.lib.input.FileSplit;
//import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
//import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
//import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
//
//import static org.apache.cassandra.utils.ByteBufferUtil.*;
//
//public class MapReduce {
//
//    public class Map extends Mapper<LongWritable, Text, Text, Text> {
//        private Text forest = new Text();
//        private Text tree = new Text();
//        private HashMap<Integer, ArrayList<Integer>> features = new HashMap<>();
//        private int lineNum ;
//
//        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
//            forest.set("forest");
//            String lines = value.toString();
//            String []line = lines.split("\n");
//            int index = (int)key.get()/lineNum;
//            ArrayList<Integer> set = features.get(index);
//
//            DecisionTree dt = new DecisionTree();
//            TreeNode root = dt.getNode(set, value.toString().split("\n"));
//
//            // get Json
//            Gson gson = new GsonBuilder().create();
//            String treeJson = gson.toJson(root);
//
//            // output
//            tree.set(treeJson);
//            context.write(forest,tree);
//        }
//
//        protected void setup(Context context) throws IOException, InterruptedException {
//            Configuration conf = context.getConfiguration();
//            lineNum = conf.getInt("lineNum", 100);
//            features = new HashMap<>();
//
//            // get forest feature randomly
//            String [] set = conf.get("featureSet").split("\n");
//            String []f ;
//            for(int k=0;k<set.length;k++) {
//                f = set[k].split(",");
//                ArrayList<Integer> tmp = new ArrayList<>();
//
//                for (String i: f){
//                    tmp.add(Integer.parseInt(i));
//                }
//                features.put(k,tmp);
//            }
//        }
//
//    }
//
//    public class Reduce extends Reducer<Text, Text, Text, Text> {
//
//        public void reduce(Text key, Iterable<Text> values, Context context)
//                throws IOException, InterruptedException {
//            int count = 0;
//            ArrayList<TreeNode> forest = new ArrayList<>();
//            Gson gson = new GsonBuilder().create();
//
//            for (Text val : values) {
//                TreeNode root = gson.fromJson(val.toString(), TreeNode.class);
//                forest.add(root);
//            }
//
//            RandomForest rf = new RandomForest(forest);
//            context.write(key, new Text(gson.toJson(rf)));
//        }
//    }
//
//
//    public void main(String[] args) throws Exception {
//
//        Configuration conf = new Configuration();
//
//        String IP = args[0];
//        String KEYSPACE = args[1];
//        String TABLE = args[2];
//        int N = Integer.parseInt(args[3]);
//        String output = args[4];
//
//        Database db = new Database(IP,KEYSPACE);
//
//        String url = "inputFile";
//        convertDBtoFile(db, TABLE, url, N);
//
//        String query = "select count(*) from " + TABLE + " ;";
//        ResultSet result = db.executeWithResult(query);
//        Row r = result.one();
//        int count = r.getInt(0);
//        // get number of lines for one tree
//        int lineNum = count/N;
//
//        conf.setInt("treeNumber", N);
//        int featureNum = (int) Math.ceil(Math.sqrt((double)N));
//        conf.set("featureSet", storeFeatureSet(N, featureNum));
//
//        Job job = Job.getInstance(conf, "RandomForest");
//        job.setJarByClass(RandomForest.class);
//
//        job.setOutputKeyClass(Text.class);
//        job.setOutputValueClass(Text.class);
//
//        job.setMapperClass(Map.class);
//        job.setCombinerClass(Reducer.class);
//        job.setReducerClass(Reducer.class);
//        job.setNumReduceTasks(1);
//
//        FileOutputFormat.setOutputPath(job, new Path("output"));
//
//        job.setInputFormatClass(NLineInputFormat.class);
//
//        ConfigHelper.setInputRpcPort(job.getConfiguration(), "9160");
//        ConfigHelper.setInputInitialAddress(job.getConfiguration(), "10.0.0.4");
//        ConfigHelper.setInputPartitioner(job.getConfiguration(), "org.apache.cassandra.dht.RandomPartitioner");
//        ConfigHelper.setInputColumnFamily(job.getConfiguration(), "bigdata", "train");
//        ConfigHelper.setRangeBatchSize(conf, lineNum); // Total 10
//        // trees
//
//        SlicePredicate predicate = new SlicePredicate()
//                .setSlice_range(new SliceRange().setStart(EMPTY_BYTE_BUFFER)
//                        .setFinish(EMPTY_BYTE_BUFFER).setCount(10000));
//        ConfigHelper.setInputSlicePredicate(job.getConfiguration(), predicate);
//
//        job.waitForCompletion(true);
//    }
//
//    public String storeFeatureSet(int N, int featureNum) {
//        RandomForest rf = new RandomForest(N, "");
//        HashSet<ArrayList<Integer>> featureSet = new HashSet<>();
//        StringBuffer sb = new StringBuffer();
//
//        ArrayList<Integer> list ;
//        for(int i=0;i<N;){
//            list = rf.getFeatureSet(featureSet, featureNum);
//            if (list !=null){
//                i++;
//                for(int j : list) {
//                    sb.append(j);
//                    sb.append(",");
//                }
//                sb.append("\n");
//            }
//        }
//      return sb.toString();
//    }
//
//    public void convertDBtoFile(Database db, String tablename, String url, int N) throws IOException {
//        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
//                new FileOutputStream(url), "utf-8"));
//
//        ResultSet results = db.selectAllFromTable(tablename);
//
//        StringBuffer sb ;
//
//        for(Row row : results){
//            sb = new StringBuffer();
//            for(int i=1;i<=7;i++){
//                sb.append(row.getInt(i));
//                sb.append("\t");
//            }
//            sb.deleteCharAt(sb.length()-1);
//            sb.append("\n");
//            wr.write(sb.toString());
//        }
//
//        wr.close();
//    }
//}
