(ns perf.benchmarks
  (:require [com.brunobonacci.reservoir :refer :all]))


(defn items
  [size]
  (->> (mapcat #(repeat % %) (range))
    (take size)
    (shuffle)))


(def items1M (doall (items 1000000)))


(defn reservoir-algorithm-R-10K-1M
  []
  (into (reservoir 10000 :algorithm :algorithm-R) items1M))


(defn reservoir-algorithm-L-10K-1M
  []
  (into (reservoir 10000 :algorithm :algorithm-L) items1M))
