(defproject fullcontact/full.cljs "0.2.1"
  :description "ClojureScript sugar - logging, browser API's etc."

  :url "https://github.com/fullcontact/full.cljs"

  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :deploy-repositories [["releases" {:url "https://clojars.org/repo/" :creds :gpg}]]

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.40"]
                 [cljs-ajax "0.5.4"]
                 [camel-snake-kebab "0.4.0"]])
