package com.cmd;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BenchmarkQuery {

    static Random r = new Random();

    public static void main(String[] args) throws IOException, SolrServerException, InterruptedException {
//        final SolrClient client = new CloudSolrClient.Builder().withZkHost("localhost:9983").build();
        final SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr").build();
        final AtomicInteger count = new AtomicInteger();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            long prevTime;
            int oldCount;
            @Override
            public void run() {
                int currentCount = count.get();
                long currentTime = System.currentTimeMillis();
                if (prevTime != 0) {
                    double qps = (double) (currentCount - oldCount) /  (currentTime - prevTime);
                    System.out.println("QPS " + qps);
                }
                prevTime = currentTime;
                oldCount = currentCount;
            }
        }, 0, 1000);
        ExecutorService ser = Executors.newFixedThreadPool(500);
        for (int i = 0; i < 1000000; i++ ){
            ser.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        SolrQuery query = new SolrQuery("c_txt_en", randomStr());
                        client.query("gettingstarted", query);
                        count.incrementAndGet();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        ser.shutdown();
        System.out.println("wait");
        ser.awaitTermination(1000, TimeUnit.MINUTES);
        client.close();
        timer.cancel();
    }

    private static String randomStr() {
        int wordLen = r.nextInt(7) + 2;
        return RandomStringUtils.random(wordLen, true, true);

    }
}
