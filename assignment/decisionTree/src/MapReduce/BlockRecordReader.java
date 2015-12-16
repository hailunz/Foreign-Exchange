//package MapReduce;
//
//import org.apache.hadoop.io.LongWritable;
//import org.apache.hadoop.io.Text;
//import org.apache.hadoop.mapred.*;
//
//import java.io.IOException;
//
//
///**
// * Hailun Zhu
// * ID: hailunz
// * Date: 11/1/15
// */
//public class BlockRecordReader implements RecordReader<LongWritable, Text> {
//        private LineRecordReader lineRecord;
//        private LongWritable lineKey;
//        private Text lineValue;
//
//        public BlockRecordReader(JobConf conf, FileSplit split) throws IOException {
//            lineRecord = new LineRecordReader(conf, split);
//            lineKey = lineRecord.createKey();
//            lineValue = lineRecord.createValue();
//        }
//
//        @Override
//        public void close() throws IOException {
//            lineRecord.close();
//        }
//
//        @Override
//        public LongWritable createKey() {
//            return new LongWritable();
//
//        }
//
//        @Override
//        public Text createValue() {
//            return new Text("");
//
//        }
//
//        @Override
//        public float getProgress() throws IOException {
//            return lineRecord.getPos();
//
//        }
//
//        @Override
//        public synchronized boolean next(LongWritable key, Text value) throws IOException {
//            boolean appended, isNextLineAvailable;
//            boolean retval;
//            byte space[] = {' '};
//            value.clear();
//            isNextLineAvailable = false;
//            do {
//                appended = false;
//                retval = lineRecord.next(lineKey, lineValue);
//                if (retval) {
//                    if (lineValue.toString().length() > 0) {
//                        byte[] rawline = lineValue.getBytes();
//                        int rawlinelen = lineValue.getLength();
//                        value.append(rawline, 0, rawlinelen);
//                        value.append(space, 0, 1);
//                        appended = true;
//                    }
//                    isNextLineAvailable = true;
//                }
//            } while (appended);
//
//            return isNextLineAvailable;
//        }
//
//        @Override
//        public long getPos() throws IOException {
//            return lineRecord.getPos();
//        }
//
//}
