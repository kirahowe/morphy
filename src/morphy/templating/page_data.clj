(ns morphy.templating.page-data
  (:require [datoteka.core :as fs]
            [morphy.util :as util]))

(defn populate-slug [{:keys [site/path] :as page}]
  (let [parent-path (when-let [p (fs/parent path)] (str "/" p))
        slug (str parent-path "/")]
    (assoc page :slug slug)))

(defn format-dates [{:keys [date] :as page}]
  (cond-> page
    date (assoc :rss-date (util/->rfc-1123-date (:date page)))
    date (update :date util/format-date)))

(defn- flatten-front-matter [{:keys [front-matter] :as page}]
  (merge (dissoc page :front-matter) front-matter))

(defn populate [page]
  ((comp populate-slug
         format-dates
         flatten-front-matter)
   page))
