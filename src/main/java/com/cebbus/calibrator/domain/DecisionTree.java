package com.cebbus.calibrator.domain;

import com.cebbus.calibrator.domain.enums.MethodType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;

@Data
@ToString(exclude = {"root"})
@EqualsAndHashCode(exclude = {"root"}, callSuper = true)
@Entity
public class DecisionTree extends Base {

    @Column
    @Enumerated(EnumType.STRING)
    private MethodType method;

    @ManyToOne
    @JoinColumn(name = "structureId")
    private Structure structure;

    @OneToMany(mappedBy = "decisionTree", cascade = CascadeType.ALL, orphanRemoval = true)
    @Where(clause = "parent_id is null")
    @BatchSize(size = 100)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @JsonManagedReference
    private List<DecisionTreeItem> root;

    public DecisionTreeItem getRoot() {
        return root == null || root.isEmpty() ? null : root.get(0);
    }

    public void setRoot(DecisionTreeItem root) {
        this.root = Collections.singletonList(root);
    }
}
