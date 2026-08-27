package com.repifuzz.Entity;

public enum ResolutionType {
    FIXED,          // Issue was resolved
    DUPLICATE,      // Duplicate of another incident
    NOT_A_BUG,      // Behavior is as designed
    CANNOT_REPRODUCE,
    DEFERRED,       // Planned for future release
    CLOSED_NO_ACTION
}