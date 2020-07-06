(defproject com.brunobonacci/reservoir "0.1.0-SNAPSHOT"
  :description "A fast implementation of Reservoir Sampling with Immutable Persistent data structures."

  :url "https://github.com/BrunoBonacci/reservoir"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :scm {:name "git" :url "https://github.com/BrunoBonacci/reservoir.git"}

  :dependencies [[org.clojure/clojure "1.10.2-alpha1"]]

  :global-vars {*warn-on-reflection* true}

  :jvm-opts ["-server"]

  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[midje "1.9.9"]
                                  [org.clojure/test.check "1.0.0"]
                                  [criterium "0.4.5"]
                                  [org.slf4j/slf4j-log4j12 "1.7.30"]
                                  [jmh-clojure "0.2.1"]
                                  ;; for comparison
                                  [bigml/sampling "3.2"]
                                  ]
                   :resource-paths ["dev-resources"]
                   :plugins      [[lein-midje "3.2.2"]]}}

  :aliases
  {"perf" ["with-profile" "dev" "jmh" #=(pr-str {:file "./dev/perf/benchmarks.edn" :status true :pprint true})]}
  )
