clj-swingrepl
=============

Swing Clojure REPL that uses BeanShell's JConsole component.

Build
-----

* Install [Leiningen](http://github.com/technomancy/leiningen)
* `lein deps`
* `lein javac`

Install
-------
You can pull the joinr fork from clojars at: 


Run
---

You can run `lein swank` and connect with SLIME via Emacs, or you can build a distributable jar with `lein uberjar`

To run, use something like `java -jar swingrepl-standalone.jar`

Updates (joinr fork Feb 2017)
-------

clj-swingrepl now supports the ability to send code to the repl as if the user 
typed the code, effectively allowing "echoing" of output to repl.

* org.dipert.swingrepl.main/send-repl
* This provides a useful platform for embedding a live "repl" in your 
  host application that you can send input to as if the user typed it.
* Users can click on widgets, use buttons, etc. and see the corresponding 
  clojure forms that are evaluated.

Todo
----

* Completions for things available in the current namespace: JConsole has its own completions mechanism that might be hooked into
* Bracket, parentheses, quote completion/matching/highlighting
* Better as-library behavior: provide configurable automatic imports

Notes
-----

* A Clojure implementation of something like JConsole might be nice

Thanks
------

Many props to the BeanShell dude for making such a cool REPL.

Copyright 2012 Alan Dipert
Distributed under the Eclipse Public License, the same as Clojure.
