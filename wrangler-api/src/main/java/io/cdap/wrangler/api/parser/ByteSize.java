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
 * Represents a byte size value (e.g., KB, MB, GB).
 * Provides parsing and conversion utilities.
 */

public class ByteSize implements Token {
    private final String value;
    private final long bytes;

    public ByteSize(String value) {
        this.value = value.trim().toLowerCase();
        this.bytes = parseBytes(this.value);
    }

    private long parseBytes(String input) {
        if (input.endsWith("kb")) {
            return (long) (Double.parseDouble(input.replace("kb", "")) * 1024);
        } else if (input.endsWith("mb")) {
            return (long) (Double.parseDouble(input.replace("mb", "")) * 1024 * 1024);
        } else if (input.endsWith("gb")) {
            return (long) (Double.parseDouble(input.replace("gb", "")) * 1024 * 1024 * 1024);
        } else if (input.endsWith("tb")) {
            return (long) (Double.parseDouble(input.replace("tb", "")) * 1024L * 1024L * 1024L * 1024L);
        } else if (input.endsWith("b")) {
            return (long) Double.parseDouble(input.replace("b", ""));
        }
        throw new IllegalArgumentException("Invalid byte size format: " + input);
    }

    public long getBytes() {
        return bytes;
    }

    @Override
    public Object value() {
        return value;
    }

    @Override
    public TokenType type() {
        return TokenType.BYTE_SIZE;
    }
    public double toUnit(String unit) {
        switch (unit.toLowerCase()) {
            case "kb": return bytes / 1024.0;
            case "mb": return bytes / (1024.0 * 1024);
            case "gb": return bytes / (1024.0 * 1024 * 1024);
            case "tb": return bytes / (1024.0 * 1024 * 1024 * 1024);
            default: return bytes;
        }
    }


    @Override
    public JsonElement toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("value", value);
        obj.addProperty("bytes", bytes);
        return obj;
    }
}
