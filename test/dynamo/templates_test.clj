(ns dynamo.templates-test
  (:require [dynamo.templates :as sut]
            [clojure.test :refer [deftest testing is]]
            [dynamo.test-utils :as u]
            [dynamo.core :as core]
            [clojure.string :as str]
            [datoteka.core :as fs])
  (:import java.time.ZonedDateTime
           java.time.LocalDateTime
           java.time.Month
           java.time.ZoneId))

(deftest populate-slug
  (testing "it inserts slug into each page"
    (let [slugged (sut/populate-slug {:site/path "index.html"})]
      (is (= "/" (:slug slugged))))

    (let [slugged (sut/populate-slug {:site/path "this-is-the-slug/index.html"})]
      (is (= "/this-is-the-slug/" (:slug slugged))))

    (let [slugged (sut/populate-slug {:site/path "nested/page-name/index.html"})]
      (is (= "/nested/page-name/" (:slug slugged))))

    (let [slugged (sut/populate-slug {:site/path "even-more/nested/page-name/index.html"})]
      (is (= "/even-more/nested/page-name/" (:slug slugged))))))

(defn- get-site [dir]
  (let [input-dir (str u/resources "templates/" dir)]
    (-> {:input-dir input-dir :root-url "https://test.com"}
        (merge (core/build-pages input-dir))
        sut/render)))

(defn- get-content [site search]
  (->> site
       :pages/templatable
       (filter (fn [{:keys [site/path]}]
                 (= search (-> path fs/parent (or "") fs/name str))))
       first
       :content))

(deftest format-dates
  (testing "it formats dates and also adds a rss-formatted date"
    (let [formatted (sut/format-dates {:date (ZonedDateTime/of
                                               (LocalDateTime/of 2019, Month/MARCH, 28, 14, 33)
                                               (ZoneId/of "UTC")) })]
      (is (= "Mar 28, 2019" (:date formatted)))
      (is (= "Thu, 28 Mar 2019 14:33:00 GMT" (:rss-date formatted)))))

  (testing "it formats dates from front matter"
    (let [site (get-site "dates")]
      (is (= "Formatted date: Jan 01, 2020\n" (get-content site "page"))))))

(deftest basic-templating
  (let [site (get-site "simple-layout")]
    (testing "it inserts plain pages into the layout as is, and works without partials"
      (is (= "In Layout! This is root content\n\n"
             (get-content site "root")))
      (is (= "In Layout! This is nested content\n\n"
             (get-content site "nested")))
      (is (= "In Layout! This is more nested content\n\n"
             (get-content site "more-nested"))))

    (testing "it gives the site as context to pages that are themselves templates"
      (is (= "In Layout! Root:
- /templated/
- /root/

Nested:
- /nested/nested/

More nested:
- /nested/more-nested/more-nested/

"
             (get-content site "templated")))))

  (testing "it maintains the order of files from the file list"
    (let [site (get-site "ordering")]
      (is (= "- Top\n\n- Middle\n\n- Bottom\n\n"
             (get-content site ""))))))

(deftest user-overrides
  (let [site (get-site "slugs")]
    (testing "it provides the slug in the page context"
      (is (str/starts-with?
            (get-content site "root-override") "slug: /root-override/"))
      (is (str/starts-with?
            (get-content site "customized") "slug: /customized/"))
      (is (str/starts-with?
            (get-content site "already-expanded-custom-slug") "slug: /already-expanded-custom-slug/"))))

  (let [site (get-site "user-data")]
    (testing "it passes along custom user front matter to templates"
      (is (str/starts-with? (get-content site "root") "one - two - three"))
      (is (str/starts-with? (get-content site "file") "four - five - six")))))

(deftest layouts
  (let [site (get-site "assets")]
    (testing "it inserts each non-asset page into the layout"
      (is (every? #(str/starts-with? (:content %) "Layout:") (:pages/templatable site))))

    (testing "it does not add content to asset pages"
      (is (every? #(nil? (:content %)) (:pages/assets site)))))

  (let [site (get-site "layouts")]
    (testing "it uses the nearest default layout to the page"
      (is (str/starts-with? (get-content site "root")
                            "Default layout: Root"))
      (is (str/starts-with? (get-content site "nested")
                            "Closer layout next to file: Nested"))
      (is (str/starts-with? (get-content site "inside-nested")
                            "Closer layout next to file: Inside nested"))
      (is (str/starts-with? (get-content site "more-nested")
                            "Closest layout to more nested: More nested"))
      (is (str/starts-with? (get-content site "nested-no-layout-override")
                            "Default layout: Nested no override")))

    (testing "a user can override the layout in the front-matter"
      (is (str/starts-with? (get-content site "custom")
                            "Custom layout: Root"))
      (is (str/starts-with? (get-content site "nested-custom")
                            "Custom nested layout: Nested content")))

    (testing "a user can optionally specify 'no layout' for a page"
      (is (str/starts-with? (get-content site "no-layout")
                            "This is not a layout"))
      (is (str/starts-with? (get-content site "nested-no-layout")
                            "This is not a layout")))))

(deftest using-partials
  (let [site (get-site "partials")
        just-layout (get-content site "")
        page (get-content site "a-page-title")]
    (testing "it renders plain partials"
      (is (str/includes? just-layout "Just a plain partial")
          "in layouts")
      (is (str/includes? page "Other plain partial")
          "in pages"))

    (testing "it passes the rendering context to the partials"
      (is (str/includes? just-layout "Page data used in partial: title - Index title"))
      (is (str/includes? page "Page data used in partial: title - A page title")))

    (testing "it passes the site context to partials used in layouts"
      (is (str/includes? just-layout "Site data used in partial:\nRoot file - title: Root file 2"))
      (is (str/includes? just-layout "Root file - title: Root file 1"))
      (is (str/includes? just-layout "Root file - title: Root file 2")))

    (testing "it passes the page context to partials used in pages"
      (is (str/includes? page "Page data used in partial: title - A page title")))

    (testing "it passes the site context to partials used in pages"
      (is (str/includes? page "Page content:"))
      (is (str/includes? page "Site data used in partial:\nRoot file - title: Root file 2")))

    (testing "it uses the nearest partial to the page"
      (is (str/includes? (get-content site "nested")
                         "Nested content. From partial: Closer plain partial"))
      (is (str/includes? (get-content site "more-nested")
                         "Deeper nested content. From partial: Closer plain partial"))
      (is (str/includes? (get-content site "deeply-nested")
                         "Different partial nested content. From partial: Different nested partial")))

    (testing "pages that use no layout can still use partials"
      (is (str/starts-with? (get-content site "no-layout-title")
                            "Just a plain partial - no layout")))))

(deftest site-metadata
  (let [site (get-site "meta")]
    (testing "exposes root url to normal pages"
      (is (str/includes? (get-content site "") "In page: https://test.com")))

    (testing "exposes last modified to normal pages"
      (is (re-find #"Last build time: \w+" (get-content site ""))))

    (testing "exposts root url to layouts"
      (is (str/includes? (get-content site "with-layout") "From layout: https://test.com")))

    (testing "exposes last modified to normal pages"
      (is (re-find #"Last build time: \w+" (get-content site "with-layout"))))

    (testing "exposts root url to partials"
      (is (str/includes? (get-content site "with-partial") "From partial: https://test.com")))

    (testing "exposes last modified to partials"
      (is (re-find #"Last build time: \w+" (get-content site "with-partial"))))))

(deftest overrides
  (let [site (get-site "overrides")]
    (testing "it uses the right partials when the slug is overridden"
      (is (str/starts-with? (get-content site "root") "Partial: Override")))

    (testing "it uses the right layout when the slug is overridden"
      (is (str/starts-with? (get-content site "custom") "Nested layout override")))))
