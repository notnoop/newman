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
package com.notnoop.newman

import javax.mail.AuthenticationFailedException
import javax.mail.event.MessageCountEvent
import javax.mail.event.MessageCountListener
import javax.mail.Folder

trait AccountListener extends MessageCountListener {
    def accountMonitored(f: Folder) : Unit
    def invalidAuthentication(e: AuthenticationFailedException) : Unit
    def accountDisabled(e: Exception) : Unit

    def messagesAdded(e: MessageCountEvent): Unit
    def messagesRemoved(e: MessageCountEvent): Unit
}

object EmptyAccountListener extends AccountListener {
    def accountMonitored(f: Folder) = {}
    def invalidAuthentication(e: AuthenticationFailedException) = {}
    def accountDisabled(e: Exception) = {}

    def messagesAdded(e: MessageCountEvent) = {}
    def messagesRemoved(e: MessageCountEvent) = {}
}

object SampleAccountListener extends AccountListener {
    def accountMonitored(f: Folder) = { println("monitoring " + f) }
    def invalidAuthentication(e: AuthenticationFailedException) = {}
    def accountDisabled(e: Exception) = { println("Good bye") }

    def messagesAdded(e: MessageCountEvent) = { println("new mail!") }
    def messagesRemoved(e: MessageCountEvent) = { println("bye mail!") }
}
