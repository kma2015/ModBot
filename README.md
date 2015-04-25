# ModBot
An IRC bot whose main goal is to keep the peace. Can also be modified to do other cool things!

**How to Use**

Just pull the source, configure libraries, and start coding! ModBot stores login info (login.txt) and the database (database.txt) in %USER_HOME%/ModBot.

You don't have to worry about database.txt, it is generated and written to automatically. But in order to test your code on a server, you need to configure login.txt. In ModBot's folder (it will tell you what that is on startup), create the login.txt file and add:

    username=example
    password=example
    server_ip=example
    port=example
    channel=#example

Set the values to whatever you want, then run ModBot. It should successfully connect with the login information.

**Libraries**

* PircBot

**Documentation**

Javadoc is included in the source code. If you still are confused as to the code's layout and how to use it, just ask me and I'll explain it.
