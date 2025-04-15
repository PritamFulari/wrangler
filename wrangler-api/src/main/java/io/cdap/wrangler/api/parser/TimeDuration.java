package io.cdap.wrangler.api.parser;

/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Represents a time duration (e.g., 5s, 10min).
 * Provides parsing and conversion utilities.
 */

public class TimeDuration implements Token {
    private final String value;
    private final long nanos;

    public TimeDuration(String value) {
        this.value = value.trim().toLowerCase();
        this.nanos = parseNanos(this.value);
    }

    private long parseNanos(String input) {
        if (input.endsWith("ms")) {
            return (long) (Double.parseDouble(input.replace("ms", "")) * 1_000_000);
        } else if (input.endsWith("s")) {
            return (long) (Double.parseDouble(input.replace("s", "")) * 1_000_000_000);
        } else if (input.endsWith("m")) {
            return (long) (Double.parseDouble(input.replace("m", "")) * 60 * 1_000_000_000L);
        } else if (input.endsWith("h")) {
            return (long) (Double.parseDouble(input.replace("h", "")) * 3600 * 1_000_000_000L);
        }
        throw new IllegalArgumentException("Invalid time duration format: " + input);
    }

    public long getNanos() {
        return nanos;
    }

    @Override
    public Object value() {
        return value;
    }

    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }

    public double toUnit(String unit) {
        switch (unit.toLowerCase()) {
            case "ms": return nanos / 1_000_000.0;
            case "s":
            case "sec":
            case "seconds": return nanos / 1_000_000_000.0;
            case "min":
            case "minutes": return nanos / (60.0 * 1_000_000_000);
            case "h":
            case "hr":
            case "hours": return nanos / (3600.0 * 1_000_000_000);
            default: return nanos;
        }
    }

    @Override
    public JsonElement toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("value", value);
        obj.addProperty("nanos", nanos);
        return obj;
    }
}
