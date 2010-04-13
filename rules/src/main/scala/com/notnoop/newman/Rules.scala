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
import javax.mail.Folder
import javax.mail.Message

class RuleListener(rules: List[Rule]) extends AccountListener {
    def accountMonitored(f: Folder) = {}
    def invalidAuthentication(e: AuthenticationFailedException) = {}
    def accountDisabled(e: Exception) = {}
    def messagesRemoved(e: MessageCountEvent) = {}

    def messagesAdded(e: MessageCountEvent) = {
        for (msg <- e.getMessages) {
            for (rule <- rules) {
                rule(msg)
            }
        }
    }
}

trait Rule {
    def apply(m: Message)
}

