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
  (-> (:template front-matter) (or (first-found-template input-dir path))
      slurp))

(defn- get-partials [input-dir path]
  ;; gather partials for path context here to use in templates
  ;; closest one gets used, pass them all to mustache
  {})

(defn- render-template [input-dir {:keys [path front-matter] :as page}]
  (let [template (get-template input-dir page)
        vars (-> page
                 (dissoc :front-matter)
                 (merge front-matter))
        partials (get-partials input-dir path)]
    (assoc page :content (m/render template vars partials))))

(defn- templatable? [path]
  ;; is in _assets
  ;; (= "assets" (fs/parent path))
  (contains? #{"md" "html"} (fs/ext path)))

(defn render [input-dir {:keys [path] :as page}]
  (if (templatable? path)
    (render-template input-dir page)
    page))
