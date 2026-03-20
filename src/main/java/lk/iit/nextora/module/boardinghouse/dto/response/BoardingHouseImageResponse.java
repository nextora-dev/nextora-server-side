package lk.iit.nextora.module.boardinghouse.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardingHouseImageResponse {

    private Long id;
    private String imageUrl;
    private Integer displayOrder;
    private Boolean isPrimary;
    private LocalDateTime createdAt;
}
