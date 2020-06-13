(ns dynamo.content
  (:require [datoteka.core :as fs]
            [markdown.core :as md]
            [clojure.spec.alpha :as s]
            [dynamo.content.html :as html]))

(defn- matches-ext [ext test-path]
  (re-find (re-pattern (str "\\." ext "$")) (str test-path)))

(s/def ::path fs/path?)
(s/def ::content string?)
(s/def ::page (s/keys :req-un [::path ::content]))

(s/def ::html/path (s/and ::path (partial matches-ext "html")))
(s/def ::html/page (s/keys :req-un [::html/path ::content ::layout]
                           :opt-un [::slug]))

(s/fdef ext-to-html
  :args ::path
  :ret ::html/path
  :fn #(= (fs/name (:args %)) (fs/name (:ret %))))

(defn- ext-to-html [path]
  (-> path fs/split-ext first (str ".html") fs/path))

(defmulti process (fn [{:keys [path]}] (fs/ext path)))

(defmethod process "md" [page]
  (-> page
      (update :path ext-to-html)
      (update :content md/md-to-html-string)))

(defmethod process :default [page]
  page)
