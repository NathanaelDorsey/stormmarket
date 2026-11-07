package com.nathanaeldorsey.stormmarket.product;

import com.nathanaeldorsey.stormmarket.common.exception.InvalidFileException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for bulk product import and export operations.
 *
 * Supports importing products from CSV or JSON files and exporting all products
 * to CSV or JSON. CSV files must include a header row with columns:
 * name, description, price, stockQuantity, imageUrl.
 *
 * Each imported row is validated and persisted independently, so a single bad
 * row does not abort the entire batch. Row-level failures are reported in the
 * result without throwing. File type and size are validated up front.
 */
@Service
@RequiredArgsConstructor
public class ProductBulkService {

    private static final long MAX_FILE_SIZE_BYTES = 2L * 1024 * 1024; // 2 MB

    private final ProductService productService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    /* IMPORT */

    /**
     * Imports products from a CSV multipart file.
     *
     * @param file the CSV file to import
     * @return a summary of imported rows and any row-level errors
     * @throws InvalidFileException if the file is empty, too large, not a .csv file,
     *         missing required columns, or has an invalid header
     */
    public BulkImportResult importFromCsv(MultipartFile file) {
        validateFile(file, "csv");
        List<BulkImportError> errors = new ArrayList<>();
        int totalRows = 0;
        int succeeded = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setIgnoreSurroundingSpaces(true)
                     .build()
                     .parse(reader)) {

            // Read header from file
            if (!parser.iterator().hasNext()) {
                throw new InvalidFileException("CSV file is empty (no header row)");
            }
            CSVRecord headerRecord = parser.iterator().next();
            List<String> headers = headerRecord.toList();

            // Validate required columns
            Set<String> requiredColumns = Set.of("name", "description", "price", "stockQuantity", "imageUrl");
            Set<String> optionalColumns = Set.of("categoryIds", "active");
            Set<String> allAllowedColumns = new HashSet<>(requiredColumns);
            allAllowedColumns.addAll(optionalColumns);

            for (String col : requiredColumns) {
                if (!headers.contains(col)) {
                    throw new InvalidFileException("Missing required column: " + col);
                }
            }
            // Warn about unknown columns
            for (String col : headers) {
                if (!allAllowedColumns.contains(col)) {
                    errors.add(new BulkImportError(1, "Unknown column in header: " + col + " (will be ignored)"));
                }
            }

            int rowNumber = 1;
            Iterator<CSVRecord> iterator = parser.iterator();
            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();
                totalRows++;
                rowNumber++;
                try {
                    ProductRequest request = toProductRequest(record, headers);
                    validateProductRequest(request);
                    productService.createProduct(request);
                    succeeded++;
                } catch (Exception ex) {
                    errors.add(new BulkImportError(rowNumber, ex.getMessage()));
                }
            }
        } catch (IOException ex) {
            throw new InvalidFileException("Failed to parse CSV file: " + ex.getMessage());
        } catch (Exception ex) {
            throw new InvalidFileException("Failed to parse CSV file: " + ex.getMessage());
        }

        return BulkImportResult.of(totalRows, succeeded, totalRows - succeeded, errors);
    }

    /**
     * Imports products from a JSON multipart file containing an array of product objects.
     *
     * @param file the JSON file to import
     * @return a summary of imported rows and any row-level errors
     * @throws InvalidFileException if the file is empty, too large, or not a .json file
     */
    public BulkImportResult importFromJson(MultipartFile file) {
        validateFile(file, "json");
        List<BulkImportError> errors = new ArrayList<>();
        int totalRows = 0;
        int succeeded = 0;

        try {
            // Read as JsonNode array first to allow per-row error handling
            tools.jackson.databind.JsonNode jsonArray = objectMapper.readTree(file.getInputStream());
            if (!jsonArray.isArray()) {
                throw new InvalidFileException("JSON file must contain an array of product objects");
            }
            int rowNumber = 1;
            for (tools.jackson.databind.JsonNode node : jsonArray) {
                totalRows++;
                rowNumber++;
                try {
                    ProductRequest request = objectMapper.treeToValue(node, ProductRequest.class);
                    validateProductRequest(request);
                    productService.createProduct(request);
                    succeeded++;
                } catch (Exception ex) {
                    errors.add(new BulkImportError(rowNumber, ex.getMessage()));
                }
            }
        } catch (Exception ex) {
            throw new InvalidFileException("Failed to parse JSON file: " + ex.getMessage());
        }

        return BulkImportResult.of(totalRows, succeeded, totalRows - succeeded, errors);
    }

    /* EXPORT */

    /**
     * Exports all products (including inactive) to CSV bytes.
     *
     * @return the CSV file content as a UTF-8 byte array
     * @throws InvalidFileException if the export cannot be generated
     */
    public byte[] exportToCsv() {
        List<ProductResponse> products = productService.getAllProductsForAdmin();
        try (StringWriter writer = new StringWriter();
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader("name", "description", "price", "stockQuantity", "imageUrl", "categoryIds", "active")
                     .build())) {
            for (ProductResponse product : products) {
                printer.printRecord(
                        sanitizeForCsv(product.getName()),
                        sanitizeForCsv(product.getDescription()),
                        product.getPrice(),
                        product.getStockQuantity(),
                        sanitizeForCsv(product.getImageUrl()),
                        formatCategoryIds(product.getCategoryIds()),
                        product.isActive());
            }
            printer.flush();
            return writer.toString().getBytes(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new InvalidFileException("Failed to generate CSV export: " + ex.getMessage());
        }
    }

    /**
     * Formats a set of category IDs as comma-separated string for CSV export.
     * Empty set produces empty string (empty cell in CSV).
     */
    private String formatCategoryIds(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return "";
        }
        return categoryIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    /**
     * Exports all products (including inactive) to pretty-printed JSON bytes.
     *
     * @return the JSON file content as a UTF-8 byte array
     * @throws InvalidFileException if the export cannot be generated
     */
    public byte[] exportToJson() {
        List<ProductResponse> products = productService.getAllProductsForAdmin();
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(products);
        } catch (Exception ex) {
            throw new InvalidFileException("Failed to generate JSON export: " + ex.getMessage());
        }
    }

    /* helpers */

    private ProductRequest toProductRequest(CSVRecord record, List<String> headers) {
        ProductRequest request = new ProductRequest();
        // Map column values by header name
        Map<String, String> columnMap = new HashMap<>();
        for (int i = 0; i < headers.size() && i < record.size(); i++) {
            columnMap.put(headers.get(i), record.get(i));
        }
        
        request.setName(columnMap.get("name"));
        request.setDescription(columnMap.get("description"));
        request.setPrice(parseBigDecimal(columnMap.get("price"), "price"));
        request.setStockQuantity(parseInteger(columnMap.get("stockQuantity"), "stockQuantity"));
        request.setImageUrl(columnMap.get("imageUrl"));
        // Optional categoryIds: comma-separated list of Long values
        String categoryIdsStr = columnMap.get("categoryIds");
        if (categoryIdsStr != null && !categoryIdsStr.isBlank()) {
            Set<Long> categoryIds = java.util.Arrays.stream(categoryIdsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(java.util.stream.Collectors.toSet());
            request.setCategoryIds(categoryIds);
        }
        // Optional active flag - only accept "true" or "false" (case insensitive)
        String activeStr = columnMap.get("active");
        if (activeStr != null && !activeStr.isBlank()) {
            String normalized = activeStr.trim().toLowerCase(Locale.ROOT);
            if ("true".equals(normalized)) {
                request.setActive(true);
            } else if ("false".equals(normalized)) {
                request.setActive(false);
            } else {
                throw new IllegalArgumentException("Invalid active value: '" + activeStr + "' (must be 'true' or 'false')");
            }
        }
        return request;
    }

    private BigDecimal parseBigDecimal(String value, String field) {
        try {
            return new BigDecimal(value == null ? "" : value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + field + ": '" + value + "'");
        }
    }

    private Integer parseInteger(String value, String field) {
        try {
            return value == null || value.isBlank() ? null : Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + field + ": '" + value + "'");
        }
    }

    /**
     * Sanitizes a value for CSV export to prevent formula injection.
     * Prefixes values starting with =, +, -, @ with a single quote.
     */
    private String sanitizeForCsv(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        char firstChar = value.charAt(0);
        if (firstChar == '=' || firstChar == '+' || firstChar == '-' || firstChar == '@') {
            return "'" + value;
        }
        return value;
    }

    private void validateProductRequest(ProductRequest request) {
        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce((first, second) -> first + "; " + second)
                    .orElse("Invalid product");
            throw new IllegalArgumentException(message);
        }
    }

    private void validateFile(MultipartFile file, String expectedExtension) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Uploaded file must not be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidFileException("File exceeds maximum size of 2 MB");
        }
        String filename = file.getOriginalFilename();
        boolean validExtension = filename != null
                && filename.toLowerCase(Locale.ROOT).endsWith("." + expectedExtension);
        if (!validExtension) {
            throw new InvalidFileException("Invalid file type. Only ." + expectedExtension + " files are accepted");
        }
    }
}
