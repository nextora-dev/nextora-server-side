package lk.iit.nextora.module.club.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lk.iit.nextora.common.enums.FacultyType;
import lk.iit.nextora.module.auth.entity.AcademicStaff;
import lk.iit.nextora.module.auth.entity.Student;
import lk.iit.nextora.module.election.entity.Election;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a club in the system.
 * Clubs can organize elections for their members.
 */
@Entity
@Table(name = "clubs", indexes = {
        @Index(name = "idx_club_code", columnList = "clubCode"),
        @Index(name = "idx_club_name", columnList = "name"),
        @Index(name = "idx_club_faculty", columnList = "faculty")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Club extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String clubCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private FacultyType faculty;

    @Column(length = 200)
    private String email;

    @Column(length = 15)
    private String contactNumber;

    @Column
    private LocalDate establishedDate;

    @Column(length = 500)
    private String socialMediaLinks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "president_id")
    private Student president;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advisor_id")
    private AcademicStaff advisor;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxMembers = 500;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRegistrationOpen = true;

    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Election> elections = new HashSet<>();

    /**
     * Check if club can accept new members
     */
    public boolean canAcceptMembers() {
        return isRegistrationOpen && getIsActive();
    }

    /**
     * Check if club has any active elections (voting open)
     */
    public boolean hasActiveElection() {
        return elections != null && elections.stream()
                .anyMatch(e -> e.getStatus() == lk.iit.nextora.common.enums.ElectionStatus.VOTING_OPEN);
    }
}
