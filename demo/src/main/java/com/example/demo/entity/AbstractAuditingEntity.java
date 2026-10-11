package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"createdBy", "createdDate", "modifiedBy", "modifiedDate"}, allowGetters = true)
public abstract class AbstractAuditingEntity {

    @CreatedBy
    @Column(name = "created_by", length = 100, nullable = false, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdDate = Instant.now();

    @LastModifiedBy
    @Column(name = "modified_by", length = 100)
    private String modifiedBy;

    @LastModifiedDate
    @Column(name = "modified_date")
    @Builder.Default
    private Instant modifiedDate = Instant.now();
}