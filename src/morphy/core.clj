(ns morphy.core
  (:gen-class)
  (:require [datoteka.core :as fs]
            [morphy.content :as content]
            [morphy.data :as data]
            [morphy.templates :as templates]
            [morphy.metadata :as metadata]
            [clojure.java.io :as io]))

;; (require 'sc.api)

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

(defn- write-file! [{:keys [output-dir input-dir]} {:keys [site/path content]}]
  (let [file-name (fs/path output-dir path)]
    (ensure-dir! file-name)
    (if content
      (spit file-name content)
      (io/copy (io/file input-dir path) (io/file file-name)))
    (println "Wrote " (str file-name))))

(defn- write-files [{:keys [pages/assets pages/templatable output-dir] :as context}]
  (ensure-dir! output-dir)
  (doall
    (for [page (concat assets templatable)]
      (write-file! context page)))
  (println "Success!"))

(defn- update-templatable [pages f]
  (update pages :pages/templatable (partial map f)))

(defn build-pages [input-dir]
  (-> input-dir
      data/load-pages
      (update-templatable metadata/extract)
      (update-templatable content/process)))

(defn generate-site [{:keys [input-dir] :as context}]
  (-> context
      (merge (build-pages input-dir))
      templates/render
      write-files))

(defn -main
  "Generates a static website"
  [context]
  (generate-site context))

(comment
  (def input-dir "/Users/kmclean/code/projects/blog/site")
  (def output-dir "/Users/kmclean/code/projects/blog/dist")
  (def context {:input-dir input-dir :output-dir output-dir :root-url "https://kiramclean.com"})

  (generate-site context))
