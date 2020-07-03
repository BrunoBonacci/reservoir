(ns com.brunobonacci.reservoir
  (:import [clojure.lang Counted
            Sequential
            IPersistentCollection
            Indexed
            IPersistentStack
            Reversible
            IObj]
           [java.io Writer Serializable]
           [java.util Collection]))



(defprotocol IReservoir

  (capacity    [_] "Returns the capacity of the reservoir")
  (algorithm   [_] "Returns the algorithm used for the reservoir")
  (samples     [_] "Returns the items from currently stored in the reservoir")
  (total-items [_] "Returns the total number of items")
  (add-item [_ item]
    "Probably adds the item to the reservoir and returns a new
    reservoir which could contain this item."))



(deftype PersistentReservoir
    [^long capacity ^long n buf meta]

  IReservoir
  (capacity    [_] capacity)
  (algorithm   [_] :basic)
  (samples     [_] buf)
  (total-items [_] n)
  (add-item    [_ item]
    (if (< (count buf) capacity)
      (PersistentReservoir. capacity (inc n) (conj buf item) meta)
      (let [idx (rand-int (inc n))]
        (if (< idx capacity)
          (PersistentReservoir. capacity (inc n) (assoc buf idx item) meta)
          (PersistentReservoir. capacity (inc n) buf meta)))))


  ;; Interfaces and protocols to work with Clojure sequence
  ;; abstractions and other collection APIs
  Serializable

  Counted
  (count [this] (count buf))

  Sequential ;; tagging interface

  IObj
  (withMeta [this m]
    (PersistentReservoir. capacity n buf m))
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
    (PersistentReservoir. capacity 0 [] meta))
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
    (nth buf i default)))



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
    (nth buf i default)))



(defn reservoir
  [capacity & {:keys [algorithm] :or {algorithm :algorithm-L}}]
  (case algorithm

    (nil :algorithm-L)
    (PersistentReservoirAlgorithmL. capacity 0 [] nil
      (Math/exp (/ (Math/log (rand)) capacity)) capacity)

    :basic
    (PersistentReservoir. capacity 0 [] nil)))
