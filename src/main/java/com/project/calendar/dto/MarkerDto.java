package com.project.calendar.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Data
public class MarkerDto {
    private Map<String, Marker> marker;

    public MarkerDto() {
        this.marker = new HashMap<>();
    }

    public void addDay(String date, Marker scheduleDay) {
        marker.put(date, scheduleDay);
    }

    public Marker getDay(String date) {
        return marker.get(date);
    }

    @Data
    public static class Marker {
        private boolean marked;
        private ArrayList<MarkerPeriod> periods;

        public Marker() {
            marked = true;
            periods = new ArrayList<>();
        }

        public void addPeriods(MarkerPeriod period) {
            periods.add(period);
        }

        public int periodsIndex(MarkerPeriod period) {
            return periods.indexOf(period);
        }

        public int periodsSize() {
            return periods.size();
        }

        public void modifyPeriods(int index, MarkerPeriod period) {
            periods.set(index, period);
        }

        public MarkerPeriod indexPeriod(int index) {
            if(periods.size() == 0) {
                return null;
            }
            return periods.get(index);
        }
    }

    @Data
    public static class MarkerPeriod {
        private String color;
        private String startingDay;
        private boolean endingDay;

        public MarkerPeriod(String color, String startingDay, boolean endingDay) {
            this.color = color;
            this.startingDay = startingDay;
            this.endingDay = endingDay;
        }
    }

}
