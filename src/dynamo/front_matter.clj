(ns dynamo.front-matter
  (:require [front-matter.core :as fm]))

(defn extract [{:keys [content] :as page}]
  ;; TODO remove nil key -- idiomatic to just exclude nil keys
  (->> content fm/parse-front-matter (merge page)))
