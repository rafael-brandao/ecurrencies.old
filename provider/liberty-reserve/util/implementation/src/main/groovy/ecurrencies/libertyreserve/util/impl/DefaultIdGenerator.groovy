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

import java.text.DecimalFormat
import java.text.NumberFormat

import ecurrencies.libertyreserve.util.IdGenerator

/**
 * Default Implementation that receives a NumberFormat object to create a String
 * that contains the current UTC time in milliseconds
 *
 * @author Rafael de Andrade
 */
class DefaultIdGenerator implements IdGenerator {

    private static final String ID_NUMBER_PATTERN = "00000000000000000000"

    private final NumberFormat numberFormat

    DefaultIdGenerator() {
        this.numberFormat = new DecimalFormat(ID_NUMBER_PATTERN)
    }

    @Override
    String createId() {
        numberFormat.format(System.currentTimeMillis())
    }
}
