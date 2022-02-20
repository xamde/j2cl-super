I just read a lengthy discussion from 2019 about super-source. Still, to me it is now clear how to realize the following use case.
I understood j2cl has no built-in concept of **super-sources**, just sources. 

I am big fan of **shared code**. 
For me, shared code is a JAR on which I can depend from server-side code in pom.xml and use it.
Or I can depend on it from j2cl-client-side code and use the same library.
Isn't the beauty of j2cl to have business logic being tested to death in expressive JUnit tests on the server-side and later be used on the client-side? In GWT this was nicely supported via super-source mechanism.

## Example
Assume I write a math-library, for being used on client and server side.
I am fully aware of j2cl and want to create a compatible library.
On the server-side I'd like to use slf4j-api.
But when running on the client, I need to super-source the slf4j-api and use a compatible replacement.

Note: Deep down, the server side will use System.out.print as a fall-back and the client side will use Console.log instead.
A good example is straight from the bazel docs on super-sourcing in j2cl: https://github.com/google/j2cl/blob/master/docs/best-practices.md
So somewhere I need to realize this "switch".

The best way I could come up with is:

- `math-lib` uses `slf4j` (carefully using only some parts of it).
- And I create a `sl4fj-j2cl-lib` which is used on the client side.

A server-side app simply depends on `math-lib`, which transitively depends on `slf4j`. 
Slf4j in turn uses server-side only code, but everything runs fine on the server-side (javac).

Now a client-side j2cl-app using math-lib needs these dependencies

```
   <dependencies>
        <dependency>
		    <artifactId>math-lib</artifactId>
            <exclusions>
                <exclusion>
	                <!-- to hide incompatible code from j2cl -->
                    <artifactId>slf4j-api</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <!-- to show j2cl an slf4j api & impl for the client side -->
		    <artifactId>sl4fj-j2cl-lib</artifactId>
        </dependency>
	</dependencies>    

```

Issues:

- Users of `math-lib` need know-how and which dependencies of if to replace with what -- knowledge that would better be kept in math-lib
- The lib `sl4fj-j2cl-lib` needs to duplicate the slf4j API sources (the compatible parts).
- If the dependency chain gets deeper, every client-side consumer of any dependency using math-lib needs to add the right exclusion & replacement dependencies.

Is there a more elegant (hint: super-source?) way to achieve this goal?
Maybe putting some extra info in the pom of math-lib that `slf4j-api` dependency should be replaced with  `sl4fj-j2cl-lib` dependency?

Maybe these remarks from the j2cl-m-p can be generalized for public use
```
[INFO] --- j2cl-maven-plugin:0.19-SNAPSHOT:build (default-cli) @ client-app ---
[INFO] Removing dependency com.google.jsinterop:base:jar:1.0.0:compile, replacing with com.vertispan.jsinterop:base:jar:1.0.0-SNAPSHOT
```

## This Repository
- `log-api` -- simplistic log API (mentally put slf4j here)
- `log-impl-j2cl` -- a 'rewrite' of the log API which is j2cl compatible (slf4j API copied, deleted complex parts, elemental2-ified some base parts)
- `math-lib` -- a shared library
- `server-app` -- a plain old server app using math-lib and log-api
- `client-app` -- a j2cl app using math-lib

