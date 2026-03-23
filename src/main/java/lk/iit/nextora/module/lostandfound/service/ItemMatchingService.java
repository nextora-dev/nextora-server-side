package lk.iit.nextora.module.lostandfound.service;

import lk.iit.nextora.module.lostandfound.dto.response.MatchSuggestionResponse;

import java.util.List;

public interface ItemMatchingService {

    double calculateMatchScore(String lostTitle, String foundTitle);

    List<MatchSuggestionResponse> findMatchesForLostItem(Long lostItemId);

    List<MatchSuggestionResponse> findMatchesForFoundItem(Long foundItemId);
}
