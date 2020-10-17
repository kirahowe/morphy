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

(defn- rename-slug [slug path]
  (let [name (fs/name path)
        current-slug (fs/parent path)]
    (fs/path (or (fs/parent current-slug) "") slug name)) )

(defn- custom-slug [{{:keys [slug]} :front-matter :as page}]
  (-> page
      (update :path (partial rename-slug slug))
      (remove-from-front-matter :slug)))

(defn- slugify [s]
  (-> s
      str/trim
      str/lower-case
      (str/replace #"\s+" "-")) )

(defn- slugify-title [{:keys [title] :as page}]
  (cond-> page
    title (update :path (partial rename-slug (slugify title)))))

(defn- update-slug [{:keys [title] {:keys [slug]} :front-matter :as page}]
  (cond
    slug (custom-slug page)
    title (slugify-title page)
    :else page))

(defn- fill-in-title [page]
  (-> page
      add-title
      (remove-from-front-matter :title)
      slugify-title))

(defn- extract-front-matter [{:keys [content] :as page}]
  (merge page (fm/parse-front-matter content)))

(defn extract [{:keys [content] :as page}]
  (if content
    (-> page
        extract-front-matter
        fill-in-title
        update-slug)
    page))
