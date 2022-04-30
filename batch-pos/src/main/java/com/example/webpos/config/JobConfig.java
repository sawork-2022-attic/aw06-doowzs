package com.example.webpos.config;

import com.example.webpos.db.PosDB;
import com.example.webpos.job.JsonFileReader;
import com.example.webpos.job.ProductProcessor;
import com.example.webpos.job.ProductWriter;
import com.example.webpos.model.Product;
import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableBatchProcessing
@Import(DataSourceConfig.class)
public class JobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final PosDB posDB;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public JobConfig(JobBuilderFactory jobBuilderFactory,
            StepBuilderFactory stepBuilderFactory,
            PosDB posDB) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.posDB = posDB;
    }

    @Bean
    @StepScope
    public JsonFileReader itemReader(@Value("#{stepExecutionContext['fileName']}") String file) {
        logger.info(String.format("Read file part %s", file));
        return new JsonFileReader(file);
    }

    @Bean
    public ItemProcessor<JsonNode, Product> itemProcessor() {
        return new ProductProcessor();
    }

    @Bean
    public ItemWriter<Product> itemWriter() {
        return new ProductWriter(posDB);
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(2000);
        return executor;
    }

    @Bean
    public Partitioner partitioner() throws Exception {
        MultiResourcePartitioner partitioner = new MultiResourcePartitioner();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("file:/data/x*");
        partitioner.setResources(resources);
        return partitioner;
    }

    @Bean
    public Step importProductsWorkerStep() {
        return stepBuilderFactory.get("processProducts")
                .<JsonNode, Product>chunk(500)
                .reader(itemReader(null))
                .processor(itemProcessor())
                .writer(itemWriter())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Step importProductsManagerStep() throws Exception {
        return stepBuilderFactory.get("importProductsManagerStep")
                .partitioner("importProductsWorkerStep", partitioner())
                .gridSize(10)
                .step(importProductsWorkerStep())
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public Job importProductsJob() throws Exception {
        return jobBuilderFactory.get("importProducts")
                .incrementer(new RunIdIncrementer())
                .start(importProductsManagerStep())
                .build();
    }

}
