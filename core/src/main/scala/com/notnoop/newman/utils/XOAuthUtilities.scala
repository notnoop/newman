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

class XOAuthSaslProvider extends
Provider("gmail.oauth", 1, "XOAuth Implementation") {
    put("SaslClientFactory.XOAUTH", classOf[XOAuthSaslClientFactory].getCanonicalName())
}

object XOAuthSaslProvider {
    val XOAUTH_SIGNER_PROP = "xoauth.signer"
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
                props.get(XOAUTH_SIGNER_PROP).asInstanceOf[OAuthResponseBuilder],
                props.get(XOAUTH_EMAIL_PROP).toString,
                props.get(XOAUTH_TOKEN_PROP).toString,
                props.get(XOAUTH_TOKEN_SECRET_PROP).toString
                )

    def getMechanismNames(props: java.util.Map[String, _]) = Array("XOAUTH")
}

class XOAuthSaslClient(responseBuilder: OAuthResponseBuilder, email: String, token: String, secret: String)
extends SaslClient {
    override def getMechanismName() = "XOAUTH"
    override def evaluateChallenge(challenge: Array[Byte])
        = responseBuilder.plainRequest(email, token, secret).getBytes()

    override def hasInitialResponse() = true
    override def isComplete() = false
    override def unwrap(incoming: Array[Byte], offset: Int, len: Int) = null
    override def wrap(ooutgoing: Array[Byte], offset: Int, len: Int) = null
    override def getNegotiatedProperty(propName: String) = null
    override def dispose() {}
}

