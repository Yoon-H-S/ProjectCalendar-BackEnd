package com.project.calendar.controller;

import com.project.calendar.dto.RestDayDto;
import com.project.calendar.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CalendarController {

    CalendarService cs;

    @Autowired
    public CalendarController(CalendarService cs) {
        this.cs = cs;
    }

    @GetMapping("/rest-day")
    public List<RestDayDto> getRestDay(@RequestParam String year) {
        try {
            return cs.getRestDay(year);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
