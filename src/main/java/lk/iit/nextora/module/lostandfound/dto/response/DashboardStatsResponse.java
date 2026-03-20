package lk.iit.nextora.module.lostandfound.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalLostItems;
    private long totalFoundItems;
    private long activeLostItems;
    private long activeFoundItems;
    private long pendingClaims;
    private long approvedClaims;
    private long rejectedClaims;
    private long totalCategories;
}
