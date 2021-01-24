(ns morphy.templating.site-model-test
  (:require [morphy.templating.site-model :as sut]
            [clojure.test :refer [deftest testing is]]
            [morphy.test-utils :as u]
            [morphy.core :as core]))

(deftest grouping
  (testing "group sort priority can be set manually"
    (let [site (u/get-site-model "templates/groups/custom-sorted" ["c" "b" "a"])]
      (is (= ["c" "b" "a"]
             (->> site :site/model :site/groups (map :group/label))))))

  (testing "manual group ordering can specify just one item"
    (let [site (u/get-site-model "templates/groups/custom-sorted" ["b"])]
      (is (= ["b" "a" "c"]
             (->> site :site/model :site/groups (map :group/label)))))))

(deftest templating-groups
  (let [site (u/get-site "templates/groups/with-ungrouped")]
    (testing "it adds posts to groups only if they specify a site/group"
      (is (= "Group A - 2
-----
group A one
group A two

-----
Group B - 3
-----
group B one
group B two
group B three

-----
"
             (u/get-content site ""))))))

(deftest adding-timestamps
  (let [site (u/get-site-model "templates/simple-layout")]
    (testing "it adds an rss-compatible last-modified timestamp"
      (is (re-find #"\w{3}, \d{2} \w{3} \d{4} \d{2}:\d{2}:\d{2} "
                   (-> site :site/model :meta/last-modified))))))
