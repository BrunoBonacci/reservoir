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
