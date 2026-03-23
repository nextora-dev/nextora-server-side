package lk.iit.nextora.common.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Common MapStruct configuration for all mappers.
 * Use with @Mapper(config = MapperConfiguration.class)
 */
@MapperConfig(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface MapperConfiguration {
}

