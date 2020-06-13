(ns dynamo.templates
  (:require [datoteka.core :as fs]
            [clostache.parser :as m]
            [clojure.string :as str]
            [dynamo.util :as u]))

;; (defn- get-template-path [input-dir path]
;;   (let [parent-path (-> path
;;                         (and (fs/parent path))
;;                         (or ""))]
;;     (fs/path input-dir parent-path "_file.mustache")))

;; (defn- first-found-template [input-dir path]
;;   (let [template-path (get-template-path input-dir path)]
;;     (if (fs/exists? template-path)
;;       template-path
;;       (recur input-dir (fs/parent path)))))

;; (defn- get-template [input-dir {:keys [front-matter path]}]
;;   (-> ;; (:template front-matter) TODO: support custom templates
;;       nil
;;       (or (first-found-template input-dir path))
;;       slurp))

(defn templatable? [path]
  (not (str/includes? path "assets/")))

(defn- strip-final-ext [path]
  (-> path fs/split-ext first fs/path))

(defn- render-mustache [site partials content]
  (m/render content {:site site} partials))

(defmulti insert-template-data (fn [_context {:keys [path]}] (fs/ext path)))

(defmethod insert-template-data "mustache" [{:keys [site partials]} page]
  (-> page
      (update :path strip-final-ext)
      (update :content (partial render-mustache site partials))))

(defmethod insert-template-data :default [_context page]
  page)

(defn- flatten-front-matter [{:keys [front-matter] :as page}]
  (merge (dissoc page :front-matter) front-matter))

(defn- compile-partials [{:keys [partials] :as context}]
  (->> partials
       (u/map-values (comp :content (partial insert-template-data context)))
       (assoc context :partials)))

(defn- compile-templated-files [{:keys [site] :as context}]
  (->> site
       (u/map-leaves (partial insert-template-data context))
       (assoc context :site)))

(defn- compile-page [layout partials page]
  (assoc page :content (m/render layout page partials)))

(defn- insert-into-layout [{:keys [site partials input-dir] :as context}]
  (let [default-layout (slurp (fs/path input-dir "_layout.mustache"))]
    (->> site
         (u/map-leaves (partial compile-page default-layout partials))
         (assoc context :site))))

(defn render [context]
  (-> context
      (update :site (partial u/map-leaves flatten-front-matter))
      compile-partials
      compile-templated-files
      insert-into-layout
      (dissoc :partials)))
