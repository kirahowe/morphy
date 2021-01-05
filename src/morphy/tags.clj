(ns morphy.tags
  (:require [datoteka.core :as fs]
            [morphy.content :as content]
            [morphy.util :as util]
            [morphy.templating.page-data :as page-data]))

(def default-tag-index-template
  "# {{ tag/label }}

{{# tag/items }}
- [{{ title }}]({{ slug }})
{{/ tag/items }} ")

(defn- ->index-page [{:keys [tag/label] :as tag}]
  (merge tag
         {:content default-tag-index-template
          :site/path (fs/path "tags" (util/slugify label) "index.md")}))

(defn- add-mustache-ext [{:keys [site/path] :as page}]
  (assoc page :site/path (fs/path (str path ".mustache"))))

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

(defn generate-index-pages [{{:keys [site/tags]} :site/model :as context}]
  (->> tags
       (map ->index-page)
       (map content/process)
       (map page-data/populate)
       (map add-mustache-ext)
       (update context :pages/templatable concat)


      ;; for each tag
      ;; add a page to templatable pages for each tag
      ;; assume /tag/tag-name path
      ;; assume no layout?
      ;;   maybe a default tag layout?
      ;;   maybe look for layout in /tags/_layout.mustache
      ;;   this could use the same layout, just replacing the content with a partial

      ))
