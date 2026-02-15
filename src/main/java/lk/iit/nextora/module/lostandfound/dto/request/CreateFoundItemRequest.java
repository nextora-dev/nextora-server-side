package lk.iit.nextora.module.lostandfound.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFoundItemRequest {

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String category;

    private String location;

    @NotBlank
    private String contactNumber;
}
