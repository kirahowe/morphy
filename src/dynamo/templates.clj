(ns dynamo.templates
  (:require [datoteka.core :as fs]
            [clostache.parser :as m]
            [clojure.string :as str]
            [clojure.walk :as walk])
  (:import java.text.SimpleDateFormat))

(defn- strip-final-ext [path]
  (-> path fs/split-ext first fs/path))

(defn- flatten-front-matter [{:keys [front-matter] :as page}]
  (merge (dissoc page :front-matter) front-matter))

(defn populate-slug [{:keys [path] :as page}]
  (let [parent-path (when-let [p (fs/parent path)] (str "/" p))
        slug (str parent-path "/")]
    (-> page
        (assoc :slug slug)
        (assoc :canonical-slug (str slug "index.html")))))

(defn format-dates [page]
  (let [f (fn [[k v]] (if (= java.util.Date (type v))
                        [k (.format (SimpleDateFormat. "MMMM dd, yyyy") v)]
                        [k v]))]
    (walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) page)))

(defn templatable? [path]
  (not (str/includes? (str path) "assets/")))

(defn- get-parent-path [path]
  (or (and path (fs/parent path)) ""))

(defn- get-template-path [path input-dir]
  (fs/path input-dir (get-parent-path path) "_layout.mustache"))

(defn- first-found-template [path input-dir]
  (let [template-path (get-template-path path input-dir)]
    (if (fs/exists? template-path)
      template-path
      (recur (fs/parent path) input-dir))))

(defn- find-named-template [name input-dir]
  (let [template-path (fs/path (str input-dir "/_layouts/" name ".mustache"))]
    (when (fs/exists? template-path)
      template-path)))

(defn- get-layout [{:keys [path layout]} input-dir]
  (slurp (or (find-named-template layout input-dir)
             (first-found-template path input-dir))))

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

(defmulti insert-site-data (fn [_site-model _partials {:keys [path]}] (fs/ext path)))

(defmethod insert-site-data "mustache" [site-model partials page]
  (-> page
      (update :path strip-final-ext)
      (update :content (partial render-mustache
                                (merge site-model (dissoc page :content))
                                partials))))

(defmethod insert-site-data :default [_site-model _partials page]
  page)

(defn- insert-into-layout [site-model layout partials page]
  (assoc page :content (m/render layout (merge page site-model) partials)))

;; (require 'sc.api)
(defn- template-page [site-model input-dir {:keys [path] :as page}]
  ;; TODO: handle this differently-- grab out all the assets first or something
  (if (templatable? path)
    (let [layout (get-layout page input-dir)
          partials (get-partials path input-dir {})]
      ((comp
         (partial insert-into-layout site-model layout partials)
         (partial insert-site-data site-model partials))
       page))
    page))

(defn- get-parent [path]
  (cond-> (-> path fs/parent (or ""))
    (templatable? path) (-> fs/parent (or "root"))))

(defn- get-group-name [{:keys [path]}]
  (let [parent-dir (get-parent path)]
    (-> parent-dir str (str/replace #"\/index\..+$" "") keyword)))

(defn render [{:keys [pages input-dir] :as context}]
  (let [pages* (map (comp populate-slug flatten-front-matter format-dates) pages)
        site-model (group-by get-group-name pages*)]
    (->> pages*
         (map (partial template-page site-model input-dir))
         (assoc context :pages))))
