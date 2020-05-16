(ns dynamo.content
  (:require [datoteka.core :as fs]
            [markdown.core :as md]))

(require 'sc.api)
(defn- ext-to-html [path]
  (sc.api/spy)
  (println "PATH: ", path)
  (-> path fs/split-ext first (str ".html") fs/path))

(defmulti process (fn [{:keys [path]}] (fs/ext path)))

(defmethod process "md" [data]
  (update data :path ext-to-html :content md/md-to-html))

(defmethod process :default [{:keys [content]}]
  content)
