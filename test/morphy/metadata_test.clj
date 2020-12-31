(ns morphy.metadata-test
  (:require [morphy.metadata :as sut]
            [clojure.test :refer [deftest testing is]]
            [datoteka.core :as fs]
            [morphy.test-utils :as u]))

(def root (str u/resources "metadata/"))

(defn- test-page [file-name]
  (u/test-page root file-name))

(defn- slug [{:keys [site/path]}]
  (-> path fs/parent str))

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
    (is (= "some/path/custom-slug-different-than-the-file-name"
           (slug (sut/extract (test-page "custom-slug.md")))))
    (is (= "random/custom-slug-different-than-the-file-name"
           (slug (sut/extract (test-page "nested/path/custom-slug.md")))))
    (is (= "custom-slug-not-the-title"
           (slug (sut/extract (test-page "not-the-title.md"))))))

  (testing "it slugifies the title if there is one"
    (is (= "custom-user-title"
           (slug (sut/extract (test-page "title-override.md")))))
    (is (= "nested/a-custom-title"
           (slug (sut/extract (test-page "nested/title-override.md"))))))

  (testing "it defaults the slug to the file name"
    (is (= "lots-of-front-matter"
           (slug (sut/extract (test-page "lots-of-front-matter.md"))))))

  (testing "it does not re-title slugs for current index files"
    (is (= "index.md"
           (-> (sut/extract (test-page "index.md")) :site/path str))))

  (testing "it does rename slugs for named leaf files"
    (is (= "lots-of-front-matter/index.md"
           (-> (sut/extract (test-page "lots-of-front-matter.md")) :site/path str)))))

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
