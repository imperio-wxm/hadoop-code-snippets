package com.wxmimperio.spark;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.util.*;

public class SparkSessionShow {
    private static final Logger LOG = LoggerFactory.getLogger(SparkSessionShow.class);

    public static void main(String[] args) {
        try (JavaSparkContext sc = new JavaSparkContext();
             SparkSession sparkSession = SparkSession.builder().sparkContext(sc.sc()).getOrCreate()) {
            List<String> wordList = new ArrayList<>();
            wordList.add("While this code used the built-in support for accumulators of type Long, programmers can also create their own types by subclassing");
            wordList.add("While this code used the built-in support for accumulators of type Long, programmers can also create their own types by subclassing");
            wordList.add("While this code used the built-in support for accumulators of type Long, programmers can also create their own types by subclassing");
            JavaRDD<String> lines = sc.parallelize(wordList);

            JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
                private static final long serialVersionUID = 1L;

                @Override
                public Iterator<String> call(String line) {

                    return Arrays.asList(line.split(" ")).iterator();
                }
            });

            JavaPairRDD<String, Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>() {
                private static final long serialVersionUID = 1L;

                @Override
                public Tuple2<String, Integer> call(String word) {
                    return new Tuple2<>(word, 1);
                }
            });

            for (int i = 0; i < 10; i++) {
                String groupId = UUID.randomUUID().toString();
                sparkSession.sparkContext().setJobGroup("Group Id", groupId, true);
                LOG.info("Spark Session = " + sparkSession.toString() + ", version = " + sparkSession.version());
                Dataset<Tuple2<String, Integer>> dataset = sparkSession.createDataset(pairs.collect(), Encoders.tuple(Encoders.STRING(), Encoders.INT()));
                dataset.show(true);
                sc.cancelJobGroup(groupId);
                LOG.info("Cancel groupId = " + groupId);
            }
        }
    }
}