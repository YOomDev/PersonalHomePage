package com.ypeoomes.Entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "schedule_shifts", uniqueConstraints = @UniqueConstraint(columnNames = {"schedule_id", "day_position"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleShift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private RecurringSchedule schedule;

    /** WEEKLY: ISO weekday minus 1 (Mon=0 .. Sun=6). ROTATION: day index within the cycle (0 .. cycleLength-1). */
    @Column(name = "day_position", nullable = false)
    private int dayPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ShiftTemplate template;
}
