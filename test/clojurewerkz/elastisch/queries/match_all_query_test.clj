(ns clojurewerkz.elastisch.queries.match-all-query-test
  (:require [clojurewerkz.elastisch.rest.document      :as doc]
            [clojurewerkz.elastisch.rest.index         :as idx]
            [clojurewerkz.elastisch.query         :as q]
            [clojurewerkz.elastisch.fixtures :as fx])
  (:use clojure.test clojurewerkz.elastisch.rest.response))


(def ^{:const true} index-name "articles")
(def ^{:const true} index-type "article")

(defn- prepopulate-index
  [f]
  (idx/create index-name :mappings fx/articles-mapping)

  (doc/put index-name index-type "1" fx/article-on-elasticsearch)
  (doc/put index-name index-type "2" fx/article-on-lucene)
  (doc/put index-name index-type "3" fx/article-on-nueva-york)
  (doc/put index-name index-type "4" fx/article-on-austin)
  (idx/refresh index-name)
  (f))

(use-fixtures :each fx/reset-indexes prepopulate-index)

;;
;; field query
;;

(deftest ^{:query true} test-basic-match-all-query
  (let [response (doc/search index-name index-type :query (q/match-all))
        hits     (hits-from response)]
    (is (any-hits? response))
    (is (= 4 (total-hits response)))))
