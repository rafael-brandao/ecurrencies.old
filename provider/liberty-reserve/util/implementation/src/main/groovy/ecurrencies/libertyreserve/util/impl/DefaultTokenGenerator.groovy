/*    Copyright 2012 Rafael Brandão de Andrade
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ecurrencies.libertyreserve.util.impl

import static java.lang.System.currentTimeMillis
import static java.util.Arrays.fill

import java.security.MessageDigest

import org.joda.time.format.DateTimeFormatter

import ecurrencies.libertyreserve.util.TokenGenerator

/**
 * This is the default implementation.
 *
 * @author Rafael de Andrade
 */
class DefaultTokenGenerator implements TokenGenerator {

    private static final char COLON = ':'

    private final DateTimeFormatter dateTimeFormatter
    private final MessageDigest messageDigest

    DefaultTokenGenerator(DateTimeFormatter dateTimeFormatter, MessageDigest messageDigest) {
        this.dateTimeFormatter = dateTimeFormatter
        this.messageDigest = messageDigest
    }

    @Override
    String createToken(char[] apiSecurityWord) {
        createToken(apiSecurityWord, currentTimeMillis())
    }

    @Override
    String createToken(char[] apiSecurityWord,long timestamp) {

        byte[] token = null

        try {
            def date = dateTimeFormatter.print(timestamp).toCharArray() as char[]

            token = new byte[apiSecurityWord.length + date.length + 1]

            int i = 0
            apiSecurityWord.length.times { j ->
                token[i++] = (byte) apiSecurityWord[j]

            }
            token[i++] = (byte) COLON
            date.length.times { j ->
                token[i++] = (byte) date[j]
            }

            return BitConverter.toHexString(messageDigest.digest(token))

        } finally {
            apiSecurityWord?.eachWithIndex { it, i -> apiSecurityWord[i] = '0' }
            token?.eachWithIndex { it, i -> token[i] = ((byte) 0) }
        }
    }

    private static class BitConverter {
        private static final char[] HEX_CHARS = (['0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'] as char[])

        static String toHexString(byte[] b) {
            def sb = new StringBuffer(b.length * 2)
            b.each { byte element ->
                // look up high nibble char
                sb << HEX_CHARS[(element & 0xf0) >>> 4]
                // look up low nibble char
                sb << HEX_CHARS[element & 0x0f]
            }
            sb.toString()
        }
    }
}