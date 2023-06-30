package com.genesistech.njangiapi.model;

import lombok.*;
import org.hibernate.Hibernate;
import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder

@Table(name="`RefreshToken`")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="`RefreshTokenId`")
    private Long refreshTokenId;

    @OneToOne
    @JoinColumn(name="`UserId`")
    private User user;

    @Column(name="`Token`", nullable = false, unique = true)
    private String token;

    @Column(name="`ExpiryDate`", nullable = false)
    private Instant expiryDate;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        RefreshToken that = (RefreshToken) o;
        return getRefreshTokenId() != null && Objects.equals(getRefreshTokenId(), that.getRefreshTokenId());
    }
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}