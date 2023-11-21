package com.project.calendar.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
@Table(name = "user")
public class UserEntity {
    @Id
    @Column(name = "u_num")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userNum;
    @Column(name = "u_id")
    private String userId;
}
