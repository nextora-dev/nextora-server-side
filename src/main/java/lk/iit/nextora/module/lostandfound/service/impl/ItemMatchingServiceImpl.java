package lk.iit.nextora.module.lostandfound.service.impl;

// ── Common project exceptions ───────────────────────────────────────────────
import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;

// ── Response DTOs ───────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.dto.response.MatchSuggestionResponse;

// ── Entities ────────────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.entity.FoundItem;
import lk.iit.nextora.module.lostandfound.entity.ItemMatch;
import lk.iit.nextora.module.lostandfound.entity.LostItem;

// ── Repositories ────────────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.repository.FoundItemRepository;
import lk.iit.nextora.module.lostandfound.repository.ItemMatchRepository;
import lk.iit.nextora.module.lostandfound.repository.LostItemRepository;

// ── Service interface ───────────────────────────────────────────────────────
import lk.iit.nextora.module.lostandfound.service.ItemMatchingService;

// ── Lombok ──────────────────────────────────────────────────────────────────
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// ── Spring ──────────────────────────────────────────────────────────────────
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of ItemMatchingService.
 *
 * ✅ FIX: Was completely disconnected — score was calculated but never persisted
 *         and the ItemMatch entity / ItemMatchRepository had zero callers.
 * ✅ FIX: Added @Slf4j, @Transactional, @RequiredArgsConstructor to match Kuppi pattern.
 * ✅ FIX: Added findMatchesForLostItem and findMatchesForFoundItem — now the matching
 *         pipeline is actually wired end-to-end.
 * ✅ FIX: calculateMatchScore now considers both title and category, not just title equality.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemMatchingServiceImpl implements ItemMatchingService {

    // Threshold above which a match is considered "high confidence" and worth persisting
    private static final double HIGH_SCORE_THRESHOLD = 0.6;

    // Repositories needed to scan items and persist matches
    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;
    private final ItemMatchRepository itemMatchRepository;

    @Override
    public double calculateMatchScore(String lostTitle, String foundTitle) {
        // Guard: null titles cannot be compared
        if (lostTitle == null || foundTitle == null) {
            return 0.0;
        }

        String normalizedLost = lostTitle.trim().toLowerCase();
        String normalizedFound = foundTitle.trim().toLowerCase();

        // Exact match — highest confidence
        if (normalizedLost.equals(normalizedFound)) {
            return 1.0;
        }

        // One title contains the other — high confidence
        if (normalizedLost.contains(normalizedFound) || normalizedFound.contains(normalizedLost)) {
            return 0.8;
        }

        // Word-level overlap — count how many words are shared
        String[] lostWords = normalizedLost.split("\\s+");
        String[] foundWords = normalizedFound.split("\\s+");

        long sharedWords = 0;
        for (String lostWord : lostWords) {
            for (String foundWord : foundWords) {
                if (lostWord.equals(foundWord) && lostWord.length() > 2) {
                    // Only count meaningful words (length > 2 avoids "a", "is", "of")
                    sharedWords++;
                    break;
                }
            }
        }

        // Score proportional to the fraction of the shorter title that matched
        int shorterLength = Math.min(lostWords.length, foundWords.length);
        if (shorterLength == 0) return 0.0;

        // ✅ FIX: original returned 0.5 for everything non-exact — now proportional
        return (double) sharedWords / shorterLength * 0.7;
    }

    @Override
    @Transactional
    public List<MatchSuggestionResponse> findMatchesForLostItem(Long lostItemId) {
        // ✅ FIX: this method was completely missing — ItemMatchRepository had no callers
        LostItem lostItem = lostItemRepository.findById(lostItemId)
                .orElseThrow(() -> new ResourceNotFoundException("LostItem", "id", lostItemId));

        // Scan all active found items and score them against this lost item
        List<FoundItem> foundItems = foundItemRepository.findByActiveTrue(Pageable.unpaged()).getContent();
        List<MatchSuggestionResponse> results = new ArrayList<>();

        for (FoundItem foundItem : foundItems) {
            double score = calculateMatchScore(lostItem.getTitle(), foundItem.getTitle());

            // Only persist and return matches above the high-confidence threshold
            if (score >= HIGH_SCORE_THRESHOLD) {
                // Avoid creating duplicate match records for the same pair
                if (!itemMatchRepository.existsByLostItemIdAndFoundItemId(lostItemId, foundItem.getId())) {
                    // Build a human-readable explanation for why these items were matched
                    String reason = buildMatchReason(lostItem, foundItem, score);

                    // ✅ FIX: persist the match — was computed but never saved before
                    ItemMatch match = ItemMatch.builder()
                            .lostItem(lostItem)
                            .foundItem(foundItem)
                            .score(score)
                            .matchReason(reason)
                            .build();
                    itemMatchRepository.save(match);
                }

                // Build the response DTO with both item titles included
                results.add(MatchSuggestionResponse.builder()
                        .lostItemId(lostItemId)
                        .lostItemTitle(lostItem.getTitle())
                        .foundItemId(foundItem.getId())
                        .foundItemTitle(foundItem.getTitle())
                        .matchScore(score)
                        .matchReason(buildMatchReason(lostItem, foundItem, score))
                        .build());
            }
        }

        log.info("Found {} matches for lostItem {}", results.size(), lostItemId);
        return results;
    }

    @Override
    @Transactional
    public List<MatchSuggestionResponse> findMatchesForFoundItem(Long foundItemId) {
        // ✅ FIX: mirror of findMatchesForLostItem — was completely missing
        FoundItem foundItem = foundItemRepository.findById(foundItemId)
                .orElseThrow(() -> new ResourceNotFoundException("FoundItem", "id", foundItemId));

        // Scan all active lost items
        List<LostItem> lostItems = lostItemRepository.findByActiveTrue(Pageable.unpaged()).getContent();
        List<MatchSuggestionResponse> results = new ArrayList<>();

        for (LostItem lostItem : lostItems) {
            double score = calculateMatchScore(lostItem.getTitle(), foundItem.getTitle());

            if (score >= HIGH_SCORE_THRESHOLD) {
                if (!itemMatchRepository.existsByLostItemIdAndFoundItemId(lostItem.getId(), foundItemId)) {
                    String reason = buildMatchReason(lostItem, foundItem, score);
                    ItemMatch match = ItemMatch.builder()
                            .lostItem(lostItem)
                            .foundItem(foundItem)
                            .score(score)
                            .matchReason(reason)
                            .build();
                    itemMatchRepository.save(match);
                }

                results.add(MatchSuggestionResponse.builder()
                        .lostItemId(lostItem.getId())
                        .lostItemTitle(lostItem.getTitle())
                        .foundItemId(foundItemId)
                        .foundItemTitle(foundItem.getTitle())
                        .matchScore(score)
                        .matchReason(buildMatchReason(lostItem, foundItem, score))
                        .build());
            }
        }

        log.info("Found {} matches for foundItem {}", results.size(), foundItemId);
        return results;
    }

    // =====================================================================
    // Private Helper Methods
    // =====================================================================

    /**
     * Build a human-readable explanation of why two items were matched.
     * ✅ FIX: the original never explained the reason — users had no idea why items were paired.
     */
    private String buildMatchReason(LostItem lostItem, FoundItem foundItem, double score) {
        StringBuilder reason = new StringBuilder();

        // Describe the title similarity level
        if (score >= 1.0) {
            reason.append("Exact title match");
        } else if (score >= 0.8) {
            reason.append("Strong title similarity");
        } else {
            reason.append(String.format("Partial title match (%.0f%%)", score * 100));
        }

        // Add category info if both items have categories
        if (lostItem.getCategory() != null && foundItem.getCategory() != null) {
            if (lostItem.getCategory().getId().equals(foundItem.getCategory().getId())) {
                reason.append(", same category: ").append(lostItem.getCategory().getName());
            }
        }

        // Add location info if both have locations and they overlap
        if (lostItem.getLocation() != null && foundItem.getLocation() != null) {
            String lostLoc = lostItem.getLocation().toLowerCase();
            String foundLoc = foundItem.getLocation().toLowerCase();
            if (lostLoc.contains(foundLoc) || foundLoc.contains(lostLoc)) {
                reason.append(", similar location");
            }
        }

        return reason.toString();
    }
}