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

private[newman] object Utilities {

    def ignoreException(fun: => Unit) : Unit = {
        try {
            fun
        } catch {
            case _ => None
        }
    }

    def forceClose(resource: Closeable) {
        ignoreException { resource.close() }
    }

    def forceClose(resource: {def close(): Any}) = {
        ignoreException { resource.close() }
    }
}
