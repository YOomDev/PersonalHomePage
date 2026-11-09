package com.ypeoomes.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "recurring_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScheduleType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CalendarVisibility visibility;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** Open-ended when null. */
    @Column(name = "end_date")
    private LocalDate endDate;

    /** Length of the rotation cycle in days (ROTATION only). Day position = floorMod(date - startDate, cycleLength). */
    @Column(name = "cycle_length")
    private Integer cycleLength;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<ScheduleShift> shifts = new java.util.ArrayList<>();

    /** Dates where an occurrence is hidden (a virtual occurrence was "deleted"). */
    @ElementCollection
    @CollectionTable(name = "recurring_schedule_exclusions", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "exclusion_date", nullable = false)
    @Builder.Default
    private Set<LocalDate> excludedDates = new HashSet<>();
}
