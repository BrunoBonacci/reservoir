(ns com.brunobonacci.reservoir-test
  (:require [com.brunobonacci.reservoir :refer :all]
            [midje.sweet :refer :all]
            [incanter.core :as i]
            [incanter.stats :as s]
            [incanter.charts :as p]))



(def tollerance  0.1)
(def data-size   1000000)
(def sample-size 1000)



(defchecker approx
  ([expected low high]
   (chatty-checker [actual]
     (and (number? actual)
       (<= low actual high))))
  ([expected tollerance]
   (let [a (-' expected (*' expected tollerance))
         b (+' expected (*' expected tollerance))
         low (min a b)
         high (max a b)]
     (approx expected low high)))
  ([expected]
   (approx expected tollerance)))



(facts "algorithm-R"

  (fact "test reservoir with NORMAL distribution"

    (let [data   (s/sample-normal data-size :mean 100 :sd 10)
          ;; sorting the data to highlight bias towards the beginning of the series
          sample (into (reservoir sample-size :algorithm :algorithm-R) (sort data))]

      (s/mean sample) => (approx (s/mean data))
      (s/sd sample)   => (approx (s/sd data))
      ))



  (fact "test reservoir with EXPONENTIAL distribution"

    (let [data   (s/sample-exp data-size :rate 1/10)
          ;; sorting the data to highlight bias towards the beginning of the series
          sample (into (reservoir sample-size :algorithm :algorithm-R) (sort data))]

      (s/mean sample) => (approx (s/mean data))
      (s/sd sample)   => (approx (s/sd data))
      ))



  (fact "test reservoir with GAMMA distribution"

    (let [data   (s/sample-gamma data-size :shape 2 :scale 2)
          ;; sorting the data to highlight bias towards the beginning of the series
          sample (into (reservoir sample-size :algorithm :algorithm-R) (sort data))]

      (s/mean sample) => (approx (s/mean data))
      (s/sd sample)   => (approx (s/sd data))
      ))



  (fact "test reservoir with BINOMIAL distribution"

    (let [data   (s/sample-binomial data-size :size 100)
          ;; sorting the data to highlight bias towards the beginning of the series
          sample (into (reservoir sample-size :algorithm :algorithm-R) (sort data))]

      (s/mean sample) => (approx (s/mean data))
      (s/sd sample)   => (approx (s/sd data))
      ))



  (fact "test reservoir with POISSON distribution"

    (let [data   (s/sample-poisson data-size)
          ;; sorting the data to highlight bias towards the beginning of the series
          sample (into (reservoir sample-size :algorithm :algorithm-R) (sort data))]

      (s/mean sample) => (approx (s/mean data))
      (s/sd sample)   => (approx (s/sd data))
      ))



  (fact "test reservoir with UNIFORM distribution"

    (let [data   (s/sample-uniform data-size)
          ;; sorting the data to highlight bias towards the beginning of the series
          sample (into (reservoir sample-size :algorithm :algorithm-R) (sort data))]

      (s/mean sample) => (approx (s/mean data))
      (s/sd sample)   => (approx (s/sd data))
      ))


  )



(facts "algorithm-L"

  (fact "test reservoir with NORMAL distribution"

    (let [data   (s/sample-normal data-size :mean 100 :sd 10)
          ;; sorting the data to highlight bias towards the beginning of the series
          sample (into (reservoir sample-size :algorithm :algorithm-L) (sort data))]

      (s/mean sample) => (approx (s/mean data))
      (s/sd sample)   => (approx (s/sd data))
      ))



  (fact "test reservoir with EXPONENTIAL distribution"

    (let [data   (s/sample-exp data-size :rate 1/10)
          ;; sorting the data to highlight bias towards the beginning of the series
          sample (into (reservoir sample-size :algorithm :algorithm-L) (sort data))]

      (s/mean sample) => (approx (s/mean data))
      (s/sd sample)   => (approx (s/sd data))
      ))



  (fact "test reservoir with GAMMA distribution"

    (let [data   (s/sample-gamma data-size :shape 2 :scale 2)
          ;; sorting the data to highlight bias towards the beginning of the series
          sample (into (reservoir sample-size :algorithm :algorithm-L) (sort data))]

      (s/mean sample) => (approx (s/mean data))
      (s/sd sample)   => (approx (s/sd data))
      ))



  (fact "test reservoir with BINOMIAL distribution"

    (let [data   (s/sample-binomial data-size :size 100)
          ;; sorting the data to highlight bias towards the beginning of the series
          sample (into (reservoir sample-size :algorithm :algorithm-L) (sort data))]

      (s/mean sample) => (approx (s/mean data))
      (s/sd sample)   => (approx (s/sd data))
      ))



  (fact "test reservoir with POISSON distribution"

    (let [data   (s/sample-poisson data-size)
          ;; sorting the data to highlight bias towards the beginning of the series
          sample (into (reservoir sample-size :algorithm :algorithm-L) (sort data))]

      (s/mean sample) => (approx (s/mean data))
      (s/sd sample)   => (approx (s/sd data))
      ))



  (fact "test reservoir with UNIFORM distribution"

    (let [data   (s/sample-uniform data-size)
          ;; sorting the data to highlight bias towards the beginning of the series
          sample (into (reservoir sample-size :algorithm :algorithm-L) (sort data))]

      (s/mean sample) => (approx (s/mean data))
      (s/sd sample)   => (approx (s/sd data))
      ))


  )



(comment

  (defn expRV []
    (cons
      (* (/ -1 1/10) (Math/log (.nextDouble (java.util.concurrent.ThreadLocalRandom/current))))
      (lazy-seq
        (expRV))))



  (defn show [chart]
    "Renders a chart and saves the result in a temp file"
    (i/save chart "/tmp/chart.png" :width 900 :height 600))



  (defn make-cdf
    "Given a seq of numbers, return a plottable vector of [x,y] pairs
  representing the cumulative distribution function.
  Ex:
    (cdf-data [1 2 1 3])
    ; => [[1 0] [1 1/2] [2 3/4] [3 1]]"
    [xs]
    {:pre [(sequential? xs)]}
    (let [n (count xs)
          uniq-xs (->> xs frequencies (into (sorted-map)))
          cdf-xs (keys uniq-xs)
          n-so-far-seq (reductions + (vals uniq-xs))
          cdf-ys (map #(/ % n) n-so-far-seq)
          cdf-xy-pairs (map vector cdf-xs cdf-ys)
          init-pair [(-> uniq-xs first key) 0]]
      (vec (cons init-pair cdf-xy-pairs))))



  (def d   (s/sample-normal 1000000 :mean 100 :sd 10))
  (def d   (s/sample-exp    1000000 :rate 0.1))
  (def d   (sort (take      1000000 (exprv))))

  (def d'  (into (reservoir 10000 :algorithm :algorithm-R) (sort d)))
  (def d'' (into (reservoir 10000 :algorithm :algorithm-L) (sort d)))

  (s/mean d)
  (s/mean d')
  (s/mean d'')

  (s/sd d)

  (show (p/histogram d   :density true :nbins 30))
  (show (p/histogram d'  :density true :nbins 30))
  (show (p/histogram d'' :density true :nbins 30))

  (def cdf-d   (make-cdf d))
  (def cdf-d'  (make-cdf d'))
  (def cdf-d'' (make-cdf d''))

  (let [cdf (make-cdf d)]
    (doto (p/scatter-plot (map first cdf-d) (map second cdf-d))
      (p/set-point-size 1)
      (p/add-points (map first cdf-d') (map second cdf-d') :series 1)
      (p/set-point-size 1)
      (p/set-stroke-color java.awt.Color/blue)
      (p/add-points (map first cdf-d'') (map second cdf-d'') :series 1)
      (p/set-point-size 1)
      (p/set-stroke-color java.awt.Color/green)
      (p/set-alpha 0.1)
      (show)))


  )
