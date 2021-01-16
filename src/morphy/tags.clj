(ns morphy.tags
  (:require [datoteka.core :as fs]
            [morphy.content :as content]
            [morphy.util :as util]
            [morphy.templating.page-data :as page-data]
            [clojure.string :as str]))

(defn- ->tag [[label items]]
  {:tag/label label
   :tag/count (count items)
   :tag/items items
   :tag/slug (str "/tags/" (util/slugify label))})

(defn populate [{:keys [pages/templatable] :as context}]
  (->> templatable
       (reduce (fn [result {:keys [site/tags] :as page}]
                 (reduce (fn [r tag] (util/push r tag page)) result tags))
               {})
       (map ->tag)
       (sort-by :tag/label)
       (assoc-in context [:site/model :site/tags])))

(def default-tag-index-template
  "<h1>{{ tag/label }}</h1>
<ul>
{{# tag/items }}
<li><a href={{ slug }}>{{ title }}</a></li>
{{/ tag/items }}
</ul>")

(defn- ->index-page [{:keys [tag/label tag/slug] :as tag}]
  (let [path (fs/path (str/replace-first slug "/" "") "index.html.mustache")]
    (merge tag
           {:content default-tag-index-template
            :site/path path :site/source-path path})))

(defn generate-index-pages [context]
  ;; (sc.api/spy)
  (->> context
       :site/model
       :site/tags
       (map ->index-page)
       (update context :pages/templatable concat)


      ;; for each tag
      ;; add a page to templatable pages for each tag
      ;; assume /tag/tag-name path
      ;; assume no layout?
      ;;   maybe a default tag layout?
      ;;   maybe look for layout in /tags/_layout.mustache
      ;;   this could use the same layout, just replacing the content with a partial

      ))
