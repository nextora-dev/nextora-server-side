package lk.iit.nextora.module.lostandfound.repository;

import lk.iit.nextora.module.lostandfound.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByLostItemId(Long lostItemId);

    List<Claim> findByFoundItemId(Long foundItemId);

    List<Claim> findByStatus(String status);

    long countByStatus(String status);
}
