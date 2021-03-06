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

import com.notnoop.newman.utils.EmailUtilities

import javax.mail.Message
import javax.mail.Flags.Flag

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.UsernamePasswordCredentials
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.auth.AuthScope
import scala.io.Source

trait Action {
  def apply(x: Message): Unit
}

case class Delete() extends Action {
  def apply(x: Message) = x.setFlag(Flag.DELETED, true)
}

case class MarkAsRead() extends Action {
  def apply(x: Message) = x.setFlag(Flag.SEEN, true)
}

case class MarkAsSpam() extends Action {
  def apply(x: Message) = { throw new RuntimeException("Not implemented yet") }
}

case class Archive() extends Action {
  def apply(x: Message) = { throw new RuntimeException("Not implemented yet") }
}

case class markAsStarred() extends Action {
  def apply(x: Message) = { throw new RuntimeException("Not Implemented yet") }
}

case class ApplyLabel(name: String) extends Action {
  def apply(x: Message) = { throw new RuntimeException("Not Implemented yet") }
}

case class Notifo(username: String, apiKey: String) extends Action {
  val requestURL = "https://api.notifo.com/v1/send_notification"

  def pushMessage(title: String, message: String) = {
    val data = Map("label"->"Newman", "title"->title, "msg"->message)

    val client = new HttpClient
    client.getParams().setAuthenticationPreemptive(true);
    val defaultcreds = new UsernamePasswordCredentials(username, apiKey)
    client.getState().setCredentials(AuthScope.ANY, defaultcreds)


    val post = new PostMethod(requestURL)
    data.foreach(x => post.addParameter(x._1, x._2))

    val i = client.executeMethod(post)
    println(i)

    post.getResponseBodyAsString
  }

  def apply(m: Message) = {
      pushMessage(EmailUtilities.fromOf(m) + ":" + m.getSubject(),
        EmailUtilities.textBodyOf(m).getOrElse("[No Text]"))
  }
}

