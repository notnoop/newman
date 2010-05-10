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

import org.specs._
import org.specs.mock.MockitoStubs

object MessageFactory extends MockitoStubs {

    def msgFrom(from: String*) = {
        val m = mock[Message]
        val fromAddr: Array[Address] = from.map(new InternetAddress(_)).toArray
        m.getFrom returns fromAddr
        m
    }
}

class CriteriasSpec extends Specification {

  "From criteria" should {
    val from = From("test@example.com")
    import MessageFactory._

    "handle empty froms" in {
      from(msgFrom()) must be(false)
    }

    "ignore other froms" in {
      from(msgFrom("other@example.com")) must be(false)
    }

    "match expected froms" in {
      from(msgFrom("test@example.com")) must be(true)
    }

    "handle cases with name specified" in {
      from(msgFrom("What <other@example.com>")) must be(false)
      from(msgFrom("What <test@example.com>")) must be(true)
    }

    "matches array if contains one of expected" in {
      from(msgFrom("o@ex.com", "what@ex.com", "test@example.com")) must be(true)
    }

    "ignore array if doesn't contain any of specified froms" in {
      from(msgFrom("o@ex.com", "what@ex.com")) must be(false)
    }
  }
}

