(ns dynamo.core
  (:gen-class)
  (:require [datoteka.core :as fs]
            [dynamo.content :as content]
            [dynamo.data :as data]
            [dynamo.templates :as templates]
            [dynamo.front-matter :as front-matter]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(require 'sc.api)

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
  ;; TODO return something useful here -- what got written, maybe? success/error?
  (let [file-name (fs/path output-dir path)]
    (ensure-dir! file-name)
    (spit file-name content)
    (println "Wrote " path)))

(defn- write-files [{:keys [data] :as context}]
  (doall (map (partial write-file! context) data)))

(defn- insert-into-templates [{:keys [input-dir data] :as context}]
  (->> data
       (map (partial templates/render input-dir))
       (assoc context :data)))

(defn- extract-front-matter [{:keys [data] :as context}]
  (->> data (map front-matter/extract) (assoc context :data)))

(defn- convert-to-html [{:keys [data] :as context}]
  (->> data (map content/process) (assoc context :data)))

;; (s/fdef process
;;   :args (s/coll-of ::content/page)
;;   :ret (s/coll-of ::content/page)
;;   :fn #(= (-> % :ret count) (-> % :args count)))

(defn load-data [{:keys [input-dir] :as context}]
  (->> input-dir data/load-pages (assoc context :data)))

;; (s/def ::input-dir string?)
;; (s/def ::output-dir string?)
;; (s/def ::initial-context (s/keys :req-un [::input-dir ::output-dir]))
;; (s/fdef generate-site
;;   :args ::initial-context)

(defn generate-site [context]
  (->> context
       load-data
       extract-front-matter
       convert-to-html
       insert-into-templates
       write-files))

(defn -main
  "Generates a static website"
  [context]
  (generate-site context))
