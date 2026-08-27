package com.repifuzz.Entity;

public enum IncidentSeverity {
    CRITICAL,       // System down, major impact
    HIGH,           // Significant impact, workaround exists
    MEDIUM,         // Moderate impact, low workaround
    LOW             // Minor impact, cosmetic
}