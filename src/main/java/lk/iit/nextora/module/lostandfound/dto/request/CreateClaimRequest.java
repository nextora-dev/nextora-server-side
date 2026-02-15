package lk.iit.nextora.module.lostandfound.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateClaimRequest {

    @NotNull
    private Long lostItemId;

    @NotNull
    private Long foundItemId;

    private String proofDescription;
}
