package lk.iit.nextora.module.lostandfound.service.impl;

import lk.iit.nextora.common.exception.custom.ResourceNotFoundException;
import lk.iit.nextora.module.lostandfound.dto.response.MatchSuggestionResponse;
import lk.iit.nextora.module.lostandfound.entity.FoundItem;
import lk.iit.nextora.module.lostandfound.entity.ItemMatch;
import lk.iit.nextora.module.lostandfound.entity.LostItem;
import lk.iit.nextora.module.lostandfound.repository.FoundItemRepository;
import lk.iit.nextora.module.lostandfound.repository.ItemMatchRepository;
import lk.iit.nextora.module.lostandfound.repository.LostItemRepository;
import lk.iit.nextora.module.lostandfound.service.ItemMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemMatchingServiceImpl implements ItemMatchingService {

    private static final double HIGH_SCORE_THRESHOLD = 0.5;

    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;
    private final ItemMatchRepository itemMatchRepository;

    @Override
    public double calculateMatchScore(String lostTitle, String foundTitle) {
        if (lostTitle == null || foundTitle == null) return 0.0;

        String normalizedLost = lostTitle.trim().toLowerCase();
        String normalizedFound = foundTitle.trim().toLowerCase();

        if (normalizedLost.equals(normalizedFound)) return 1.0;

        if (normalizedLost.contains(normalizedFound) || normalizedFound.contains(normalizedLost)) {
            return 0.8;
        }

        String[] lostWords = normalizedLost.split("\\s+");
        String[] foundWords = normalizedFound.split("\\s+");

        long sharedWords = 0;
        for (String lostWord : lostWords) {
            for (String foundWord : foundWords) {
                if (lostWord.equals(foundWord) && lostWord.length() > 2) {
                    sharedWords++;
                    break;
                }
            }
        }

        int shorterLength = Math.min(lostWords.length, foundWords.length);
        if (shorterLength == 0) return 0.0;

        return (double) sharedWords / shorterLength * 0.7;
    }

    @Override
    @Transactional
    public List<MatchSuggestionResponse> findMatchesForLostItem(Long lostItemId) {
        LostItem lostItem = lostItemRepository.findById(lostItemId)
                .orElseThrow(() -> new ResourceNotFoundException("LostItem", "id", lostItemId));

        List<FoundItem> foundItems = foundItemRepository.findByActiveTrue(Pageable.unpaged()).getContent();
        List<MatchSuggestionResponse> results = new ArrayList<>();

        for (FoundItem foundItem : foundItems) {
            double titleScore = calculateMatchScore(lostItem.getTitle(), foundItem.getTitle());

            boolean sameCategory = lostItem.getCategory() != null && foundItem.getCategory() != null
                    && lostItem.getCategory().getId().equals(foundItem.getCategory().getId());
            double categoryBonus = sameCategory ? 0.15 : 0.0;

            boolean similarLocation = hasSimilarLocation(lostItem.getLocation(), foundItem.getLocation());
            double locationBonus = similarLocation ? 0.1 : 0.0;

            double finalScore = Math.min(1.0, titleScore + categoryBonus + locationBonus);

            if (finalScore >= HIGH_SCORE_THRESHOLD) {
                if (!itemMatchRepository.existsByLostItemIdAndFoundItemId(lostItemId, foundItem.getId())) {
                    String reason = buildMatchReason(lostItem, foundItem, titleScore, sameCategory, similarLocation);
                    ItemMatch match = ItemMatch.builder()
                            .lostItem(lostItem)
                            .foundItem(foundItem)
                            .score(finalScore)
                            .matchReason(reason)
                            .build();
                    itemMatchRepository.save(match);
                }

                results.add(MatchSuggestionResponse.builder()
                        .lostItemId(lostItemId)
                        .lostItemTitle(lostItem.getTitle())
                        .foundItemId(foundItem.getId())
                        .foundItemTitle(foundItem.getTitle())
                        .matchScore(finalScore)
                        .matchReason(buildMatchReason(lostItem, foundItem, titleScore, sameCategory, similarLocation))
                        .build());
            }
        }

        log.info("Found {} matches for lostItem {}", results.size(), lostItemId);
        return results;
    }

    @Override
    @Transactional
    public List<MatchSuggestionResponse> findMatchesForFoundItem(Long foundItemId) {
        FoundItem foundItem = foundItemRepository.findById(foundItemId)
                .orElseThrow(() -> new ResourceNotFoundException("FoundItem", "id", foundItemId));

        List<LostItem> lostItems = lostItemRepository.findByActiveTrue(Pageable.unpaged()).getContent();
        List<MatchSuggestionResponse> results = new ArrayList<>();

        for (LostItem lostItem : lostItems) {
            double titleScore = calculateMatchScore(lostItem.getTitle(), foundItem.getTitle());

            boolean sameCategory = lostItem.getCategory() != null && foundItem.getCategory() != null
                    && lostItem.getCategory().getId().equals(foundItem.getCategory().getId());
            double categoryBonus = sameCategory ? 0.15 : 0.0;

            boolean similarLocation = hasSimilarLocation(lostItem.getLocation(), foundItem.getLocation());
            double locationBonus = similarLocation ? 0.1 : 0.0;

            double finalScore = Math.min(1.0, titleScore + categoryBonus + locationBonus);

            if (finalScore >= HIGH_SCORE_THRESHOLD) {
                if (!itemMatchRepository.existsByLostItemIdAndFoundItemId(lostItem.getId(), foundItemId)) {
                    String reason = buildMatchReason(lostItem, foundItem, titleScore, sameCategory, similarLocation);
                    ItemMatch match = ItemMatch.builder()
                            .lostItem(lostItem)
                            .foundItem(foundItem)
                            .score(finalScore)
                            .matchReason(reason)
                            .build();
                    itemMatchRepository.save(match);
                }

                results.add(MatchSuggestionResponse.builder()
                        .lostItemId(lostItem.getId())
                        .lostItemTitle(lostItem.getTitle())
                        .foundItemId(foundItemId)
                        .foundItemTitle(foundItem.getTitle())
                        .matchScore(finalScore)
                        .matchReason(buildMatchReason(lostItem, foundItem, titleScore, sameCategory, similarLocation))
                        .build());
            }
        }

        log.info("Found {} matches for foundItem {}", results.size(), foundItemId);
        return results;
    }

    private boolean hasSimilarLocation(String loc1, String loc2) {
        if (loc1 == null || loc2 == null) return false;
        String l1 = loc1.toLowerCase();
        String l2 = loc2.toLowerCase();
        return l1.contains(l2) || l2.contains(l1);
    }

    private String buildMatchReason(LostItem lostItem, FoundItem foundItem,
                                     double titleScore, boolean sameCategory, boolean similarLocation) {
        StringBuilder reason = new StringBuilder();

        if (titleScore >= 1.0) {
            reason.append("Exact title match");
        } else if (titleScore >= 0.8) {
            reason.append("Strong title similarity");
        } else if (titleScore > 0) {
            reason.append(String.format("Partial title match (%.0f%%)", titleScore * 100));
        }

        if (sameCategory) {
            if (reason.length() > 0) reason.append(", ");
            reason.append("same category: ").append(lostItem.getCategory().getName());
        }

        if (similarLocation) {
            if (reason.length() > 0) reason.append(", ");
            reason.append("similar location");
        }

        return reason.toString();
    }
}