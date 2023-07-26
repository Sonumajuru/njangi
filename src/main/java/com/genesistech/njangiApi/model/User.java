package com.genesistech.njangiApi.model;

import com.genesistech.njangiApi.Enum.Subscription;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder

// User model Class

@Table(name="`User`")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="`UserId`")
    private Long id;

    @GeneratedValue(generator="`UserUuid`")
    @GenericGenerator(name="`UserUuid`", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name="`UserUuid`", unique = true, updatable = false)
    private String userUuid;

    @Column(name="`Title`")
    private boolean title;

    @Column(name="`Firstname`", nullable = false)
    private String firstname;

    @Column(name="`Lastname`", nullable = false)
    private String lastname;

    @Column(name="`Password`", nullable = false)
    private String password;

    @Column(name="`Email`", unique = true, nullable = false)
    private String email;

    @Column(name="`PhoneNumber`")
    private String phoneNumber;

    @Column(name="`Address`")
    private String address;

    @Column(name="`Country`")
    private String country;

    @Column(name="`DOB`", columnDefinition = "DATE")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Transient
    @Column(name="`Verified`")
    private boolean verify;

    @Column(name="`Subscription`")
    private Subscription subscription;
    @PrePersist
    public void autoFill(){
        this.setUserUuid(UUID.randomUUID().toString());
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}