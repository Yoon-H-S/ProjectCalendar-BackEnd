package com.project.calendar.service.impl;

import com.project.calendar.dto.RestDayDto;
import com.project.calendar.service.CalendarService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class CalendarServiceImpl implements CalendarService {
    private final Environment env;

    public CalendarServiceImpl(Environment env) {
        this.env = env;
    }

    @Override
    public List<RestDayDto> getRestDay(String year) throws Exception {
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo"); // URL
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=" + env.getProperty("serviceKey")); // 공공데이터포털 개인 인증키
        urlBuilder.append("&" + URLEncoder.encode("solYear","UTF-8") + "=" + URLEncoder.encode(year, "UTF-8")); // 조회할 년도
        urlBuilder.append("&" + URLEncoder.encode("_type","UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); // 받을 데이터 타입
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("30", "UTF-8")); // 한번에 받을 데이터 갯수
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Response code: " + conn.getResponseCode());
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
            restDayDtoList.add(RestDayDto.builder().date(new Date(sf.parse(locdate).getTime())).name(dateName).build());
        }

        return restDayDtoList;
    }
}
