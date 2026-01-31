package lk.iit.nextora.module.election.mapper;

import lk.iit.nextora.common.mapper.MapperConfiguration;
import lk.iit.nextora.module.election.dto.request.CreateElectionRequest;
import lk.iit.nextora.module.election.dto.request.NominateCandidateRequest;
import lk.iit.nextora.module.election.dto.request.UpdateElectionRequest;
import lk.iit.nextora.module.election.dto.response.*;
import lk.iit.nextora.module.election.entity.*;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Voting module entities and DTOs
 * Note: Club and ClubMembership mappings are in ClubMapper (club module)
 */
@Mapper(config = MapperConfiguration.class)
public interface VotingMapper {


    // ==================== Election Mappings ====================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "club", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "resultsPublishedAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "candidates", ignore = true)
    @Mapping(target = "votes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Election toEntity(CreateElectionRequest request);

    @Mapping(target = "clubId", source = "club.id")
    @Mapping(target = "clubName", source = "club.name")
    @Mapping(target = "clubCode", source = "club.clubCode")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByName", expression = "java(election.getCreatedBy().getFullName())")
    @Mapping(target = "totalCandidates", expression = "java((int) election.getCandidates().size())")
    @Mapping(target = "approvedCandidates", expression = "java((int) election.getApprovedCandidatesCount())")
    @Mapping(target = "totalVotes", expression = "java(election.getTotalVotes())")
    @Mapping(target = "eligibleVoters", ignore = true)
    @Mapping(target = "participationRate", ignore = true)
    @Mapping(target = "canNominate", expression = "java(election.isNominationOpen())")
    @Mapping(target = "canVote", expression = "java(election.isVotingOpen())")
    @Mapping(target = "canViewResults", expression = "java(election.canViewResults())")
    @Mapping(target = "candidates", ignore = true)
    ElectionResponse toResponse(Election election);

    List<ElectionResponse> toElectionResponseList(List<Election> elections);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "club", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "resultsPublishedAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "candidates", ignore = true)
    @Mapping(target = "votes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateElectionFromRequest(UpdateElectionRequest request, @MappingTarget Election election);

    // ==================== Candidate Mappings ====================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "election", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "nominatedAt", ignore = true)
    @Mapping(target = "nominatedBy", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "voteCount", ignore = true)
    @Mapping(target = "displayOrder", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Candidate toEntity(NominateCandidateRequest request);

    @Mapping(target = "electionId", source = "election.id")
    @Mapping(target = "electionTitle", source = "election.title")
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentName", expression = "java(candidate.getStudent().getFullName())")
    @Mapping(target = "studentEmail", source = "student.email")
    @Mapping(target = "studentBatch", source = "student.batch")
    @Mapping(target = "studentProgram", source = "student.program")
    @Mapping(target = "nominatedById", source = "nominatedBy.id")
    @Mapping(target = "nominatedByName", expression = "java(candidate.getNominatedBy() != null ? candidate.getNominatedBy().getFullName() : null)")
    @Mapping(target = "reviewedById", source = "reviewedBy.id")
    @Mapping(target = "reviewedByName", expression = "java(candidate.getReviewedBy() != null ? candidate.getReviewedBy().getFullName() : null)")
    @Mapping(target = "votePercentage", ignore = true)
    @Mapping(target = "rank", ignore = true)
    @Mapping(target = "isWinner", ignore = true)
    CandidateResponse toResponse(Candidate candidate);

    List<CandidateResponse> toCandidateResponseList(List<Candidate> candidates);

    // ==================== Vote Mappings ====================

    @Mapping(target = "electionId", source = "election.id")
    @Mapping(target = "electionTitle", source = "election.title")
    @Mapping(target = "candidateId", source = "candidate.id")
    @Mapping(target = "candidateName", expression = "java(vote.getCandidate().getCandidateName())")
    @Mapping(target = "message", constant = "Your vote has been recorded successfully")
    VoteResponse toResponse(Vote vote);
}
