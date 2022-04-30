package com.example.webpos.job;

import com.example.webpos.model.Product;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;

public class ProductProcessor implements ItemProcessor<JsonNode, Product>, StepExecutionListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void beforeStep(StepExecution stepExecution) {
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }

    @Override
    public Product process(JsonNode jsonNode) throws Exception {
        if (!jsonNode.has("id") || jsonNode.get("id").isEmpty() ||
                !jsonNode.has("title") || jsonNode.get("title").isEmpty() ||
                !jsonNode.has("price") || jsonNode.get("price").isEmpty() ||
                !jsonNode.has("imageURL") || jsonNode.get("imageURL").isEmpty()) {
            return null;
        }
        String id = jsonNode.get("asin").textValue();
        String name = jsonNode.get("title").textValue();
        double price = Double.parseDouble(jsonNode.get("price").textValue().substring(1));
        String image = jsonNode.get("imageURL").get(0).textValue();

        logger.info(String.format("Process product id=%s", id));
        return new Product(id, name, price, image);
    }
}
