package lk.iit.nextora.module.intranet.entity;

import jakarta.persistence.*;
import lk.iit.nextora.common.entity.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a module within a programme.
 */
@Entity
@Table(name = "intranet_program_modules")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramModule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Column(name = "year")
    private Integer year;

    @Column(name = "semester")
    private Integer semester;

    @Column(name = "module_code", nullable = false, length = 30)
    private String moduleCode;

    @Column(name = "module_name", nullable = false, length = 300)
    private String moduleName;

    @Column(name = "credits")
    private Integer credits;

    @Column(name = "is_core")
    @Builder.Default
    private Boolean isCore = true;
}

