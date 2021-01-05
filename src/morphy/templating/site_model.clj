(ns morphy.templating.site-model
  (:require [clojure.string :as str]
            [datoteka.core :as fs]
            [morphy.util :as util]))

(defn- get-parent [path]
  (-> path fs/parent (or "") fs/parent (or "root")))

(defn- get-parent-name [{:keys [site/path]}]
  (let [parent-dir (get-parent path)]
    (-> parent-dir str (str/replace #"\/index\..+$" "") keyword)))

(defn- ->group [[label items]]
  (when label
    {:group/label label
     :group/count (count items)
     :group/items items}))

(defn get-group-sort-priority [sort-priority page]
  (let [priority (.indexOf sort-priority (:group/label page))]
    (if (= -1 priority)
      (Double/POSITIVE_INFINITY)
      priority)))

(defn- get-groups [{:keys [pages/templatable groups/sort-priority]
                    :or {sort-priority []}}]
  (->> templatable
       (group-by :site/group)
       (map ->group)
       (remove nil?)
       (sort-by :group/label)
       (sort-by (partial get-group-sort-priority sort-priority))))


(defn- ->tag [[label items]]
  {:tag/label label
   :tag/count (count items)
   :tag/items items})

(defn- get-tags [{:keys [pages/templatable]}]
  (->> templatable
       (reduce (fn [result {:keys [site/tags] :as page}]
                 (reduce (fn [r tag] (util/push r tag page)) result tags))
               {})
       (map ->tag)
       (sort-by :tag/label)))

(defn build [{:keys [pages/templatable root-url] :as context}]
  (assoc context :site/model (-> (group-by get-parent-name templatable)
                                 (assoc :site/groups (get-groups context))
                                 (assoc :site/tags (get-tags context))
                                 (assoc :meta/last-modified (util/now))
                                 (assoc :meta/root-url root-url))))

;; tags:
;; - auto-generate an index page in `tags` for each tag
;; - allow customizing the name of this directory
;; - allow customizing the layout of these index pages

