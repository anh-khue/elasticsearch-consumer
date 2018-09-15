package io.anhkhue.elasticsearchconsumer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ElasticsearchConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElasticsearchConsumerApplication.class, args);
    }

    @Bean
    WebClient webClient() {
        return WebClient.builder()
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .baseUrl("http://localhost:9200")
                        .build();
    }

    @Bean
    CommandLineRunner retrieveData(WebClient client) {
        try {
            JSONObject message = new JSONObject();
            message.put("keyword", "Tôi muốn trở thành một Back-end Developer.");

            JSONObject match = new JSONObject();
            match.put("match", message);

            JSONObject requestBody = new JSONObject();
            requestBody.put("query", match);

            return args -> {
                String response = client.post()
                                        .uri("/elastic_keywords/my_type/_search")
                                        .body(BodyInserters.fromObject(requestBody.toString()))
                                        .accept(MediaType.APPLICATION_JSON)
                                        .retrieve()
                                        .bodyToMono(String.class)
                                        .block();

                JSONArray hitsArray = new JSONObject(response)
                        .getJSONObject("hits")
                        .getJSONArray("hits");

                List<String> hitKeywords = new ArrayList<>();

                for (int i = 0; i < hitsArray.length(); i++) {
                    JSONObject hit = hitsArray.getJSONObject(i);
                    hitKeywords.add(hit.getJSONObject("_source")
                                       .getString("keyword"));
                }

                hitKeywords.forEach(System.out::println);
            };
        } catch (JSONException e) {
            throw new RuntimeException();
        }
    }
}
