package com.beamcard.profile.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "opening_hours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpeningHoursJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "affiliation_id", nullable = false)
    private UUID affiliationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", length = 9, nullable = false)
    private DayOfWeek day;

    @Column(name = "open_time", length = 5, nullable = false)
    private String openTime;

    @Column(name = "close_time", length = 5, nullable = false)
    private String closeTime;

    @Column(name = "position", nullable = false)
    private int position;
}
