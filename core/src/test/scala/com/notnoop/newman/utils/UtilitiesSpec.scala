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
