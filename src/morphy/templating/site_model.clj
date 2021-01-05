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

(defn build [{:keys [pages/templatable root-url] :as context}]
  (update context :site/model merge (-> (group-by get-parent-name templatable)
                                        (assoc :site/groups (get-groups context))
                                        (assoc :meta/last-modified (util/now))
                                        (assoc :meta/root-url root-url))))
