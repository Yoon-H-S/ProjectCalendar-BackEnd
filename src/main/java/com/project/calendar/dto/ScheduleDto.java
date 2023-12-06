package com.project.calendar.dto;

import com.project.calendar.entity.ScheduleEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleDto {
    private Integer scheduleNum;
    private Integer userNum;
    private String title;
    private String start;
    private String end;
    private String lunar;
    private String memo;
    private Integer notify;
    private String repType;
    private String repEnd;
    private String file;
    private String location;
    
    public ScheduleEntity toEntity() {
        return ScheduleEntity.builder()
                .scheduleNum(scheduleNum)
                .userNum(userNum)
                .title(title)
                .start(start)
                .end(end)
                .lunar(lunar)
                .memo(memo)
                .notify(notify)
                .file(file)
                .location(location)
                .build();
    }
}
