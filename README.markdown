Newman is an email library for monitoring IMAP emails and firing actions
as soon as emails arrive.

Sample Uses
------------------
  * Sending an Notification for new emails
  * Filtering new emails
  * Automatically replying to emails
  * Analyzing incoming emails

Sample Use
------------------

To start monitoring an account, you can script the following:

    import com.notnoop.newman._

    val listener = new NewmanListener {
        val account = GmailAccount("email", "password")
        val listener = loopReactListener {
            case MessageAddedEvent(e) => println("New email! " + e)
            case _ => // Do nothing!
        }
    }
    listener.monitor()

That's it!

Next Items
------------------

The following features are in the road map

  * Integrate with Growl, notifo, and notify.io notification services
  * Integrate with XMPP
  * Provide sample code for sending iPhone and Android push notifications
  * Allow for more sources (e.g. buzz, twitter)

This library is in its very alpha stage at the moment, but ultimately,
the library should allow for the following:

    val listener = new NewmanListener {
        val classifier = new EmailClassifier()
        val phone = new SMSSender("646123456")

        val account = GmailAccount("email", "password")
        val listener = RuleListener(
            From("Seinfeld") -> MarkAsSpam(),
            classifier.Critical() -> And(LabelAs("Critical"), phone.sendSms()),
            Subject("broccoli") -> Ignore()
        )
    }
    listener.monitor()

How to Contribute
------------------------------------

Please feel free to fork the project and implement your new beloved
features, and I would pull from you.  You can also email me with new ideas
too.
