Hi!

Use the `h2-maven-plugin` like this:

    mvn com.edugility:h2-maven-plugin:1.0:spawn

That starts a new H2 TCP server.  Then:

    mvn com.edugility:h2-maven-plugin:1.0:stop

...will kill it gracefully.
