(ns morphy.templates-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as str]
            [morphy.test-utils :as u]))

(deftest basic-templating
  (let [site (u/get-site "templates/simple-layout")]
    (testing "it inserts plain pages into the layout as is, and works without partials"
      (is (= "In Layout! This is root content\n\n"
             (u/get-content site "root")))
      (is (= "In Layout! This is nested content\n\n"
             (u/get-content site "nested")))
      (is (= "In Layout! This is more nested content\n\n"
             (u/get-content site "more-nested"))))

    (testing "it gives the site as context to pages that are themselves templates"
      (is (= "In Layout! Root:
- /templated/
- /root/

Nested:
- /nested/nested/

More nested:
- /nested/more-nested/more-nested/

"
             (u/get-content site "templated")))))

  (testing "it maintains the order of files from the file list"
    (let [site (u/get-site "templates/ordering")]
      (is (= "- Top\n\n- Middle\n\n- Bottom\n\n"
             (u/get-content site ""))))))

(deftest user-overrides
  (let [site (u/get-site "templates/slugs")]
    (testing "it provides the slug in the page context"
      (is (str/starts-with?
            (u/get-content site "root-override") "slug: /root-override/"))
      (is (str/starts-with?
            (u/get-content site "customized") "slug: /customized/"))
      (is (str/starts-with?
            (u/get-content site "already-expanded-custom-slug") "slug: /already-expanded-custom-slug/"))))

  (let [site (u/get-site "templates/user-data")]
    (testing "it passes along custom user front matter to templates"
      (is (str/starts-with? (u/get-content site "root") "one - two - three"))
      (is (str/starts-with? (u/get-content site "file") "four - five - six")))))

(deftest layouts
  (let [site (u/get-site "templates/assets")]
    (testing "it inserts each non-asset page into the layout"
      (is (every? #(str/starts-with? (:content %) "Layout:") (:pages/templatable site))))

    (testing "it does not add content to asset pages"
      (is (every? #(nil? (:content %)) (:pages/assets site)))))

  (let [site (u/get-site "templates/layouts")]
    (testing "it uses the nearest default layout to the page"
      (is (str/starts-with? (u/get-content site "root")
                            "Default layout: Root"))
      (is (str/starts-with? (u/get-content site "nested")
                            "Closer layout next to file: Nested"))
      (is (str/starts-with? (u/get-content site "inside-nested")
                            "Closer layout next to file: Inside nested"))
      (is (str/starts-with? (u/get-content site "more-nested")
                            "Closest layout to more nested: More nested"))
      (is (str/starts-with? (u/get-content site "nested-no-layout-override")
                            "Default layout: Nested no override")))

    (testing "a user can override the layout in the front-matter"
      (is (str/starts-with? (u/get-content site "custom")
                            "Custom layout: Root"))
      (is (str/starts-with? (u/get-content site "nested-custom")
                            "Custom nested layout: Nested content")))

    (testing "a user can optionally specify 'no layout' for a page"
      (is (str/starts-with? (u/get-content site "no-layout")
                            "This is not a layout"))
      (is (str/starts-with? (u/get-content site "nested-no-layout")
                            "This is not a layout")))))

(deftest using-partials
  (let [site (u/get-site "templates/partials")
        just-layout (u/get-content site "")
        page (u/get-content site "a-page-title")]
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
      (is (str/includes? (u/get-content site "nested")
                         "Nested content. From partial: Closer plain partial"))
      (is (str/includes? (u/get-content site "more-nested")
                         "Deeper nested content. From partial: Closer plain partial"))
      (is (str/includes? (u/get-content site "deeply-nested")
                         "Different partial nested content. From partial: Different nested partial")))

    (testing "pages that use no layout can still use partials"
      (is (str/starts-with? (u/get-content site "no-layout-title")
                            "Just a plain partial - no layout")))))

(deftest site-metadata
  (let [site (u/get-site "templates/meta")]
    (testing "it exposes site metadata to normal pages"
      (is (str/includes? (u/get-content site "") "In page: https://test.com"))
      (is (re-find #"Last build time: \w+" (u/get-content site ""))))

    (testing "it exposes site metadata to layouts"
      (is (str/includes? (u/get-content site "with-layout")
                         "From layout: https://test.com"))
      (is (re-find #"Last build time: \w+"
                   (u/get-content site "with-layout"))))

    (testing "it exposes site metadata to partials"
      (is (str/includes? (u/get-content site "with-partial")
                         "From partial: https://test.com"))
      (is (re-find #"Last build time: \w+"
                   (u/get-content site "with-partial"))))))

(deftest overrides
  (let [site (u/get-site "templates/overrides")]
    (testing "it uses the right partials when the slug is overridden"
      (is (str/starts-with? (u/get-content site "root") "Partial: Override")))

    (testing "it uses the right layout when the slug is overridden"
      (is (str/starts-with? (u/get-content site "custom") "Nested layout override")))))
