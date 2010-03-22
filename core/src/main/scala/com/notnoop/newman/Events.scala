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
package com.notnoop.newman

import javax.mail.AuthenticationFailedException
import javax.mail.event.MessageCountEvent
import javax.mail.event.MessageCountListener
import javax.mail.Folder
import javax.mail.Message

import scala.actors.Actor
import scala.actors.Actor._

// messages
sealed abstract case class NewmanEvent()

case class BeginMonitorEvent(f: Folder) extends NewmanEvent
case class InvalidAuthenticationEvent(e: AuthenticationFailedException)
    extends NewmanEvent
case class AccountDisabledEvent(e: Exception) extends NewmanEvent

case class MessageAddedEvent(e: Message) extends NewmanEvent
case class MessageRemovedEvent(e: Message) extends NewmanEvent

// Actual listener
class NewmanEventListener(a: Actor) extends AccountListener {
    def accountMonitored(f: Folder) = a ! BeginMonitorEvent(f)
    def invalidAuthentication(e: AuthenticationFailedException) =
        a ! InvalidAuthenticationEvent(e)

    def accountDisabled(e: Exception) = a ! AccountDisabledEvent(e)

    def messagesAdded(e: MessageCountEvent) =
        e.getMessages.foreach{ a ! MessageAddedEvent(_) }

    def messagesRemoved(e: MessageCountEvent) =
        e.getMessages.foreach {a ! MessageRemovedEvent(_) }
}

object ActorUtils {
    implicit def acorToAccounListener(e: Actor) = new NewmanEventListener(e)
    
    def loopReact(f : PartialFunction[Any, Unit]) : Actor = {
        val a = actor {
            loop {
                react {
                    f
                }
            }
        }
        a.start
        return a
    }

    def loopReactListener(f : PartialFunction[Any, Unit]) : AccountListener =
        return loopReact(f)
}
