package com.nathanaeldorsey.stormmarket.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nathanaeldorsey.stormmarket.common.exception.InvalidFileException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

@ExtendWith(MockitoExtension.class)
/**
 * Tests for ProductBulkService bulk import functionality.
 * Covers CSV/JSON parsing, validation, and import results.
 */

class ProductBulkServiceTest {

    @Mock
    private ProductService productService;

    @Mock
    private Validator validator;

    private ObjectMapper objectMapper = new ObjectMapper();
    private ProductBulkService bulkService;

    @BeforeEach
    void setUp() {
        bulkService = new ProductBulkService(productService, objectMapper, validator);
    }

    @Test
    void importFromCsvImportsAllValidRows() {
        String csv = "name,description,price,stockQuantity,imageUrl,categoryIds,active\n"
                + "Widget, A widget, 9.99, 10, https://example.com/w.jpg,,\n"
                + "Gadget,, 19.99, 5, ,,\n";
        MultipartFile file = new MockMultipartFile("file", "products.csv", "text/csv", csv.getBytes());
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(new ProductResponse());
        when(validator.validate(any())).thenReturn(Set.of());

        BulkImportResult result = bulkService.importFromCsv(file);

        assertEquals(2, result.totalRows());
        assertEquals(2, result.succeeded());
        assertEquals(0, result.failed());
        assertTrue(result.errors().isEmpty());
        verify(productService, times(2)).createProduct(any(ProductRequest.class));
    }

    @Test
    void importFromCsvReportsRowErrorsWithoutAbortingBatch() {
        String csv = "name,description,price,stockQuantity,imageUrl,categoryIds,active\n"
                + "Widget,,not-a-price,10,,,\n"
                + "Gadget,, 19.99, 5,,,\n"
                + "Bad Product,desc,notaprice,5,,,\n";
        MultipartFile file = new MockMultipartFile("file", "products.csv", "text/csv", csv.getBytes());
        when(validator.validate(any())).thenReturn(Set.of());

        BulkImportResult result = bulkService.importFromCsv(file);

        assertEquals(3, result.totalRows());
        assertEquals(1, result.succeeded());
        assertEquals(2, result.failed());
        assertEquals(2, result.errors().size());
        assertEquals(2, result.errors().getFirst().rowNumber());
        assertEquals(4, result.errors().get(1).rowNumber());
    }

    @Test
    void importFromCsvRejectsNonCsvFile() {
        MultipartFile file = new MockMultipartFile("file", "products.txt", "text/plain", "name,price\n".getBytes());

        InvalidFileException ex = assertThrows(InvalidFileException.class,
                () -> bulkService.importFromCsv(file));
        assertTrue(ex.getMessage().contains(".csv"));
    }

    @Test
    void importFromCsvRejectsEmptyFile() {
        MultipartFile file = new MockMultipartFile("file", "products.csv", "text/csv", new byte[0]);

        InvalidFileException ex = assertThrows(InvalidFileException.class,
                () -> bulkService.importFromCsv(file));
        assertTrue(ex.getMessage().contains("not be empty"));
    }

    @Test
    void importFromCsvRejectsOversizedFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(2L * 1024 * 1024 + 1);

        InvalidFileException ex = assertThrows(InvalidFileException.class,
                () -> bulkService.importFromCsv(file));
        assertTrue(ex.getMessage().contains("2 MB"));
    }

    @Test
    void importFromJsonImportsAllValidRows() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Widget");
        request.setPrice(new BigDecimal("9.99"));
        request.setStockQuantity(10);

        String json = "[{\"name\":\"Widget\",\"price\":9.99,\"stockQuantity\":10}]";
        MultipartFile file = new MockMultipartFile("file", "products.json", "application/json", json.getBytes());
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(new ProductResponse());
        when(validator.validate(any())).thenReturn(Set.of());

        BulkImportResult result = bulkService.importFromJson(file);

        assertEquals(1, result.totalRows());
        assertEquals(1, result.succeeded());
        assertEquals(0, result.failed());
        verify(productService).createProduct(any(ProductRequest.class));
    }

    @Test
    void importFromJsonReportsRowErrors() throws Exception {
        ConstraintViolation<ProductRequest> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("name must not be blank");

        String json = "[{\"name\":\"Widget\",\"price\":9.99,\"stockQuantity\":10},{}]";
        MultipartFile file = new MockMultipartFile("file", "products.json", "application/json", json.getBytes());
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(new ProductResponse());
        // Valid request has name, price, stockQuantity
        when(validator.validate(argThat((ProductRequest r) -> r != null && r.getName() != null && r.getPrice() != null)))
                .thenReturn(Set.of());
        // Invalid request (empty object) fails validation
        when(validator.validate(argThat((ProductRequest r) -> r == null || r.getName() == null || r.getPrice() == null)))
                .thenReturn(Set.of(violation));

        BulkImportResult result = bulkService.importFromJson(file);

        assertEquals(2, result.totalRows());
        assertEquals(1, result.succeeded());
        assertEquals(1, result.failed());
        assertEquals(1, result.errors().size());
        assertEquals(3, result.errors().getFirst().rowNumber());
        assertTrue(result.errors().getFirst().message().contains("name must not be blank"));
    }

    @Test
    void importFromJsonRejectsNonJsonFile() {
        MultipartFile file = new MockMultipartFile("file", "products.csv", "text/csv", "{}".getBytes());

        InvalidFileException ex = assertThrows(InvalidFileException.class,
                () -> bulkService.importFromJson(file));
        assertTrue(ex.getMessage().contains(".json"));
    }

    @Test
    void exportToCsvReturnsCsvBytes() {
        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("Widget");
        product.setPrice(new BigDecimal("9.99"));
        product.setStockQuantity(10);
        product.setActive(true);

        when(productService.getAllProductsForAdmin()).thenReturn(List.of(product));

        byte[] csv = bulkService.exportToCsv();
        String content = new String(csv, java.nio.charset.StandardCharsets.UTF_8);

        assertTrue(content.contains("name,description,price,stockQuantity,imageUrl,categoryIds,active"));
        assertTrue(content.contains("Widget"));
        assertTrue(content.contains("9.99"));
    }

    @Test
    void exportToJsonReturnsJsonBytes() {
        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("Widget");
        product.setPrice(new BigDecimal("9.99"));
        product.setStockQuantity(10);

        when(productService.getAllProductsForAdmin()).thenReturn(List.of(product));

        byte[] json = bulkService.exportToJson();
        String content = new String(json, java.nio.charset.StandardCharsets.UTF_8);

        assertTrue(content.contains("Widget"));
        assertTrue(content.contains("9.99"));
    }
}
