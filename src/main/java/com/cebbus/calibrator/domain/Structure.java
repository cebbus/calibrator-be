package com.cebbus.calibrator.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.Set;

@Data
@ToString(exclude = {"fields"})
@EqualsAndHashCode(exclude = {"fields"}, callSuper = true)
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = "className"),
        @UniqueConstraint(columnNames = "tableName")})
public class Structure extends Base {

    @Column
    private String className;

    @Column
    private String tableName;

    @Column
    private Boolean created;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "structure", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    @JsonManagedReference
    private Set<StructureField> fields;

}
