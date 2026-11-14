package br.com.jessbijus.api.model;

import java.time.Instant;
import java.util.UUID;

import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor()
@Entity
@Table(name = "admin_profiles")
public class AdminProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "auth_subject", unique = true, length = 255)
    private String authSubject;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(length = 80)
    private String nickname;

    @Column(name = "photo_url", columnDefinition = "text")
    private String photoUrl;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
