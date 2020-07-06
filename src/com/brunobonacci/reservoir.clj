(ns com.brunobonacci.reservoir
  (:import [clojure.lang Counted
            Sequential
            IPersistentCollection
            Indexed
            IPersistentStack
            Reversible
            IObj]
           [java.io Writer Serializable]
           [java.util Collection]
           [java.util.concurrent ThreadLocalRandom]))



(defprotocol IReservoir

  (capacity    [_] "Returns the capacity of the reservoir")
  (algorithm   [_] "Returns the algorithm used for the reservoir")
  (samples     [_] "Returns the items from currently stored in the reservoir")
  (total-items [_] "Returns the total number of items")
  (add-item [_ item]
    "Probably adds the item to the reservoir and returns a new
    reservoir which could contain this item."))



;;
;; internal protocol for printing dispatch.
;;
(defprotocol IPrintMethod

  (-print [_ ^Writer writer] "It outputs an EDN representation to the writer"))



;;
;; From [Wikipedia's article](https://en.wikipedia.org/wiki/Reservoir_sampling#Simple_algorithm)



;; A simple and popular but slow algorithm, commonly known as
;; Algorithm R, is due to Alan Waterman The algorithm works by
;; maintaining a reservoir of size `capacity`, which initially contains the
;; first items of the input.
;;
;; The algorithm then generates a random number `idx` between 1 and
;; excluding `n`. If `idx` is less than `capacity`, then the item is selected
;; and replaces whichever item currently occupies the `idx`-th position
;; in the reservoir. Otherwise, the item is discarded.
;;
;; In effect, for all `i`, the `i`th element of the input is chosen
;; to be included in the reservoir with probability `capcity/i`.
;; Similarly, at each iteration the `j`th element of the reservoir
;; array is chosen to be replaced with probability
;; `1/capacity x capacity/i = 1/i`.
;;
;; The time complexity for this algorithm is `O(n)`
;;

(deftype PersistentReservoirSimple
    [^long capacity ^long n buf meta]

  IReservoir
  (capacity    [_] capacity)
  (algorithm   [_] :algorithm-R)
  (samples     [_] buf)
  (total-items [_] n)
  (add-item    [_ item]
    (if (< (count buf) capacity)
      (PersistentReservoirSimple. capacity (inc n) (conj buf item) meta)
      (let [idx (rand-int (inc n))]
        (if (< idx capacity)
          (PersistentReservoirSimple. capacity (inc n) (assoc buf idx item) meta)
          (PersistentReservoirSimple. capacity (inc n) buf meta)))))


  ;; Interfaces and protocols to work with Clojure sequence
  ;; abstractions and other collection APIs
  Serializable

  Counted
  (count [this] (count buf))

  Sequential ;; tagging interface

  IObj
  (withMeta [this m]
    (PersistentReservoirSimple. capacity n buf m))
  (meta [this] meta)

  Object
  (toString [this]
    (pr-str (lazy-seq (seq this))))

  IPersistentCollection
  (cons [this item]
    (add-item this item))
  (seq [this]
    (seq buf))
  (empty [this]
    (PersistentReservoirSimple. capacity 0 [] meta))
  (equiv [this other]
    (and (sequential? other)
      (or (not (counted? other))
        (= (count this) (count other)))
      (= (seq this) (seq other))))

  Collection
  (iterator [this]
    (.iterator ^Iterable (sequence this)))
  (contains [this e]
    (boolean (some #(= e %) (.seq this))))
  (containsAll [this elts]
    (every? #(.contains this %) elts))
  (size [this]
    (.count this))
  (isEmpty [this]
    (empty? this))
  (^objects toArray [this ^objects dest]
   (reduce (fn [idx item]
        (aset dest idx item)
        (inc idx))
     0, this)
   dest)
  (toArray [this]
    (.toArray this (object-array (.count this))))

  Indexed
  (nth [this i]
    (nth buf i))
  (nth [this i default]
    (nth buf i default))

  IPrintMethod
  (-print [this w]
    (.write ^Writer w "#com.brunobonacci/reservoir ")
    (print-method [:algorithm-R capacity n buf] w))
  )



;;
;; From [Wikipedia's article](https://en.wikipedia.org/wiki/Reservoir_sampling#An_optimal_algorithm)



;;
;; `Algorithm L` improves upon this algorithm by computing how many
;; items are discarded before the next item enters the reservoir. The
;; key observation is that this number follows a geometric
;; distribution and can therefore be computed in constant time.
;;
;; This algorithm computes three random numbers for each item that
;; becomes part of the reservoir, and does not spend any time on items
;; that do not. Its expected running time is thus `O(k(1+log(n/k)))`
;; which is optimal. At the same time, it is simple to implement
;; efficiently and does not depend on random deviates from exotic or
;; hard-to-compute distributions.
;;
(deftype PersistentReservoirAlgorithmL
    [^long capacity ^long n buf meta ^double W ^long n']

  IReservoir
  (capacity    [_] capacity)
  (algorithm   [_] :algorithm-L)
  (samples     [_] buf)
  (total-items [_] n)
  (add-item    [_ item]
    (if (< (count buf) capacity)
      (PersistentReservoirAlgorithmL. capacity (inc n)
        (conj buf item) meta W n')
      (if (= n n')
        (let [idx (rand-int capacity)
              r  (rand)
              n' (+ n' (/ (Math/log r) (Math/log (- 1 W))) 1)
              W' (* W (Math/exp (/ (Math/log r) capacity)))]
          (PersistentReservoirAlgorithmL. capacity (inc n)
            (assoc buf idx item) meta W' n'))
        (PersistentReservoirAlgorithmL.   capacity (inc n) buf meta W n'))))


  ;; Interfaces and protocols to work with Clojure sequence
  ;; abstractions and other collection APIs
  Serializable

  Counted
  (count [this] (count buf))

  Sequential ;; tagging interface

  IObj
  (withMeta [this m]
    (PersistentReservoirAlgorithmL. capacity n buf m W n'))
  (meta [this] meta)

  Object
  (toString [this]
    (pr-str (lazy-seq (seq this))))

  IPersistentCollection
  (cons [this x]
    (add-item this x))
  (seq [this]
    (seq buf))
  (empty [this]
    (PersistentReservoirAlgorithmL. capacity 0 [] meta
      (Math/exp (/ (Math/log (rand)) capacity)) capacity))
  (equiv [this other]
    (and (sequential? other)
      (or (not (counted? other))
        (= (count this) (count other)))
      (= (seq this) (seq other))))

  Collection
  (iterator [this]
    (.iterator ^Iterable (sequence this)))
  (contains [this e]
    (boolean (some #(= e %) (.seq this))))
  (containsAll [this elts]
    (every? #(.contains this %) elts))
  (size [this]
    (.count this))
  (isEmpty [this]
    (empty? this))
  (^objects toArray [this ^objects dest]
   (reduce (fn [idx item]
        (aset dest idx item)
        (inc idx))
     0, this)
   dest)
  (toArray [this]
    (.toArray this (object-array (.count this))))

  Indexed
  (nth [this i]
    (nth buf i))
  (nth [this i default]
    (nth buf i default))

  IPrintMethod
  (-print [this w]
    (.write ^Writer w "#com.brunobonacci/reservoir ")
    (print-method [:algorithm-L capacity n buf] w)))



(defmethod print-method com.brunobonacci.reservoir.IReservoir
  [^com.brunobonacci.reservoir.IReservoir b ^Writer w]
  (-print b w))



(defmethod print-dup com.brunobonacci.reservoir.IReservoir
  [r ^java.io.Writer w]
  (print-method r w))



(defn- make-reservoir [[algo capacity n buf]]
  (case algo
    :algorithm-L
    (PersistentReservoirAlgorithmL. capacity n buf nil
      (Math/exp (/ (Math/log (rand)) capacity)) (inc n))

    :algorithm-R
    (PersistentReservoirSimple. capacity n buf nil)))



(defn reservoir
  [capacity & {:keys [algorithm] :or {algorithm :algorithm-L}}]
  (case algorithm

    (nil :algorithm-L)
    (PersistentReservoirAlgorithmL. capacity 0 [] nil
      (Math/exp (/ (Math/log (rand)) capacity)) capacity)

    (:simple :algorithm-R)
    (PersistentReservoirSimple. capacity 0 [] nil)))



(defn- combine-reservoirs
  [^com.brunobonacci.reservoir.IReservoir r1
   ^com.brunobonacci.reservoir.IReservoir r2]
  (let [n1 (total-items r1)
        n2 (total-items r2)
        n' (+ n1 n2)
        p  (/ n2 n')
        lr (ThreadLocalRandom/current)
        k1 (capacity r1)
        R' (reduce
             (fn [acc i]
               (if (<= (rand) p)
                 (assoc acc (.nextInt lr k1) i)
                 acc))
             (samples r1)
             (samples r2))]
    (make-reservoir [(algorithm r1) (capacity r1) n' R'])))



(defn merge-reservoirs
  [& reservoirs]
  (reduce combine-reservoirs reservoirs))



(defn frequency-density
  [^com.brunobonacci.reservoir.IReservoir reservoir]
  (let [k (capacity reservoir)]
    (->> (frequencies reservoir)
      (map (fn [[i f]] [i (/ f k)]))
      (into {}))))
