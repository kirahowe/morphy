(ns morphy.data-test
  (:require [morphy.data :as sut]
            [clojure.test :refer [deftest testing is]]
            [morphy.test-utils :as u]))

(deftest loading-pages
  (let [result (->> (sut/load-pages (str u/resources "data"))
                    vals
                    flatten
                    (reduce (fn [result {:keys [site/path] :as page}]
                              (assoc result (str path) page))
                            {}))]

    (testing "it only returns pages"
      (is (every? #(re-find #"\..+$" %) (keys result))))

    (testing "it does not load asset contents"
      (is (not (contains? (get result "assets/styles.css") :content)))
      (is (not (contains? (get result "assets/scripts.js") :content)))
      (is (not (contains? (get result "dir/already-expanded/assets/style.css") :content))))

    (testing "it does not load templates"
      (is (not-any? #(re-find #"_" %) (keys result))))

    (testing "it does not load partials"
      (is (not-any? #(re-find #"partials" %) (keys result))))

    (testing "expands loose files into a directory with an index file"
      (is (contains? result "a-file/index.html"))
      (is (contains? result "dir/loose/index.html")))

    (testing "it does not expand paths for loose pages that are flagged"
      (is (contains? result "404.html"))
      (is (contains? result "dir/do-not-expand-me.html")))

    (testing "it does not change directories that already have an index in them"
      (is (contains? result "dir/already-expanded/index.html")))

    (testing "it names files that are themselves templates properly"
      (is (contains? result "index.html.mustache"))
      (is (contains? result "dir/templated/index.html.mustache")))))
