(ns morphy.templates
  (:require [datoteka.core :as fs]
            [cljstache.core :as m]
            [clojure.string :as str]
            [morphy.templating.page-data :as pd]
            [morphy.templating.site-model :as sm]))

(defn- strip-final-ext [path]
  (-> path fs/split-ext first fs/path))

(defn- get-parent-path [path]
  (or (and path (fs/parent path)) ""))

(defn- get-template-path [path input-dir]
  (fs/path input-dir (get-parent-path path) "_layout.mustache"))

(defn- first-found-template [path input-dir]
  (when path
    (let [template-path (get-template-path path input-dir)]
      (if (fs/exists? template-path)
        template-path
        (recur (fs/parent path) input-dir)))))

(defn- find-named-template [name input-dir]
  (let [template-path (fs/path (str input-dir "/_layouts/" name ".mustache"))]
    (when (fs/exists? template-path)
      template-path)))

(def null-layout "{{{content}}}")

(defn- find-layout [{:keys [site/source-path layout]} input-dir]
  (if-let [template-path (or (find-named-template layout input-dir)
                             (first-found-template source-path input-dir))]
    (slurp template-path)
    null-layout))

(defn- get-layout [{:keys [:site/no-layout] :as page} input-dir]
  (if no-layout
    null-layout
    (find-layout page input-dir)))

(defn- strip-extensions[path]
  (str/replace path #"\..+$" ""))

(defn- get-partials-dir [parent input-dir]
  (fs/path input-dir parent "_partials/"))

(defn- load-partials [partials-dir]
  (if (fs/exists? partials-dir)
    (->> partials-dir
         fs/file
         file-seq
         (remove fs/directory?)
         (map #(fs/relativize % partials-dir))
         (map (fn [path]
                [(-> path strip-extensions keyword)
                 (-> (fs/path partials-dir path) slurp str/trim)]))
         (into {}))
    {}))

(defn- get-partials [path input-dir partials]
  (let [parent (get-parent-path path)
        partials-dir (get-partials-dir parent input-dir)
        with-next-level-partials (merge (load-partials partials-dir) partials)]
    (if (= "" parent)
      with-next-level-partials
      (recur parent input-dir with-next-level-partials))))

(defn- render-mustache [site-model partials template]
  (m/render template site-model partials))

(defmulti insert-site-data (fn [_site-model _partials {:keys [site/path]}] (fs/ext path)))

(defmethod insert-site-data "mustache" [site-model partials page]
  (-> page
      (update :site/path strip-final-ext)
      (update :content (partial render-mustache
                                (merge site-model (dissoc page :content))
                                partials))))

(defmethod insert-site-data :default [_site-model _partials page]
  page)

(defn- insert-into-layout [site-model layout partials page]
  (assoc page :content (m/render layout (merge page site-model) partials)))

(defn- template-page [site-model input-dir {:keys [site/source-path] :as page}]
  (let [layout (get-layout page input-dir)
        partials (get-partials source-path input-dir {})]
    ((comp
       (partial insert-into-layout site-model layout partials)
       (partial insert-site-data site-model partials))
     page)))

(defn render [{:keys [pages/templatable site/model input-dir] :as context}]
  (->> templatable
       (map (partial template-page model input-dir))
       (assoc context :pages/templatable)))
