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
package io.cdap.wrangler.parser;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;

import org.junit.Assert;
import org.junit.Test;

public class ByteSizeTimeDurationTest {

    @Test
    public void testByteSizeParsing() {
        Assert.assertEquals(10240L, new ByteSize("10kb").getBytes());
        Assert.assertEquals(1572864L, new ByteSize("1.5MB").getBytes());
        Assert.assertEquals(1073741824L, new ByteSize("1gb").getBytes());
    }

    @Test
    public void testByteSizeConversion() {
        ByteSize bs = new ByteSize("2048b");
        Assert.assertEquals(2.0, bs.toUnit("kb"), 0.001);
    }

    @Test
    public void testTimeDurationParsing() {
        Assert.assertEquals(5_000_000L, new TimeDuration("5ms").getNanos());
        Assert.assertEquals(2_100_000_000L, new TimeDuration("2.1s").getNanos());
    }

    @Test
    public void testTimeDurationConversion() {
        TimeDuration td = new TimeDuration("60000ms");
        Assert.assertEquals(60.0, td.toUnit("s"), 0.001);
    }
}
