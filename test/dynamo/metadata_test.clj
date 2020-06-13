(ns dynamo.metadata-test
  (:require [dynamo.metadata :as sut]
            [clojure.test :as t]))

(deftest extract-test
  (testing "it pulls front-matter out to the same level as the page"))
