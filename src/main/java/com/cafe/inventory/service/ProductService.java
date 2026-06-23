package com.cafe.inventory.service;

import com.cafe.inventory.dto.ProductDto;
import com.cafe.inventory.entity.Product;
import com.cafe.inventory.exception.BusinessException;
import com.cafe.inventory.exception.ResourceNotFoundException;
import com.cafe.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductDto> findAll() {
        return productRepository.findAll().stream().map(ProductDto::from).toList();
    }

    public List<Product> findAllActive() {
        return productRepository.findByActiveFlagTrue();
    }

    public ProductDto findById(Long id) {
        return ProductDto.from(get(id));
    }

    @Transactional
    public ProductDto create(ProductDto dto, String user) {
        if (productRepository.existsByProductCode(dto.productCode())) {
            throw new BusinessException("Product code already exists: " + dto.productCode());
        }
        Product p = new Product();
        apply(p, dto);
        p.setCreatedBy(user);
        return ProductDto.from(productRepository.save(p));
    }

    @Transactional
    public ProductDto update(Long id, ProductDto dto, String user) {
        Product p = get(id);
        apply(p, dto);
        p.setUpdatedBy(user);
        return ProductDto.from(productRepository.save(p));
    }

    @Transactional
    public void delete(Long id) {
        Product p = get(id);
        p.setActiveFlag(false);
        productRepository.save(p);
    }

    private void apply(Product p, ProductDto dto) {
        p.setProductCode(dto.productCode());
        p.setProductName(dto.productName());
        p.setSalePrice(dto.salePrice());
        if (dto.activeFlag() != null) {
            p.setActiveFlag(dto.activeFlag());
        }
    }

    private Product get(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }
}
