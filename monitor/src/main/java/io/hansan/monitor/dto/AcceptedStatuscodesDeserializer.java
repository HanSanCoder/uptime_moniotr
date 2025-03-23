package io.hansan.monitor.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AcceptedStatuscodesDeserializer extends JsonDeserializer<List<String>> {
    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        try {
            // 尝试作为数组读取
            return ctxt.readValue(p, ctxt.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            // 如果失败，尝试作为单个字符串读取
            String value = p.getValueAsString();
            if (value == null || value.isEmpty()) {
                return new ArrayList<>();
            }
            return Arrays.asList(value);
        }
    }
}