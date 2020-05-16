(ns dynamo.core
  (:gen-class)
  (:require [datoteka.core :as fs]
            [dynamo.content :as content]
            [clojure.spec.alpha :as s]))

(defn matches-ext [ext test-path]
  (re-find (re-pattern (str "\\." ext "$")) (str test-path)))

(s/def ::path fs/path?)
(s/def ::html-path (s/and ::path (partial matches-ext "html")))
(s/def ::content string?)
(s/def ::html-page (s/keys :req-un [::html-path ::content] ))
(s/def ::page (s/keys :req-un [::path ::content]))

(defn- ensure-dir! [path]
  (when-let [parent (fs/parent path)]
    (when-not (fs/exists? parent)
      (fs/create-dir parent))))

(defn- write-file! [{:keys [output-dir]} {:keys [path content]}]
  (let [file-name (fs/path output-dir path)]
    (ensure-dir! file-name)
    (spit file-name content)))

(defn- write-files [context data]
  (doall (map (partial write-file! context) data)))

(s/fdef process
  :args (s/coll-of ::page)
  :ret (s/coll-of ::page)
  :fn #(= (-> % :ret count) (-> % :args count)))

(defn- process [data]
  (map content/process data))

(defn- load-path [input-dir path]
  {:path (fs/relativize path input-dir)
   :content (slurp path)})

(defn- load-data [{:keys [input-dir]}]
  (->> input-dir
       fs/file
       file-seq
       (filter (complement fs/directory?))
       (map (partial load-path input-dir))))

(defn generate-site [context]
  (->> context
       load-data
       process
       (write-files context)))

(defn -main
  "Generates a static website"
  [context]
  (generate-site context))
