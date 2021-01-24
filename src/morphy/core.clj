(ns morphy.core
  (:gen-class)
  (:require [datoteka.core :as fs]
            [morphy.content :as content]
            [morphy.data :as data]
            [morphy.templates :as templates]
            [morphy.metadata :as metadata]
            [morphy.templating.page-data :as page-data]
            [morphy.templating.site-model :as site-model]
            [clojure.java.io :as io]))

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
      (update-templatable content/process)
      (update-templatable page-data/populate)))

(defn build-site [{:keys [input-dir] :as context}]
  (-> context (merge (build-pages input-dir)) site-model/build templates/render))

(defn generate-site [context]
  (-> context build-site write-files))

(defn -main
  "Generates a static website"
  [context]
  (generate-site context))

(comment
  (def input-dir "/Users/kira/code/projects/blog/site")
  (def output-dir "/Users/kira/code/projects/blog/dist")
  (def context {:input-dir input-dir
                :output-dir output-dir
                :root-url "https://kiramclean.com"
                :groups/sort-priority ["Recent" "Tech"]})

  (generate-site context))
