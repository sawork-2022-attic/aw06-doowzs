package com.example.webpos.job;

import com.example.webpos.biz.PosService;
import com.example.webpos.db.PosDB;
import com.example.webpos.model.Product;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class ProductWriter implements ItemWriter<Product>, StepExecutionListener {

    private final PosDB posDB;

    public ProductWriter(PosDB posDB) {
        this.posDB = posDB;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }

    @Override
    public void write(List<? extends Product> list) throws Exception {
        for (Product product : list) {
            posDB.addProduct(product);
        }
    }

}
