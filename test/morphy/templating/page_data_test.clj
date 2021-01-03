(ns morphy.templating.page-data-test
  (:require [morphy.templating.page-data :as sut]
            [clojure.test :refer [deftest testing is]]
            [morphy.test-utils :as u])
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

(deftest format-dates
  (testing "it formats dates and also adds a rss-formatted date"
    (let [formatted (sut/format-dates {:date (ZonedDateTime/of
                                               (LocalDateTime/of 2019, Month/MARCH, 28, 14, 33)
                                               (ZoneId/of "UTC")) })]
      (is (= "March 28, 2019" (:date formatted)))
      (is (= "Thu, 28 Mar 2019 14:33:00 GMT" (:rss-date formatted)))))

  (testing "it formats dates from front matter"
    (let [site (u/get-site "/templates/dates")]
      (is (= "Formatted date: January 1, 2020\n" (u/get-content site "page"))))))
