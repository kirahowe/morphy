(ns morphy.data
  (:require [datoteka.core :as fs]
            [clojure.string :as str]
            [morphy.templates :as templates]
            [clojure.java.io :as io]
            [front-matter.core :as fm]
            [morphy.util :as util]))

(defn- make-dir-with-index [path]
  (let [[_ name exts] (re-matches #"([^\..]+)(\..+)$" (str path))]
    (fs/path name (str "index" exts))))

(defn- expand-path? [relative-path {:keys [:site/leave-path-alone]}]
  (and (not (str/includes? relative-path "index."))
       (not leave-path-alone)))

(defn- get-path [relative-path metadata]
  (if (expand-path? relative-path metadata)
    (make-dir-with-index relative-path)
    relative-path))

(defn- named-as-template? [path]
  (-> path fs/name (str/starts-with? "_")))

(defn- template? [path]
  (when path
    (or (named-as-template? path)
        (recur (fs/parent path)))))

(defn- templatable? [path]
  (not (str/includes? (str path) "assets/")))

(defn ->page [relative-path original-path]
  (let [parsed (-> original-path slurp fm/parse-front-matter)
        new-path (get-path relative-path (:front-matter parsed))]
    (assoc parsed :site/path new-path :site/source-path new-path)))

(defn- ->pages [input-dir pages file]
  (let [relative-path (fs/relativize file input-dir)]
    (if (templatable? relative-path)
      (util/push pages :pages/templatable (->page relative-path file))
      (util/push pages :pages/assets {:site/path relative-path}))))

(defn load-pages [input-dir]
  (->> input-dir
       io/file
       file-seq
       (remove fs/directory?)
       (remove template?)
       sort
       reverse
       (reduce (partial ->pages input-dir) {})))
