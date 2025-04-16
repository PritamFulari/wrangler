/*
 *  Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */
package io.cdap.wrangler.statistics;

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.Row;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class AggregateStatsTest {

    @Test
    public void testAggregateTotal() throws Exception {
        List<Row> rows = Arrays.asList(
                new Row("data_transfer_size", "1MB").add("response_time", "2s"),
                new Row("data_transfer_size", "512KB").add("response_time", "1s")
        );

        String[] recipe = new String[] {
                "aggregate-stats :data_transfer_size :response_time :total_size_mb :total_time_sec 'mb' 's' 'total'"
        };

        List<Row> results = TestingRig.execute(recipe, rows);
        Assert.assertEquals(1, results.size());

        double expectedMB = (1024 * 1024 + 512 * 1024) / (1024.0 * 1024); // 1.5 MB
        double expectedSec = (2 + 1); // 3 seconds

        Assert.assertEquals(expectedMB, (Double) results.get(0).getValue("total_size_mb"), 0.001);
        Assert.assertEquals(expectedSec, (Double) results.get(0).getValue("total_time_sec"), 0.001);
    }

    @Test
    public void testAggregateAverage() throws Exception {
        List<Row> rows = Arrays.asList(
                new Row("data_transfer_size", "2MB").add("response_time", "2s"),
                new Row("data_transfer_size", "2MB").add("response_time", "2s")
        );

        String[] recipe = new String[] {
                "aggregate-stats :data_transfer_size :response_time :avg_size_mb :avg_time_sec 'mb' 's' 'average'"
        };

        List<Row> results = TestingRig.execute(recipe, rows);
        Assert.assertEquals(1, results.size());

        Assert.assertEquals(2.0, (Double) results.get(0).getValue("avg_size_mb"), 0.001);
        Assert.assertEquals(2.0, (Double) results.get(0).getValue("avg_time_sec"), 0.001);
    }
}
