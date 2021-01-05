(ns morphy.tags-test
  (:require [morphy.tags :as sut]
            [clojure.test :refer [deftest testing is]]
            [morphy.test-utils :as u]))

(deftest generating-tag-index-pages
  (testing "basic case"
    (let [site (->> (u/get-site-model "templates/tags/basic")
                    sut/generate-index-pages)]
      )

    ))

(deftest templating-tags
  (let [site (u/get-site "templates/tags/basic")]
    (testing "it groups posts by tag"
      (is (= "one - 2 - /tags/one
-----
In one only
In one and two

-----
two - 2 - /tags/two
-----
In two only
In one and two

-----
"
             (u/get-content site ""))))))

;; (deftest templating-tags
;;   (let [site (u/get-site "templates/tags/basic")]
;;     (testing "it creates an index page for each tag"
;;       (is (= "CONTENT" (u/get-content site "one")))
;;       (is (= "CONTENT" (u/get-content site "two"))))))
