package com.ypeoomes.Services;

import com.ypeoomes.DTO.CalendarEventDto;
import com.ypeoomes.DTO.RecurringScheduleDto;
import com.ypeoomes.DTO.ScheduleShiftDto;
import com.ypeoomes.DTO.ShiftTemplateDto;
import com.ypeoomes.Entities.*;
import com.ypeoomes.Repositories.CalendarShareRepository;
import com.ypeoomes.Repositories.RecurringScheduleRepository;
import com.ypeoomes.Repositories.ScheduleShiftRepository;
import com.ypeoomes.Repositories.ShiftTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftScheduleService {
    private final ShiftTemplateRepository templateRepository;
    private final RecurringScheduleRepository scheduleRepository;
    private final ScheduleShiftRepository shiftRepository;
    private final CalendarShareRepository shareRepository;

    // ---------- Templates ----------

    @Transactional(readOnly = true)
    public List<ShiftTemplateDto> listTemplates(User user) {
        return templateRepository.findByUser(user).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ShiftTemplateDto createTemplate(User user, ShiftTemplateDto dto) {
        ShiftTemplate template = ShiftTemplate.builder()
                .user(user)
                .allDay(dto.isAllDay())
                .build();
        applyTemplateDto(template, dto);
        return toDto(templateRepository.save(template));
    }

    @Transactional
    public ShiftTemplateDto updateTemplate(User user, Long id, ShiftTemplateDto dto) {
        ShiftTemplate template = templateRepository.findById(id)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Shift template not found"));
        applyTemplateDto(template, dto);
        return toDto(templateRepository.save(template));
    }

    @Transactional
    public void deleteTemplate(User user, Long id) {
        ShiftTemplate template = templateRepository.findById(id)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Shift template not found"));
        if (shiftRepository.existsByTemplateId(id)) {
            throw new IllegalArgumentException("Template is used by a repeating schedule; remove it from the schedule first");
        }
        templateRepository.delete(template);
    }

    private void applyTemplateDto(ShiftTemplate template, ShiftTemplateDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Template name is required");
        }
        template.setName(dto.getName().trim());
        template.setAllDay(dto.isAllDay());
        template.setColor(colorOrNull(dto.getColor()));
        LocalTime dayStart = java.time.LocalTime.of(0, 0);
        LocalTime dayEnd = java.time.LocalTime.of(23, 59);
        if (dto.isAllDay()) {
            template.setStartTime(dayStart);
            template.setEndTime(dayEnd);
        } else {
            LocalTime start = dto.getStartTime() != null ? dto.getStartTime() : dayStart;
            LocalTime end = dto.getEndTime() != null ? dto.getEndTime() : start.plusHours(8);
            if (start.equals(end)) {
                throw new IllegalArgumentException("Start time and end time cannot be identical");
            }
            template.setStartTime(start);
            template.setEndTime(end);
        }
    }

    private ShiftTemplateDto toDto(ShiftTemplate template) {
        return ShiftTemplateDto.builder()
                .id(template.getId())
                .name(template.getName())
                .startTime(template.getStartTime())
                .endTime(template.getEndTime())
                .allDay(template.isAllDay())
                .color(template.getColor())
                .build();
    }

    // ---------- Schedules ----------

    @Transactional(readOnly = true)
    public List<RecurringScheduleDto> listSchedules(User user) {
        return scheduleRepository.findByUser(user).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public RecurringScheduleDto createSchedule(User user, RecurringScheduleDto dto) {
        RecurringSchedule schedule = RecurringSchedule.builder()
                .user(user)
                .excludedDates(new HashSet<>())
                .build();
        applyScheduleDto(schedule, dto, user);
        RecurringSchedule saved = scheduleRepository.save(schedule); // generate id first
        shiftRepository.saveAll(saved.getShifts()); // shifts reference the persisted schedule
        return toDto(saved);
    }

    @Transactional
    public RecurringScheduleDto updateSchedule(User user, Long id, RecurringScheduleDto dto) {
        RecurringSchedule schedule = scheduleRepository.findById(id)
                .filter(s -> s.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        applyScheduleDto(schedule, dto, user);
        shiftRepository.saveAll(schedule.getShifts());
        return toDto(scheduleRepository.save(schedule));
    }

    @Transactional
    public void deleteSchedule(User user, Long id) {
        RecurringSchedule schedule = scheduleRepository.findById(id)
                .filter(s -> s.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        scheduleRepository.delete(schedule);
    }

    /** "Deletes" a single virtual occurrence by excluding its date. */
    @Transactional
    public void excludeOccurrence(User user, Long id, LocalDate date) {
        RecurringSchedule schedule = scheduleRepository.findById(id)
                .filter(s -> s.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
        schedule.getExcludedDates().add(date);
        scheduleRepository.save(schedule);
    }

    private void applyScheduleDto(RecurringSchedule schedule, RecurringScheduleDto dto, User user) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Schedule name is required");
        }
        if (dto.getType() == null) {
            throw new IllegalArgumentException("Schedule type is required (WEEKLY or ROTATION)");
        }

        List<ScheduleShiftDto> shiftsIn = dto.getShifts() != null ? dto.getShifts() : List.of();
        if (shiftsIn.isEmpty() || shiftsIn.stream().allMatch(s -> s.getTemplateId() == null)) {
            throw new IllegalArgumentException("Assign at least one shift template to the schedule");
        }

        Map<Long, ShiftTemplate> ownedTemplates = templateRepository.findByUser(user).stream()
                .collect(Collectors.toMap(ShiftTemplate::getId, Function.identity()));

        int maxPosition;
        if (dto.getType() == ScheduleType.WEEKLY) {
            maxPosition = 6; // Mon=0 .. Sun=6
        } else if (dto.getType() == ScheduleType.ROTATION) {
            Integer cycle = dto.getCycleLength();
            if (cycle == null || cycle < 1 || cycle > 366) {
                throw new IllegalArgumentException("Rotation cycle length must be between 1 and 366 days");
            }
            if (dto.getStartDate() == null) {
                throw new IllegalArgumentException("Rotation schedules need a start date to anchor the cycle");
            }
            maxPosition = cycle - 1;
        } else {
            throw new IllegalArgumentException("Unknown schedule type");
        }

        for (ScheduleShiftDto shift : shiftsIn) {
            if (shift.getTemplateId() != null && !ownedTemplates.containsKey(shift.getTemplateId())) {
                throw new IllegalArgumentException("Shift template not found (or belongs to another user)");
            }
        }

        boolean hasAssignment = shiftsIn.stream()
                .anyMatch(s -> s.getTemplateId() != null
                        && s.getDayPosition() >= 0
                        && s.getDayPosition() <= maxPosition);
        if (!hasAssignment) {
            throw new IllegalArgumentException("Assign at least one shift template to a valid day of the schedule");
        }

        // Dedupe: keep the last assignment per position
        Map<Integer, ScheduleShiftDto> byPosition = new java.util.LinkedHashMap<>();
        for (ScheduleShiftDto shift : shiftsIn) {
            if (shift.getTemplateId() != null && shift.getDayPosition() >= 0 && shift.getDayPosition() <= maxPosition) {
                byPosition.put(shift.getDayPosition(), shift);
            }
        }

        schedule.setName(dto.getName().trim());
        schedule.setType(dto.getType());
        schedule.setVisibility(dto.getVisibility() != null ? dto.getVisibility() : CalendarVisibility.PRIVATE);
        schedule.setStartDate(dto.getStartDate());
        schedule.setEndDate(dto.getEndDate());
        schedule.setCycleLength(dto.getType() == ScheduleType.ROTATION ? dto.getCycleLength() : null);

        schedule.getShifts().clear();
        for (ScheduleShiftDto shift : byPosition.values()) {
            ScheduleShift entity = ScheduleShift.builder()
                    .schedule(schedule)
                    .dayPosition(shift.getDayPosition())
                    .template(ownedTemplates.get(shift.getTemplateId()))
                    .build();
            schedule.getShifts().add(entity);
        }
    }

    private RecurringScheduleDto toDto(RecurringSchedule schedule) {
        return RecurringScheduleDto.builder()
                .id(schedule.getId())
                .name(schedule.getName())
                .type(schedule.getType())
                .visibility(schedule.getVisibility())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getCycleLength() != null || schedule.getStartDate() != null ? schedule.getEndDate() : schedule.getEndDate())
                .cycleLength(schedule.getCycleLength())
                .shifts(schedule.getShifts().stream()
                        .sorted(Comparator.comparingInt(ScheduleShift::getDayPosition))
                        .map(shift -> ScheduleShiftDto.builder()
                                .dayPosition(shift.getDayPosition())
                                .templateId(shift.getTemplate().getId())
                                .template(toDto(shift.getTemplate()))
                                .build())
                        .toList())
                .build();
    }

    // ---------- Expansion into virtual occurrences ----------

    /**
     * Expands all visible schedules (the viewer's own plus any they have access to
     * through calendar shares) into virtual occurrences within [from, to].
     */
    @Transactional(readOnly = true)
    public List<CalendarEventDto> getOccurrences(User viewer, LocalDate from, LocalDate to) {
        List<CalendarEventDto> result = new ArrayList<>();

        // Own schedules: always in full detail
        for (RecurringSchedule schedule : scheduleRepository.findByUser(viewer)) {
            emitOccurrences(schedule, viewer.getUsername(), true, from, to, result);
        }

        // Shared schedules, mirroring event sharing rules
        for (CalendarShare rule : shareRepository.findByGranteeUsername(viewer.getUsername())) {
            boolean fullAccess = rule.getMode() == CalendarShareMode.FULL;
            for (RecurringSchedule schedule : scheduleRepository.findByUser(rule.getOwner())) {
                CalendarVisibility visibility = schedule.getVisibility() != null
                        ? schedule.getVisibility()
                        : CalendarVisibility.PRIVATE;
                boolean detailAllowed = fullAccess || rule.getAllowedVisibilities().contains(visibility);
                emitOccurrences(schedule, rule.getOwner().getUsername(), detailAllowed, from, to, result);
            }
        }

        result.sort(Comparator.comparing(CalendarEventDto::getStartAt));
        return result;
    }

    private void emitOccurrences(RecurringSchedule schedule, String ownerUsername, boolean detailAllowed,
                                 LocalDate from, LocalDate to, List<CalendarEventDto> result) {
        Map<Integer, ShiftTemplate> byPosition = schedule.getShifts().stream()
                .collect(Collectors.toMap(ScheduleShift::getDayPosition, ScheduleShift::getTemplate));

        LocalDate scheduleEnd = schedule.getEndDate();
        LocalDate rangeEnd = scheduleEnd != null && scheduleEnd.isBefore(to) ? scheduleEnd : to;
        LocalDate rangeStart = schedule.getStartDate() != null && schedule.getStartDate().isAfter(from)
                ? schedule.getStartDate() : from;

        for (LocalDate date = rangeStart; !date.isAfter(rangeEnd); date = date.plusDays(1)) {
            Integer position = positionForDate(schedule, date);
            if (position == null) {
                continue;
            }
            ShiftTemplate template = byPosition.get(position);
            if (template == null || schedule.getExcludedDates().contains(date)) {
                continue;
            }
            result.add(toOccurrenceDto(schedule, template, date, ownerUsername, detailAllowed));
        }
    }

    private Integer positionForDate(RecurringSchedule schedule, LocalDate date) {
        if (schedule.getType() == ScheduleType.WEEKLY) {
            return date.getDayOfWeek().getValue() - 1; // Monday=0 .. Sunday=6
        }
        if (schedule.getType() == ScheduleType.ROTATION && schedule.getStartDate() != null && schedule.getCycleLength() != null) {
            long days = ChronoUnit.DAYS.between(schedule.getStartDate(), date);
            return Math.floorMod(days, schedule.getCycleLength());
        }
        return null;
    }

    private CalendarEventDto toOccurrenceDto(RecurringSchedule schedule, ShiftTemplate template, LocalDate date,
                                             String ownerUsername, boolean detailAllowed) {
        LocalDateTime startAt;
        LocalDateTime endAt;
        boolean allDay;

        if (template.isAllDay()) {
            allDay = true;
            startAt = date.atStartOfDay();
            endAt = date.atTime(23, 59);
        } else {
            allDay = false;
            startAt = date.atTime(template.getStartTime());
            endAt = date.atTime(template.getEndTime());
            if (endAt.isBefore(startAt)) {
                // Overnight shift (e.g. 22:00 -> 06:00): ends the next day
                endAt = date.plusDays(1).atTime(template.getEndTime());
            }
        }

        CalendarEventDto.CalendarEventDtoBuilder builder = CalendarEventDto.builder()
                .ownerUsername(ownerUsername)
                .masked(!detailAllowed)
                .startAt(startAt)
                .endAt(endAt)
                .allDay(allDay)
                .color(template.getColor())
                .virtual(true)
                .scheduleId(schedule.getId())
                .occurrenceDate(date.toString());

        if (detailAllowed) {
            builder.title(template.getName());
        }

        builder.visibility(schedule.getVisibility() != null ? schedule.getVisibility() : CalendarVisibility.PRIVATE);
        return builder.build();
    }

    /**
     * Parses an ISO yyyy-MM-dd range from request params, falling back to a wide default
     * horizon so clients that pass no params still get a year of occurrences in both directions.
     */
    public static LocalDate[] parseRange(String fromParam, String toParam, LocalDate today) {
        try {
            LocalDate from = fromParam != null && !fromParam.isBlank() ? LocalDate.parse(fromParam) : today.minusDays(366);
            LocalDate to = toParam != null && !toParam.isBlank() ? LocalDate.parse(toParam) : today.plusDays(366);
            if (from.isAfter(to)) {
                from = to;
            }
            return new LocalDate[]{from, to};
        } catch (DateTimeParseException e) {
            return new LocalDate[]{today.minusDays(366), today.plusDays(366)};
        }
    }

    private String colorOrNull(String color) {
        if (color == null || color.isBlank()) {
            return null;
        }
        return color.matches("^#[0-9a-fA-F]{3,8}$") ? color.trim() : null;
    }
}
