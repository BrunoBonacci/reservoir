(ns user
  (:require [com.brunobonacci.reservoir :as rsv]))


(comment

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


  ;; you can merge two or more reservoir as following
  ;; the resulting reservoir will have the characteristics
  ;; of the first reservoir with all the sample combined
  ;; based on their probability to be in the final sample.
  (rsv/merge-reservoirs
    (into (rsv/reservoir 5) (range 20))
    (into (rsv/reservoir 3) (range 10))
    (into (rsv/reservoir 4 :algorithm :algorithm-R) (range 20)))
  ;; => #com.brunobonacci/reservoir [:algorithm-L 5 50 [0 5 8 17 10]]
  )
