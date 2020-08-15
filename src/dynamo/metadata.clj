(ns dynamo.metadata
  (:require [front-matter.core :as fm]
            [datoteka.core :as fs]
            [clojure.string :as str]))

(defn- first-header-as-plaintext [content]
  (let [[_match header] (re-find #"^#*\s(.+)" (str/trim content))]
    header))

(defn- remove-from-front-matter [{:keys [front-matter] :as page} kw]
  (let [updated-fm (dissoc front-matter kw)]
    (if (empty? updated-fm)
      (dissoc page :front-matter)
      (assoc page :front-matter updated-fm))))

(defn- add-title [{{:keys [title]} :front-matter
                   content :content
                   :as page}]
  (let [title (or title (first-header-as-plaintext content))]
    (cond-> page
      title (assoc :title (or title (first-header-as-plaintext content))
                   :has-title? true))))

(defn- fill-in-defaults [page]
  (-> page
      add-title
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
