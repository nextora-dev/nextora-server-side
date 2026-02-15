package lk.iit.nextora.module.event.mapper;

import lk.iit.nextora.common.mapper.MapperConfiguration;
import lk.iit.nextora.module.event.dto.request.CreateEventRequest;
import lk.iit.nextora.module.event.dto.request.UpdateEventRequest;
import lk.iit.nextora.module.event.dto.response.EventResponse;
import lk.iit.nextora.module.event.entity.Event;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Event module entities and DTOs
 */
@Mapper(config = MapperConfiguration.class)
public interface EventMapper {

    // ==================== Create Mapping ====================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Event toEntity(CreateEventRequest request);

    // ==================== Response Mapping ====================

    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByName", expression = "java(event.getCreatedBy().getFullName())")
    @Mapping(target = "createdByEmail", source = "createdBy.email")
    @Mapping(target = "canEdit", expression = "java(event.canEdit())")
    @Mapping(target = "isVisible", expression = "java(event.isVisible())")
    @Mapping(target = "isUpcoming", expression = "java(event.isUpcoming())")
    @Mapping(target = "isOngoing", expression = "java(event.isOngoing())")
    EventResponse toResponse(Event event);

    List<EventResponse> toResponseList(List<Event> events);

    // ==================== Update Mapping ====================

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEventFromRequest(UpdateEventRequest request, @MappingTarget Event event);
}
