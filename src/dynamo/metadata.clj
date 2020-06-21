(ns dynamo.metadata
  (:require [front-matter.core :as fm]
            [datoteka.core :as fs]))

(defn- first-line-plaintext [content]
  (or (re-find #"[^#*\s].+" content) ""))

(defn- remove-from-front-matter [{:keys [front-matter] :as page} kw]
  (let [updated-fm (dissoc front-matter kw)]
    (if (empty? updated-fm)
      (dissoc page :front-matter)
      (assoc page :front-matter updated-fm))))

(defn- fill-in-defaults [{{:keys [title]} :front-matter
                          content :content
                          :as page}]
  (-> page
      (assoc :title (or title (first-line-plaintext content)))
      (remove-from-front-matter :title)))

(defn- rename-slug [slug path]
  (let [name (fs/name path)
        current-slug (fs/parent path)]
    (fs/path (or (fs/parent current-slug) "") slug name)) )

(defn- custom-slug [{{:keys [slug]} :front-matter :as page}]
  (-> page
      (update :path (partial rename-slug slug))
      (remove-from-front-matter :slug)))

(defn- extract-front-matter [{:keys [content] :as page}]
  (let [parsed (fm/parse-front-matter content)]
    (cond-> (merge page parsed)
      (get-in parsed [:front-matter :slug]) custom-slug)))

(defn extract [{:keys [content] :as page}]
  (if content
    (-> page
        extract-front-matter
        fill-in-defaults)
    page))
