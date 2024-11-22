package uz.tenzorsoft.scaleapplication.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "commands", uniqueConstraints = @UniqueConstraint(columnNames = "scale_id"))
public class CommandsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scale_id", unique = true)  // Ensuring unique scaleId
    private Long scaleId;

    @Column(name = "open_gate1")
    private Boolean openGate1;

    @Column(name = "close_gate1")
    private Boolean closeGate1;

    @Column(name = "weighing")
    private Boolean weighing;

    @Column(name = "open_gate2")
    private Boolean openGate2;

    @Column(name = "close_gate2")
    private Boolean closeGate2;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;


    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
