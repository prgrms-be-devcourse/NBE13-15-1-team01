package com.kdt.cafesystem.menu.dto;

import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.DatabindException;
import tools.jackson.core.JsonToken;

public record MenuAvailabilityRequestDto(
        @NotNull
        @JsonDeserialize(using = StrictBooleanDeserializer.class)
        Boolean available
) {

    public static class StrictBooleanDeserializer
            extends StdDeserializer<Boolean> {

        public StrictBooleanDeserializer() {
            super(Boolean.class);
        }

        @Override
        public Boolean deserialize(JsonParser parser,
                                   DeserializationContext context) {
            JsonToken token = parser.currentToken();

            if (token == JsonToken.VALUE_TRUE) {
                return true;
            }

            if (token == JsonToken.VALUE_FALSE) {
                return false;
            }

            throw DatabindException.from(
                    parser,
                    "available must be a JSON boolean"
            );
        }
    }
}
