(ns dynamo.data
  (:require [datoteka.core :as fs]
            [clojure.string :as str]
            [dynamo.templates :as templates]
            [clojure.java.io :as io]))

(defn- make-dir-with-index [path]
  (let [[_ name exts] (re-matches #"([^\..]+)(\..+)$" (str path))]
    (fs/path name (str "index" exts))))

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

(defn ->page [input-dir path]
  (cond-> {:path (get-path input-dir path)}
    (templates/templatable? path) (assoc :content (slurp path))))

(defn load-pages [input-dir]
  (->> input-dir
       io/file
       file-seq
       (remove fs/directory?)
       (remove template?)
       (map (partial ->page input-dir))))
