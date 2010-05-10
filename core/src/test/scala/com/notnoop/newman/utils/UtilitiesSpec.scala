/*
 * Copyright 2010 Mahmood Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.notnoop.newman.utils

import com.notnoop.newman.utils.Utilities._

import org.specs._

class IgnoreExceptions extends Specification {

    "Ignore Exceptions" should {
        "should suppress exceptions" in {
            ignoreExceptions {
                val a = null
                a.toString
            }
            // ensure that this line is executed
            true must be(true)
        }

        "should actually execute" in {
            var a = 1
            ignoreExceptions {
                a = 2
            }
            a must be(2)
        }

        "should partially execute" in {
            var (a, b) = (1, 2)
            ignoreExceptions {
                a = 2
                val random = 2 / 0
                b = 1
            }
            a must be(2)
            b must be(2)
        }
    }

}
