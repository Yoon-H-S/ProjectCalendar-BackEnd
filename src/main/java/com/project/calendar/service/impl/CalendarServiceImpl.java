package com.project.calendar.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.calendar.dto.MarkerDto;
import com.project.calendar.dto.RestDayDto;
import com.project.calendar.dto.ScheduleDto;
import com.project.calendar.entity.ScheduleEntity;
import com.project.calendar.entity.UserEntity;
import com.project.calendar.repository.ScheduleRepository;
import com.project.calendar.repository.UserRepository;
import com.project.calendar.service.CalendarService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CalendarServiceImpl implements CalendarService {
    private final Environment env;
    private final RestTemplate restTemplate = new RestTemplate();

    UserRepository userRepository;
    ScheduleRepository scheduleRepository;

    @Autowired
    public CalendarServiceImpl(Environment env, UserRepository userRepository, ScheduleRepository scheduleRepository) {
        this.env = env;
        this.userRepository = userRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Override
    public MarkerDto getScheduleList(int userNum) {

        List<ScheduleEntity> scheduleEntityList = scheduleRepository.findByUserNumOrderByStart(userNum);

        MarkerDto dto = new MarkerDto();
        int type = 0;
        int index = 0;
        for(ScheduleEntity entity : scheduleEntityList) {
            LocalDate start = LocalDate.parse(entity.getStart().substring(0, 10));
            LocalDate end = LocalDate.parse(entity.getEnd().substring(0, 10));
            for(LocalDate i = start; !i.isAfter(end); i = i.plusDays(1)) {
                MarkerDto.Marker marker = dto.getDay(i.toString());

                if(i.equals(start)) { // 이번 일정의 첫번째 칸(날짜)이라면
                    MarkerDto.MarkerPeriod period = new MarkerDto.MarkerPeriod("#5A61E0", entity.getTitle(), i.equals(end));
                    if (marker != null) { // 이미 칸(날짜)이 있다면
                        boolean isEmpty = false;
                        for(int j = 0; j < marker.periodsSize(); j++) { // 첫번째 줄(일정)부터 확인하면서
                            MarkerDto.MarkerPeriod indexPeriod = marker.indexPeriod(j);
                            if(indexPeriod.getColor().equals("transparent")) { // 그 줄(일정)이 공백일정이라면
                                marker.modifyPeriods(j, period); // 이번 일정으로 교체
                                isEmpty = true;
                            }
                        }
                        if(!isEmpty) {
                            marker.addPeriods(period); // period 추가
                        }
                    } else { // 칸(날짜)이 존재하지 않는 경우
                        marker = new MarkerDto.Marker(); // 새로운 객체를 생성하여 추가
                        marker.addPeriods(period);
                        dto.addDay(i.toString(), marker);
                    }
                    index = marker.periodsIndex(period); // 이번 일정의 줄 구하기
                } else { // 이번 일정의 첫번째 칸(날짜)이 아니면
                    MarkerDto.MarkerPeriod period = new MarkerDto.MarkerPeriod("#5A61E0", null, i.equals(end));
                    if(marker != null) { // 이미 칸(날짜)이 있다면 period 추가
                        if(marker.periodsSize() < index) { // 이번 일정이 이 칸의 첫번째 줄(일정)이 아니라면
                            for(int j = marker.periodsSize(); j < index; j++) { // 첫번째 줄(일정)부터 확인하면서
                                if(marker.indexPeriod(j) == null) { // 그 줄(일정)에 아무것도 없다면 공백일정 생성
                                    marker.addPeriods(new MarkerDto.MarkerPeriod("transparent", null, false));
                                }
                            }
                        }
                        marker.addPeriods(period);
                    } else { // 칸(날짜)이 존재하지 않는 경우
                        marker = new MarkerDto.Marker(); // 새로운 객체를 생성하여 추가
                        if(marker.periodsSize() < index) { // 이번 일정이 이 칸의 첫번째 줄(일정)이 아니라면
                            for(int j = marker.periodsSize(); j < index; j++) { // 첫번째 줄(일정)부터 확인하면서
                                if(marker.indexPeriod(j) == null) { // 그 줄(일정)에 아무것도 없다면 공백일정 생성
                                    marker.addPeriods(new MarkerDto.MarkerPeriod("transparent", null, false));
                                }
                            }
                        }
                        marker.addPeriods(period);
                        dto.addDay(i.toString(), marker);
                    }
                }
            }
            type++;
        }

        return dto;
    }

    @Override
    public List<RestDayDto> getRestDay(String year) throws Exception {
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo"); // URL
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=" + env.getProperty("serviceKey")); // 공공데이터포털 개인 인증키
        urlBuilder.append("&" + URLEncoder.encode("_type","UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); // 받을 데이터 타입
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("30", "UTF-8")); // 한번에 받을 데이터 갯수
        urlBuilder.append("&" + URLEncoder.encode("solYear","UTF-8") + "=" + URLEncoder.encode(year, "UTF-8")); // 조회할 년도
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Rest-Day Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        // JSON 파싱 후 리스트에 담기
        JSONParser jsonParser = new JSONParser();
        JSONObject jo = (JSONObject)jsonParser.parse(sb.toString());
        JSONArray jsonArray = (JSONArray)((JSONObject)((JSONObject)((JSONObject)jo.get("response")).get("body")).get("items")).get("item");

        List<RestDayDto> restDayDtoList = new ArrayList<>();
        String locdate = "";
        String dateName = "";
        SimpleDateFormat sf = new SimpleDateFormat();
        sf.applyPattern("yyyyMMdd");
        for(int i = 0; i < jsonArray.size(); i++) {
            locdate = ((JSONObject)jsonArray.get(i)).get("locdate").toString();
            dateName = ((JSONObject)jsonArray.get(i)).get("dateName").toString();
            restDayDtoList.add(RestDayDto.builder()
                    .date(new Date(sf.parse(locdate).getTime()))
                    .name(dateName)
                    .build());
        }

        return restDayDtoList;
    }

    @Override
    public int getUserNumber(String token) throws Exception {
        String resourceUri = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity entity = new HttpEntity(headers);
        JsonNode responseNode = restTemplate.exchange(resourceUri, HttpMethod.GET, entity, JsonNode.class).getBody();

        JsonNode kakaoAccount = responseNode.get("kakao_account");
        String kakaoId = kakaoAccount.get("email").asText();

        Optional<UserEntity> optionalUser = userRepository.findByUserId(kakaoId);
        if(optionalUser.isPresent()) {
            return optionalUser.get().getUserNum();
        } else {
            return userRepository.save(UserEntity.builder().userId(kakaoId).build()).getUserNum();
        }
    }

    @Override
    public void addSchedule(ScheduleDto dto) {
        if(dto.getRepType() != null) {
            LocalDate repeatStartDate = LocalDate.parse(dto.getStart().substring(0, 10));

            if (dto.getRepEnd() == null) {
                dateRepeatSchedule(repeatStartDate, LocalDate.of(2030, 12, 31), dto); // 기본값: 2030년까지 반복
            } else if (dto.getRepEnd().length() > 4) {
                dateRepeatSchedule(repeatStartDate, LocalDate.parse(dto.getRepEnd()), dto);
            } else {
                countRepeatSchedule(repeatStartDate, Integer.parseInt(dto.getRepEnd()), dto);
            }

        } else {
            dto.setStart(dto.getStart().substring(0, 10) + " " + dto.getStart().substring(11, 19));
            dto.setEnd(dto.getEnd().substring(0, 10) + " " + dto.getEnd().substring(11, 19));

            scheduleRepository.save(dto.toEntity());
        }
    }

    private void dateRepeatSchedule(LocalDate startDate, LocalDate endDate, ScheduleDto dto) {
        LocalDate scheduleEnd = LocalDate.parse(dto.getEnd().substring(0, 10));

        switch (dto.getRepType()) {
            case "1":
                while (!startDate.isAfter(endDate)) {
                    dto.setStart(startDate + " " + dto.getStart().substring(11, 19));
                    dto.setEnd(scheduleEnd + " " + dto.getEnd().substring(11, 19));

                    scheduleRepository.save(dto.toEntity());
                    startDate = startDate.plusDays(1);
                    scheduleEnd = scheduleEnd.plusDays(1);
                }
                break;
            case "2":
                while (!startDate.isAfter(endDate)) {
                    dto.setStart(startDate + " " + dto.getStart().substring(11, 19));
                    dto.setEnd(scheduleEnd + " " + dto.getEnd().substring(11, 19));

                    scheduleRepository.save(dto.toEntity());
                    startDate = startDate.plusWeeks(1);
                    scheduleEnd = scheduleEnd.plusWeeks(1);
                }
                break;
            case "3":
                while (!startDate.isAfter(endDate)) {
                    dto.setStart(startDate + " " + dto.getStart().substring(11, 19));
                    dto.setEnd(scheduleEnd + " " + dto.getEnd().substring(11, 19));

                    scheduleRepository.save(dto.toEntity());
                    startDate = startDate.plusMonths(1);
                    scheduleEnd = scheduleEnd.plusMonths(1);
                }
                break;
            case "4":
                while (!startDate.isAfter(endDate)) {
                    dto.setStart(startDate + " " + dto.getStart().substring(11, 19));
                    dto.setEnd(scheduleEnd + " " + dto.getEnd().substring(11, 19));

                    scheduleRepository.save(dto.toEntity());
                    startDate = startDate.plusYears(1);
                    scheduleEnd = scheduleEnd.plusYears(1);
                }
                break;
            default:
                throw new IllegalArgumentException("잘못된 일정 타입");
        }
    }

    private void countRepeatSchedule(LocalDate startDate, int endCount, ScheduleDto dto) {
        LocalDate scheduleEnd = LocalDate.parse(dto.getEnd().substring(0, 10));

        switch (dto.getRepType()) {
            case "1":
                for(int i = 0; i < endCount; i++) {
                    dto.setStart(startDate + " " + dto.getStart().substring(11, 19));
                    dto.setEnd(scheduleEnd + " " + dto.getEnd().substring(11, 19));

                    scheduleRepository.save(dto.toEntity());
                    startDate = startDate.plusDays(1);
                    scheduleEnd = scheduleEnd.plusDays(1);
                }
                break;
            case "2":
                for(int i = 0; i < endCount; i++) {
                    dto.setStart(startDate + " " + dto.getStart().substring(11, 19));
                    dto.setEnd(scheduleEnd + " " + dto.getEnd().substring(11, 19));

                    scheduleRepository.save(dto.toEntity());
                    startDate = startDate.plusWeeks(1);
                    scheduleEnd = scheduleEnd.plusWeeks(1);
                }
                break;
            case "3":
                for(int i = 0; i < endCount; i++) {
                    dto.setStart(startDate + " " + dto.getStart().substring(11, 19));
                    dto.setEnd(scheduleEnd + " " + dto.getEnd().substring(11, 19));

                    scheduleRepository.save(dto.toEntity());
                    startDate = startDate.plusMonths(1);
                    scheduleEnd = scheduleEnd.plusMonths(1);
                }
                break;
            case "4":
                for(int i = 0; i < endCount; i++) {
                    dto.setStart(startDate + " " + dto.getStart().substring(11, 19));
                    dto.setEnd(scheduleEnd + " " + dto.getEnd().substring(11, 19));

                    scheduleRepository.save(dto.toEntity());
                    startDate = startDate.plusYears(1);
                    scheduleEnd = scheduleEnd.plusYears(1);
                }
                break;
            default:
                throw new IllegalArgumentException("잘못된 일정 타입");
        }
    }
}
