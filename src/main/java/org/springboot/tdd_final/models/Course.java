package org.springboot.tdd_final.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long price;

    public boolean hasStarted() {
        return LocalDateTime.now().isAfter(startTime);
    }

    public boolean hasEnded() {
        return LocalDateTime.now().isAfter(endTime);
    }
}