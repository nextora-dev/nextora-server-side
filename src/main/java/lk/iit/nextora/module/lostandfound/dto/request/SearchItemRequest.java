package lk.iit.nextora.module.lostandfound.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchItemRequest {

    private String keyword;
    private String category;
    private int page = 0;
    private int size = 10;
}
