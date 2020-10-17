(ns dynamo.metadata-test
  (:require [dynamo.metadata :as sut]
            [clojure.test :refer [deftest testing is]]
            [dynamo.data :as data]
            [datoteka.core :as fs]
            [dynamo.test-utils :as u]))

(def root (str u/resources "metadata/"))

(defn- test-page [file-name]
  (data/->page u/resources (fs/path root file-name)))

(defn- slug [{:keys [path]}]
  (-> path fs/parent fs/name))

(deftest extract-test
  (testing "it pulls front-matter out to the same level as the page"
    (is (= {:my-var "Whatever"
            :a_list ["one" "two" "three"]
            :another/thing "whee"}
           (:front-matter (sut/extract (test-page "lots-of-front-matter.md"))))))

  (testing "it does not leave empty font-matter lying around"
    (is (false?
          (contains? (sut/extract (test-page "custom-slug.md"))
                     :front-matter)))
    (is (false?
          (contains? (sut/extract (test-page "nested/path/custom-slug.md"))
                     :front-matter)))
    (is (false?
          (contains? (sut/extract (test-page "first-line-no-front-matter.md"))
                     :front-matter)))))

(deftest renaming-slug-part-of-paths
  (testing "it prefers first a user specified slug if there is one"
    (is (= "custom-slug-different-than-the-file-name"
           (slug (sut/extract (test-page "custom-slug.md")))))
    (is (= "custom-slug-different-than-the-file-name"
           (slug (sut/extract (test-page "nested/path/custom-slug.md")))))
    (is (= "custom-slug-not-the-title"
           (slug (sut/extract (test-page "not-the-title.md"))))))

  (testing "it slugifies the title if there is one"
    (is (= "custom-user-title"
           (slug (sut/extract (test-page "title-override.md"))))))

  (testing "it defaults the slug to the file name"
    (is (= "lots-of-front-matter"
           (slug (sut/extract (test-page "lots-of-front-matter.md")))))))

(deftest getting-title
  (testing "it defaults the title to the first line that is a header and strips md headers"
    (is (= "This is the first line"
           (:title (sut/extract (test-page "first-line-no-front-matter.md")))))
    (is (= "This is the first line should be the title"
           (:title (sut/extract (test-page "first-line-with-front-matter.md"))))))

  (testing "it does not add a title if the first line is not a header"
    (let [no-title-page (sut/extract (test-page "no-header.html"))]
      (is (= nil (:title no-title-page)))
      (is (= nil (:has-title?  no-title-page)))))

  (testing "it's title is over-writable by user front matter"
    (is (= "Custom user title"
           (:title (sut/extract (test-page "title-override.md"))))))

  (testing "it adds a flag so templates can test for presence of a title"
    (is (= true (:has-title? (sut/extract (test-page "title-override.md")))))))
