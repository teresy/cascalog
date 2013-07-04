(ns cascalog.math.stats
  (:use cascalog.api)
  (:require [cascalog.logic.ops :as c]
            [cascalog.predicate :as p]
            [clojure.contrib [accumulators :as acc]]))

(defn initialize-mean-variance-parallel [& X]
  (map (fn [x] (acc/mean-variance {:mean x :variance 0 :n 1})) X))

(p/defparallelagg mean-variance-parallel
  :init-var #'initialize-mean-variance-parallel
  :combine-var #'acc/combine)

(defn get-variance [mvp-struct]
  (mvp-struct :variance))

(defn get-mean [mvp-struct]
  (mvp-struct :mean))

(def variance-parallel
  (<- [!val :> !var]
      (mean-variance-parallel :< !val :> !ret)
      (get-variance :< !ret :> !var)))

(def mean-parallel
  (<- [!val :> !var]
      (mean-variance-parallel :< !val :> !ret)
      (get-mean :< !ret :> !var)))

(def variance
  "Predicate macro that calculates the variance of the supplied input
   var."
  (<- [!val :> !var]
      (* !val !val :> !squared)
      (c/sum !squared :> !square-sum)
      (c/count !count)
      (c/avg !val :> !mean)
      (* !mean !mean :> !mean-squared)
      (div !square-sum !count :> !i)
      (- !i !mean-squared :> !var)))

(def sample-variance
  "Predicate macro that calculates the sample variance of the supplied input
   var."
  (<- [!val :> !var]
      (* !val !val :> !squared)
      (c/sum !squared :> !squared-sum)
      (c/count !count)
      (c/sum !val :> !sum)
      (c/avg !val :> !mean)
      (* !sum !mean :> !i)
      (- !squared-sum !i :> !num)
      (- !count 1 :> !denom)
      (div !num !denom :> !var)))

