package lk.iit.nextora.module.lostandfound.service.impl;

import lk.iit.nextora.module.lostandfound.service.ItemMatchingService;
import org.springframework.stereotype.Service;

@Service
public class ItemMatchingServiceImpl implements ItemMatchingService {

    @Override
    public double calculateMatchScore(String lostTitle, String foundTitle) {
        if (lostTitle == null || foundTitle == null) return 0.0;
        return lostTitle.equalsIgnoreCase(foundTitle) ? 1.0 : 0.5;
    }
}
