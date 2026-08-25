package com.repifuzz.controller;

import com.repifuzz.EntityDTO.IncidentRequest;
import com.repifuzz.EntityDTO.IncidentResponse;
import com.repifuzz.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ims/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    public ResponseEntity<IncidentResponse> createIncident(@RequestBody IncidentRequest request) {
        return ResponseEntity.ok(incidentService.createIncident(request));
    }

    @GetMapping("/{incidentId}")
    public ResponseEntity<IncidentResponse> getIncident(@PathVariable String incidentId) {
        return ResponseEntity.ok(incidentService.getIncident(incidentId));
    }
}