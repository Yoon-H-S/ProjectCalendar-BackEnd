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
@Table(name = "schedule")
public class ScheduleEntity {
    @Id
    @Column(name = "s_num")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer scheduleNum;
    @Column(name = "u_num")
    private Integer userNum;
    @Column(name = "title")
    private String title;
    @Column(name = "start")
    private String start;
    @Column(name = "end")
    private String end;
    @Column(name = "lunar")
    private String lunar;
    @Column(name = "memo")
    private String memo;
    @Column(name = "notify")
    private Integer notify;
    @Column(name = "repetition_type")
    private String repType;
    @Column(name = "repetition_week")
    private String repWeek;
    @Column(name = "repetition_end")
    private String repEnd;
    @Column(name = "file")
    private String file;
    @Column(name = "location")
    private String location;
}
