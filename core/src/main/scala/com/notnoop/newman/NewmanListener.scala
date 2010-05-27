/*
 * Copyright 2010 Mahmood Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.notnoop.newman

import com.sun.mail.imap.IMAPFolder
import javax.mail._
import java.security._
import java.util.{Timer, TimerTask}

import org.slf4j.{Logger, LoggerFactory}

import com.notnoop.newman.utils.XOAuthConsumer
import com.notnoop.newman.utils.XOAuthSaslProvider
import com.notnoop.newman.utils.XOAuthSaslProvider._

import scala.actors.Actor

/**
 * Monitors the IMAP Account and notifies the listener of actions
 */
abstract class NewmanListener {
    val logger = LoggerFactory.getLogger(getClass)

    /**
     * The account to be monitored
     */
    val account: Account

    /**
     * The Account listener (or Actor) instance to be notified with new events
     */
    val listener: AccountListener

    /**
     * The consumer used with XOauth accounts
     */
     val oauthConsumer: Option[XOAuthConsumer] = None

    private var store: Store = _
    private var folder: Folder = _

    /**
     * Log in to the account
     */
    def login() : Store = {
        val props = System.getProperties

        if (account.isInstanceOf[OAuthAccount]) {
            assert(oauthConsumer.isDefined)
            val p = account.protocol
            props.setProperty("mail." + p + ".sasl.enable", "true");
            props.setProperty("mail." + p + ".sasl.mechanisms", "XOAUTH");

            // xoauth implementation specific
            Security.addProvider(new XOAuthSaslProvider())
            val oa = account.asInstanceOf[OAuthAccount]
            props.put(XOAUTH_CONSUMER_PROP, oauthConsumer.get)
            props.setProperty(XOAUTH_EMAIL_PROP, oa.email)
            props.setProperty(XOAUTH_TOKEN_PROP, oa.oauthToken)
            props.setProperty(XOAUTH_TOKEN_SECRET_PROP, oa.oauthSecret)
        }

        val session = Session.getInstance(props, null)
        val store = session.getStore(account.protocol)
        account match {
            case pa : PasswordAccount =>
                store.connect(pa.mailServer, pa.email, pa.password)
            case oa : OAuthAccount =>
                store.connect(oa.mailServer, oa.oauthToken, oa.oauthSecret)
        }

        return store
    }

    private[this] def openFolder(s: Store, f: String) : Folder = {
        folder = NewmanUtilities.localizedOf(store, f)
        folder.open(Folder.READ_ONLY)
        return folder
    }

    def monitor() {
        logger.debug("Start monitoring account: {}", account.email)
        store = login()
        folder = openFolder(store, account.folder)
        folder.addMessageCountListener(listener)
        listener.accountMonitored(folder)

        schedulePing()

        logger.debug("Logged in and started")
        while (true)
            try {
                folder.asInstanceOf[IMAPFolder].idle()
            } catch {
                case e: NullPointerException => None
            }
    }

    def start() {
        var shouldContinue = true
        while (shouldContinue)
            try {
            monitor()
          } catch {
            case e: FolderClosedException => // nothing
              logger.debug("Folder was closed, restarting...")
            case e =>
              logger.warn("Unexpected error, e")
              shouldContinue = false
        }
        logger.debug("Stopped account monitoring")
    }

    def loopReactListener(f : PartialFunction[Any, Unit]) =
        ActorUtils.loopReactListener(f)

    val timer = new Timer("Ping(" + account.email + ")")
    def schedulePing() = {
        object PingTask extends TimerTask {
          override def run() {
            logger.debug("Sending ping")
            if (folder != null) {
              val n = folder.getMessageCount()
              logger.debug("Folder has {} messages", n)
            }
          }
        }

        timer.schedule(PingTask, 0, 20 * 1000)
    }
}

private[newman] object NewmanUtilities {
    def withPrefix(a : String*) : List[String] =
      a.toList.map(e => List("[Gmail]/" + e, "[Google Mail]/"+e)).flatten

    val folderLocalization: Map[String, List[String]] =
        Map("Inbox" -> List("Inbox", "INBOX"),
            "[Gmail]/All Mail" -> withPrefix("All Mail", "ŸÉŸÑ ÿßŸÑÿ®ÿ±ŸäÿØ"))

    def localizedOf(store: Store, folderName: String) : Folder = {
        val f = store.getFolder(folderName)
        if (f.exists) return f

        for (
            l <- folderLocalization(folderName);
            m = store.getFolder(l)
            if m.exists
            ) return m

        return f
    }
}
