package org.alex;

import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;

import java.util.Random;
import java.util.concurrent.ExecutionException;


public class IgnCl2023 {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        IntervalTimer itm = new IntervalTimer();

        ClientConfiguration cfg = new ClientConfiguration().setAddresses("192.168.111.3:10800");
        cfg.setPartitionAwarenessEnabled(true);

        try (IgniteClient client = Ignition.startClient(cfg)) {
            ClientCache<Integer, String> cache = client.getOrCreateCache("Cache_1");
            if (cache.size(CachePeekMode.PRIMARY) == 0) {
                System.out.println("The cache \"Cache_1\" is empty!");
            } else {
                System.out.println("The cache \"Cache_1\" already contains data. All data will be dropped and reinserted");
                cache.clear();
            }

            final int iteration_num = 10; //10;
            final int num_keys = 10_000;
            long iteration_period = 0;
            for (int iteration = 0; iteration < iteration_num; iteration++) {
                // add 10000 keys in cache and get time for this operation
                //System.out.println("Iteration " + iteration);
                itm.start();
                for (int i = 0; i < num_keys; i++) {
                    //cache.put(i, "Value_" + i); // This method is very slowly! About 5 - 6 sec!
                    cache.putAsync(i, "Iteration: " + iteration + ", Value_" + i);
                }
                //iteration_period += itm.stopWithMessage();
                iteration_period += itm.stop();
//                try {
//                    Thread.sleep(2000L);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
            }
            System.out.println("Average time for " + iteration_num + " iterations is: " + (iteration_period / iteration_num) + " ms.");

            System.out.println("Get 5 values by random keys");
            Random rnd = new Random(System.currentTimeMillis());
            int rnd_num = 0;
            for (int i = 0; i < 5; i++) {
                rnd_num = rnd.nextInt(num_keys);
                System.out.println("Key: " + rnd_num + "    Value: " + cache.getAsync(rnd_num).get());
            }
        }
    }
}