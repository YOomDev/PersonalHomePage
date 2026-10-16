package com.ypeoomes.Entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.Map;

@Entity
@Table(name = "user_modules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserModule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> config;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "display_order")
    private int displayOrder = 0;
}