package com.example.assignment;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

@SpringBootApplication
public class AssignmentApp implements CommandLineRunner {

    public static void main(String[] args) {
        // Disable Tomcat (no web server needed)
        new SpringApplicationBuilder(AssignmentApp.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(String... args) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            // STEP 1: Generate Webhook
            String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

            JSONObject request = new JSONObject();
            request.put("name", "Deepam Goyal"); 
            request.put("regNo", "22BCY10060"); 
            request.put("email", "deepam02goyal@gmail.com"); 

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(request.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            JSONObject json = new JSONObject(response.getBody());
            String webhookUrl = json.getString("webhook");
            String accessToken = json.getString("accessToken");

            System.out.println("Webhook URL: " + webhookUrl);
            System.out.println("Access Token: " + accessToken);

            // STEP 2: Final SQL Query
            String finalQuery =
                "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, " +
                "COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT " +
                "FROM EMPLOYEE e1 " +
                "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID " +
                "LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT " +
                "AND e2.DOB > e1.DOB " +
                "GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME " +
                "ORDER BY e1.EMP_ID DESC;";

            JSONObject submitBody = new JSONObject();
            submitBody.put("finalQuery", finalQuery);

            HttpHeaders submitHeaders = new HttpHeaders();
            submitHeaders.setContentType(MediaType.APPLICATION_JSON);
            submitHeaders.set("Authorization", accessToken);

            HttpEntity<String> submitEntity = new HttpEntity<>(submitBody.toString(), submitHeaders);

            ResponseEntity<String> submitResponse =
                    restTemplate.postForEntity(webhookUrl, submitEntity, String.class);

            System.out.println("Response: " + submitResponse.getBody());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
