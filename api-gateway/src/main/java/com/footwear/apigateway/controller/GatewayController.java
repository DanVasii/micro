package com.footwear.apigateway.controller;

import com.footwear.apigateway.factory.ResponseFactory;
import com.footwear.apigateway.factory.ResponseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "*")
public class GatewayController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.services.user}")
    private String userServiceUrl;

    @Value("${app.services.product}")
    private String productServiceUrl;

    @Value("${app.services.inventory}")
    private String inventoryServiceUrl;

    // Health check fyrir gateway
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        ResponseFactory factory = ResponseFactory.getFactory(ResponseType.SUCCESS);
        return factory.createResponse("API Gateway is running",
                "{\"service\":\"api-gateway\",\"version\":\"1.0\",\"timestamp\":\"" + System.currentTimeMillis() + "\"}");
    }

    // ==========================================
    // AUTH ROUTES (login, register, validate)
    // ==========================================

    @PostMapping("/api/auth/login")
    public ResponseEntity<String> login(@RequestBody String body) {
        return forwardToUserService("/api/users/login", "POST", body, null);
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<String> register(@RequestBody String body) {
        return forwardToUserService("/api/users/register", "POST", body, null);
    }

    @PostMapping("/api/auth/validate-token")
    public ResponseEntity<String> validateToken(@RequestBody String body) {
        return forwardToUserService("/api/users/validate-token", "POST", body, null);
    }

    // ==========================================
    // USER ROUTES
    // ==========================================

    @GetMapping("/api/users/**")
    public ResponseEntity<String> getUsersGet(HttpServletRequest request,
                                              @RequestHeader(value = "Authorization", required = false) String token) {
        String path = extractPath(request, "/api/users");
        String queryString = request.getQueryString();
        if (queryString != null) {
            path += "?" + queryString;
        }
        return forwardToUserService("/api/users" + path, "GET", null, token);
    }

    @PostMapping("/api/users/**")
    public ResponseEntity<String> getUsersPost(HttpServletRequest request,
                                               @RequestBody String body,
                                               @RequestHeader(value = "Authorization", required = false) String token) {
        String path = extractPath(request, "/api/users");
        return forwardToUserService("/api/users" + path, "POST", body, token);
    }

    @PutMapping("/api/users/**")
    public ResponseEntity<String> getUsersPut(HttpServletRequest request,
                                              @RequestBody String body,
                                              @RequestHeader(value = "Authorization", required = false) String token) {
        String path = extractPath(request, "/api/users");
        return forwardToUserService("/api/users" + path, "PUT", body, token);
    }

    @DeleteMapping("/api/users/**")
    public ResponseEntity<String> getUsersDelete(HttpServletRequest request,
                                                 @RequestHeader(value = "Authorization", required = false) String token) {
        String path = extractPath(request, "/api/users");
        return forwardToUserService("/api/users" + path, "DELETE", null, token);
    }

    // ==========================================
    // PRODUCT ROUTES - COMPLETE
    // ==========================================

    @GetMapping("/api/products/**")
    public ResponseEntity<String> getProducts(HttpServletRequest request,
                                              @RequestHeader(value = "Authorization", required = false) String token) {
        String path = extractPath(request, "/api/products");
        String queryString = request.getQueryString();
        if (queryString != null) {
            path += "?" + queryString;
        }
        return forwardToProductService("/api/products" + path, "GET", null, token);
    }

    @PostMapping("/api/products/**")
    public ResponseEntity<String> postProducts(HttpServletRequest request,
                                               @RequestBody String body,
                                               @RequestHeader(value = "Authorization", required = false) String token) {
        String path = extractPath(request, "/api/products");
        return forwardToProductService("/api/products" + path, "POST", body, token);
    }

    @PutMapping("/api/products/**")
    public ResponseEntity<String> putProducts(HttpServletRequest request,
                                              @RequestBody String body,
                                              @RequestHeader(value = "Authorization", required = false) String token) {
        String path = extractPath(request, "/api/products");
        return forwardToProductService("/api/products" + path, "PUT", body, token);
    }

    @DeleteMapping("/api/products/**")
    public ResponseEntity<String> deleteProducts(HttpServletRequest request,
                                                 @RequestHeader(value = "Authorization", required = false) String token) {
        String path = extractPath(request, "/api/products");
        return forwardToProductService("/api/products" + path, "DELETE", null, token);
    }

    // ==========================================
    // INVENTORY ROUTES
    // ==========================================

    @GetMapping("/api/inventory/**")
    public ResponseEntity<String> getInventory(HttpServletRequest request,
                                               @RequestHeader(value = "Authorization", required = false) String token) {
        String path = extractPath(request, "/api/inventory");
        String queryString = request.getQueryString();
        if (queryString != null) {
            path += "?" + queryString;
        }
        return forwardToInventoryService("/api/inventory" + path, "GET", null, token);
    }

    @PostMapping("/api/inventory/**")
    public ResponseEntity<String> postInventory(HttpServletRequest request,
                                                @RequestBody String body,
                                                @RequestHeader(value = "Authorization", required = false) String token) {
        String path = extractPath(request, "/api/inventory");
        return forwardToInventoryService("/api/inventory" + path, "POST", body, token);
    }

    @PutMapping("/api/inventory/**")
    public ResponseEntity<String> putInventory(HttpServletRequest request,
                                               @RequestBody String body,
                                               @RequestHeader(value = "Authorization", required = false) String token) {
        String path = extractPath(request, "/api/inventory");
        return forwardToInventoryService("/api/inventory" + path, "PUT", body, token);
    }

    @DeleteMapping("/api/inventory/**")
    public ResponseEntity<String> deleteInventory(HttpServletRequest request,
                                                  @RequestHeader(value = "Authorization", required = false) String token) {
        String path = extractPath(request, "/api/inventory");
        return forwardToInventoryService("/api/inventory" + path, "DELETE", null, token);
    }

    // ==========================================
    // TEST ROUTES (pentru debugging) - Updated with Factory
    // ==========================================

    @GetMapping("/test/user")
    public ResponseEntity<String> testUserService() {
        try {
            String url = userServiceUrl + "/api/users/test";
            String response = restTemplate.getForObject(url, String.class);
            ResponseFactory factory = ResponseFactory.getFactory(ResponseType.SUCCESS);
            return factory.createResponse("User service test completed",
                    "{\"gateway\":\"OK\",\"user-service\":\"" + response + "\"}");
        } catch (Exception e) {
            ResponseFactory factory = ResponseFactory.getFactory(ResponseType.ERROR);
            return factory.createResponse("User service unavailable", "SERVICE_DOWN");
        }
    }

    @GetMapping("/test/user/health")
    public ResponseEntity<String> testUserServiceHealth() {
        try {
            String url = userServiceUrl + "/api/users/health";
            String response = restTemplate.getForObject(url, String.class);
            ResponseFactory factory = ResponseFactory.getFactory(ResponseType.SUCCESS);
            return factory.createResponse("User service health check completed", response);
        } catch (Exception e) {
            ResponseFactory factory = ResponseFactory.getFactory(ResponseType.ERROR);
            return factory.createResponse("User service health check failed", "HEALTH_CHECK_FAILED");
        }
    }

    @GetMapping("/test/product")
    public ResponseEntity<String> testProductService() {
        try {
            String url = productServiceUrl + "/api/products/test";
            String response = restTemplate.getForObject(url, String.class);
            ResponseFactory factory = ResponseFactory.getFactory(ResponseType.SUCCESS);
            return factory.createResponse("Product service test completed",
                    "{\"gateway\":\"OK\",\"product-service\":\"" + response + "\"}");
        } catch (Exception e) {
            ResponseFactory factory = ResponseFactory.getFactory(ResponseType.ERROR);
            return factory.createResponse("Product service unavailable", "SERVICE_DOWN");
        }
    }

    @GetMapping("/test/product/health")
    public ResponseEntity<String> testProductServiceHealth() {
        try {
            String url = productServiceUrl + "/api/products/health";
            String response = restTemplate.getForObject(url, String.class);
            ResponseFactory factory = ResponseFactory.getFactory(ResponseType.SUCCESS);
            return factory.createResponse("Product service health check completed", response);
        } catch (Exception e) {
            ResponseFactory factory = ResponseFactory.getFactory(ResponseType.ERROR);
            return factory.createResponse("Product service health check failed", "HEALTH_CHECK_FAILED");
        }
    }

    @GetMapping("/test/inventory")
    public ResponseEntity<String> testInventoryService() {
        try {
            String url = inventoryServiceUrl + "/api/inventory/test";
            String response = restTemplate.getForObject(url, String.class);
            ResponseFactory factory = ResponseFactory.getFactory(ResponseType.SUCCESS);
            return factory.createResponse("Inventory service test completed",
                    "{\"gateway\":\"OK\",\"inventory-service\":\"" + response + "\"}");
        } catch (Exception e) {
            ResponseFactory factory = ResponseFactory.getFactory(ResponseType.ERROR);
            return factory.createResponse("Inventory service unavailable", "SERVICE_DOWN");
        }
    }

    @GetMapping("/test/inventory/health")
    public ResponseEntity<String> testInventoryServiceHealth() {
        try {
            String url = inventoryServiceUrl + "/api/inventory/health";
            String response = restTemplate.getForObject(url, String.class);
            ResponseFactory factory = ResponseFactory.getFactory(ResponseType.SUCCESS);
            return factory.createResponse("Inventory service health check completed", response);
        } catch (Exception e) {
            ResponseFactory factory = ResponseFactory.getFactory(ResponseType.ERROR);
            return factory.createResponse("Inventory service health check failed", "HEALTH_CHECK_FAILED");
        }
    }

    @GetMapping("/config")
    public ResponseEntity<String> showConfig() {
        ResponseFactory factory = ResponseFactory.getFactory(ResponseType.SUCCESS);
        String configData = String.format(
                "{\"userServiceUrl\":\"%s\",\"productServiceUrl\":\"%s\",\"inventoryServiceUrl\":\"%s\"}",
                userServiceUrl, productServiceUrl, inventoryServiceUrl);
        return factory.createResponse("Configuration retrieved", configData);
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private ResponseEntity<String> forwardToProductService(String path, String method, String body, String token) {
        return forwardRequest(productServiceUrl + path, method, body, token);
    }

    private ResponseEntity<String> forwardToUserService(String path, String method, String body, String token) {
        return forwardRequest(userServiceUrl + path, method, body, token);
    }

    private ResponseEntity<String> forwardToInventoryService(String path, String method, String body, String token) {
        return forwardRequest(inventoryServiceUrl + path, method, body, token);
    }

    private ResponseEntity<String> forwardRequest(String url, String method, String body, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (token != null && !token.isEmpty()) {
                headers.set("Authorization", token);
            }

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            HttpMethod httpMethod = switch (method.toUpperCase()) {
                case "GET" -> HttpMethod.GET;
                case "POST" -> HttpMethod.POST;
                case "PUT" -> HttpMethod.PUT;
                case "DELETE" -> HttpMethod.DELETE;
                default -> HttpMethod.GET;
            };

            ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, entity, String.class);

            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            ResponseFactory factory = ResponseFactory.getFactory(ResponseType.ERROR);
            return factory.createResponse("Service unavailable: " + e.getMessage(), "GATEWAY_ERROR");
        }
    }

    private String extractPath(HttpServletRequest request, String prefix) {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith(prefix)) {
            String path = requestURI.substring(prefix.length());
            return path.isEmpty() ? "" : path;
        }
        return "";
    }
}