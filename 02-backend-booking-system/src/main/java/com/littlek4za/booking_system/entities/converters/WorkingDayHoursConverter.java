package com.littlek4za.booking_system.entities.converters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.littlek4za.booking_system.models.TimeRange;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class WorkingDayHoursConverter implements AttributeConverter<Map<Integer,List<TimeRange>>, String>{

    private final ObjectMapper objectMapper;

    public WorkingDayHoursConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String convertToDatabaseColumn(Map<Integer, List<TimeRange>> attribute) {
        if(attribute == null){
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting WorkingDayHours to JSON", e);
        }
    }

    @Override
    public Map<Integer, List<TimeRange>> convertToEntityAttribute(String dbData) {
        if(dbData == null || dbData.isEmpty()){
            return new HashMap<>();
        }
        try {
            TypeReference<Map<Integer,List<TimeRange>>> typeRef = new TypeReference<Map<Integer,List<TimeRange>>>() {};
            return objectMapper.readValue(dbData,typeRef);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting JSON to WorkingDayHours", e);
        }
    }

}
