
package com.footwear.apigateway.controller;

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

    // Health check pentru gateway
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\":\"UP\",\"service\":\"api-gateway\"}");
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
    // TEST ROUTES (pentru debugging)
    // ==========================================

    @GetMapping("/test/user")
    public ResponseEntity<String> testUserService() {
        try {
            String url = userServiceUrl + "/api/users/test";
            String response = restTemplate.getForObject(url, String.class);
            return ResponseEntity.ok("{\"gateway\":\"OK\",\"user-service\":\"" + response + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"User service unavailable\",\"details\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/test/user/health")
    public ResponseEntity<String> testUserServiceHealth() {
        try {
            String url = userServiceUrl + "/api/users/health";
            String response = restTemplate.getForObject(url, String.class);
            return ResponseEntity.ok("{\"gateway\":\"OK\",\"user-service-health\":" + response + "}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"User service health check failed\",\"details\":\"" + e.getMessage() + "\"}");
        }
    }

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


    @GetMapping("/config")
    public ResponseEntity<String> showConfig() {
        return ResponseEntity.ok("{\"userServiceUrl\":\"" + userServiceUrl + "\",\"productServiceUrl\":\"" + productServiceUrl + "\",\"inventoryServiceUrl\":\"" + inventoryServiceUrl + "\"}");
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Service unavailable: " + e.getMessage() + "\"}");
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
