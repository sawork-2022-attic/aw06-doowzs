package com.example.webpos.config;

import com.example.webpos.db.PosDB;
import com.example.webpos.job.JsonFilesReader;
import com.example.webpos.job.ProductProcessor;
import com.example.webpos.job.ProductWriter;
import com.example.webpos.model.Product;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.util.List;

@Configuration
@EnableBatchProcessing
@Import(DataSourceConfig.class)
public class JobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final PosDB posDB;

    public JobConfig(JobBuilderFactory jobBuilderFactory,
                     StepBuilderFactory stepBuilderFactory,
                     PosDB posDB) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.posDB = posDB;
    }

    @Bean
    public ItemReader<JsonNode> itemReader() {
        return new JsonFilesReader(List.of(
                "/data/meta_Books.json",
                "/data/meta_Clothing_Shoes_and_Jewelry.json"
        ));
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
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        return executor;
    }

    @Bean
    public Step processProductsStep(ItemReader<JsonNode> reader,
                                    ItemProcessor<JsonNode, Product> processor,
                                    ItemWriter<Product> writer) {
        return stepBuilderFactory.get("processProducts")
                .<JsonNode, Product>chunk(20)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Job importProductsJob() {
        return jobBuilderFactory.get("importProducts")
                .start(processProductsStep(itemReader(), itemProcessor(), itemWriter()))
                .build();
    }

}
