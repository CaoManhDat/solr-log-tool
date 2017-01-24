package com.cmd;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class BenchmarkIndexing {

    static Random r = new Random();

    public static void main(String[] args) throws IOException, SolrServerException, InterruptedException {
//        final CloudSolrClient client = new CloudSolrClient.Builder().withZkHost("localhost:9983").build();
        final SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr").build();
        ExecutorService ser = Executors.newFixedThreadPool(500);
        for (int i = 0; i < 50000; i++ ){
            ser.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        UpdateResponse res = client.add("gettingstarted", randomDoc());
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
    }

    private static List<SolrInputDocument> randomDoc() {
        List<SolrInputDocument> arr = new ArrayList<>();
        int numDoc = r.nextInt(30) + 2;
        for (int j = 0; j < numDoc; j++) {
            SolrInputDocument doc = new SolrInputDocument();
            StringBuilder builder = new StringBuilder();
            int len = r.nextInt(100) + 10;
            for (int i = 0; i < len; i++) {
                int wordLen = r.nextInt(7) + 2;
                builder.append(RandomStringUtils.random(wordLen, true, true)).append(' ');
            }
            doc.setField("c_txt_en", builder.toString());
//            return doc;
            arr.add(doc);
        }

        return arr;
    }
}
