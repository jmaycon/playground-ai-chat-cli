// src/main/java/edu/jmaycon/playground/BusinessCardRepository.java
package edu.jmaycon.playground;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BusinessCardRepository {

    private final VectorStore store; // PgVectorStore via Spring config

    private final List<BusinessCard> cards = new ArrayList<>();
    private final Map<String, List<BusinessCard>> byService = new HashMap<>();

    @PostConstruct
    public void init() throws Exception {
        loadData();
        indexVectors();
    }

    public List<BusinessCard> searchByNameContains(String q) {
        if (q == null || q.isBlank()) return List.of();
        var lq = q.toLowerCase(Locale.ROOT);
        return cards.stream()
                .filter(c -> contains(c.name(), lq) || contains(c.company(), lq))
                .limit(50)
                .toList();
    }

    public List<BusinessCard> searchByService(String service) {
        if (service == null || service.isBlank()) return List.of();
        return byService.getOrDefault(service.toLowerCase(Locale.ROOT), List.of());
    }

    public List<BusinessCard> searchByServiceAndPrice(String service, double maxPrice) {
        var list = searchByService(service);
        var key = service == null ? "" : service.toLowerCase(Locale.ROOT);
        return list.stream()
                .filter(c -> c.prices().getOrDefault(key, Double.MAX_VALUE) <= maxPrice)
                .sorted(Comparator.comparingDouble(c -> c.prices().getOrDefault(key, Double.MAX_VALUE)))
                .limit(50)
                .toList();
    }

    public List<BusinessCard> semanticSearch(String query, int topK, double minScore) {
        if (query == null || query.isBlank()) return List.of();
        var hits = store.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(minScore)
                .build());
        var byId = cards.stream().collect(Collectors.toMap(BusinessCard::id, c -> c));
        return hits.stream()
                .map(d -> byId.get(d.getId()))
                .filter(Objects::nonNull)
                .toList();
    }

    // ---------- internals ----------
    private void loadData() throws Exception {
        var cpr = new ClassPathResource("data/business-cards.json");
        try (InputStream in = cpr.getInputStream()) {
            var om = new ObjectMapper();
            var loaded = om.readValue(in, new TypeReference<List<BusinessCard>>() {});
            cards.clear();
            cards.addAll(loaded);

            byService.clear();
            for (var c : cards) {
                for (var s : c.services()) {
                    var k = s.trim().toLowerCase(Locale.ROOT);
                    byService.computeIfAbsent(k, x -> new ArrayList<>()).add(c);
                }
            }
        }
    }

    private void indexVectors() {
        var docs = cards.stream()
                .map(c -> {
                    var svc = String.join(", ", c.services());
                    var priceLines = c.prices().entrySet().stream()
                            .sorted(Entry.comparingByKey())
                            .map(e -> e.getKey() + ":€" + e.getValue())
                            .collect(Collectors.joining(" | "));
                    var text =
                            """
                Provider: %s
                Company: %s
                City: %s
                Services: %s
                Prices: %s
                Contact: %s %s
                """
                                    .formatted(
                                            safe(c.name()),
                                            safe(c.company()),
                                            safe(c.city()),
                                            svc,
                                            priceLines,
                                            safe(c.email()),
                                            safe(c.phone()))
                                    .replaceAll("[\\s\\n]+", " ")
                                    .trim();

                    Map<String, Object> meta = Map.of(
                            "id", c.id(),
                            "name", c.name(),
                            "company", c.company(),
                            "city", c.city(),
                            "services", String.join(",", c.services()));
                    return Document.builder().id(c.id).text(text).metadata(meta).build();
                })
                .toList();

        // persist to Postgres (pgvector)
        store.add(docs);
    }

    private static boolean contains(String s, String q) {
        return s != null && s.toLowerCase(Locale.ROOT).contains(q);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    public record BusinessCard(
            String id,
            String name,
            String company,
            String email,
            String phone,
            String city,
            List<String> services,
            Map<String, Double> prices) {}
}
