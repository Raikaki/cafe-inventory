package com.cafe.inventory.service;

import com.cafe.inventory.dto.SalesDtos.SaleLine;
import com.cafe.inventory.dto.SalesDtos.SalesRequest;
import com.cafe.inventory.dto.SalesDtos.SalesResult;
import com.cafe.inventory.entity.*;
import com.cafe.inventory.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SalesServiceTest {

    @Mock ProductRepository productRepository;
    @Mock RecipeRepository recipeRepository;
    @Mock MaterialRepository materialRepository;
    @Mock SaleRepository saleRepository;
    @Mock InventoryService inventoryService;
    @Mock VoucherService voucherService;
    @Mock PeriodLockService periodLockService;
    @InjectMocks SalesService salesService;

    @BeforeEach
    void setUp() {
        Product product = new Product();
        product.setId(2L);
        product.setProductCode("CF002");
        product.setProductName("Coffee Milk");
        when(productRepository.findByProductCode("CF002")).thenReturn(Optional.of(product));

        Recipe recipe = new Recipe();
        recipe.setProductId(2L);
        recipe.setActiveFlag(true);
        RecipeDetail d = new RecipeDetail();
        d.setMaterialId(1L);
        d.setStandardQty(new BigDecimal("20")); // 20g per cup
        recipe.addDetail(d);
        when(recipeRepository.findFirstByProductIdAndActiveFlagTrue(2L)).thenReturn(Optional.of(recipe));

        Material material = new Material();
        material.setId(1L);
        material.setMaterialCode("MAT001");
        material.setMaterialName("Coffee Powder");
        material.setUnit("g");
        material.setCurrentQty(new BigDecimal("1000"));
        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));

        when(saleRepository.count()).thenReturn(0L);
        when(saleRepository.save(any(Sale.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void recordSales_explodesRecipeAndConsumesMaterials() {
        // sell 10 cups -> 20g * 10 = 200g coffee
        SalesRequest req = new SalesRequest(LocalDate.now(),
                List.of(new SaleLine("CF002", new BigDecimal("10"))));

        SalesResult result = salesService.recordSales(req, "user");

        assertThat(result.salesLines()).isEqualTo(1);
        assertThat(result.consumption()).hasSize(1);
        assertThat(result.consumption().get(0).consumedQty()).isEqualByComparingTo("200");
        assertThat(result.consumption().get(0).afterQty()).isEqualByComparingTo("800");

        verify(inventoryService).consume(eq(1L), any(BigDecimal.class), anyString(), eq("user"), anyString());
    }
}
