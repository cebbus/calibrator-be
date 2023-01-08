package com.cebbus.calibrator.domain;

import com.cebbus.calibrator.domain.enums.MethodType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Entity
public class MethodCompare extends Base {

    @Column
    private LocalDateTime trainingStart;

    @Column
    private LocalDateTime trainingEnd;

    @Column
    private Integer trainingSize;

    @Column
    private LocalDateTime testStart;

    @Column
    private LocalDateTime testEnd;

    @Column
    private Integer testSize;

    @Column
    private Integer unclassifiedDataSize;

    @Column
    private Integer wrongClassifiedDataSize;

    @Column
    @Enumerated(EnumType.STRING)
    private MethodType method;

    @Column
    private Double nodeWalk;

    @ManyToOne
    @JoinColumn(name = "structureId")
    private Structure structure;

}
