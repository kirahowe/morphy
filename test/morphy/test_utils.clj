(ns morphy.test-utils
  (:require
   ;; [clojure.spec.gen.alpha :as gen]
            [datoteka.core :as fs]
            ;; [clojure.string :as str]
            [morphy.data :as data]))

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
