(defproject com.brunobonacci/reservoir "0.1.0-SNAPSHOT"
  :description "FIXME: write description"

  :url "https://github.com/BrunoBonacci/reservoir"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :scm {:name "git" :url "https://github.com/BrunoBonacci/reservoir.git"}

  :dependencies [[org.clojure/clojure "1.10.2-alpha1"]]

  :global-vars {*warn-on-reflection* true}

  :jvm-opts ["-server"]

  :profiles {:dev {:dependencies [[midje "1.9.9"]
                                  [org.clojure/test.check "1.0.0"]
                                  [criterium "0.4.5"]
                                  [org.slf4j/slf4j-log4j12 "1.7.30"]]
                   :resource-paths ["dev-resources"]
                   :plugins      [[lein-midje "3.2.2"]]}}
  )
