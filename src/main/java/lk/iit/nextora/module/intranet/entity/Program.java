package lk.iit.nextora.module.intranet.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an undergraduate or postgraduate programme.
 */
@Entity
@Table(name = "intranet_programs", uniqueConstraints = {
        @UniqueConstraint(columnNames = "program_slug")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Program extends BaseEntity {

    @Column(name = "program_code", nullable = false, length = 50)
    private String programCode;

    @Column(name = "program_name", nullable = false, length = 300)
    private String programName;

    @Column(name = "program_slug", nullable = false, unique = true, length = 200)
    private String programSlug;

    @Column(name = "awarding_university", length = 300)
    private String awardingUniversity;

    @Column(name = "duration", length = 50)
    private String duration;

    @Column(name = "total_credits")
    private Integer totalCredits;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "entry_requirements", columnDefinition = "TEXT")
    private String entryRequirements;

    /**
     * UNDERGRADUATE or POSTGRADUATE
     */
    @Column(name = "program_level", nullable = false, length = 30)
    private String programLevel;

    @ElementCollection
    @CollectionTable(name = "intranet_program_career_prospects", joinColumns = @JoinColumn(name = "program_id"))
    @Column(name = "career_prospect", length = 300)
    @Builder.Default
    private List<String> careerProspects = new ArrayList<>();

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("year ASC, semester ASC")
    @Builder.Default
    private List<ProgramModule> modules = new ArrayList<>();

    @Column(name = "program_specification_url", length = 500)
    private String programSpecificationUrl;

    @Column(name = "handbook_url", length = 500)
    private String handbookUrl;

    // Helper
    public void addModule(ProgramModule module) {
        modules.add(module);
        module.setProgram(this);
    }
}

