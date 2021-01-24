(ns morphy.tags-test
  (:require [morphy.tags :as sut]
            [clojure.test :refer [deftest testing is]]
            [morphy.test-utils :as u]
            [clojure.string :as str]))

(defn- get-page [site path]
  (->> site
       :pages/templatable
       (filter #(= path (-> % :site/path str)))
       first))

(deftest tags-in-site-model
  (let [site (u/get-site "templates/tags/basic")]
    (testing "it groups posts by tag and makes them available in site model"
      (is (= "IN LAYOUT:
one - 2 - /tags/one/
-----
In one only
In one and two

-----
two - 2 - /tags/two/
-----
In two only
In one and two

-----

"
             (u/get-content site ""))))))

(deftest tag-index-pages
  (let [site (u/get-site "templates/tags/basic")
        tag-one-page (u/get-content site "one")
        tag-two-page (u/get-content site "two")]
    (testing "it creates an index page for each tag with a default layout"
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
             (str/replace tag-two-page #"\n" ""))))))

(deftest tag-index-layouts
  (let [site (u/get-site "templates/tags/custom-layout")]
    (testing "it prefers a configured layout if found in the magic spot"
      (is (str/starts-with? (u/get-content site "one") "This is a custom layout"))
      (is (str/starts-with? (u/get-content site "two") "This is a custom layout")))))

(deftest tag-feed-pages
  (testing "it generates an rss feed page for each tag automatically"
    (let [site (u/get-site "templates/tags/basic")
          feed-one-content (-> site (get-page "tags/one/feed.xml") :content)
          feed-two-content (-> site (get-page "tags/two/feed.xml") :content)]
      (is (str/starts-with? feed-one-content "<?xml"))
      (is (str/starts-with? feed-two-content "<?xml"))))

  (testing "it uses a custom rss template if found"
    (let [site (u/get-site "templates/tags/custom-layout")
          feed-one-content (-> site (get-page "tags/one/feed.xml") :content)
          feed-two-content (-> site (get-page "tags/two/feed.xml") :content)]
      (is (str/starts-with? feed-one-content "my custom rss feed"))
      (is (str/starts-with? feed-two-content "my custom rss feed")))))

(deftest no-tags
  (let [site (u/get-site "templates/simple-layout")]
    (testing "it does not generate any tag indexes when no pages have tags"
      (is (->> site
               :pages/templatable
               (map :slug)
               (map #(str/includes? % "tags"))
               (every? false?))))))
