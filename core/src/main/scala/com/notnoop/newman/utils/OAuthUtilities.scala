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
package com.notnoop.newman.utils

import com.notnoop.newman.OAuthAccount

import net.oauth._

object OAuthUtilities {
    implicit def oauthAccountToRichAccount(x: OAuthAccount) = new RichAccount(x)

    class RichAccount(x: OAuthAccount) {
        def plainIR(implicit signer: OAuthResponseBuilder) =
            signer.plainRequest(x.email, x.oauthToken, x.oauthSecret)
    }
}

class OAuthResponseBuilder(consumerKey: String, consumerSecret: String) {
    val METHOD = "GET"
    val consumer = new OAuthConsumer("", consumerKey, consumerSecret, null)

    def oauthURL(email: String) =
        "https://mail.google.com/mail/b/" + email + "/imap/"

    def plainRequest(email: String, token: String, tokenSecret: String) = {
        val accessor = new OAuthAccessor(consumer)
        accessor.tokenSecret = tokenSecret

        val parameters = new java.util.HashMap[String, String]
        parameters.put(OAuth.OAUTH_SIGNATURE_METHOD, "HMAC-SHA1")
        parameters.put(OAuth.OAUTH_TOKEN, token)

        val msg = new OAuthMessage(METHOD, oauthURL(email),
            parameters.entrySet)
        msg.addRequiredParameters(accessor)

        header(METHOD, oauthURL(email), msg.getParameters)
    }

    private[this] def fpair(key: String, value: String) =
        OAuth.percentEncode(key) + "=\"" +
        OAuth.percentEncode(value) + "\""

    private[this] def baseParam(parameters:
    java.util.List[java.util.Map.Entry[String, String]]) = {
        val sb = new StringBuilder()

        for (pair <- new ScalaWrapper(parameters))
            sb.append(',').append(fpair(pair.getKey, pair.getValue))

        sb.toString
    }

    def header(method: String, url: String, params:
    java.util.List[java.util.Map.Entry[String, String]]) =
        method + " " + url + " " +
        baseParam(params)


}

private class ScalaWrapper[A](i:java.lang.Iterable[A]) {
    def foreach(f: A => Unit): Unit = {
        val iter = i.iterator
        while(iter.hasNext){
          f(iter.next)
        }
    }
}

