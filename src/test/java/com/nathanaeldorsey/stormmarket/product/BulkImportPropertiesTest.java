package com.nathanaeldorsey.stormmarket.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import jakarta.validation.Validator;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Combinators;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

/**
 * Property-based tests for bulk product import using jqwik.
 *
 * Verifies that every randomly generated, well-formed CSV row is imported
 * successfully and that the bulk import result stays internally consistent.
 */
class BulkImportPropertiesTest {

    private final ProductService productService = mock(ProductService.class);
    private final Validator validator = mock(Validator.class);
    private final ProductBulkService bulkService = new ProductBulkService(productService, new ObjectMapper(), validator);

    @Provide
    Arbitrary<List<String>> validRows() {
        return validRow().list().ofMinSize(1).ofMaxSize(10);
    }

    private Arbitrary<String> validRow() {
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(40),
                Arbitraries.integers().between(1, 99999)
                    .map(i -> String.format(Locale.ROOT, "%d.%02d", i / 100, i % 100)),
                Arbitraries.integers().between(0, 10000))
            .as((name, price, stock) -> name + ",," + price + "," + stock + ",,,");
    }

    @Property(tries = 100)
    void importFromCsvSucceedsForEveryValidGeneratedRow(@ForAll("validRows") List<String> rows) {
        StringBuilder csv = new StringBuilder("name,description,price,stockQuantity,imageUrl,categoryIds,active\n");
        for (String row : rows) {
            csv.append(row).append("\n");
        }
        MultipartFile file = new MockMultipartFile("file", "products.csv", "text/csv",
                csv.toString().getBytes());
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(new ProductResponse());
        when(validator.validate(any())).thenReturn(Set.of());

        BulkImportResult result = bulkService.importFromCsv(file);

        assertThat(result.failed()).isZero();
        assertThat(result.succeeded()).isEqualTo(result.totalRows());
        assertThat(result.totalRows()).isEqualTo(rows.size());
        assertThat(result.errors()).isEmpty();
    }
}
