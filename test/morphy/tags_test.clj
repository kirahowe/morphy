(ns morphy.tags-test
  (:require [morphy.tags :as sut]
            [clojure.test :refer [deftest testing is]]
            [morphy.test-utils :as u]
            [clojure.string :as str]))

(deftest generating-tag-index-pages
  (testing "basic case"
    (let [site (->> (u/get-site-model "templates/tags/basic")
                    sut/generate-index-pages)]
      )

    ))

(deftest tags-in-site-model
  (let [site (u/get-site "templates/tags/basic")]
    (testing "it groups posts by tag and makes them available in site model"
      (is (= "IN LAYOUT:
one - 2 - /tags/one
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

(deftest templating-tags
  (let [site (u/get-site "templates/tags/basic")
        tag-one-page (u/get-content site "one")
        tag-two-page (u/get-content site "two")]
    (testing "it creates an index page for each tag"
      (is (= (str "IN LAYOUT:"
                  "<h1>one</h1>"
                  "<ul>"
                  "<li><a href=/posts/title-one-only/>Title one only</a></li>"
                  "<li><a href=/posts/title-one-and-two/>Title one and two</a></li>"
                  "</ul>")
             (str/replace tag-one-page #"\n" "")))
      (is (= (str "IN LAYOUT:"
                  "<h1>two</h1>"
                  "<ul>"
                  "<li><a href=/posts/title-two-only/>Title two only</a></li>"
                  "<li><a href=/posts/title-one-and-two/>Title one and two</a></li>"
                  "</ul>")
             (str/replace tag-two-page #"\n" ""))))

    (testing "it inserts the index pages into the layout"
      (is (str/starts-with? tag-one-page "IN LAYOUT:"))
      (is (str/starts-with? tag-two-page "IN LAYOUT:")))))

(deftest no-tags
  (let [site (u/get-site "templates/simple-layout")]
    (testing "it does not generate any tag indexes when no pages have tags"
      (is (->> site
               :pages/templatable
               (map :slug)
               (map #(str/includes? % "tags"))
               (every? false?))))))
