package lk.iit.nextora.module.club.mapper;

import lk.iit.nextora.common.mapper.MapperConfiguration;
import lk.iit.nextora.module.club.dto.request.CreateClubRequest;
import lk.iit.nextora.module.club.dto.response.*;
import lk.iit.nextora.module.club.entity.*;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Club module entities and DTOs
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

    @Mapping(target = "president", ignore = true)
    @Mapping(target = "advisor", ignore = true)
    @Mapping(target = "totalMembers", ignore = true)
    @Mapping(target = "activeMembers", ignore = true)
    @Mapping(target = "totalElections", ignore = true)
    @Mapping(target = "activeElections", ignore = true)
    @Mapping(target = "vicePresident", ignore = true)
    @Mapping(target = "secretary", ignore = true)
    @Mapping(target = "treasurer", ignore = true)
    ClubResponse toResponse(Club club);

    List<ClubResponse> toClubResponseList(List<Club> clubs);

    // ==================== Club Membership Mappings ====================

    @Mapping(target = "clubId", source = "club.id")
    @Mapping(target = "clubCode", source = "club.clubCode")
    @Mapping(target = "clubName", source = "club.name")
    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "memberName", expression = "java(membership.getMember().getFullName())")
    @Mapping(target = "memberEmail", source = "member.email")
    @Mapping(target = "memberProfilePictureUrl", source = "member.profilePictureUrl")
    @Mapping(target = "memberStudentId", source = "member.studentId")
    @Mapping(target = "memberBatch", source = "member.batch")
    @Mapping(target = "approvedById", source = "approvedBy.id")
    @Mapping(target = "approvedByName", expression = "java(membership.getApprovedBy() != null ? membership.getApprovedBy().getFullName() : null)")
    @Mapping(target = "canVote", expression = "java(membership.canVote())")
    @Mapping(target = "canNominate", expression = "java(membership.canNominate())")
    ClubMembershipResponse toResponse(ClubMembership membership);

    List<ClubMembershipResponse> toMembershipResponseList(List<ClubMembership> memberships);


    // ==================== Club Announcement Mappings ====================

    @Mapping(target = "clubId", source = "club.id")
    @Mapping(target = "clubCode", source = "club.clubCode")
    @Mapping(target = "clubName", source = "club.name")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorName", expression = "java(announcement.getAuthor() != null ? announcement.getAuthor().getFullName() : null)")
    @Mapping(target = "authorEmail", source = "author.email")
    ClubAnnouncementResponse toResponse(ClubAnnouncement announcement);

    List<ClubAnnouncementResponse> toAnnouncementResponseList(List<ClubAnnouncement> announcements);

    // ==================== Club Activity Log Mappings ====================

    @Mapping(target = "clubId", source = "club.id")
    @Mapping(target = "clubName", source = "club.name")
    ClubActivityLogResponse toResponse(ClubActivityLog activityLog);

    List<ClubActivityLogResponse> toActivityLogResponseList(List<ClubActivityLog> activityLogs);
}
