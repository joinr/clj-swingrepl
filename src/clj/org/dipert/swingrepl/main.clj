(ns org.dipert.swingrepl.main
  "Swing Clojure REPL using BeanShell's JConsole"
  (:require clojure.main clojure.repl)
  (:import (javax.swing JFrame)
           (java.awt.event WindowEvent)
           (java.awt Font)
           (bsh.util JConsole))
  (:gen-class))

(def ^{:doc "Formatted Clojure version string"
       :private true}
     clj-version
     (apply str (interpose \. (map *clojure-version* [:major :minor :incremental]))))

(def ^{:doc "Default REPL options"
       :private false}
     default-opts
     {:width 972
      :height 400
      :font (Font. "Monospaced" Font/PLAIN 14)
      :title (str "Clojure " clj-version " REPL")
      :prompt #(printf "%s=> " (ns-name *ns*))
      :init #()
      :eval eval
      :on-close JFrame/DISPOSE_ON_CLOSE})

(def ^{:doc "Default debug REPL options"
       :private false}
     default-dbg-opts
     {:title (str "Clojure " clj-version " Debug REPL")
      :prompt #(print "dr => ")
      :eval (comment "See make-dbg-repl-jframe")})

;;Added by Tom...
(defn send-repl
  "Sends a string xs, assumably an evaluable expression in string form, 
   across the shell's inPipe, which is surprisingly hard to do.  Echoes 
   the input to the console, and evaluates as if the user had entered
   the input, complete with an enter keypress."
  [^bsh.util.JConsole rpl ^String xs]
  (do (.readLine rpl xs)
      (.print rpl xs)
      (.enterEvent rpl)))

(defmacro eval-repl
  "Convenience macro.  Allows us to evaluate arbitrary expressions in 
   the swingrepl.  Provides the string conversion for us."
  [rpl & body]
  (let [r (with-meta (gensym "swingrepl") {:tag 'bsh.util.JConsole})]
    `(let [~r ~rpl]
       (send-repl ~r
                  ~(str (first body))))))

(defn- make-repl-thread [console & repl-args]
  (binding [*out* (.getOut console)
            *in*  (clojure.lang.LineNumberingPushbackReader. (.getIn console))
            *err* (.getOut console)]
    (Thread. (bound-fn []
               (apply clojure.main/repl repl-args)))))

(defn- window-closing-dispatcher [window]
  #(.dispatchEvent window (WindowEvent. window WindowEvent/WINDOW_CLOSING)))


(defn make-repl-jconsole
  "Returns a JConsole component"
  [options]
  (let [{:keys [font prompt init eval eof in out]} options
        console (bsh.util.JConsole. in out font)
        thread  (make-repl-thread console :prompt prompt :init init :eval eval)
        stopper (clojure.repl/thread-stopper thread)]
    (doto console
      (.setInterruptFunction (fn [reason] (stopper reason)))
      (.setEOFFunction eof))
    (.start thread)
    console))


(defn make-repl-jframe
  "Displays a JFrame with JConsole and attached REPL."
  ([] (make-repl-jframe {}))
  ([optmap]
     (let [options (merge default-opts optmap)
           {:keys [title width height font on-close prompt init eval]} options
           jframe (doto (JFrame. title)
                   (.setSize width height)
                   (.setDefaultCloseOperation on-close)
                   (.setLocationRelativeTo nil))
           eof (window-closing-dispatcher jframe)]
       (let [console (make-repl-jconsole
                      (merge
                       {:eof eof}
                       options))]
         (doto (.getContentPane jframe)
           (.setLayout (java.awt.BorderLayout.))
           (.add console))
         (doto jframe
           (.pack)
           (.setSize width height))
         (.requestFocus console)
         (.setVisible jframe true)))))

;; local-bindings and eval-with-locals are from http://gist.github.com/252421
;; Inspired by George Jahad's version: http://georgejahad.com/clojure/debug-repl.html
(defmacro local-bindings
  "Produces a map of the names of local bindings to their values."
  []
  (let [symbols (map key @clojure.lang.Compiler/LOCAL_ENV)]
    (zipmap (map (fn [sym] `(quote ~sym)) symbols) symbols)))

(declare ^:dynamic *locals*)
(defn eval-with-locals
  "Evals a form with given locals. The locals should be a map of symbols to
  values."
  [locals form]
  (binding [*locals* locals]
    (eval
      `(let ~(vec (mapcat #(list % `(*locals* '~%)) (keys locals)))
         ~form))))

(defmacro make-dbg-repl-jframe
  "Displays a JFrame with JConsole and attached REPL. The frame has the context
  from wherever it has been called, effectively creating a debugging REPL.

  Usage:

    (use 'org.dipert.swingrepl.main)
    (defn foo [a] (+ a 5) (make-dbg-repl-jframe {}) (+ a 2))
    (foo 3)

  This will pop up the debugging REPL, you should be able to access the var 'a'
  from the REPL."
  ([] `(make-dbg-repl-jframe {}))
  ([optmap]
   `(make-repl-jframe (merge
      default-opts
      default-dbg-opts
      {:eval (partial eval-with-locals (local-bindings))}
      ~optmap))))

(defn -main
  [& args]
  (make-repl-jframe {:on-close JFrame/EXIT_ON_CLOSE}))
