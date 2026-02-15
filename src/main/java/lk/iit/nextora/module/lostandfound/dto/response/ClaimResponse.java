package lk.iit.nextora.module.lostandfound.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClaimResponse {

    private Long id;
    private Long lostItemId;
    private Long foundItemId;
    private String status;
}
