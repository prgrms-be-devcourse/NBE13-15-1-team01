package com.kdt.cafesystem.menu.dto;

import jakarta.validation.constraints.NotNull;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * A21 판매 여부 변경 요청. available은 JSON boolean(true/false)만 허용한다.
 */
public record MenuAvailabilityRequestDto(

        @NotNull
        @JsonDeserialize(using = StrictBooleanDeserializer.class)
        Boolean available
) {

    // "true", 1 같은 값이 boolean으로 바뀌지 않도록 막는다 → 400
    public static class StrictBooleanDeserializer extends StdDeserializer<Boolean> {

        public StrictBooleanDeserializer() {
            super(Boolean.class);
        }

        @Override
        public Boolean deserialize(JsonParser parser, DeserializationContext context) {
            JsonToken token = parser.currentToken();
            if (token == JsonToken.VALUE_TRUE) {
                return true;
            }
            if (token == JsonToken.VALUE_FALSE) {
                return false;
            }
            throw DatabindException.from(parser, "available must be a JSON boolean");
        }
    }
}
