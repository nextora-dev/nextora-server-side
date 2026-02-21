package lk.iit.nextora.module.lostandfound.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateItemRequest {

    private String title;
    private String description;
    private String location;
    private String contactNumber;
    private Boolean active;
}
