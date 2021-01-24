(ns morphy.tags
  (:require [datoteka.core :as fs]
            [morphy.content :as content]
            [morphy.util :as util]
            [morphy.templating.page-data :as page-data]
            [clojure.string :as str]
            [morphy.templating.common :as c]))

(defn- ->tag [[label items]]
  {:tag/label label
   :tag/count (count items)
   :tag/items items
   :tag/slug (str "/tags/" (util/slugify label) "/")})

(def ^:private default-tag-index-content
  "<h1>{{ tag/label }}</h1>
<ul>
{{# tag/items }}
<li><a href={{ slug }}>{{ title }}</a></li>
{{/ tag/items }}
</ul>")

(defn- get-named-template [{:keys [input-dir]} template-name]
  (when-let [template (c/find-named-template template-name input-dir)]
    (slurp template)))

(defn- get-html-index-content [context]
  (or (get-named-template context "morphy-tag-html-index")
      default-tag-index-content))

(defn- page-data [{:keys [tag/slug tag/label]} file-name]
  (let [path (fs/path (str/replace-first slug "/" "") file-name)]
    {:title (str/capitalize label)
     :site/path path
     :site/source-path path}))

(defn- generate-html-index [context tag]
  (merge tag
         (page-data tag "index.html.mustache")
         {:content (get-html-index-content context)}))

(def ^:private default-rss-feed-content
  (slurp "resources/default-rss-tag-feed.xml.mustache"))

(defn- get-rss-feed-content [context]
  (or (get-named-template context "morphy-tag-rss-index")
      default-rss-feed-content))

(defn- generate-rss-feed [context {:keys [tag/slug] :as tag}]
  (merge tag
         (page-data tag "feed.xml.mustache")
         {:content (get-rss-feed-content context)
          :site/leave-path-alone true
          :site/no-layout true}))

(defn- ->index-pages [context tag]
  [(generate-html-index context tag)
   (generate-rss-feed context tag)])

(defn- populate-tags [{:keys [pages/templatable] :as context}]
  (->> templatable
       (reduce (fn [result {:keys [site/tags] :as page}]
                 (reduce (fn [r tag] (util/push r tag page)) result tags))
               {})
       (map ->tag)
       (sort-by :tag/label)
       (assoc-in context [:site/model :site/tags])))

(defn- generate-index-pages [context]
  (->> context
       :site/model
       :site/tags
       (mapcat (partial ->index-pages context))
       (update context :pages/templatable concat)))

(defn generate [context]
  (-> context populate-tags generate-index-pages))
