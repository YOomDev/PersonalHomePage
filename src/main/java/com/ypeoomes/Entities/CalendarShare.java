package com.ypeoomes.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "calendar_shares", uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "grantee_username"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "grantee_username", nullable = false, length = 50)
    private String granteeUsername;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CalendarShareMode mode;

    @ElementCollection
    @CollectionTable(name = "calendar_share_visibilities", joinColumns = @JoinColumn(name = "share_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", length = 20)
    @Builder.Default
    private Set<CalendarVisibility> allowedVisibilities = new HashSet<>();
}
