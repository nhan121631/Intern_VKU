package com.vku.job.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class UserProfile extends BaseEntity {
    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "address", length = 500)
    private String address;

    @OneToOne(mappedBy = "profile", fetch = FetchType.LAZY)
    private User user;
}
