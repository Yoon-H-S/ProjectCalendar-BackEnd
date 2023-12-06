package com.project.calendar.service;

import com.project.calendar.dto.MarkerDto;
import com.project.calendar.dto.RestDayDto;
import com.project.calendar.dto.ScheduleDto;

import java.util.List;

public interface CalendarService {
    /** 일정 조회 */
    MarkerDto getScheduleList(int userNum);
    /** 공휴일 조회 */
    List<RestDayDto> getRestDay(String year) throws Exception;
    /** 유저 번호 조회, 없다면 생성 */
    int getUserNumber(String token) throws Exception;
    /** 일정 추가 */
    void addSchedule(ScheduleDto dto);
}
