# reservoir
[![Clojars Project](https://img.shields.io/clojars/v/com.brunobonacci/reservoir.svg)](https://clojars.org/com.brunobonacci/reservoir)  [![cljdoc badge](https://cljdoc.org/badge/com.brunobonacci/reservoir)](https://cljdoc.org/d/com.brunobonacci/reservoir/CURRENT) ![CircleCi](https://img.shields.io/circleci/project/BrunoBonacci/reservoir.svg) ![last-commit](https://img.shields.io/github/last-commit/BrunoBonacci/reservoir.svg)

A fast implementation of Reservoir Sampling with Immutable Persistent data structures.

`*** WORK IS STILL IN PROGRESS ***`

## Usage

In order to use the library add the dependency to your `project.clj`

``` clojure
;; Leiningen project
[com.brunobonacci/reservoir "0.1.0-SNAPSHOT"]

;; deps.edn format
{:deps { com.brunobonacci/reservoir {:mvn/version "0.1.0-SNAPSHOT"}}}
```

Current version: [![Clojars Project](https://img.shields.io/clojars/v/com.brunobonacci/reservoir.svg)](https://clojars.org/com.brunobonacci/reservoir)


Then require the namespace:

``` clojure
(ns foo.bar
  (:require [com.brunobonacci.reservoir :as rsv]))
```

Check the [online documentation](https://cljdoc.org/d/com.brunobonacci/reservoir/CURRENT)

``` clojure
;; create a reservoir
(def r (rsv/reservoir 3))

;; add some items, returns a new reservoir
(def r' (conj r :item1 :item2 :item3 :item4 :item4))

(frequencies r')
;; => {:item4 2, :item3 1}

;; to add many items you can use `into`
(def r2
  (->> (repeatedly #(rand-int 1000))
    (take 100000)
    (into (rsv/reservoir 1000))))

;; the distribution of the samples should mimic the normal
;; distribution (pseudo)
(->> (frequencies r2)
  (sort-by second >)
  (take 10))
;; => ([481 6]
;;     [186 5]
;;     [647 5]
;;     [194 5]
;;     [362 5]
;;     [887 4]
;;     [592 4]
;;     [694 4]
;;     [21 4]
;;     [708 4])

;; to see how many items passed by the reservoir
(rsv/total-items r2)
;; => 100000

;; to get the samples
(rsv/samples r2)
;; => [205 831 423 830 238  ...]

;; reservoir and just normal clojure sequences
(seq r2)
;; => (205 831 423 830 238  ...)


(def r3 (into (rsv/reservoir 100) (range 30)))

;; this is the total capacity of the reservoir
(rsv/capacity r3)
;; => 100

;; here the current number of items in the reservoir
;; it will be always between 0 and `capacity`
(count r3)
;; => 30

;; this is the total number of items which passed
;; into the reservoir. let's add more elements
(rsv/total-items (into r3 (range 120)))
;; => 150
```

### Performances

There are two implementation of the reservoir sampling.
The **Simple** algorithm (also known as [Algorithm-R](https://en.wikipedia.org/wiki/Reservoir_sampling#Simple_algorithm)
which has a time complexity of `O(n)` and an optimal version called
[Algorithm-L](https://en.wikipedia.org/wiki/Reservoir_sampling#An_optimal_algorithm)
which is `O(k(1+log(n/k)))`. *The default implementation is Algorithm-L*

``` clojure
  ;; :simple :algorithm-R
  (quick-bench
    (into (reservoir 10000 :algorithm :simple) (range 1000000))

  ;; Time per million of items
  ;; --------------------------------
  ;; Execution time mean : 48.510345 ms
  ;; Execution time std-deviation : 1.994757 ms
  ;; Execution time lower quantile : 46.083439 ms ( 2.5%)
  ;; Execution time upper quantile : 51.081795 ms (97.5%)
  ;; Overhead used : 2.309092 ns


  ;; :algorithm-L  (default)
  (quick-bench
    (into (reservoir 10000 :algorithm :algorithm-L) (range 1000000)))

  ;; Time per million of items
  ;; --------------------------------
  ;; Execution time mean : 30.131117 ms
  ;; Execution time std-deviation : 1.487733 ms
  ;; Execution time lower quantile : 28.247301 ms ( 2.5%)
  ;; Execution time upper quantile : 31.998845 ms (97.5%)
  ;; Overhead used : 8.211514 ns

```

This reservoir implementation is up to two orders or magnitude faster
(~50x) than the excellent
[BigML/sampling](https://github.com/bigmlcom/sampling) implementation.

``` clojure
  (require '[bigml.sampling.reservoir :as reservoir])

  (quick-bench
    (into (reservoir/create  10000 :implementation :efraimdis) (range 1000000)))

  ;; Execution time mean : 1439.723 ms
  ;; Execution time std-deviation : 56.219255 ms
  ;; Execution time lower quantile : 1.384248 sec ( 2.5%)
  ;; Execution time upper quantile : 1.513384 sec (97.5%)
  ;; Overhead used : 1.827186 ns

  (quick-bench
    (into (reservoir/create  10000 :implementation :insertion) (range 1000000)))

  ;; Execution time mean : 462.243933 ms
  ;; Execution time std-deviation : 8.206758 ms
  ;; Execution time lower quantile : 447.970058 ms ( 2.5%)
  ;; Execution time upper quantile : 469.125423 ms (97.5%)
  ;; Overhead used : 1.827186 ns

```

## License

Copyright © 2020 Bruno Bonacci - Distributed under the [Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0)
