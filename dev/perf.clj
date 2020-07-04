(ns perf
  (:require [com.brunobonacci.reservoir :refer :all]
            [criterium.core :refer [bench quick-bench]]
            [clojure.pprint]))


(defn items
  [size]
  (->> (mapcat #(repeat % %) (range))
    (take size)
    (shuffle)))


(comment

  (def items1M (items 1000000))
  (frequencies items1M)

  ;; simple pvector
  (quick-bench
    (into [] items1M))
  ;; Execution time mean : 14.770293 ms
  ;; Execution time std-deviation : 104.432807 µs
  ;; Execution time lower quantile : 14.612321 ms ( 2.5%)
  ;; Execution time upper quantile : 14.876415 ms (97.5%)
  ;; Overhead used : 2.309092 ns

  ;; :simple :algorithm
  (quick-bench
    (into (reservoir 10000 :algorithm :simple) items1M))

  ;; Execution time mean : 48.510345 ms
  ;; Execution time std-deviation : 1.994757 ms
  ;; Execution time lower quantile : 46.083439 ms ( 2.5%)
  ;; Execution time upper quantile : 51.081795 ms (97.5%)
  ;; Overhead used : 2.309092 ns


  ;;  :algorithm-L
  (quick-bench
    (into (reservoir 10000 :algorithm :algorithm-L) items1M))

  ;; Execution time mean : 30.131117 ms
  ;; Execution time std-deviation : 1.487733 ms
  ;; Execution time lower quantile : 28.247301 ms ( 2.5%)
  ;; Execution time upper quantile : 31.998845 ms (97.5%)
  ;; Overhead used : 8.211514 ns

  )



(defn print-table
  [table]
  (->> table
    (map (fn [[n k v]]
           {:n n (format "%.0f%%" (* k 100)) (format "%.2f%%" (* v 100))}))
    (group-by :n)
    (map (fn [[n vs]] (apply merge vs)))
    (clojure.pprint/print-table)))

(comment


  (for [size [10000 100000 1000000 10000000]
      pct  [0.01 0.02 0.03 0.05]]
    (let [capacity (long (* size pct))
          all-items (items size)
          distinct-items (-> all-items distinct count)]
      [size
       pct
       (/
         (->> all-items
           (into (reservoir capacity))
           (distinct)
           (count))
         distinct-items
         1.0)]))

  (print-table *1)

  ;; |       :n |     1% |     2% |     3% |     5% |
  ;; |----------+--------+--------+--------+--------|
  ;; |    10000 | 47.52% | 65.96% | 75.89% | 84.40% |
  ;; |   100000 | 80.31% | 91.95% | 92.39% | 95.75% |
  ;; |  1000000 | 92.72% | 96.61% | 97.24% | 98.73% |
  ;; | 10000000 | 98.01% | 98.84% | 99.24% | 99.53% |




  )




(comment
  ;;
  ;; Comparison with bigml/sampling (doesn't work with java11)
  ;;
  (require '[bigml.sampling.reservoir :as reservoir])

  (quick-bench
    (into (reservoir/create  10000 :implementation :efraimdis) items1M))

  ;; Execution time mean : 1439.723 ms
  ;; Execution time std-deviation : 56.219255 ms
  ;; Execution time lower quantile : 1.384248 sec ( 2.5%)
  ;; Execution time upper quantile : 1.513384 sec (97.5%)
  ;; Overhead used : 1.827186 ns

  (quick-bench
    (into (reservoir/create  10000 :implementation :insertion) items1M))

  ;; Execution time mean : 462.243933 ms
  ;; Execution time std-deviation : 8.206758 ms
  ;; Execution time lower quantile : 447.970058 ms ( 2.5%)
  ;; Execution time upper quantile : 469.125423 ms (97.5%)
  ;; Overhead used : 1.827186 ns


  )
