package lk.iit.nextora.module.lostandfound.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ItemListResponse {

    private List<ItemResponse> items;
    private long totalElements;
}
