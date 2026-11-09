package com.ypeoomes.Controllers;

import com.ypeoomes.DTO.RecurringScheduleDto;
import com.ypeoomes.DTO.ShiftTemplateDto;
import com.ypeoomes.Entities.User;
import com.ypeoomes.Services.ShiftScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedules/shifts")
@RequiredArgsConstructor
public class ShiftScheduleController {
    private final ShiftScheduleService shiftScheduleService;

    // ---------- Templates ----------

    @GetMapping("/templates")
    public ResponseEntity<List<ShiftTemplateDto>> listTemplates(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(shiftScheduleService.listTemplates(user));
    }

    @PostMapping("/templates")
    public ResponseEntity<ShiftTemplateDto> createTemplate(@AuthenticationPrincipal User user, @RequestBody ShiftTemplateDto dto) {
        return ResponseEntity.ok(shiftScheduleService.createTemplate(user, dto));
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<ShiftTemplateDto> updateTemplate(@AuthenticationPrincipal User user, @PathVariable Long id, @RequestBody ShiftTemplateDto dto) {
        return ResponseEntity.ok(shiftScheduleService.updateTemplate(user, id, dto));
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Void> deleteTemplate(@AuthenticationPrincipal User user, @PathVariable Long id) {
        shiftScheduleService.deleteTemplate(user, id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Schedules ----------

    @GetMapping
    public ResponseEntity<List<RecurringScheduleDto>> listSchedules(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(shiftScheduleService.listSchedules(user));
    }

    @PostMapping
    public ResponseEntity<RecurringScheduleDto> createSchedule(@AuthenticationPrincipal User user, @RequestBody RecurringScheduleDto dto) {
        return ResponseEntity.ok(shiftScheduleService.createSchedule(user, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringScheduleDto> updateSchedule(@AuthenticationPrincipal User user, @PathVariable Long id, @RequestBody RecurringScheduleDto dto) {
        return ResponseEntity.ok(shiftScheduleService.updateSchedule(user, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@AuthenticationPrincipal User user, @PathVariable Long id) {
        shiftScheduleService.deleteSchedule(user, id);
        return ResponseEntity.noContent().build();
    }

    /** Hides a single virtual occurrence by excluding its date. */
    @DeleteMapping("/{id}/occurrence/{date}")
    public ResponseEntity<Void> excludeOccurrence(@AuthenticationPrincipal User user, @PathVariable Long id, @PathVariable String date) {
        shiftScheduleService.excludeOccurrence(user, id, LocalDate.parse(date));
        return ResponseEntity.noContent().build();
    }
}
