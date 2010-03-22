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

import java.net.URL

/**
 * Represents an email account that contains all the info necessarily
 * to login to the IMAP service
 */
sealed abstract class Account {
    /** User email **/
    def email: String
    /** The protocol used to connect to the server, e.g. IMAP, POP */
    def protocol: String
    /** The hostname/IP-address of the mail server, e.g. imap.gmail.com */
    def mailServer: String
    /** The folder to be watched */
    def folder: String

    override def hashCode() = 31 + email.hashCode()

    override def equals(that: Any) = that match {
        case other: Account => this.email == other.email
        case _ => false
    }
}

/**
 * Represents an email account which gets authenticated with a password.
 * The password is in clear text
 */
case class PasswordAccount(
    email: String,
    password: String,
    protocol: String,
    mailServer: String,
    folder: String
) extends Account

/**
 * Represents an email account which gets authenticated via XOAuth protocol
 * as supported by Gmail.
 *
 * @see http://sites.google.com/site/oauthgoog/Home/oauthimap
 */
case class OAuthAccount(
    override val email: String,
    val oauthToken: String,
    val oauthSecret: String,
    val protocol: String,
    val mailServer: String,
    val folder: String
) extends Account

/**
 * Represents a Gmail Account, with the pre-defiend mail server
 */
case class GmailAccount(
    override val email: String,
    override val password: String,
    override val folder: String
) extends PasswordAccount(email, password, "imaps", "imap.gmail.com", folder)

case class GmailOAuthAccount(
    override val email: String,
    override val oauthToken: String,
    override val oauthSecret: String,
    override val folder: String
) extends OAuthAccount(email, oauthToken, oauthSecret, "imaps", "216.239.59.109", folder)
