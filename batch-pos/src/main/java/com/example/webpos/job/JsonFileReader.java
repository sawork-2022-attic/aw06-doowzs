package com.example.webpos.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class JsonFileReader implements StepExecutionListener, ItemReader<JsonNode> {

    private BufferedReader bufferedReader;

    private final String file;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public JsonFileReader(String file) {
        if (file.startsWith("file:/")) {
            this.file = file.substring(6);
        } else {
            this.file = file;
        }
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }

    @Override
    public JsonNode read() throws Exception {
        if (bufferedReader == null) {
            try {
                File _file = new File(file);
                FileReader fileReader = new FileReader(_file);
                bufferedReader = new BufferedReader(fileReader);
            } catch (Exception e) {
                logger.error("Failed to read file", e);
                return null;
            }
        }

        String line = bufferedReader.readLine();
        if (line == null) {
            return null;
        }
        return objectMapper.readTree(line);
    }
}
