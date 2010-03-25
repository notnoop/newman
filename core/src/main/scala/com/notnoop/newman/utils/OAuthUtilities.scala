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

import com.google.gdata.client.authn.oauth._
import com.notnoop.newman.OAuthAccount

object OAuthUtilities {
    implicit def oauthAccountToRichAccount(x: OAuthAccount) = new RichAccount(x)

    class RichAccount(x: OAuthAccount) {
        def plainIR(implicit signer: XOAuthSigner) = signer.plainRequest(x.email, x.oauthToken, x.oauthSecret)
    }
}

class XOAuthSigner(consumerKey: String, consumerSecret: String) {
    val METHOD = "GET"

    def oauthURL(email: String) = "https://mail.google.com/mail/b/" + email + "/imap/"

    def parameters(email: String, token: String, tokenSecret: String) : OAuthParameters = {
        val params = new OAuthParameters()

        params.setOAuthConsumerKey(consumerKey)
        params.setOAuthConsumerSecret(consumerSecret)
        params.setOAuthToken(token)
        params.setOAuthTokenSecret(tokenSecret)
        return params
    }

    def sign(email: String, params: OAuthParameters) {
        // Sign the request
        val url = oauthURL(email)
        addCommonRequestParameters(url, METHOD, params, new OAuthHmacSha1Signer())
    }

    def plainRequest(email: String, token: String, tokenSecret: String): String = {
        val params = parameters(email, token, tokenSecret)
        sign(email, params)
        val hdr = header(METHOD, oauthURL(email), params)
        return hdr
    }

    def addCommonRequestParameters(baseUrl: String, httpMethod: String,
            parameters: OAuthParameters, signer: OAuthSigner) {
        // add the signature method if it doesn't already exist.
        if (parameters.getOAuthSignatureMethod().length() == 0) {
            parameters.setOAuthSignatureMethod(signer.getSignatureMethod())
        }

        // add the nonce if it doesn't already exist.
        if (parameters.getOAuthTimestamp().length() == 0) {
            parameters.setOAuthTimestamp(OAuthUtil.getTimestamp())
        }

        // add the timestamp if it doesn't already exist.
        if (parameters.getOAuthNonce().length() == 0) {
            parameters.setOAuthNonce(OAuthUtil.getNonce())
        }

        // add the signature if it doesn't already exist.
        // The signature is calculated by the {@link OAuthSigner}.
        if (parameters.getOAuthSignature().length() == 0) {
            val baseString = OAuthUtil.getSignatureBaseString(baseUrl, httpMethod,
                    parameters.getBaseParameters())
            parameters.setOAuthSignature(signer.getSignature(baseString, parameters))
        }
    }

    private[this] def fpair(key: String, value: String) =
        OAuthUtil.encode(key) + "=\"" +
        OAuthUtil.encode(value) + "\""

    private[this] def baseParam(map: java.util.Map[String, String]) : String = {
        val sb = new StringBuilder()

        for (pair <- new ScalaWrapper(map.entrySet))
            sb.append(',').append(fpair(pair.getKey, pair.getValue))

        return sb.toString
    }

    def header(method: String, url: String, params: OAuthParameters): String =
        method + " " + url + " " +
        fpair(OAuthParameters.OAUTH_SIGNATURE_KEY, params.getOAuthSignature) +
        baseParam(params.getBaseParameters)


}

private class ScalaWrapper[A](i:java.lang.Iterable[A]) {
    def foreach(f: A => Unit): Unit = {
        val iter = i.iterator
        while(iter.hasNext){
          f(iter.next)
        }
    }
}
