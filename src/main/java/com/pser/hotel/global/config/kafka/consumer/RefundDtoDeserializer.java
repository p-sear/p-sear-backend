package com.pser.hotel.global.config.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pser.hotel.global.common.RefundDto;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class RefundDtoDeserializer implements Deserializer<RefundDto> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public RefundDto deserialize(String topic, byte[] data) {
        try {
            if (data == null) {
                return null;
            }
            return objectMapper.readValue(new String(data, StandardCharsets.UTF_8), RefundDto.class);
        } catch (Exception e) {
            throw new SerializationException("Error when deserializing byte[] to MessageDto");
        }
    }

    @Override
    public void close() {
    }
}
