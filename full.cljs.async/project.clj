(defproject fullcontact/full.cljs.async "0.2.1-SNAPSHOT"
  :description "Extensions and helpers for cljs.core.async."

  :url "https://github.com/fullcontact/full.cljs"

  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :deploy-repositories [["releases" {:url "https://clojars.org/repo/" :creds :gpg}]]

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.40"]
                 [org.clojure/core.async "0.2.374"]]

  :plugins [[lein-cljsbuild "1.1.3"]
            [com.cemerick/clojurescript.test "0.3.3"]]

  :hooks [leiningen.cljsbuild]

  :profiles {:dev {:plugins [[com.cemerick/clojurescript.test "0.3.3"]]}}

  :aliases {"autotest" ["do" "clean," "cljsbuild" "auto" "test"]}

  :cljsbuild {:test-commands {"test" ["phantomjs" :runner "target/test.js"]}
              :builds [{:id "test"
                        :notify-command ["phantomjs" :cljs.test/runner "target/test.js"]
                        :source-paths ["src" "test"]
                        :compiler {:output-to "target/test.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]
              }
  )
