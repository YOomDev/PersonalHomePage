package com.ypeoomes.Controllers;

import com.ypeoomes.DTO.CalendarShareDto;
import com.ypeoomes.DTO.CalendarShareRequest;
import com.ypeoomes.Entities.User;
import com.ypeoomes.Services.CalendarShareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events/calendar/sharing")
@RequiredArgsConstructor
public class CalendarShareController {
    private final CalendarShareService calendarShareService;

    @GetMapping
    public ResponseEntity<List<CalendarShareDto>> getRules(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(calendarShareService.listRules(user));
    }

    @PostMapping
    public ResponseEntity<CalendarShareDto> upsertRule(@AuthenticationPrincipal User user, @Valid @RequestBody CalendarShareRequest request) {
        return ResponseEntity.ok(calendarShareService.upsertRule(user, request));
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteRule(@AuthenticationPrincipal User user, @PathVariable String username) {
        calendarShareService.deleteRule(user, username);
        return ResponseEntity.noContent().build();
    }
}
