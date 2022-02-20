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

### Status

```
mvn clean install -pl log-api
mvn clean install -pl log-impl-j2cl
mvn clean install -pl math-lib
```
all run fine.
```
cd client-app
mvn clean j2cl:build
```
fails with
```
...
[INFO] Starting com.google.jsinterop:jsinterop-annotations:2.0.0/stripped_bytecode_headers
[INFO] Starting de.xam.example.log:log-api:0.1.0-SNAPSHOT/stripped_bytecode_headers
[INFO] Starting com.google.elemental2:elemental2-core:1.1.0/stripped_sources
[INFO] Finished com.google.elemental2:elemental2-core:1.1.0/stripped_sources in 170ms
[INFO] Starting de.xam.example.math:math-lib:0.1.0-SNAPSHOT/stripped_bytecode
[ERROR] de.xam.example.math:math-lib:0.1.0-SNAPSHOT/stripped_bytecode: /C:/_data_/_p_/_git/GitHub/j2cl-super/client-app/target/gwt3BuildCache/0.19-SNAPSHOT/de.xam.example.math-math-lib-0.1.0-SNAPSHOT/026f68a0c11cd044707d943b8dd286d4-stripped_sources/results
/org/example/math/MathUtil.java:3 package org.example.log does not exist
[ERROR] de.xam.example.math:math-lib:0.1.0-SNAPSHOT/stripped_bytecode: /C:/_data_/_p_/_git/GitHub/j2cl-super/client-app/target/gwt3BuildCache/0.19-SNAPSHOT/de.xam.example.math-math-lib-0.1.0-SNAPSHOT/026f68a0c11cd044707d943b8dd286d4-stripped_sources/results
/org/example/math/MathUtil.java:4 package org.example.log does not exist
[ERROR] de.xam.example.math:math-lib:0.1.0-SNAPSHOT/stripped_bytecode: /C:/_data_/_p_/_git/GitHub/j2cl-super/client-app/target/gwt3BuildCache/0.19-SNAPSHOT/de.xam.example.math-math-lib-0.1.0-SNAPSHOT/026f68a0c11cd044707d943b8dd286d4-stripped_sources/results
/org/example/math/MathUtil.java:8 cannot find symbol
  symbol:   class Log
  location: class org.example.math.MathUtil
[ERROR] de.xam.example.math:math-lib:0.1.0-SNAPSHOT/stripped_bytecode: /C:/_data_/_p_/_git/GitHub/j2cl-super/client-app/target/gwt3BuildCache/0.19-SNAPSHOT/de.xam.example.math-math-lib-0.1.0-SNAPSHOT/026f68a0c11cd044707d943b8dd286d4-stripped_sources/results
/org/example/math/MathUtil.java:8 cannot find symbol
  symbol:   class LogImpl
  location: class org.example.math.MathUtil
[INFO] Finished de.xam.example.math:math-lib:0.1.0-SNAPSHOT/stripped_bytecode in 805ms
[INFO] Starting de.xam.example.math:math-lib:0.1.0-SNAPSHOT/transpiled_js
[INFO] Finished com.vertispan.jsinterop:base:1.0.0-SNAPSHOT/stripped_bytecode in 1912ms
[INFO] Starting de.xam.example.math:math-lib:0.1.0-SNAPSHOT/stripped_bytecode_headers
[INFO] Starting com.vertispan.jsinterop:base:1.0.0-SNAPSHOT/stripped_bytecode_headers
[ERROR] de.xam.example.math:math-lib:0.1.0-SNAPSHOT/transpiled_js: Error:C:\_data_\_p_\_git\GitHub\j2cl-super\client-app\target\gwt3BuildCache\0.19-SNAPSHOT\de.xam.example.math-math-lib-0.1.0-SNAPSHOT\026f68a0c11cd044707d943b8dd286d4-stripped_sources\result
s\org\example\math\MathUtil.java:3: The import org.example.log cannot be resolved
[ERROR] de.xam.example.math:math-lib:0.1.0-SNAPSHOT/transpiled_js: Error:C:\_data_\_p_\_git\GitHub\j2cl-super\client-app\target\gwt3BuildCache\0.19-SNAPSHOT\de.xam.example.math-math-lib-0.1.0-SNAPSHOT\026f68a0c11cd044707d943b8dd286d4-stripped_sources\result
s\org\example\math\MathUtil.java:4: The import org.example.log cannot be resolved
[ERROR] de.xam.example.math:math-lib:0.1.0-SNAPSHOT/transpiled_js: Error:C:\_data_\_p_\_git\GitHub\j2cl-super\client-app\target\gwt3BuildCache\0.19-SNAPSHOT\de.xam.example.math-math-lib-0.1.0-SNAPSHOT\026f68a0c11cd044707d943b8dd286d4-stripped_sources\result
s\org\example\math\MathUtil.java:8: Log cannot be resolved to a type
[ERROR] de.xam.example.math:math-lib:0.1.0-SNAPSHOT/transpiled_js: Error:C:\_data_\_p_\_git\GitHub\j2cl-super\client-app\target\gwt3BuildCache\0.19-SNAPSHOT\de.xam.example.math-math-lib-0.1.0-SNAPSHOT\026f68a0c11cd044707d943b8dd286d4-stripped_sources\result
s\org\example\math\MathUtil.java:8: LogImpl cannot be resolved to a type
[ERROR] de.xam.example.math:math-lib:0.1.0-SNAPSHOT/transpiled_js: Error:C:\_data_\_p_\_git\GitHub\j2cl-super\client-app\target\gwt3BuildCache\0.19-SNAPSHOT\de.xam.example.math-math-lib-0.1.0-SNAPSHOT\026f68a0c11cd044707d943b8dd286d4-stripped_sources\result
s\org\example\math\MathUtil.java:18: Log cannot be resolved to a type
[ERROR] de.xam.example.math:math-lib:0.1.0-SNAPSHOT/transpiled_js: Error:C:\_data_\_p_\_git\GitHub\j2cl-super\client-app\target\gwt3BuildCache\0.19-SNAPSHOT\de.xam.example.math-math-lib-0.1.0-SNAPSHOT\026f68a0c11cd044707d943b8dd286d4-stripped_sources\result
s\org\example\math\MathUtil.java:20: Log cannot be resolved to a type
[ERROR] Exception executing task de.xam.example.math:math-lib:0.1.0-SNAPSHOT/transpiled_js
java.lang.IllegalStateException: Error while running J2CL
    at com.vertispan.j2cl.build.provided.J2clTask.lambda$resolve$7 (J2clTask.java:83)
    at com.vertispan.j2cl.build.TaskScheduler$2.executeTask (TaskScheduler.java:172)
    at com.vertispan.j2cl.build.TaskScheduler$2.lambda$onReady$0 (TaskScheduler.java:211)
    at java.util.concurrent.Executors$RunnableAdapter.call (Executors.java:539)
    at java.util.concurrent.FutureTask.run (FutureTask.java:264)
    at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run (ScheduledThreadPoolExecutor.java:304)
    at java.util.concurrent.ThreadPoolExecutor.runWorker (ThreadPoolExecutor.java:1136)
    at java.util.concurrent.ThreadPoolExecutor$Worker.run (ThreadPoolExecutor.java:635)
    at java.lang.Thread.run (Thread.java:833)
[INFO] Finished com.vertispan.jsinterop:base:1.0.0-SNAPSHOT/stripped_bytecode_headers in 25ms
java.lang.RuntimeException
        at com.vertispan.j2cl.build.DiskCache.markFailed(DiskCache.java:452)
        at com.vertispan.j2cl.build.DiskCache$CacheResult.markFailure(DiskCache.java:54)
        at com.vertispan.j2cl.build.TaskScheduler$2.executeTask(TaskScheduler.java:181)
        at com.vertispan.j2cl.build.TaskScheduler$2.lambda$onReady$0(TaskScheduler.java:211)
[INFO] ------------------------------------------------------------------------
        at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:539)
[INFO] BUILD FAILURE    at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)

[INFO] ------------------------------------------------------------------------ at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:304)

        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
        at java.base/java.lang.Thread.run(Thread.java:833)
[INFO] Total time:  12.276 s
[INFO] Finished at: 2022-02-20T21:48:07+01:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal com.vertispan.j2cl:j2cl-maven-plugin:0.19-SNAPSHOT:build (default-cli) on project client-app: Build failed, check log for failures -> [Help 1]
[ERROR]
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR]
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException
[INFO] Starting com.google.elemental2:elemental2-promise:1.1.0/stripped_bytecode
[INFO] Starting com.google.elemental2:elemental2-promise:1.1.0/transpiled_js
[ERROR] Exception executing task com.vertispan.jsinterop:base:1.0.0-SNAPSHOT/transpiled_js
java.lang.NoClassDefFoundError: org/eclipse/jdt/internal/compiler/lookup/ReductionResult$1
    at org.eclipse.jdt.internal.compiler.lookup.InferenceContext18.inferInvocationApplicability (InferenceContext18.java:376)
    at org.eclipse.jdt.internal.compiler.lookup.ParameterizedGenericMethodBinding.computeCompatibleMethod18 (ParameterizedGenericMethodBinding.java:248)
    at org.eclipse.jdt.internal.compiler.lookup.ParameterizedGenericMethodBinding.computeCompatibleMethod (ParameterizedGenericMethodBinding.java:92)
    at org.eclipse.jdt.internal.compiler.lookup.Scope.computeCompatibleMethod (Scope.java:841)
    at org.eclipse.jdt.internal.compiler.lookup.Scope.computeCompatibleMethod (Scope.java:798)
    at org.eclipse.jdt.internal.compiler.lookup.Scope.findMethod0 (Scope.java:1756)
```

So compiling math-lib failed due to log classes not found? Weird.