(ns morphy.templating.common
  (:require [datoteka.core :as fs]))

(defn find-named-template [name input-dir]
  (let [template-path (fs/path (str input-dir "/_layouts/" name ".mustache"))]
    (when (fs/exists? template-path)
      template-path)))
