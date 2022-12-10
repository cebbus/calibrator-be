package com.cebbus.calibrator.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.List;

@Data
@ToString(exclude = {"parent", "children"})
@EqualsAndHashCode(exclude = {"parent", "children"}, callSuper = true)
@Entity
public class DecisionTreeItem extends Base {

    @Column
    private String fieldName;

    @Column
    private String fieldValue;

    @Column
    private String classification;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    private List<DecisionTreeItem> children;

    @ManyToOne
    @JoinColumn(name = "parentId")
    @JsonIgnore
    private DecisionTreeItem parent;

    @ManyToOne
    @JoinColumn(name = "decisionTreeId")
    @JsonBackReference
    private DecisionTree decisionTree;

    private transient boolean leaf;

    private transient boolean expanded = true;

    public boolean isLeaf() {
        return this.children.isEmpty();
    }
}
