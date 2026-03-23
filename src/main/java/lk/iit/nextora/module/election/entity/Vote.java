package lk.iit.nextora.module.election.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lk.iit.nextora.module.auth.entity.Student;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Entity representing a vote cast by a member in an election.
 * Implements secure voting with hash-based verification for anonymous voting.
 * Each member can vote only once per election.
 */
@Entity
@Table(name = "votes",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_election_voter", columnNames = {"election_id", "voter_id"}),
           @UniqueConstraint(name = "uk_election_vote_hash", columnNames = {"election_id", "vote_hash"})
       },
       indexes = {
           @Index(name = "idx_vote_election", columnList = "election_id"),
           @Index(name = "idx_vote_candidate", columnList = "candidate_id"),
           @Index(name = "idx_vote_voter", columnList = "voter_id"),
           @Index(name = "idx_vote_hash", columnList = "vote_hash")
       })
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Vote extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id", nullable = false)
    private Election election;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    /**
     * Voter reference - stored only if election is NOT anonymous
     * For anonymous elections, this field is null after vote verification
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id")
    private Student voter;

    /**
     * Hash of voter identity - used to prevent duplicate votes in anonymous elections
     * Hash = SHA-256(electionId + voterId + secret)
     */
    @Column(nullable = false, length = 64)
    private String voteHash;

    @Column(nullable = false)
    private LocalDateTime votedAt;

    /**
     * IP address of voter - for audit purposes
     */
    @Column(length = 45)
    private String voterIpAddress;

    /**
     * User agent - for audit purposes
     */
    @Column(length = 500)
    private String userAgent;

    /**
     * Verification token - provided to voter as receipt
     */
    @Column(unique = true, length = 64)
    private String verificationToken;

    /**
     * Whether vote has been verified by voter
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    // ==================== Lifecycle hook ====================

    /**
     * Set votedAt timestamp when vote is created
     * Called by service layer before saving
     */
    public void initializeVoteTimestamp() {
        if (votedAt == null) {
            votedAt = LocalDateTime.now();
        }
    }

    // ==================== Business Logic Methods ====================

    /**
     * Generate vote hash for anonymous voting
     */
    public static String generateVoteHash(Long electionId, Long voterId, String secret) {
        String data = electionId + ":" + voterId + ":" + secret;
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Generate verification token for voter receipt
     */
    public static String generateVerificationToken() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Anonymize vote (remove voter reference for anonymous elections)
     */
    public void anonymize() {
        this.voter = null;
    }
}
