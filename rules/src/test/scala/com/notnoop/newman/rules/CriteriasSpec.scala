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

package com.notnoop.newman.rules

import javax.mail.Message
import javax.mail.Address
import javax.mail.internet.InternetAddress

import org.scalatest.mock.MockitoSugar
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers

import org.mockito.Mockito._

class FromSpec extends Spec with ShouldMatchers with MockitoSugar {
    def messageFrom(from: Array[String]) = {
        val m = mock[Message]
        val fromAddr: Array[Address] = from.map(new InternetAddress(_))
        when(m.getFrom).thenReturn(fromAddr)
        m
    }

    describe("From criteria") {
        val from = From("test@example.com")

        def testFrom(msg: String, fromAddr: Array[String], expected: Boolean) =
            it("should handle " + msg) {
                val m = messageFrom(fromAddr)
                from(m) should be(expected)
            }

        testFrom("empty froms", Array(), false)
        testFrom("other from", Array("other@example.com"), false)
        testFrom("from target", Array("test@example.com"), true)

        testFrom("Sophisticated", Array("What <other@example.com>"), false)
        testFrom("True sophisticated", Array("What <test@example.com>"), true)

        testFrom("In Array false", Array("o@ex.com", "what@ex.com"), false)
        testFrom("In Array true", Array("o@ex.com", "test@example.com"), true)
    }
}

