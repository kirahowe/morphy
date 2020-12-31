(ns morphy.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [morphy.core :as sut]
            [clojure.java.io :as io]
            [datoteka.core :as fs]))

(def input-dir "test/morphy/resources/test-site/")
(def output-dir "test/morphy/resources/site/")

(def context {:input-dir input-dir :output-dir output-dir})

(defn cleanup! []
  (when (fs/exists? output-dir)
    (fs/delete output-dir)))

(deftest generate-site
  (cleanup!)
  (sut/generate-site context)

  (testing "it writes the right files"
    (let [result (->> output-dir
                      io/file
                      file-seq
                      (remove fs/directory?)
                      (map str)
                      set)]
      (is (= #{;; handles mustache templated-files
               "test/morphy/resources/site/index.html"

               ;; handles multiple files in root directory
               "test/morphy/resources/site/root-file/index.html"

               ;; does not break files that are already html
               "test/morphy/resources/site/already-html/index.html"

               ;; converts md->html, expands loose files
               "test/morphy/resources/site/blog/this-is-the-title/index.html"
               "test/morphy/resources/site/blog/a-title/index.html"

               ;; does not process assets in the assets directory
               "test/morphy/resources/site/assets/leave.css"

               ;; handles nested directories
               "test/morphy/resources/site/a-directory/nested/custom-title/index.html"
               "test/morphy/resources/site/a-directory/second-nested/a-file/index.html"}
             result))))

  (testing "copies assets as they are"
    (is (= ".body {\n  background: orange;\n}\n"
           (slurp "test/morphy/resources/site/assets/leave.css"))))

  (cleanup!))
