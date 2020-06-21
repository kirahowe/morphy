(ns dynamo.test-utils
  (:require
   ;; [clojure.spec.gen.alpha :as gen]
            [datoteka.core :as fs]
            ;; [clojure.string :as str]
            [dynamo.data :as data]))

;; (defn non-blank-list-gen [g]
;;   (gen/such-that #(seq %) (gen/list g)))

;; (def non-blank-string-gen (gen/such-that #(not= % "") (gen/string-alphanumeric)))

;; (defn path-gen [ext]
;;   (gen/fmap (comp fs/path
;;                   (fn [& args]
;;                     (str (apply str/join "/" args) "." ext)))
;;             (non-blank-list-gen non-blank-string-gen)))

(def resources "test/dynamo/resources/")

(defn test-page
  "Generates a test page, assumes file is relative to resources path"
  [file]
  (data/->page resources (fs/path resources file)))
