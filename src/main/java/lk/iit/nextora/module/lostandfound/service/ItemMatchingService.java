package lk.iit.nextora.module.lostandfound.service;

// ── Response DTO ─────────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.response.MatchSuggestionResponse;

import java.util.List;

/**
 * Service interface for computing similarity matches between lost and found items.
 *
 * ✅ FIX: Added findMatchesForLostItem and findMatchesForFoundItem — the matching
 *         service was previously computing scores but NEVER persisting or returning them.
 *         The ItemMatch entity and ItemMatchRepository existed with no callers.
 */
public interface ItemMatchingService {

    /**
     * Compute a similarity score between a lost item title and a found item title.
     * Returns a value between 0.0 (no similarity) and 1.0 (exact match).
     * Used internally when scanning for potential matches.
     */
    double calculateMatchScore(String lostTitle, String foundTitle);

    /**
     * Find all found items that are likely matches for the given lost item.
     * Computes scores against all active found items, saves high-scoring matches,
     * and returns them sorted by score descending.
     *
     * ✅ FIX: this method was missing — the ItemMatchRepository had no callers.
     */
    List<MatchSuggestionResponse> findMatchesForLostItem(Long lostItemId);

    /**
     * Find all lost items that are likely matches for the given found item.
     * Mirror of findMatchesForLostItem — called when a new found item is reported.
     *
     * ✅ FIX: this method was missing — matching only worked in one direction.
     */
    List<MatchSuggestionResponse> findMatchesForFoundItem(Long foundItemId);
}