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

import java.security._
import javax.security.auth.callback.CallbackHandler
import javax.security.sasl._

import net.oauth._

case class XOAuthConsumer(consumerKey: String, consumerSecret: String) {
    private[utils] val oauthConsumer = new OAuthConsumer("", consumerKey,
                                                      consumerSecret, null)
}

class XOAuthSaslProvider extends
Provider("gmail.oauth", 1, "XOAuth Implementation") {
    put("SaslClientFactory.XOAUTH", classOf[XOAuthSaslClientFactory].getCanonicalName())
}

object XOAuthSaslProvider {
    val XOAUTH_CONSUMER_PROP = "xoauth.consumer"
    val XOAUTH_EMAIL_PROP = "xoauth.email"
    val XOAUTH_TOKEN_PROP = "xoauth.token"
    val XOAUTH_TOKEN_SECRET_PROP = "xoauth.tokenSecret"
}

class XOAuthSaslClientFactory extends SaslClientFactory {

    import com.notnoop.newman.utils.XOAuthSaslProvider._
    def createSaslClient(mechanisms: Array[String],
        authorizeationId: String, protocol: String, serverName: String,
        props: java.util.Map[String, _], cbh: CallbackHandler) =
            new XOAuthSaslClient(
                props.get(XOAUTH_CONSUMER_PROP).asInstanceOf[XOAuthConsumer],
                props.get(XOAUTH_EMAIL_PROP).toString,
                props.get(XOAUTH_TOKEN_PROP).toString,
                props.get(XOAUTH_TOKEN_SECRET_PROP).toString
                )

    def getMechanismNames(props: java.util.Map[String, _]) = Array("XOAUTH")
}

class XOAuthSaslClient(consumer: XOAuthConsumer, email: String, token: String, secret: String)
extends SaslClient {
    override def getMechanismName() = "XOAUTH"
    override def evaluateChallenge(challenge: Array[Byte])
        = new OAuthResponseBuilder(consumer).plainRequest(email, token, secret).getBytes()

    override def hasInitialResponse() = true
    override def isComplete() = false
    override def unwrap(incoming: Array[Byte], offset: Int, len: Int) = null
    override def wrap(ooutgoing: Array[Byte], offset: Int, len: Int) = null
    override def getNegotiatedProperty(propName: String) = null
    override def dispose() {}
}

private[utils] class OAuthResponseBuilder(consumer: XOAuthConsumer) {
    val METHOD = "GET"

    def oauthURL(email: String) =
        "https://mail.google.com/mail/b/" + email + "/imap/"

    def plainRequest(email: String, token: String, tokenSecret: String) = {
        val accessor = new OAuthAccessor(consumer.oauthConsumer)
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

