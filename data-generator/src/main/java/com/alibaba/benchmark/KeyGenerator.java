/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.benchmark;

public class KeyGenerator {

    private KeyGenerator() {
    }

    private static final String BUSINESS_KEY_CHARS = "ABCDEFGHIJKLMNOP"; // 16 possible characters for the key

    public static String makeKey(long primary) {
        String keyPart1 = longTo8CharString(primary >> 32); // create string based on the upper 32 bits
        String keyPart2 = longTo8CharString(primary); // create string based on the lower 32 bits
        String s = keyPart1 + keyPart2;
        // make a longer string
        StringBuilder buf = new StringBuilder(128);
        for (int i = 0; i < 20; i++) {
            buf.append(s);
        }
        return buf.toString();
    }

    private static String longTo8CharString(long value) {
        StringBuilder builder = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            int charIndex = (int)(value & 0xF); // The lower 4 bits of value, a number 0-15
            builder.append(BUSINESS_KEY_CHARS.charAt(charIndex));
            value >>= 4;
        }
        return builder.toString();
    }
}
