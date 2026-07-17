package com.beamcard.profile.persistence.model;

import com.beamcard.profile.domain.model.PriceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "price_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceItemJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @Column(name = "name", length = 500, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", length = 16, nullable = false)
    private PriceType priceType;

    @Column(name = "amount_min", precision = 12, scale = 2)
    private BigDecimal amountMin;

    @Column(name = "amount_max", precision = 12, scale = 2)
    private BigDecimal amountMax;

    @Column(name = "position", nullable = false)
    private int position;
}
