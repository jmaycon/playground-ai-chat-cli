package edu.jmaycon.playground;

import edu.jmaycon.playground.BusinessCardRepository.BusinessCard;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusinessCardTools {

    private final BusinessCardRepository repo;

    @Tool(
            name = "find_cards_by_name",
            description =
                    """
                Retrieve professional service providers by matching a name or company fragment.
                Useful when the user remembers only part of the provider’s name (e.g. “Marta”, “CleanCo”, or “FixIt”).
                Returns each provider with their contact info and per-service estimated prices.
                """)
    public BusinessCardSearchResponse findByName(@ToolParam(description = "Partial name or company") String query) {
        var matches = repo.searchByNameContains(query);
        return new BusinessCardSearchResponse(query, null, null, matches.size(), matches);
    }

    @Tool(
            name = "find_cards_by_service",
            description =
                    """
                Retrieve local service providers by the type of service offered
                (e.g. “plumbing”, “electrical repair”, “home cleaning”, “roofing”).
                Results are sorted by lowest price for that specific service and include full contact and pricing data.
                """)
    public BusinessCardSearchResponse findByService(
            @ToolParam(description = "Type of service requested, like 'cleaning' or 'construction'") String service) {
        var matches = repo.searchByService(service).stream()
                .sorted(Comparator.comparingDouble(
                        a -> a.prices().getOrDefault(service.toLowerCase(), Double.MAX_VALUE)))
                .limit(50)
                .toList();
        return new BusinessCardSearchResponse(null, service, null, matches.size(), matches);
    }

    @Tool(
            name = "find_cards_by_service_and_price",
            description =
                    """
                Find professionals by service type and filter by maximum acceptable price.
                Example: find cleaning services under 80 euros, or electricians below 150.
                Prioritizes affordable, verified providers and returns relevant contact information.
                """)
    public BusinessCardSearchResponse findByServiceAndPrice(
            @ToolParam(description = "Service type, e.g. 'plumbing', 'painting'") String service,
            @ToolParam(description = "Maximum price threshold for the given service") Double maxPrice) {
        var matches = repo.searchByServiceAndPrice(service, maxPrice);
        return new BusinessCardSearchResponse(null, service, maxPrice, matches.size(), matches);
    }

    @Tool(
            name = "semantic_find_cards",
            description =
                    """
                Perform a semantic (meaning-based) search to find the best matching service providers.
                The query can be natural language such as:
                “I need someone to fix a leaking kitchen pipe” or “looking for affordable home cleaning in Berlin”.
                Uses embeddings with similarity threshold 0.7 to find conceptually relevant providers.
                """)
    public BusinessCardSearchResponse semanticFind(
            @ToolParam(description = "Natural language query describing the service need", required = true)
                    String query,
            @ToolParam(description = "Maximum number of results to return (default 10, max 50)", required = false)
                    Integer topK) {
        var k = (topK == null || topK < 1) ? 10 : Math.min(topK, 50);
        var matches = repo.semanticSearch(query, k, 0.60);
        return new BusinessCardSearchResponse(query, null, null, matches.size(), matches);
    }

    public record BusinessCardSearchResponse(
            String query, String service, Double maxPrice, int total, List<BusinessCard> matches) {}
}
