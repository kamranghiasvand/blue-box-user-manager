package com.bluebox.service;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.UUID;


@Setter
@ToString
@EqualsAndHashCode(of = {"id", "uuid"})
@MappedSuperclass
public class BaseEntity {
    private Long id;
    private String uuid;
    private Boolean deleted;
    private Timestamp created;
    private Timestamp lastUpdated;


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(
            name = "native",
            strategy = "native"
    )
    @Column(name = "pk_id")
    public Long getId() {
        return id;
    }

    @Column(name = "deleted")
    public Boolean getDeleted() {
        return deleted;
    }

    @Column(name = "uuid", unique = true, nullable = false, updatable = false)
    public String getUuid() {
        return uuid;
    }

    @Column(name = "create_date", nullable = false)
    public Timestamp getCreated() {
        return created;
    }

    @Column(name = "last_update", nullable = false)
    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

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
