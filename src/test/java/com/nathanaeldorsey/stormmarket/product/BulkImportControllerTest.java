package com.nathanaeldorsey.stormmarket.product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

@ActiveProfiles("test")
@WebMvcTest(
        controllers = BulkImportController.class,
        excludeAutoConfiguration = {
            org.springframework.boot.security.oauth2.server.resource.autoconfigure.web
                    .OAuth2ResourceServerWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
/**
 * Tests for BulkImportController bulk product import/export endpoints.
 * Covers CSV and JSON import, export, and error handling.
 */

class BulkImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductBulkService productBulkService;

    @Test
    void bulkImportCsvReturnsImportResult() throws Exception {
        BulkImportResult result = BulkImportResult.of(2, 2, 0, List.of());
        when(productBulkService.importFromCsv(any(MultipartFile.class))).thenReturn(result);

        MockMultipartFile file = new MockMultipartFile("file", "products.csv", "text/csv",
                "name,price\nWidget,9.99\nGadget,19.99\n".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/v1/products/bulk-import").file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalRows").value(2))
                .andExpect(jsonPath("$.succeeded").value(2))
                .andExpect(jsonPath("$.failed").value(0))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void bulkImportJsonReturnsImportResult() throws Exception {
        BulkImportResult result = BulkImportResult.of(3, 2, 1, List.of());
        when(productBulkService.importFromJson(any(MultipartFile.class))).thenReturn(result);

        MockMultipartFile file = new MockMultipartFile("file", "products.json", "application/json",
                "[{\"name\":\"Widget\",\"price\":9.99}]".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/v1/products/bulk-import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(3))
                .andExpect(jsonPath("$.succeeded").value(2))
                .andExpect(jsonPath("$.failed").value(1));
    }

    @Test
    void bulkImportUnsupportedFileTypeReturnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "products.txt", "text/plain", "name,price\n".getBytes());

        mockMvc.perform(multipart("/v1/products/bulk-import").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bulkExportCsvReturnsCsvDownload() throws Exception {
        byte[] csv = "id,name\n1,Widget\n".getBytes(StandardCharsets.UTF_8);
        when(productBulkService.exportToCsv()).thenReturn(csv);

        mockMvc.perform(get("/v1/products/bulk-export"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"products-export.csv\""))
                .andExpect(content().bytes(csv));
    }

    @Test
    void bulkExportJsonReturnsJsonDownload() throws Exception {
        byte[] json = "[{\"id\":1,\"name\":\"Widget\"}]".getBytes(StandardCharsets.UTF_8);
        when(productBulkService.exportToJson()).thenReturn(json);

        mockMvc.perform(get("/v1/products/bulk-export").param("format", "json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"products-export.json\""))
                .andExpect(content().bytes(json));
    }
}
