import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;  // Changed from IntWritable to FloatWritable
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;

public class RatingCount {

  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, FloatWritable> {

    private final static FloatWritable rating = new FloatWritable();  // Changed to FloatWritable
    private Text product = new Text();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString());
      while (itr.hasMoreTokens()) {
        String productName = itr.nextToken();
        int quantity = Integer.parseInt(itr.nextToken());
        String price = itr.nextToken();
        float productRating = Float.parseFloat(itr.nextToken());

        product.set(productName);
        rating.set(productRating);

        context.write(product, rating);
      }
    }
  }

  public static class FloatSumReducer
       extends Reducer<Text, FloatWritable, Text, FloatWritable> {

    private FloatWritable result = new FloatWritable();

    public void reduce(Text key, Iterable<FloatWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
      float sum = 0.0f;
      int count = 0;

      for (FloatWritable val : values) {
        sum += val.get();
        count++;
      }

      float averageRating = sum / count;
      result.set(averageRating);

      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "rating count");
    job.setJarByClass(RatingCount.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(FloatSumReducer.class);
    job.setReducerClass(FloatSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(FloatWritable.class);  // Changed from IntWritable to FloatWritable
    
    // Option 1: splits given by the NumLines
    job.setInputFormatClass(NLineInputFormat.class);
    NLineInputFormat.addInputPath(job, new Path(args[0]));
    NLineInputFormat.setNumLinesPerSplit(job, 1);
    
    // Option 2: default splits
    // FileInputFormat.addInputPath(job, new Path(args[0]));
    
    Path output_path = new Path(args[1]);
    FileOutputFormat.setOutputPath(job, output_path);
    
    if (output_path.getFileSystem(conf).exists(output_path))
      output_path.getFileSystem(conf).delete(output_path, true);

    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
