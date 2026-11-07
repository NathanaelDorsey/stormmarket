package com.nathanaeldorsey.stormmarket.product;

import java.util.Locale;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * HTTP entry point for bulk product import and export.
 *
 * POST /v1/products/bulk-import accepts a multipart CSV or JSON file and imports
 * its products. GET /v1/products/bulk-export returns all products as a downloadable
 * CSV or JSON file (selected via the optional ?format=csv|json query parameter).
 * All endpoints require the ADMIN role.
 */
@RestController
@RequestMapping("/v1/products")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class BulkImportController {

    private final ProductBulkService productBulkService;

    @PostMapping("/bulk-import")
    public BulkImportResult bulkImport(@RequestParam("file") MultipartFile file) {
        String extension = extensionOf(file.getOriginalFilename());
        return switch (extension.toLowerCase(Locale.ROOT)) {
            case "csv" -> productBulkService.importFromCsv(file);
            case "json" -> productBulkService.importFromJson(file);
            default -> throw new IllegalArgumentException(
                    "Unsupported file type '" + extension + "'. Use .csv or .json");
        };
    }

    @GetMapping("/bulk-export")
    public ResponseEntity<ByteArrayResource> bulkExport(@RequestParam(required = false) String format) {
        String fmt = (format == null ? "csv" : format).toLowerCase(Locale.ROOT);
        byte[] content;
        String fileName;
        MediaType mediaType;

        switch (fmt) {
            case "json":
                content = productBulkService.exportToJson();
                fileName = "products-export.json";
                mediaType = MediaType.APPLICATION_JSON;
                break;
            case "csv":
            default:
                content = productBulkService.exportToCsv();
                fileName = "products-export.csv";
                mediaType = MediaType.parseMediaType("text/csv");
                break;
        }

        ByteArrayResource resource = new ByteArrayResource(content);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(content.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    private static String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1);
    }
}
