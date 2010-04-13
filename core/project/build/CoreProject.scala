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
import sbt._

class CoreProject(info: ProjectInfo) extends DefaultProject(info)
{
    // repositories
    val javamailRepo = "Oracle JavaMail repository" at "http://download.java.net/maven/2"
    val oauthRepo = "OAuth Library repository" at "http://oauth.googlecode.com/svn/code/maven"

    // snapshot repo
    val snapshotRepo = "Scala Snapshot Repo" at "http://www.scala-tools.org/repo-snapshots/"

    // dependencies
    val javamail = "javax.mail" % "mail" % "1.4.1"
    val oauth = "net.oauth.core" % "oauth" % "20090531"

    val scalatest = buildScalaVersion match {
        case "2.7.7" => "org.scalatest" % "scalatest" % "1.0" % "test"
        case "2.8.0.Beta1" => "org.scalatest" % "scalatest" % "1.0.1-for-scala-2.8.0.Beta1-SNAPSHOT" % "test"
        case x => error("Unsupported Scala version " + x)
    }
}

