(ns morphy.metadata
  (:require [datoteka.core :as fs]
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
      title (assoc :title title :has-title? true))))

(defn- rename-slug [{:keys [site/path] :as page} slug]
  (let [name (fs/name path)
        new-path (fs/path slug name)]
    (assoc page :site/path new-path)))

(defn- slugify [s]
  (some-> s
          str/trim
          str/lower-case
          (str/replace #"\s+" "-")) )

(defn- slug-from-title [{:keys [title site/path]}]
  (let [current-slug (fs/parent path)
        from-title (slugify title)
        current-parent (when current-slug (fs/parent current-slug))
        new-slug (str current-parent (when current-parent "/") from-title)]
    (and current-slug from-title new-slug)))

(defn- get-new-slug [{{custom-slug :slug} :front-matter :as page}]
  (or custom-slug
      (slug-from-title page)))

(defn- update-slug [page]
  (if-let [new-slug (get-new-slug page)]
    (-> page (rename-slug new-slug) (remove-from-front-matter :slug))
    page))

(defn- fill-in-title [page]
  (-> page add-title (remove-from-front-matter :title)))

(defn extract [page]
  (-> page fill-in-title update-slug))
