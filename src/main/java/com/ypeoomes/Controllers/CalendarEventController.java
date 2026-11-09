package com.ypeoomes.Controllers;

import com.ypeoomes.DTO.CalendarEventDto;
import com.ypeoomes.Entities.User;
import com.ypeoomes.Services.CalendarEventService;
import com.ypeoomes.Services.CalendarShareService;
import com.ypeoomes.Services.ShiftScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/events/calendar")
@RequiredArgsConstructor
public class CalendarEventController {
    private final CalendarEventService calendarEventService;
    private final CalendarShareService calendarShareService;
    private final ShiftScheduleService shiftScheduleService;

    @GetMapping
    public ResponseEntity<List<CalendarEventDto>> getEvents(@AuthenticationPrincipal User user,
                                                            @RequestParam(required = false) String from,
                                                            @RequestParam(required = false) String to) {
        LocalDate today = LocalDate.now();
        LocalDate[] range = ShiftScheduleService.parseRange(from, to, today);

        List<CalendarEventDto> all = new ArrayList<>(calendarEventService.getUserEvents(user));
        all.addAll(calendarShareService.getSharedEvents(user));
        all.addAll(shiftScheduleService.getOccurrences(user, range[0], range[1]));
        all.sort(Comparator.comparing(CalendarEventDto::getStartAt));
        return ResponseEntity.ok(all);
    }

    @PostMapping
    public ResponseEntity<CalendarEventDto> createEvent(@AuthenticationPrincipal User user, @Valid @RequestBody CalendarEventDto dto) {
        return ResponseEntity.ok(calendarEventService.createEvent(user, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CalendarEventDto> updateEvent(@AuthenticationPrincipal User user, @PathVariable Long id, @Valid @RequestBody CalendarEventDto dto) {
        return ResponseEntity.ok(calendarEventService.updateEvent(user, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@AuthenticationPrincipal User user, @PathVariable Long id) {
        calendarEventService.deleteEvent(user, id);
        return ResponseEntity.noContent().build();
    }
}