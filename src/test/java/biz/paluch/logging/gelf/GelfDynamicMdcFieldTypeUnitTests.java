/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package biz.paluch.logging.gelf;

import biz.paluch.logging.gelf.log4j2.GelfDynamicMdcFieldType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Thomas Herzog
 */
public class GelfDynamicMdcFieldTypeUnitTests {

    @Test
    void testWithNullRegex() {
        // -- Given --
        final String regex = null;
        final String type = "String";

        // -- When --
        final GelfDynamicMdcFieldType fieldType = GelfDynamicMdcFieldType.createField(null, regex, type);

        // -- Then --
        assertThat(fieldType).isNull();
    }

    @Test
    void testWithNullType() {
        // -- Given --
        final String regex = ".*";
        final String type = null;

        // -- When --
        final GelfDynamicMdcFieldType fieldType = GelfDynamicMdcFieldType.createField(null, regex, type);

        // -- Then --
        assertThat(fieldType).isNull();
    }

    @Test
    void testWithInvalidRegex() {
        // -- Given --
        final String regex = "*";
        final String type = "String";

        // -- When --
        final GelfDynamicMdcFieldType fieldType = GelfDynamicMdcFieldType.createField(null, regex, type);

        // -- Then --
        assertThat(fieldType).isNull();
    }

    @Test
    void testWithValidRegexAndType() {
        // -- Given --
        final String regex = ".*";
        final String type = "String";

        // -- When --
        final GelfDynamicMdcFieldType fieldType = GelfDynamicMdcFieldType.createField(null, regex, type);

        // -- Then --
        assertThat(fieldType).isNotNull();
        assertThat(fieldType.getRegex()).isEqualTo(regex);
        assertThat(fieldType.getType()).isEqualTo(type);
    }
}
