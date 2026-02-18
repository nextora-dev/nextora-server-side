package lk.iit.nextora.module.lostandfound.service;

public interface ItemMatchingService {

    double calculateMatchScore(String lostTitle, String foundTitle);
}
