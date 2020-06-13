(ns dynamo.data
  (:require [datoteka.core :as fs]
            [clojure.string :as str]
            [dynamo.templates :as templates]
            [dynamo.util :as u]))

(defn- make-dir-with-index [path]
  (let [[name ext] (fs/split-ext path)]
    (fs/path name (str "index" ext))))

(defn- leaf-file? [path]
  (and (templates/templatable? path)
       (not (str/includes? path "index."))))

(defn- get-path [input-dir path]
  (let [relative-path (fs/relativize path input-dir)]
    (if (leaf-file? relative-path)
      (make-dir-with-index relative-path)
      relative-path)))

(defn- named-as-template? [path]
  (-> path fs/name (str/starts-with? "_")))

(defn- template? [path]
  (when path
    (or (named-as-template? path)
        (recur (fs/parent path)))))

(defn- ->page [input-dir path]
  (cond-> {:path (get-path input-dir path)}
    (templates/templatable? path) (assoc :content (slurp path))))

(declare expand-dir)

(defn- process-node [input-dir node]
  (if (seq? node)
    (map (partial ->page input-dir) node)
    (expand-dir input-dir node)))

(defn- add-directory [site path]
  (assoc site (keyword (fs/name path)) path))

(defn- push-child [site path]
  (update site :children (partial cons path)))

(defn- load-path [site path]
  (if (fs/directory? path)
    (add-directory site path)
    (push-child site path)))

(defn- expand-dir [input-dir path]
  (->> path
       fs/list-dir
       (remove template?)
       (reduce load-path {})
       (u/map-values (partial process-node input-dir))))

(defn load-site [input-dir]
  (expand-dir input-dir input-dir))

(defn- strip-extentions [path]
  (str/replace path #"\..+$" ""))

(defn- ->partial [partials-dir path]
  {:path path
   :content (slurp (fs/path partials-dir path))})

(defn load-partials [input-dir]
  (let [partials-dir (fs/path input-dir "_partials")]
    (->> partials-dir
         fs/file
         file-seq
         (remove fs/directory?)
         (map #(fs/relativize % partials-dir))
         (map (fn [path]
                [(-> path strip-extentions keyword)
                 (->partial partials-dir path)]))
         (into {}))))
