package com.example.webpos.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.stream.Collectors;

public class JsonFilesReader implements StepExecutionListener, ItemReader<JsonNode> {

    private BufferedReader reader;

    private final List<String> files;
    private final ObjectMapper objectMapper;

    public JsonFilesReader(List<String> files) {
        this.files = files.stream().map((file) -> {
            if (file.matches("^file:(.*)")) {
                return file.substring(file.indexOf(":") + 1);
            } else {
                return file;
            }
        }).collect(Collectors.toList());
        this.objectMapper = new ObjectMapper();
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
        String line;
        do {
            if (reader == null) {
                if (files.isEmpty()) {
                    return null;
                } else {
                    File file = new File(files.remove(0));
                    reader = new BufferedReader(new FileReader(file));
                }
            }
            line = reader.readLine();
            if (line == null) {
                reader = null;
            }
        } while (line == null);
        return objectMapper.readTree(reader.readLine());
    }
}
