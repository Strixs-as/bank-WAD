package com.techstore.bank_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(unique = true, nullable = false)
    @ToString.Include
    private String name;

    @Column
    private String description;

    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    @ToString.Exclude
    private Set<User> users = new HashSet<>();
}
