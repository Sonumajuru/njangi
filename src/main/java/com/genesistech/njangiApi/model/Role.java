package com.genesistech.njangiApi.model;

import lombok.*;
import org.hibernate.Hibernate;
import com.genesistech.njangiApi.Enum.ERole;
import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder

@Table(name="`Roles`")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="`RoleId`")
    private Long roleId;

    @Enumerated(EnumType.STRING)
    @Column(name="`Name`", length = 20)
    private ERole name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Role role = (Role) o;
        return getRoleId() != null && Objects.equals(getRoleId(), role.getRoleId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
