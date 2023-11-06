package com.project.calendar.service;

import com.project.calendar.dto.RestDayDto;

import java.util.List;

public interface CalendarService {
    List<RestDayDto> getRestDay(String year) throws Exception;
}
