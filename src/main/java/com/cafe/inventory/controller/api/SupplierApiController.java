package com.cafe.inventory.controller.api;

import com.cafe.inventory.entity.Supplier;
import com.cafe.inventory.service.SupplierService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Suppliers")
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierApiController {

    private final SupplierService supplierService;

    @GetMapping
    public List<Supplier> list() {
        return supplierService.findAll();
    }
}
