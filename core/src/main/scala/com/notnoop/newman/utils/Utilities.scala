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

import java.io.Closeable

import javax.mail.{Message, Part, Multipart}

object Utilities {

    def ignoreExceptions(fun: => Unit) : Unit = {
        try {
            fun
        } catch {
            case _ => None
        }
    }

    def forceClose(resource: Closeable) =
        ignoreExceptions { resource.close() }

    def forceClose(resource: {def close(): Any}) =
        ignoreExceptions { resource.close() }

    def textBodyOf(p: Part) : Option[String] = {
        p.getContent match {
        case text: String =>
            if (p.getContentType.contains("TEXT/PLAIN")) Some(text) else None
        case mp: Multipart =>
            val count = mp.getCount
            for (i <- 0 until count) {
                val p = mp.getBodyPart(i)
                val text = textBodyOf(p)
                text match {
                case Some(_) => return text
                case _ => // do nothing
                }
            }
            return None
        case _ => None
        }
    }

    val fromPattern = "(.*)<.*>.*".r
    def fromOf(m: Message) = {
        m.getFrom.firstOption match {
            case None => "Unknown"
            case Some(from) =>
                from.toString match {
                    case fromPattern(name) => name
                    case _ => from
                }
        }
    }

}

