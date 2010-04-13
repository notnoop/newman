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

package com.notnoop.newman.rules.criterias

import javax.mail.Message
import javax.mail.Part
import javax.mail.Multipart
import javax.mail.BodyPart
import javax.mail.search.BodyTerm

trait Criteria {
  def apply(m: Message): Boolean
}

case class Not(c: Criteria) extends Criteria {
  def apply(m: Message) = !c(m)
}

case class And(cs: Criteria*) extends Criteria {
  def apply(m: Message) = cs.forall(c => c(m))
}

case class Or(cs: Criteria*) extends Criteria {
  def apply(m: Message) = cs.exists(c => c(m))
}

case class From(person: String) extends Criteria {
  def apply(m: Message) = m.getFrom().exists(_.toString.indexOf(person) >= 0)
}

case class Subject(clause: String) extends Criteria {
  def apply(m: Message) = m.getSubject().indexOf(clause) >= 0
}

case class Body(clause: String) extends Criteria {
  def apply(m: Message) = m.`match`(new BodyTerm(clause))
}

case class Recepient(person: String) extends Criteria {
  def apply(m: Message) =
    m.getAllRecipients().exists(_.toString.indexOf(person) >= 0)
}

case class HasAttachment() extends Criteria {
  def apply(m: Message) : Boolean = {
    val content = m.getContent
    content match {
      case s: String => false
      case mp: Multipart =>
        for (i <- 0 to mp.getCount()) {
          val bp = mp.getBodyPart(i)
          if (Part.ATTACHMENT.equalsIgnoreCase(bp.getDisposition()))
            return true
        }
        false
      case _ => false
    }
  }
}

case class HasHeader(headerName: String, pattern: String) extends Criteria {
  def apply(m: Message) : Boolean = {
    m.getHeader(headerName) match {
      case null => false
      case a: Array[String] => a.exists(_.indexOf(pattern) >= 0)
    }
  }
}

case class MailingList(email: String) extends Criteria {
  private[this] val matcher = HasHeader("List-ID", email)
  def apply(m: Message) = matcher(m)
}

