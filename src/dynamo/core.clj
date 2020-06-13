(ns dynamo.core
  (:gen-class)
  (:require [datoteka.core :as fs]
            [dynamo.content :as content]
            [dynamo.data :as data]
            [dynamo.templates :as templates]
            [dynamo.metadata :as metadata]
            [clojure.spec.alpha :as s]
            [dynamo.util :as u]))

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

(defn- write-file! [{:keys [output-dir]} {:keys [path content] :as page}]
  (let [file-name (fs/path output-dir path)]
    (ensure-dir! file-name)
    (spit file-name content)
    (println "Wrote " path)
    (assoc page :success true)))

(defn- write-files [{:keys [site] :as context}]
  (doall (u/map-leaves (partial write-file! context) site)))

;; (defn- insert-into-templates [{:keys [input-dir to-process] :as context}]
;;   (->> to-process
;;        (map (partial templates/render input-dir))
;;        (assoc context :to-process)))

;; (s/def ::input-dir string?)
;; (s/def ::output-dir string?)
;; (s/def ::initial-context (s/keys :req-un [::input-dir ::output-dir]))
;; (s/fdef generate-site
;;   :args ::initial-context)

(defn generate-site [{:keys [input-dir] :as context}]
  (-> context
      (assoc :site (data/load-site input-dir))
      (assoc :partials (data/load-partials input-dir))
      (update :site (partial u/map-leaves metadata/extract))
      (update :site (partial u/map-leaves content/process))
      templates/render
      write-files
      ))

(defn -main
  "Generates a static website"
  [context]
  (generate-site context))
