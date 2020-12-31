(ns morphy.util
  (:require [clojure.walk :as walk]
            [clojure.string :as str])
  (:import java.time.ZonedDateTime
           java.time.format.DateTimeFormatter
           java.time.ZoneId))

(defn deep-transform-values [m f]
  (walk/postwalk (fn [x]
                   (if (map? x)
                     (->> x
                          (map (fn [[k v]] [k (f v)]))
                          (into {}))
                     x))
                 m))

(defn- ensure-local-date [v]
  (if (instance? java.util.Date v)
    (-> v .toInstant
        (.atZone (ZoneId/of "UTC"))
        (.withZoneSameLocal (ZoneId/systemDefault)))
    v))

(defn format-date [v]
  (-> v
      ensure-local-date
      (.format (DateTimeFormatter/ofPattern "MMM dd, yyyy"))
      (str/replace #"\." "")))

(defn ->rfc-1123-date [v]
  (-> v
      ensure-local-date
      (.format DateTimeFormatter/RFC_1123_DATE_TIME)))

(defn now []
  (->rfc-1123-date (ZonedDateTime/now)))

(defn push [m k v]
  (update m k concat [v]))
