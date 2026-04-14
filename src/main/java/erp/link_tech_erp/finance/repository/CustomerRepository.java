package erp.link_tech_erp.finance.repository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import erp.link_tech_erp.finance.model.Customer;

public class CustomerRepository {
    private final SupabaseRestClient restClient;
    private final ObjectMapper objectMapper;

    public CustomerRepository(SupabaseRestConfig config) {
        this.restClient = new SupabaseRestClient(config);
        this.objectMapper = new ObjectMapper();
    }

    public Customer create(Customer customer) {
        if (customer.getId() == null || customer.getId().isBlank()) {
            customer.setId(UUID.randomUUID().toString());
        }
        String body = "{" +
                "\"id\":\"" + esc(customer.getId()) + "\"," +
                "\"code\":\"" + esc(orEmpty(customer.getCode())) + "\"," +
                "\"name\":\"" + esc(orEmpty(customer.getName())) + "\"," +
                "\"email\":\"" + esc(orEmpty(customer.getEmail())) + "\"," +
                "\"phone\":\"" + esc(orEmpty(customer.getPhone())) + "\"," +
                "\"address\":\"" + esc(orEmpty(customer.getAddress())) + "\"" +
                "}";

        String response = restClient.post("/customers", body, true);
        return parseFirstCustomer(response, customer);
    }

    public List<Customer> findAll() {
        String select = URLEncoder.encode("id,code,name,email,phone,address", StandardCharsets.UTF_8);
        String response = restClient.get("/customers?select=" + select + "&order=name.asc");
        return parseCustomers(response);
    }

    private List<Customer> parseCustomers(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            List<Customer> result = new ArrayList<>();
            if (root.isArray()) {
                for (JsonNode node : root) {
                    result.add(mapCustomer(node));
                }
            }
            return result;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to parse customers response.", exception);
        }
    }

    private Customer parseFirstCustomer(String response, Customer fallback) {
        List<Customer> parsed = parseCustomers(response);
        return parsed.isEmpty() ? fallback : parsed.get(0);
    }

    private Customer mapCustomer(JsonNode node) {
        Customer customer = new Customer();
        customer.setId(text(node, "id"));
        customer.setCode(text(node, "code"));
        customer.setName(text(node, "name"));
        customer.setEmail(text(node, "email"));
        customer.setPhone(text(node, "phone"));
        customer.setAddress(text(node, "address"));
        return customer;
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
