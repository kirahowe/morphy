(ns morphy.templating.site-model
  (:require [clojure.string :as str]
            [datoteka.core :as fs]
            [morphy.util :as util]))

(defn- assoc-metadata [root-url site]
  (assoc site
         :meta/last-modified (util/now)
         :meta/root-url root-url))

(defn- get-parent [path]
  (-> path fs/parent (or "") fs/parent (or "root")))

(defn- get-group-name [{:keys [site/path]}]
  (let [parent-dir (get-parent path)]
    (-> parent-dir str (str/replace #"\/index\..+$" "") keyword)))

(defn get-site-model [{:keys [pages/ready-to-template root-url]}]
  (->> ready-to-template
       (group-by get-group-name)
       (assoc-metadata root-url)))
