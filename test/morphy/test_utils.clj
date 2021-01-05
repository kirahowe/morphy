(ns morphy.test-utils
  (:require
   [datoteka.core :as fs]
   [morphy.data :as data]
   [morphy.core :as core]))

;; (defn non-blank-list-gen [g]
;;   (gen/such-that #(seq %) (gen/list g)))

;; (def non-blank-string-gen (gen/such-that #(not= % "") (gen/string-alphanumeric)))

;; (defn path-gen [ext]
;;   (gen/fmap (comp fs/path
;;                   (fn [& args]
;;                     (str (apply str/join "/" args) "." ext)))
;;             (non-blank-list-gen non-blank-string-gen)))

(def resources "test/morphy/resources/")

(defn test-page
  "Generates a test page, defaults file to  being relative to resources path"
  ([file]
   (test-page file resources))
  ([root file]
   (let [full-path (fs/path root file)]
     (data/->page file full-path))))

(defn get-site [dir]
  (let [input-dir (str resources dir)]
    (-> {:input-dir input-dir :root-url "https://test.com"}
        (core/build-site))))

(defn get-content [site search]
  (->> site
       :pages/templatable
       (filter (fn [{:keys [site/path]}]
                 (= search (-> path fs/parent (or "") fs/name str))))
       first
       :content))
