(ns dynamo.metadata
  (:require [front-matter.core :as fm]))

(defn extract [{:keys [content] :as page}]
  ;; TODO -- extract title, slug maybe? Use slug if given one
  (if content
    (->> content fm/parse-front-matter (merge page))
    page))
