(ns dynamo.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [dynamo.core :as sut]
            [clojure.java.io :as io]
            [datoteka.core :as fs]))

(def input-dir "test/dynamo/resources/test-site/")
(def output-dir "test/dynamo/resources/site/")

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
               "test/dynamo/resources/site/index.html"

               ;; handles multiple files in root directory
               "test/dynamo/resources/site/root-file/index.html"

               ;; does not break files that are already html
               "test/dynamo/resources/site/already-html/index.html"

               ;; converts md->html, expands loose files
               "test/dynamo/resources/site/blog/a-post/index.html"
               "test/dynamo/resources/site/blog/another-post/index.html"

               ;; does not process assets in the assets directory
               "test/dynamo/resources/site/assets/leave.css"

               ;; handles nested directories
               "test/dynamo/resources/site/a-directory/nested/another-file/index.html"
               "test/dynamo/resources/site/a-directory/second-nested/a-file/index.html"}
             result))))

  (testing "copies assets as they are"
    (is (= ".body {\n  background: orange;\n}\n"
           (slurp "test/dynamo/resources/site/assets/leave.css"))))

  (cleanup!))

(deftest build-pages
  (testing "it sorts the posts in descending order by date"
    (let [pages (sut/build-pages input-dir)]
      (sc.api/spy)
      (is (= ["2020-01-02" "2020-01-01"]
             pages)))
    ))
