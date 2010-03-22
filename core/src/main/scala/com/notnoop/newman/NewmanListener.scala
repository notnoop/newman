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

import com.sun.mail.imap.IMAPFolder
import javax.mail._

import com.notnoop.newman.utils.XOAuthSigner
import com.notnoop.newman.utils.OAuthUtilities._

import scala.actors.Actor

/**
 * Monitors the IMAP Account and notifies the listener of actions
 */
abstract class NewmanListener {
    /**
     * The account to be monitored
     */
    val account: Account

    /**
     * The Account listener (or Actor) instance to be notified with new events
     */
    val listener: AccountListener

    /**
     * The signer to be used with XOAuth accounts
     */
    val signer : Option[XOAuthSigner] = None

    private var store: Store = _
    private var folder: Folder = _

    /**
     * Log in to the account
     */
    def login() : Store = {
        val props = System.getProperties

        if (account.isInstanceOf[OAuthAccount]) {
            assert(signer.isDefined)
            val p = account.protocol
            props.setProperty("mail." + p + ".sasl.enable", "true");
            props.setProperty("mail." + p + ".sasl.mechanisms", "XOAUTH");
            props.setProperty("newman.ir", account.asInstanceOf[OAuthAccount].encodedIR(signer.get));
        }

        val session = Session.getInstance(props, null)
        val store = session.getStore(account.protocol)
        account match {
            case pa : PasswordAccount => store.connect(pa.mailServer, pa.email, pa.password)
            case oa : OAuthAccount => store.connect(oa.mailServer, oa.oauthToken, oa.oauthSecret)
        }

        return store
    }

    private[this] def openFolder(s: Store, f: String) : Folder = {
        folder = NewmanUtilities.localizedOf(store, f)
        folder.open(Folder.READ_ONLY)
        return folder
    }

    /**
     * Start the monitoring activity
     */
    def monitor() {
        store = login()
        folder = openFolder(store, account.folder)
        folder.addMessageCountListener(listener)
        listener.accountMonitored(folder)

        while (true)
            try {
                folder.asInstanceOf[IMAPFolder].idle()
            } catch {
                case e: NullPointerException => None
            }
    }

}

private[newman] object NewmanUtilities {
    def withPrefix(a : String*) : List[String] = a.toList.map(e => List("[Gmail]/" + e, "[Google Mail]/"+e)).flatten

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
