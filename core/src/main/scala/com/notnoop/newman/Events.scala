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
/**
 * The parent class of all the events generated by the library
 */
sealed abstract case class NewmanEvent()

/**
 * Represents an event where the passed folder is about to be monitored
 *
 * @param f the folder being monitored
 */
case class BeginMonitorEvent(f: Folder) extends NewmanEvent

/**
 * Represents an event where monitoring the account has failed due to
 * the passed authentication error
 *
 * @param e     the authentication exception
 */
case class InvalidAuthenticationEvent(e: AuthenticationFailedException)
    extends NewmanEvent

/**
 * Represents an event where the library disables monitoring the account,
 * for reasons other than authentication failures, e.g. due to connectivity
 * issues
 *
 * @param e     the Exception that was thrown to connect it
 */
case class AccountDisabledEvent(e: Exception) extends NewmanEvent

/**
 * Represents an event where a new email was received
 *
 * @param   e   the new received email
 */
case class MessageAddedEvent(e: Message) extends NewmanEvent

/**
 * Represents an event where an email was deleted from the folder
 *
 * @param e     the deleted email
 */
case class MessageRemovedEvent(e: Message) extends NewmanEvent

// Actual listener
private[newman] class NewmanEventListener(a: Actor) extends AccountListener {
    def accountMonitored(f: Folder) = a ! BeginMonitorEvent(f)
    def invalidAuthentication(e: AuthenticationFailedException) =
        a ! InvalidAuthenticationEvent(e)

    def accountDisabled(e: Exception) = a ! AccountDisabledEvent(e)

    def messagesAdded(e: MessageCountEvent) =
        e.getMessages.foreach{ a ! MessageAddedEvent(_) }

    def messagesRemoved(e: MessageCountEvent) =
        e.getMessages.foreach {a ! MessageRemovedEvent(_) }
}

/**
 * Provides utilities classes for supporting interaction between actors
 * and message listeners
 */
object ActorUtils {
    /**
     * An implicit conversion method to convert an actor to an Email account
     * listener
     */
    implicit def acorToAccounListener(e: Actor) : AccountListener =
        new NewmanEventListener(e)

    /**
     * This is a factory method for creating actors.
     *
     * <p>The following example demonstrates its usage:
     *
     * <pre><code>
     * ...
     * val a = loopReact {
     *     ...
     * }
     * </code></pre>
     *
     * which is equivalent to
     *
     * <pre><code>
     * import scala.actors.Actor._
     * ...
     * val a = actor {
     *     loop {
     *         react {
     *             ....
     *         }
     *     }
     * }
     * ...
     * </code></pre>
     *
     * @param a partial function with message patterns and actions
     * @return the newly created actor. Note that it is automatically started
     */
    def loopReact(f : PartialFunction[Any, Unit]) : Actor =
        actor {
            loop {
                react {
                    f
                }
            }
        }

    /**
     * This is a factory method for creating account listeners with
     * actor syntax
     *
     * @param a partial function with message patterns and actions
     * @return the newly created Account listener
     */
    def loopReactListener(f : PartialFunction[Any, Unit]) : AccountListener =
        loopReact(f)
}
