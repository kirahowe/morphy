(ns dynamo.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [dynamo.core :as sut]
            [clojure.java.io :as io]))

(def input-dir "test/dynamo/resources/content/")
(def output-dir "test/dynamo/resources/site/")

(deftest copies-files-to-the-right-place
  (testing "turns content into a static site"
    (sut/generate-site {:input-dir input-dir :output-dir output-dir})
    (let [result (->> output-dir io/file file-seq (map str) set) ]
      (is (= #{"test/dynamo/resources/site"
               "test/dynamo/resources/site/a-file.html"
               "test/dynamo/resources/site/already-html.html" ;; does not mess with files that are already html
               "test/dynamo/resources/site/assets"
               "test/dynamo/resources/site/assets/leave.css" ;; does not process assets in the assets dir
               "test/dynamo/resources/site/a-directory"
               "test/dynamo/resources/site/a-directory/another-file.html"
               "test/dynamo/resources/site/a-directory/nested"
               "test/dynamo/resources/site/a-directory/nested/a-file.html"}
             result))))
  (cleanup!))
