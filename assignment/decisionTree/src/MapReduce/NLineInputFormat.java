package MapReduce;

/**
 * Hailun Zhu
 * ID: hailunz
 * Date: 11/4/15
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.apache.hadoop.util.LineReader;

/**
 * NLineInputFormat which splits N lines of input as one split.
 */

public class NLineInputFormat extends FileInputFormat<LongWritable, Text> {
    public static final String LINES_PER_MAP = "mapreduce.input.lineinputformat.linespermap";

    public RecordReader<LongWritable, Text> createRecordReader(
            InputSplit genericSplit, TaskAttemptContext context)
            throws IOException {
        context.setStatus(genericSplit.toString());
        return new LineRecordReader();
    }

    /**
     * Logically splits the set of input files for the job, splits N lines of
     * the input as one split.
     *
     * @see FileInputFormat#getSplits(JobContext)
     */
    public List<InputSplit> getSplits(JobContext job) throws IOException {
        List<InputSplit> splits = new ArrayList<InputSplit>();
        int numLinesPerSplit = getNumLinesPerSplit(job);
        for (FileStatus status : listStatus(job)) {
            splits.addAll(getSplitsForFile(status, job.getConfiguration(),
                    numLinesPerSplit));
        }
        return splits;
    }

    public static List<FileSplit> getSplitsForFile(FileStatus status,
                                                   Configuration conf, int numLinesPerSplit) throws IOException {
        List<FileSplit> splits = new ArrayList<FileSplit>();
        Path fileName = status.getPath();
        if (status.isDir()) {
            throw new IOException("Not a file: " + fileName);
        }
        FileSystem fs = fileName.getFileSystem(conf);
        LineReader lr = null;
        try {
            FSDataInputStream in = fs.open(fileName);
            lr = new LineReader(in, conf);
            Text line = new Text();
            int numLines = 0;
            long begin = 0;
            long length = 0;
            int num = -1;
            while ((num = lr.readLine(line)) > 0) {
                numLines++;
                length += num;
                if (numLines == numLinesPerSplit) {
                    if (begin == 0) {
                        splits.add(new FileSplit(fileName, begin, length - 1,
                                new String[] {}));
                    } else {
                        splits.add(new FileSplit(fileName, begin - 1, length,
                                new String[] {}));
                    }
                    begin += length;
                    length = 0;
                    numLines = 0;
                }
            }
            if (numLines != 0) {
                splits.add(new FileSplit(fileName, begin, length,
                        new String[] {}));
            }
        } finally {
            if (lr != null) {
                lr.close();
            }
        }
        return splits;
    }

    /**
     * Set the number of lines per split
     *
     * @param job
     *            the job to modify
     * @param numLines
     *            the number of lines per split
     */
    public static void setNumLinesPerSplit(Job job, int numLines) {
        job.getConfiguration().setInt(LINES_PER_MAP, numLines);
    }

    /**
     * Get the number of lines per split
     *
     * @param job
     *            the job
     * @return the number of lines per split
     */
    public static int getNumLinesPerSplit(JobContext job) {
        return job.getConfiguration().getInt(LINES_PER_MAP, 1);
    }
}
