(ns dynamo.data
  (:require [datoteka.core :as fs]
            [clojure.string :as str]
            [dynamo.templates :as templates]))

(defn- make-dir-with-index [path]
  (let [name (fs/name path)
        ext (fs/ext path)]
    (fs/path name (str "index." ext))))

(defn- leaf-file? [path]
  (and (templates/templatable? path)
       (not= "index" (fs/name path))))

(defn- get-path [input-dir path]
  (let [relative-path (fs/relativize path input-dir)]
    (if (leaf-file? relative-path)
      (make-dir-with-index relative-path)
      relative-path)))

(defn- ->page [input-dir path]
  {:path (get-path input-dir path)
   :content "content would go here";; (slurp path)
   })

(defn- named-as-template? [path]
  (-> path fs/name (str/starts-with? "_")))

(defn- template? [path]
  (-> (named-as-template? path)
      (or (when-let [parent (fs/parent path)]
            (template? parent)))))

(defn- group-name [path]
  (if (templates/templatable? path)
    :to-process
    :to-copy))

(defn- load-processable-files [data]
  (update data :to-process ->page))

(defn load-pages [input-dir]
  (sc.api/spy)
  (->> input-dir
       fs/file
       file-seq
       (filter (complement fs/directory?))
       (remove template?)
       (map (partial load-file input-dir))))
