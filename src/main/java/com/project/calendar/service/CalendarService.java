package com.project.calendar.service;

import com.project.calendar.dto.LunarDto;
import com.project.calendar.dto.RestDayDto;

import java.util.List;

public interface CalendarService {
    /** 공휴일 조회 */
    List<RestDayDto> getRestDay(String year) throws Exception;
    /** 음력 조회 */
    LunarDto getLunarDate(String year, String month, String day) throws Exception;
    /** 유저 번호 조회, 없다면 생성 */
    int getUserNumber(String token) throws Exception;
}
