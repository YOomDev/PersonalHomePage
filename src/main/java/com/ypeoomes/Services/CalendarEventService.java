package com.ypeoomes.Services;

import com.ypeoomes.DTO.CalendarEventDto;
import com.ypeoomes.Entities.CalendarEvent;
import com.ypeoomes.Entities.CalendarVisibility;
import com.ypeoomes.Entities.User;
import com.ypeoomes.Repositories.CalendarEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarEventService {
    private final CalendarEventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<CalendarEventDto> getUserEvents(User user) {
        return eventRepository.findByUserOrderByStartAtAsc(user).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public CalendarEventDto createEvent(User user, CalendarEventDto dto) {
        CalendarEvent event = CalendarEvent.builder()
                .user(user)
                .build();
        applyDto(event, dto);
        return toDto(eventRepository.save(event));
    }

    @Transactional
    public CalendarEventDto updateEvent(User user, Long id, CalendarEventDto dto) {
        CalendarEvent event = eventRepository.findById(id)
                .filter(e -> e.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        applyDto(event, dto);
        return toDto(eventRepository.save(event));
    }

    @Transactional
    public void deleteEvent(User user, Long id) {
        CalendarEvent event = eventRepository.findById(id)
                .filter(e -> e.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        eventRepository.delete(event);
    }

    private void applyDto(CalendarEvent event, CalendarEventDto dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (dto.getStartAt() == null) {
            throw new IllegalArgumentException("Start time is required");
        }

        LocalDateTime start = dto.getStartAt();
        LocalDateTime end = dto.getEndAt() != null ? dto.getEndAt() : start;

        if (dto.isAllDay()) {
            start = start.toLocalDate().atStartOfDay();
            end = end.toLocalDate().atTime(23, 59);
        }

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }

        event.setTitle(dto.getTitle().trim());
        event.setDescription(dto.getDescription());
        event.setLocation(dto.getLocation());
        event.setStartAt(start);
        event.setEndAt(end);
        event.setAllDay(dto.isAllDay());
        if (dto.getVisibility() != null) {
            event.setVisibility(dto.getVisibility());
        } else if (event.getVisibility() == null) {
            event.setVisibility(CalendarVisibility.PRIVATE);
        }
    }

    private CalendarEventDto toDto(CalendarEvent event) {
        return CalendarEventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .startAt(event.getStartAt())
                .endAt(event.getEndAt())
                .allDay(event.isAllDay())
                .visibility(event.getVisibility() != null ? event.getVisibility() : CalendarVisibility.PRIVATE)
                .username(event.getUser().getUsername())
                .build();
    }
}