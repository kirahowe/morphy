(ns dynamo.util
  (:require [clojure.walk :as walk])
  (:import java.text.SimpleDateFormat
           java.util.Locale
           java.util.Date))

(defn deep-transform-values [m f]
  (walk/postwalk (fn [x]
                   (if (map? x)
                     (->> x
                          (map (fn [[k v]] [k (f v)]))
                          (into {}))
                     x))
                 m))

(defn- format-date* [s v]
  (.format (SimpleDateFormat. s Locale/US) v))

(defn format-date [v]
  (format-date* "MMM dd, yyyy" v))

(defn ->rfc-822-date [v]
  (format-date* "E, d MMM yyyy HH:mm:ss Z" v))

(defn now []
  (->rfc-822-date (Date.)))
