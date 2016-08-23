(defproject swingrepl "1.4.2-SNAPSHOT"
  :source-paths ["src/clj"]
  :java-source-paths ["src/jvm"]
  :javac-options     ["-target" "1.6" "-source" "1.6"]
  :description "A Swing Clojure REPL using BeanShell's JConsole"
  :dependencies [[org.clojure/clojure "1.7.0"]]
  :plugins [[lein-localrepo "0.5.3"]]
  :main org.dipert.swingrepl.main)
