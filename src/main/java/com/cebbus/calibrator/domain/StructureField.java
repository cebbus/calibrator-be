package com.cebbus.calibrator.domain;

import com.cebbus.calibrator.domain.enums.DataType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"fieldName", "structureId"}),
        @UniqueConstraint(columnNames = {"columnName", "structureId"})})
public class StructureField extends Base {

    @Column
    private String fieldName;

    @Column
    private String columnName;

    @Enumerated(EnumType.STRING)
    private DataType type;

    @Column
    private boolean classifier;

    @Column
    private boolean differentiator;

    @ManyToOne
    @JoinColumn(name = "structureId")
    @JsonBackReference
    private Structure structure;

}
