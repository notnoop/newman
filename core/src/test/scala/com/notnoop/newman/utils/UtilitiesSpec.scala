/**
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
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import scala.collection.mutable.Stack

class StackSpec extends Spec with ShouldMatchers {

    describe("Ignore Exceptions") {
        it("should suppress exceptions") {
            ignoreException {
                val a = null
                a.toString
            }
        }

         it("should actually execute") {
             var (a, b) = (1, 2)
             ignoreException {
                 a = 2
                 val random = 2 / 0
                 b = 1
             }
             a should be(2)
             b should be(2)
         }
     }
}
