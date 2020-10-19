(ns dynamo.templates-test
  (:require [dynamo.templates :as sut]
            [clojure.test :refer [deftest testing is]]
            [dynamo.test-utils :as u]
            [dynamo.core :as core]
            [clojure.string :as str]
            [datoteka.core :as fs])
  (:import java.util.Date))

(deftest populate-slug
  (testing "it inserts slug and canonical-slug into each page"
    (let [slugged (sut/populate-slug {:path "index.html"})]
      (is (= "/" (:slug slugged)))
      (is (= "/index.html" (:canonical-slug slugged))))

    (let [slugged (sut/populate-slug {:path "this-is-the-slug/index.html"})]
      (is (= "/this-is-the-slug/" (:slug slugged)))
      (is (= "/this-is-the-slug/index.html" (:canonical-slug slugged))))

    (let [slugged (sut/populate-slug {:path "nested/page-name/index.html"})]
      (is (= "/nested/page-name/" (:slug slugged)))
      (is (= "/nested/page-name/index.html" (:canonical-slug slugged))))

    (let [slugged (sut/populate-slug {:path "even-more/nested/page-name/index.html"})]
      (is (= "/even-more/nested/page-name/" (:slug slugged)))
      (is (= "/even-more/nested/page-name/index.html"(:canonical-slug slugged))))))

(deftest format-dates
  (testing "it formats dates anywhere they appear")

  (testing "it formats nested dates"
    (let [formatted (sut/format-dates {:date (Date. 1111111111111)
                                       :nested {:date (Date. 1111211111111)
                                                :more-nested {:date (Date. 1113111111111)}}})]
      (is (= "March 17, 2005" (:date formatted)))
      (is (= "March 19, 2005" (-> formatted :nested :date)))
      (is (= "April 10, 2005" (-> formatted :nested :more-nested :date))))))

(defn- get-site [dir]
  (let [input-dir (str u/resources "templates/" dir)]
    (-> {:input-dir input-dir}
        (assoc :pages (core/build-pages input-dir))
        sut/render)))

(defn- get-non-asset-pages [site]
  (filter #(sut/templatable? (:path %)) (:pages site)))

(defn- get-asset-pages [site]
  (filter #(not (sut/templatable? (:path %))) (:pages site)))

(defn- get-content [site search]
  (->> site
       :pages
       (filter (fn [{:keys [path]}]
                 (= search (-> path fs/parent (or "") fs/name str))))
       first
       :content))

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
- /root/
- /templated/

Nested:
- /nested/nested/

More nested:
- /nested/more-nested/more-nested/

"
             (get-content site "templated"))))))

(deftest user-overrides
  (let [site (get-site "slugs")]
    (testing "it provides the slug and canonical slug in the page context"
      (is (str/starts-with?
            (get-content site "root-override")
            "slug: /root-override/ canonical-slug: /root-override/index.html"))
      (is (str/starts-with?
            (get-content site "customized")
            "slug: /nested/customized/ canonical-slug: /nested/customized/index.html"))
      (is (str/starts-with?
            (get-content site "already-expanded-custom-slug")
            "slug: /already-expanded-custom-slug/ canonical-slug: /already-expanded-custom-slug/index.html"))))

  (let [site (get-site "user-data")]
    (testing "it passes along custom user front matter to templates"
      (is (str/starts-with? (get-content site "root") "one - two - three"))
      (is (str/starts-with? (get-content site "file") "four - five - six")))))

(deftest layouts
  (let [site (get-site "assets")]
    (testing "it inserts each non-asset page into the layout"
      (is (every? #(str/starts-with? (:content %) "Layout:")
                  (get-non-asset-pages site))))

    (testing "it does not add content to asset pages"
      (is (every? #(nil? (:content %)) (get-asset-pages site)))))

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
      (is (str/includes? just-layout "plain partial")
          "in layouts")
      (is (str/includes? (get-content site "this-is-a-root-file") "plain partial")
          "in pages"))

    (testing "it passes the page context to partials used in layouts"
      (is (str/includes? just-layout "from partial - title: Index title")))

    (testing "it passes the site context to partials used in layouts"
      (is (str/includes? just-layout "Root file - title: This is a root file"))
      (is (str/includes? just-layout "From partial: Root file - title: This is another root file"))
      (is (str/includes? just-layout "Root file - title: Index title")))

    (testing "it passes the page context to partials used in pages"
      (is (str/includes? page "from partial - title: A page title")))

    (testing "it passes the site context to partials used in pages"
      (is (str/includes? page "Page content:"))
      (is (str/includes? page "Root file - title: This is a root file"))
      (is (str/includes? page "From partial: Root file - title: This is another root file")))

    (testing "it uses the nearest partial to the page"
      (is (str/includes? (get-content site "nested")
                         "Nested content. From partial: closer partial"))
      (is (str/includes? (get-content site "more-nested")
                         "Deeper nested content. From partial: closer partial"))
      (is (str/includes? (get-content site "deeply-nested")
                         "Different partial nested content. From partial: different nested partial")))

    (testing "partials have access to the site model"
      (is (str/includes? just-layout "Root file - title: This is a root file"))
      (is (str/includes? just-layout "Root file - title: This is another root file")))

    (testing "it passes the rendering context to the partials"
      (is (str/includes? just-layout "from partial - title: Index title"))
      (is (str/includes? page "from partial - title: A page title")))

    (testing "pages that use no layout can still use partials"
      (is (str/starts-with? (get-content site "no-layout")
                            "plain partial - no layout")))))
