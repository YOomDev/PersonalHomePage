package com.ypeoomes.Services;

import com.ypeoomes.DTO.CalendarEventDto;
import com.ypeoomes.DTO.CalendarShareDto;
import com.ypeoomes.DTO.CalendarShareRequest;
import com.ypeoomes.Entities.CalendarEvent;
import com.ypeoomes.Entities.CalendarShare;
import com.ypeoomes.Entities.CalendarShareMode;
import com.ypeoomes.Entities.CalendarVisibility;
import com.ypeoomes.Entities.User;
import com.ypeoomes.Repositories.CalendarEventRepository;
import com.ypeoomes.Repositories.CalendarShareRepository;
import com.ypeoomes.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CalendarShareService {
    private final CalendarShareRepository shareRepository;
    private final CalendarEventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CalendarShareDto> listRules(User owner) {
        return shareRepository.findByOwner(owner).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public CalendarShareDto upsertRule(User owner, CalendarShareRequest request) {
        String granteeUsername = request.getUsername().trim();

        if (granteeUsername.equalsIgnoreCase(owner.getUsername())) {
            throw new IllegalArgumentException("You cannot share events with yourself");
        }
        if (userRepository.findByUsername(granteeUsername).isEmpty()) {
            throw new IllegalArgumentException("User '" + granteeUsername + "' does not exist");
        }

        CalendarShare share = shareRepository.findByOwnerAndGranteeUsername(owner, granteeUsername)
                .orElseGet(() -> CalendarShare.builder()
                        .owner(owner)
                        .granteeUsername(granteeUsername)
                        .build());

        share.setMode(request.getMode());
        Set<CalendarVisibility> requested = request.getAllowedVisibilities() != null
                ? request.getAllowedVisibilities()
                : Set.of();
        Set<CalendarVisibility> allowed = EnumSet.noneOf(CalendarVisibility.class);
        if (request.getMode() == CalendarShareMode.LIMITED) {
            for (CalendarVisibility v : requested) {
                if (v == CalendarVisibility.PRIVATE) {
                    throw new IllegalArgumentException("Private events cannot be shared directly; grant full access instead");
                }
                allowed.add(v);
            }
            if (allowed.isEmpty()) {
                throw new IllegalArgumentException("Select at least one category for limited access");
            }
        }
        share.getAllowedVisibilities().clear();
        share.getAllowedVisibilities().addAll(allowed);

        return toDto(shareRepository.save(share));
    }

    @Transactional
    public void deleteRule(User owner, String granteeUsername) {
        CalendarShare share = shareRepository.findByOwnerAndGranteeUsername(owner, granteeUsername)
                .orElseThrow(() -> new IllegalArgumentException("No share rule for user '" + granteeUsername + "'"));
        shareRepository.delete(share);
    }

    @Transactional(readOnly = true)
    public List<CalendarEventDto> getSharedEvents(User viewer) {
        List<CalendarEventDto> result = new ArrayList<>();

        for (CalendarShare rule : shareRepository.findByGranteeUsername(viewer.getUsername())) {
            boolean fullAccess = rule.getMode() == CalendarShareMode.FULL;

            for (CalendarEvent event : eventRepository.findByUserOrderByStartAtAsc(rule.getOwner())) {
                CalendarVisibility visibility = event.getVisibility() != null
                        ? event.getVisibility()
                        : CalendarVisibility.PRIVATE;

                boolean detailAllowed = fullAccess || rule.getAllowedVisibilities().contains(visibility);
                result.add(toDto(event, rule.getOwner().getUsername(), detailAllowed));
            }
        }

        result.sort((a, b) -> a.getStartAt().compareTo(b.getStartAt()));
        return result;
    }

    private CalendarEventDto toDto(CalendarEvent event, String ownerUsername, boolean detailAllowed) {
        CalendarEventDto.CalendarEventDtoBuilder builder = CalendarEventDto.builder()
                .ownerUsername(ownerUsername)
                .masked(!detailAllowed)
                .startAt(event.getStartAt())
                .endAt(event.getEndAt())
                .allDay(event.isAllDay());

        if (detailAllowed) {
            builder
                    .id(event.getId())
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .location(event.getLocation());
        }

        builder.visibility(event.getVisibility() != null ? event.getVisibility() : CalendarVisibility.PRIVATE);
        return builder.build();
    }

    private CalendarShareDto toDto(CalendarShare share) {
        return CalendarShareDto.builder()
                .username(share.getGranteeUsername())
                .mode(share.getMode())
                .allowedVisibilities(share.getAllowedVisibilities())
                .build();
    }
}
