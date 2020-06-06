(ns dynamo.templates
  (:require [datoteka.core :as fs]
            [clostache.parser :as m]))

(defn- get-template-path [input-dir path]
  (let [parent-path (-> path
                        (and (fs/parent path))
                        (or ""))]
    (fs/path input-dir parent-path "_file.mustache")))

(defn- first-found-template [input-dir path]
  (let [template-path (get-template-path input-dir path)]
    (if (fs/exists? template-path)
      template-path
      (recur input-dir (fs/parent path)))))

(defn- get-template [input-dir {:keys [front-matter path]}]
  (-> ;; (:template front-matter) TODO: support custom templates
      nil
      (or (first-found-template input-dir path))
      slurp))

(defn- get-partials [input-dir path]
  ;; gather partials for path context here to use in templates
  ;; closest one gets used, pass them all to mustache
  ;; right now just gets list of partials in root dir, assume all just there (flat), no nested ones
  (-> input-dir
      (fs/path "_partials")
      fs/list-dir))



(defn- template [input-dir {:keys [path front-matter] :as page}]
  ;; (if (= "mustache" (fs/ext path))
  ;;   (recur (render-content page))
  ;;   (insert-into-layout page))
  (let [template (fs/path input-dir "_layout.mustache")
        vars (-> page
                 (dissoc :front-matter)
                 (merge front-matter))
        partials (get-partials input-dir path)]
    (assoc page :content (m/render template vars partials))))

(defn templatable? [path]
  (let [parent (some-> path fs/parent fs/name str)]
    (or (nil? parent) (not= "assets" parent))))

(defn render [input-dir {:keys [path] :as page}]
  (if (templatable? path)
    (template input-dir page)
    page))
