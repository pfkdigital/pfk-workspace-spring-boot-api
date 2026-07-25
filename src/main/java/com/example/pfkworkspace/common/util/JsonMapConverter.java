package com.example.pfkworkspace.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

@Converter
public class JsonMapConverter implements AttributeConverter<Map<String, String>, String> {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(Map<String, String> attribute) {
    try {
      return objectMapper.writeValueAsString(attribute);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Error serializing payload", e);
    }
  }

  @Override
  public Map<String, String> convertToEntityAttribute(String dbData) {
    try {
      return objectMapper.readValue(dbData, new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Error deserializing payload", e);
    }
  }
}
