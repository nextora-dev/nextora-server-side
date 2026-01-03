package lk.iit.nextora.common.mapper;

import lk.iit.nextora.common.dto.PagedResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base mapper interface providing common mapping methods.
 * All module-specific mappers should extend this interface.
 *
 * @param <E> Entity type
 * @param <D> DTO type
 */
public interface BaseMapper<E, D> {

    /**
     * Convert entity to DTO
     *
     * @param entity the source entity
     * @return the mapped DTO
     */
    D toDto(E entity);

    /**
     * Convert DTO to entity
     *
     * @param dto the source DTO
     * @return the mapped entity
     */
    E toEntity(D dto);

    /**
     * Convert list of entities to list of DTOs
     *
     * @param entities the source entity list
     * @return the mapped DTO list
     */
    default List<D> toDtoList(List<E> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of DTOs to list of entities
     *
     * @param dtos the source DTO list
     * @return the mapped entity list
     */
    default List<E> toEntityList(List<D> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * Convert set of entities to set of DTOs
     *
     * @param entities the source entity set
     * @return the mapped DTO set
     */
    default Set<D> toDtoSet(Set<E> entities) {
        if (entities == null) {
            return Set.of();
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toSet());
    }

    /**
     * Convert set of DTOs to set of entities
     *
     * @param dtos the source DTO set
     * @return the mapped entity set
     */
    default Set<E> toEntitySet(Set<D> dtos) {
        if (dtos == null) {
            return Set.of();
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toSet());
    }

    /**
     * Convert Spring Page to PagedResponse
     *
     * @param page the Spring Page object
     * @return PagedResponse containing mapped DTOs
     */
    default PagedResponse<D> toPagedResponse(Page<E> page) {
        if (page == null) {
            return PagedResponse.<D>builder()
                    .content(List.of())
                    .pageNumber(0)
                    .pageSize(0)
                    .totalElements(0)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .empty(true)
                    .build();
        }

        List<D> content = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return PagedResponse.<D>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    /**
     * Update entity from DTO (partial update, null values ignored)
     *
     * @param dto    the source DTO with updates
     * @param entity the target entity to update
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(D dto, @MappingTarget E entity);
}
