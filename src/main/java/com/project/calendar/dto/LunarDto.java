package com.project.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LunarDto {
    private String lunYear;
    private String lunMonth;
    private String lunDay;
    private String leap;
}
