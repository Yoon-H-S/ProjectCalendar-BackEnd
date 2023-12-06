package com.project.calendar.controller;

import com.project.calendar.dto.MarkerDto;
import com.project.calendar.dto.RestDayDto;
import com.project.calendar.dto.ScheduleDto;
import com.project.calendar.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CalendarController {

    CalendarService cs;

    @Autowired
    public CalendarController(CalendarService cs) {
        this.cs = cs;
    }

    @PostMapping("kakao-login")
    public int getUserNumber(@RequestParam String token) {
        try {
            return cs.getUserNumber(token);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("kakao-api")
    public void KakaoRedirectUri() {
        System.out.println("카카오 로그인 시도");
    }

    @GetMapping("schedule-list")
    public MarkerDto getScheduleList(@RequestParam int userNum) {
        return cs.getScheduleList(userNum);
    }

    @GetMapping("rest-day")
    public List<RestDayDto> getRestDay(@RequestParam String year) {
        System.out.println("공휴일 요청");
        try {
            return cs.getRestDay(year);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("add-schedule")
    public void addSchedule(@RequestBody ScheduleDto dto) {
        cs.addSchedule(dto);
    }
}
