package com.agile.image;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
public class ScheduledFetcherConfig implements SchedulingConfigurer {

    private final int fetchRate;
    private final int fetchDelay;
    private final String apiKey;

    @Autowired
    public ScheduledFetcherConfig(
            @Value("${apikey}") String apiKey,
            @Value("${fetch.rate}") int fetchRate,
            @Value("${fetch.delay}") int fetchDelay) {
        this.fetchRate = fetchRate;
        this.fetchDelay = fetchDelay;
        this.apiKey = apiKey;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(scheduler());
        taskRegistrar.addFixedRateTask(new IntervalTask(new Runnable() {
            @Override
            public void run() {
                fetcher().fetchPeriodically();
            }
        }, fetchRate, fetchDelay));
    }

    @Bean(destroyMethod="shutdown")
    public Executor scheduler() {
        return Executors.newScheduledThreadPool(3);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().build();
    }

    @Bean
    public ImageFetcher fetcher() {
        return new ImageFetcher(apiKey, restTemplate(), imageQueue());
    }

    @Bean
    public SubscribableChannel imageQueue() {
        return new DirectChannel();
    }
}
