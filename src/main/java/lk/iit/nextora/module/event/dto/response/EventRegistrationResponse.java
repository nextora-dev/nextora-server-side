package lk.iit.nextora.module.event.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventRegistrationResponse {

    private Long id;
    private Long eventId;
    private String eventTitle;
    private LocalDateTime eventStartAt;
    private LocalDateTime eventEndAt;
    private String eventLocation;
    private Long userId;
    private String userName;
    private String userEmail;
    private LocalDateTime registeredAt;
    private Boolean isCancelled;
    private LocalDateTime cancelledAt;
}
