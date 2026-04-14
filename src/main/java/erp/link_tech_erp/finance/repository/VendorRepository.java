package erp.link_tech_erp.finance.repository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import erp.link_tech_erp.finance.model.Vendor;

public class VendorRepository {
    private final SupabaseRestClient restClient;
    private final ObjectMapper objectMapper;

    public VendorRepository(SupabaseRestConfig config) {
        this.restClient = new SupabaseRestClient(config);
        this.objectMapper = new ObjectMapper();
    }

    public Vendor create(Vendor vendor) {
        if (vendor.getId() == null || vendor.getId().isBlank()) {
            vendor.setId(UUID.randomUUID().toString());
        }
        String body = "{" +
                "\"id\":\"" + esc(vendor.getId()) + "\"," +
                "\"code\":\"" + esc(orEmpty(vendor.getCode())) + "\"," +
                "\"name\":\"" + esc(orEmpty(vendor.getName())) + "\"," +
                "\"email\":\"" + esc(orEmpty(vendor.getEmail())) + "\"," +
                "\"phone\":\"" + esc(orEmpty(vendor.getPhone())) + "\"," +
                "\"address\":\"" + esc(orEmpty(vendor.getAddress())) + "\"" +
                "}";

        String response = restClient.post("/vendors", body, true);
        return parseFirstVendor(response, vendor);
    }

    public List<Vendor> findAll() {
        String select = URLEncoder.encode("id,code,name,email,phone,address", StandardCharsets.UTF_8);
        String response = restClient.get("/vendors?select=" + select + "&order=name.asc");
        return parseVendors(response);
    }

    private List<Vendor> parseVendors(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            List<Vendor> result = new ArrayList<>();
            if (root.isArray()) {
                for (JsonNode node : root) {
                    result.add(mapVendor(node));
                }
            }
            return result;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to parse vendors response.", exception);
        }
    }

    private Vendor parseFirstVendor(String response, Vendor fallback) {
        List<Vendor> parsed = parseVendors(response);
        return parsed.isEmpty() ? fallback : parsed.get(0);
    }

    private Vendor mapVendor(JsonNode node) {
        Vendor vendor = new Vendor();
        vendor.setId(text(node, "id"));
        vendor.setCode(text(node, "code"));
        vendor.setName(text(node, "name"));
        vendor.setEmail(text(node, "email"));
        vendor.setPhone(text(node, "phone"));
        vendor.setAddress(text(node, "address"));
        return vendor;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private String esc(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
