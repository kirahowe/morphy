(ns morphy.content
  (:require [datoteka.core :as fs]
            ;; [clojure.spec.alpha :as s]
            )
  (:import org.commonmark.parser.Parser
           org.commonmark.renderer.html.HtmlRenderer))

;; (defn- matches-ext [ext test-path]
;;   (re-find (re-pattern (str "\\." ext "$")) (str test-path)))

;; (s/def ::path fs/path?)
;; (s/def ::content string?)
;; (s/def ::page (s/keys :req-un [::path ::content]))

;; (s/def ::html/path (s/and ::path (partial matches-ext "html")))
;; (s/def ::html/page (s/keys :req-un [::html/path ::content ::layout]
;;                            :opt-un [::slug]))

;; (s/fdef ext-to-html
;;   :args ::path
;;   :ret ::html/path
;;   :fn #(= (fs/name (:args %)) (fs/name (:ret %))))

(defn- ext-to-html [path]
  (-> path fs/split-ext first (str ".html") fs/path))

(defmulti process (fn [{:keys [site/path]}] (fs/ext path)))

(defn- markdown->html [content]
  (let [parsed (-> (Parser/builder) .build (.parse content))]
    (-> (HtmlRenderer/builder) .build (.render parsed))))

(defmethod process "md" [page]
  (-> page
      (update :site/path ext-to-html)
      (update :content markdown->html)))

(defmethod process :default [page]
  page)
