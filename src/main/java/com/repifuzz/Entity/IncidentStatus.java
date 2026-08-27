package com.repifuzz.Entity;

public enum IncidentStatus {
    OPEN,           // Initial state - just reported
    ACKNOWLEDGED,   // Analyst confirmed receipt
    ASSIGNED,       // Assigned to a team member
    IN_PROGRESS,    // Active investigation/resolution
    ON_HOLD,        // Paused (awaiting info, etc.)
    RESOLVED,       // Issue fixed
    CLOSED          // No further action needed
}