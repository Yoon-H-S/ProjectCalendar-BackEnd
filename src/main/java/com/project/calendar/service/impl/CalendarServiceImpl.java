package com.project.calendar.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.calendar.dto.LunarDto;
import com.project.calendar.dto.RestDayDto;
import com.project.calendar.dto.ScheduleDto;
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
    public LunarDto getLunarDate(String year, String month, String day) throws Exception {
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B090041/openapi/service/LrsrCldInfoService/getLunCalInfo"); // URL
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=" + env.getProperty("serviceKey")); // 공공데이터포털 개인 인증키
        urlBuilder.append("&" + URLEncoder.encode("_type","UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); // 받을 데이터 타입
        urlBuilder.append("&" + URLEncoder.encode("solYear","UTF-8") + "=" + URLEncoder.encode(year, "UTF-8")); // 조회할 년도
        urlBuilder.append("&" + URLEncoder.encode("solMonth","UTF-8") + "=" + URLEncoder.encode(month, "UTF-8")); // 조회할 달
        urlBuilder.append("&" + URLEncoder.encode("solDay","UTF-8") + "=" + URLEncoder.encode(day, "UTF-8")); // 조회할 날
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Lunar-Date Response code: " + conn.getResponseCode());
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
        jo = (JSONObject) ((JSONObject)((JSONObject)((JSONObject)jo.get("response")).get("body")).get("items")).get("item");

        String lunYear = jo.get("lunYear").toString();
        String lunMonth = jo.get("lunMonth").toString();
        String lunDay = jo.get("lunDay").toString();
        String leap = jo.get("lunLeapmonth").toString();

        LunarDto lunarDto = new LunarDto().builder()
                .lunYear(lunYear)
                .lunMonth(lunMonth)
                .lunDay(lunDay)
                .leap(leap)
                .build();

        return lunarDto;
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
    public int addSchedule(ScheduleDto dto) {
        return scheduleRepository.save(dto.toEntity()).getScheduleNum();
    }
}
