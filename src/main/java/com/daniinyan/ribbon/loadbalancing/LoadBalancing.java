package com.daniinyan.ribbon.loadbalancing;

import com.google.common.collect.Lists;
import com.netflix.loadbalancer.*;
import com.netflix.loadbalancer.reactive.LoadBalancerCommand;
import com.netflix.loadbalancer.reactive.ServerOperation;
import rx.Observable;

import java.net.URL;
import java.util.List;

public class LoadBalancing {

    private final ILoadBalancer loadBalancer;

    public LoadBalancing(List<Server> serverList) {
        loadBalancer = LoadBalancerBuilder.newBuilder().buildFixedServerListLoadBalancer(serverList);
    }

    public String call(final String path) throws Exception {
        ServerOperation<String> submitObservableString = new ServerOperation<String>() {
            @Override
            public Observable<String> call(Server server) {
                URL url;
                try {
                    url = new URL("http://" + server.getHost() + ":" + server.getPort());
                    return Observable.just(url.toString());
                } catch (Exception e) {
                    return Observable.error(e);
                }
            }
        };

        return LoadBalancerCommand.<String>builder()
                .withLoadBalancer(loadBalancer)
                .build()
                .submit(submitObservableString)
                .toBlocking()
                .first();
    }

    public LoadBalancerStats getLoadBalancerStats() {
        return ((BaseLoadBalancer) loadBalancer).getLoadBalancerStats();
    }

    public static void main(String[] args) throws Exception {
        LoadBalancing urlLoadBalancer = new LoadBalancing(Lists.newArrayList(
                new Server("www.google.com", 80),
                new Server("www.linkedin.com", 80),
                new Server("www.yahoo.com", 80)));

        for (int i = 0; i < 6; i++) {
            System.out.println(urlLoadBalancer.call("/"));
        }
        System.out.println("=== STATS ===");
        System.out.println(urlLoadBalancer.getLoadBalancerStats());
    }
}