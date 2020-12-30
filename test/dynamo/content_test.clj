(ns dynamo.content-test
  (:require [dynamo.content :as sut]
            [clojure.test :refer [deftest testing is]]
            [dynamo.test-utils :as u]
            [datoteka.core :as fs]))

(def root (str u/resources "content/"))

(deftest processing-content
  (testing "it converts md->html"
    (let [result (sut/process (u/test-page root "markdown.md"))]
      (is (= "<p>This is <em>not</em> double escaped <strong>markdown</strong>.</p>\n"
             (:content result)))
      (is (= "html" (-> result :site/path fs/ext)))))

  (testing "it leaves html as-is"
    (let [result (sut/process (u/test-page root "html.html"))]
      (is (= "<article>Already html</article>\n"
             (:content result)))
      (is (= "html" (-> result :site/path fs/ext)))))

  (testing "it passes through other extensions"
    (let [result (sut/process (u/test-page root "css.css"))]
      (is (= "css" (-> result :site/path fs/ext))))))
