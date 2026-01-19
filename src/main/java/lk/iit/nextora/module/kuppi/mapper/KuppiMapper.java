package lk.iit.nextora.module.kuppi.mapper;

import lk.iit.nextora.common.mapper.MapperConfiguration;
import lk.iit.nextora.module.kuppi.dto.request.CreateKuppiNoteRequest;
import lk.iit.nextora.module.kuppi.dto.request.CreateKuppiSessionRequest;
import lk.iit.nextora.module.kuppi.dto.response.KuppiNoteResponse;
import lk.iit.nextora.module.kuppi.dto.response.KuppiSessionResponse;
import lk.iit.nextora.module.kuppi.entity.KuppiNote;
import lk.iit.nextora.module.kuppi.entity.KuppiSession;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Kuppi module entities and DTOs
 */
@Mapper(config = MapperConfiguration.class)
public interface KuppiMapper {

    // ==================== Session Mappings ====================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "host", ignore = true)
    @Mapping(target = "sessionType", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    KuppiSession toEntity(CreateKuppiSessionRequest request);

    @Mapping(target = "hostId", source = "host.id")
    @Mapping(target = "hostName", expression = "java(session.getHost().getFullName())")
    @Mapping(target = "hostEmail", source = "host.email")
    @Mapping(target = "canJoin", expression = "java(session.isJoinable())")
    @Mapping(target = "notes", source = "notes", qualifiedByName = "mapNotesToResponse")
    KuppiSessionResponse toResponse(KuppiSession session);

    @Named("mapNotesToResponse")
    default List<KuppiNoteResponse> mapNotesToResponse(java.util.Set<KuppiNote> notes) {
        if (notes == null || notes.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return notes.stream()
                .filter(note -> !note.getIsDeleted())
                .map(this::toResponse)
                .toList();
    }

    List<KuppiSessionResponse> toResponseList(List<KuppiSession> sessions);

    // ==================== Note Mappings ====================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "session", ignore = true)
    @Mapping(target = "uploadedBy", ignore = true)
    @Mapping(target = "downloadCount", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    KuppiNote toEntity(CreateKuppiNoteRequest request);

    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "sessionTitle", source = "session.title")
    @Mapping(target = "uploadedById", source = "uploadedBy.id")
    @Mapping(target = "uploaderName", expression = "java(note.getUploadedBy().getFullName())")
    @Mapping(target = "formattedFileSize", expression = "java(note.getFileSize() != null ? lk.iit.nextora.common.util.FileUtils.formatFileSize(note.getFileSize()) : null)")
    KuppiNoteResponse toResponse(KuppiNote note);

    List<KuppiNoteResponse> toNoteResponseList(List<KuppiNote> notes);

    // ==================== Update Mappings ====================

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "host", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateSessionFromRequest(lk.iit.nextora.module.kuppi.dto.request.UpdateKuppiSessionRequest request, @MappingTarget KuppiSession session);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "session", ignore = true)
    @Mapping(target = "uploadedBy", ignore = true)
    @Mapping(target = "downloadCount", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    void updateNoteFromRequest(lk.iit.nextora.module.kuppi.dto.request.UpdateKuppiNoteRequest request, @MappingTarget KuppiNote note);
}

