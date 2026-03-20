package lk.iit.nextora.module.club.dto.response.dashboard;

import lk.iit.nextora.module.club.dto.response.ClubResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicStaffClubDashboardResponse {

    private long totalActiveClubs;
    private long openForRegistrationCount;
    private List<ClubResponse> clubs;
    private List<ClubResponse> advisingClubs;
    private LocalDateTime generatedAt;
}

