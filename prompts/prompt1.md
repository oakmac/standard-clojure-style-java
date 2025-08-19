We are porting the Standard Clojure Style library from JavaScript into Java 🤓🎉

This is an open source project used by many Clojure developers worldwide, so this is an exciting and
important community project. Basically: everyone is really looking forward for this to exist, and we have an
opportunity to write something that is both technically interesting (a language parser + formatter)
as well as useful to a large programming community.

As best as we can, we would like for this port to be "line for line" among all of the programming languages
that it is implemented in (you can see this currently between the JS and Lua versions).

For Java, I think we should take a similar approach, which will necessarily mean that this library
is not very idiomatic Java. Instead of many small classes and embracing the type system, I would like to use
just a handful of classes with static methods and pass around HashMaps instead of typed Objects.

Once we get the test suite passing, we can embrace more idiomatic Java and embrace the type system gradually;
for now let's keep going with the generic HashMap / ArrayList approach and keep types to a minimum.

I want this library to have wide platform support, so let's use only the most basic Java language constructs.
I don't know what minimum version of Java I want to support, but ideally something like ~10 years old?

Basically I do not want users of this library to have to be using a bleeding edge version of Java in order
to format their Clojure code: keep it as simple and portable as possible.

The library has a large test suite, so if we can get that all to pass we can be confident that the port is accurate
and exhibits correct behavior.

You can see in the implementation that there are very few language concepts used: objects in JS / tables in Lua,
Arrays, numbers, strings, and functions. Pretty bare-bones in terms of language features.

There is a lot of object literal usage, as well as the objects themselves being very stateful and dynamic.

We finally have the parser definitions complete and all of the parser_tests.json test cases pass! Hurrah!

Can you fill out what we need inside of ParseNsTest.java in order to test the parse_ns_tests.json test cases?