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

import java.net.URL

/**
 * Represents an email account that contains all the info necessarily
 * to login to the IMAP service
 */
sealed abstract class Account {
    val email: String
    var protocol: String
    var mailServer: String
    var folder: String

    override def hashCode() = 31 + email.hashCode()

    override def equals(that: Any) = that match {
        case other: Account => this.email == other.email
        case _ => false
    }
}

case class PasswordAccount(
    email: String,
    var password: String,
    var protocol: String,
    var mailServer: String,
    var folder: String
) extends Account

case class OAuthAccount(
    override val email: String,
    var oauthToken: String,
    var oauthSecret: String,
    var protocol: String,
    var mailServer: String,
    var folder: String
) extends Account

object AccountFactory {
    def GmailPasswordAccount(email: String, password: String, folder: String)
        = PasswordAccount(email, password, "imaps", "imap.gmail.com", folder)

    def GmailOAuthAccount(email: String, oauthToken: String,
                          oauthSecret: String, folder: String)
        = OAuthAccount(email, oauthToken, oauthSecret, "imaps", "216.239.59.109", folder)
}
