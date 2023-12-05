package com.project.calendar.repository;

import com.project.calendar.entity.ScheduleEntity;
import com.project.calendar.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Integer> {
    List<ScheduleEntity> findByUserNumOrderByStart(int userNum);
}
