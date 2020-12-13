(ns dynamo.util
  (:require [clojure.walk :as walk])
  (:import java.text.SimpleDateFormat))

(defn deep-transform-values [m f]
  (walk/postwalk (fn [x]
                   (if (map? x)
                     (->> x
                          (map (fn [[k v]] [k (f v)]))
                          (into {}))
                     x))
                 m))

(defn format-date [v]
  (if (= java.util.Date (type v))
    (.format (SimpleDateFormat. "MMMM dd, yyyy") v)
    v))
