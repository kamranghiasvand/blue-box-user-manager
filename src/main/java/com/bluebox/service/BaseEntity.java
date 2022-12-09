package com.bluebox.service;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.UUID;


@Setter
@Getter
@ToString
@EqualsAndHashCode(of = {"id", "uuid"})
@MappedSuperclass
public class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(
            name = "native",
            strategy = "native"
    )
    @Column(name = "pk_id")
    private Long id;
    @Column(name = "uuid", unique = true, nullable = false, updatable = false)
    private String uuid;
    @Column(name = "deleted")
    private Boolean deleted = false;
    @Column(name = "create_date", nullable = false)
    private Timestamp created;
    @Column(name = "last_update", nullable = false)
    private Timestamp lastUpdated;

    @PrePersist
    public void prePersist() {
        uuid = UUID.randomUUID().toString();
        lastUpdated = created = new Timestamp(System.currentTimeMillis());
    }

    @PreUpdate
    public void update() {
        lastUpdated = new Timestamp(System.currentTimeMillis());
    }

}
