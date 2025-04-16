/*
 * Copyright © 2015-2025 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cdap.wrangler.statistics;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.Identifier;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Aggregates byte size and time duration values across rows
 * and outputs a single row with totals or averages.
 */
public class AggregateStats implements Directive {

    private String sizeColumn;
    private String timeColumn;
    private String outputSizeColumn;
    private String outputTimeColumn;
    private String sizeUnit = "bytes";   // default
    private String timeUnit = "nanos";   // default
    private String aggType = "total";    // total or average

    private long totalBytes = 0;
    private long totalNanos = 0;
    private int rowCount = 0;

    // Valid units for size and time
    private static final Set<String> VALID_SIZE_UNITS = new HashSet<String>() {{
        add("b");
        add("kb");
        add("mb");
        add("gb");
        add("tb");
        add("bytes");
    }};

    private static final Set<String> VALID_TIME_UNITS = new HashSet<String>() {{
        add("ns");
        add("us");
        add("ms");
        add("s");
        add("m");
        add("h");
        add("nanos");
        add("micros");
        add("millis");
        add("seconds");
        add("minutes");
        add("hours");
    }};

    private static final Set<String> VALID_AGG_TYPES = new HashSet<String>() {{
        add("total");
        add("average");
    }};

    @Override
    public UsageDefinition define() {
        // Assuming you need to pass a string to initialize the builder
        UsageDefinition.Builder builder = new UsageDefinition.Builder("aggregate-stats");

        builder.define("size-column", TokenType.IDENTIFIER);
        builder.define("time-column", TokenType.IDENTIFIER);
        builder.define("output-size-column", TokenType.IDENTIFIER);
        builder.define("output-time-column", TokenType.IDENTIFIER);

        // Optional fields (use .define() directly if .defineOptional() is not available)
        builder.define("size-unit", TokenType.TEXT, true);   // true for optional
        builder.define("time-unit", TokenType.TEXT, true);   // true for optional
        builder.define("aggregation-type", TokenType.TEXT, true);   // true for optional

        return builder.build();
    }

    @Override
    public void initialize(Arguments arguments) throws DirectiveParseException {
        sizeColumn = ((Identifier) arguments.value("size-column")).value();
        timeColumn = ((Identifier) arguments.value("time-column")).value();
        outputSizeColumn = ((Identifier) arguments.value("output-size-column")).value();
        outputTimeColumn = ((Identifier) arguments.value("output-time-column")).value();

        if (arguments.value("size-unit") != null) {
            sizeUnit = ((Text) arguments.value("size-unit")).value().toLowerCase();
        }
        if (arguments.value("time-unit") != null) {
            timeUnit = ((Text) arguments.value("time-unit")).value().toLowerCase();
        }
        if (arguments.value("aggregation-type") != null) {
            aggType = ((Text) arguments.value("aggregation-type")).value().toLowerCase();
        }

        // Validate the size and time units
        if (!VALID_SIZE_UNITS.contains(sizeUnit)) {
            throw new DirectiveParseException("Invalid size unit: " + sizeUnit);
        }
        if (!VALID_TIME_UNITS.contains(timeUnit)) {
            throw new DirectiveParseException("Invalid time unit: " + timeUnit);
        }
        if (!VALID_AGG_TYPES.contains(aggType)) {
            throw new DirectiveParseException("Invalid aggregation type: " + aggType);
        }
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
        for (Row row : rows) {
            Object sizeObj = row.getValue(sizeColumn);
            Object timeObj = row.getValue(timeColumn);

            if (sizeObj instanceof String && timeObj instanceof String) {
                ByteSize byteSize = new ByteSize((String) sizeObj);
                TimeDuration timeDuration = new TimeDuration((String) timeObj);

                totalBytes += byteSize.getBytes();
                totalNanos += timeDuration.getNanos();
                rowCount++;
            } else {
                throw new DirectiveExecutionException("Expected string values in size and time columns.");
            }
        }

        double finalBytes = aggType.equals("average") ? totalBytes / (double) rowCount : totalBytes;
        double finalNanos = aggType.equals("average") ? totalNanos / (double) rowCount : totalNanos;

        ByteSize byteSizeResult = new ByteSize(finalBytes + "b");
        TimeDuration timeDurationResult = new TimeDuration(finalNanos + "ns");

        double convertedBytes = byteSizeResult.toUnit(sizeUnit);
        double convertedTime = timeDurationResult.toUnit(timeUnit);

        Row result = new Row();
        result.add(outputSizeColumn, convertedBytes);
        result.add(outputTimeColumn, convertedTime);

        return Collections.singletonList(result);
    }

    @Override
    public void destroy() {
        // No-op
    }
}
