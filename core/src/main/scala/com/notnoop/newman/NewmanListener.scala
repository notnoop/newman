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

import com.sun.mail.imap.IMAPFolder
import javax.mail._
import com.notnoop.newman.utils.OAuthUtilities._

class NewmanListener(a: Account, listener: AccountListener) {
    var store: Store = _
    var folder: Folder = _

    def login() : Store = {
        val props = System.getProperties

        if (a.isInstanceOf[OAuthAccount]) {
            val p = a.protocol
            props.setProperty("mail." + p + ".sasl.enable", "true");
            props.setProperty("mail." + p + ".sasl.mechanisms", "XOAUTH");
            props.setProperty("newman.ir", a.asInstanceOf[OAuthAccount].encodedIR);
        }

        val session = Session.getInstance(props, null)
        val store = session.getStore(a.protocol)
        a match {
            case pa : PasswordAccount => store.connect(pa.mailServer, pa.email, pa.password)
            case oa : OAuthAccount => store.connect(oa.mailServer, oa.oauthToken, oa.oauthSecret)
        }

        return store
    }

    def openFolder(s: Store, f: String) : Folder = {
        folder = NewmanUtilities.localizedOf(store, f)
        folder.open(Folder.READ_ONLY)
        return folder
    }

    def close() = if (store != null) store.close

    def monitor() {
        store = login()
        folder = openFolder(store, a.folder)
        folder.addMessageCountListener(listener)
        listener.accountMonitored(folder)

        while (true)
            try {
                folder.asInstanceOf[IMAPFolder].idle()
            } catch {
                case e: NullPointerException => None
            }
    }

    def connectToSever() : Unit = {
        val store = login()
        store.connect(null, null, null)
    }
}

object NewmanUtilities {
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
