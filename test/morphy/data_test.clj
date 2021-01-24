(ns morphy.data-test
  (:require [morphy.data :as sut]
            [clojure.test :refer [deftest testing is]]
            [morphy.test-utils :as u]
            [clojure.string :as str]
            [datoteka.core :as fs]))

(defn- build-site []
  (sut/load-pages {:input-dir (str u/resources "data")}))

(defn- select-pages [site]
  (-> site (select-keys [:pages/assets :pages/templatable]) vals flatten))

(deftest loading-pages
  (let [indexed-by-name (reduce (fn [result {:keys [site/path] :as page}]
                                  (assoc result (str path) page))
                                {}
                                (select-pages (build-site)))]

    (testing "it only returns pages"
      (is (every? #(re-find #"\..+$" %) (keys indexed-by-name))))

    (testing "it does not load asset contents"
      (is (not (contains? (get indexed-by-name" assets/styles.css") :content)))
      (is (not (contains? (get indexed-by-name" assets/scripts.js") :content)))
      (is (not (contains? (get indexed-by-name" dir/already-expanded/assets/style.css") :content))))

    (testing "it does not load templates"
      (is (not-any? #(re-find #"_" %) (keys indexed-by-name))))

    (testing "it does not load partials"
      (is (not-any? #(re-find #"partials" %) (keys indexed-by-name))))

    (testing "expands loose files into a directory with an index file"
      (is (contains? indexed-by-name "a-file/index.html"))
      (is (contains? indexed-by-name "dir/loose/index.html")))

    (testing "it does not expand paths for loose pages that are flagged"
      (is (contains? indexed-by-name "404.html"))
      (is (contains? indexed-by-name "dir/do-not-expand-me.html")))

    (testing "it does not change directories that already have an index in them"
      (is (contains? indexed-by-name "dir/already-expanded/index.html")))

    (testing "it names files that are themselves templates properly"
      (is (contains? indexed-by-name "index.html.mustache"))
      (is (contains? indexed-by-name "dir/templated/index.html.mustache")))))

(deftest dot-files
  (testing "it ignores arbitrarily-named dotfiles mixed with content"
    (let [site (build-site)]
      (is (= 3 (-> site :pages/assets count)))
      (is (= 7 (-> site :pages/templatable count)))
      (is (every? false? (->> (select-pages site)
                              (map :site/path)
                              (map fs/name)
                              (map #(str/starts-with? % "."))))))))
