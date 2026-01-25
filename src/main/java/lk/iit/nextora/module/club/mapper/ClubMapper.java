package lk.iit.nextora.module.club.mapper;

import lk.iit.nextora.common.mapper.MapperConfiguration;
import lk.iit.nextora.module.club.dto.request.CreateClubRequest;
import lk.iit.nextora.module.club.dto.response.*;
import lk.iit.nextora.module.club.entity.*;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Voting module entities and DTOs
 */
@Mapper(config = MapperConfiguration.class)
public interface ClubMapper {

    // ==================== Club Mappings ====================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "president", ignore = true)
    @Mapping(target = "advisor", ignore = true)
    @Mapping(target = "isRegistrationOpen", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Club toEntity(CreateClubRequest request);

    @Mapping(target = "presidentId", source = "president.id")
    @Mapping(target = "presidentName", expression = "java(club.getPresident() != null ? club.getPresident().getFullName() : null)")
    @Mapping(target = "presidentEmail", source = "president.email")
    @Mapping(target = "advisorId", source = "advisor.id")
    @Mapping(target = "advisorName", expression = "java(club.getAdvisor() != null ? club.getAdvisor().getFullName() : null)")
    @Mapping(target = "advisorEmail", source = "advisor.email")
    @Mapping(target = "advisorDepartment", source = "advisor.department")
    @Mapping(target = "totalMembers", ignore = true)
    @Mapping(target = "activeMembers", ignore = true)
    @Mapping(target = "totalElections", ignore = true)
    @Mapping(target = "activeElections", ignore = true)
    ClubResponse toResponse(Club club);

    List<ClubResponse> toClubResponseList(List<Club> clubs);

    // ==================== Club Membership Mappings ====================

    @Mapping(target = "clubId", source = "club.id")
    @Mapping(target = "clubCode", source = "club.clubCode")
    @Mapping(target = "clubName", source = "club.name")
    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "memberName", expression = "java(membership.getMember().getFullName())")
    @Mapping(target = "memberEmail", source = "member.email")
    @Mapping(target = "memberStudentId", source = "member.studentId")
    @Mapping(target = "memberBatch", source = "member.batch")
    @Mapping(target = "approvedById", source = "approvedBy.id")
    @Mapping(target = "approvedByName", expression = "java(membership.getApprovedBy() != null ? membership.getApprovedBy().getFullName() : null)")
    @Mapping(target = "canVote", expression = "java(membership.canVote())")
    @Mapping(target = "canNominate", expression = "java(membership.canNominate())")
    ClubMembershipResponse toResponse(ClubMembership membership);

    List<ClubMembershipResponse> toMembershipResponseList(List<ClubMembership> memberships);
}
