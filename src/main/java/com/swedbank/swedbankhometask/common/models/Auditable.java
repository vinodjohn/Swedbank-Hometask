package com.swedbank.swedbankhometask.common.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

/**
 * Base class adding audit metadata to entities. Only the creation date is exposed in JSON.
 *
 * @author vinodjohn
 * @since 22.07.2026
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable<V> {
    @CreatedBy
    @JsonIgnore
    @Column(updatable = false)
    protected V createdBy;

    @CreatedDate
    @Column(updatable = false)
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    protected LocalDateTime createdDate;

    @LastModifiedBy
    @JsonIgnore
    protected V lastModifiedBy;

    @LastModifiedDate
    @JsonIgnore
    protected LocalDateTime lastModifiedDate;
}
