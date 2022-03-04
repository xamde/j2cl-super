How to deal in j2cl with the absence of super-source.
j2cl has no built-in concept of **super-sources**, just sources.

Using `@GwtIncompatible` you can write shared code to run on the client and aub-class it with more performant or more feature-rich JVM-only implementations.
However, you cannot remove any field or method for the JVM version.

I am big fan of **shared code**. 
For me, shared code is a JAR on which I can depend from server-side code in pom.xml and use it.
Or I can depend on it from j2cl-client-side code and use the same library.
Isn't the beauty of j2cl to have business logic being tested to death in expressive JUnit tests on the server-side and later be used on the client-side? 
In GWT this was nicely supported via super-source mechanism.

## Example
Assume I write a math-library, for being used on client and server side.
I am fully aware of j2cl and want to create a compatible library.
On the server-side I'd like to use [slf4j-api](https://github.com/qos-ch/slf4j/tree/master/slf4j-api) for logging.
But when running on the client, I need to super-source the slf4j-api and use a compatible replacement.

NOTE: Deep down, the server side will use `System.out.print` as a fall-back and the client side will use `Console.log` instead.
A good example is straight from the [bazel docs on super-sourcing in j2cl](https://github.com/google/j2cl/blob/master/docs/best-practices.md).
So somewhere I need to realize this "switch".

The best way I could come up with is:

- Fork `slf4j-api` code and rewrite/annotate for j2cl -> [slf4j2cl](https://github.com/xamde/slf4j2cl)  
- `math-lib` uses `slf4j2cl` on the client-side
- Users of `math-lib` on the JVM/server-side need to manually exclude `slf4j2cl` in the `pom.xml` and include the real `slf4j-api` instead

```
   <dependencies>
        <dependency>
		    <artifactId>math-lib</artifactId>
            <exclusions>
                <exclusion>
	                <!-- we want the real deal on JVM, not this j2cl replacement -->
                    <groupId>de.xam.j2cl</groupId>
                    <artifactId>slf4j2cl</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
           <!-- to show j2cl an slf4j api & impl for the client side -->
	       <groupId>org.slf4j</groupId>
           <artifactId>slf4j-api</artifactId>
           <version>1.7.36</version>
        </dependency>
	</dependencies>    
```

Issues:

- Users of `math-lib` need know-how and which dependencies of if to replace with what -- knowledge that would better be kept in math-lib
- If the dependency chain gets deeper, every client-side consumer of any dependency using math-lib needs to add the right exclusion & replacement dependencies.

Is there a more elegant (hint: super-source?) way to achieve this goal?
Maybe putting some extra info in the pom of math-lib that `slf4j2cl` dependency should be replaced with  `slf4j-api` dependency?

Maybe these remarks from the j2cl-m-p can be generalized for public use
```
[INFO] --- j2cl-maven-plugin:0.19-SNAPSHOT:build (default-cli) @ client-app ---
[INFO] Removing dependency com.google.jsinterop:base:jar:1.0.0:compile, replacing with com.vertispan.jsinterop:base:jar:1.0.0-SNAPSHOT
```

## This Repository
- `math-lib` -- a fictive shared library (math is a good example for shared code)
- `server-app` -- a plain old server app using math-lib and log-api
- `client-app` -- a j2cl app using math-lib