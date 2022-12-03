package com.cebbus.calibrator.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.Set;

@Data
@ToString(callSuper = true, of = "name")
@EqualsAndHashCode(callSuper = true, of = "name")
@Entity
@Table(name = "account")
public class User extends Base {
    private String name;
    private String surname;
    private String username;
    private String email;
    private Boolean enabled;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    private Set<Role> roles;
}
