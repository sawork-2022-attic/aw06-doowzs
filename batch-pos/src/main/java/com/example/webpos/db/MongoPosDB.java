package com.example.webpos.db;

import com.example.webpos.model.Product;
import com.example.webpos.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Repository
public class MongoPosDB implements PosDB {

    private final ProductRepository repository;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MongoPosDB(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Product> getProducts() {
        // 默认取ID前100个
        return repository.findAll(PageRequest.of(1, 100)).getContent();
    }

    @Override
    public Product getProduct(String productId) {
        return repository.findById(productId).orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
    }

    @Override
    public void addProduct(Product product) {
        repository.save(product);
    }

}
