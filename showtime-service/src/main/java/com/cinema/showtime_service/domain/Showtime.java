package com.cinema.showtime_service.domain;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "showtimes")
@Setter
@Getter
public class Showtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Instant startTime;
    private Instant endTime;
    private double price;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "movie_id")
    private Long movieId;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "room_id")
    private Long roomId;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedAt = Instant.now();
    }
}
