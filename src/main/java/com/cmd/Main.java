package com.cmd;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static Pattern PATTERN = Pattern.compile("([0-9-: .]+)\\s+([A-Z]+)\\s+\\(([A-Za-z0-9-:_ /]+)\\)\\s+\\[([A-Za-z0-9-:_ /]+)\\](.*)");
    public static void main(String[] args) throws IOException {
        args = new String[2];
        args[0] = "/Users/caomanhdat/tmp/tmp.txt";
        args[1] = "/Users/caomanhdat/tmp";
        File input = new File(args[0]);
        Map<String, List<Data>> threadNameToData = new HashMap<>();
        Map<String, List<Data>> replicaToData = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(input))) {
            String line;
            Data lastData = null;
            while ( (line = reader.readLine()) != null) {
                String[] parts = separate(line);
                if (parts == null && lastData != null) {
                    if (line.startsWith("2016-")) {
                        System.out.println(line);
                    }
                    lastData.append(line);
                } else {
                    lastData = new Data(line,parts);
                    if (!threadNameToData.containsKey(lastData.threadName)) {
                        threadNameToData.put(lastData.threadName, new ArrayList<Data>());
                    }
                    threadNameToData.get(lastData.threadName).add(lastData);

                    if (!replicaToData.containsKey(lastData.xreplica)) {
                        replicaToData.put(lastData.xreplica, new ArrayList<Data>());
                    }
                    replicaToData.get(lastData.xreplica).add(lastData);
                }
            }
        }

        File outFolder = new File(args[1]);
        outFolder.mkdirs();
        for (Map.Entry<String, List<Data>> entry : replicaToData.entrySet()) {
            String fileName = entry.getKey().replace(":","_");
            if (entry.getKey().trim().length() == 0) {
                fileName = input.getName()+"startup";
            }
            File file = new File(outFolder.getAbsolutePath()+"/"+fileName);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (Data data : entry.getValue()) {
                writer.write(data.all.toString());
                writer.write("\n");
            }
            writer.close();

        }
    }

    private static class Data {
        String time;
        String level;
        String threadName;
        String replica;
        String xreplica;
        StringBuilder builder = new StringBuilder();
        StringBuilder all = new StringBuilder();

        public Data(String all, String[] parts) {
            this.all.append(all);
            this.time = parts[0];
            this.level = parts[1];
            this.threadName = parts[2];
            this.replica = parts[3];
            try {
                this.xreplica = replica.substring(replica.indexOf("x:")).trim();
            } catch (StringIndexOutOfBoundsException e) {
                this.xreplica = replica;
            }

            this.builder.append(parts[4]);
        }

        public void append(String line) {
            builder.append(line+"\n");
            all.append(line+"\n");
        }

    }

    private static String[] separate(String line) {
        Matcher m = PATTERN.matcher(line);
        if (m.find()) {
            String[] strs = new String[m.groupCount()];
            for (int i = 1; i <= m.groupCount(); i++) {
                strs[i-1] = m.group(i);
            }
            return strs;
        } else {
            return null;
        }
    }
}
